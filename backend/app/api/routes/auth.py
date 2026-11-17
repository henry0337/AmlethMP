"""Authentication endpoints backed by Google OAuth."""

from __future__ import annotations

import base64
import hashlib
import logging
import secrets
from typing import Annotated
from urllib.parse import urlencode
from uuid import UUID

import httpx
from fastapi import APIRouter, Depends, HTTPException, Request, status
from fastapi.responses import RedirectResponse, Response
from pydantic import BaseModel, Field, ValidationError
from sqlalchemy import select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import joinedload

from app.core.config import Settings, get_settings
from app.db.session import get_db_session
from app.models import OAuthAccount, OAuthProvider, User

router = APIRouter(prefix="/auth", tags=["authentication"])
logger = logging.getLogger(__name__)

GOOGLE_AUTHORIZE_URL = "https://accounts.google.com/o/oauth2/v2/auth"
GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token"
GOOGLE_USERINFO_URL = "https://openidconnect.googleapis.com/v1/userinfo"
GOOGLE_SCOPES = "openid email profile"
OAUTH_STATE_KEY = "google_oauth_state"
OAUTH_VERIFIER_KEY = "google_oauth_verifier"

DatabaseSession = Annotated[AsyncSession, Depends(get_db_session)]


class GoogleIdentity(BaseModel):
    """The subset of OpenID Connect user info required by AmlethMP."""

    sub: str = Field(min_length=1, max_length=255)
    email: str = Field(min_length=1, max_length=320)
    email_verified: bool = False
    name: str | None = Field(default=None, max_length=100)
    picture: str | None = Field(default=None, max_length=2048)


class CurrentUserResponse(BaseModel):
    id: UUID
    email: str | None
    display_name: str | None
    avatar_url: str | None


class GoogleOAuthError(Exception):
    """Raised when Google does not complete an OAuth request successfully."""


def create_code_challenge(verifier: str) -> str:
    """Create an RFC 7636 S256 challenge for an OAuth PKCE verifier."""
    digest = hashlib.sha256(verifier.encode("ascii")).digest()
    return base64.urlsafe_b64encode(digest).rstrip(b"=").decode("ascii")


def build_google_authorization_url(client_id: str, redirect_uri: str, state: str, verifier: str) -> str:
    """Create the Google authorization URL with least-privilege identity scopes."""
    query = urlencode(
        {
            "client_id": client_id,
            "code_challenge": create_code_challenge(verifier),
            "code_challenge_method": "S256",
            "redirect_uri": redirect_uri,
            "response_type": "code",
            "scope": GOOGLE_SCOPES,
            "state": state,
        }
    )
    return f"{GOOGLE_AUTHORIZE_URL}?{query}"


def require_google_login_configuration(settings: Settings) -> tuple[str, str]:
    """Return Google credentials only when the complete server-side flow is enabled."""
    if not settings.google_login_is_configured:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Google sign-in is not configured.",
        )

    client_id = settings.google_client_id
    client_secret = settings.google_client_secret
    if client_id is None or client_secret is None:  # Defensive narrowing for type checkers.
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Google sign-in is not configured.",
        )

    return client_id, client_secret.get_secret_value()


def frontend_login_url(settings: Settings, result: str) -> str:
    return f"{settings.web_origin.rstrip('/')}/?{urlencode({'login': result})}"


async def fetch_google_identity(
    *,
    client_id: str,
    client_secret: str,
    code: str,
    redirect_uri: str,
    verifier: str,
) -> GoogleIdentity:
    """Exchange a one-time code and retrieve the signed-in user's profile."""
    try:
        async with httpx.AsyncClient(timeout=httpx.Timeout(10.0, connect=5.0)) as client:
            token_response = await client.post(
                GOOGLE_TOKEN_URL,
                data={
                    "client_id": client_id,
                    "client_secret": client_secret,
                    "code": code,
                    "code_verifier": verifier,
                    "grant_type": "authorization_code",
                    "redirect_uri": redirect_uri,
                },
            )
            token_response.raise_for_status()
            token_data = token_response.json()

            access_token = token_data.get("access_token") if isinstance(token_data, dict) else None
            if not isinstance(access_token, str) or not access_token:
                raise GoogleOAuthError

            user_info_response = await client.get(
                GOOGLE_USERINFO_URL,
                headers={"Authorization": f"Bearer {access_token}"},
            )
            user_info_response.raise_for_status()
            identity = GoogleIdentity.model_validate(user_info_response.json())
    except (httpx.HTTPError, ValidationError, ValueError) as error:
        raise GoogleOAuthError from error

    if not identity.email_verified:
        raise GoogleOAuthError

    return identity


