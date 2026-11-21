const fallbackApiOrigin = "http://localhost:8000";

export function getApiOrigin(): string {
  return (process.env.NEXT_PUBLIC_API_ORIGIN ?? fallbackApiOrigin).replace(
    /\/$/,
    ""
  );
}

export function getGoogleLoginUrl(): string {
  return `${getApiOrigin()}/auth/google/login`;
}
