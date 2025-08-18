import "@/styles/globals.css";
import Layout from "@/layout/layout";
import React, { createContext, useState, useEffect } from "react";

export const AppContext = createContext(null);

export default function App({ Component, pageProps }) {
  const [theme, setTheme] = useState("dark");

  useEffect(() => {
    const storedTheme = localStorage.getItem("theme");
    if (!storedTheme) {
      const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
      setTheme(prefersDark ? "dark" : "light");
    }
  }, []);

  return (
    <AppContext.Provider value={{ theme, setTheme }}>
      <Layout>
        <Component {...pageProps} />
      </Layout>
    </AppContext.Provider>
  );
}
