"use client";
import { useEffect, useState } from "react";
import ExpenditureList from "@/components/expenditures/ExpenditureList";
import Cookies from "js-cookie";

export default function AllExpenditures() {
  const [expenditures, setExpenditures] = useState([]);
  const [token, setToken] = useState("");
  const [isDark, setIsDark] = useState(false);

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
    const temp = Cookies.get("token");
    setToken(temp);
  }, []);


  const fetchExpenses = async () => {
    if (!token) return;
    try {
      const res = await fetch( `../api/expenditures`,
        {
          headers: { Authorization: `Bearer ${token}` },
          credentials: "include",
        }
      );
      if (res.ok) {
        const data = await res.json();
        setExpenditures(data);
      } else {
        console.error("Failed to fetch expenses:", res.statusText);
      }
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    if (token) fetchExpenses();
  }, [token]);

  return (
    <div className={`${isDark ? 'bg-gray-700 text-white' : 'bg-white text-gray-800'} p-6 min-h-screen`}>
      <h1 className={`mt-8 text-4xl font-extrabold mb-6 text-center tracking-tight ${
        isDark ? 'text-blue-400' : 'text-transparent bg-clip-text bg-gradient-to-r from-gray-900 via-gray-400 to-gray-800'
      }`}>
        All Expenditures
      </h1>

      <ExpenditureList
        expenditures={expenditures}
        token={token}
        refreshData={fetchExpenses}
      />

    </div>
  );
}
