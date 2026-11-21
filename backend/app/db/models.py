"""Import every module's ORM models so `Base.metadata` and the mapper registry are complete.

Alembic and any code that needs all tables should import this module. Add each new module's
models here.
"""

from app.db.base import Base
from app.modules.auth.models import OAuthAccount, OAuthProvider
from app.modules.users.models import User

metadata = Base.metadata

__all__ = ["OAuthAccount", "OAuthProvider", "User", "metadata"]
