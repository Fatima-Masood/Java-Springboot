const serverUri = 'http://localhost:8080';

async function fetchUser() {
    try {
        const res = await fetch(serverUri + '/api/users', {
            credentials: 'include',
            headers: {
                    'Content-Type': 'application/json'
            },
        });
        console.log(res);
        const user = await res.json();
        localStorage.setItem("username", user);
        document.getElementById('username').textContent = user.username;
        document.getElementById('displayUsername').textContent = user.username;
        document.getElementById('role').textContent = user.role;

    } catch (err) {
        alert ("Cannot fetch user");
        console.error('Fetch user failed:', err);
        window.location.href = '/pages/login.html';
    }
}

document.getElementById('updatePasswordForm').addEventListener('submit', async function (e) {
    e.preventDefault();

    const oldPass = document.getElementById('oldPassword').value;
    const newPass = document.getElementById('newPassword').value;

    try {
        const res = await fetch(serverUri + '/api/users', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include',
            body: JSON.stringify({
                oldPassword: oldPass,
                newPassword: newPass
            })
        });

        const text = await res.text();
        if (res.ok) {
            alert ("Password updated!")
            document.getElementById('updatePasswordForm').reset();
        }
    } catch (err) {
        console.error('Password update failed:', err);
    }
});

async function deleteAccount() {
    if (!confirm('Are you sure you want to delete your account? This action is irreversible.')) return;

    try {
        const res = await fetch(serverUri + '/api/users', {
            method: 'DELETE',
            credentials: 'include'
        });

        const message = await res.text();
        if (res.ok) {
            alert("Account Deleted!");
            localStorage.removeItem('authToken');
            localStorage.removeItem('username');
            window.location.href = '/pages/login.html';
        }
    } catch (err) {
        console.error('Delete account failed:', err);
    }
}

fetchUser();
