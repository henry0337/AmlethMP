"""Authentication endpoints backed by Google and Discord OAuth."""

from __future__ import annotations

import secrets
from urllib.parse import urlencode
from uuid import UUID

from fastapi import APIRouter, HTTPException, Request, status
from fastapi.responses import RedirectResponse, Response

from app.core.config import Settings, get_settings
from app.db.session import DatabaseSession
from app.modules.auth.discord import build_discord_authorization_url, fetch_discord_identity
from app.modules.auth.exceptions import OAuthError
from app.modules.auth.google import build_google_authorization_url, fetch_google_identity
from app.modules.auth.models import OAuthProvider
from app.modules.auth.schemas import CurrentUserResponse
from app.modules.auth.service import find_or_create_discord_user, find_or_create_google_user
from app.modules.users.models import User

router = APIRouter(prefix="/auth", tags=["authentication"])

OAUTH_STATE_KEY = "google_oauth_state"
OAUTH_VERIFIER_KEY = "google_oauth_verifier"
DISCORD_OAUTH_STATE_KEY = "discord_oauth_state"
DISCORD_OAUTH_VERIFIER_KEY = "discord_oauth_verifier"


def require_login_configuration(
    provider_name: str, client_id: str | None, client_secret: str | None, is_configured: bool
) -> tuple[str, str]:
    """Return provider credentials only when the complete server-side flow is enabled."""
    if not is_configured or client_id is None or client_secret is None:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=f"{provider_name} sign-in is not configured."
        )

    return client_id, client_secret


def require_google_login_configuration(settings: Settings) -> tuple[str, str]:
    """Return Google credentials only when the complete server-side flow is enabled."""
    secret = settings.google_client_secret
    return require_login_configuration(
        "Google",
        settings.google_client_id,
        secret.get_secret_value() if secret is not None else None,
        settings.google_login_is_configured
    )


def require_discord_login_configuration(settings: Settings) -> tuple[str, str]:
    """Return Discord credentials only when the complete server-side flow is enabled."""
    secret = settings.discord_client_secret
    return require_login_configuration(
        "Discord",
        settings.discord_client_id,
        secret.get_secret_value() if secret is not None else None,
        settings.discord_login_is_configured
    )


def frontend_login_url(settings: Settings, result: str, provider: OAuthProvider) -> str:
    query = urlencode({"login": result, "provider": provider.value})
    return f"{settings.web_origin.rstrip('/')}/?{query}"


def redirect_to_frontend(
    settings: Settings, result: str, provider: OAuthProvider
) -> RedirectResponse:
    return RedirectResponse(
        frontend_login_url(settings, result, provider), status_code=status.HTTP_303_SEE_OTHER
    )


def start_oauth_session(request: Request, state_key: str, verifier_key: str) -> tuple[str, str]:
    """Store fresh CSRF and PKCE values in the browser session and return them."""
    state = secrets.token_urlsafe(32)
    verifier = secrets.token_urlsafe(64)
    request.session[state_key] = state
    request.session[verifier_key] = verifier
    return state, verifier


def take_callback_values(
    request: Request, state_key: str, verifier_key: str, state: str | None, code: str | None
) -> tuple[str, str] | None:
    """Consume the one-time session values; return `(code, verifier)` for a valid callback."""
    expected_state = request.session.pop(state_key, None)
    verifier = request.session.pop(verifier_key, None)
    if (
        not isinstance(expected_state, str)
        or not isinstance(verifier, str)
        or not isinstance(state, str)
        or not secrets.compare_digest(state, expected_state)
        or not code
    ):
        return None

    return code, verifier


def login_result_for_http_error(error: HTTPException) -> str:
    return "account-conflict" if error.status_code == status.HTTP_409_CONFLICT else "error"


def start_session_for_user(request: Request, user: User) -> None:
    request.session.clear()
    request.session["user_id"] = str(user.id)


@router.get("/google/login", status_code=status.HTTP_307_TEMPORARY_REDIRECT)
async def begin_google_login(request: Request) -> RedirectResponse:
    """Redirect the browser to Google after storing CSRF and PKCE state in its session."""
    settings = get_settings()
    client_id, _ = require_google_login_configuration(settings)
    state, verifier = start_oauth_session(request, OAUTH_STATE_KEY, OAUTH_VERIFIER_KEY)

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

    callback = take_callback_values(request, OAUTH_STATE_KEY, OAUTH_VERIFIER_KEY, state, code)
    if error is not None:
        return redirect_to_frontend(settings, "cancelled", OAuthProvider.GOOGLE)
    if callback is None:
        return redirect_to_frontend(settings, "error", OAuthProvider.GOOGLE)

    authorization_code, verifier = callback
    try:
        identity = await fetch_google_identity(
            client_id=client_id,
            client_secret=client_secret,
            code=authorization_code,
            redirect_uri=settings.google_redirect_uri,
            verifier=verifier
        )
        user = await find_or_create_google_user(session, identity)
    except OAuthError:
        return redirect_to_frontend(settings, "error", OAuthProvider.GOOGLE)
    except HTTPException as error_response:
        return redirect_to_frontend(
            settings, login_result_for_http_error(error_response), OAuthProvider.GOOGLE
        )

    start_session_for_user(request, user)
    return redirect_to_frontend(settings, "success", OAuthProvider.GOOGLE)


@router.get("/discord/login", status_code=status.HTTP_307_TEMPORARY_REDIRECT)
async def begin_discord_login(request: Request) -> RedirectResponse:
    """Redirect the browser to Discord after storing CSRF and PKCE state in its session."""
    settings = get_settings()
    client_id, _ = require_discord_login_configuration(settings)
    state, verifier = start_oauth_session(
        request, DISCORD_OAUTH_STATE_KEY, DISCORD_OAUTH_VERIFIER_KEY
    )

    return RedirectResponse(
        build_discord_authorization_url(client_id, settings.discord_redirect_uri, state, verifier),
        status_code=status.HTTP_307_TEMPORARY_REDIRECT
    )


@router.get("/discord/callback")
async def complete_discord_login(
    request: Request,
    session: DatabaseSession,
    code: str | None = None,
    state: str | None = None,
    error: str | None = None
) -> RedirectResponse:
    """Validate Discord's callback, establish an app session, and return to the web app."""
    settings = get_settings()
    client_id, client_secret = require_discord_login_configuration(settings)

    callback = take_callback_values(
        request, DISCORD_OAUTH_STATE_KEY, DISCORD_OAUTH_VERIFIER_KEY, state, code
    )
    if error is not None:
        return redirect_to_frontend(settings, "cancelled", OAuthProvider.DISCORD)
    if callback is None:
        return redirect_to_frontend(settings, "error", OAuthProvider.DISCORD)

    authorization_code, verifier = callback
    try:
        identity = await fetch_discord_identity(
            client_id=client_id,
            client_secret=client_secret,
            code=authorization_code,
            redirect_uri=settings.discord_redirect_uri,
            verifier=verifier
        )
        user = await find_or_create_discord_user(session, identity)
    except OAuthError:
        return redirect_to_frontend(settings, "error", OAuthProvider.DISCORD)
    except HTTPException as error_response:
        return redirect_to_frontend(
            settings, login_result_for_http_error(error_response), OAuthProvider.DISCORD
        )

    start_session_for_user(request, user)
    return redirect_to_frontend(settings, "success", OAuthProvider.DISCORD)


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