async def find_or_create_google_user(session: AsyncSession, identity: GoogleIdentity) -> User:
    """Find the Google account owner or create a new user with that identity."""
    account = await session.scalar(
        select(OAuthAccount)
        .options(joinedload(OAuthAccount.user))
        .where(
            OAuthAccount.provider == OAuthProvider.GOOGLE,
            OAuthAccount.provider_account_id == identity.sub,
        )
    )
    if account is not None:
        if not account.user.is_active:
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="This account is disabled.")
        return account.user

    existing_user = await session.scalar(select(User).where(User.email == identity.email.lower()))
    if existing_user is not None:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="An account with this email already exists.",
        )

    user = User(
        email=identity.email.lower(),
        display_name=identity.name,
        avatar_url=identity.picture,
    )
    session.add(user)
    await session.flush()
    session.add(
        OAuthAccount(
            user_id=user.id,
            provider=OAuthProvider.GOOGLE,
            provider_account_id=identity.sub,
        )
    )

    try:
        await session.commit()
    except IntegrityError as error:
        await session.rollback()
        logger.warning("Could not create a Google OAuth account because of a concurrent update.")
        raise GoogleOAuthError from error

    return user


@router.get("/google/login", status_code=status.HTTP_307_TEMPORARY_REDIRECT)
async def begin_google_login(request: Request) -> RedirectResponse:
    """Redirect the browser to Google after storing CSRF and PKCE state in its session."""
    settings = get_settings()
    client_id, _ = require_google_login_configuration(settings)
    state = secrets.token_urlsafe(32)
    verifier = secrets.token_urlsafe(64)
    request.session[OAUTH_STATE_KEY] = state
    request.session[OAUTH_VERIFIER_KEY] = verifier

    return RedirectResponse(
        build_google_authorization_url(client_id, settings.google_redirect_uri, state, verifier),
        status_code=status.HTTP_307_TEMPORARY_REDIRECT,
    )


@router.get("/google/callback")
async def complete_google_login(
    request: Request,
    session: DatabaseSession,
    code: str | None = None,
    state: str | None = None,
    error: str | None = None,
) -> RedirectResponse:
    """Validate Google's callback, establish an app session, and return to the web app."""
    settings = get_settings()
    client_id, client_secret = require_google_login_configuration(settings)

    expected_state = request.session.pop(OAUTH_STATE_KEY, None)
    verifier = request.session.pop(OAUTH_VERIFIER_KEY, None)
    if error is not None:
        return RedirectResponse(frontend_login_url(settings, "cancelled"), status_code=status.HTTP_303_SEE_OTHER)
    if (
        not isinstance(expected_state, str)
        or not isinstance(verifier, str)
        or not isinstance(state, str)
        or not secrets.compare_digest(state, expected_state)
        or not code
    ):
        return RedirectResponse(frontend_login_url(settings, "error"), status_code=status.HTTP_303_SEE_OTHER)

    try:
        identity = await fetch_google_identity(
            client_id=client_id,
            client_secret=client_secret,
            code=code,
            redirect_uri=settings.google_redirect_uri,
            verifier=verifier,
        )
        user = await find_or_create_google_user(session, identity)
    except GoogleOAuthError:
        return RedirectResponse(frontend_login_url(settings, "error"), status_code=status.HTTP_303_SEE_OTHER)
    except HTTPException as error_response:
        result = "account-conflict" if error_response.status_code == status.HTTP_409_CONFLICT else "error"
        return RedirectResponse(frontend_login_url(settings, result), status_code=status.HTTP_303_SEE_OTHER)

    request.session.clear()
    request.session["user_id"] = str(user.id)
    return RedirectResponse(frontend_login_url(settings, "success"), status_code=status.HTTP_303_SEE_OTHER)


@router.get("/me", response_model=CurrentUserResponse)
async def get_current_user(request: Request, session: DatabaseSession) -> CurrentUserResponse:
    """Return the user represented by the current signed, HttpOnly session cookie."""
    raw_user_id = request.session.get("user_id")
    try:
        user_id = UUID(raw_user_id) if isinstance(raw_user_id, str) else None
    except ValueError:
        user_id = None

    if user_id is None:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Authentication is required.")

    user = await session.get(User, user_id)
    if user is None or not user.is_active:
        request.session.clear()
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Authentication is required.")

    return CurrentUserResponse(
        id=user.id,
        email=user.email,
        display_name=user.display_name,
        avatar_url=user.avatar_url,
    )


@router.post("/logout", status_code=status.HTTP_204_NO_CONTENT)
async def logout(request: Request) -> Response:
    """Clear the current browser session."""
    request.session.clear()
    return Response(status_code=status.HTTP_204_NO_CONTENT)
