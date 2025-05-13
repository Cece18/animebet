document.addEventListener('DOMContentLoaded', function() {
    const tabs = document.querySelectorAll('.tab');
    const tabContents = document.querySelectorAll('.tab-content');
    const loginButton = document.getElementById('loginButton');
    const registerButton = document.getElementById('registerButton');
    const googleSignIn = document.getElementById('googleSignIn');
    const errorMessage = document.getElementById('errorMessage');

    // Tab switching
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            tabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            
            const targetId = tab.dataset.tab + 'Content';
            tabContents.forEach(content => {
                content.classList.add('hidden');
                if (content.id === targetId) {
                    content.classList.remove('hidden');
                }
            });
        });
    });

    // Login handler
    loginButton.addEventListener('click', async () => {
        const username = document.getElementById('loginUsername').value;
        const password = document.getElementById('loginPassword').value;
        const spinner = loginButton.querySelector('.spinner');

        try {
            spinner.classList.remove('hidden');
            loginButton.disabled = true;
            errorMessage.textContent = '';

            const response = await fetch('http://localhost:8080/api/users/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ username, password })
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Login failed');
            }

            localStorage.setItem('user', JSON.stringify({ 
                username: data.user.username, 
                points: data.user.points 
            }));

            window.location.href = '/home-page';

        } catch (err) {
            errorMessage.textContent = err.message || 'An error occurred during login';
        } finally {
            spinner.classList.add('hidden');
            loginButton.disabled = false;
        }
    });

    // Register handler
    registerButton.addEventListener('click', async () => {
        const username = document.getElementById('registerUsername').value;
        const email = document.getElementById('registerEmail').value;
        const password = document.getElementById('registerPassword').value;
        const spinner = registerButton.querySelector('.spinner');

        try {
            spinner.classList.remove('hidden');
            registerButton.disabled = true;
            errorMessage.textContent = '';

            const response = await fetch('http://localhost:8080/api/users/signup', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ username, email, password })
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'Registration failed');
            }

            localStorage.setItem('user', JSON.stringify({ username }));
            window.location.href = '/home-page';

        } catch (err) {
            errorMessage.textContent = err.message || 'An error occurred during registration';
        } finally {
            spinner.classList.add('hidden');
            registerButton.disabled = false;
        }
    });

    // Google Sign In handler
    googleSignIn.addEventListener('click', () => {
        console.log("Google sign-in clicked");
    });
});
