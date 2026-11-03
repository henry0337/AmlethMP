from functools import lru_cache
from typing import Annotated

from fastapi import Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import Settings, get_settings
from app.db.session import get_db_session
from app.services.google_id_token import GoogleIdTokenVerifier

DbSession = Annotated[AsyncSession, Depends(get_db_session)]
SettingsDep = Annotated[Settings, Depends(get_settings)]


# One verifier for the whole process, so Google's signing keys are cached between requests.
@lru_cache
def get_google_verifier() -> GoogleIdTokenVerifier:
    return GoogleIdTokenVerifier(get_settings().google_web_client_id)


GoogleVerifier = Annotated[GoogleIdTokenVerifier, Depends(get_google_verifier)]
