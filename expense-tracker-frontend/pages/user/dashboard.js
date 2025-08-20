"use client";
import { useEffect, useState } from "react";
import ChangePassword from "@/components/user/ChangePassword";
import Cookies from "js-cookie";
import DeleteUser from "@/components/user/DeleteUser";

export default function Dashboard() {
  const [user, setUser] = useState(null);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [token, setToken] = useState("");
  const [isDark, setIsDark] = useState(false);

  
  useEffect(() => {
    const updateTheme = () => {
      const currentTheme = localStorage.getItem("theme") || "dark";
      setIsDark(currentTheme === "dark");
    };
    window.addEventListener("theme-changed", updateTheme);
    updateTheme();
    return () => window.removeEventListener("theme-changed", updateTheme);
  }, []);

  
  useEffect(() => {
    const token = Cookies.get("token");
    setToken(token);
  }, []);

  
  useEffect(() => {
    setMessage("");
    setError("");
    if (!token) return setError("No token found");

    const fetchUser = async () => {
      try {
        const res = await fetch(`/api/users`, {
          method: "GET",
          credentials: "include",
          headers: { Authorization: `Bearer ${token}` },
        });
        if (!res.ok) {
          const text = await res.text();
          setError(`Failed to load user details: ${res.status} - ${text}`);
          return;
        }
        const data = await res.text();
        setUser(data);
      } catch (err) {
        setError(err.message);
      }
    };
    fetchUser();
  }, [token]);

  
  const bgGradient = isDark
    ? "bg-gradient-to-br from-blue-900 via-gray-800 to-blue-900"
    : "bg-gradient-to-br from-gray-100 to-white";
  const glassCard =
    "backdrop-blur-lg bg-white/10 border border-white/20 shadow-2xl rounded-3xl";
  const cardPadding = "p-12";
  const heading = isDark ? "text-blue-200 drop-shadow-lg" : "text-blue-800 drop-shadow";
  const subHeading = isDark ? "text-gray-200" : "text-gray-700";
  const divider = "border-b border-blue-200 my-8";
  const inputBg = isDark ? "bg-gray-800/80" : "bg-white/80";
  const inputText = isDark ? "text-blue-100" : "text-blue-900";

  if (!token) {
    return (
      <div className={`${bgGradient} flex justify-center items-center min-h-screen`}>
        <div className={`${glassCard} ${cardPadding} max-w-lg w-full flex flex-col items-center`}>
          <p className="text-red-400 text-xl font-semibold text-center">
            Authentication token is missing.<br />Please log in again.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col min-h-screen">
      <main className={`${bgGradient} flex-1 flex justify-center items-start py-10 transition-colors duration-300`}>
        <div className={`${glassCard} ${cardPadding} max-w-2xl w-full`}>
          <div className="flex flex-col items-center">
            <h1 className={`text-5xl font-extrabold mb-4 tracking-tight ${heading}`}>Welcome</h1>
            <span className={`text-lg font-medium mb-6 ${subHeading}`}>User Dashboard</span>
          </div>

          {error && <div className="bg-red-100 text-red-700 px-4 py-2 rounded-lg shadow mb-6 text-center">{error}</div>}
          {message && <div className="bg-green-100 text-green-700 px-4 py-2 rounded-lg shadow mb-6 text-center">{message}</div>}

          {user && (
            <div className="flex flex-col items-center mb-10">
              <h2 className={`text-2xl font-semibold text-center ${subHeading}`}>{user}</h2>
            </div>
          )}

          <div className={divider}></div>

          {token && (
            <div className="mb-10">
              <ChangePassword token={token} setError={setError} setMessage={setMessage} inputBg={inputBg} inputText={inputText} isDark={isDark} />
            </div>
          )}

          <div className={divider}></div>

          <DeleteUser token={token} setError={setError} setMessage={setMessage} isDark={isDark} />
        </div>
      </main>


      <footer className="h-20 flex items-center justify-center bg-gray-900 text-white shadow">
        <p className="text-sm">© 2025 My App</p>
      </footer>
    </div>
  );
}
