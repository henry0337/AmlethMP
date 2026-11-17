import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  applicationName: "AmlethMP",
  title: "Đăng nhập — AmlethMP",
  description: "Đăng nhập vào AmlethMP để có trải nghiệm âm nhạc trọn vẹn.",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="vi">
      <body>{children}</body>
    </html>
  );
}
