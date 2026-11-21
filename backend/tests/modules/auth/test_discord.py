import asyncio
from collections.abc import Callable
from typing import Any
from urllib.parse import parse_qs, urlparse

import httpx
import pytest

from app.modules.auth import discord
from app.modules.auth.discord import (
    DISCORD_AUTHORIZE_URL,
    DISCORD_SCOPES,
    DISCORD_TOKEN_URL,
    DISCORD_USER_URL,
    build_discord_authorization_url,
    fetch_discord_identity
)
from app.modules.auth.exceptions import DiscordOAuthError
from app.modules.auth.google import create_code_challenge

REDIRECT_URI = "http://localhost:8000/auth/discord/callback"
USER_PAYLOAD = {
    "id": "80351110224678912",
    "username": "nelly",
    "global_name": "Nelly",
    "avatar": "8342729096ea3675442027381ff50dfe",
    "email": "nelly@example.com",
    "verified": True
}


def test_discord_authorization_url_contains_oauth_and_pkce_parameters() -> None:
    url = build_discord_authorization_url("client-id", REDIRECT_URI, "csrf-state", "verifier")
    parsed = urlparse(url)

    assert f"{parsed.scheme}://{parsed.netloc}{parsed.path}" == DISCORD_AUTHORIZE_URL
    assert parse_qs(parsed.query) == {
        "client_id": ["client-id"],
        "code_challenge": [create_code_challenge("verifier")],
        "code_challenge_method": ["S256"],
        "redirect_uri": [REDIRECT_URI],
        "response_type": ["code"],
        "scope": [DISCORD_SCOPES],
        "state": ["csrf-state"]
    }


def install_discord_transport(
    monkeypatch: pytest.MonkeyPatch, handler: Callable[[httpx.Request], httpx.Response]
) -> list[httpx.Request]:
    """Route the client created by `fetch_discord_identity` to an in-memory handler."""
    requests: list[httpx.Request] = []
    real_client = httpx.AsyncClient

    def record(request: httpx.Request) -> httpx.Response:
        requests.append(request)
        return handler(request)

    def make_client(**kwargs: Any) -> httpx.AsyncClient:
        return real_client(transport=httpx.MockTransport(record), **kwargs)

    monkeypatch.setattr(discord.httpx, "AsyncClient", make_client)
    return requests


def fetch_identity() -> Any:
    return asyncio.run(
        fetch_discord_identity(
            client_id="client-id",
            client_secret="client-secret",
            code="auth-code",
            redirect_uri=REDIRECT_URI,
            verifier="verifier"
        )
    )


def test_fetch_exchanges_the_code_then_reads_the_user_profile(
    monkeypatch: pytest.MonkeyPatch
) -> None:
    def handler(request: httpx.Request) -> httpx.Response:
        if str(request.url) == DISCORD_TOKEN_URL:
            return httpx.Response(200, json={"access_token": "access-token"})
        return httpx.Response(200, json=USER_PAYLOAD)

    requests = install_discord_transport(monkeypatch, handler)

    identity = fetch_identity()

    token_request, user_request = requests
    assert parse_qs(token_request.content.decode()) == {
        "client_id": ["client-id"],
        "client_secret": ["client-secret"],
        "code": ["auth-code"],
        "code_verifier": ["verifier"],
        "grant_type": ["authorization_code"],
        "redirect_uri": [REDIRECT_URI]
    }
    assert str(user_request.url) == DISCORD_USER_URL
    assert user_request.headers["Authorization"] == "Bearer access-token"
    assert identity.id == "80351110224678912"
    assert identity.verified_email == "nelly@example.com"


@pytest.mark.parametrize(
    ("token_status", "user_status"),
    [pytest.param(400, 200, id="token-rejected"), pytest.param(200, 401, id="user-rejected")]
)
def test_fetch_wraps_http_errors(
    monkeypatch: pytest.MonkeyPatch, token_status: int, user_status: int
) -> None:
    def handler(request: httpx.Request) -> httpx.Response:
        if str(request.url) == DISCORD_TOKEN_URL:
            return httpx.Response(token_status, json={"access_token": "access-token"})
        return httpx.Response(user_status, json=USER_PAYLOAD)

    install_discord_transport(monkeypatch, handler)

    with pytest.raises(DiscordOAuthError):
        fetch_identity()


def test_fetch_rejects_an_invalid_user_profile(monkeypatch: pytest.MonkeyPatch) -> None:
    def handler(request: httpx.Request) -> httpx.Response:
        if str(request.url) == DISCORD_TOKEN_URL:
            return httpx.Response(200, json={"access_token": "access-token"})
        return httpx.Response(200, json={**USER_PAYLOAD, "id": "not-a-snowflake"})

    install_discord_transport(monkeypatch, handler)

    with pytest.raises(DiscordOAuthError):
        fetch_identity()
