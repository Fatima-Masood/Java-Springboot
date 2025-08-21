"use client";
import { useEffect } from "react";
import Cookies from "js-cookie";
import { useRouter } from "next/navigation";

export default function OAuthRedirect() {
  const router = useRouter();

  useEffect(() => {
    const token = Cookies.get("access_token");
    if (!token) {
      console.error("No access token found in cookies.");
      return;
    }

    Cookies.set("token", token, { sameSite: "Strict" });
    console.log("Access token set in cookies:", token);
    window.close();
  }, [router]);

  return <div>Logging in, please wait...</div>;
}
