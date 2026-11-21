from typing import cast

from sqlalchemy import Table
from sqlalchemy.schema import ColumnCollectionConstraint

from app.modules.auth.models import OAuthAccount, OAuthProvider, enum_values


def test_oauth_account_requires_a_unique_provider_identity() -> None:
    constraints = {
        constraint.name: tuple(column.name for column in constraint.columns)
        for constraint in cast(Table, OAuthAccount.__table__).constraints
        if isinstance(constraint, ColumnCollectionConstraint) and constraint.name is not None
    }

    assert constraints["uq_oauth_accounts_provider_account"] == ("provider", "provider_account_id")


def test_provider_column_stores_enum_values_instead_of_member_names() -> None:
    assert enum_values(OAuthProvider) == ["google", "discord"]
