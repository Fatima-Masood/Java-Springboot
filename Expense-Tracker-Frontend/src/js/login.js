document.addEventListener("DOMContentLoaded", () => {
  document.getElementById("loginForm").addEventListener("submit", loginHandler);
});


function loginHandler(e) {
  e.preventDefault();

  const username = document.getElementById("username").value.trim();
  const password = document.getElementById("password").value;

  if (!username || !password) {
    alert("Enter username and password");
    return;
  }

  fetch("/api/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      username: username,
      password: password
    })
  })
  .then(response => {
    if (!response.ok) throw new Error("Login failed");
    return response.json();
  })
  .then(data => {
    alert("Login successful");
    // window.location.href = "/dashboard.html";
  })
  .catch(error => {
    alert(error.message || error);
    const errMsg = document.getElementById("errorMsg");
    if (errMsg) errMsg.classList.remove("d-none");
  });
}
