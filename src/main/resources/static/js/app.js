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
    const navSubitems = document.querySelectorAll('.nav-subitem');
    const pessoaMenuItem = document.getElementById('pessoaMenuItem');
    const pessoaSubmenu = document.getElementById('pessoaSubmenu');

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

    // Toggle do menu dropdown de Pessoa
    if (pessoaMenuItem) {
        pessoaMenuItem.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();

            const isActive = pessoaSubmenu.classList.contains('active');
            pessoaSubmenu.classList.toggle('active');
            pessoaMenuItem.classList.toggle('active');

            // Se o submenu foi aberto, não fecha o sidebar
            if (!isActive) {
                return false;
            }
        });
    }

    // Navegação dos itens principais (sem dropdown)
    navItems.forEach(item => {
        // Ignora o item pai do dropdown
        if (item.id === 'pessoaMenuItem') {
            return;
        }

        item.addEventListener('click', function(e) {
            e.preventDefault();

            // Remove active de todos os itens e subitens
            navItems.forEach(nav => {
                if (nav.id !== 'pessoaMenuItem') {
                    nav.classList.remove('active');
                }
            });
            navSubitems.forEach(sub => sub.classList.remove('active'));

            // Fecha o submenu de Pessoa
            pessoaSubmenu.classList.remove('active');
            pessoaMenuItem.classList.remove('active');

            // Ativa o item clicado
            this.classList.add('active');

            const section = this.getAttribute('data-section');
            showSection(section);

            closeSidebarMenu();
        });
    });

    // Navegação dos subitens do dropdown
    navSubitems.forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();

            // Remove active de todos os itens principais (exceto o pai)
            navItems.forEach(nav => {
                if (nav.id !== 'pessoaMenuItem') {
                    nav.classList.remove('active');
                }
            });

            // Remove active de todos os subitens
            navSubitems.forEach(sub => sub.classList.remove('active'));

            // Ativa o subitem clicado
            this.classList.add('active');

            // Mantém o item pai ativo
            pessoaMenuItem.classList.add('active');

            const section = this.getAttribute('data-section');
            showSection(section);

            closeSidebarMenu();
        });
    });
}

