/**
 * SISTEMA DE CONTROLE DE ACESSO — ENTERPRISE SECURITY CONTROLLER
 * Interface corporativa baseada em IBM Carbon / Fluent UI / Cybersecurity Dashboard
 */

let tempFaceEncoding = null;
let dashboardUpdateInterval = null;
let cachedPessoas = [];
let cachedLocais = [];
let sortPessoasField = 'id';
let sortPessoasAsc = true;

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

/* --------------------------------------------------------------------------
   1. ATUALIZAÇÃO AUTOMÁTICA DO DASHBOARD
   -------------------------------------------------------------------------- */
function iniciarAtualizacaoDashboard() {
    if (dashboardUpdateInterval) {
        clearInterval(dashboardUpdateInterval);
    }
    
    carregarEstatisticas();
    dashboardUpdateInterval = setInterval(() => {
        carregarEstatisticas();
    }, 5000);
}

function pararAtualizacaoDashboard() {
    if (dashboardUpdateInterval) {
        clearInterval(dashboardUpdateInterval);
        dashboardUpdateInterval = null;
    }
}

/* --------------------------------------------------------------------------
   2. AUTENTICAÇÃO E LOGIN
   -------------------------------------------------------------------------- */
function setupLoginForm() {
    const loginForm = document.getElementById('loginForm');
    const loginContainer = document.getElementById('loginContainer');
    const dashboardContainer = document.getElementById('dashboardContainer');
    const errorMessage = document.getElementById('errorMessage');
    const errorText = document.getElementById('errorText');
    const togglePasswordBtn = document.getElementById('togglePasswordBtn');
    const passwordInput = document.getElementById('password');
    const passwordToggleIcon = document.getElementById('passwordToggleIcon');

    // Alternar visibilidade de senha (mostrar/ocultar)
    if (togglePasswordBtn && passwordInput && passwordToggleIcon) {
        togglePasswordBtn.addEventListener('click', function(e) {
            e.preventDefault();
            if (passwordInput.type === 'password') {
                passwordInput.type = 'text';
                passwordToggleIcon.className = 'fas fa-eye-slash';
            } else {
                passwordInput.type = 'password';
                passwordToggleIcon.className = 'fas fa-eye';
            }
        });
    }

    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();

            const username = document.getElementById('username').value.trim();
            const password = document.getElementById('password').value.trim();

            if (username === 'admin' && password === 'admin') {
                loginContainer.style.display = 'none';
                dashboardContainer.style.display = 'flex';
                document.getElementById('sidebarUserName').textContent = 'Admin';
                const supervisorName = document.getElementById('supervisorName');
                if (supervisorName) supervisorName.textContent = 'Admin';
                errorMessage.style.display = 'none';
                iniciarAtualizacaoDashboard();
                showSection('dashboard');
            } else {
                if (errorText) errorText.textContent = 'Credenciais incorretas! Verifique usuário e senha.';
                errorMessage.style.display = 'flex';
            }
        });
    }
}

function logout() {
    pararAtualizacaoDashboard();
    const loginContainer = document.getElementById('loginContainer');
    const dashboardContainer = document.getElementById('dashboardContainer');

    dashboardContainer.style.display = 'none';
    loginContainer.style.display = 'flex';

    const loginForm = document.getElementById('loginForm');
    if (loginForm) loginForm.reset();

    const passwordInput = document.getElementById('password');
    const passwordToggleIcon = document.getElementById('passwordToggleIcon');
    if (passwordInput && passwordToggleIcon) {
        passwordInput.type = 'password';
        passwordToggleIcon.className = 'fas fa-eye';
    }

    const errorMessage = document.getElementById('errorMessage');
    if (errorMessage) errorMessage.style.display = 'none';
}

