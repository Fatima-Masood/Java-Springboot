import React from 'react';
import { useEffect, useState } from "react";

const Footer = () => {
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


    const footerClass = isDark
        ? "bg-gray-900 text-white text-center py-4 w-full fixed bottom-0 left-0 z-50":
        "bg-gray-100 text-gray-900 text-center py-4 w-full fixed bottom-0 left-0 z-50";

    return (
        <footer className={footerClass}>
            <p className="m-0 text-base trackings-wide text-gray-500">
                &copy; {new Date().getFullYear()} Expense Tracker. All rights reserved.
            </p>
        </footer>
    );
};

export default Footer;
