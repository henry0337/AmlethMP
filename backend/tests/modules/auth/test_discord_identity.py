import pytest
from pydantic import ValidationError

from app.modules.auth.schemas import DiscordIdentity, DiscordTokenResponse

SNOWFLAKE = "80351110224678912"
AVATAR_HASH = "8342729096ea3675442027381ff50dfe"


def make_identity(**overrides: object) -> DiscordIdentity:
    payload: dict[str, object] = {"id": SNOWFLAKE, "username": "nelly"}
    return DiscordIdentity.model_validate({**payload, **overrides})


def test_verified_email_is_returned_only_when_discord_verified_it() -> None:
    assert make_identity(email="a@example.com", verified=True).verified_email == "a@example.com"
    assert make_identity(email="a@example.com", verified=False).verified_email is None
    assert make_identity(email="a@example.com").verified_email is None


@pytest.mark.parametrize("email", [None, ""])
def test_a_missing_email_has_no_verified_email(email: str | None) -> None:
    assert make_identity(email=email, verified=True).verified_email is None


def test_display_name_prefers_the_global_name() -> None:
    assert make_identity(global_name="Nelly").display_name == "Nelly"
    assert make_identity().display_name == "nelly"
    assert make_identity(global_name=None).display_name == "nelly"


def test_avatar_url_is_built_from_the_avatar_hash() -> None:
    identity = make_identity(avatar=AVATAR_HASH)

    assert (
        identity.avatar_url == f"https://cdn.discordapp.com/avatars/{SNOWFLAKE}/{AVATAR_HASH}.png"
    )
    assert make_identity().avatar_url is None


@pytest.mark.parametrize(
    "overrides",
    [
        pytest.param({"id": ""}, id="empty-id"),
        pytest.param({"id": "abc"}, id="non-numeric-id"),
        pytest.param({"username": ""}, id="empty-username"),
        pytest.param({"avatar": "../evil"}, id="avatar-with-path"),
        pytest.param({"avatar": "a b"}, id="avatar-with-space")
    ]
)
def test_rejects_unsafe_or_incomplete_profiles(overrides: dict[str, object]) -> None:
    with pytest.raises(ValidationError):
        make_identity(**overrides)


def test_token_response_requires_an_access_token() -> None:
    assert DiscordTokenResponse.model_validate({"access_token": "t"}).access_token == "t"

    with pytest.raises(ValidationError):
        DiscordTokenResponse.model_validate({"access_token": ""})
