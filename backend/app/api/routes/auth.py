import logging

from fastapi import APIRouter, HTTPException, status

from app.api.deps import DbSession, GoogleVerifier, SettingsDep
from app.schemas.auth import GoogleSignInRequest, TokenResponse
from app.services import auth as auth_service
from app.services.google_id_token import (
    GoogleCertsUnavailableError,
    InvalidGoogleTokenError,
)

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/auth", tags=["auth"])


@router.post(
    "/google",
    summary="Sign in with a Google ID token",
    description=(
        "Exchanges the ID token the Android app got from Google for this API's own access and "
        "refresh tokens. The user is created on the first sign-in."
    ),
    responses={
        status.HTTP_401_UNAUTHORIZED: {
            "description": "The Google ID token is not valid."
        },
        status.HTTP_403_FORBIDDEN: {
            "description": "The account is disabled or deleted."
        },
        status.HTTP_503_SERVICE_UNAVAILABLE: {
            "description": "Google's signing keys could not be fetched; retry later."
        },
    },
)
async def sign_in_with_google(
    body: GoogleSignInRequest,
    session: DbSession,
    verifier: GoogleVerifier,
    settings: SettingsDep,
) -> TokenResponse:
    try:
        identity = await verifier.verify(body.id_token)
    except InvalidGoogleTokenError as error:
        logger.info("Rejected a Google ID token: %s", error)
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid Google ID token.",
        ) from error
    except GoogleCertsUnavailableError as error:
        logger.warning("Could not fetch Google's signing keys.", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Google sign-in is temporarily unavailable.",
        ) from error

    try:
        return await auth_service.sign_in_with_google(
            session,
            identity,
            client=body.client,
            device_name=body.device_name,
            settings=settings,
        )
    except auth_service.AccountUnavailableError as error:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="This account is not available.",
        ) from error
