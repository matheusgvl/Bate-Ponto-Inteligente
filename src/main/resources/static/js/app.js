document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

function initializeApp() {
    setupLoginForm();
    setupSidebar();
    setupPersonForm();
    setupModal();
    updateTime();
    setInterval(updateTime, 1000);
}

function setupLoginForm() {
    const loginForm = document.getElementById('loginForm');
    const loginContainer = document.getElementById('loginContainer');
    const dashboardContainer = document.getElementById('dashboardContainer');
    const errorMessage = document.getElementById('errorMessage');
    
    loginForm.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value.trim();
        
        if (username === 'david alenor' && password === '123456') {
            loginContainer.style.display = 'none';
            dashboardContainer.style.display = 'block';
            document.getElementById('sidebarUserName').textContent = 'David Alenor';
            errorMessage.style.display = 'none';
        } else {
            errorMessage.textContent = 'Usuário ou senha incorretos!';
            errorMessage.style.display = 'block';
        }
    });
}

function setupSidebar() {
    const menuToggle = document.getElementById('menuToggle');
    const sidebar = document.getElementById('sidebar');
    const sidebarOverlay = document.getElementById('sidebarOverlay');
    const closeSidebar = document.getElementById('closeSidebar');
    const navItems = document.querySelectorAll('.nav-item');
    
    if (menuToggle) {
        menuToggle.addEventListener('click', function() {
            sidebar.classList.add('active');
            sidebarOverlay.classList.add('active');
        });
    }
    
    function closeSidebarMenu() {
        sidebar.classList.remove('active');
        sidebarOverlay.classList.remove('active');
    }
    
    closeSidebar.addEventListener('click', closeSidebarMenu);
    sidebarOverlay.addEventListener('click', closeSidebarMenu);
    
    navItems.forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            
            navItems.forEach(nav => nav.classList.remove('active'));
            this.classList.add('active');
            
            const section = this.getAttribute('data-section');
            showSection(section);
            
            closeSidebarMenu();
        });
    });
}

function showSection(section) {
    const dashboardMain = document.getElementById('dashboardMain');
    const cadastroPessoasPanel = document.getElementById('cadastroPessoasPanel');
    
    if (section === 'dashboard') {
        dashboardMain.style.display = 'grid';
        if (cadastroPessoasPanel) cadastroPessoasPanel.style.display = 'none';
    } else if (section === 'cadastrar-pessoas') {
        dashboardMain.style.display = 'none';
        if (cadastroPessoasPanel) cadastroPessoasPanel.style.display = 'block';
    }
}

function setupPersonForm() {
    const personForm = document.getElementById('cadastroPessoaForm');
    const cpfInput = document.getElementById('cpf');
    const btnCancelar = document.getElementById('btnCancelar');
    const btnCadastrarCartao = document.getElementById('btnCadastrarCartao');
    
    if (cpfInput) {
        cpfInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            if (value.length <= 11) {
                value = value.replace(/(\d{3})(\d)/, '$1.$2');
                value = value.replace(/(\d{3})(\d)/, '$1.$2');
                value = value.replace(/(\d{3})(\d{1,2})$/, '$1-$2');
            }
            e.target.value = value;
        });
    }
    
    if (personForm) {
        personForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const formData = new FormData(personForm);
            const personData = {};
            
            for (let [key, value] of formData.entries()) {
                personData[key] = value;
            }
            
            if (validateCPF(personData.cpf)) {
                showModal(
                    'Sucesso!',
                    `Pessoa ${personData.nome} cadastrada com sucesso!`,
                    'fas fa-check-circle',
                    [{
                        text: 'OK',
                        class: 'modal-btn-primary',
                        action: () => {
                            personForm.reset();
                            closeModal();
                            // Voltar ao Dashboard após sucesso
                            showSection('dashboard');
                            // Atualizar navegação ativa
                            document.querySelectorAll('.nav-item').forEach(nav => nav.classList.remove('active'));
                            document.querySelector('.nav-item[data-section="dashboard"]').classList.add('active');
                        }
                    }]
                );
            } else {
                showModal(
                    'CPF Inválido',
                    'Por favor, digite um CPF válido.',
                    'fas fa-exclamation-triangle',
                    [{
                        text: 'OK',
                        class: 'modal-btn-primary',
                        action: closeModal
                    }]
                );
            }
        });
    }
    
    if (btnCancelar) {
        btnCancelar.addEventListener('click', function() {
            showModal(
                'Cancelar Cadastro',
                'Tem certeza que deseja cancelar? Todos os dados serão perdidos.',
                'fas fa-exclamation-triangle',
                [
                    {
                        text: 'Não, continuar',
                        class: 'modal-btn-secondary',
                        action: closeModal
                    },
                    {
                        text: 'Sim, cancelar',
                        class: 'modal-btn-primary',
                        action: () => {
                            personForm.reset();
                            closeModal();
                            showSection('dashboard');
                            document.querySelectorAll('.nav-item').forEach(nav => nav.classList.remove('active'));
                            document.querySelector('.nav-item[data-section="dashboard"]').classList.add('active');
                        }
                    }
                ]
            );
        });
    }
    
    if (btnCadastrarCartao) {
        btnCadastrarCartao.addEventListener('click', function() {
            showModal(
                'Cadastrar Cartão',
                'Funcionalidade de cadastro de cartão será implementada em breve.',
                'fas fa-credit-card',
                [{
                    text: 'OK',
                    class: 'modal-btn-primary',
                    action: closeModal
                }]
            );
        });
    }
}

