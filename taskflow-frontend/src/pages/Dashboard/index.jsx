import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';
import { Sidebar } from '../../components/Sidebar';
import './Dashboard.css';

export function Dashboard() {
    const [projetos, setProjetos] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [nomeProjeto, setNomeProjeto] = useState('');
    const [creating, setCreating] = useState(false);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        loadProjetos();
    }, []);

    async function loadProjetos() {
        setLoading(true);
        setError('');
        try {
            const res = await api.get('/api/taskflow/projetos');
            setProjetos(res.data);
        } catch (err) {
            setError('Não foi possível carregar os projetos. Verifique se o backend (porta 8083) está rodando.');
        } finally {
            setLoading(false);
        }
    }

    async function criarProjeto(e) {
        e.preventDefault();
        if (!nomeProjeto.trim()) return;
        setCreating(true);
        try {
            const res = await api.post('/api/taskflow/projetos', null, { params: { nome: nomeProjeto } });
            setProjetos([res.data, ...projetos]);
            setNomeProjeto('');
            setShowModal(false);
        } catch (err) {
            alert('Erro ao criar projeto.');
        } finally {
            setCreating(false);
        }
    }

    return (
        <div className="app-layout">
            <Sidebar />
            <main className="main-content">
                <header className="dash-header">
                    <div>
                        <h1 className="dash-greeting">Projetos 📋</h1>
                        <p className="dash-sub">
                            {projetos.length === 0
                                ? 'Crie seu primeiro projeto para começar a delegar tarefas'
                                : `${projetos.length} projeto${projetos.length !== 1 ? 's' : ''} ativo${projetos.length !== 1 ? 's' : ''}`}
                        </p>
                    </div>
                    <button className="btn-create" onClick={() => setShowModal(true)}>
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
                        </svg>
                        Novo projeto
                    </button>
                </header>

                {error && <div className="form-error">{error}</div>}

                {loading ? (
                    <div className="loading-grid">
                        {[1,2,3].map(i => <div key={i} className="skeleton-card" />)}
                    </div>
                ) : projetos.length === 0 ? (
                    <div className="empty-state animate-in">
                        <div className="empty-icon">🗂️</div>
                        <h3>Nenhum projeto ainda</h3>
                        <p>Crie um projeto para adicionar colaboradores e delegar tarefas</p>
                        <button className="btn-create" onClick={() => setShowModal(true)}>Criar primeiro projeto</button>
                    </div>
                ) : (
                    <div className="projetos-grid animate-in">
                        {projetos.map((p, i) => {
                            const pendentes = p.tasks?.filter(t => !t.concluded).length || 0;
                            return (
                                <div
                                    key={p.id}
                                    className="projeto-card"
                                    style={{ animationDelay: `${i * 0.06}s` }}
                                    onClick={() => navigate(`/projeto/${p.id}`)}
                                >
                                    <div className="projeto-card-top">
                                        <div className="projeto-avatar">{p.name[0].toUpperCase()}</div>
                                        <div className="projeto-arrow">→</div>
                                    </div>
                                    <h3 className="projeto-name">{p.name}</h3>
                                    <div className="projeto-meta">
                                        <span>{pendentes} tarefa{pendentes !== 1 ? 's' : ''} pendente{pendentes !== 1 ? 's' : ''}</span>
                                        <span className="dot">·</span>
                                        <span>carga {Number(p.totalValue || 0).toFixed(1)}</span>
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                )}

                {showModal && (
                    <div className="modal-overlay" onClick={() => setShowModal(false)}>
                        <div className="modal animate-in" onClick={e => e.stopPropagation()}>
                            <div className="modal-header">
                                <h3>Novo Projeto</h3>
                                <button className="close-btn" onClick={() => setShowModal(false)}>✕</button>
                            </div>
                            <form onSubmit={criarProjeto}>
                                <div className="field-group">
                                    <label>Nome do projeto *</label>
                                    <input
                                        placeholder="Ex: Sprint 12, Evento de Formatura..."
                                        value={nomeProjeto}
                                        onChange={e => setNomeProjeto(e.target.value)}
                                        required
                                        autoFocus
                                    />
                                </div>
                                <div className="modal-actions">
                                    <button type="button" className="btn-ghost" onClick={() => setShowModal(false)}>Cancelar</button>
                                    <button type="submit" className="btn-primary" disabled={creating}>
                                        {creating ? <span className="spinner" /> : 'Criar Projeto'}
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

export default Dashboard;
