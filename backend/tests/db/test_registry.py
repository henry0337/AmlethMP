import unittest

from sqlalchemy.orm import configure_mappers

from app.db.base import Base
from app.db.models import OAuthAccount, User


class ModelRegistryTestCase(unittest.TestCase):
    def test_registry_contains_the_models_of_every_module(self) -> None:
        self.assertEqual(
            set(Base.metadata.tables), {User.__tablename__, OAuthAccount.__tablename__}
        )

    def test_models_share_the_base_columns(self) -> None:
        for table in Base.metadata.tables.values():
            self.assertTrue(table.c.id.primary_key)
            self.assertFalse(table.c.created_at.nullable)
            self.assertFalse(table.c.updated_at.nullable)
            self.assertIs(table.metadata, Base.metadata)

    def test_relationships_between_modules_resolve(self) -> None:
        configure_mappers()
