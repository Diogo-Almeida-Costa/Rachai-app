import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import api from '../../services/api';
import { Sidebar } from '../../components/Sidebar';
import './TurmaDetails.css';

export function TurmaDetails() {
    const { id } = useParams();
    const turmaId = Number(id);
    const navigate = useNavigate();

    const [turma, setTurma] = useState(null);
    const [registros, setRegistros] = useState([]);
    const [estatisticas, setEstatisticas] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const [showAlunoModal, setShowAlunoModal] = useState(false);
    const [novoAluno, setNovoAluno] = useState({ nome: '', email: '' });
    const [savingAluno, setSavingAluno] = useState(false);

    const [alunoSelecionado, setAlunoSelecionado] = useState('');
    const [presente, setPresente] = useState(true);
    const [savingRegistro, setSavingRegistro] = useState(false);

    const [aiLoading, setAiLoading] = useState(false);
    const [analise, setAnalise] = useState(null);
    const [aiError, setAiError] = useState('');
    const [showAiPanel, setShowAiPanel] = useState(false);
    const [showPrompt, setShowPrompt] = useState(false);
    const [promptBruto, setPromptBruto] = useState('');

    useEffect(() => {
        loadAll();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [turmaId]);

    async function loadAll() {
        setLoading(true);
        setError('');
        try {
            const [turmasRes, registrosRes, estatisticasRes] = await Promise.all([
                api.get('/turmas'),
                api.get(`/turmas/${turmaId}/registros`),
                api.get(`/turmas/${turmaId}/estatisticas`),
            ]);
            const turmaAtual = turmasRes.data.find(t => t.id === turmaId);
            if (!turmaAtual) {
                setError('Turma não encontrada.');
            } else {
                setTurma(turmaAtual);
                if (turmaAtual.students?.length && !alunoSelecionado) {
                    setAlunoSelecionado(String(turmaAtual.students[0].id));
                }
            }
            setRegistros(registrosRes.data);
            setEstatisticas(estatisticasRes.data);
        } catch (err) {
            setError('Não foi possível carregar os dados da turma. Verifique se o backend (porta 8082) está rodando.');
        } finally {
            setLoading(false);
        }
    }

    async function matricularAluno(e) {
        e.preventDefault();
        if (!novoAluno.nome.trim() || !novoAluno.email.trim()) return;
        setSavingAluno(true);
        try {
            await api.post(`/turmas/${turmaId}/alunos`, null, { params: novoAluno });
            setNovoAluno({ nome: '', email: '' });
            setShowAlunoModal(false);
            await loadAll();
        } catch (err) {
            alert('Erro ao matricular aluno.');
        } finally {
            setSavingAluno(false);
        }
    }

    async function registrarPresenca(e) {
        e.preventDefault();
        if (!alunoSelecionado) return;
        setSavingRegistro(true);
        try {
            await api.post(`/turmas/${turmaId}/registros`, null, {
                params: { alunoId: alunoSelecionado, presente },
            });
            await loadAll();
        } catch (err) {
            alert('Erro ao registrar presença.');
        } finally {
            setSavingRegistro(false);
        }
    }

    async function analisarComIA() {
        setAiLoading(true);
        setAiError('');
        setAnalise(null);
        try {
            const res = await api.get(`/turmas/${turmaId}/analise-risco`);
            setAnalise(res.data);
        } catch (err) {
            setAiError('Não foi possível gerar a análise de risco no momento.');
        } finally {
            setAiLoading(false);
        }
    }

    async function verPrompt() {
        setShowPrompt(!showPrompt);
        if (!promptBruto) {
            try {
                const res = await api.get(`/turmas/${turmaId}/prompt-risco`);
                setPromptBruto(typeof res.data === 'string' ? res.data : JSON.stringify(res.data, null, 2));
            } catch (err) {
                setPromptBruto('Não foi possível carregar o prompt.');
            }
        }
    }

    if (loading) {
        return (
            <div className="app-layout">
                <Sidebar />
                <main className="main-content"><p className="text-dim">Carregando turma...</p></main>
            </div>
        );
    }

    if (error || !turma) {
        return (
            <div className="app-layout">
                <Sidebar />
                <main className="main-content">
                    <div className="form-error">{error || 'Turma não encontrada.'}</div>
                    <Link to="/" className="btn-ghost" style={{ display: 'inline-block', marginTop: 16 }}>← Voltar</Link>
                </main>
            </div>
        );
    }

    return (
        <div className="app-layout">
            <Sidebar />
            <main className="main-content">
                <button className="back-link" onClick={() => navigate('/')}>← Turmas</button>

                <header className="turma-header">
                    <div>
                        <h1>{turma.nome}</h1>
                        <p className="dash-sub">{turma.students.length} aluno{turma.students.length !== 1 ? 's' : ''} matriculado{turma.students.length !== 1 ? 's' : ''}</p>
                    </div>
                    <div className="dash-actions">
                        <button className="btn-ai" onClick={() => { setShowAiPanel(!showAiPanel); if (!analise) analisarComIA(); }}>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                                <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                            </svg>
                            Análise de Risco (IA)
                        </button>
                        <button className="btn-create" onClick={() => setShowAlunoModal(true)}>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
                            </svg>
                            Matricular aluno
                        </button>
                    </div>
                </header>

                {showAiPanel && (
                    <div className="ai-panel animate-in">
                        <div className="ai-panel-header">
                            <div className="ai-badge">✦ IA</div>
                            <h3>Análise de Risco</h3>
                            <button className="close-btn" onClick={() => setShowAiPanel(false)}>✕</button>
                        </div>
                        <p className="ai-panel-desc">
                            Chamada real à IA (Groq) a partir das estatísticas de assiduidade calculadas no servidor.
                            Se a IA estiver indisponível, o sistema calcula a mesma análise localmente como reserva.
                        </p>

                        {aiLoading ? (
                            <div className="ai-loading"><span className="spinner" /> Consultando IA...</div>
                        ) : aiError ? (
                            <div className="form-error">{aiError}</div>
                        ) : analise ? (
                            <div className="ai-result animate-in">
                                <div className="origem-row">
                                    <span className={`badge ${analise.origem === 'ia' ? 'badge-green' : 'badge-amber'}`}>
                                        {analise.origem === 'ia' ? '✓ Resposta da IA (Groq)' : '⚠ Fallback local (IA indisponível)'}
                                    </span>
                                </div>
                                <p className="ai-observacao">{analise.observacaoGeral}</p>
                                {analise.alunosEmRisco.length === 0 ? (
                                    <p className="text-dim">Nenhum aluno em risco no momento.</p>
                                ) : (
                                    <div className="risco-list">
                                        {analise.alunosEmRisco.map(a => (
                                            <div className="risco-item" key={a.alunoId}>
                                                <span>{a.nome}</span>
                                                <span className="badge badge-red">{a.percentualFaltas.toFixed(0)}% faltas</span>
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
                            <button className="btn-ai-go" onClick={analisarComIA}>Gerar análise</button>
                        )}
                    </div>
                )}

                <section className="panel">
                    <h3 className="panel-title">Registrar presença</h3>
                    {turma.students.length === 0 ? (
                        <p className="text-dim">Matricule ao menos um aluno para registrar presença.</p>
                    ) : (
                        <form className="registro-form" onSubmit={registrarPresenca}>
                            <select value={alunoSelecionado} onChange={e => setAlunoSelecionado(e.target.value)}>
                                {turma.students.map(a => (
                                    <option key={a.id} value={a.id}>{a.name}</option>
                                ))}
                            </select>
                            <div className="presence-toggle">
                                <button
                                    type="button"
                                    className={`toggle-btn ${presente ? 'active-green' : ''}`}
                                    onClick={() => setPresente(true)}
                                >Presente</button>
                                <button
                                    type="button"
                                    className={`toggle-btn ${!presente ? 'active-red' : ''}`}
                                    onClick={() => setPresente(false)}
                                >Falta</button>
                            </div>
                            <button type="submit" className="btn-primary" disabled={savingRegistro}>
                                {savingRegistro ? <span className="spinner" /> : 'Registrar'}
                            </button>
                        </form>
                    )}
                </section>

                <section className="panel">
                    <h3 className="panel-title">Alunos e assiduidade</h3>
                    <p className="text-dim" style={{ marginBottom: 12, fontSize: 12.5 }}>
                        Calculado no servidor · risco a partir de 25% de faltas
                    </p>
                    {estatisticas.length === 0 ? (
                        <p className="text-dim">Nenhum aluno matriculado ainda.</p>
                    ) : (
                        <div className="students-table">
                            <div className="table-row table-head">
                                <span>Aluno</span>
                                <span>Presenças</span>
                                <span>Faltas</span>
                                <span>% Faltas</span>
                                <span>Situação</span>
                            </div>
                            {estatisticas.map((e) => (
                                <div className="table-row" key={e.alunoId}>
                                    <span>{e.alunoNome}</span>
                                    <span>{e.totalRegistros - e.faltas}</span>
                                    <span>{e.faltas}</span>
                                    <span>{e.percentualFaltas.toFixed(0)}%</span>
                                    <span>
                                        {e.totalRegistros === 0 ? (
                                            <span className="badge badge-amber">Sem registros</span>
                                        ) : e.emRisco ? (
                                            <span className="badge badge-red">Em risco</span>
                                        ) : (
                                            <span className="badge badge-green">Regular</span>
                                        )}
                                    </span>
                                </div>
                            ))}
                        </div>
                    )}
                </section>

                <section className="panel">
                    <h3 className="panel-title">Histórico de registros</h3>
                    {registros.length === 0 ? (
                        <p className="text-dim">Nenhum registro de presença ainda.</p>
                    ) : (
                        <div className="registros-list">
                            {[...registros].reverse().map(r => {
                                const aluno = turma.students.find(a => a.id === r.studentId);
                                return (
                                    <div className="registro-item" key={r.id}>
                                        <span className="registro-data">{r.data}</span>
                                        <span className="registro-aluno">{aluno?.name || `Aluno #${r.studentId}`}</span>
                                        {r.presente
                                            ? <span className="badge badge-green">Presente</span>
                                            : <span className="badge badge-red">Falta</span>}
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </section>

                {showAlunoModal && (
                    <div className="modal-overlay" onClick={() => setShowAlunoModal(false)}>
                        <div className="modal animate-in" onClick={e => e.stopPropagation()}>
                            <div className="modal-header">
                                <h3>Matricular aluno</h3>
                                <button className="close-btn" onClick={() => setShowAlunoModal(false)}>✕</button>
                            </div>
                            <form onSubmit={matricularAluno}>
                                <div className="field-group">
                                    <label>Nome *</label>
                                    <input
                                        placeholder="Nome do aluno"
                                        value={novoAluno.nome}
                                        onChange={e => setNovoAluno({ ...novoAluno, nome: e.target.value })}
                                        required
                                        autoFocus
                                    />
                                </div>
                                <div className="field-group">
                                    <label>E-mail *</label>
                                    <input
                                        type="email"
                                        placeholder="aluno@email.com"
                                        value={novoAluno.email}
                                        onChange={e => setNovoAluno({ ...novoAluno, email: e.target.value })}
                                        required
                                    />
                                </div>
                                <div className="modal-actions">
                                    <button type="button" className="btn-ghost" onClick={() => setShowAlunoModal(false)}>Cancelar</button>
                                    <button type="submit" className="btn-primary" disabled={savingAluno}>
                                        {savingAluno ? <span className="spinner" /> : 'Matricular'}
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

export default TurmaDetails;