/* --------------------------------------------------------------------------
   3. NAVEGAÇÃO E SIDEBAR
   -------------------------------------------------------------------------- */
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

    if (menuToggle && sidebar && sidebarOverlay) {
        menuToggle.addEventListener('click', function() {
            sidebar.classList.toggle('active');
            sidebarOverlay.classList.toggle('active');
        });
    }

    function closeSidebarMenu() {
        if (sidebar) sidebar.classList.remove('active');
        if (sidebarOverlay) sidebarOverlay.classList.remove('active');
    }

    if (closeSidebar) closeSidebar.addEventListener('click', closeSidebarMenu);
    if (sidebarOverlay) sidebarOverlay.addEventListener('click', closeSidebarMenu);

    // Dropdown Pessoas
    if (pessoaMenuItem && pessoaSubmenu) {
        pessoaMenuItem.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            pessoaSubmenu.classList.toggle('active');
            pessoaMenuItem.classList.toggle('submenu-open');

            if (localSubmenu) {
                localSubmenu.classList.remove('active');
                localMenuItem.classList.remove('submenu-open');
            }
            if (logAcessoSubmenu) {
                logAcessoSubmenu.classList.remove('active');
                logAcessoMenuItem.classList.remove('submenu-open');
            }
        });
    }

    // Dropdown Locais
    if (localMenuItem && localSubmenu) {
        localMenuItem.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            localSubmenu.classList.toggle('active');
            localMenuItem.classList.toggle('submenu-open');

            if (pessoaSubmenu) {
                pessoaSubmenu.classList.remove('active');
                pessoaMenuItem.classList.remove('submenu-open');
            }
            if (logAcessoSubmenu) {
                logAcessoSubmenu.classList.remove('active');
                logAcessoMenuItem.classList.remove('submenu-open');
            }
        });
    }

    // Dropdown Logs de Acesso
    if (logAcessoMenuItem && logAcessoSubmenu) {
        logAcessoMenuItem.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            logAcessoSubmenu.classList.toggle('active');
            logAcessoMenuItem.classList.toggle('submenu-open');

            if (pessoaSubmenu) {
                pessoaSubmenu.classList.remove('active');
                pessoaMenuItem.classList.remove('submenu-open');
            }
            if (localSubmenu) {
                localSubmenu.classList.remove('active');
                localMenuItem.classList.remove('submenu-open');
            }
        });
    }

    // Cliques em itens principais (fora de dropdowns)
    navItems.forEach(item => {
        if (item.id === 'pessoaMenuItem' || item.id === 'localMenuItem' || item.id === 'logAcessoMenuItem') {
            return;
        }

        item.addEventListener('click', function(e) {
            const sectionName = this.getAttribute('data-section');
            if (!sectionName) return;

            e.preventDefault();

            navItems.forEach(nav => {
                if (nav.id !== 'pessoaMenuItem' && nav.id !== 'localMenuItem' && nav.id !== 'logAcessoMenuItem') {
                    nav.classList.remove('active');
                }
            });
            navSubitems.forEach(sub => sub.classList.remove('active'));

            this.classList.add('active');
            showSection(sectionName);
            closeSidebarMenu();
        });
    });

    // Cliques em subitens
    navSubitems.forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            const section = this.getAttribute('data-section');

            navItems.forEach(nav => {
                if (nav.id !== 'pessoaMenuItem' && nav.id !== 'localMenuItem' && nav.id !== 'logAcessoMenuItem') {
                    nav.classList.remove('active');
                }
            });
            navSubitems.forEach(sub => sub.classList.remove('active'));

            this.classList.add('active');

            if (section.includes('pessoas') && pessoaMenuItem) {
                pessoaMenuItem.classList.add('active');
            } else if (section.includes('local') && localMenuItem) {
                localMenuItem.classList.add('active');
            } else if (section.includes('logs') || section.includes('relatorio')) {
                if (logAcessoMenuItem) logAcessoMenuItem.classList.add('active');
            }

            showSection(section);
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
    const headerTitle = document.getElementById('headerPageTitle');
    const headerSubtitle = document.getElementById('headerPageSubtitle');

    if (dashboardMain) dashboardMain.style.display = 'none';
    if (cadastroPessoasPanel) cadastroPessoasPanel.style.display = 'none';
    if (listagemPessoasPanel) listagemPessoasPanel.style.display = 'none';
    if (cadastroLocalPanel) cadastroLocalPanel.style.display = 'none';
    if (listagemLocaisPanel) listagemLocaisPanel.style.display = 'none';
    if (visualizarLogsPanel) visualizarLogsPanel.style.display = 'none';
    if (relatorioAcessosPanel) relatorioAcessosPanel.style.display = 'none';

    // Atualiza título do Header
    const sectionHeaders = {
        'dashboard': {
            title: '<i class="fas fa-shield-halved"></i> <span>Controle de Acesso</span>',
            subtitle: 'Sistema de gestão e monitoramento de acessos'
        },
        'listar-pessoas': {
            title: '<i class="fas fa-users-gear"></i> <span>Gestão de Pessoas</span>',
            subtitle: 'Cadastre, edite e gerencie os usuários e permissões do sistema'
        },
        'cadastrar-pessoas': {
            title: '<i class="fas fa-user-plus"></i> <span>Cadastrar Nova Pessoa</span>',
            subtitle: 'Preencha os dados cadastrais, dados biométricos e permissões de acesso'
        },
        'listar-locais': {
            title: '<i class="fas fa-layer-group"></i> <span>Locais e Áreas Monitoradas</span>',
            subtitle: 'Gerencie ambientes físicos, capacidades máximas e níveis de segurança'
        },
        'cadastrar-local': {
            title: '<i class="fas fa-square-plus"></i> <span>Cadastrar Novo Local</span>',
            subtitle: 'Configure novas portas, catracas ou salas com restrição de acesso'
        },
        'visualizar-logs': {
            title: '<i class="fas fa-table-list"></i> <span>Visualizar Logs de Acesso</span>',
            subtitle: 'Histórico consolidado e imutável de todas as tentativas de acesso registradas'
        },
        'relatorio-acessos': {
            title: '<i class="fas fa-chart-pie"></i> <span>Relatório Analítico de Acessos</span>',
            subtitle: 'Análise estatística de fluxo, ocupação e conformidade de acessos'
        }
    };

    if (sectionHeaders[section] && headerTitle && headerSubtitle) {
        headerTitle.innerHTML = sectionHeaders[section].title;
        headerSubtitle.textContent = sectionHeaders[section].subtitle;
    }

    if (section === 'dashboard') {
        if (dashboardMain) dashboardMain.style.display = 'flex';
        iniciarAtualizacaoDashboard();
    } else {
        pararAtualizacaoDashboard();

        if (section === 'cadastrar-pessoas' && cadastroPessoasPanel) {
            cadastroPessoasPanel.style.display = 'block';
        } else if (section === 'listar-pessoas' && listagemPessoasPanel) {
            listagemPessoasPanel.style.display = 'block';
            carregarListaPessoas();
        } else if (section === 'cadastrar-local' && cadastroLocalPanel) {
            cadastroLocalPanel.style.display = 'block';
        } else if (section === 'listar-locais' && listagemLocaisPanel) {
            listagemLocaisPanel.style.display = 'block';
            carregarListaLocais();
        } else if (section === 'visualizar-logs' && visualizarLogsPanel) {
            visualizarLogsPanel.style.display = 'block';
            carregarFiltrosLogs();
            carregarLogs();
        } else if (section === 'relatorio-acessos' && relatorioAcessosPanel) {
            relatorioAcessosPanel.style.display = 'block';
            gerarRelatorio();
        }
    }
}

/* --------------------------------------------------------------------------
   4. MODAIS E UTILITÁRIOS
   -------------------------------------------------------------------------- */
function setupModal() {
    const modalOverlay = document.getElementById('modalOverlay');
    if (modalOverlay) {
        modalOverlay.addEventListener('click', function(e) {
            if (e.target === modalOverlay) {
                closeModal();
            }
        });
    }
}

function showModal(title, message, icon, buttons) {
    const modalOverlay = document.getElementById('modalOverlay');
    const modalTitle = document.getElementById('modalTitle');
    const modalMessage = document.getElementById('modalMessage');
    const modalIcon = document.querySelector('.modal-icon i');
    const modalActions = document.getElementById('modalActions');

    if (!modalOverlay) return;

    if (modalTitle) modalTitle.textContent = title;
    if (modalMessage) modalMessage.textContent = message;
    if (modalIcon) modalIcon.className = icon || 'fas fa-circle-exclamation';

    if (modalActions) {
        modalActions.innerHTML = '';
        (buttons || []).forEach(button => {
            const btn = document.createElement('button');
            btn.className = `modal-btn ${button.class || 'modal-btn-secondary'}`;
            btn.innerHTML = `<i class="${button.icon || 'fas fa-check'}"></i> ${button.text}`;
            btn.addEventListener('click', button.action || closeModal);
            modalActions.appendChild(btn);
        });
    }

    modalOverlay.classList.add('active');
}

function closeModal() {
    const modalOverlay = document.getElementById('modalOverlay');
    if (modalOverlay) modalOverlay.classList.remove('active');
}

function updateTime() {
    const timeElement = document.getElementById('currentTime');
    const dateElement = document.getElementById('currentDate');

    if (timeElement && dateElement) {
        const now = new Date();
        const timeString = now.toLocaleTimeString('pt-BR', { hour12: false });

        const diasSemana = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];
        const meses = [
            'janeiro', 'fevereiro', 'março', 'abril', 'maio', 'junho',
            'julho', 'agosto', 'setembro', 'outubro', 'novembro', 'dezembro'
        ];
        const diaSemana = diasSemana[now.getDay()];
        const dia = now.getDate();
        const mes = meses[now.getMonth()];
        const ano = now.getFullYear();

        timeElement.textContent = timeString;
        dateElement.textContent = `${diaSemana}, ${dia} de ${mes} de ${ano}`;
    }
}

function abrirModuloInfo(nome) {
    showModal(
        nome,
        `O módulo "${nome}" está configurado nas diretrizes corporativas e será integrado na próxima fase operacional. O Spring Security e os logs de auditoria já monitoram as ações do sistema.`,
        'fas fa-shield-halved',
        [{
            text: 'Compreendido',
            class: 'modal-btn-primary',
            action: closeModal
        }]
    );
}

function abrirNotificacoes() {
    showModal(
        'Central de Notificações',
        'Todos os subsistemas de controle de acesso (NFC, Biometria e Banco de Dados) estão operando dentro dos parâmetros de segurança. Nenhum incidente crítico detectado.',
        'fas fa-bell',
        [{
            text: 'Fechar',
            class: 'modal-btn-primary',
            action: closeModal
        }]
    );
}

