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
    if (buttonClass !== "customButton")
       logout(e);

    const pageArea = document.getElementById("page-area");
    pageArea.innerHTML = "";

    const iframe = document.createElement("iframe");
    iframe.src = link;
    iframe.className = "page";

    pageArea.appendChild(iframe);


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

    localStorage.removeItem('authToken');
    document.cookie = "access_token=; Max-Age=0; path=/; secure; HttpOnly";
    updateNavbar();
}

window.addEventListener('storage', function (e) {
  if (e.key === 'authToken') {
    updateNavbar();
  }
});

document.addEventListener("DOMContentLoaded", updateNavbar);
