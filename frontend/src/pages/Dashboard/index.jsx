import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';
import { Sidebar } from '../../components/Sidebar';
import './Dashboard.css';

export function Dashboard() {
    const [groups, setGroups] = useState([]);
    const [user, setUser] = useState(null);
    const [showModal, setShowModal] = useState(false);
    const [newGroup, setNewGroup] = useState({ name: '', description: '' });
    const [loading, setLoading] = useState(true);
    const [creating, setCreating] = useState(false);
    const [aiLoading, setAiLoading] = useState(false);
    const [aiSuggestion, setAiSuggestion] = useState(null);
    const [aiContext, setAiContext] = useState('');
    const [showAiPanel, setShowAiPanel] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        loadData();
    }, []);

    async function loadData() {
        setLoading(true);
        try {
            const [gRes, uRes] = await Promise.all([
                api.get('/groups'),
                api.get('/users/me'),
            ]);
            setGroups(gRes.data);
            setUser(uRes.data);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    }

    async function createGroup(e) {
        e.preventDefault();
        if (!newGroup.name.trim()) return;
        setCreating(true);
        try {
            const res = await api.post('/groups', newGroup);
            setGroups([res.data, ...groups]);
            setNewGroup({ name: '', description: '' });
            setShowModal(false);
        } catch (err) {
            alert('Erro ao criar grupo.');
        } finally {
            setCreating(false);
        }
    }

    async function getSuggestion() {
        setAiLoading(true);
        setAiSuggestion(null);
        try {
            const res = await api.post('/groups/suggest', aiContext ? { context: aiContext } : {});
            setAiSuggestion(res.data);
        } catch (err) {
            alert('Erro ao gerar sugestão.');
        } finally {
            setAiLoading(false);
        }
    }

    async function confirmSuggestion() {
        if (!aiSuggestion) return;
        setCreating(true);
        try {
            const res = await api.post('/groups/suggest/confirm', {
                name: aiSuggestion.name,
                description: aiSuggestion.description,
                memberIds: aiSuggestion.suggestedMemberIds || [],
            });
            setGroups([res.data, ...groups]);
            setAiSuggestion(null);
            setShowAiPanel(false);
            setAiContext('');
        } catch (err) {
            alert('Erro ao criar grupo sugerido.');
        } finally {
            setCreating(false);
        }
    }

    const firstName = user?.name?.split(' ')[0] || 'você';

    return (
        <div className="app-layout">
            <Sidebar />
            <main className="main-content">
                <header className="dash-header">
                    <div>
                        <h1 className="dash-greeting">Olá, {firstName} 👋</h1>
                        <p className="dash-sub">
                            {groups.length === 0
                                ? 'Crie seu primeiro grupo para começar'
                                : `${groups.length} grupo${groups.length !== 1 ? 's' : ''} ativo${groups.length !== 1 ? 's' : ''}`}
                        </p>
                    </div>
                    <div className="dash-actions">
                        <button className="btn-ai" onClick={() => setShowAiPanel(!showAiPanel)}>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                                <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                            </svg>
                            Sugerir com IA
                        </button>
                        <button className="btn-create" onClick={() => setShowModal(true)}>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
                            </svg>
                            Novo grupo
                        </button>
                    </div>
                </header>

                {showAiPanel && (
                    <div className="ai-panel animate-in">
                        <div className="ai-panel-header">
                            <div className="ai-badge">✦ IA</div>
                            <h3>Sugestão de Grupo</h3>
                            <button className="close-btn" onClick={() => setShowAiPanel(false)}>✕</button>
                        </div>
                        <p className="ai-panel-desc">Descreva o contexto (opcional) e a IA vai sugerir um grupo baseado no seu histórico.</p>
                        <div className="ai-input-row">
                            <input
                                placeholder='Ex: "viagem para a praia com os amigos"'
                                value={aiContext}
                                onChange={e => setAiContext(e.target.value)}
                            />
                            <button className="btn-ai-go" onClick={getSuggestion} disabled={aiLoading}>
                                {aiLoading ? <span className="spinner" /> : 'Gerar'}
                            </button>
                        </div>
                        {aiSuggestion && (
                            <div className="ai-result animate-in">
                                <div className="ai-result-name">{aiSuggestion.name}</div>
                                <div className="ai-result-desc">{aiSuggestion.description}</div>
                                <button className="btn-confirm" onClick={confirmSuggestion} disabled={creating}>
                                    {creating ? <span className="spinner" /> : '✓ Criar esse grupo'}
                                </button>
                            </div>
                        )}
                    </div>
                )}

                {loading ? (
                    <div className="loading-grid">
                        {[1,2,3].map(i => <div key={i} className="skeleton-card" />)}
                    </div>
                ) : groups.length === 0 ? (
                    <div className="empty-state animate-in">
                        <div className="empty-icon">📂</div>
                        <h3>Nenhum grupo ainda</h3>
                        <p>Crie um grupo para começar a dividir despesas</p>
                        <button className="btn-create" onClick={() => setShowModal(true)}>Criar primeiro grupo</button>
                    </div>
                ) : (
                    <div className="groups-grid animate-in">
                        {groups.map((g, i) => (
                            <div
                                key={g.id}
                                className="group-card"
                                style={{ animationDelay: `${i * 0.06}s` }}
                                onClick={() => navigate(`/group/${g.id}`)}
                            >
                                <div className="group-card-top">
                                    <div className="group-avatar" style={{ background: getColor(g.id) }}>
                                        {g.name[0].toUpperCase()}
                                    </div>
                                    <div className="group-arrow">→</div>
                                </div>
                                <h3 className="group-name">{g.name}</h3>
                                {g.description && <p className="group-desc">{g.description}</p>}
                                <div className="group-meta">
                                    <span>{g.members?.length || 0} membro{g.members?.length !== 1 ? 's' : ''}</span>
                                </div>
                            </div>
                        ))}
                    </div>
                )}

                {showModal && (
                    <div className="modal-overlay" onClick={() => setShowModal(false)}>
                        <div className="modal animate-in" onClick={e => e.stopPropagation()}>
                            <div className="modal-header">
                                <h3>Novo Grupo</h3>
                                <button className="close-btn" onClick={() => setShowModal(false)}>✕</button>
                            </div>
                            <form onSubmit={createGroup} className="modal-form">
                                <div className="field-group">
                                    <label>Nome do grupo *</label>
                                    <input
                                        placeholder="Ex: Viagem Rio, Aluguel..."
                                        value={newGroup.name}
                                        onChange={e => setNewGroup({ ...newGroup, name: e.target.value })}
                                        required
                                        autoFocus
                                    />
                                </div>
                                <div className="field-group">
                                    <label>Descrição (opcional)</label>
                                    <input
                                        placeholder="Breve descrição do grupo"
                                        value={newGroup.description}
                                        onChange={e => setNewGroup({ ...newGroup, description: e.target.value })}
                                    />
                                </div>
                                <div className="modal-actions">
                                    <button type="button" className="btn-ghost" onClick={() => setShowModal(false)}>Cancelar</button>
                                    <button type="submit" className="btn-primary" disabled={creating}>
                                        {creating ? <span className="spinner" /> : 'Criar Grupo'}
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

function getColor(id) {
    const colors = [
        'linear-gradient(135deg, #7c6fff, #6355e0)',
        'linear-gradient(135deg, #ff6b9d, #e0557a)',
        'linear-gradient(135deg, #00e5a0, #00b87c)',
        'linear-gradient(135deg, #ff9e45, #e07830)',
        'linear-gradient(135deg, #45b8ff, #2090e0)',
    ];
    return colors[id % colors.length];
}

export default Dashboard;