function alternarFiltroRapidoPessoas() {
    const searchInput = document.getElementById('searchPessoa');
    if (searchInput) {
        searchInput.focus();
        searchInput.select();
    }
}

/* --------------------------------------------------------------------------
   5. CADASTRO DE PESSOAS E BIOMETRIA
   -------------------------------------------------------------------------- */
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
                    'Por favor, digite um CPF válido de 11 dígitos.',
                    'fas fa-triangle-exclamation',
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
                'Tem certeza que deseja cancelar? Todos os dados digitados serão perdidos.',
                'fas fa-triangle-exclamation',
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
                            showSection('listar-pessoas');
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
                    'Por favor, preencha pelo menos o Nome e o CPF antes de cadastrar o cartão.',
                    'fas fa-triangle-exclamation',
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
                    'Por favor, preencha pelo menos o Nome e o CPF antes de cadastrar o rosto.',
                    'fas fa-triangle-exclamation',
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
    if (!cpf) return false;
    cpf = cpf.replace(/[^\d]/g, '');
    if (cpf.length !== 11) return false;
    if (/^(\d)\1{10}$/.test(cpf)) return false;

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

function startCardReading() {
    showModal(
        'Aproxime o Cartão',
        'Aproxime o cartão NFC/RFID do leitor e aguarde a detecção...',
        'fas fa-credit-card',
        [{
            text: 'Cancelar',
            class: 'modal-btn-secondary',
            action: () => {
                stopCardReading();
                closeModal();
            }
        }]
    );

    readCardUID();
}

async function readCardUID() {
    try {
        const response = await fetch('/api/person/card/read', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) throw new Error('Falha na comunicação com leitor');
        const data = await response.json();

        if (data.status === 'success' && data.uid) {
            handleCardDetected(data.uid);
        } else if (data.status === 'timeout') {
            showCardError('Tempo limite esgotado. Tente aproximar o cartão novamente.');
        } else {
            showCardError(data.message || 'Erro ao ler cartão.');
        }
    } catch (error) {
        showCardError('Erro de conexão com o serviço do leitor de cartões.');
    }
}

