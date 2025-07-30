const serverUri = 'http://localhost:8080';

async function fetchUser() {
    try {
        const res = await fetch(serverUri + '/api/users', {
            credentials: 'include'
        });

        const user = await res.json();
        console.log(user);
        document.getElementById('username').textContent = user.username;
        document.getElementById('displayUsername').textContent = user.username;
        document.getElementById('role').textContent = user.role;

    } catch (err) {
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
        alert(text);
        if (res.ok) {
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
        alert(message);
        if (res.ok) {
            alert("Account Deleted!");
            window.location.href = '/pages/login.html';
        }
    } catch (err) {
        console.error('Delete account failed:', err);
    }
}

fetchUser();
