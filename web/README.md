This is a [Next.js](https://nextjs.org) project bootstrapped with [`create-next-app`](https://nextjs.org/docs/app/api-reference/cli/create-next-app).

## Getting Started

First, run the development server:

```bash
pnpm install
pnpm dev
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.

You can start editing the page by modifying `src/app/page.tsx`. The page auto-updates as you edit the file.

## Code quality and formatting

Run these commands from `web/` using the pnpm version in `package.json`:

| Command             | Purpose                                                                   |
| ------------------- | ------------------------------------------------------------------------- |
| `pnpm lint`         | Check Next.js, React and TypeScript lint rules; warnings fail the check.  |
| `pnpm lint:fix`     | Apply automatic ESLint fixes; review remaining diagnostics.               |
| `pnpm format`       | Format supported project files with Prettier.                             |
| `pnpm format:check` | Check formatting without changing files.                                  |
| `pnpm typecheck`    | Generate Next.js route types, then run TypeScript without emitting files. |
| `pnpm check`        | Run formatting, lint and type checks in sequence.                         |

Before opening a pull request, run `pnpm check` and `pnpm build`.

Prettier is the project's formatter. `.prettierrc.json` defines two-space
indentation, double quotes, semicolons, trailing commas, LF line endings and an
80-column target. The Tailwind plugin sorts utility classes using the Tailwind 4
stylesheet at `src/app/globals.css`. ESLint handles code-quality rules;
`eslint-config-prettier` disables conflicting formatting rules.

`.prettierignore` excludes generated files, the lockfile, static assets and
environment-managed directories. Prettier also respects `.gitignore`.

For VS Code, open `web/` as a folder or as a folder in a multi-root workspace,
then install the recommended ESLint, Prettier and Tailwind CSS extensions.
The folder settings enable Prettier on save and ESLint fixes on explicit saves.
Opening only the parent repository does not apply `web/.vscode/settings.json`.
Other editors can use `.editorconfig` and the local Prettier configuration.

This project uses [`next/font`](https://nextjs.org/docs/app/building-your-application/optimizing/fonts) to automatically optimize and load [Geist](https://vercel.com/font), a new font family for Vercel.

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.

You can check out [the Next.js GitHub repository](https://github.com/vercel/next.js) - your feedback and contributions are welcome!

## Deploy on Vercel

The easiest way to deploy your Next.js app is to use the [Vercel Platform](https://vercel.com/new?utm_medium=default-template&filter=next.js&utm_source=create-next-app&utm_campaign=create-next-app-readme) from the creators of Next.js.

Check out our [Next.js deployment documentation](https://nextjs.org/docs/app/building-your-application/deploying) for more details.