function handleCardDetected(uid) {
    showModal(
        'Cartão Detectado',
        `UID do Cartão: ${uid}. Deseja vincular a este cadastro?`,
        'fas fa-check-circle',
        [
            {
                text: 'Não',
                class: 'modal-btn-secondary',
                action: closeModal
            },
            {
                text: 'Sim, Salvar',
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
    savePersonData(personData);
}

async function savePersonData(personData) {
    try {
        const response = await fetch('/api/person/save', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(personData)
        });

        if (response.ok) {
            showModal(
                'Cadastro Concluído',
                'A pessoa foi cadastrada com sucesso no sistema!',
                'fas fa-circle-check',
                [{
                    text: 'Concluir',
                    class: 'modal-btn-primary',
                    action: () => {
                        closeModal();
                        document.getElementById('cadastroPessoaForm').reset();
                        showSection('listar-pessoas');
                        carregarEstatisticas();
                    }
                }]
            );
        } else {
            const error = await response.json().catch(() => ({}));
            showCardError(error.message || 'Erro ao salvar pessoa.');
        }
    } catch (error) {
        showCardError('Erro de conexão com o servidor.');
    }
}

function showCardError(message) {
    showModal(
        'Aviso',
        message,
        'fas fa-triangle-exclamation',
        [
            {
                text: 'Tentar Novamente',
                class: 'modal-btn-primary',
                action: startCardReading
            },
            {
                text: 'Fechar',
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
        'Iniciando captura de biometria facial para ' + nome + '. Posicione-se em frente à câmera...',
        'fas fa-camera',
        [{
            text: 'Cancelar',
            class: 'modal-btn-secondary',
            action: () => {
                stopFaceCapture();
                closeModal();
            }
        }]
    );

    captureFaceData(nome);
}

async function captureFaceData(nome) {
    try {
        tempFaceEncoding = null;
        const response = await fetch('/api/person/face/capture', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nome: nome })
        });

        if (!response.ok) throw new Error('Falha ao iniciar captura no servidor');
        verificarCaptura(nome);
    } catch (error) {
        showModal(
            'Erro na Captura',
            'Erro de comunicação com o serviço biométrico.',
            'fas fa-triangle-exclamation',
            [{
                text: 'OK',
                class: 'modal-btn-primary',
                action: closeModal
            }]
        );
    }
}

function verificarCaptura(nome) {
    const checkInterval = setInterval(async () => {
        try {
            const statusResponse = await fetch('/api/person/face/status');
            const status = await statusResponse.json();

            if (status.completed) {
                clearInterval(checkInterval);
                if (status.success) {
                    showModal(
                        'Rosto Capturado',
                        'Biometria facial registrada com sucesso!',
                        'fas fa-circle-check',
                        [{
                            text: 'OK',
                            class: 'modal-btn-primary',
                            action: closeModal
                        }]
                    );
                } else {
                    showModal(
                        'Falha na Captura',
                        status.message || 'Não foi possível capturar o rosto com nitidez suficiente.',
                        'fas fa-triangle-exclamation',
                        [{
                            text: 'Tentar Novamente',
                            class: 'modal-btn-primary',
                            action: () => captureFaceData(nome)
                        }]
                    );
                }
            }
        } catch (error) {
            clearInterval(checkInterval);
            closeModal();
        }
    }, 1000);

    setTimeout(() => {
        clearInterval(checkInterval);
        stopFaceCapture();
    }, 30000);
}

function stopFaceCapture() {
    fetch('/api/person/face/stop', { method: 'POST' }).catch(() => {});
}

/* --------------------------------------------------------------------------
   6. ESTATÍSTICAS E PAINEL PRINCIPAL
   -------------------------------------------------------------------------- */
async function carregarEstatisticas() {
    try {
        const response = await fetch('/api/person/stats', {
            method: 'GET',
            cache: 'no-cache',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) throw new Error('Falha ao buscar estatísticas');

        const stats = await response.json();
        
        // Indicadores do topo
        const statPessoasAtivas = document.getElementById('stat-pessoas-ativas');
        const statAcessosHoje = document.getElementById('stat-acessos-hoje');
        const statAcessosNegados = document.getElementById('stat-acessos-negados');
        const statAlertasCriticos = document.getElementById('stat-alertas-criticos');

        if (statPessoasAtivas) statPessoasAtivas.textContent = stats.totalPessoas ?? 0;
        if (statAcessosHoje) statAcessosHoje.textContent = stats.acessosHoje ?? 0;
        if (statAcessosNegados) statAcessosNegados.textContent = stats.acessosNegados ?? 0;
        if (statAlertasCriticos) statAlertasCriticos.textContent = stats.alertasCriticos ?? 0;

        // Indicadores dos cards complementares
        const miniTotalUsuarios = document.getElementById('miniTotalUsuarios');
        if (miniTotalUsuarios) miniTotalUsuarios.textContent = stats.totalPessoas ?? 0;

        carregarAreasRestritas();
        carregarAcessosTempoReal();
    } catch (error) {
        console.warn('Estatísticas temporariamente indisponíveis:', error.message);
    }
}

/* --------------------------------------------------------------------------
   7. GESTÃO DE PESSOAS (TABELA CORPORATIVA)
   -------------------------------------------------------------------------- */
async function carregarListaPessoas() {
    const pessoasGrid = document.getElementById('pessoasGrid');
    if (!pessoasGrid) return;

    pessoasGrid.innerHTML = `
        <div class="loading-state" style="padding: 30px; text-align: center; color: var(--text-muted);">
            <i class="fas fa-spinner fa-spin" style="font-size: 22px; margin-bottom: 8px;"></i>
            <p>Carregando registros de pessoas...</p>
        </div>
    `;

    try {
        const response = await fetch('/api/person/list', {
            method: 'GET',
            cache: 'no-cache',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) throw new Error('Falha ao buscar pessoas');

        const pessoas = await response.json();
        cachedPessoas = pessoas || [];
        renderizarPessoas(cachedPessoas);
    } catch (error) {
        pessoasGrid.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-triangle-exclamation" style="color: var(--color-error);"></i>
                <h4>Erro ao carregar pessoas</h4>
                <p>${error.message || 'Tente novamente em instantes.'}</p>
            </div>
        `;
    }
}

function renderizarPessoas(pessoas) {
    const pessoasGrid = document.getElementById('pessoasGrid');
    const searchInput = document.getElementById('searchPessoa');
    if (!pessoasGrid) return;

    function render(listaPessoas) {
        if (!listaPessoas || listaPessoas.length === 0) {
            pessoasGrid.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-users-slash"></i>
                    <h4>Nenhuma pessoa encontrada</h4>
                    <p>Cadastre uma nova pessoa ou ajuste o termo de busca.</p>
                </div>
            `;
            return;
        }

        // Ordenação
        listaPessoas.sort((a, b) => {
            let valA = a[sortPessoasField];
            let valB = b[sortPessoasField];
            if (typeof valA === 'string') valA = valA.toLowerCase();
            if (typeof valB === 'string') valB = valB.toLowerCase();
            if (valA < valB) return sortPessoasAsc ? -1 : 1;
            if (valA > valB) return sortPessoasAsc ? 1 : -1;
            return 0;
        });

        let rowsHtml = listaPessoas.map(pessoa => {
            const iniciais = (pessoa.nome || 'P')
                .split(' ')
                .filter(Boolean)
                .map(n => n[0])
                .slice(0, 2)
                .join('')
                .toUpperCase();

            const matricula = String(pessoa.id).padStart(4, '0');
            const funcaoNormalizada = (pessoa.funcao || 'OPERADOR').toUpperCase();
            
            // Perfil com badge semântico
            let perfilBadge = '';
            if (funcaoNormalizada.includes('SUPERVISOR')) {
                perfilBadge = `<span class="badge-perfil supervisor"><i class="fas fa-shield-halved"></i> Supervisor</span>`;
            } else if (funcaoNormalizada.includes('GERENTE')) {
                perfilBadge = `<span class="badge-perfil"><i class="fas fa-briefcase"></i> Gerente</span>`;
            } else if (funcaoNormalizada.includes('DIRETOR')) {
                perfilBadge = `<span class="badge-perfil"><i class="fas fa-user-tie"></i> Diretor</span>`;
            } else if (funcaoNormalizada.includes('TERCEIRIZADO')) {
                perfilBadge = `<span class="badge-perfil"><i class="fas fa-id-badge"></i> Terceirizado</span>`;
            } else if (funcaoNormalizada.includes('VISITANTE')) {
                perfilBadge = `<span class="badge-perfil"><i class="fas fa-user-clock"></i> Visitante</span>`;
            } else {
                perfilBadge = `<span class="badge-perfil"><i class="fas fa-user"></i> Funcionário</span>`;
            }

            const dataCadastroStr = pessoa.dataCadastro ? formatarDataHora(pessoa.dataCadastro) : 'Ativo recente';

            return `
                <tr>
                    <td style="width: 40px;">
                        <input type="checkbox" class="corp-checkbox person-row-check" value="${pessoa.id}">
                    </td>
                    <td>
                        <div class="table-person-cell">
                            <div class="table-person-avatar">${iniciais}</div>
                            <span class="table-person-name">${escapeHtml(pessoa.nome)}</span>
                        </div>
                    </td>
                    <td>
                        <span class="table-matricula-text">${matricula}</span>
                    </td>
                    <td>
                        ${perfilBadge}
                    </td>
                    <td>
                        <span class="status-pill-table ativo">
                            <span class="status-dot-inline"></span> Ativo
                        </span>
                    </td>
                    <td>
                        <span style="font-size: 12.5px; color: var(--text-secondary);">${dataCadastroStr}</span>
                    </td>
                    <td>
                        <div class="table-actions-cell">
                            <button class="btn-table-action" title="Editar Pessoa" onclick="editarPessoa(${pessoa.id})">
                                <i class="fas fa-pencil"></i>
                            </button>
                            <button class="btn-table-action delete" title="Excluir Pessoa" onclick="excluirPessoa(${pessoa.id}, '${escapeHtml(pessoa.nome)}')">
                                <i class="fas fa-trash-can"></i>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');

        pessoasGrid.innerHTML = `
            <div class="corp-table-wrapper">
                <table class="corp-table">
                    <thead>
                        <tr>
                            <th style="width: 40px;">
                                <input type="checkbox" id="checkAllPersons" class="corp-checkbox" title="Selecionar Todos">
                            </th>
                            <th class="sortable" onclick="ordenarPessoas('nome')">
                                Nome <i class="fas fa-sort sort-icon"></i>
                            </th>
                            <th class="sortable" onclick="ordenarPessoas('id')">
                                Matrícula <i class="fas fa-sort sort-icon"></i>
                            </th>
                            <th>Perfil</th>
                            <th>Status</th>
                            <th class="sortable" onclick="ordenarPessoas('dataCadastro')">
                                Último Acesso <i class="fas fa-sort sort-icon"></i>
                            </th>
                            <th>Ações</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${rowsHtml}
                    </tbody>
                </table>
            </div>
            
            <div class="table-footer-pagination">
                <div class="pagination-counter">
                    Exibindo 1 &ndash; ${listaPessoas.length} de ${pessoas.length} registros
                </div>
                <div class="pagination-controls-corp">
                    <button class="btn-page-corp" disabled title="Página Anterior">
                        <i class="fas fa-chevron-left"></i>
                    </button>
                    <span class="page-number-corp">1</span>
                    <button class="btn-page-corp" disabled title="Próxima Página">
                        <i class="fas fa-chevron-right"></i>
                    </button>
                </div>
            </div>
        `;

        // Check All handler
        const checkAll = document.getElementById('checkAllPersons');
        if (checkAll) {
            checkAll.addEventListener('change', function() {
                const checks = document.querySelectorAll('.person-row-check');
                checks.forEach(c => c.checked = checkAll.checked);
            });
        }
    }

    render(pessoas);

    if (searchInput) {
        searchInput.oninput = function() {
            const query = searchInput.value.toLowerCase().trim();
            const filtradas = pessoas.filter(p => {
                const nomeMatch = (p.nome || '').toLowerCase().includes(query);
                const matMatch = String(p.id).includes(query);
                const funcaoMatch = (p.funcao || '').toLowerCase().includes(query);
                const cpfMatch = (p.cpf || '').includes(query);
                return nomeMatch || matMatch || funcaoMatch || cpfMatch;
            });
            render(filtradas);
        };
    }
}

function ordenarPessoas(campo) {
    if (sortPessoasField === campo) {
        sortPessoasAsc = !sortPessoasAsc;
    } else {
        sortPessoasField = campo;
        sortPessoasAsc = true;
    }
    renderizarPessoas(cachedPessoas);
}

function formatarDataHora(dataString) {
    if (!dataString) return '--';
    const date = new Date(dataString);
    if (isNaN(date.getTime())) return dataString;
    return date.toLocaleDateString('pt-BR') + ' ' + date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
}

function formatarData(dataString) {
    if (!dataString) return '--';
    const date = new Date(dataString);
    if (isNaN(date.getTime())) return dataString;
    return date.toLocaleDateString('pt-BR');
}

function escapeHtml(text) {
    if (!text) return '';
    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

/* --------------------------------------------------------------------------
   8. EDIÇÃO E EXCLUSÃO DE PESSOA
   -------------------------------------------------------------------------- */
async function editarPessoa(id) {
    try {
        const response = await fetch(`/api/person/${id}`);
        if (!response.ok) throw new Error('Não foi possível obter dados da pessoa');

        const pessoa = await response.json();

        document.getElementById('editPersonId').value = pessoa.id;
        document.getElementById('editNomePessoa').value = pessoa.nome;
        document.getElementById('editCpfPessoa').value = pessoa.cpf;
        document.getElementById('editRgPessoa').value = pessoa.rg;
        document.getElementById('editDataNascimento').value = pessoa.dataNascimento;
        document.getElementById('editFuncaoPessoa').value = (pessoa.funcao || 'FUNCIONARIO').toUpperCase();
        document.getElementById('editCardUIDPessoa').value = pessoa.cardUID || '';

        const btnAlterarCartao = document.getElementById('btnAlterarCartao');
        if (btnAlterarCartao) {
            btnAlterarCartao.onclick = () => startCardReadingForEdit(pessoa.id);
        }

        const editPersonForm = document.getElementById('editPersonForm');
        editPersonForm.onsubmit = async function(e) {
            e.preventDefault();
            const formData = new FormData(editPersonForm);
            const data = {};
            for (let [k, v] of formData.entries()) {
                data[k] = v;
            }
            await updatePersonData(data);
        };

        const modal = document.getElementById('editPersonModalOverlay');
        if (modal) modal.classList.add('active');
    } catch (error) {
        showModal(
            'Erro',
            error.message || 'Falha ao abrir edição de pessoa.',
            'fas fa-triangle-exclamation',
            [{ text: 'OK', class: 'modal-btn-primary', action: closeModal }]
        );
    }
}

function closeEditPersonModal() {
    const modal = document.getElementById('editPersonModalOverlay');
    if (modal) modal.classList.remove('active');
}

async function updatePersonData(personData) {
    try {
        const id = personData.id;
        const response = await fetch(`/api/person/update/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(personData)
        });

        if (response.ok) {
            closeEditPersonModal();
            showModal(
                'Atualização Realizada',
                'Os dados da pessoa foram atualizados com sucesso!',
                'fas fa-circle-check',
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
            throw new Error('Falha ao atualizar dados');
        }
    } catch (error) {
        showModal(
            'Erro',
            'Não foi possível atualizar a pessoa.',
            'fas fa-triangle-exclamation',
            [{ text: 'OK', class: 'modal-btn-primary', action: closeModal }]
        );
    }
}

function startCardReadingForEdit(personId) {
    showModal(
        'Aproxime o Novo Cartão',
        'Aproxime o novo cartão NFC do leitor para atualizar o cadastro...',
        'fas fa-credit-card',
        [{
            text: 'Cancelar',
            class: 'modal-btn-secondary',
            action: () => {
                stopCardReading();
                closeModal();
            }
        }]
    );

    readCardUIDForEdit(personId);
}

async function readCardUIDForEdit(personId) {
    try {
        const response = await fetch('/api/person/card/read', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });
        const data = await response.json();
        if (data.status === 'success' && data.uid) {
            document.getElementById('editCardUIDPessoa').value = data.uid;
            closeModal();
        } else {
            showCardError('Não foi possível ler o cartão.');
        }
    } catch (e) {
        showCardError('Erro ao comunicar com o leitor.');
    }
}

function excluirPessoa(id, nome) {
    showModal(
        'Confirmar Exclusão',
        `Tem certeza que deseja excluir "${nome}"? Esta ação removerá as permissões e biometrias vinculadas.`,
        'fas fa-triangle-exclamation',
        [
            {
                text: 'Cancelar',
                class: 'modal-btn-secondary',
                action: closeModal
            },
            {
                text: 'Sim, Excluir',
                class: 'modal-btn-primary',
                action: async () => {
                    try {
                        const response = await fetch(`/api/person/${id}`, { method: 'DELETE' });
                        if (response.ok) {
                            closeModal();
                            carregarListaPessoas();
                            carregarEstatisticas();
                        } else {
                            throw new Error('Erro ao excluir');
                        }
                    } catch (err) {
                        showModal('Erro', 'Não foi possível excluir a pessoa.', 'fas fa-triangle-exclamation', [
                            { text: 'OK', class: 'modal-btn-primary', action: closeModal }
                        ]);
                    }
                }
            }
        ]
    );
}

/* --------------------------------------------------------------------------
   9. GESTÃO DE LOCAIS / ÁREAS (CADASTRO, LISTAGEM E EDIÇÃO)
   -------------------------------------------------------------------------- */
function setupLocalForm() {
    const localForm = document.getElementById('cadastroLocalForm');
    const btnCancelarLocal = document.getElementById('btnCancelarLocal');

    if (localForm) {
        localForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const formData = new FormData(localForm);
            const localData = {
                nome: formData.get('nome'),
                descricao: formData.get('descricao'),
                nivelRestricao: formData.get('nivelRestricao'),
                capacidadeMaxima: parseInt(formData.get('capacidadeMaxima')),
                funcoesAutorizadas: []
            };

            const funcoesCheckboxes = document.querySelectorAll('input[name="funcoesAutorizadas"]:checked');
            funcoesCheckboxes.forEach(cb => localData.funcoesAutorizadas.push(cb.value));

            saveLocalData(localData);
        });
    }

    if (btnCancelarLocal) {
        btnCancelarLocal.addEventListener('click', function() {
            localForm.reset();
            showSection('listar-locais');
        });
    }
}

