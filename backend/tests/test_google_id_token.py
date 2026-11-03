import time
from typing import Any

import jwt
import pytest
from cryptography.hazmat.primitives.asymmetric import rsa

from app.services.google_id_token import (
    GoogleCertsUnavailableError,
    GoogleIdTokenVerifier,
    InvalidGoogleTokenError,
)

CLIENT_ID = "test-web-client.apps.googleusercontent.com"
ISSUER = "https://accounts.google.com"

SIGNING_KEY = rsa.generate_private_key(public_exponent=65537, key_size=2048)
OTHER_KEY = rsa.generate_private_key(public_exponent=65537, key_size=2048)


class FakeSigningKey:
    key = SIGNING_KEY.public_key()


class FakeJwksClient:
    """Stands in for PyJWKClient so no test reaches Google."""

    def __init__(self, error: Exception | None = None) -> None:
        self.error = error

    def get_signing_key_from_jwt(self, token: str) -> FakeSigningKey:
        if self.error is not None:
            raise self.error
        return FakeSigningKey()


def make_token(private_key: Any = SIGNING_KEY, **overrides: Any) -> str:
    now = int(time.time())
    claims: dict[str, Any] = {
        "iss": ISSUER,
        "aud": CLIENT_ID,
        "sub": "1234567890",
        "iat": now,
        "exp": now + 3600,
        "email": "listener@example.com",
        "email_verified": True,
        "name": "Listener One",
        "picture": "https://example.com/avatar.png",
    }
    claims.update(overrides)
    claims = {key: value for key, value in claims.items() if value is not None}
    return jwt.encode(claims, private_key, algorithm="RS256")


def make_verifier(error: Exception | None = None) -> GoogleIdTokenVerifier:
    return GoogleIdTokenVerifier(CLIENT_ID, jwks_client=FakeJwksClient(error))  # type: ignore[arg-type]


async def test_valid_token_yields_the_identity() -> None:
    identity = await make_verifier().verify(make_token())

    assert identity.subject == "1234567890"
    assert identity.email == "listener@example.com"
    assert identity.email_verified is True
    assert identity.name == "Listener One"
    assert identity.picture == "https://example.com/avatar.png"


async def test_issuer_without_scheme_is_accepted() -> None:
    identity = await make_verifier().verify(make_token(iss="accounts.google.com"))

    assert identity.subject == "1234567890"


async def test_optional_claims_may_be_missing() -> None:
    token = make_token(email=None, email_verified=None, name=None, picture=None)

    identity = await make_verifier().verify(token)

    assert identity.email is None
    assert identity.email_verified is False
    assert identity.name is None
    assert identity.picture is None


@pytest.mark.parametrize(
    "token",
    [
        pytest.param(
            make_token(aud="another-app.apps.googleusercontent.com"),
            id="wrong audience",
        ),
        pytest.param(make_token(iss="https://evil.example.com"), id="wrong issuer"),
        pytest.param(make_token(exp=int(time.time()) - 3600), id="expired"),
        pytest.param(make_token(OTHER_KEY), id="signed by another key"),
        pytest.param(make_token(sub=None), id="missing subject"),
        pytest.param(make_token(sub="x" * 256), id="oversized subject"),
        pytest.param("not-a-jwt", id="garbage"),
    ],
)
async def test_invalid_token_is_rejected(token: str) -> None:
    with pytest.raises(InvalidGoogleTokenError):
        await make_verifier().verify(token)


async def test_unsigned_token_is_rejected() -> None:
    now = int(time.time())
    token = jwt.encode(
        {"iss": ISSUER, "aud": CLIENT_ID, "sub": "1", "exp": now + 3600},
        None,
        algorithm="none",
    )

    with pytest.raises(InvalidGoogleTokenError):
        await make_verifier().verify(token)


async def test_unreachable_google_is_not_reported_as_an_invalid_token() -> None:
    verifier = make_verifier(error=jwt.PyJWKClientConnectionError("offline"))

    with pytest.raises(GoogleCertsUnavailableError):
        await verifier.verify(make_token())


async def test_unknown_signing_key_is_an_invalid_token() -> None:
    verifier = make_verifier(error=jwt.PyJWKClientError("Unable to find a signing key"))

    with pytest.raises(InvalidGoogleTokenError):
        await verifier.verify(make_token())
