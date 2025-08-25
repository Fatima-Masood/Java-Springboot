import "@/styles/globals.css";
import Layout from "@/layout/layout";
import React, { useState, useEffect } from "react";
import { AppContext } from "@/context/AppContext";

export default function App({ Component, pageProps }) {
  const [theme, setTheme] = useState("dark");
  const [token, setToken] = useState(null);

  useEffect(() => {
    const storedTheme = localStorage.getItem("theme");
    if (!storedTheme) {
      const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
      setTheme(prefersDark ? "dark" : "light");
    }
  }, []);

  return (
    <AppContext.Provider value={{ theme, setTheme, token, setToken }}>
      <Layout>
        <Component {...pageProps} />
      </Layout>
    </AppContext.Provider>
  );
}
