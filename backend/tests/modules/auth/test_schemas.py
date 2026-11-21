import unittest

from pydantic import ValidationError

from app.modules.auth.schemas import GoogleTokenResponse


class GoogleTokenResponseTestCase(unittest.TestCase):
    def test_accepts_a_response_with_an_access_token(self) -> None:
        token = GoogleTokenResponse.model_validate({"access_token": "token", "expires_in": 3599})

        self.assertEqual(token.access_token, "token")

    def test_rejects_responses_without_a_usable_access_token(self) -> None:
        invalid_responses: list[object] = [{}, {"access_token": ""}, {"access_token": 1}, [], None]

        for response in invalid_responses:
            with self.subTest(response=response), self.assertRaises(ValidationError):
                GoogleTokenResponse.model_validate(response)