function validateCPF(cpf) {
    cpf = cpf.replace(/[^\d]+/g, '');
    
    if (cpf.length !== 11 || /^(\d)\1{10}$/.test(cpf)) {
        return false;
    }
    
    let sum = 0;
    for (let i = 0; i < 9; i++) {
        sum += parseInt(cpf.charAt(i)) * (10 - i);
    }
    let checkDigit = 11 - (sum % 11);
    if (checkDigit === 10 || checkDigit === 11) checkDigit = 0;
    if (checkDigit !== parseInt(cpf.charAt(9))) return false;
    
    sum = 0;
    for (let i = 0; i < 10; i++) {
        sum += parseInt(cpf.charAt(i)) * (11 - i);
    }
    checkDigit = 11 - (sum % 11);
    if (checkDigit === 10 || checkDigit === 11) checkDigit = 0;
    
    return checkDigit === parseInt(cpf.charAt(10));
}

function setupModal() {
    const modalOverlay = document.getElementById('modalOverlay');
    
    modalOverlay.addEventListener('click', function(e) {
        if (e.target === modalOverlay) {
            closeModal();
        }
    });
}

function showModal(title, message, icon, buttons) {
    const modalOverlay = document.getElementById('modalOverlay');
    const modalTitle = document.getElementById('modalTitle');
    const modalMessage = document.getElementById('modalMessage');
    const modalIcon = document.querySelector('.modal-icon i');
    const modalActions = document.querySelector('.modal-actions');
    
    modalTitle.textContent = title;
    modalMessage.textContent = message;
    modalIcon.className = icon;
    
    modalActions.innerHTML = '';
    buttons.forEach(button => {
        const btn = document.createElement('button');
        btn.className = `modal-btn ${button.class}`;
        btn.innerHTML = `<i class="${button.icon || 'fas fa-check'}"></i> ${button.text}`;
        btn.addEventListener('click', button.action);
        modalActions.appendChild(btn);
    });
    
    modalOverlay.classList.add('active');
}

function closeModal() {
    const modalOverlay = document.getElementById('modalOverlay');
    modalOverlay.classList.remove('active');
}

function updateTime() {
    const timeElement = document.querySelector('.time');
    const dateElement = document.querySelector('.date');
    
    if (timeElement && dateElement) {
        const now = new Date();
        const timeString = now.toLocaleTimeString('pt-BR');
        const dateString = now.toLocaleDateString('pt-BR', {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
        
        timeElement.textContent = timeString;
        dateElement.textContent = dateString;
    }
}

function logout() {
    const loginContainer = document.getElementById('loginContainer');
    const dashboardContainer = document.getElementById('dashboardContainer');
    
    dashboardContainer.style.display = 'none';
    loginContainer.style.display = 'flex';
    
    document.getElementById('username').value = '';
    document.getElementById('password').value = '';
    document.getElementById('errorMessage').style.display = 'none';
}
