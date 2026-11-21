"use client";

import { useSearchParams } from "next/navigation";
import { LoginScreen, type LoginPreviewState } from "./LoginScreen";

const previewStates = new Set<LoginPreviewState>([
  "loading",
  "loading-discord",
  "error",
  "success",
  "cancelled",
  "account-conflict"
]);

export function LoginPreview() {
  const searchParams = useSearchParams();
  const state = searchParams.get("state") ?? searchParams.get("login");
  const initialState =
    state && previewStates.has(state as LoginPreviewState)
      ? (state as LoginPreviewState)
      : undefined;

  return <LoginScreen initialState={initialState} />;
}
