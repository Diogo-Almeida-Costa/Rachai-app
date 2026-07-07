import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import api from '../../services/api';
import { Sidebar } from '../../components/Sidebar';
import './ProjetoDetails.css';

const PRIORIDADES = ['ALTA', 'MEDIA', 'BAIXA'];

export function ProjetoDetails() {
    const { id } = useParams();
    const projectId = Number(id);
    const navigate = useNavigate();

    const [projeto, setProjeto] = useState(null);
    const [tarefasPendentes, setTarefasPendentes] = useState([]);
    const [colaboradores, setColaboradores] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const [showColabModal, setShowColabModal] = useState(false);
    const [novoColab, setNovoColab] = useState({ nome: '', email: '', ocupacaoInicial: 30 });
    const [savingColab, setSavingColab] = useState(false);

    const [showTarefaModal, setShowTarefaModal] = useState(false);
    const [novaTarefa, setNovaTarefa] = useState({ titulo: '', prioridade: 'MEDIA', esforcoEstimado: 1 });
    const [savingTarefa, setSavingTarefa] = useState(false);
    const [concludingId, setConcludingId] = useState(null);

    const [aiLoading, setAiLoading] = useState(false);
    const [sugestao, setSugestao] = useState(null);
    const [aiError, setAiError] = useState('');
    const [showAiPanel, setShowAiPanel] = useState(false);
    const [showPrompt, setShowPrompt] = useState(false);
    const [promptBruto, setPromptBruto] = useState('');

    const [showOcrPanel, setShowOcrPanel] = useState(false);
    const [arquivo, setArquivo] = useState(null);
    const [colabsSelecionados, setColabsSelecionados] = useState([]);
    const [ocrLoading, setOcrLoading] = useState(false);
    const [ocrError, setOcrError] = useState('');
    const [delegacao, setDelegacao] = useState(null);

    useEffect(() => {
        loadAll();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [projectId]);

    async function loadAll() {
        setLoading(true);
        setError('');
        try {
            const [projetosRes, tarefasRes, colabsRes] = await Promise.all([
                api.get('/api/taskflow/projetos'),
                api.get(`/api/taskflow/projetos/${projectId}/tarefas`),
                api.get(`/api/taskflow/projetos/${projectId}/colaboradores`),
            ]);
            const projetoAtual = projetosRes.data.find(p => p.id === projectId);
            if (!projetoAtual) {
                setError('Projeto não encontrado.');
            } else {
                setProjeto(projetoAtual);
            }
            setTarefasPendentes(tarefasRes.data);
            setColaboradores(colabsRes.data);
        } catch (err) {
            setError('Não foi possível carregar os dados do projeto. Verifique se o backend (porta 8083) está rodando.');
        } finally {
            setLoading(false);
        }
    }

    async function adicionarColaborador(e) {
        e.preventDefault();
        if (!novoColab.nome.trim() || !novoColab.email.trim()) return;
        setSavingColab(true);
        try {
            await api.post(`/api/taskflow/projetos/${projectId}/colaboradores`, null, {
                params: novoColab,
            });
            setNovoColab({ nome: '', email: '', ocupacaoInicial: 30 });
            setShowColabModal(false);
            await loadAll();
        } catch (err) {
            alert('Erro ao adicionar colaborador.');
        } finally {
            setSavingColab(false);
        }
    }

    async function criarTarefa(e) {
        e.preventDefault();
        if (!novaTarefa.titulo.trim()) return;
        setSavingTarefa(true);
        try {
            await api.post(`/api/taskflow/projetos/${projectId}/tarefas`, null, {
                params: novaTarefa,
            });
            setNovaTarefa({ titulo: '', prioridade: 'MEDIA', esforcoEstimado: 1 });
            setShowTarefaModal(false);
            await loadAll();
        } catch (err) {
            alert('Erro ao criar tarefa.');
        } finally {
            setSavingTarefa(false);
        }
    }

    async function concluirTarefa(taskId) {
        setConcludingId(taskId);
        try {
            await api.post(`/api/taskflow/projetos/${projectId}/tarefas/${taskId}/concluir`);
            await loadAll();
        } catch (err) {
            alert('Erro ao concluir tarefa.');
        } finally {
            setConcludingId(null);
        }
    }

    async function gerarSugestao() {
        setAiLoading(true);
        setSugestao(null);
        setAiError('');
        try {
            const res = await api.get(`/api/taskflow/projetos/${projectId}/analise-sugestao`);
            setSugestao(res.data);
        } catch (err) {
            setAiError('Não foi possível gerar a sugestão de tarefas no momento.');
        } finally {
            setAiLoading(false);
        }
    }

    async function verPrompt() {
        setShowPrompt(!showPrompt);
        if (!promptBruto) {
            try {
                const res = await api.get(`/api/taskflow/projetos/${projectId}/prompt-sugestao`);
                setPromptBruto(typeof res.data === 'string' ? res.data : JSON.stringify(res.data, null, 2));
            } catch (err) {
                setPromptBruto('Não foi possível carregar o prompt.');
            }
        }
    }

    function toggleColabSelecionado(colabId) {
        setColabsSelecionados(prev =>
            prev.includes(colabId) ? prev.filter(id => id !== colabId) : [...prev, colabId]
        );
    }

    async function processarListaViaOcr(e) {
        e.preventDefault();
        setOcrError('');
        setDelegacao(null);
        if (!arquivo) {
            setOcrError('Selecione uma imagem da lista de tarefas.');
            return;
        }
        if (colabsSelecionados.length === 0) {
            setOcrError('Selecione ao menos um colaborador para a delegação.');
            return;
        }
        setOcrLoading(true);
        try {
            const formData = new FormData();
            formData.append('file', arquivo);
            colabsSelecionados.forEach(cid => formData.append('collaboratorIds', cid));

            const res = await api.post('/api/framework/tasks/process', formData);

            // O backend serializa Map<IUser, BigDecimal>; a chave complexa (Collaborator)
            // não vira um JSON "bonito", então associamos os valores (na ordem retornada,
            // preservada pelo LinkedHashMap do framework) aos colaboradores selecionados,
            // na mesma ordem em que foram enviados.
            const valores = Object.values(res.data);
            const resultado = colabsSelecionados.map((cid, i) => {
                const colaborador = colaboradores.find(c => c.id === cid);
                return { colaborador, carga: valores[i] };
            });
            setDelegacao(resultado);
            await loadAll();
        } catch (err) {
            const msg = err.response?.data?.message || err.response?.data || 'Erro ao processar a lista de tarefas via OCR.';
            setOcrError(typeof msg === 'string' ? msg : 'Erro ao processar a lista de tarefas via OCR.');
        } finally {
            setOcrLoading(false);
        }
    }

    if (loading) {
        return (
            <div className="app-layout">
                <Sidebar />
                <main className="main-content"><p className="text-dim">Carregando projeto...</p></main>
            </div>
        );
    }

    if (error || !projeto) {
        return (
            <div className="app-layout">
                <Sidebar />
                <main className="main-content">
                    <div className="form-error">{error || 'Projeto não encontrado.'}</div>
                    <Link to="/" className="btn-ghost" style={{ display: 'inline-block', marginTop: 16 }}>← Voltar</Link>
                </main>
            </div>
        );
    }

    return (
        <div className="app-layout">
            <Sidebar />
            <main className="main-content">
                <button className="back-link" onClick={() => navigate('/')}>← Projetos</button>

                <header className="projeto-header">
                    <div>
                        <h1>{projeto.name}</h1>
                        <p className="dash-sub">
                            {tarefasPendentes.length} tarefa{tarefasPendentes.length !== 1 ? 's' : ''} pendente{tarefasPendentes.length !== 1 ? 's' : ''}
                            <span className="dot"> · </span>
                            carga total {Number(projeto.totalValue || 0).toFixed(1)}
                        </p>
                    </div>
                    <div className="dash-actions">
                        <button className="btn-ai" onClick={() => { setShowAiPanel(!showAiPanel); if (!sugestao) gerarSugestao(); }}>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                                <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                            </svg>
                            Sugestão de Tarefas (IA)
                        </button>
                        <button className="btn-ai" onClick={() => setShowOcrPanel(!showOcrPanel)}>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                                <rect x="3" y="3" width="18" height="18" rx="2"/>
                                <path d="M9 9h6M9 13h6M9 17h3"/>
                            </svg>
                            Importar lista (OCR)
                        </button>
                    </div>
                </header>

                {showAiPanel && (
                    <div className="ai-panel animate-in">
                        <div className="ai-panel-header">
                            <div className="ai-badge">✦ IA</div>
                            <h3>Sugestão de Tarefas</h3>
                            <button className="close-btn" onClick={() => setShowAiPanel(false)}>✕</button>
                        </div>
                        <p className="ai-panel-desc">
                            Chamada real à IA (Groq) a partir das tarefas pendentes. Se a IA estiver indisponível, o
                            sistema calcula uma recomendação local como reserva (sem inventar tarefas novas).
                        </p>
                        {aiLoading ? (
                            <div className="ai-loading"><span className="spinner" /> Consultando IA...</div>
                        ) : aiError ? (
                            <div className="form-error">{aiError}</div>
                        ) : sugestao ? (
                            <div className="ai-result animate-in">
                                <div className="origem-row">
                                    <span className={`badge ${sugestao.origem === 'ia' ? 'badge-green' : 'badge-amber'}`}>
                                        {sugestao.origem === 'ia' ? '✓ Resposta da IA (Groq)' : '⚠ Fallback local (IA indisponível)'}
                                    </span>
                                </div>
                                <p className="ai-observacao">{sugestao.recomendacaoPreditiva}</p>
                                {sugestao.tarefasSugeridas.length === 0 ? (
                                    <p className="text-dim">Nenhuma tarefa correlata sugerida no momento.</p>
                                ) : (
                                    <div className="sugestoes-list">
                                        {sugestao.tarefasSugeridas.map((t, i) => (
                                            <div className="sugestao-item" key={i}>
                                                <span className={`badge ${prioridadeBadge(t.prioridade)}`}>{t.prioridade}</span>
                                                <div className="sugestao-info">
                                                    <span className="sugestao-titulo">{t.titulo}</span>
                                                    <span className="text-dim">{t.motivo}</span>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                )}
                                <button className="btn-ghost sm" style={{ marginTop: 14 }} onClick={verPrompt}>
                                    {showPrompt ? 'Ocultar prompt enviado' : 'Ver prompt enviado à IA'}
                                </button>
                                {showPrompt && <pre className="ai-prompt-box">{promptBruto}</pre>}
                            </div>
                        ) : (
                            <button className="btn-ai-go" onClick={gerarSugestao}>Gerar sugestão</button>
                        )}
                    </div>
                )}


                {showOcrPanel && (
                    <div className="ai-panel animate-in">
                        <div className="ai-panel-header">
                            <div className="ai-badge badge-purple-solid">⚙ Framework</div>
                            <h3>Importar lista de tarefas via OCR e delegar</h3>
                            <button className="close-btn" onClick={() => setShowOcrPanel(false)}>✕</button>
                        </div>
                        <p className="ai-panel-desc">
                            Dispara o fluxo completo do framework: <code>OCRModule</code> lê a imagem da lista de tarefas,
                            e o <code>ResourceSplitter</code> delega a carga entre os colaboradores selecionados, de acordo
                            com a prioridade das tarefas e a ocupação de cada um.
                        </p>
                        <form className="ocr-form" onSubmit={processarListaViaOcr}>
                            <div className="field-group">
                                <label>Imagem da lista de tarefas *</label>
                                <input type="file" accept="image/*,.pdf" onChange={e => setArquivo(e.target.files[0])} />
                            </div>
                            <div className="field-group">
                                <label>Colaboradores participantes da delegação *</label>
                                {colaboradores.length === 0 ? (
                                    <p className="text-dim">Adicione colaboradores ao projeto antes de importar uma lista.</p>
                                ) : (
                                    <div className="colab-checklist">
                                        {colaboradores.map(c => (
                                            <label key={c.id} className="colab-check-item">
                                                <input
                                                    type="checkbox"
                                                    checked={colabsSelecionados.includes(c.id)}
                                                    onChange={() => toggleColabSelecionado(c.id)}
                                                />
                                                {c.name} <span className="text-dim">(ocupação {c.occupationLevel}%)</span>
                                            </label>
                                        ))}
                                    </div>
                                )}
                            </div>
                            {ocrError && <div className="form-error">{ocrError}</div>}
                            <button type="submit" className="btn-primary" disabled={ocrLoading}>
                                {ocrLoading ? <span className="spinner" /> : 'Processar e delegar'}
                            </button>
                        </form>

                        {delegacao && (
                            <div className="delegacao-result animate-in">
                                <h4>Carga delegada</h4>
                                {delegacao.map(({ colaborador, carga }) => (
                                    <div className="delegacao-item" key={colaborador?.id}>
                                        <span>{colaborador?.name || 'Colaborador'}</span>
                                        <span className="badge badge-purple">{Number(carga).toFixed(2)} pts</span>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                )}

                <section className="panel">
                    <div className="panel-header-row">
                        <h3 className="panel-title">Colaboradores</h3>
                        <button className="btn-create sm" onClick={() => setShowColabModal(true)}>+ Adicionar</button>
                    </div>
                    {colaboradores.length === 0 ? (
                        <p className="text-dim">Nenhum colaborador adicionado ainda.</p>
                    ) : (
                        <div className="colab-grid">
                            {colaboradores.map(c => (
                                <div className="colab-card" key={c.id}>
                                    <div className="colab-avatar">{c.name[0].toUpperCase()}</div>
                                    <div className="colab-info">
                                        <span className="colab-name">{c.name}</span>
                                        <span className="colab-email text-dim">{c.email}</span>
                                    </div>
                                    <div className="occupation-bar-wrap">
                                        <div className="occupation-bar">
                                            <div className="occupation-fill" style={{ width: `${c.occupationLevel}%` }} />
                                        </div>
                                        <span className="occupation-label">{c.occupationLevel}% ocupado</span>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </section>

                <section className="panel">
                    <div className="panel-header-row">
                        <h3 className="panel-title">Tarefas pendentes</h3>
                        <button className="btn-create sm" onClick={() => setShowTarefaModal(true)}>+ Nova tarefa</button>
                    </div>
                    {tarefasPendentes.length === 0 ? (
                        <p className="text-dim">Nenhuma tarefa pendente. Crie uma tarefa ou importe uma lista via OCR.</p>
                    ) : (
                        <div className="tarefas-list">
                            {tarefasPendentes.map(t => (
                                <div className="tarefa-item" key={t.id}>
                                    <span className={`badge ${prioridadeBadge(t.priority)}`}>{t.priority}</span>
                                    <span className="tarefa-titulo">{t.title}</span>
                                    <span className="tarefa-esforco text-dim">esforço {Number(t.estimatedEffort).toFixed(1)}</span>
                                    <button
                                        className="btn-ghost sm"
                                        onClick={() => concluirTarefa(t.id)}
                                        disabled={concludingId === t.id}
                                    >
                                        {concludingId === t.id ? <span className="spinner" /> : 'Concluir'}
                                    </button>
                                </div>
                            ))}
                        </div>
                    )}
                </section>

                {showColabModal && (
                    <div className="modal-overlay" onClick={() => setShowColabModal(false)}>
                        <div className="modal animate-in" onClick={e => e.stopPropagation()}>
                            <div className="modal-header">
                                <h3>Adicionar colaborador</h3>
                                <button className="close-btn" onClick={() => setShowColabModal(false)}>✕</button>
                            </div>
                            <form onSubmit={adicionarColaborador}>
                                <div className="field-group">
                                    <label>Nome *</label>
                                    <input
                                        placeholder="Nome do colaborador"
                                        value={novoColab.nome}
                                        onChange={e => setNovoColab({ ...novoColab, nome: e.target.value })}
                                        required
                                        autoFocus
                                    />
                                </div>
                                <div className="field-group">
                                    <label>E-mail *</label>
                                    <input
                                        type="email"
                                        placeholder="colaborador@email.com"
                                        value={novoColab.email}
                                        onChange={e => setNovoColab({ ...novoColab, email: e.target.value })}
                                        required
                                    />
                                </div>
                                <div className="field-group">
                                    <label>Ocupação atual: {novoColab.ocupacaoInicial}%</label>
                                    <input
                                        type="range"
                                        min="0"
                                        max="100"
                                        value={novoColab.ocupacaoInicial}
                                        onChange={e => setNovoColab({ ...novoColab, ocupacaoInicial: Number(e.target.value) })}
                                    />
                                </div>
                                <div className="modal-actions">
                                    <button type="button" className="btn-ghost" onClick={() => setShowColabModal(false)}>Cancelar</button>
                                    <button type="submit" className="btn-primary" disabled={savingColab}>
                                        {savingColab ? <span className="spinner" /> : 'Adicionar'}
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                )}

                {showTarefaModal && (
                    <div className="modal-overlay" onClick={() => setShowTarefaModal(false)}>
                        <div className="modal animate-in" onClick={e => e.stopPropagation()}>
                            <div className="modal-header">
                                <h3>Nova tarefa</h3>
                                <button className="close-btn" onClick={() => setShowTarefaModal(false)}>✕</button>
                            </div>
                            <form onSubmit={criarTarefa}>
                                <div className="field-group">
                                    <label>Título *</label>
                                    <input
                                        placeholder="Ex: Revisar contrato do fornecedor"
                                        value={novaTarefa.titulo}
                                        onChange={e => setNovaTarefa({ ...novaTarefa, titulo: e.target.value })}
                                        required
                                        autoFocus
                                    />
                                </div>
                                <div className="field-group">
                                    <label>Prioridade</label>
                                    <select
                                        value={novaTarefa.prioridade}
                                        onChange={e => setNovaTarefa({ ...novaTarefa, prioridade: e.target.value })}
                                    >
                                        {PRIORIDADES.map(p => <option key={p} value={p}>{p}</option>)}
                                    </select>
                                </div>
                                <div className="field-group">
                                    <label>Esforço estimado (horas/pontos)</label>
                                    <input
                                        type="number"
                                        min="0.1"
                                        step="0.1"
                                        value={novaTarefa.esforcoEstimado}
                                        onChange={e => setNovaTarefa({ ...novaTarefa, esforcoEstimado: e.target.value })}
                                        required
                                    />
                                </div>
                                <div className="modal-actions">
                                    <button type="button" className="btn-ghost" onClick={() => setShowTarefaModal(false)}>Cancelar</button>
                                    <button type="submit" className="btn-primary" disabled={savingTarefa}>
                                        {savingTarefa ? <span className="spinner" /> : 'Criar tarefa'}
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                )}
            </main>
        </div>
    );
}

function prioridadeBadge(prioridade) {
    if (prioridade === 'ALTA') return 'badge-red';
    if (prioridade === 'MEDIA') return 'badge-amber';
    return 'badge-green';
}

export default ProjetoDetails;
