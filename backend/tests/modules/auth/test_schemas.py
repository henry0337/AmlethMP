import pytest
from pydantic import ValidationError

from app.modules.auth.schemas import GoogleTokenResponse


def test_accepts_a_response_with_an_access_token() -> None:
    token = GoogleTokenResponse.model_validate({"access_token": "token", "expires_in": 3599})

    assert token.access_token == "token"


@pytest.mark.parametrize(
    "response",
    [
        pytest.param({}, id="missing"),
        pytest.param({"access_token": ""}, id="empty"),
        pytest.param({"access_token": 1}, id="not-a-string"),
        pytest.param([], id="list"),
        pytest.param(None, id="none")
    ]
)
def test_rejects_responses_without_a_usable_access_token(response: object) -> None:
    with pytest.raises(ValidationError):
        GoogleTokenResponse.model_validate(response)
