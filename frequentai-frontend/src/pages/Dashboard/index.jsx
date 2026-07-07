import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';
import { Sidebar } from '../../components/Sidebar';
import './Dashboard.css';

export function Dashboard() {
    const [turmas, setTurmas] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [nomeTurma, setNomeTurma] = useState('');
    const [creating, setCreating] = useState(false);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        loadTurmas();
    }, []);

    async function loadTurmas() {
        setLoading(true);
        setError('');
        try {
            const res = await api.get('/turmas');
            setTurmas(res.data);
        } catch (err) {
            setError('Não foi possível carregar as turmas. Verifique se o backend (porta 8082) está rodando.');
        } finally {
            setLoading(false);
        }
    }

    async function criarTurma(e) {
        e.preventDefault();
        if (!nomeTurma.trim()) return;
        setCreating(true);
        try {
            const res = await api.post('/turmas', null, { params: { nome: nomeTurma } });
            setTurmas([res.data, ...turmas]);
            setNomeTurma('');
            setShowModal(false);
        } catch (err) {
            alert('Erro ao criar turma.');
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
                        <h1 className="dash-greeting">Turmas 🎓</h1>
                        <p className="dash-sub">
                            {turmas.length === 0
                                ? 'Crie sua primeira turma para começar a registrar presenças'
                                : `${turmas.length} turma${turmas.length !== 1 ? 's' : ''} cadastrada${turmas.length !== 1 ? 's' : ''}`}
                        </p>
                    </div>
                    <button className="btn-create" onClick={() => setShowModal(true)}>
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
                        </svg>
                        Nova turma
                    </button>
                </header>

                {error && <div className="form-error">{error}</div>}

                {loading ? (
                    <div className="loading-grid">
                        {[1,2,3].map(i => <div key={i} className="skeleton-card" />)}
                    </div>
                ) : turmas.length === 0 ? (
                    <div className="empty-state animate-in">
                        <div className="empty-icon">📋</div>
                        <h3>Nenhuma turma ainda</h3>
                        <p>Crie uma turma para matricular alunos e registrar presenças</p>
                        <button className="btn-create" onClick={() => setShowModal(true)}>Criar primeira turma</button>
                    </div>
                ) : (
                    <div className="turmas-grid animate-in">
                        {turmas.map((t, i) => (
                            <div
                                key={t.id}
                                className="turma-card"
                                style={{ animationDelay: `${i * 0.06}s` }}
                                onClick={() => navigate(`/turma/${t.id}`)}
                            >
                                <div className="turma-card-top">
                                    <div className="turma-avatar">{t.nome[0].toUpperCase()}</div>
                                    <div className="turma-arrow">→</div>
                                </div>
                                <h3 className="turma-name">{t.nome}</h3>
                                <div className="turma-meta">
                                    <span>{t.students?.length || 0} aluno{t.students?.length !== 1 ? 's' : ''}</span>
                                </div>
                            </div>
                        ))}
                    </div>
                )}

                {showModal && (
                    <div className="modal-overlay" onClick={() => setShowModal(false)}>
                        <div className="modal animate-in" onClick={e => e.stopPropagation()}>
                            <div className="modal-header">
                                <h3>Nova Turma</h3>
                                <button className="close-btn" onClick={() => setShowModal(false)}>✕</button>
                            </div>
                            <form onSubmit={criarTurma}>
                                <div className="field-group">
                                    <label>Nome da turma *</label>
                                    <input
                                        placeholder="Ex: Turma A - Manhã"
                                        value={nomeTurma}
                                        onChange={e => setNomeTurma(e.target.value)}
                                        required
                                        autoFocus
                                    />
                                </div>
                                <div className="modal-actions">
                                    <button type="button" className="btn-ghost" onClick={() => setShowModal(false)}>Cancelar</button>
                                    <button type="submit" className="btn-primary" disabled={creating}>
                                        {creating ? <span className="spinner" /> : 'Criar Turma'}
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
