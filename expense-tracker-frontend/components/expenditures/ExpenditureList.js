"use client";
import { useState, useEffect } from "react"; // Add useEffect import

export default function ExpenditureList({ expenditures, token, refreshData }) {
    const [formVisible, setFormVisible] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({ title: "", amount: "" });
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

  // Open form for add
  const openAddForm = () => {
    setEditingId(null);
    setFormData({ title: "", amount: "" });
    setFormVisible(true);
  };

  // Open form for edit
  const openEditForm = (exp) => {
    setEditingId(exp.id);
    setFormData({ title: exp.title, amount: exp.amount });
    setFormVisible(true);
  };

  // Submit form (add or update)
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.title || !formData.amount) return;

    const url = editingId ? `/api/expenditures/${editingId}` : "/api/expenditures";
    const method = editingId ? "PUT" : "POST";

    const res = await fetch(url, {
      method,
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(formData),
    });

    if (res.ok) {
      setFormVisible(false);
      setEditingId(null);
      setFormData({ title: "", amount: "" });
      refreshData();
    }
  };

  // Delete expenditure
  const deleteExpenditure = async (id) => {
    if (!confirm("Are you sure you want to delete this expenditure?")) return;
    const res = await fetch(`/api/expenditures/${id}`, {
      method: "DELETE",
      headers: { Authorization: `Bearer ${token}` },
    });
    if (res.ok) refreshData();
  };

    return (
        <div className={`p-8 rounded-3xl shadow-xl ${isDark ? 'bg-gray-800' : 'bg-white'} mt-8 mx-auto max-w-3xl`}>
            <h2 className={`text-3xl font-extrabold mb-8 ${isDark ? 'text-gray-200' : 'text-gray-800'} border-b pb-2`}>
                    Expenditures
            </h2>

            <button
                    onClick={openAddForm}
                    className={`${isDark ? 'bg-blue-900 hover:bg-blue-800' : 'bg-blue-600 hover:bg-blue-700'} 
                    text-white font-semibold px-4 py-2 rounded-lg transition-all duration-300 mb-6 shadow-md hover:shadow-lg transform hover:-translate-y-0.5`}
            >
                    + Add Expenditure
            </button>

            {formVisible && (
                    <form onSubmit={handleSubmit} className={`flex flex-col sm:flex-row gap-4 mb-8 ${isDark ? 'bg-gray-700' : 'bg-gray-50'} p-6 rounded-xl`}>
                            <input
                                    type="text"
                                    placeholder="Title"
                                    value={formData.title}
                                    onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                                    className={`border ${isDark ? 'bg-gray-600 border-gray-600 text-white' : 'border-gray-300'} 
                                    p-4 rounded-lg flex-1 focus:outline-none focus:ring-2 focus:ring-blue-400 transition shadow-sm`}
                            />
                            <input
                                    type="number"
                                    placeholder="Amount"
                                    value={formData.amount}
                                    onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
                                    className={`border ${isDark ? 'bg-gray-600 border-gray-600 text-white' : 'border-gray-300'} 
                                    p-4 rounded-lg w-40 focus:outline-none focus:ring-2 focus:ring-blue-400 transition shadow-sm`}
                            />
                            <button
                                    type="submit"
                                    className={`${isDark ? 'bg-blue-900 hover:bg-blue-800' : 'bg-green-600 hover:bg-green-700'} 
                                    text-white font-semibold px-6 py-4 rounded-lg transition-all duration-300 shadow-md hover:shadow-lg`}
                            >
                                    {editingId ? "Update" : "Add"}
                            </button>
                            <button
                                    type="button"
                                    onClick={() => setFormVisible(false)}
                                    className={`${isDark ? 'bg-gray-600 hover:bg-gray-500 text-gray-200' : 'bg-gray-200 hover:bg-gray-300 text-gray-700'} 
                                    font-semibold px-6 py-4 rounded-lg transition-all duration-300`}
                            >
                                    Cancel
                            </button>
                    </form>
            )}

            <ul className="space-y-2 overflow-y-auto max-w-3xl mx-auto">
                    <div className={`grid grid-cols-5 gap-4 p-3 rounded-xl ${isDark ? 'bg-gray-700 text-gray-200 border-gray-600' : 'bg-gray-100 text-gray-800 border-gray-300'} 
                            font-semibold border text-xl`}>
                            <span className="col-span-2">Title</span>
                            <span className="col-span-2">Amount (Rs.)</span>
                            <span>Actions</span>
                    </div>

                    {expenditures?.map((exp) => (
                        <li
                        key={exp.id}
                        className={`grid grid-cols-5 gap-4 items-center p-3 rounded-xl ${isDark ? 'hover:bg-gray-700 border-gray-600 bg-gray-800' : 'hover:bg-gray-50 border-gray-200'} 
                        transition-all duration-300 border shadow-sm hover:shadow-md`}
                        >
                            <span 
                                    className={`col-span-2 cursor-pointer text-lg ${isDark ? 'text-gray-200' : 'text-gray-700'} hover:underline`}
                                    onClick={() => openEditForm(exp)}
                            >
                                    {exp.title}
                            </span>
                            <span 
                                    className={`col-span-2 cursor-pointer text-lg ${isDark ? 'text-red-400' : 'text-red-700'}`}
                                    onClick={() => openEditForm(exp)}
                            >
                                    Rs. {exp.amount}
                            </span>
                            <button
                                    onClick={() => deleteExpenditure(exp.id)}
                                    className={`${isDark ? 'bg-red-900 hover:bg-red-800 text-red-200' : 'bg-red-50 hover:bg-red-100 text-red-700'} 
                                    font-medium px-3 py-1 rounded-lg transition-all duration-300 hover:shadow-md`}
                            >
                                Delete
                            </button>
                        </li>
                    ))}
            </ul>
        </div>
    );
}
