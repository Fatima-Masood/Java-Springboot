
  const navbarToggler = document.querySelector('.navbar-toggler');
  const navbarCollapse = document.querySelector('.navbar-collapse');
  const navLinks = document.getElementById('navLinks');

  navbarToggler.addEventListener('click', function() {
    navbarCollapse.classList.toggle('show');
  });

  function updateNavbar() {
    const isAuthenticated = checkAuthStatus();

    navLinks.innerHTML = '';

    if (isAuthenticated) {
      const links = [
            {
              text: 'Dashboard',
              onClick: () => {
                window.location.href = '/pages/dashboard';
              }
            },
            {
              text: 'Expenditures',
              href: '/expenditures'
            },
            {
              text: 'Logout',
              href: '#',
              onClick: logout
            }
          ];

      links.forEach(link => {
        const li = document.createElement('li');
        li.className = 'nav-item';

        const a = document.createElement('a');
        a.className = 'nav-link';
        a.href = link.href;
        a.textContent = link.text;

        if (link.onClick) {
          a.addEventListener('click', link.onClick);
        }

        li.appendChild(a);
        navLinks.appendChild(li);
      });
    } else {
      const links = [
            {
              text: 'Login',
              className: 'btn btn-outline-light',
              onClick: () => {
                window.location.href = '/pages/login.html';
              }
            },
            {
              text: 'Register',
              className: 'btn btn-primary',
              onClick: () => {
                window.location.href = '/pages/register.html';
              }
            }
          ];

      links.forEach(link => {
        const li = document.createElement('li');
        li.className = 'nav-item';

        const a = document.createElement('a');
        a.className = `nav-link ${link.className || ''}`;
        a.href = link.href;
        a.textContent = link.text;

        li.appendChild(a);
        navLinks.appendChild(li);
      });
    }
  }

  function checkAuthStatus() {
    console.log(localStorage.getItem('authToken'));
    return localStorage.getItem('authToken') !== null;
  }

  function logout(e) {
    e.preventDefault();

    fetch(`${serverUri}/api/users/logout`, {
      method: 'POST',
      credentials: 'include'
    })
    .then(response => {
      .then(response => {
        if (response.ok) {
          localStorage.removeItem('authToken');
          updateNavbar();
          window.location.href = '/pages/login.html';
        } else {
          throw new Error('Logout failed');
        }
    })
    .catch(error => {
      console.error('Logout error:', error);
      alert('Failed to logout');
    });
    document.cookie = "access_token=; Max-Age=0; path=/; secure; HttpOnly";
    window.location.href = '/pages/login.html';

  }

  updateNavbar();

  window.addEventListener('storage', function(e) {
    if (e.key === 'authToken') {
      updateNavbar();
    }
  });

const serverUri = 'http://localhost:8080';