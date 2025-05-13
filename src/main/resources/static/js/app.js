// State management
let currentUser = null;
let categories = [];
let selectedCategoryId = null;
let notifications = [];

// DOM Elements
const userPointsElement = document.getElementById('userPoints');
const userInitialsElement = document.getElementById('userInitials');
const usernameElement = document.getElementById('username');
const categorySelectorElement = document.getElementById('categorySelector');
const selectedCategoryElement = document.getElementById('selectedCategory');
const nomineesGridElement = document.getElementById('nomineesGrid');
const loadingStateElement = document.getElementById('loadingState');
const notificationsPanelElement = document.getElementById('notificationsPanel');
const notificationsBtn = document.getElementById('notificationsBtn');
const userDropdownElement = document.getElementById('userDropdown');

// Initialize the application
async function init() {
    try {
        await fetchCurrentUser();
        if (!currentUser) {
            // Redirect to login if no authenticated user
            window.location.href = '/login';
            return;
        }
        updateUserInfo();
        setupTimerCountdown();
        await fetchCategories();
        setupCategories();
        setupEventListeners();
    } catch (error) {
        console.error('Error initializing app:', error);
        showToast('Error', 'Failed to initialize application', 'error');
    } finally {
        hideLoading();
    }
}

async function fetchCurrentUser() {
    try {
        const response = await fetch('/api/users/current');
        if (!response.ok) {
            throw new Error('Failed to fetch user data');
        }
        currentUser = await response.json();
    } catch (error) {
        console.error('Error fetching user:', error);
        showToast('Error', 'Failed to load user data', 'error');
        currentUser = null;
    }
}

async function fetchCategories() {
    try {
        const response = await fetch('/api/categories');
        if (!response.ok) {
            throw new Error('Failed to fetch categories');
        }
        categories = await response.json();
        if (!Array.isArray(categories)) {
            throw new Error('Invalid categories data');
        }
    } catch (error) {
        console.error('Error fetching categories:', error);
        showToast('Error', 'Failed to load categories', 'error');
        categories = [];
    }
}

// Update user information in the UI
function updateUserInfo() {
    if (!currentUser) return;
    
    userPointsElement.textContent = currentUser.points;
    userInitialsElement.textContent = currentUser.initials;
    usernameElement.textContent = currentUser.username;
}

