from urllib.parse import parse_qs, urlparse

import pytest
from fastapi import HTTPException, Request
from pydantic import SecretStr
from pydantic_settings import SettingsConfigDict

from app.core.config import Settings
from app.modules.auth.models import OAuthProvider
from app.modules.auth.router import (
    frontend_login_url,
    login_result_for_http_error,
    require_login_configuration,
    start_oauth_session,
    take_callback_values
)

STATE_KEY = "test_state"
VERIFIER_KEY = "test_verifier"


def make_request(session: dict[str, object]) -> Request:
    return Request({"type": "http", "session": session})


class IsolatedSettings(Settings):
    """Settings that ignore the developer's real `.env` file."""

    model_config = SettingsConfigDict(env_file=None)


def make_settings(
    *,
    web_origin: str = "http://localhost:3000",
    discord_client_id: str | None = None,
    discord_client_secret: str | None = None,
    auth_session_secret: str | None = None
) -> Settings:
    return IsolatedSettings(
        database_url="postgresql+asyncpg://user:pw@localhost/db",
        web_origin=web_origin,
        discord_client_id=discord_client_id,
        discord_client_secret=SecretStr(discord_client_secret) if discord_client_secret else None,
        auth_session_secret=SecretStr(auth_session_secret) if auth_session_secret else None
    )


def test_start_oauth_session_stores_fresh_state_and_verifier() -> None:
    request = make_request({})

    state, verifier = start_oauth_session(request, STATE_KEY, VERIFIER_KEY)

    assert request.session == {STATE_KEY: state, VERIFIER_KEY: verifier}
    assert start_oauth_session(make_request({}), STATE_KEY, VERIFIER_KEY) != (state, verifier)


def test_callback_values_are_returned_once_when_the_state_matches() -> None:
    request = make_request({STATE_KEY: "state", VERIFIER_KEY: "verifier"})

    assert take_callback_values(request, STATE_KEY, VERIFIER_KEY, "state", "code") == (
        "code",
        "verifier"
    )
    assert take_callback_values(request, STATE_KEY, VERIFIER_KEY, "state", "code") is None


@pytest.mark.parametrize(
    ("session", "state", "code"),
    [
        pytest.param({STATE_KEY: "state", VERIFIER_KEY: "v"}, "other", "code", id="wrong-state"),
        pytest.param({STATE_KEY: "state", VERIFIER_KEY: "v"}, None, "code", id="missing-state"),
        pytest.param({STATE_KEY: "state", VERIFIER_KEY: "v"}, "state", None, id="missing-code"),
        pytest.param({STATE_KEY: "state", VERIFIER_KEY: "v"}, "state", "", id="empty-code"),
        pytest.param({VERIFIER_KEY: "v"}, "state", "code", id="no-stored-state"),
        pytest.param({STATE_KEY: "state"}, "state", "code", id="no-stored-verifier"),
        pytest.param({}, "state", "code", id="empty-session")
    ]
)
def test_callback_values_are_rejected_when_invalid(
    session: dict[str, object], state: str | None, code: str | None
) -> None:
    request = make_request(session)

    assert take_callback_values(request, STATE_KEY, VERIFIER_KEY, state, code) is None
    assert STATE_KEY not in request.session
    assert VERIFIER_KEY not in request.session


@pytest.mark.parametrize("provider", list(OAuthProvider))
def test_frontend_login_url_reports_the_result_and_provider(provider: OAuthProvider) -> None:
    settings = make_settings(web_origin="http://localhost:3000/")

    parsed = urlparse(frontend_login_url(settings, "success", provider))

    assert f"{parsed.scheme}://{parsed.netloc}{parsed.path}" == "http://localhost:3000/"
    assert parse_qs(parsed.query) == {"login": ["success"], "provider": [provider.value]}


@pytest.mark.parametrize(
    ("status_code", "result"), [(409, "account-conflict"), (403, "error"), (500, "error")]
)
def test_http_errors_map_to_a_login_result(status_code: int, result: str) -> None:
    assert login_result_for_http_error(HTTPException(status_code=status_code)) == result


def test_login_configuration_requires_every_credential() -> None:
    assert require_login_configuration("Discord", "id", "secret", True) == ("id", "secret")

    for client_id, client_secret, is_configured in [
        (None, "secret", True),
        ("id", None, True),
        ("id", "secret", False)
    ]:
        with pytest.raises(HTTPException) as error:
            require_login_configuration("Discord", client_id, client_secret, is_configured)
        assert error.value.status_code == 503
        assert error.value.detail == "Discord sign-in is not configured."


def test_discord_login_is_configured_only_with_a_client_a_secret_and_a_session_secret() -> None:
    assert not make_settings().discord_login_is_configured
    assert not make_settings(
        discord_client_id="id", discord_client_secret="secret"
    ).discord_login_is_configured
    assert make_settings(
        discord_client_id="id", discord_client_secret="secret", auth_session_secret="session"
    ).discord_login_is_configured
