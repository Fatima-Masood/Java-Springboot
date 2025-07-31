
const serverUri = 'http://localhost:8080';

document.addEventListener("DOMContentLoaded", () => {
  document.getElementById("loginForm").addEventListener("submit", loginHandler);
});


function loginHandler(e) {
  e.preventDefault();

  const user = document.getElementById("username").value.trim();
  const pass = document.getElementById("password").value;

  if (!user || !pass) {
    alert("Enter username and password");
    return;
  }

  fetch(serverUri + "/api/users/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    credentials: "include",
    body: JSON.stringify({
      username: user,
      password: pass
    })
  })
  .then(async response => {
    if (!response.ok) {
      const text = await response.text();
      throw new Error(text);
    }

    const contentType = response.headers.get("Content-Type");
    if (contentType && contentType.includes("application/json")) {
      return response.json();
    } else {
      return response.text();
    }
  })
  .then(data => {
    localStorage.setItem("authToken", data);

    window.location.href = '/pages/dashboard.html';
  })
  .catch(error => {
    alert(error.message || error);
  });

}
