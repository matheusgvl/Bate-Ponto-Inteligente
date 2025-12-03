let tempFaceEncoding = null;
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

function initializeApp() {
    setupLoginForm();
    setupSidebar();
    setupPersonForm();
    setupLocalForm();
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
    const localMenuItem = document.getElementById('localMenuItem');
    const localSubmenu = document.getElementById('localSubmenu');
    const logAcessoMenuItem = document.getElementById('logAcessoMenuItem');
    const logAcessoSubmenu = document.getElementById('logAcessoSubmenu');

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

    if (pessoaMenuItem) {
        pessoaMenuItem.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();

            const isActive = pessoaSubmenu.classList.contains('active');
            pessoaSubmenu.classList.toggle('active');
            pessoaMenuItem.classList.toggle('active');

            if (localSubmenu) {
                localSubmenu.classList.remove('active');
                localMenuItem.classList.remove('active');
            }

            if (!isActive) {
                return false;
            }
        });
    }

    if (localMenuItem) {
        localMenuItem.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();

            const isActive = localSubmenu.classList.contains('active');
            localSubmenu.classList.toggle('active');
            localMenuItem.classList.toggle('active');

            if (pessoaSubmenu) {
                pessoaSubmenu.classList.remove('active');
                pessoaMenuItem.classList.remove('active');
            }

            if (logAcessoSubmenu) {
                logAcessoSubmenu.classList.remove('active');
                logAcessoMenuItem.classList.remove('active');
            }

            if (!isActive) {
                return false;
            }
        });
    }

    if (logAcessoMenuItem) {
        logAcessoMenuItem.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();

            const isActive = logAcessoSubmenu.classList.contains('active');
            logAcessoSubmenu.classList.toggle('active');
            logAcessoMenuItem.classList.toggle('active');

            if (pessoaSubmenu) {
                pessoaSubmenu.classList.remove('active');
                pessoaMenuItem.classList.remove('active');
            }

            if (localSubmenu) {
                localSubmenu.classList.remove('active');
                localMenuItem.classList.remove('active');
            }

            if (!isActive) {
                return false;
            }
        });
    }

    navItems.forEach(item => {
        if (item.id === 'pessoaMenuItem' || item.id === 'localMenuItem' || item.id === 'logAcessoMenuItem') {
            return;
        }

        item.addEventListener('click', function(e) {
            e.preventDefault();

            navItems.forEach(nav => {
                if (nav.id !== 'pessoaMenuItem' && nav.id !== 'localMenuItem' && nav.id !== 'logAcessoMenuItem') {
                    nav.classList.remove('active');
                }
            });
            navSubitems.forEach(sub => sub.classList.remove('active'));

            if (pessoaSubmenu) {
                pessoaSubmenu.classList.remove('active');
                pessoaMenuItem.classList.remove('active');
            }
            if (localSubmenu) {
                localSubmenu.classList.remove('active');
                localMenuItem.classList.remove('active');
            }
            if (logAcessoSubmenu) {
                logAcessoSubmenu.classList.remove('active');
                logAcessoMenuItem.classList.remove('active');
            }

            this.classList.add('active');

            const sectionName = this.getAttribute('data-section');
            showSection(sectionName);

            closeSidebarMenu();
        });
    });

    navSubitems.forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();

            navItems.forEach(nav => {
                if (nav.id !== 'pessoaMenuItem' && nav.id !== 'localMenuItem') {
                    nav.classList.remove('active');
                }
            });

            navSubitems.forEach(sub => sub.classList.remove('active'));

            this.classList.add('active');

            const sectionType = this.getAttribute('data-section');
            if (sectionType.includes('pessoas')) {
                pessoaMenuItem.classList.add('active');
                if (localMenuItem) localMenuItem.classList.remove('active');
            } else if (sectionType.includes('local')) {
                if (localMenuItem) localMenuItem.classList.add('active');
                pessoaMenuItem.classList.remove('active');
            }

            const sectionToShow = this.getAttribute('data-section');
            showSection(sectionToShow);

            closeSidebarMenu();
        });
    });
}

