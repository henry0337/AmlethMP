"""Discord OAuth 2.0 client with PKCE."""

from urllib.parse import urlencode

import httpx
from pydantic import ValidationError

from app.modules.auth.exceptions import DiscordOAuthError
from app.modules.auth.google import create_code_challenge
from app.modules.auth.schemas import DiscordIdentity, DiscordTokenResponse

DISCORD_AUTHORIZE_URL = "https://discord.com/oauth2/authorize"
DISCORD_TOKEN_URL = "https://discord.com/api/oauth2/token"  # noqa: S105 - endpoint URL, not a secret
DISCORD_USER_URL = "https://discord.com/api/users/@me"
DISCORD_SCOPES = "identify email"


def build_discord_authorization_url(
    client_id: str, redirect_uri: str, state: str, verifier: str
) -> str:
    """Create the Discord authorization URL with least-privilege identity scopes."""
    query = urlencode(
        {
            "client_id": client_id,
            "code_challenge": create_code_challenge(verifier),
            "code_challenge_method": "S256",
            "redirect_uri": redirect_uri,
            "response_type": "code",
            "scope": DISCORD_SCOPES,
            "state": state
        }
    )
    return f"{DISCORD_AUTHORIZE_URL}?{query}"


async def fetch_discord_identity(
    *, client_id: str, client_secret: str, code: str, redirect_uri: str, verifier: str
) -> DiscordIdentity:
    """Exchange a one-time code and retrieve the signed-in user's Discord profile."""
    try:
        async with httpx.AsyncClient(timeout=httpx.Timeout(10.0, connect=5.0)) as client:
            token_response = await client.post(
                DISCORD_TOKEN_URL,
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
            token = DiscordTokenResponse.model_validate(token_response.json())

            user_response = await client.get(
                DISCORD_USER_URL, headers={"Authorization": f"Bearer {token.access_token}"}
            )
            user_response.raise_for_status()
            return DiscordIdentity.model_validate(user_response.json())
    except (httpx.HTTPError, ValidationError, ValueError) as error:
        raise DiscordOAuthError from error
