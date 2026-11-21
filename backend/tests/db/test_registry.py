import pytest
from sqlalchemy import Table
from sqlalchemy.orm import configure_mappers

from app.db.base import Base
from app.db.models import OAuthAccount, User

TABLES = Base.metadata.sorted_tables


def test_registry_contains_the_models_of_every_module() -> None:
    assert set(Base.metadata.tables) == {User.__tablename__, OAuthAccount.__tablename__}


@pytest.mark.parametrize("table", TABLES, ids=[table.name for table in TABLES])
def test_models_share_the_base_columns(table: Table) -> None:
    assert table.c.id.primary_key
    assert not table.c.created_at.nullable
    assert not table.c.updated_at.nullable
    assert table.metadata is Base.metadata


def test_relationships_between_modules_resolve() -> None:
    configure_mappers()
