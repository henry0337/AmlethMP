import enum

from sqlalchemy import Enum


def string_enum[E: enum.StrEnum](enum_class: type[E], *, name: str) -> Enum:
    # Stored as VARCHAR plus a CHECK constraint instead of a native PostgreSQL enum,
    # because adding a value later is then a plain constraint change.
    return Enum(
        enum_class,
        name=name,
        native_enum=False,
        length=max(len(member.value) for member in enum_class),
        create_constraint=True,
        validate_strings=True,
        values_callable=lambda members: [member.value for member in members],
    )
