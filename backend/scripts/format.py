"""Sort imports and format the backend with Ruff, then remove trailing commas.

Ruff (like Black and isort) always adds a trailing comma when it puts one element per line and has
no option to turn that off, but this project does not use trailing commas. This script sorts
imports, runs `ruff format` and then removes every comma that directly precedes a closing bracket,
except where removing it would change the program (for example the one-element tuple `(value,)`).

Usage, from the `backend/` directory:
    uv run python scripts/format.py          # rewrite files
    uv run python scripts/format.py --check  # list files that would change; exit 1 if any
"""

import argparse
import ast
import io
import subprocess
import sys
import tokenize
from collections.abc import Sequence
from itertools import islice
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CLOSING_BRACKETS = frozenset({")", "]", "}"})
LAYOUT_TOKENS = frozenset({tokenize.NL, tokenize.COMMENT})


def find_trailing_comma_offsets(source: str) -> list[int]:
    """Return the offsets of commas whose next code token is a closing bracket."""
    line_starts = [0]
    for line in io.StringIO(source).readlines():
        line_starts.append(line_starts[-1] + len(line))

    tokens = list(tokenize.generate_tokens(io.StringIO(source).readline))
    offsets: list[int] = []
    for index, token in enumerate(tokens):
        if token.type != tokenize.OP or token.string != ",":
            continue
        following = next(
            (item for item in islice(tokens, index + 1, None) if item.type not in LAYOUT_TOKENS),
            None
        )
        if (
            following is not None
            and following.type == tokenize.OP
            and following.string in CLOSING_BRACKETS
        ):
            row, column = token.start
            offsets.append(line_starts[row - 1] + column)
    return offsets


def remove_characters(source: str, offsets: Sequence[int]) -> str:
    for offset in sorted(offsets, reverse=True):
        source = source[:offset] + source[offset + 1 :]
    return source


def has_same_syntax_tree(source: str, expected_dump: str) -> bool:
    try:
        return ast.dump(ast.parse(source)) == expected_dump
    except SyntaxError:
        return False


def strip_trailing_commas(source: str) -> str:
    """Remove trailing commas that are not needed to keep the program's meaning."""
    offsets = find_trailing_comma_offsets(source)
    if not offsets:
        return source

    expected_dump = ast.dump(ast.parse(source))
    stripped = remove_characters(source, offsets)
    if has_same_syntax_tree(stripped, expected_dump):
        return stripped

    removable = [
        offset
        for offset in offsets
        if has_same_syntax_tree(remove_characters(source, [offset]), expected_dump)
    ]
    stripped = remove_characters(source, removable)
    if not has_same_syntax_tree(stripped, expected_dump):
        raise ValueError("Removing trailing commas changed the syntax tree.")
    return stripped


def run_ruff(*arguments: str, stdin: bytes | None = None) -> bytes:
    result = subprocess.run(  # noqa: S603 - fixed arguments, no shell
        [sys.executable, "-m", "ruff", *arguments],
        input=stdin,
        capture_output=True,
        cwd=ROOT,
        check=False
    )
    if result.returncode != 0:
        message = result.stderr.decode("utf-8", errors="replace").strip()
        raise RuntimeError(f"ruff {' '.join(arguments)} failed:\n{message}")
    return result.stdout


def project_files() -> list[Path]:
    """List the Python files Ruff would check, so the configured excludes also apply here."""
    output = run_ruff("check", "--show-files").decode("utf-8")
    paths = (Path(line.strip()) for line in output.splitlines() if line.strip())
    return sorted(path for path in paths if path.suffix == ".py")


def format_source(path: Path, source: str) -> str:
    name = str(path)
    sorted_imports = run_ruff(
        "check",
        "--select",
        "I",
        "--fix",
        "--exit-zero",
        "--stdin-filename",
        name,
        "-",
        stdin=source.encode("utf-8")
    )
    formatted = run_ruff("format", "--stdin-filename", name, "-", stdin=sorted_imports)
    return strip_trailing_commas(formatted.decode("utf-8"))


def main(arguments: Sequence[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Format Python files without trailing commas.")
    parser.add_argument(
        "--check",
        action="store_true",
        help="do not write files; exit with status 1 if any file would change"
    )
    options = parser.parse_args(arguments)

    changed: list[Path] = []
    files = project_files()
    for path in files:
        original = path.read_bytes().decode("utf-8")
        formatted = format_source(path, original)
        if formatted == original:
            continue
        changed.append(path)
        if not options.check:
            path.write_bytes(formatted.encode("utf-8"))

    verb = "Would reformat" if options.check else "Reformatted"
    for path in changed:
        print(f"{verb} {path.relative_to(ROOT)}")
    print(
        f"{len(changed)} file(s) {'would change' if options.check else 'changed'}, "
        f"{len(files) - len(changed)} unchanged"
    )
    return 1 if options.check and changed else 0


if __name__ == "__main__":
    sys.exit(main())
