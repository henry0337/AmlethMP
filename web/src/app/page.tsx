import { Suspense } from "react";
import { LoginPreview } from "./page/login/LoginPreview";
import { LoginScreen } from "./page/login/LoginScreen";

export default function Home() {
  return (
    <Suspense fallback={<LoginScreen />}>
      <LoginPreview />
    </Suspense>
  );
}
