"use client";
import { useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Cookies from "js-cookie";

export default function AuthCallback() {
  const router = useRouter();
  const searchParams = useSearchParams();

  useEffect(() => {
    const token = searchParams.get("token");
    if (token) {
      Cookies.set("token", token, { sameSite: "Strict" });
      router.push("../user/dashboard");
    }
  }, [searchParams, router]);

  return <p>Logging in with GitHub...</p>;
}
