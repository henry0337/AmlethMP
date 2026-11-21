import ast

import pytest

from scripts.format import strip_trailing_commas


@pytest.mark.parametrize(
    ("source", "expected"),
    [
        pytest.param(
            "call(\n    first,\n    second,\n)\n",
            "call(\n    first,\n    second\n)\n",
            id="call-arguments"
        ),
        pytest.param("items = [\n    1,\n    2,\n]\n", "items = [\n    1,\n    2\n]\n", id="list"),
        pytest.param("mapping = {'a': 1,}\n", "mapping = {'a': 1}\n", id="inline-dict"),
        pytest.param(
            "pair = (1,)\ncall(\n    a,\n    b,\n)\n",
            "pair = (1,)\ncall(\n    a,\n    b\n)\n",
            id="mixed-with-a-one-element-tuple"
        ),
        pytest.param(
            "call(\n    first,  # why\n    second,  # because\n)\n",
            "call(\n    first,  # why\n    second  # because\n)\n",
            id="keeps-the-comment-after-the-comma"
        )
    ]
)
def test_removes_trailing_commas_before_closing_brackets(source: str, expected: str) -> None:
    assert strip_trailing_commas(source) == expected


@pytest.mark.parametrize(
    "source",
    [
        pytest.param(
            "pair = (1,)\nvalue = lookup[key,]\nfirst, = items\n", id="one-element-tuples"
        ),
        pytest.param(
            'text = "a,)"\n# call(a,)\nvalue = f"{amount:,}"\n', id="strings-and-comments"
        )
    ]
)
def test_keeps_commas_that_are_part_of_the_syntax(source: str) -> None:
    assert strip_trailing_commas(source) == source


def test_never_changes_the_syntax_tree() -> None:
    source = "def f(a, b,):\n    return (a,), [b,], {a: b,}, g(*a,), h[a, b,]\n"

    assert ast.dump(ast.parse(strip_trailing_commas(source))) == ast.dump(ast.parse(source))