function showSection(section) {
    const dashboardMain = document.getElementById('dashboardMain');
    const cadastroPessoasPanel = document.getElementById('cadastroPessoasPanel');
    const listagemPessoasPanel = document.getElementById('listagemPessoasPanel');
    const cadastroLocalPanel = document.getElementById('cadastroLocalPanel');
    const listagemLocaisPanel = document.getElementById('listagemLocaisPanel');
    const visualizarLogsPanel = document.getElementById('visualizarLogsPanel');
    const relatorioAcessosPanel = document.getElementById('relatorioAcessosPanel');

    dashboardMain.style.display = 'none';
    if (cadastroPessoasPanel) cadastroPessoasPanel.style.display = 'none';
    if (listagemPessoasPanel) listagemPessoasPanel.style.display = 'none';
    if (cadastroLocalPanel) cadastroLocalPanel.style.display = 'none';
    if (listagemLocaisPanel) listagemLocaisPanel.style.display = 'none';
    if (visualizarLogsPanel) visualizarLogsPanel.style.display = 'none';
    if (relatorioAcessosPanel) relatorioAcessosPanel.style.display = 'none';

    if (section === 'dashboard') {
        dashboardMain.style.display = 'grid';
    } else if (section === 'cadastrar-pessoas') {
        if (cadastroPessoasPanel) cadastroPessoasPanel.style.display = 'block';
    } else if (section === 'listar-pessoas') {
        if (listagemPessoasPanel) {
            listagemPessoasPanel.style.display = 'block';
            carregarListaPessoas();
        }
    } else if (section === 'cadastrar-local') {
        if (cadastroLocalPanel) cadastroLocalPanel.style.display = 'block';
    } else if (section === 'listar-locais') {
        if (listagemLocaisPanel) {
            listagemLocaisPanel.style.display = 'block';
            carregarListaLocais();
        }
    } else if (section === 'visualizar-logs') {
        if (visualizarLogsPanel) {
            visualizarLogsPanel.style.display = 'block';
            carregarFiltrosLogs();
            carregarLogs();
        }
    } else if (section === 'relatorio-acessos') {
        if (relatorioAcessosPanel) {
            relatorioAcessosPanel.style.display = 'block';
            gerarRelatorio();
        }
    }
}

