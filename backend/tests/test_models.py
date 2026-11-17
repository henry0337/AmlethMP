import unittest

from app.db.base import Base
from app.models import OAuthAccount, User


class ModelTestCase(unittest.TestCase):
    def test_models_share_the_base_columns(self) -> None:
        for model in (User, OAuthAccount):
            table = model.__table__

            self.assertTrue(table.c.id.primary_key)
            self.assertFalse(table.c.created_at.nullable)
            self.assertFalse(table.c.updated_at.nullable)
            self.assertIs(table.metadata, Base.metadata)

    def test_oauth_account_requires_a_unique_provider_identity(self) -> None:
        constraints = {
            constraint.name: tuple(column.name for column in constraint.columns)
            for constraint in OAuthAccount.__table__.constraints
            if constraint.name is not None
        }

        self.assertEqual(
            constraints["uq_oauth_accounts_provider_account"],
            ("provider", "provider_account_id"),
        )
