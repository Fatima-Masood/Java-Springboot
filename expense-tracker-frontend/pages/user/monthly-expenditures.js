"use client";
import { useEffect, useState, useContext } from "react";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import ExpenditureList from "@/components/expenditures/ExpenditureList";
import Cookies from "js-cookie";
import { AppContext } from "@/context/AppContext";

export default function MonthlyExpenditure() {
  const [summary, setSummary] = useState(null);
  const [expenditures, setExpenditures] = useState([]);
  const [year, setYear] = useState(new Date().getFullYear());
  const [month, setMonth] = useState(new Date().getMonth() + 1);
  const {token, setToken} = useContext(AppContext);
  const [isDark, setIsDark] = useState(false);

  const [showLimitForm, setShowLimitForm] = useState(false);
  const [newLimit, setNewLimit] = useState("");

  useEffect(() => {
    const updateTheme = () => {
      const currentTheme = localStorage.getItem("theme");
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
    console.log("Token:", temp);

    const currentDate = new Date();
    setYear((prev) => prev ?? currentDate.getFullYear());
    setMonth((prev) => prev ?? currentDate.getMonth() + 1);
  }, []);

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

  const handleSetLimit = async (e) => {
    e.preventDefault();
    try {
      const res = await fetch(`/api/expenditures/monthly/set-limit?year=${year}&month=${month}&limit=${newLimit}`, {
        method: "POST",
        headers: { Authorization: `Bearer ${token}` },
        credentials: "include",
      });

      if (res.ok) {
        const msg = await res.text();
        console.log("Limit set:", msg);
        setShowLimitForm(false);
        fetchSummary();
      } else {
        console.error("Failed to set limit:", res.statusText);
      }
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div
      className={`${
        isDark ? "bg-gray-700 text-white" : "bg-gradient-to-br from-gray-100 to-white text-gray-800"
      } p-6 min-h-screen`}
    >
      <h1
        className={`mt-8 text-4xl font-extrabold mb-6 text-center tracking-tight ${
          isDark
            ? "text-blue-400"
            : "text-transparent bg-clip-text bg-gradient-to-r from-gray-900 via-gray-400 to-gray-800"
        }`}
      >
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
            isDark
              ? "bg-gray-800 border-gray-700 text-white"
              : "bg-white border-gray-300 text-gray-900"
          }`}
          calendarClassName={`rounded-lg shadow-xl ${
            isDark ? "bg-gray-800" : "bg-gray-100"
          }`}
        />
      </div>

      {summary && (
        <div
          className={`grid grid-cols-3 gap-6 mb-6 max-w-4xl p-8 rounded-3xl shadow-xl mt-8 mx-auto ${
            isDark ? "bg-gray-800" : "bg-white"
          }`}
        >
          <div
            onClick={() => setShowLimitForm(true)}
            className={`p-3 rounded-xl cursor-pointer transition-transform hover:scale-105 ${
              isDark ? "bg-green-900" : "bg-green-100"
            }`}
          >
            <p className="font-semibold">Limit</p>
            <p className="text-lg">Rs. {summary.limitAmount}</p>
            <p className="text-sm italic text-blue-300">
              (Click to set new limit)
            </p>
          </div>

          <div
            className={`p-3 rounded-xl ${
              isDark ? "bg-red-900" : "bg-red-100"
            }`}
          >
            <p className="font-semibold">Spent</p>
            <p className="text-lg">Rs. {summary.totalSpent}</p>
          </div>
          <div
            className={`p-3 rounded-xl ${
              isDark ? "bg-blue-900" : "bg-blue-100"
            }`}
          >
            <p className="font-semibold">Remaining</p>
            <p className="text-lg">
              Rs. {summary.limitAmount - summary.totalSpent}
            </p>
          </div>
        </div>
      )}

      {/* Limit Form Modal */}
      {showLimitForm && (
        <div className="fixed inset-0 flex items-center justify-center bg-white/60 z-50">
          <form
            onSubmit={handleSetLimit}
            className={`p-6 rounded-2xl shadow-xl ${
              isDark ? "bg-gray-900 text-white" : "bg-white text-gray-800"
            }`}
          >
            <h2 className="text-xl font-bold mb-4">
              Set Limit for {String(month).padStart(2, "0")}/{year}
            </h2>
            <input
              type="number"
              value={newLimit}
              onChange={(e) => setNewLimit(e.target.value)}
              placeholder="Enter limit"
              required
              className={`w-full p-3 mb-4 border rounded-lg focus:ring-2 focus:ring-blue-500 ${
                isDark
                  ? "bg-gray-800 border-gray-600"
                  : "bg-white border-gray-300"
              }`}
            />
            <div className="flex justify-end gap-3">
              <button
                type="button"
                onClick={() => setShowLimitForm(false)}
                className="px-4 py-2 rounded-lg bg-gray-500 text-white hover:bg-gray-600"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-4 py-2 rounded-lg bg-blue-600 text-white hover:bg-blue-700"
              >
                Save
              </button>
            </div>
          </form>
        </div>
      )}

      <ExpenditureList
        expenditures={expenditures}
        token={token}
        refreshData={fetchSummary}
        month={month}
        year={year}
      />
    </div>
  );
}
