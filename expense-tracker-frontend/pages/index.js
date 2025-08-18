import { useState, useEffect } from "react";

export default function Home() {
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

  // Tailwind class combinations
  const containerStyles = `min-h-screen ${isDark ? 'bg-gray-700' : 'bg-gray'}`;
  const headingStyles = `text-4xl font-bold text-center ${isDark ? 'text-gray-100' : 'text-gray-600'} mb-8`;
  const subheadingStyles = `text-2xl ${isDark ? 'text-gray-300' : 'text-gray-500'} mb-4`;
  const cardStyles = `${isDark ? 'bg-gray-800' : 'bg-white'} p-6 rounded-lg shadow-md`;
  const cardTitleStyles = `text-2xl font-semibold ${isDark ? 'text-blue-300' : 'text-[#2c5282]'} mb-3`;
  const cardTextStyles = `${isDark ? 'text-gray-400' : 'text-[#4a5568]'} text-xl`;
  const buttonStyles = `${isDark ? 'bg-blue-600 hover:bg-blue-700' : 'bg-[#1a365d] hover:bg-[#2c5282]'} 
    text-white px-8 py-3 rounded-full transition-colors text-xl`;

  const features = [
    {
      title: "Track Expenses",
      description: "Easily log and categorize your daily expenses in one place"
    },
    {
      title: "Visual Reports",
      description: "View detailed charts and analytics of your spending patterns"
    },
    {
      title: "Budget Smart",
      description: "Set budgets and get notifications to stay on track"
    }
  ];

  return (
    <div className={containerStyles}>
      <div className="max-w-5xl mx-auto px-4 py-16">
        <h1 className={headingStyles}>
          Welcome to Smart Expense Tracker
        </h1>
        
        <div className="text-center mb-12">
          <p className={subheadingStyles}>
            Take control of your finances with our intuitive expense tracking solution
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-12">
          {features.map((feature, index) => (
            <div key={index} className={cardStyles}>
              <h3 className={cardTitleStyles}>{feature.title}</h3>
              <p className={cardTextStyles}>{feature.description}</p>
            </div>
          ))}
        </div>

        <div className="text-center">
          <button className={buttonStyles}>
            Get Started
          </button>
        </div>
      </div>
    </div>
  );
}