function setupPersonForm() {
    const personForm = document.getElementById('cadastroPessoaForm');
    const cpfInput = document.getElementById('cpf');
    const btnCancelar = document.getElementById('btnCancelar');
    const btnCadastrarCartao = document.getElementById('btnCadastrarCartao');
    const btnCadastrarRosto = document.getElementById('btnCadastrarRosto');

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

    if (btnCadastrarRosto) {
        btnCadastrarRosto.addEventListener('click', function() {
            const nome = document.getElementById('nome').value.trim();
            const cpf = document.getElementById('cpf').value.trim();

            if (!nome || !cpf) {
                showModal(
                    'Dados Incompletos',
                    'Por favor, preencha pelo menos o nome e CPF antes de cadastrar o rosto.',
                    'fas fa-exclamation-triangle',
                    [{
                        text: 'OK',
                        class: 'modal-btn-primary',
                        action: closeModal
                    }]
                );
                return;
            }

            startFaceCapture();
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
            const pessoaSalva = await response.json();

            if (tempFaceEncoding) {
                console.log("Salvando rosto para o ID:", pessoaSalva.id);

                await fetch('/api/person/register-face', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({
                        id: pessoaSalva.id,
                        encoding: JSON.stringify(tempFaceEncoding)
                    })
                });
            }

            showModal(
                'Sucesso!',
                `Pessoa ${personData.nome} cadastrada com sucesso!`,
                'fas fa-check-circle',
                [{
                    text: 'OK',
                    class: 'modal-btn-primary',
                    action: () => {
                        document.getElementById('cadastroPessoaForm').reset();
                        tempFaceEncoding = null;

                        const cardInput = document.getElementById('cardUID');
                        if(cardInput) cardInput.value = '';

                        closeModal();
                        showSection('dashboard');

                        if(typeof carregarEstatisticas === 'function') carregarEstatisticas();
                        if(typeof carregarListaPessoas === 'function') carregarListaPessoas();
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

function startFaceCapture() {
    const nome = document.getElementById('nome').value.trim();
    
    showModal(
        'Captura de Rosto',
        'Iniciando captura de rosto para ' + nome + '. Posicione-se em frente à câmera...',
        'fas fa-camera',
        [
            {
                text: 'Cancelar',
                class: 'modal-btn-secondary',
                action: () => {
                    stopFaceCapture();
                    closeModal();
                }
            }
        ]
    );

    const modalIcon = document.querySelector('.modal-icon i');
    modalIcon.className = 'fas fa-camera';
    modalIcon.parentElement.style.animation = 'modalPulse 1s infinite';

    captureFaceData(nome);
}

async function captureFaceData(nome) {
    try {
        tempFaceEncoding = null;

        showModal(
            'Processando...',
            'Iniciando sistema de câmera...',
            'fas fa-spinner fa-spin',
            []
        );

        const response = await fetch('/api/person/face/capture', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                nome: nome
            })
        });

        if (!response.ok) throw new Error('Falha ao iniciar captura no servidor');

        showModal(
            'Escaneando Rosto...',
            'Olhe fixamente para a câmera. Aguardando detecção...',
            'fas fa-camera',
            []
        );

        const checkInterval = setInterval(async () => {
            try {
                const statusRes = await fetch('/api/person/face/status');
                const statusData = await statusRes.json();

                if (statusData.status === 'success' && statusData.encoding) {
                    clearInterval(checkInterval);

                    tempFaceEncoding = statusData.encoding;

                    showModal(
                        'Rosto Capturado!',
                        `Rosto de ${nome} detectado com sucesso! Clique em "Salvar Pessoa" para finalizar.`,
                        'fas fa-check-circle',
                        [{
                            text: 'OK',
                            class: 'modal-btn-primary',
                            action: closeModal
                        }]
                    );
                }
                else if (statusData.status === 'error') {
                    clearInterval(checkInterval);
                    showModal('Erro', 'O sistema não conseguiu capturar o rosto.', 'fas fa-times', [{text: 'Fechar', action: closeModal}]);
                }
            } catch (e) {
                console.error("Erro no polling:", e);
            }
        }, 1000);

        setTimeout(() => {
            clearInterval(checkInterval);
            if (!tempFaceEncoding) {
                fetch('/api/person/face/stop', {method: 'POST'}).catch(() => {});

                showModal(
                    'Tempo Esgotado',
                    'Nenhum rosto detectado em 30 segundos. Tente novamente.',
                    'fas fa-clock',
                    [{text: 'Fechar', action: closeModal}]
                );
            }
        }, 30000);

    } catch (error) {
        console.error('Erro na captura de rosto:', error);
        showModal(
            'Erro na Captura',
            'Não foi possível conectar com a câmera: ' + error.message,
            'fas fa-exclamation-triangle',
            [{
                text: 'OK',
                class: 'modal-btn-primary',
                action: closeModal
            }]
        );
    }
}

function stopFaceCapture() {
    fetch('/api/person/face/stop', { method: 'POST' }).catch(() => {});
}

async function carregarEstatisticas() {
    console.log('Carregando estatísticas do dashboard...');
    try {
        const response = await fetch('/api/person/stats', {
            method: 'GET',
            cache: 'no-cache',
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
        
        carregarAreasRestritas();

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
            throw new Error(`Falha ao buscar pessoas: ${response.status}`);
        }

        const pessoas = await response.json();

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

    render(pessoas);

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
    fetch(`/api/person/${id}`)
        .then(response => response.json())
        .then(pessoa => {
            document.getElementById('editPersonId').value = pessoa.id;
            document.getElementById('editNomePessoa').value = pessoa.nome;
            document.getElementById('editCpfPessoa').value = pessoa.cpf;
            document.getElementById('editRgPessoa').value = pessoa.rg;
            document.getElementById('editDataNascimento').value = pessoa.dataNascimento;
            document.getElementById('editFuncaoPessoa').value = pessoa.funcao;
            document.getElementById('editCardUIDPessoa').value = pessoa.cardUID || '';
            
            document.getElementById('editPersonModalOverlay').classList.add('active');
        })
        .catch(error => {
            console.error('Erro ao buscar dados da pessoa:', error);
            showModal(
                'Erro',
                'Não foi possível carregar os dados da pessoa.',
                'fas fa-exclamation-triangle',
                [{
                    text: 'OK',
                    class: 'modal-btn-primary',
                    action: closeModal
                }]
            );
        });
}

function closeEditPersonModal() {
    document.getElementById('editPersonModalOverlay').classList.remove('active');
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

function setupLocalForm() {
    const localForm = document.getElementById('cadastroLocalForm');
    const btnCancelarLocal = document.getElementById('btnCancelarLocal');

    if (localForm) {
        localForm.addEventListener('submit', function(e) {
            e.preventDefault();

            const formData = new FormData(localForm);
            const localData = {};

            for (let [key, value] of formData.entries()) {
                if (key !== 'funcoesAutorizadas') {
                    localData[key] = value.trim();
                }
            }

            const funcoesCheckboxes = document.querySelectorAll('input[name="funcoesAutorizadas"]:checked');
            localData.funcoesAutorizadas = Array.from(funcoesCheckboxes).map(checkbox => checkbox.value);

            if (localData.nome && localData.descricao && localData.nivelRestricao && localData.capacidadeMaxima) {
                if (localData.funcoesAutorizadas.length === 0) {
                    showModal(
                        'Funções Não Selecionadas',
                        'Por favor, selecione pelo menos uma função autorizada para este local.',
                        'fas fa-exclamation-triangle',
                        [{
                            text: 'OK',
                            class: 'modal-btn-primary',
                            action: closeModal
                        }]
                    );
                } else {
                    saveLocalData(localData);
                }
            } else {
                showModal(
                    'Dados Incompletos',
                    'Por favor, preencha todos os campos obrigatórios.',
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

    if (btnCancelarLocal) {
        btnCancelarLocal.addEventListener('click', function() {
            showModal(
                'Cancelar Cadastro',
                'Deseja cancelar o cadastro? Todos os dados serão perdidos.',
                'fas fa-exclamation-triangle',
                [
                    {
                        text: 'Continuar Editando',
                        class: 'modal-btn-secondary',
                        action: closeModal
                    },
                    {
                        text: 'Cancelar Cadastro',
                        class: 'modal-btn-primary',
                        action: () => {
                            document.getElementById('cadastroLocalForm').reset();
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
}

async function saveLocalData(localData) {
    try {
        const response = await fetch('/api/local', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(localData)
        });

        if (response.ok) {
            const result = await response.json();
            showModal(
                'Sucesso!',
                `Local "${localData.nome}" cadastrado com sucesso!`,
                'fas fa-check-circle',
                [{
                    text: 'OK',
                    class: 'modal-btn-primary',
                    action: () => {
                        document.getElementById('cadastroLocalForm').reset();
                        closeModal();
                        showSection('dashboard');
                        document.querySelectorAll('.nav-item').forEach(nav => nav.classList.remove('active'));
                        document.querySelector('.nav-item[data-section="dashboard"]').classList.add('active');
                    }
                }]
            );
        } else {
            const error = await response.json();
            showModal(
                'Erro ao Salvar',
                error.message || 'Erro ao salvar o local.',
                'fas fa-exclamation-triangle',
                [{
                    text: 'OK',
                    class: 'modal-btn-primary',
                    action: closeModal
                }]
            );
        }
    } catch (error) {
        console.error('Erro ao salvar local:', error);
        showModal(
            'Erro de Conexão',
            'Erro de conexão com o servidor.',
            'fas fa-exclamation-triangle',
            [{
                text: 'OK',
                class: 'modal-btn-primary',
                action: closeModal
            }]
        );
    }
}

async function carregarListaLocais() {
    const locaisGrid = document.getElementById('locaisGrid');

    locaisGrid.innerHTML = `
        <div class="loading-state">
            <i class="fas fa-spinner fa-spin"></i>
            <p>Carregando locais...</p>
        </div>
    `;

    try {
        const response = await fetch('/api/local/list', {
            method: 'GET',
            cache: 'no-cache',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error(`Falha ao buscar locais: ${response.status}`);
        }

        const locais = await response.json();

        if (locais.length === 0) {
            locaisGrid.innerHTML = `
                <div class="empty-state-list">
                    <i class="fas fa-building"></i>
                    <h4>Nenhum local cadastrado</h4>
                    <p>Cadastre o primeiro local no sistema</p>
                </div>
            `;
            return;
        }

        renderizarLocais(locais);

    } catch (error) {
        console.error('Erro ao carregar locais:', error);
        locaisGrid.innerHTML = `
            <div class="empty-state-list">
                <i class="fas fa-exclamation-triangle"></i>
                <h4>Erro ao carregar locais</h4>
                <p>Não foi possível conectar ao servidor</p>
            </div>
        `;
    }
}

function renderizarLocais(locais) {
    const locaisGrid = document.getElementById('locaisGrid');
    const searchInput = document.getElementById('searchLocal');

    function render(listaLocais) {
        locaisGrid.innerHTML = '';

        listaLocais.forEach(local => {
            const dataFormatada = new Date(local.dataCadastro).toLocaleDateString('pt-BR');
            const nivelCor = {
                'BAIXO': '#48bb78',
                'MEDIO': '#ed8936', 
                'ALTO': '#f56565'
            }[local.nivelRestricao] || '#667eea';
            
            const funcoesFormatadas = (local.funcoesAutorizadas || []).map(funcao => {
                return funcao.charAt(0).toUpperCase() + funcao.slice(1).toLowerCase();
            }).join(', ') || 'Nenhuma função definida';
            
            const localCard = document.createElement('div');
            localCard.className = 'local-card';
            localCard.innerHTML = `
                <div class="local-header">
                    <div class="local-icon">
                        <i class="fas fa-building"></i>
                    </div>
                    <div class="local-info">
                        <h3>${local.nome}</h3>
                        <div class="local-status">
                            <i class="fas fa-circle"></i>
                            ${local.ativo ? 'Ativo' : 'Inativo'}
                        </div>
                    </div>
                </div>
                <div class="local-description">
                    ${local.descricao}
                </div>
                <div class="local-details">
                    <div class="local-detail-item">
                        <span class="local-label">Nível de Restrição:</span>
                        <span class="local-badge" style="background-color: ${nivelCor}20; border-color: ${nivelCor}40; color: ${nivelCor}">
                            ${local.nivelRestricao}
                        </span>
                    </div>
                    <div class="local-detail-item">
                        <span class="local-label">Capacidade:</span>
                        <span class="local-value">${local.capacidadeMaxima} pessoas</span>
                    </div>
                    <div class="local-detail-item">
                        <span class="local-label">Funções Autorizadas:</span>
                        <span class="local-value funcoes-autorizadas">${funcoesFormatadas}</span>
                    </div>
                </div>
                <div class="local-meta">
                    <div class="local-date">
                        <i class="fas fa-calendar"></i>
                        ${dataFormatada}
                    </div>
                    <div class="local-actions">
                        <button class="local-action-btn edit" onclick="editarLocal(${local.id})" title="Editar">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="local-action-btn delete" onclick="excluirLocal(${local.id}, '${local.nome}')" title="Excluir">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </div>
            `;

            locaisGrid.appendChild(localCard);
        });
    }

    render(locais);

    if (searchInput) {
        searchInput.addEventListener('input', function() {
            const termo = this.value.toLowerCase();
            const locaisFiltrados = locais.filter(local =>
                local.nome.toLowerCase().includes(termo) ||
                local.descricao.toLowerCase().includes(termo)
            );
            render(locaisFiltrados);
        });
    }
}

function editarLocal(id) {
    fetch(`/api/local/${id}`)
        .then(response => response.json())
        .then(local => {
            document.getElementById('editLocalId').value = local.id;
            document.getElementById('editNomeLocal').value = local.nome;
            document.getElementById('editDescricaoLocal').value = local.descricao;
            document.getElementById('editNivelRestricao').value = local.nivelRestricao;
            document.getElementById('editCapacidadeMaxima').value = local.capacidadeMaxima;
            
            document.querySelectorAll('#editFuncoesAutorizadas input[type="checkbox"]').forEach(checkbox => {
                checkbox.checked = false;
            });
            
            if (local.funcoesAutorizadas && local.funcoesAutorizadas.length > 0) {
                local.funcoesAutorizadas.forEach(funcao => {
                    const checkbox = document.getElementById(`edit_funcao_${funcao}`);
                    if (checkbox) {
                        checkbox.checked = true;
                    }
                });
            }
            
            document.getElementById('editLocalModalOverlay').classList.add('active');
        })
        .catch(error => {
            console.error('Erro ao buscar dados do local:', error);
            showModal(
                'Erro',
                'Não foi possível carregar os dados do local.',
                'fas fa-exclamation-triangle',
                [{
                    text: 'OK',
                    class: 'modal-btn-primary',
                    action: closeModal
                }]
            );
        });
}

function closeEditLocalModal() {
    document.getElementById('editLocalModalOverlay').classList.remove('active');
}

document.addEventListener('DOMContentLoaded', function() {
    const editLocalForm = document.getElementById('editLocalForm');
    if (editLocalForm) {
        editLocalForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const formData = new FormData(editLocalForm);
            const localData = {};
            
            for (let [key, value] of formData.entries()) {
                if (key !== 'funcoesAutorizadas') {
                    localData[key] = value.trim();
                }
            }
            
            const funcoesCheckboxes = document.querySelectorAll('#editFuncoesAutorizadas input[name="funcoesAutorizadas"]:checked');
            localData.funcoesAutorizadas = Array.from(funcoesCheckboxes).map(checkbox => checkbox.value);
            
            if (localData.nome && localData.descricao && localData.nivelRestricao && localData.capacidadeMaxima) {
                if (localData.funcoesAutorizadas.length === 0) {
                    showModal(
                        'Funções Não Selecionadas',
                        'Por favor, selecione pelo menos uma função autorizada para este local.',
                        'fas fa-exclamation-triangle',
                        [{
                            text: 'OK',
                            class: 'modal-btn-primary',
                            action: closeModal
                        }]
                    );
                } else {
                    updateLocalData(localData);
                }
            } else {
                showModal(
                    'Dados Incompletos',
                    'Por favor, preencha todos os campos obrigatórios.',
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
    
    const editPersonForm = document.getElementById('editPersonForm');
    if (editPersonForm) {
        const editCpfInput = document.getElementById('editCpfPessoa');
        if (editCpfInput) {
            editCpfInput.addEventListener('input', function(e) {
                let value = e.target.value.replace(/\D/g, '');
                if (value.length <= 11) {
                    value = value.replace(/(\d{3})(\d)/, '$1.$2');
                    value = value.replace(/(\d{3})(\d)/, '$1.$2');
                    value = value.replace(/(\d{3})(\d{1,2})$/, '$1-$2');
                }
                e.target.value = value;
            });
        }
        
        editPersonForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const formData = new FormData(editPersonForm);
            const personData = {};
            
            for (let [key, value] of formData.entries()) {
                personData[key] = value.trim();
            }
            
            if (personData.nome && personData.rg && personData.dataNascimento && personData.funcao) {
                if (!validateCPF(personData.cpf)) {
                    showModal(
                        'CPF Inválido',
                        'CPF inválido detectado.',
                        'fas fa-exclamation-triangle',
                        [{
                            text: 'OK',
                            class: 'modal-btn-primary',
                            action: closeModal
                        }]
                    );
                } else {
                    updatePersonData(personData);
                }
            } else {
                showModal(
                    'Dados Incompletos',
                    'Por favor, preencha todos os campos obrigatórios.',
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
    
    const btnAlterarCartao = document.getElementById('btnAlterarCartao');
    if (btnAlterarCartao) {
        btnAlterarCartao.addEventListener('click', function() {
            const personId = document.getElementById('editPersonId').value;
            if (personId) {
                startCardReadingForEdit(personId);
            }
        });
    }
});

async function updateLocalData(localData) {
    try {
        const response = await fetch(`/api/local/${localData.id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(localData)
        });

        if (response.ok) {
            showModal(
                'Sucesso!',
                `Local "${localData.nome}" foi atualizado com sucesso!`,
                'fas fa-check-circle',
                [{
                    text: 'OK',
                    class: 'modal-btn-primary',
                    action: () => {
                        closeEditLocalModal();
                        closeModal();
                        carregarListaLocais();
                    }
                }]
            );
        } else {
            const error = await response.json();
            showModal(
                'Erro ao Atualizar',
                error.message || 'Erro ao atualizar o local.',
                'fas fa-exclamation-triangle',
                [{
                    text: 'OK',
                    class: 'modal-btn-primary',
                    action: closeModal
                }]
            );
        }
    } catch (error) {
        console.error('Erro ao atualizar local:', error);
        showModal(
            'Erro de Conexão',
            'Erro de conexão com o servidor.',
            'fas fa-exclamation-triangle',
            [{
                text: 'OK',
                class: 'modal-btn-primary',
                action: closeModal
            }]
        );
    }
}

async function updatePersonData(personData) {
    try {
        const response = await fetch(`/api/person/${personData.id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(personData)
        });

        if (response.ok) {
            showModal(
                'Sucesso!',
                `Dados de "${personData.nome}" foram atualizados com sucesso!`,
                'fas fa-check-circle',
                [{
                    text: 'OK',
                    class: 'modal-btn-primary',
                    action: () => {
                        closeEditPersonModal();
                        closeModal();
                        carregarListaPessoas();
                        carregarEstatisticas();
                    }
                }]
            );
        } else {
            const error = await response.json();
            showModal(
                'Erro ao Atualizar',
                error.message || 'Erro ao atualizar a pessoa.',
                'fas fa-exclamation-triangle',
                [{
                    text: 'OK',
                    class: 'modal-btn-primary',
                    action: closeModal
                }]
            );
        }
    } catch (error) {
        console.error('Erro ao atualizar pessoa:', error);
        showModal(
            'Erro de Conexão',
            'Erro de conexão com o servidor.',
            'fas fa-exclamation-triangle',
            [{
                text: 'OK',
                class: 'modal-btn-primary',
                action: closeModal
            }]
        );
    }
}

function startCardReadingForEdit(personId) {
    showModal(
        'Aguardando Novo Cartão NFC',
        'Aproxime o novo cartão do leitor para alterar...',
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

    readCardUIDForEdit(personId);
}

async function readCardUIDForEdit(personId) {
    try {
        console.log('Iniciando leitura do novo cartão NFC...');

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
                console.log('Novo cartão detectado com sucesso:', data.uid);
                handleNewCardDetected(data.uid, personId);
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

function handleNewCardDetected(uid, personId) {
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
                    action: () => startCardReadingForEdit(personId)
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
        'Novo Cartão Detectado!',
        `UID do novo cartão: ${uid}\n\nDeseja alterar para este cartão?`,
        'fas fa-check-circle',
        [
            {
                text: 'Não, tentar outro',
                class: 'modal-btn-secondary',
                action: () => startCardReadingForEdit(personId)
            },
            {
                text: 'Sim, alterar',
                class: 'modal-btn-primary',
                action: () => {
                    document.getElementById('editCardUIDPessoa').value = uid;
                    closeModal();
                }
            }
        ]
    );
}

function excluirLocal(id, nome) {
    showModal(
        'Confirmar Exclusão',
        `Tem certeza que deseja excluir o local "${nome}"? Esta ação não pode ser desfeita.`,
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
                        const response = await fetch(`/api/local/${id}`, {
                            method: 'DELETE'
                        });

                        if (response.ok) {
                            showModal(
                                'Sucesso',
                                `Local "${nome}" foi excluído com sucesso.`,
                                'fas fa-check-circle',
                                [{
                                    text: 'OK',
                                    class: 'modal-btn-primary',
                                    action: () => {
                                        closeModal();
                                        carregarListaLocais();
                                    }
                                }]
                            );
                        } else {
                            throw new Error('Erro ao excluir');
                        }
                    } catch (error) {
                        showModal(
                            'Erro',
                            'Não foi possível excluir o local.',
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

async function carregarFiltrosLogs() {
    carregarPessoasParaFiltro();
    carregarLocaisParaFiltro();
}

async function carregarPessoasParaFiltro() {
    try {
        const response = await fetch('/api/pessoa/list');
        const pessoas = await response.json();
        
        const select = document.getElementById('filtroPessoa');
        select.innerHTML = '<option value="">Todas as pessoas</option>';
        
        pessoas.forEach(pessoa => {
            const option = document.createElement('option');
            option.value = pessoa.id;
            option.textContent = pessoa.nome;
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Erro ao carregar pessoas para filtro:', error);
    }
}

async function carregarLocaisParaFiltro() {
    try {
        const response = await fetch('/api/local/list');
        const locais = await response.json();
        
        const select = document.getElementById('filtroLocal');
        select.innerHTML = '<option value="">Todos os locais</option>';
        
        locais.forEach(local => {
            const option = document.createElement('option');
            option.value = local.id;
            option.textContent = local.nome;
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Erro ao carregar locais para filtro:', error);
    }
}

async function carregarLogs(page = 0, size = 10) {
    try {
        const params = new URLSearchParams();
        params.append('page', page);
        params.append('size', size);
        
        const data = document.getElementById('filtroData').value;
        const pessoaId = document.getElementById('filtroPessoa').value;
        const localId = document.getElementById('filtroLocal').value;
        const tipoAcesso = document.getElementById('filtroTipoAcesso').value;
        
        if (data) params.append('data', data);
        if (pessoaId) params.append('pessoaId', pessoaId);
        if (localId) params.append('localId', localId);
        if (tipoAcesso) params.append('tipoAcesso', tipoAcesso);
        
        currentPage = page;
        currentSize = size;
        currentFilters = { data, pessoaId, localId, tipoAcesso };
        
        const response = await fetch(`/api/acesso/logs?${params}`);
        const result = await response.json();
        
        exibirLogs(result.content);
        atualizarPaginacao(result);
        
    } catch (error) {
        console.error('Erro ao carregar logs:', error);
        const tbody = document.getElementById('logsTableBody');
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="error-message">
                    <i class="fas fa-exclamation-triangle"></i> 
                    Erro ao carregar logs: ${error.message}
                </td>
            </tr>
        `;
    }
}

function exibirLogs(logs) {
    const tbody = document.getElementById('logsTableBody');
    
    if (logs.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="empty-state">
                    <i class="fas fa-inbox"></i>
                    <p>Nenhum log encontrado com os filtros aplicados</p>
                </td>
            </tr>
        `;
        return;
    }
    
    tbody.innerHTML = logs.map(log => {
        const dataFormatada = new Date(log.dataHora).toLocaleString('pt-BR');
        const statusClass = log.tipoAcesso.startsWith('PERMITIDO') ? 'status-permitido' : 'status-negado';
        const statusText = log.tipoAcesso.startsWith('PERMITIDO') ? 'Permitido' : 'Negado';
        const statusIcon = log.tipoAcesso.startsWith('PERMITIDO') ? 'fas fa-check-circle' : 'fas fa-times-circle';
        
        return `
            <tr>
                <td>${dataFormatada}</td>
                <td>
                    <div class="pessoa-info">
                        <i class="fas fa-user"></i>
                        <span>${log.nomePessoa}</span>
                    </div>
                </td>
                <td>
                    <div class="local-info">
                        <i class="fas fa-building"></i>
                        <span>${log.nomeLocal}</span>
                    </div>
                </td>
                <td>
                    <span class="tipo-acesso">${log.tipoAcesso.replace(/_/g, ' ')}</span>
                </td>
                <td>
                    <span class="status ${statusClass}">
                        <i class="${statusIcon}"></i>
                        ${statusText}
                    </span>
                </td>
                <td>
                    <button class="btn-small btn-secondary" onclick="verDetalhesLog(${log.id})">
                        <i class="fas fa-eye"></i> Ver Detalhes
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

function atualizarPaginacao(result) {
    const info = document.getElementById('paginationInfo');
    const btnPrev = document.getElementById('btnPrevPage');
    const btnNext = document.getElementById('btnNextPage');
    const pageNumbers = document.getElementById('pageNumbers');
    
    const total = result.totalElements;
    const inicio = (result.number * result.size) + 1;
    const fim = Math.min((result.number + 1) * result.size, total);
    
    info.textContent = `Mostrando ${inicio} a ${fim} de ${total} registros`;
    
    btnPrev.disabled = result.first;
    btnNext.disabled = result.last;
    
    const currentPage = result.number + 1;
    const totalPages = result.totalPages;
    pageNumbers.textContent = `${currentPage} de ${totalPages}`;
}

function paginaAnterior() {
    if (currentPage > 0) {
        carregarLogs(currentPage - 1, currentSize);
    }
}

function proximaPagina() {
    carregarLogs(currentPage + 1, currentSize);
}

function limparFiltros() {
    document.getElementById('filtroData').value = '';
    document.getElementById('filtroPessoa').value = '';
    document.getElementById('filtroLocal').value = '';
    document.getElementById('filtroTipoAcesso').value = '';
    carregarLogs(0, 10);
}

function verDetalhesLog(logId) {
    showModal(
        'Detalhes do Log',
        'Funcionalidade de detalhes será implementada em breve.',
        'fas fa-info-circle',
        [{
            text: 'OK',
            class: 'modal-btn-primary',
            action: closeModal
        }]
    );
}

async function gerarRelatorio() {
    try {
        const periodo = document.getElementById('periodoRelatorio').value;
        const params = new URLSearchParams();
        params.append('periodo', periodo);
        
        if (periodo === 'personalizado') {
            const dataInicio = document.getElementById('dataInicio').value;
            const dataFim = document.getElementById('dataFim').value;
            
            if (dataInicio && dataFim) {
                params.append('dataInicio', dataInicio);
                params.append('dataFim', dataFim);
            }
        }
        
        const response = await fetch(`/api/acesso/relatorio?${params}`);
        const relatorio = await response.json();
        
        exibirRelatorio(relatorio);
        
    } catch (error) {
        console.error('Erro ao gerar relatório:', error);
        showModal(
            'Erro',
            'Não foi possível gerar o relatório: ' + error.message,
            'fas fa-exclamation-triangle',
            [{
                text: 'OK',
                class: 'modal-btn-primary',
                action: closeModal
            }]
        );
    }
}

function exibirRelatorio(relatorio) {
    document.getElementById('totalAcessosPermitidos').textContent = relatorio.acessosPermitidos || 0;
    document.getElementById('totalAcessosNegados').textContent = relatorio.acessosNegados || 0;
    document.getElementById('totalPessoasUnicas').textContent = relatorio.pessoasUnicas || 0;
    document.getElementById('totalLocaisAcessados').textContent = relatorio.locaisAcessados || 0;
    
    exibirTopPessoas(relatorio.topPessoas || {});
    
    exibirTopLocais(relatorio.topLocais || {});
}

function exibirTopPessoas(topPessoas) {
    const container = document.getElementById('topPessoas');
    
    if (Object.keys(topPessoas).length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-user-slash"></i>
                <p>Nenhum dado disponível</p>
            </div>
        `;
        return;
    }
    
    container.innerHTML = Object.entries(topPessoas).map(([nome, count], index) => `
        <div class="top-item">
            <div class="ranking">${index + 1}º</div>
            <div class="info">
                <div class="name">${nome}</div>
                <div class="count">${count} acessos</div>
            </div>
        </div>
    `).join('');
}

function exibirTopLocais(topLocais) {
    const container = document.getElementById('topLocais');
    
    if (Object.keys(topLocais).length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-building-slash"></i>
                <p>Nenhum dado disponível</p>
            </div>
        `;
        return;
    }
    
    container.innerHTML = Object.entries(topLocais).map(([nome, count], index) => `
        <div class="top-item">
            <div class="ranking">${index + 1}º</div>
            <div class="info">
                <div class="name">${nome}</div>
                <div class="count">${count} acessos</div>
            </div>
        </div>
    `).join('');
}

document.addEventListener('DOMContentLoaded', function() {
    const periodoSelect = document.getElementById('periodoRelatorio');
    const periodoPersonalizado = document.getElementById('periodoPersonalizado');
    
    if (periodoSelect) {
        periodoSelect.addEventListener('change', function() {
            if (this.value === 'personalizado') {
                periodoPersonalizado.style.display = 'flex';
                periodoPersonalizado.classList.add('show');
                
                setTimeout(() => {
                    periodoPersonalizado.style.opacity = '1';
                    periodoPersonalizado.style.transform = 'translateY(0)';
                }, 10);
            } else {
                periodoPersonalizado.style.opacity = '0';
                periodoPersonalizado.style.transform = 'translateY(-10px)';
                periodoPersonalizado.classList.remove('show');
                
                setTimeout(() => {
                    periodoPersonalizado.style.display = 'none';
                }, 200);
            }
        });
    }

    carregarAreasRestritas();
});

async function carregarAreasRestritas() {
    try {
        const response = await fetch('/api/local/list', {
            method: 'GET',
            cache: 'no-cache',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`Falha ao buscar áreas: ${response.status}`);
        }

        const locais = await response.json();
        renderizarAreasRestritas(locais);

    } catch (error) {
        console.error('Erro ao carregar áreas restritas:', error);
        renderizarAreasRestritas([]);
    }
}

function renderizarAreasRestritas(locais) {
    const areasSection = document.querySelector('.areas-section');
    const sectionSubtitle = areasSection.querySelector('.section-subtitle');
    
    // Atualizar contador
    sectionSubtitle.textContent = `${locais.length} áreas monitoradas`;
    
    // Remover estado vazio ou lista existente
    const existingContent = areasSection.querySelector('.empty-state, .areas-list');
    if (existingContent) {
        existingContent.remove();
    }
    
    if (locais.length === 0) {
        const emptyState = document.createElement('div');
        emptyState.className = 'empty-state';
        emptyState.innerHTML = `
            <i class="fas fa-inbox"></i>
            <h4>Nenhuma área configurada</h4>
            <p>Sistema novo sem dados cadastrados</p>
        `;
        areasSection.appendChild(emptyState);
        return;
    }
    
    // Criar lista de áreas
    const areasList = document.createElement('div');
    areasList.className = 'areas-list';
    
    locais.forEach(local => {
        const nivelCor = {
            'BAIXO': '#48bb78',
            'MEDIO': '#ed8936', 
            'ALTO': '#f56565'
        };
        
        const areaItem = document.createElement('div');
        areaItem.className = 'area-item';
        areaItem.innerHTML = `
            <div class="area-info">
                <div class="area-name">${local.nome}</div>
                <div class="area-details">
                    <span class="area-restriction" style="color: ${nivelCor[local.nivelRestricao]}">
                        ${local.nivelRestricao}
                    </span>
                    <span class="area-capacity">${local.capacidadeMaxima} pessoas</span>
                </div>
            </div>
            <div class="area-status">
                <span class="status-active">Monitorando</span>
            </div>
        `;
        areasList.appendChild(areaItem);
    });
    
    areasSection.appendChild(areasList);
}