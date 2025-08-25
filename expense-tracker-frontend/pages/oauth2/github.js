"use client";
import { useEffect, useContext } from "react";
import Cookies from "js-cookie";
import { useRouter, useSearchParams } from "next/navigation";
import { AppContext } from "@/context/AppContext";

export default function OAuthRedirect() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const {setToken} = useContext(AppContext);

  useEffect(() => {
    const token = searchParams.get("token");
    if (!token) {
      console.error("No access token found in URL.");
      return;
    }
    Cookies.set("token", token, { sameSite: "Strict" });
    setToken(token);
    console.log("Access token set in cookies:", token);
    window.close();
  }, [router, searchParams]);

  return <div>Logging in, please wait...</div>;
}
