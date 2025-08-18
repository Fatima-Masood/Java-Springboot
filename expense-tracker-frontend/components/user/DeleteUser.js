import { useRouter } from "next/router";
export default function DeleteUser({token, setError, setMessage}) {
    const router = useRouter();

    const handleDeleteUser = async () => {
        setMessage("");
        setError("");

        if (!token) {
            setError("No token found");
            return;
        }

        try {
            const res = await fetch(`../api/users/delete`, {
                method: "DELETE",
                credentials: "include",
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (!res.ok) {
                const text = await res.text();
                throw new Error(`Failed to delete account: ${res.status} - ${text}`);
            }

            setMessage("Account deleted. Redirecting...");
            setTimeout(() => {
                Cookies.remove("token");
                Cookies.remove("csrfToken");
                router.push("/authentication/login");
            }, 2000);
        } catch (err) {
            console.error(err);
            setError(err.message);
        }
    };

    return (
        <button 
            onClick={handleDeleteUser}
            className="w-full bg-gray-600 text-white py-2 rounded hover:bg-red-700 transition"
        >
            Delete My Account
        </button>
    );
}
