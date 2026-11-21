"""Authentication endpoints backed by Google OAuth."""

from __future__ import annotations

import secrets
from urllib.parse import urlencode
from uuid import UUID

from fastapi import APIRouter, HTTPException, Request, status
from fastapi.responses import RedirectResponse, Response

from app.core.config import Settings, get_settings
from app.db.session import DatabaseSession
from app.modules.auth.exceptions import GoogleOAuthError
from app.modules.auth.google import build_google_authorization_url, fetch_google_identity
from app.modules.auth.schemas import CurrentUserResponse
from app.modules.auth.service import find_or_create_google_user
from app.modules.users.models import User

router = APIRouter(prefix="/auth", tags=["authentication"])

OAUTH_STATE_KEY = "google_oauth_state"
OAUTH_VERIFIER_KEY = "google_oauth_verifier"


def require_google_login_configuration(settings: Settings) -> tuple[str, str]:
    """Return Google credentials only when the complete server-side flow is enabled."""
    if not settings.google_login_is_configured:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Google sign-in is not configured."
        )

    client_id = settings.google_client_id
    client_secret = settings.google_client_secret
    if client_id is None or client_secret is None:  # Defensive narrowing for type checkers.
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Google sign-in is not configured."
        )

    return client_id, client_secret.get_secret_value()


def frontend_login_url(settings: Settings, result: str) -> str:
    return f"{settings.web_origin.rstrip('/')}/?{urlencode({'login': result})}"


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
        status_code=status.HTTP_307_TEMPORARY_REDIRECT
    )


@router.get("/google/callback")
async def complete_google_login(
    request: Request,
    session: DatabaseSession,
    code: str | None = None,
    state: str | None = None,
    error: str | None = None
) -> RedirectResponse:
    """Validate Google's callback, establish an app session, and return to the web app."""
    settings = get_settings()
    client_id, client_secret = require_google_login_configuration(settings)

    expected_state = request.session.pop(OAUTH_STATE_KEY, None)
    verifier = request.session.pop(OAUTH_VERIFIER_KEY, None)
    if error is not None:
        return RedirectResponse(
            frontend_login_url(settings, "cancelled"), status_code=status.HTTP_303_SEE_OTHER
        )
    if (
        not isinstance(expected_state, str)
        or not isinstance(verifier, str)
        or not isinstance(state, str)
        or not secrets.compare_digest(state, expected_state)
        or not code
    ):
        return RedirectResponse(
            frontend_login_url(settings, "error"), status_code=status.HTTP_303_SEE_OTHER
        )

    try:
        identity = await fetch_google_identity(
            client_id=client_id,
            client_secret=client_secret,
            code=code,
            redirect_uri=settings.google_redirect_uri,
            verifier=verifier
        )
        user = await find_or_create_google_user(session, identity)
    except GoogleOAuthError:
        return RedirectResponse(
            frontend_login_url(settings, "error"), status_code=status.HTTP_303_SEE_OTHER
        )
    except HTTPException as error_response:
        result = (
            "account-conflict"
            if error_response.status_code == status.HTTP_409_CONFLICT
            else "error"
        )
        return RedirectResponse(
            frontend_login_url(settings, result), status_code=status.HTTP_303_SEE_OTHER
        )

    request.session.clear()
    request.session["user_id"] = str(user.id)
    return RedirectResponse(
        frontend_login_url(settings, "success"), status_code=status.HTTP_303_SEE_OTHER
    )


@router.get("/me", response_model=CurrentUserResponse)
async def get_current_user(request: Request, session: DatabaseSession) -> CurrentUserResponse:
    """Return the user represented by the current signed, HttpOnly session cookie."""
    raw_user_id = request.session.get("user_id")
    try:
        user_id = UUID(raw_user_id) if isinstance(raw_user_id, str) else None
    except ValueError:
        user_id = None

    if user_id is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, detail="Authentication is required."
        )

    user = await session.get(User, user_id)
    if user is None or not user.is_active:
        request.session.clear()
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, detail="Authentication is required."
        )

    return CurrentUserResponse(
        id=user.id, email=user.email, display_name=user.display_name, avatar_url=user.avatar_url
    )


@router.post("/logout", status_code=status.HTTP_204_NO_CONTENT)
async def logout(request: Request) -> Response:
    """Clear the current browser session."""
    request.session.clear()
    return Response(status_code=status.HTTP_204_NO_CONTENT)
