"use client";
import { useEffect, useState } from "react";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import ExpenditureList from "@/components/expenditures/ExpenditureList";
import Cookies from "js-cookie";

export default function MonthlyExpenditure() {
  const [summary, setSummary] = useState(null);
  const [expenditures, setExpenditures] = useState([]);
  const [year, setYear] = useState(new Date().getFullYear());
  const [month, setMonth] = useState(new Date().getMonth() + 1);
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

    const currentDate = new Date();
    setYear((prev) => prev ?? currentDate.getFullYear());
    setMonth((prev) => prev ?? currentDate.getMonth() + 1);
  }, []);

  // Fetch summary
  const fetchSummary = async () => {
    if (!token) return;
    try {
      const res = await fetch(
        `../api/expenditures/monthly/monthly-summary?year=${year}&month=${month}`,
        {
          headers: { Authorization: `Bearer ${token}` },
          credentials: "include",
        }
      );
      if (res.ok) {
        const data = await res.json();
        setSummary(data);
        setExpenditures(data.expenses);
      } else {
        console.error("Failed to fetch summary:", res.statusText);
      }
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    if (token) fetchSummary();
  }, [year, month, token]);

  return (
    <div className={`${isDark ? 'bg-gray-700 text-white' : 'bg-white text-gray-800'} p-6 min-h-screen`}>
      <h1 className={`mt-8 text-4xl font-extrabold mb-6 text-center tracking-tight ${
        isDark ? 'text-blue-400' : 'text-transparent bg-clip-text bg-gradient-to-r from-gray-900 via-gray-400 to-gray-800'
      }`}>
        Monthly Summary for {String(month).padStart(2, "0")} / {year}
      </h1>

      <div className="flex flex-col max-w-5xl mx-auto">
        <DatePicker 
          selected={new Date(year, month - 1)}
          onChange={(date) => {
            setYear(date.getFullYear());
            setMonth(date.getMonth() + 1);
          }}
          dateFormat="MM/yyyy"
          showMonthYearPicker
          className={`block mx-auto border p-3 rounded-lg shadow-md hover:shadow-lg transition-shadow duration-200 focus:ring-2 focus:ring-blue-500 focus:outline-none text-center text-lg w-48 ${
            isDark ? 'bg-gray-800 border-gray-700 text-white' : 'bg-white border-gray-300 text-gray-900'
          }`}
          calendarClassName={`rounded-lg shadow-xl ${isDark ? 'bg-gray-800' : 'bg-white'}`}
        />
      </div>

      {/* Summary Info */}
      {summary && (
        <div className={`grid grid-cols-3 gap-6 mb-6 max-w-3xl p-8 rounded-3xl shadow-xl mt-8 mx-auto ${
          isDark ? 'bg-gray-800' : 'bg-white'
        }`}>
          <div className={`p-4 rounded-xl ${isDark ? 'bg-green-900' : 'bg-green-100'}`}>
            <p className="font-semibold">Limit</p>
            <p className="text-lg">Rs. {summary.limitAmount}</p>
          </div>
          <div className={`p-4 rounded-xl ${isDark ? 'bg-red-900' : 'bg-red-100'}`}>
            <p className="font-semibold">Spent</p>
            <p className="text-lg">Rs. {summary.totalSpent}</p>
          </div>
          <div className={`p-4 rounded-xl ${isDark ? 'bg-blue-900' : 'bg-blue-100'}`}>
            <p className="font-semibold">Remaining</p>
            <p className="text-lg">Rs. {summary.limitAmount - summary.totalSpent}</p>
          </div>
        </div>
      )}

      <ExpenditureList
        expenditures={expenditures}
        token={token}
        refreshData={fetchSummary}
      />

    </div>
  );
}