function showSection(section) {
    const dashboardMain = document.getElementById('dashboardMain');
    const cadastroPessoasPanel = document.getElementById('cadastroPessoasPanel');
    const listagemPessoasPanel = document.getElementById('listagemPessoasPanel');

    // Esconde todas as seções
    dashboardMain.style.display = 'none';
    if (cadastroPessoasPanel) cadastroPessoasPanel.style.display = 'none';
    if (listagemPessoasPanel) listagemPessoasPanel.style.display = 'none';

    // Mostra a seção correspondente
    if (section === 'dashboard') {
        dashboardMain.style.display = 'grid';
    } else if (section === 'cadastrar-pessoas') {
        if (cadastroPessoasPanel) cadastroPessoasPanel.style.display = 'block';
    } else if (section === 'listar-pessoas') {
        if (listagemPessoasPanel) {
            listagemPessoasPanel.style.display = 'block';
            carregarListaPessoas();
        }
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

        const response = await fetch('/api/person/card/read', {
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
    fetch('/api/person/card/stop', { method: 'POST' }).catch(() => {});
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

async function carregarListaPessoas() {
    const pessoasGrid = document.getElementById('pessoasGrid');

    // Mostra estado de carregamento
    pessoasGrid.innerHTML = `
        <div class="loading-state">
            <i class="fas fa-spinner fa-spin"></i>
            <p>Carregando pessoas...</p>
        </div>
    `;

    try {
        const response = await fetch('/api/person/list', {
            method: 'GET',
            cache: 'no-cache',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Falha ao buscar pessoas: ' + response.statusText);
        }

        const pessoas = await response.json();
        console.log('Pessoas recebidas:', pessoas);

        if (pessoas.length === 0) {
            pessoasGrid.innerHTML = `
                <div class="empty-state-list">
                    <i class="fas fa-users"></i>
                    <h4>Nenhuma pessoa cadastrada</h4>
                    <p>Cadastre a primeira pessoa no sistema</p>
                </div>
            `;
            return;
        }

        renderizarPessoas(pessoas);

    } catch (error) {
        console.error('Erro ao carregar pessoas:', error);
        pessoasGrid.innerHTML = `
            <div class="empty-state-list">
                <i class="fas fa-exclamation-triangle"></i>
                <h4>Erro ao carregar pessoas</h4>
                <p>Não foi possível conectar ao servidor</p>
            </div>
        `;
    }
}

function renderizarPessoas(pessoas) {
    const pessoasGrid = document.getElementById('pessoasGrid');
    const searchInput = document.getElementById('searchPessoa');

    // Função para renderizar
    function render(listaPessoas) {
        pessoasGrid.innerHTML = '';

        listaPessoas.forEach(pessoa => {
            const iniciais = pessoa.nome.split(' ')
                .map(n => n[0])
                .slice(0, 2)
                .join('')
                .toUpperCase();

            const funcaoFormatada = pessoa.funcao.charAt(0).toUpperCase() +
                pessoa.funcao.slice(1).toLowerCase();

            const card = document.createElement('div');
            card.className = 'pessoa-card';
            card.innerHTML = `
                <div class="pessoa-card-header">
                    <div class="pessoa-avatar">${iniciais}</div>
                    <div class="pessoa-info">
                        <div class="pessoa-nome">${pessoa.nome}</div>
                        <span class="pessoa-funcao">${funcaoFormatada}</span>
                    </div>
                </div>
                <div class="pessoa-card-body">
                    <div class="pessoa-detail">
                        <i class="fas fa-id-card"></i>
                        <span>${pessoa.cpf}</span>
                    </div>
                    <div class="pessoa-detail">
                        <i class="fas fa-address-card"></i>
                        <span>RG: ${pessoa.rg}</span>
                    </div>
                    <div class="pessoa-detail">
                        <i class="fas fa-calendar-alt"></i>
                        <span>${formatarData(pessoa.dataNascimento)}</span>
                    </div>
                    ${pessoa.cardUID ? `
                        <div class="pessoa-detail">
                            <i class="fas fa-credit-card"></i>
                            <span>Cartão: ${pessoa.cardUID}</span>
                        </div>
                    ` : ''}
                </div>
                <div class="pessoa-card-footer">
                    <div class="pessoa-status">
                        <span class="status-dot"></span>
                        <span>Ativo</span>
                    </div>
                    <div class="pessoa-actions">
                        <button class="btn-icon" title="Editar" onclick="editarPessoa(${pessoa.id})">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn-icon" title="Excluir" onclick="excluirPessoa(${pessoa.id}, '${pessoa.nome}')">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </div>
            `;
            pessoasGrid.appendChild(card);
        });
    }

    // Renderiza inicialmente todas as pessoas
    render(pessoas);

    // Adiciona funcionalidade de busca
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            const termo = this.value.toLowerCase().trim();

            if (termo === '') {
                render(pessoas);
                return;
            }

            const pessoasFiltradas = pessoas.filter(pessoa => {
                return pessoa.nome.toLowerCase().includes(termo) ||
                    pessoa.cpf.includes(termo) ||
                    pessoa.funcao.toLowerCase().includes(termo);
            });

            if (pessoasFiltradas.length === 0) {
                pessoasGrid.innerHTML = `
                    <div class="empty-state-list">
                        <i class="fas fa-search"></i>
                        <h4>Nenhum resultado encontrado</h4>
                        <p>Tente buscar com outros termos</p>
                    </div>
                `;
            } else {
                render(pessoasFiltradas);
            }
        });
    }
}

function formatarData(dataString) {
    const data = new Date(dataString + 'T00:00:00');
    return data.toLocaleDateString('pt-BR');
}

function editarPessoa(id) {
    showModal(
        'Editar Pessoa',
        'Funcionalidade em desenvolvimento.',
        'fas fa-info-circle',
        [{
            text: 'OK',
            class: 'modal-btn-primary',
            action: closeModal
        }]
    );
}

function excluirPessoa(id, nome) {
    showModal(
        'Confirmar Exclusão',
        `Tem certeza que deseja excluir ${nome}? Esta ação não pode ser desfeita.`,
        'fas fa-exclamation-triangle',
        [
            {
                text: 'Cancelar',
                class: 'modal-btn-secondary',
                action: closeModal
            },
            {
                text: 'Excluir',
                class: 'modal-btn-primary',
                action: async () => {
                    try {
                        const response = await fetch(`/api/person/${id}`, {
                            method: 'DELETE'
                        });

                        if (response.ok) {
                            showModal(
                                'Sucesso',
                                `${nome} foi excluído com sucesso.`,
                                'fas fa-check-circle',
                                [{
                                    text: 'OK',
                                    class: 'modal-btn-primary',
                                    action: () => {
                                        closeModal();
                                        carregarListaPessoas();
                                        carregarEstatisticas();
                                    }
                                }]
                            );
                        } else {
                            throw new Error('Erro ao excluir');
                        }
                    } catch (error) {
                        showModal(
                            'Erro',
                            'Não foi possível excluir a pessoa.',
                            'fas fa-exclamation-triangle',
                            [{
                                text: 'OK',
                                class: 'modal-btn-primary',
                                action: closeModal
                            }]
                        );
                    }
                }
            }
        ]
    );
}