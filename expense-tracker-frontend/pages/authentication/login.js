import { useRouter } from "next/router";
import { useEffect, useState, useContext } from "react";
import Cookies from "js-cookie";
import { AppContext } from "@/context/AppContext";

export default function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const router = useRouter();
  const [csrfToken, setCsrfToken] = useState("");
  const [isDark, setIsDark] = useState(false);
  const {setToken} = useContext(AppContext);

  useEffect(() => {
    const updateTheme = () => {
      const currentTheme = localStorage.getItem("theme") || "dark";
      setIsDark(currentTheme === "dark");
    };

    window.addEventListener("theme-changed", updateTheme);
    updateTheme();

    return () => {
      window.removeEventListener("theme-changed", updateTheme);
    };
  }, []);

  useEffect(() => {
    const csrf = Cookies.get("XSRF-TOKEN");
    setCsrfToken(csrf);
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const params = new URLSearchParams();
      params.append("username", username);
      params.append("password", password);

      const response = await fetch("/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
          "X-XSRF-TOKEN": csrfToken,
        },
        body: params.toString(),
        credentials: "include",
      });

      if (response.ok) {
        const data = await response.json();
        Cookies.set("token", data.access_token, { sameSite: "Strict" });
        setToken(data.access_token); 
        router.push("../user/dashboard");
      } else {
        const errorText = await response.text();
        console.error("Login failed:", errorText);
        alert("Login Failed, please check your credentials.");
      }
    } catch (error) {
      console.error("Login error:", error);
      alert("An error occurred while logging in. Please reload the page and try again.");
    }
  };

  const startOAuthPopup = () => {
    const popup = window.open("../oauth2/authorization/github", "OAuth2 Login", "width=600,height=700");
    if (popup) {
      const timer = setInterval(() => {
        if (popup.closed) {
          clearInterval(timer);
          router.push("../user/dashboard");
        }
      }, 500);
    }
  };

  const theme = isDark
    ? {
        bg: "bg-gradient-to-br from-[#3a4253] to-[#232b3e]",
        card: "bg-[#434b5e] text-gray-200",
        input: "bg-[#1a2233] border-gray-700 text-gray-100 placeholder-gray-400 focus:ring-blue-700",
        label: "text-gray-300",
        button: "bg-blue-900 hover:bg-blue-800 text-gray-100",
        divider: "border-gray-700",
        oauth: "bg-gray-800 hover:bg-gray-700 text-gray-100",
        or: "text-gray-400",
      }
    : {
        bg: "bg-lightGray-100",
        card: "bg-white text-gray-900",
        input: "bg-white border-gray-300 text-gray-900 placeholder-gray-400 focus:ring-blue-400",
        label: "text-gray-700",
        button: "bg-blue-700 hover:bg-blue-800 text-white",
        divider: "border-gray-300",
        oauth: "bg-gray-900 hover:bg-gray-800 text-white",
        or: "text-gray-400",
      };

  return (
    <div className={`min-h-screen flex items-center justify-center ${theme.bg} transition-colors pb-12`}>
      <div className={`p-10 rounded-2xl shadow-2xl w-full max-w-md ${theme.card} transition-colors`}>
        <h2 className="text-3xl font-extrabold mb-8 text-center" style={isDark ? { color: "#7da0d6" } : { color: "#3b5998" }}>
          Login to Expense Tracker
        </h2>
        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className={`block mb-1 font-medium ${theme.label}`} htmlFor="username">
              Username
            </label>
            <input
              id="username"
              className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 transition ${theme.input}`}
              type="text"
              placeholder="Enter your username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              autoComplete="username"
            />
          </div>
          <div>
            <label className={`block mb-1 font-medium ${theme.label}`} htmlFor="password">
              Password
            </label>
            <input
              id="password"
              className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 transition ${theme.input}`}
              type="password"
              placeholder="Enter your password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              autoComplete="current-password"
            />
          </div>
          <button
            className={`w-full py-2 rounded-lg font-semibold transition ${theme.button}`}
            type="submit"
          >
            Login
          </button>
          <div className="flex items-center my-4">
            <div className={`flex-grow border-t ${theme.divider}`}></div>
            <span className={`mx-3 text-sm ${theme.or}`}>or</span>
            <div className={`flex-grow border-t ${theme.divider}`}></div>
          </div>
          <button
            className={`w-full flex items-center justify-center gap-2 py-2 rounded-lg font-semibold transition ${theme.oauth}`}
            onClick={startOAuthPopup}
            type="button"
          >
            Login with GitHub
          </button>
        </form>
      </div>
    </div>
  );
}