  import { useRouter } from "next/router";
  import { useEffect, useState, useContext } from "react";
  import Cookies from "js-cookie";

  export default function Login() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const router = useRouter();
    const [csrfToken, setCsrfToken] = useState("");

    useEffect(() => {
        const csrfToken = Cookies.get("XSRF-TOKEN");
        setCsrfToken(csrfToken);
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
          router.push("../user/dashboard");
        } else {
          const errorText = await response.text();
          console.error("Login failed:", errorText);
          alert("Login Failed, please check your credentials.");
        }
      } catch (error) {
        console.error("Login error:", error);
        alert("An error occurred while logging in. Please try again.");
      }


    };

    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-100">
        <div className="bg-white p-8 rounded shadow-md w-full max-w-md">
          <h2 className="text-2xl font-bold mb-6 text-center">Login</h2>
          <form onSubmit={handleSubmit}>
            <input
              className="w-full px-4 py-2 mb-4 border rounded"
              type="text"
              placeholder="Username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
            <input
              className="w-full px-4 py-2 mb-4 border rounded"
              type="password"
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
            <button
              className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700 transition"
              type="submit"
            >
              Login
            </button>
          </form>
        </div>
      </div>
    );
  }
