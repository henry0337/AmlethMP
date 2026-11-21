class OAuthError(Exception):
    """Raised when an OAuth provider does not complete a sign-in request successfully."""


class GoogleOAuthError(OAuthError):
    """Raised when Google does not complete an OAuth request successfully."""


class DiscordOAuthError(OAuthError):
    """Raised when Discord does not complete an OAuth request successfully."""
