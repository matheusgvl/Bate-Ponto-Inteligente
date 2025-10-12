// Sistema de Controle de Acesso

const SYSTEM_CONFIG = {
    version: '1.0',
    environment: 'production',
    credentials: {
        username: 'david alenor',
        password: '123456'
    }
};

let systemState = {
    isLoggedIn: false,
    currentUser: null,
    stats: {
        totalAccesses: 0,
        activePersonnel: 0,
        deniedAccesses: 0,
        criticalAlerts: 0
    },
    areas: [],
    accessLogs: [],
    lastUpdate: new Date()
};

document.addEventListener('DOMContentLoaded', function() {
    localStorage.clear();
    sessionStorage.clear();
    
    setupLoginForm();
    setupClock();
    checkActiveSession();
});

// Configurar formulário de login
function setupLoginForm() {
    const loginForm = document.getElementById('loginForm');
    const loginBtn = document.getElementById('loginBtn');
    const errorMessage = document.getElementById('errorMessage');
    
    loginForm.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;
        
        // Validação simples
        if (!username || !password) {
            showError('Por favor, preencha todos os campos');
            return;
        }
        
        // Desabilitar botão durante login
        loginBtn.disabled = true;
        loginBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Entrando...';
        
        // Simular verificação (delay realista)
        setTimeout(() => {
            performLogin(username, password);
        }, 1000);
    });
}

function performLogin(username, password) {
    const loginBtn = document.getElementById('loginBtn');
    
    if (username === SYSTEM_CONFIG.credentials.username && 
        password === SYSTEM_CONFIG.credentials.password) {
        
        systemState.isLoggedIn = true;
        systemState.currentUser = {
            username: username,
            name: 'David Alenor',
            role: 'Supervisor de Segurança',
            loginTime: new Date()
        };
        
        sessionStorage.setItem('userSession', JSON.stringify(systemState.currentUser));
        showDashboard();
        
    } else {
        showError('Credenciais inválidas. Use: david alenor / 123456');
    }
    
    loginBtn.disabled = false;
    loginBtn.innerHTML = '<i class="fas fa-sign-in-alt"></i> Entrar no Sistema';
}

// Mostrar erro
function showError(message) {
    const errorDiv = document.getElementById('errorMessage');
    errorDiv.textContent = message;
    errorDiv.style.display = 'block';
    
    // Esconder após 5 segundos
    setTimeout(() => {
        errorDiv.style.display = 'none';
    }, 5000);
}

function showDashboard() {
    document.getElementById('loginContainer').style.display = 'none';
    document.getElementById('dashboardContainer').style.display = 'block';
    document.getElementById('supervisorName').textContent = systemState.currentUser.name;
    startDashboardUpdates();
}

function checkActiveSession() {
    const savedSession = sessionStorage.getItem('userSession');
    
    if (savedSession) {
        try {
            const userSession = JSON.parse(savedSession);
            systemState.isLoggedIn = true;
            systemState.currentUser = userSession;
            showDashboard();
        } catch (error) {
            sessionStorage.clear();
        }
    }
}

function setupClock() {
    function updateClock() {
        const now = new Date();
        
        const timeElement = document.getElementById('currentTime');
        if (timeElement) {
            timeElement.textContent = now.toLocaleTimeString('pt-BR', {
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit'
            });
        }
        
        // Atualizar data
        const dateElement = document.getElementById('currentDate');
        if (dateElement) {
            dateElement.textContent = now.toLocaleDateString('pt-BR', {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric'
            });
        }
    }
    
    // Atualizar imediatamente
    updateClock();
    setInterval(updateClock, 1000);
}

function startDashboardUpdates() {
    confirmCleanState();
    setInterval(confirmCleanState, 30000);
}

function confirmCleanState() {
    if (systemState.areas.length === 0 && systemState.accessLogs.length === 0) {
        // Estado correto
    } else {
        systemState.areas = [];
        systemState.accessLogs = [];
        systemState.stats = {
            totalAccesses: 0,
            activePersonnel: 0,
            deniedAccesses: 0,
            criticalAlerts: 0
        };
    }
    
    systemState.lastUpdate = new Date();
}

function logout() {
    systemState.isLoggedIn = false;
    systemState.currentUser = null;
    
    sessionStorage.clear();
    localStorage.clear();
    
    document.getElementById('dashboardContainer').style.display = 'none';
    document.getElementById('loginContainer').style.display = 'flex';
    
    // Limpar campos
    document.getElementById('username').value = '';
    document.getElementById('password').value = '';
    document.getElementById('errorMessage').style.display = 'none';
    
    console.log('✅ Logout realizado - sistema resetado');
}
