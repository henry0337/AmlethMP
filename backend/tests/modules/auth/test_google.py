from urllib.parse import parse_qs, urlparse

from app.modules.auth.google import (
    GOOGLE_AUTHORIZE_URL,
    GOOGLE_SCOPES,
    build_google_authorization_url,
    create_code_challenge
)

REDIRECT_URI = "http://localhost:8000/auth/google/callback"


def test_create_code_challenge_uses_s256_without_padding() -> None:
    verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"

    assert create_code_challenge(verifier) == "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM"


def test_google_authorization_url_contains_oauth_and_pkce_parameters() -> None:
    url = build_google_authorization_url("client-id", REDIRECT_URI, "csrf-state", "verifier")
    parsed = urlparse(url)

    assert f"{parsed.scheme}://{parsed.netloc}{parsed.path}" == GOOGLE_AUTHORIZE_URL
    assert parse_qs(parsed.query) == {
        "client_id": ["client-id"],
        "code_challenge": [create_code_challenge("verifier")],
        "code_challenge_method": ["S256"],
        "redirect_uri": [REDIRECT_URI],
        "response_type": ["code"],
        "scope": [GOOGLE_SCOPES],
        "state": ["csrf-state"]
    }