// Timer countdown setup
function setupTimerCountdown() {
    const targetDate = new Date('May 25, 2025 00:00:00').getTime();

    function updateCountdown() {
        const now = new Date().getTime();
        const difference = targetDate - now;

        if (difference <= 0) {
            document.getElementById('timerCountdown').querySelector('span').textContent = 'Betting is closed';
            return;
        }

        const days = Math.floor(difference / (1000 * 60 * 60 * 24));
        const hours = Math.floor((difference % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        const minutes = Math.floor((difference % (1000 * 60 * 60)) / (1000 * 60));

        document.getElementById('timerCountdown').querySelector('span').textContent =
            `Bets lock in: ${days}d ${hours}h ${minutes}m`;
    }

    updateCountdown();
    setInterval(updateCountdown, 60000);
}

// Set up category buttons
function setupCategories() {
    const categoryButtons = categories.map(category => `
        <button
            class="btn ${selectedCategoryId === category.id ? 'btn-primary' : 'btn-outline'}"
            data-category-id="${category.id}"
            onclick="selectCategory(${category.id})"
        >
            ${category.name}
        </button>
    `).join('');

    categorySelectorElement.querySelector('div').innerHTML = categoryButtons;

    // Select first category by default
    if (categories.length > 0 && !selectedCategoryId) {
        selectCategory(categories[0].id);
    }
}

// Handle category selection
function selectCategory(categoryId) {
    selectedCategoryId = categoryId;
    const category = categories.find(c => c.id === categoryId);

    if (category) {
        selectedCategoryElement.classList.remove('hidden');
        selectedCategoryElement.querySelector('h3').textContent = category.name;
        selectedCategoryElement.querySelector('p').textContent = category.description;

        renderNominees(category.nominees);
    }

    // Update category buttons style
    setupCategories();
}

// Render nominee cards
function renderNominees(nominees) {
    const nomineesHtml = nominees.map(nominee => `
        <div class="anime-card overflow-hidden shadow-lg nominee-card" data-nominee-id="${nominee.id}">
            <div class="w-full h-40 bg-muted flex items-center justify-center overflow-hidden">
                ${nominee.imageUrl
        ? `<img src="${nominee.imageUrl}" alt="${nominee.name}" class="w-full h-full object-cover">`
        : `<div class="w-full h-full bg-gradient-to-br from-secondary to-primary opacity-40 flex items-center justify-center">
                        <span class="text-white text-lg font-bold">${nominee.name}</span>
                       </div>`
    }
            </div>
            <div class="p-4">
                <div class="flex justify-between items-start mb-3">
                    <h4 class="text-lg font-bold text-white">${nominee.name}</h4>
                    <div class="flex items-center text-accent">
                        <svg class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"/>
                        </svg>
                        <span class="font-medium">${nominee.multiplier ? nominee.multiplier.toFixed(2) + 'x' : '1.00x'}</span>
                    </div>
                </div>
                <div class="mt-3">
                    <div class="flex justify-between items-center mb-2">
                        <span class="text-sm text-muted-foreground">Your bet:</span>
                        <span class="text-sm font-medium text-accent">0 points</span>
                    </div>
                    <div class="flex items-center mb-4">
                        <input type="number" class="input w-full h-8 text-center border-x rounded-none bet-amount" value="0" min="0" max="${currentUser.points}" data-nominee-id="${nominee.id}">
                    </div>
                    <button class="btn btn-primary w-full place-bet-btn">Place Bet</button>
                </div>
            </div>
        </div>
    `).join('');

    nomineesGridElement.innerHTML = nomineesHtml;

    setupBetButtons();
}


function setupBetButtons() {
    document.querySelectorAll('.place-bet-btn').forEach(button => {
        button.addEventListener('click', placeBetHandler);
    });
}

async function placeBet(categoryId, nomineeId, amount) {
    try {
        const response = await fetch('/bets/place-bet', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                categoryId: categoryId,
                nomineeId: nomineeId,
                amount: amount
            })
        });

        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.error || 'Failed to place bet');
        }

        // Update user points in the UI
        await fetchCurrentUser();
        updateUserInfo();
        
        showToast('Success', 'Bet placed successfully!', 'success');
        
        // Refresh user's bets if necessary
        // You might want to add a function to refresh the bets display
        
    } catch (error) {
        console.error('Error placing bet:', error);
        showToast('Error', error.message, 'error');
    }
}

async function placeBetHandler(event) {
    const button = event.currentTarget;
    const card = button.closest('.nominee-card');
    const nomineeId = parseInt(card.dataset.nomineeId);
    const amount = parseInt(button.closest('.nominee-card').querySelector('.bet-amount').value);

    if (isNaN(amount) || amount <= 0) {
        showToast('Error', 'Please enter a valid bet amount', 'error');
        return;
    }

    try {
        // Check for existing bet
        const checkResponse = await fetch(`/bets/check-existing-bet?categoryId=${selectedCategoryId}&nomineeId=${nomineeId}`);
        const checkResult = await checkResponse.json();

        if (checkResult.hasExistingBet) {
            // Create confirmation dialog
            const confirmed = await showConfirmDialog(
                'Existing Bet Found',
                'You already have a bet for this nominee in this category. Would you like to update it with the new amount?'
            );

            if (confirmed) {
                // Update existing bet
                await updateBet(checkResult.existingBetId, nomineeId, amount);
                showToast('Success', 'Bet updated successfully!', 'success');
            } else {
                return; // Do nothing if user cancels
            }
        } else {
            // Place new bet
            await placeBet(selectedCategoryId, nomineeId, amount);
            showToast('Success', 'Bet placed successfully!', 'success');
        }

        // Reset bet input and refresh user data
        button.closest('.nominee-card').querySelector('.bet-amount').value = '';
        await fetchCurrentUser();
        updateUserInfo();

    } catch (error) {
        console.error('Error in placeBetHandler:', error);
        showToast('Error', error.message, 'error');
    }
}

