import unittest
from urllib.parse import parse_qs, urlparse

from app.api.routes.auth import (
    GOOGLE_AUTHORIZE_URL,
    GOOGLE_SCOPES,
    build_google_authorization_url,
    create_code_challenge,
)


class GoogleOAuthTestCase(unittest.TestCase):
    def test_create_code_challenge_uses_s256_without_padding(self) -> None:
        verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"

        self.assertEqual(
            create_code_challenge(verifier),
            "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
        )


    def test_google_authorization_url_contains_oauth_and_pkce_parameters(self) -> None:
        url = build_google_authorization_url(
            "client-id",
            "http://localhost:8000/auth/google/callback",
            "csrf-state",
            "verifier",
        )
        parsed = urlparse(url)
        query = parse_qs(parsed.query)

        self.assertEqual(
            f"{parsed.scheme}://{parsed.netloc}{parsed.path}",
            GOOGLE_AUTHORIZE_URL,
        )
        self.assertEqual(
            query,
            {
                "client_id": ["client-id"],
                "code_challenge": [create_code_challenge("verifier")],
                "code_challenge_method": ["S256"],
                "redirect_uri": ["http://localhost:8000/auth/google/callback"],
                "response_type": ["code"],
                "scope": [GOOGLE_SCOPES],
                "state": ["csrf-state"],
            },
        )
