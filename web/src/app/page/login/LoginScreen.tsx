"use client";

import Image from "next/image";
import { useState } from "react";
import { Button } from "@/app/component/Button";
import { Toast, ToastRegion } from "@/app/component/Toast";
import { type AuthProvider, getLoginUrl } from "@/lib/auth";

export type LoginPreviewState =
  | "loading"
  | "loading-discord"
  | "error"
  | "success"
  | "cancelled"
  | "account-conflict";
type LoginNotice = Exclude<LoginPreviewState, "loading" | "loading-discord">;

const providerNames: Record<AuthProvider, string> = {
  discord: "Discord",
  google: "Google"
};

interface LoginScreenProps {
  readonly initialState?: LoginPreviewState;
  readonly provider?: AuthProvider;
}

export function LoginScreen({
  initialState,
  provider = "google"
}: LoginScreenProps) {
  const [notice, setNotice] = useState<LoginNotice | null>(
    initialState === "loading" || initialState === "loading-discord"
      ? null
      : (initialState ?? null)
  );
  const [pendingProvider, setPendingProvider] = useState<AuthProvider | null>(
    initialState === "loading"
      ? "google"
      : initialState === "loading-discord"
        ? "discord"
        : null
  );
  const isGooglePending = pendingProvider === "google";
  const isDiscordPending = pendingProvider === "discord";
  const noticeContent: Record<
    LoginNotice,
    { message: string; variant: "error" | "info" | "success" | "warning" }
  > = {
    "account-conflict": {
      message:
        "Email này đã thuộc về một tài khoản khác. Vui lòng liên hệ hỗ trợ.",
      variant: "warning" as const
    },
    cancelled: {
      message: `Bạn đã hủy đăng nhập bằng ${providerNames[provider]}.`,
      variant: "info" as const
    },
    error: {
      message: "Đã có lỗi xảy ra khi đăng nhập. Vui lòng thử lại.",
      variant: "error" as const
    },
    success: {
      message: `Đăng nhập bằng ${providerNames[provider]} thành công.`,
      variant: "success" as const
    }
  };

  function startLogin(nextProvider: AuthProvider) {
    setPendingProvider(nextProvider);
    window.location.assign(getLoginUrl(nextProvider));
  }

  return (
    <main className="relative isolate grid min-h-dvh min-h-screen grid-cols-1 desktop:grid-cols-[minmax(0,1.15fr)_minmax(0,1fr)]">
      <div
        aria-hidden="true"
        className="absolute inset-0 -z-10 overflow-hidden bg-app-bg desktop:right-auto desktop:w-[calc(100%*1.15/2.15)] desktop:mask-[linear-gradient(to_right,#000_55%,transparent_100%)]"
      >
        <Image
          alt=""
          className="object-cover object-[45%_center] grayscale desktop:object-[62%_center]"
          fill
          priority
          sizes="(min-width: 1280px) 54vw, 100vw"
          src="/assets/login-bg.jpg"
        />
        <div className="absolute inset-0 bg-[linear-gradient(to_bottom,color-mix(in_srgb,var(--color-bg)_60%,transparent),color-mix(in_srgb,var(--color-bg)_88%,transparent)_55%,color-mix(in_srgb,var(--color-bg)_96%,transparent))] desktop:bg-[color-mix(in_srgb,var(--color-bg)_25%,transparent)]" />
      </div>

      <section className="flex justify-center px-6 pt-8 pb-6 tablet:items-center tablet:p-12 desktop:col-start-2 desktop:row-start-1">
        <div className="flex w-full max-w-100 flex-col justify-between gap-8 text-center tablet:max-w-115 tablet:rounded-3xl tablet:border tablet:border-app-border tablet:bg-app-surface tablet:px-8 tablet:py-12 desktop:max-w-100 desktop:rounded-none desktop:border-0 desktop:bg-transparent desktop:p-0 desktop:text-left">
          <div className="my-auto flex flex-col gap-3 tablet:my-0">
            <div className="mb-3 self-center desktop:self-start">
              <Image
                alt="AmlethMP"
                className="block size-16 tablet:size-18"
                height={72}
                priority
                src="/assets/logo-mark.svg"
                width={72}
              />
            </div>
            <h1 className="m-0 text-[2rem] leading-[1.15] font-bold tracking-[-0.02em] text-balance tablet:text-5xl">
              Âm nhạc luôn bên bạn
            </h1>
            <p className="m-0 text-app-text-muted">
              Đăng nhập để có trải nghiệm trọn vẹn nhất
            </p>
          </div>

          <div className="flex flex-col gap-3">
            <Button
              disabled={isDiscordPending}
              icon={<GoogleIcon />}
              isLoading={isGooglePending}
              loadingLabel="Đang đăng nhập…"
              onClick={() => startLogin("google")}
              variant="primary"
            >
              Đăng nhập bằng Google
            </Button>
            <Button
              disabled={isGooglePending}
              icon={<DiscordIcon />}
              isLoading={isDiscordPending}
              loadingLabel="Đang đăng nhập…"
              onClick={() => startLogin("discord")}
              variant="discord"
            >
              Đăng nhập bằng Discord
            </Button>
            <div
              aria-hidden="true"
              className="flex items-center gap-4 text-sm text-app-text-subtle before:h-px before:flex-1 before:bg-app-border after:h-px after:flex-1 after:bg-app-border"
            >
              hoặc
            </div>
            <Button
              disabled={pendingProvider !== null}
              icon={<GuestIcon />}
              variant="secondary"
            >
              Tiếp tục với tư cách khách
            </Button>
            <p className="m-0 text-sm text-app-text-subtle">
              Ứng dụng sẽ không lưu bất cứ thông tin gì về bạn cho đến khi đăng
              nhập
            </p>
          </div>

          <p className="m-0 text-xs text-app-text-subtle">
            Bằng việc tiếp tục, bạn đồng ý với{" "}
            <a
              className="text-app-text-muted underline underline-offset-2"
              href="#"
            >
              Điều khoản dịch vụ
            </a>{" "}
            và{" "}
            <a
              className="text-app-text-muted underline underline-offset-2"
              href="#"
            >
              Chính sách quyền riêng tư
            </a>{" "}
            của chúng tôi.
          </p>
        </div>
      </section>

      {notice ? (
        <ToastRegion>
          <Toast
            message={noticeContent[notice].message}
            onDismiss={() => setNotice(null)}
            variant={noticeContent[notice].variant}
          />
        </ToastRegion>
      ) : null}
    </main>
  );
}

