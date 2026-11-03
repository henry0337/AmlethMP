from dataclasses import dataclass

import anyio.to_thread
import jwt
from jwt import PyJWKClient

GOOGLE_JWKS_URL = "https://www.googleapis.com/oauth2/v3/certs"
GOOGLE_ISSUERS = ["https://accounts.google.com", "accounts.google.com"]
CLOCK_SKEW_SECONDS = 10

# Limits of the columns the identity ends up in.
MAX_SUBJECT_LENGTH = 255
MAX_EMAIL_LENGTH = 320
MAX_PICTURE_LENGTH = 2048


class InvalidGoogleTokenError(Exception):
    """The ID token is malformed, expired, or was not issued by Google for this app."""


class GoogleCertsUnavailableError(Exception):
    """Google's signing keys could not be fetched, so the token could not be checked."""


@dataclass(frozen=True, slots=True)
class GoogleIdentity:
    subject: str
    email: str | None
    email_verified: bool
    name: str | None
    picture: str | None


class GoogleIdTokenVerifier:
    """Checks the signature, issuer, audience and expiry of a Google ID token."""

    def __init__(self, client_id: str, jwks_client: PyJWKClient | None = None) -> None:
        self._client_id = client_id
        self._jwks_client = jwks_client or PyJWKClient(
            GOOGLE_JWKS_URL, cache_keys=True, lifespan=3600, timeout=5
        )

    async def verify(self, id_token: str) -> GoogleIdentity:
        # PyJWKClient fetches the keys with urllib, which blocks, so keep it off the event loop.
        return await anyio.to_thread.run_sync(self._verify, id_token)

    def _verify(self, id_token: str) -> GoogleIdentity:
        try:
            signing_key = self._jwks_client.get_signing_key_from_jwt(id_token)
            claims = jwt.decode(
                id_token,
                signing_key.key,
                algorithms=["RS256"],
                audience=self._client_id,
                issuer=GOOGLE_ISSUERS,
                leeway=CLOCK_SKEW_SECONDS,
                options={"require": ["exp", "iss", "aud", "sub"]},
            )
        except jwt.PyJWKClientConnectionError as error:
            raise GoogleCertsUnavailableError from error
        except jwt.PyJWTError as error:
            raise InvalidGoogleTokenError(str(error)) from error

        return _identity_from_claims(claims)


def _identity_from_claims(claims: dict[str, object]) -> GoogleIdentity:
    subject = claims["sub"]
    if not isinstance(subject, str) or not 0 < len(subject) <= MAX_SUBJECT_LENGTH:
        raise InvalidGoogleTokenError("Unusable sub claim.")

    return GoogleIdentity(
        subject=subject,
        email=_text_claim(claims, "email", MAX_EMAIL_LENGTH),
        email_verified=claims.get("email_verified") is True,
        name=_text_claim(claims, "name", None),
        picture=_text_claim(claims, "picture", MAX_PICTURE_LENGTH),
    )


def _text_claim(
    claims: dict[str, object], key: str, max_length: int | None
) -> str | None:
    value = claims.get(key)
    if not isinstance(value, str) or not value.strip():
        return None
    if max_length is not None and len(value) > max_length:
        return None
    return value.strip()