async function saveLocalData(localData) {
    try {
        const response = await fetch('/api/local', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(localData)
        });

        if (response.ok) {
            showModal(
                'Local Cadastrado',
                'O local foi cadastrado com sucesso no sistema!',
                'fas fa-circle-check',
                [{
                    text: 'OK',
                    class: 'modal-btn-primary',
                    action: () => {
                        closeModal();
                        document.getElementById('cadastroLocalForm').reset();
                        showSection('listar-locais');
                        carregarEstatisticas();
                    }
                }]
            );
        } else {
            const err = await response.json().catch(() => ({}));
            showModal('Erro ao Salvar Local', err.message || 'Verifique os dados informados.', 'fas fa-triangle-exclamation', [
                { text: 'OK', class: 'modal-btn-primary', action: closeModal }
            ]);
        }
    } catch (e) {
        showModal('Erro de Rede', 'Não foi possível conectar ao servidor.', 'fas fa-triangle-exclamation', [
            { text: 'OK', class: 'modal-btn-primary', action: closeModal }
        ]);
    }
}

async function carregarListaLocais() {
    const locaisGrid = document.getElementById('locaisGrid');
    if (!locaisGrid) return;

    locaisGrid.innerHTML = `
        <div class="loading-state" style="padding: 30px; text-align: center; color: var(--text-muted);">
            <i class="fas fa-spinner fa-spin" style="font-size: 22px; margin-bottom: 8px;"></i>
            <p>Carregando locais cadastrados...</p>
        </div>
    `;

    try {
        const response = await fetch('/api/local/list', {
            method: 'GET',
            cache: 'no-cache',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) throw new Error('Falha ao buscar locais');

        const locais = await response.json();
        cachedLocais = locais || [];
        renderizarLocais(cachedLocais);
    } catch (error) {
        locaisGrid.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-triangle-exclamation" style="color: var(--color-error);"></i>
                <h4>Erro ao carregar locais</h4>
                <p>${error.message}</p>
            </div>
        `;
    }
}

function renderizarLocais(locais) {
    const locaisGrid = document.getElementById('locaisGrid');
    const searchInput = document.getElementById('searchLocal');
    if (!locaisGrid) return;

    function render(listaLocais) {
        if (!listaLocais || listaLocais.length === 0) {
            locaisGrid.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-building-circle-xmark"></i>
                    <h4>Nenhum local cadastrado</h4>
                    <p>Cadastre um novo ambiente para iniciar o controle.</p>
                </div>
            `;
            return;
        }

        const rowsHtml = listaLocais.map(local => {
            const nivelBadgeMap = {
                'BAIXO': '<span class="status-pill-table ativo"><span class="status-dot-inline"></span> Baixo</span>',
                'MEDIO': '<span style="color: var(--color-warning); font-weight: 600;"><i class="fas fa-shield"></i> Médio</span>',
                'ALTO': '<span style="color: var(--color-error); font-weight: 700;"><i class="fas fa-shield-halved"></i> Alto</span>'
            };
            const nivelBadge = nivelBadgeMap[local.nivelRestricao] || local.nivelRestricao;

            const funcoes = (local.funcoesAutorizadas || []).join(', ') || 'Nenhuma restrita';

            return `
                <tr>
                    <td>
                        <div class="area-name-cell">
                            <i class="fas fa-location-dot"></i>
                            <span>${escapeHtml(local.nome)}</span>
                        </div>
                    </td>
                    <td style="max-width: 240px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; color: var(--text-muted);">
                        ${escapeHtml(local.descricao || '--')}
                    </td>
                    <td>${nivelBadge}</td>
                    <td><strong>${local.capacidadeMaxima}</strong> pessoas</td>
                    <td style="max-width: 200px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                        <span style="font-size: 12px; color: var(--text-secondary);">${escapeHtml(funcoes)}</span>
                    </td>
                    <td>
                        <span class="badge-monitoring">
                            <i class="fas fa-heart-pulse"></i> MONITORANDO
                        </span>
                    </td>
                    <td>
                        <div class="table-actions-cell">
                            <button class="btn-table-action" title="Editar Local" onclick="editarLocal(${local.id})">
                                <i class="fas fa-pencil"></i>
                            </button>
                            <button class="btn-table-action delete" title="Desativar Local" onclick="excluirLocal(${local.id}, '${escapeHtml(local.nome)}')">
                                <i class="fas fa-trash-can"></i>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');

        locaisGrid.innerHTML = `
            <div class="corp-table-wrapper">
                <table class="corp-table">
                    <thead>
                        <tr>
                            <th>Área / Local</th>
                            <th>Descrição</th>
                            <th>Nível de Restrição</th>
                            <th>Capacidade</th>
                            <th>Funções Autorizadas</th>
                            <th>Status</th>
                            <th>Ações</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${rowsHtml}
                    </tbody>
                </table>
            </div>
            <div class="table-footer-pagination">
                <div class="pagination-counter">
                    Exibindo ${listaLocais.length} local(is) configurado(s)
                </div>
            </div>
        `;
    }

    render(locais);

    if (searchInput) {
        searchInput.oninput = function() {
            const query = searchInput.value.toLowerCase().trim();
            const filtrados = locais.filter(l => {
                return (l.nome || '').toLowerCase().includes(query) ||
                       (l.descricao || '').toLowerCase().includes(query);
            });
            render(filtrados);
        };
    }
}

async function editarLocal(id) {
    try {
        const response = await fetch(`/api/local/${id}`);
        if (!response.ok) throw new Error('Não foi possível obter dados do local');
        const local = await response.json();

        document.getElementById('editLocalId').value = local.id;
        document.getElementById('editNomeLocal').value = local.nome;
        document.getElementById('editDescricaoLocal').value = local.descricao;
        document.getElementById('editNivelRestricao').value = local.nivelRestricao;
        document.getElementById('editCapacidadeMaxima').value = local.capacidadeMaxima;

        const checkboxes = document.querySelectorAll('#editFuncoesAutorizadas input[type="checkbox"]');
        checkboxes.forEach(cb => {
            cb.checked = (local.funcoesAutorizadas || []).includes(cb.value);
        });

        const editForm = document.getElementById('editLocalForm');
        editForm.onsubmit = async function(e) {
            e.preventDefault();
            const data = {
                id: local.id,
                nome: document.getElementById('editNomeLocal').value,
                descricao: document.getElementById('editDescricaoLocal').value,
                nivelRestricao: document.getElementById('editNivelRestricao').value,
                capacidadeMaxima: parseInt(document.getElementById('editCapacidadeMaxima').value),
                funcoesAutorizadas: []
            };
            const checked = document.querySelectorAll('#editFuncoesAutorizadas input[name="funcoesAutorizadas"]:checked');
            checked.forEach(c => data.funcoesAutorizadas.push(c.value));
            await updateLocalData(data);
        };

        const modal = document.getElementById('editLocalModalOverlay');
        if (modal) modal.classList.add('active');
    } catch (e) {
        showModal('Erro', e.message || 'Falha ao abrir edição.', 'fas fa-triangle-exclamation', [
            { text: 'OK', class: 'modal-btn-primary', action: closeModal }
        ]);
    }
}

function closeEditLocalModal() {
    const modal = document.getElementById('editLocalModalOverlay');
    if (modal) modal.classList.remove('active');
}

async function updateLocalData(localData) {
    try {
        const response = await fetch(`/api/local/${localData.id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(localData)
        });

        if (response.ok) {
            closeEditLocalModal();
            showModal('Sucesso', 'Local atualizado com sucesso!', 'fas fa-circle-check', [
                {
                    text: 'OK',
                    class: 'modal-btn-primary',
                    action: () => {
                        closeModal();
                        carregarListaLocais();
                        carregarEstatisticas();
                    }
                }
            ]);
        } else {
            throw new Error('Falha ao atualizar');
        }
    } catch (e) {
        showModal('Erro', 'Não foi possível atualizar o local.', 'fas fa-triangle-exclamation', [
            { text: 'OK', class: 'modal-btn-primary', action: closeModal }
        ]);
    }
}

function excluirLocal(id, nome) {
    showModal(
        'Desativar Local',
        `Deseja desativar o monitoramento de "${nome}"?`,
        'fas fa-triangle-exclamation',
        [
            { text: 'Cancelar', class: 'modal-btn-secondary', action: closeModal },
            {
                text: 'Sim, Desativar',
                class: 'modal-btn-primary',
                action: async () => {
                    try {
                        const response = await fetch(`/api/local/${id}`, { method: 'DELETE' });
                        if (response.ok) {
                            closeModal();
                            carregarListaLocais();
                            carregarEstatisticas();
                        } else {
                            throw new Error('Falha ao desativar');
                        }
                    } catch (e) {
                        showModal('Erro', 'Não foi possível desativar o local.', 'fas fa-triangle-exclamation', [
                            { text: 'OK', class: 'modal-btn-primary', action: closeModal }
                        ]);
                    }
                }
            }
        ]
    );
}

/* --------------------------------------------------------------------------
   10. ÁREAS MONITORADAS NO DASHBOARD
   -------------------------------------------------------------------------- */
async function carregarAreasRestritas() {
    try {
        const response = await fetch('/api/local/list', {
            method: 'GET',
            cache: 'no-cache',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) throw new Error('Falha ao obter áreas');
        const locais = await response.json();
        renderizarAreasRestritas(locais || []);
    } catch (e) {
        renderizarAreasRestritas([]);
    }
}

function renderizarAreasRestritas(locais) {
    const areasSection = document.querySelector('.areas-section');
    if (!areasSection) return;

    const subtitle = areasSection.querySelector('.section-subtitle');
    if (subtitle) {
        subtitle.textContent = `${locais.length} áreas cadastradas`;
    }

    const container = areasSection.querySelector('.areas-table-container');
    if (!container) return;

    if (locais.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-inbox"></i>
                <h4>Nenhuma área configurada</h4>
                <p>Cadastre áreas para iniciar o monitoramento em tempo real</p>
            </div>
        `;
        return;
    }

    const rowsHtml = locais.map(local => {
        const statusBadge = local.ativo !== false ?
            `<span class="badge-monitoring"><i class="fas fa-heart-pulse"></i> MONITORANDO</span>` :
            `<span class="badge-inactive"><i class="fas fa-pause"></i> INATIVO</span>`;

        return `
            <tr>
                <td>
                    <div class="area-name-cell">
                        <i class="fas fa-location-dot"></i>
                        <span>${escapeHtml(local.nome)}</span>
                    </div>
                </td>
                <td><strong>${local.capacidadeMaxima}</strong></td>
                <td><span style="color: var(--text-muted);">Monitorando</span></td>
                <td>${statusBadge}</td>
            </tr>
        `;
    }).join('');

    container.innerHTML = `
        <table class="areas-corp-table">
            <thead>
                <tr>
                    <th>Área / Local</th>
                    <th>Capacidade</th>
                    <th>Ocupação</th>
                    <th>Status</th>
                </tr>
            </thead>
            <tbody>
                ${rowsHtml}
            </tbody>
        </table>
    `;
}

/* --------------------------------------------------------------------------
   11. ACESSOS EM TEMPO REAL NO DASHBOARD
   -------------------------------------------------------------------------- */
async function carregarAcessosTempoReal() {
    try {
        const response = await fetch('/api/acesso/log', {
            method: 'GET',
            cache: 'no-cache',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) throw new Error('Falha ao buscar acessos');
        const acessos = await response.json();
        renderizarAcessosTempoReal(acessos || []);
    } catch (e) {
        renderizarAcessosTempoReal([]);
    }
}

function renderizarAcessosTempoReal(acessos) {
    const accessSection = document.querySelector('.access-feed-section');
    if (!accessSection) return;

    const existingContent = accessSection.querySelector('.empty-state, .access-feed');
    if (existingContent) existingContent.remove();

    if (!acessos || acessos.length === 0) {
        const empty = document.createElement('div');
        empty.className = 'empty-state';
        empty.innerHTML = `
            <i class="fas fa-clock-rotate-left"></i>
            <h4>Nenhum acesso registrado</h4>
            <p>Os eventos de catraca, cartões e reconhecimento facial aparecerão aqui</p>
        `;
        accessSection.appendChild(empty);
        return;
    }

    const feed = document.createElement('div');
    feed.className = 'access-feed';

    const recentes = acessos.slice(0, 10);
    recentes.forEach(acesso => {
        const isPermitido = acesso.tipoAcesso && acesso.tipoAcesso.toUpperCase().includes('PERMITIDO');
        const statusClass = isPermitido ? 'granted' : 'denied';
        const statusIcon = isPermitido ? 'fa-check' : 'fa-xmark';
        const statusText = isPermitido ? 'PERMITIDO' : 'NEGADO';

        const dataHora = new Date(acesso.dataHora);
        const horaFormatada = !isNaN(dataHora.getTime()) ?
            dataHora.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' }) : '--:--';

        const item = document.createElement('div');
        item.className = 'access-item';
        item.innerHTML = `
            <div class="access-item-left">
                <div class="access-status-icon ${statusClass}">
                    <i class="fas ${statusIcon}"></i>
                </div>
                <div class="access-details">
                    <div class="access-person">${escapeHtml(acesso.nomePessoa || 'Desconhecido')}</div>
                    <div class="access-location">
                        <i class="fas fa-location-dot"></i>
                        <span>${escapeHtml(acesso.nomeLocal || 'Local não identificado')}</span>
                    </div>
                </div>
            </div>
            <div class="access-item-right">
                <span class="access-badge-status ${statusClass}">${statusText}</span>
                <span class="access-timestamp">${horaFormatada}</span>
            </div>
        `;
        feed.appendChild(item);
    });

    accessSection.appendChild(feed);
}

/* --------------------------------------------------------------------------
   12. LOGS DE ACESSO (AUDITORIA E HISTÓRICO COM PAGINAÇÃO)
   -------------------------------------------------------------------------- */
let currentPage = 0;
const pageSize = 10;
let totalPages = 1;

async function carregarFiltrosLogs() {
    await carregarPessoasParaFiltro();
    await carregarLocaisParaFiltro();
}

async function carregarPessoasParaFiltro() {
    try {
        const response = await fetch('/api/person/list');
        const pessoas = await response.json();
        const select = document.getElementById('filtroPessoa');
        if (!select) return;
        select.innerHTML = '<option value="">Todas as pessoas</option>';
        (pessoas || []).forEach(p => {
            select.innerHTML += `<option value="${p.id}">${escapeHtml(p.nome)}</option>`;
        });
    } catch (e) {}
}

async function carregarLocaisParaFiltro() {
    try {
        const response = await fetch('/api/local/list');
        const locais = await response.json();
        const select = document.getElementById('filtroLocal');
        if (!select) return;
        select.innerHTML = '<option value="">Todos os locais</option>';
        (locais || []).forEach(l => {
            select.innerHTML += `<option value="${l.id}">${escapeHtml(l.nome)}</option>`;
        });
    } catch (e) {}
}

async function carregarLogs(page = 0, size = 10) {
    currentPage = page;
    const data = document.getElementById('filtroData')?.value;
    const pessoaId = document.getElementById('filtroPessoa')?.value;
    const localId = document.getElementById('filtroLocal')?.value;
    const tipoAcesso = document.getElementById('filtroTipoAcesso')?.value;

    let url = `/api/acesso/logs?page=${page}&size=${size}`;
    if (data) url += `&data=${data}`;
    if (pessoaId) url += `&pessoaId=${pessoaId}`;
    if (localId) url += `&localId=${localId}`;
    if (tipoAcesso) url += `&tipoAcesso=${tipoAcesso}`;

    try {
        const response = await fetch(url);
        if (!response.ok) throw new Error('Falha ao carregar logs');
        const result = await response.json();
        exibirLogs(result.content || []);
        atualizarPaginacao(result);
    } catch (e) {
        const tbody = document.getElementById('logsTableBody');
        if (tbody) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="5" style="text-align: center; padding: 24px; color: var(--color-error);">
                        Erro ao carregar logs de acesso.
                    </td>
                </tr>
            `;
        }
    }
}

function exibirLogs(logs) {
    const tbody = document.getElementById('logsTableBody');
    if (!tbody) return;

    if (!logs || logs.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" style="text-align: center; padding: 28px; color: var(--text-muted);">
                    Nenhum log registrado para os filtros selecionados.
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = logs.map(log => {
        const isPermitido = (log.tipoAcesso || '').toUpperCase().includes('PERMITIDO');
        const badge = isPermitido ?
            `<span class="badge-perfil" style="background: rgba(16, 185, 129, 0.12); color: #10B981; border-color: rgba(16, 185, 129, 0.35);"><i class="fas fa-check" style="color: #10B981;"></i> Permitido</span>` :
            `<span class="badge-perfil" style="background: rgba(239, 68, 68, 0.12); color: #EF4444; border-color: rgba(239, 68, 68, 0.35);"><i class="fas fa-xmark" style="color: #EF4444;"></i> Negado</span>`;

        return `
            <tr>
                <td>${formatarDataHora(log.dataHora)}</td>
                <td><strong>${escapeHtml(log.nomePessoa || 'Desconhecido')}</strong></td>
                <td><i class="fas fa-location-dot" style="color: var(--color-cyan); margin-right: 6px;"></i> ${escapeHtml(log.nomeLocal || '--')}</td>
                <td>${badge}</td>
                <td style="color: var(--text-muted); font-size: 12px;">Registro de Catraca / Terminal</td>
            </tr>
        `;
    }).join('');
}

function atualizarPaginacao(result) {
    totalPages = result.totalPages || 1;
    const paginationInfo = document.getElementById('paginationInfo');
    const pageNumbers = document.getElementById('pageNumbers');
    const btnPrevPage = document.getElementById('btnPrevPage');
    const btnNextPage = document.getElementById('btnNextPage');

    if (paginationInfo) {
        paginationInfo.textContent = `Mostrando página ${currentPage + 1} de ${totalPages} (${result.totalElements || 0} registros)`;
    }
    if (pageNumbers) {
        pageNumbers.textContent = String(currentPage + 1);
    }
    if (btnPrevPage) btnPrevPage.disabled = currentPage <= 0;
    if (btnNextPage) btnNextPage.disabled = currentPage >= totalPages - 1;
}

function paginaAnterior() {
    if (currentPage > 0) carregarLogs(currentPage - 1, pageSize);
}

function proximaPagina() {
    if (currentPage < totalPages - 1) carregarLogs(currentPage + 1, pageSize);
}

function limparFiltros() {
    const filtroData = document.getElementById('filtroData');
    const filtroPessoa = document.getElementById('filtroPessoa');
    const filtroLocal = document.getElementById('filtroLocal');
    const filtroTipoAcesso = document.getElementById('filtroTipoAcesso');

    if (filtroData) filtroData.value = '';
    if (filtroPessoa) filtroPessoa.value = '';
    if (filtroLocal) filtroLocal.value = '';
    if (filtroTipoAcesso) filtroTipoAcesso.value = '';

    carregarLogs(0, pageSize);
}

/* --------------------------------------------------------------------------
   13. RELATÓRIOS ANALÍTICOS DE ACESSO
   -------------------------------------------------------------------------- */
async function gerarRelatorio() {
    const periodo = document.getElementById('periodoRelatorio')?.value || 'ultimo_mes';
    const dataInicio = document.getElementById('dataInicio')?.value;
    const dataFim = document.getElementById('dataFim')?.value;

    const divPersonalizado = document.getElementById('periodoPersonalizado');
    if (divPersonalizado) {
        divPersonalizado.style.display = (periodo === 'personalizado') ? 'flex' : 'none';
    }

    let url = `/api/acesso/relatorio?periodo=${periodo}`;
    if (periodo === 'personalizado' && dataInicio && dataFim) {
        url += `&dataInicio=${dataInicio}&dataFim=${dataFim}`;
    }

    try {
        const response = await fetch(url);
        if (!response.ok) throw new Error('Falha ao gerar relatório');
        const relatorio = await response.json();
        exibirRelatorio(relatorio);
    } catch (e) {
        console.warn('Erro ao carregar dados do relatório:', e);
    }
}

function exibirRelatorio(relatorio) {
    const totalPermitidos = document.getElementById('totalAcessosPermitidos');
    const totalNegados = document.getElementById('totalAcessosNegados');
    const totalPessoas = document.getElementById('totalPessoasUnicas');
    const totalLocais = document.getElementById('totalLocaisAcessados');

    if (totalPermitidos) totalPermitidos.textContent = relatorio.totalPermitidos ?? 0;
    if (totalNegados) totalNegados.textContent = relatorio.totalNegados ?? 0;
    if (totalPessoas) totalPessoas.textContent = relatorio.totalPessoas ?? 0;
    if (totalLocais) totalLocais.textContent = relatorio.totalLocais ?? 0;

    exibirTopPessoas(relatorio.topPessoas || []);
    exibirTopLocais(relatorio.topLocais || []);
}

function exibirTopPessoas(top) {
    const container = document.getElementById('topPessoas');
    if (!container) return;

    if (!top || top.length === 0) {
        container.innerHTML = '<p style="color: var(--text-muted); font-size: 13px;">Nenhum registro para o período.</p>';
        return;
    }

    container.innerHTML = top.map((p, idx) => `
        <div style="display: flex; align-items: center; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid var(--border-subtle);">
            <div style="display: flex; align-items: center; gap: 10px;">
                <span style="font-weight: 700; color: var(--color-cyan); width: 20px;">#${idx + 1}</span>
                <span style="font-weight: 500; color: var(--text-primary);">${escapeHtml(p.nome)}</span>
            </div>
            <span style="font-weight: 600; color: var(--text-secondary);">${p.totalAcessos} acessos</span>
        </div>
    `).join('');
}

function exibirTopLocais(top) {
    const container = document.getElementById('topLocais');
    if (!container) return;

    if (!top || top.length === 0) {
        container.innerHTML = '<p style="color: var(--text-muted); font-size: 13px;">Nenhum registro para o período.</p>';
        return;
    }

    container.innerHTML = top.map((l, idx) => `
        <div style="display: flex; align-items: center; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid var(--border-subtle);">
            <div style="display: flex; align-items: center; gap: 10px;">
                <span style="font-weight: 700; color: var(--color-cyan); width: 20px;">#${idx + 1}</span>
                <span style="font-weight: 500; color: var(--text-primary);">${escapeHtml(l.nome)}</span>
            </div>
            <span style="font-weight: 600; color: var(--text-secondary);">${l.totalAcessos} acessos</span>
        </div>
    `).join('');
}