function GoogleIcon() {
  return (
    <svg aria-hidden="true" viewBox="0 0 48 48">
      <path
        fill="#EA4335"
        d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"
      />
      <path
        fill="#4285F4"
        d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"
      />
      <path
        fill="#FBBC05"
        d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"
      />
      <path
        fill="#34A853"
        d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"
      />
    </svg>
  );
}

function DiscordIcon() {
  return (
    <svg aria-hidden="true" fill="currentColor" viewBox="0 0 24 24">
      <path d="M20.317 4.3698a19.7913 19.7913 0 0 0-4.8851-1.5152.0741.0741 0 0 0-.0785.0371c-.211.3753-.4447.8648-.6083 1.2495-1.8447-.2762-3.68-.2762-5.4868 0-.1636-.3933-.4058-.8742-.6177-1.2495a.077.077 0 0 0-.0785-.037 19.7363 19.7363 0 0 0-4.8852 1.515.0699.0699 0 0 0-.0321.0277C.5334 9.0458-.319 13.5799.0992 18.0578a.0824.0824 0 0 0 .0312.0561c2.0528 1.5076 4.0413 2.4228 5.9929 3.0294a.0777.0777 0 0 0 .0842-.0276c.4616-.6304.8731-1.2952 1.226-1.9942a.076.076 0 0 0-.0416-.1057c-.6528-.2476-1.2743-.5495-1.8722-.8923a.077.077 0 0 1-.0076-.1277c.1258-.0943.2517-.1923.3718-.2914a.0743.0743 0 0 1 .0776-.0105c3.9278 1.7933 8.18 1.7933 12.0614 0a.0739.0739 0 0 1 .0785.0095c.1202.099.246.1981.3728.2924a.077.077 0 0 1-.0066.1276 12.2986 12.2986 0 0 1-1.873.8914.0766.0766 0 0 0-.0407.1067c.3604.698.7719 1.3628 1.225 1.9932a.076.076 0 0 0 .0842.0286c1.961-.6067 3.9495-1.5219 6.0023-3.0294a.077.077 0 0 0 .0313-.0552c.5004-5.177-.8382-9.6739-3.5485-13.6604a.061.061 0 0 0-.0312-.0286ZM8.02 15.3312c-1.1825 0-2.1569-1.0857-2.1569-2.419 0-1.3332.9555-2.4189 2.157-2.4189 1.2108 0 2.1757 1.0952 2.1568 2.419 0 1.3332-.9555 2.4189-2.1569 2.4189Zm7.9748 0c-1.1825 0-2.1569-1.0857-2.1569-2.419 0-1.3332.9554-2.4189 2.1569-2.4189 1.2108 0 2.1757 1.0952 2.1568 2.419 0 1.3332-.946 2.4189-2.1568 2.4189Z" />
    </svg>
  );
}

function GuestIcon() {
  return (
    <svg aria-hidden="true" fill="currentColor" viewBox="0 0 24 24">
      <path d="M12 6c1.1 0 2 .9 2 2s-.9 2-2 2-2-.9-2-2 .9-2 2-2m0 10c2.7 0 5.8 1.29 6 2H6c.23-.72 3.31-2 6-2m0-12C9.79 4 8 5.79 8 8s1.79 4 4 4 4-1.79 4-4-1.79-4-4-4zm0 10c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
    </svg>
  );
}
