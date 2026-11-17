"use client";

import {
  useCallback,
  useEffect,
  useRef,
  useState,
  type CSSProperties,
  type ReactNode,
} from "react";

export type ToastVariant = "error" | "warning" | "success" | "info";

interface ToastProps {
  message: string;
  variant?: ToastVariant;
  duration?: number;
  onDismiss: () => void;
}

export function ToastRegion({ children }: { children: ReactNode }) {
  return (
    <div className="pointer-events-none fixed inset-x-0 top-12 z-100 flex justify-center px-6">
      {children}
    </div>
  );
}

export function Toast({
  message,
  variant = "error",
  duration = 5000,
  onDismiss,
}: ToastProps) {
  const [isLeaving, setIsLeaving] = useState(false);
  const remaining = useRef(duration);
  const startedAt = useRef(0);
  const timeoutId = useRef<ReturnType<typeof setTimeout> | null>(null);

  const clearTimer = useCallback(() => {
    if (timeoutId.current) {
      clearTimeout(timeoutId.current);
      timeoutId.current = null;
    }
  }, []);

  const dismiss = useCallback(() => {
    if (isLeaving) return;

    clearTimer();
    setIsLeaving(true);
    window.setTimeout(onDismiss, 180);
  }, [clearTimer, isLeaving, onDismiss]);

  const startTimer = useCallback(() => {
    if (!remaining.current || isLeaving) return;

    startedAt.current = Date.now();
    timeoutId.current = setTimeout(dismiss, remaining.current);
  }, [dismiss, isLeaving]);

  const pauseTimer = useCallback(() => {
    if (!timeoutId.current) return;

    clearTimer();
    remaining.current -= Date.now() - startedAt.current;
  }, [clearTimer]);

  useEffect(() => {
    startTimer();
    return clearTimer;
  }, [clearTimer, startTimer]);

  const toastColors: Record<ToastVariant, string> = {
    error: "var(--color-danger)",
    warning: "var(--color-warning)",
    success: "var(--color-success)",
    info: "var(--color-info)",
  };

  const toastStyle = {
    "--toast-color": toastColors[variant],
  } as CSSProperties;

  return (
    <div
      className={`pointer-events-auto flex w-full max-w-105 items-center gap-3 rounded-2xl border border-[color-mix(in_srgb,var(--toast-color)_38%,var(--color-border))] bg-[color-mix(in_srgb,var(--color-surface-raised)_92%,transparent)] py-3 pr-2 pl-4 text-sm text-app-text shadow-[0_12px_32px_rgb(0_0_0/50%),0_0_28px_color-mix(in_srgb,var(--toast-color)_16%,transparent)] backdrop-blur-[14px] ${isLeaving ? "animate-[toast-out_180ms_ease-in_both]" : "animate-[toast-in_260ms_cubic-bezier(0.2,0.8,0.2,1)_both]"}`}
      onPointerDown={pauseTimer}
      onPointerEnter={pauseTimer}
      onPointerLeave={startTimer}
      onPointerUp={startTimer}
      role={variant === "error" ? "alert" : "status"}
      style={toastStyle}
    >
      <ToastIcon variant={variant} />
      <p className="min-w-0 flex-1 leading-[1.4]">{message}</p>
      <button
        aria-label="Đóng thông báo"
        className="grid size-8 shrink-0 place-items-center rounded-full text-app-text-muted transition-colors duration-150 hover:bg-app-surface hover:text-app-text"
        onClick={dismiss}
        type="button"
      >
        <svg aria-hidden="true" className="size-4.5" viewBox="0 0 24 24">
          <path d="m19 6.41-1.41-1.41L12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z" />
        </svg>
      </button>
    </div>
  );
}

function ToastIcon({ variant }: { variant: ToastVariant }) {
  const paths: Record<ToastVariant, string> = {
    error:
      "M11 15h2v2h-2zm0-8h2v6h-2zm.99-5C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8z",
    warning:
      "M12 5.99 19.53 19H4.47L12 5.99M12 2 1 21h22L12 2zm1 14h-2v2h2v-2zm0-6h-2v4h2v-4z",
    success:
      "m16.59 7.58L10 14.17l-3.59-3.58L5 12l5 5 8-8zM12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8z",
    info: "M11 7h2v2h-2zm0 4h2v6h-2zm1-9C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z",
  };

  return (
    <svg
      aria-hidden="true"
      className="size-5.5 shrink-0 text-(--toast-color)"
      fill="currentColor"
      viewBox="0 0 24 24"
    >
      <path d={paths[variant]} />
    </svg>
  );
}
