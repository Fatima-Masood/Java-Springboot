const serverUri = 'http://localhost:8080';

function addNavItem(link, label, buttonClass = "customButton") {
  const li = document.createElement("li");
  li.className = "navItem";

  const a = document.createElement("button");
  a.textContent = label;
  a.href = "#";
  a.className = buttonClass;

  a.addEventListener("click", (e) => {
    e.preventDefault();

    const pageArea = document.getElementById("page-area");
    pageArea.innerHTML = "";

    const iframe = document.createElement("iframe");
    iframe.src = link;
    iframe.className = "page";

    pageArea.appendChild(iframe);

    if (buttonClass !== "customButton")
       logout(e);
  });

  li.appendChild(a);
  document.querySelector("ul.navList").appendChild(li);
}


function updateNavbar() {
  const navList = document.querySelector("ul.navList");
  navList.innerHTML = "";

  if (checkAuthStatus()) {
    addNavItem("/pages/dashboard.html", "Dashboard");
    addNavItem("/pages/expenditures.html", "Expenditures");
    addNavItem("/pages/login.html", "Logout", "customButtonLogout");
  } else {
    addNavItem("/pages/login.html", "Login");
    addNavItem("/pages/register.html", "Register");
  }
}

function checkAuthStatus() {
  return localStorage.getItem('authToken') !== null;
}

function logout(e) {
    e.preventDefault();

    fetch("http://localhost:8080/api/users/logout", {
        credentials: "include"
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Logout failed");
        }
        return response.json();
    })
    .then(data => {
        console.log(data.message);
        localStorage.removeItem('authToken');
        localStorage.removeItem('username');
        updateNavbar();
    })
    .catch(error => {
        console.error("Error during logout:", error);
    });
}


window.addEventListener('storage', function (e) {
  if (e.key === 'authToken') {
    updateNavbar();
  }
});

document.addEventListener("DOMContentLoaded", updateNavbar);
