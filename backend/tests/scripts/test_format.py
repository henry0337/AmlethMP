import ast
import unittest

from scripts.format import strip_trailing_commas


class StripTrailingCommasTestCase(unittest.TestCase):
    def test_removes_trailing_commas_before_closing_brackets(self) -> None:
        source = (
            "call(\n    first,\n    second,\n)\nitems = [\n    1,\n    2,\n]\nmapping = {'a': 1,}\n"
        )

        self.assertEqual(
            strip_trailing_commas(source),
            "call(\n    first,\n    second\n)\nitems = [\n    1,\n    2\n]\nmapping = {'a': 1}\n"
        )

    def test_keeps_the_comma_of_one_element_tuples(self) -> None:
        source = "pair = (1,)\nvalue = lookup[key,]\nfirst, = items\n"

        self.assertEqual(strip_trailing_commas(source), source)

    def test_removes_only_the_removable_commas_in_a_mixed_file(self) -> None:
        source = "pair = (1,)\ncall(\n    a,\n    b,\n)\n"

        self.assertEqual(strip_trailing_commas(source), "pair = (1,)\ncall(\n    a,\n    b\n)\n")

    def test_keeps_commas_inside_strings_and_comments(self) -> None:
        source = 'text = "a,)"\n# call(a,)\nvalue = f"{amount:,}"\n'

        self.assertEqual(strip_trailing_commas(source), source)

    def test_keeps_a_comment_that_follows_the_removed_comma(self) -> None:
        source = "call(\n    first,  # why\n    second,  # because\n)\n"

        self.assertEqual(
            strip_trailing_commas(source), "call(\n    first,  # why\n    second  # because\n)\n"
        )

    def test_never_changes_the_syntax_tree(self) -> None:
        source = "def f(a, b,):\n    return (a,), [b,], {a: b,}, g(*a,), h[a, b,]\n"

        self.assertEqual(
            ast.dump(ast.parse(strip_trailing_commas(source))), ast.dump(ast.parse(source))
        )
