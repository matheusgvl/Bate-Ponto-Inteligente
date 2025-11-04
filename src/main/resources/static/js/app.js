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
            carregarEstatisticas();
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
                savePersonData(personData);
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
            const nome = document.getElementById('nome').value.trim();
            const cpf = document.getElementById('cpf').value.trim();
            
            if (!nome || !cpf) {
                showModal(
                    'Dados Incompletos',
                    'Por favor, preencha pelo menos o nome e CPF antes de cadastrar o cartão.',
                    'fas fa-exclamation-triangle',
                    [{
                        text: 'OK',
                        class: 'modal-btn-primary',
                        action: closeModal
                    }]
                );
                return;
            }
            
            startCardReading();
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

function startCardReading() {
    showModal(
        'Aguardando Cartão NFC',
        'Aproxime o cartão do leitor para cadastrar...',
        'fas fa-credit-card',
        [
            {
                text: 'Cancelar',
                class: 'modal-btn-secondary',
                action: () => {
                    stopCardReading();
                    closeModal();
                }
            }
        ]
    );
    
    const modalIcon = document.querySelector('.modal-icon i');
    modalIcon.className = 'fas fa-credit-card';
    modalIcon.parentElement.style.animation = 'modalPulse 1s infinite';
    
    readCardUID();
}

async function readCardUID() {
    try {
        console.log('Iniciando leitura do cartão NFC...');
        
        const response = await fetch('/api/card/read', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        console.log('Resposta do servidor:', response.status);
        
        if (response.ok) {
            const data = await response.json();
            console.log('Dados recebidos:', data);
            
            if (data.success && data.uid) {
                console.log('Cartão detectado com sucesso:', data.uid);
                handleCardDetected(data.uid);
            } else {
                console.error('Erro na leitura:', data.error || 'Erro desconhecido');
                showCardError(data.error || 'Erro ao ler cartão. Tente novamente.');
            }
        } else {
            console.error('Erro HTTP:', response.status);
            showCardError('Erro na comunicação com o leitor.');
        }
    } catch (error) {
        console.error('Erro ao ler cartão:', error);
        showCardError('Erro de conexão com o leitor.');
    }
}

function handleCardDetected(uid) {
    if (!uid.startsWith('63DF') && !uid.startsWith('A979') && 
        !uid.startsWith('63df') && !uid.startsWith('a979')) {
        showModal(
            'Cartão Inválido',
            `Cartão não reconhecido. UID: ${uid}\nApenas cartões com UID iniciado em 63DF ou A979 são aceitos.`,
            'fas fa-exclamation-triangle',
            [
                {
                    text: 'Tentar Novamente',
                    class: 'modal-btn-primary',
                    action: startCardReading
                },
                {
                    text: 'Cancelar',
                    class: 'modal-btn-secondary',
                    action: closeModal
                }
            ]
        );
        return;
    }
    
    showModal(
        'Cartão Detectado!',
        `UID do cartão: ${uid}\n\nDeseja cadastrar este cartão?`,
        'fas fa-check-circle',
        [
            {
                text: 'Não, tentar outro',
                class: 'modal-btn-secondary',
                action: startCardReading
            },
            {
                text: 'Sim, cadastrar',
                class: 'modal-btn-primary',
                action: () => savePersonWithCard(uid)
            }
        ]
    );
}

function savePersonWithCard(cardUID) {
    const personForm = document.getElementById('cadastroPessoaForm');
    const formData = new FormData(personForm);
    const personData = {};
    
    for (let [key, value] of formData.entries()) {
        personData[key] = value;
    }
    
    personData.cardUID = cardUID;
    
    if (!validateCPF(personData.cpf)) {
        showModal(
            'CPF Inválido',
            'Por favor, digite um CPF válido antes de cadastrar.',
            'fas fa-exclamation-triangle',
            [{
                text: 'OK',
                class: 'modal-btn-primary',
                action: closeModal
            }]
        );
        return;
    }
    
    savePersonData(personData);
}

async function savePersonData(personData) {
    try {
        const response = await fetch('/api/person/save', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(personData)
        });
        
        if (response.ok) {
            const result = await response.json();
            showModal(
                'Sucesso!',
                `Pessoa ${personData.nome} cadastrada com sucesso!\nCartão UID: ${personData.cardUID}`,
                'fas fa-check-circle',
                [{
                    text: 'OK',
                    class: 'modal-btn-primary',
                    action: () => {
                        document.getElementById('cadastroPessoaForm').reset();
                        closeModal();
                        showSection('dashboard');
                        document.querySelectorAll('.nav-item').forEach(nav => nav.classList.remove('active'));
                        document.querySelector('.nav-item[data-section="dashboard"]').classList.add('active');
                    }
                }]
            );
        } else {
            const error = await response.json();
            showCardError(error.message || 'Erro ao salvar pessoa.');
        }
    } catch (error) {
        console.error('Erro ao salvar pessoa:', error);
        showCardError('Erro de conexão com o servidor.');
    }
}

function showCardError(message) {
    showModal(
        'Erro no Cartão',
        message,
        'fas fa-exclamation-triangle',
        [
            {
                text: 'Tentar Novamente',
                class: 'modal-btn-primary',
                action: startCardReading
            },
            {
                text: 'Cancelar',
                class: 'modal-btn-secondary',
                action: closeModal
            }
        ]
    );
}

function stopCardReading() {
    fetch('/api/card/stop', { method: 'POST' }).catch(() => {});
}

async function carregarEstatisticas() {
    console.log('Carregando estatísticas do dashboard...');
    try {
        const response = await fetch('/api/person/stats', {
            method: 'GET',
            cache: 'no-cache', // <-- A SOLUÇÃO
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Falha ao buscar estatísticas: ' + response.statusText);
        }

        const stats = await response.json();
        console.log('Estatísticas recebidas:', stats);
        document.getElementById('stat-pessoas-ativas').textContent = stats.totalPessoas;
        document.getElementById('stat-acessos-hoje').textContent = stats.acessosHoje;
        document.getElementById('stat-acessos-negados').textContent = stats.acessosNegados;
        document.getElementById('stat-alertas-criticos').textContent = 0;

    } catch (error) {
        console.error('Falha ao carregar estatísticas:', error);
        showModal(
            'Erro de Rede',
            'Não foi possível carregar as estatísticas do dashboard. Tente mais tarde.',
            'fas fa-exclamation-triangle',
            [{
                text: 'OK',
                class: 'modal-btn-primary',
                action: closeModal
            }]
        );
    }
}
