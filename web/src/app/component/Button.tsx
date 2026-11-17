import type { ButtonHTMLAttributes, ReactNode } from "react";

export type ButtonVariant = "primary" | "discord" | "secondary";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  icon?: ReactNode;
  isLoading?: boolean;
  loadingLabel?: string;
}

export function Button({
  children,
  className,
  disabled,
  icon,
  isLoading = false,
  loadingLabel = "Đang xử lý…",
  variant = "secondary",
  ...props
}: Readonly<ButtonProps>) {
  const variantClasses: Record<ButtonVariant, string> = {
    primary: "bg-app-light text-app-on-light hover:not-disabled:bg-[#e8e8ea]",
    discord:
      "bg-app-discord text-app-light hover:not-disabled:bg-app-discord-hover",
    secondary:
      "border-app-border text-app-text hover:not-disabled:border-app-text-subtle hover:not-disabled:bg-app-surface",
  };

  const buttonClassName = [
    "inline-flex min-h-13 w-full items-center justify-center gap-3 rounded-full border border-transparent px-6 text-base font-medium transition-[background-color,border-color,transform,opacity] duration-150 ease-out active:scale-[0.985] disabled:cursor-default disabled:opacity-[0.55] motion-reduce:transition-none",
    variantClasses[variant],
    className,
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <button
      {...props}
      aria-busy={isLoading || undefined}
      className={buttonClassName}
      disabled={disabled || isLoading}
      type={props.type ?? "button"}
    >
      {isLoading ? (
        <span
          aria-hidden="true"
          className="size-5 shrink-0 animate-spin rounded-full border-2 border-current border-r-transparent motion-reduce:[animation-duration:2s]"
        />
      ) : (
        <span aria-hidden="true" className="[&>svg]:size-5 [&>svg]:shrink-0">
          {icon}
        </span>
      )}
      <span>{isLoading ? loadingLabel : children}</span>
    </button>
  );
}