function showConfirmDialog(title, message) {
    return new Promise((resolve) => {
        const dialog = document.createElement('div');
        dialog.className = 'fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50';
        dialog.innerHTML = `
            <div class="bg-card p-6 rounded-lg shadow-lg max-w-md w-full mx-4">
                <h3 class="text-lg font-bold text-white mb-2">${title}</h3>
                <p class="text-muted-foreground mb-4">${message}</p>
                <div class="flex justify-end gap-2">
                    <button class="btn btn-outline" id="cancelBtn">Cancel</button>
                    <button class="btn btn-primary" id="confirmBtn">Update Bet</button>
                </div>
            </div>
        `;

        document.body.appendChild(dialog);

        dialog.querySelector('#confirmBtn').addEventListener('click', () => {
            dialog.remove();
            resolve(true);
        });

        dialog.querySelector('#cancelBtn').addEventListener('click', () => {
            dialog.remove();
            resolve(false);
        });
    });
}

async function updateBet(betId, nomineeId, newAmount) {
    const response = await fetch(`/bets/update-bet/${betId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            nomineeId: nomineeId,
            newAmount: newAmount
        })
    });

    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || 'Failed to update bet');
    }

    return await response.json();
}

// Notifications Panel
function toggleNotificationsPanel() {
    const isVisible = notificationsPanelElement.classList.contains('hidden');
    notificationsPanelElement.classList.toggle('hidden', !isVisible);
}

// User Dropdown
function toggleUserDropdown() {
    const dropdownContent = document.createElement('div');
    dropdownContent.className = 'dropdown-content';
    dropdownContent.innerHTML = `
        <div class="p-2 text-sm font-medium text-muted-foreground">My Account</div>
        <hr class="border-border my-2">
        <a href="/profile" class="dropdown-item">
            <svg class="h-4 w-4 mr-2" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
            </svg>
            Profile
        </a>
        <div class="dropdown-item text-destructive" onclick="logout()">
            <svg class="h-4 w-4 mr-2" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
            </svg>
            Log out
        </div>
    `;

    const existingDropdown = userDropdownElement.querySelector('.dropdown-content');
    if (existingDropdown) {
        existingDropdown.remove();
    } else {
        userDropdownElement.appendChild(dropdownContent);
    }
}

// Toast notifications
function showToast(title, message, type = 'success') {
    const toast = document.createElement('div');
    toast.className = `fixed bottom-4 right-4 bg-card p-4 rounded-lg shadow-lg border border-border z-50 ${
        type === 'error' ? 'border-destructive' : 'border-primary'
    }`;
    toast.innerHTML = `
        <h4 class="font-bold text-white">${title}</h4>
        <p class="text-sm text-muted-foreground">${message}</p>
    `;

    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

// Loading state management
function showLoading() {
    loadingStateElement.classList.remove('hidden');
}

function hideLoading() {
    loadingStateElement.classList.add('hidden');
}

// Event listeners
function setupEventListeners() {
    notificationsBtn.addEventListener('click', toggleNotificationsPanel);
    userDropdownElement.querySelector('button').addEventListener('click', toggleUserDropdown);

    // Close dropdowns when clicking outside
    document.addEventListener('click', (e) => {
        if (!notificationsPanelElement.contains(e.target) && !notificationsBtn.contains(e.target)) {
            notificationsPanelElement.classList.add('hidden');
        }

        const dropdown = userDropdownElement.querySelector('.dropdown-content');
        if (dropdown && !userDropdownElement.contains(e.target)) {
            dropdown.remove();
        }
    });
}


// Initialize the application when the DOM is loaded
document.addEventListener('DOMContentLoaded', init);

