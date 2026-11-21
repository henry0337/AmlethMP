"""Google OAuth 2.0 / OpenID Connect client with PKCE."""

import base64
import hashlib
from urllib.parse import urlencode

import httpx
from pydantic import ValidationError

from app.modules.auth.exceptions import GoogleOAuthError
from app.modules.auth.schemas import GoogleIdentity, GoogleTokenResponse

GOOGLE_AUTHORIZE_URL = "https://accounts.google.com/o/oauth2/v2/auth"
GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token"  # noqa: S105 - endpoint URL, not a secret
GOOGLE_USERINFO_URL = "https://openidconnect.googleapis.com/v1/userinfo"
GOOGLE_SCOPES = "openid email profile"


def create_code_challenge(verifier: str) -> str:
    """Create an RFC 7636 S256 challenge for an OAuth PKCE verifier."""
    digest = hashlib.sha256(verifier.encode("ascii")).digest()
    return base64.urlsafe_b64encode(digest).rstrip(b"=").decode("ascii")


def build_google_authorization_url(
    client_id: str, redirect_uri: str, state: str, verifier: str
) -> str:
    """Create the Google authorization URL with least-privilege identity scopes."""
    query = urlencode(
        {
            "client_id": client_id,
            "code_challenge": create_code_challenge(verifier),
            "code_challenge_method": "S256",
            "redirect_uri": redirect_uri,
            "response_type": "code",
            "scope": GOOGLE_SCOPES,
            "state": state
        }
    )
    return f"{GOOGLE_AUTHORIZE_URL}?{query}"


async def fetch_google_identity(
    *, client_id: str, client_secret: str, code: str, redirect_uri: str, verifier: str
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
                    "redirect_uri": redirect_uri
                }
            )
            token_response.raise_for_status()
            token = GoogleTokenResponse.model_validate(token_response.json())

            user_info_response = await client.get(
                GOOGLE_USERINFO_URL, headers={"Authorization": f"Bearer {token.access_token}"}
            )
            user_info_response.raise_for_status()
            identity = GoogleIdentity.model_validate(user_info_response.json())
    except (httpx.HTTPError, ValidationError, ValueError) as error:
        raise GoogleOAuthError from error

    if not identity.email_verified:
        raise GoogleOAuthError

    return identity
