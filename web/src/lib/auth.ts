const fallbackApiOrigin = "http://localhost:8000";

export function getApiOrigin(): string {
  return (process.env.NEXT_PUBLIC_API_ORIGIN ?? fallbackApiOrigin).replace(
    /\/$/,
    ""
  );
}

export type AuthProvider = "google" | "discord";

export const authProviders: readonly AuthProvider[] = ["google", "discord"];

export function getLoginUrl(provider: AuthProvider): string {
  return `${getApiOrigin()}/auth/${provider}/login`;
}
