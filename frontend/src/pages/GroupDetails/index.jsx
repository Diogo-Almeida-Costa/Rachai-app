import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../../services/api';
import { Sidebar } from '../../components/Sidebar';
import './GroupDetails.css';

export function GroupDetails() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [group, setGroup] = useState(null);

    // Usuário logado extraído do token JWT armazenado no localStorage
    const loggedUser = (() => {
        try {
            const token = localStorage.getItem('@RachAI:token');
            if (!token) return null;
            const payload = JSON.parse(atob(token.split('.')[1]));
            return payload;
        } catch {
            return null;
        }
    })();
    const [debts, setDebts] = useState([]);
    const [expenses, setExpenses] = useState([]);
    const [allUsers, setAllUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [tab, setTab] = useState('despesas');

    // Forms
    const [expense, setExpense] = useState({ description: '', amount: '' });
    const [memberSearch, setMemberSearch] = useState('');
    const [addingExpense, setAddingExpense] = useState(false);
    const [simplifying, setSimplifying] = useState(false);
    const [settlingId, setSettlingId] = useState(null);

    useEffect(() => { loadData(); }, [id]);

    async function loadData() {
        setLoading(true);
        try {
            const [gRes, dRes, eRes, uRes] = await Promise.all([
                api.get(`/groups/${id}`),
                api.get(`/debts/group/${id}`),
                api.get(`/expenses/group/${id}`),
                api.get('/users'),
            ]);
            setGroup(gRes.data);
            setDebts(dRes.data);
            setExpenses(eRes.data);
            setAllUsers(uRes.data);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    }

    async function handleAddExpense(e) {
        e.preventDefault();
        if (!expense.description || !expense.amount) return;
        setAddingExpense(true);
        try {
            await api.post('/expenses', {
                description: expense.description,
                amount: parseFloat(expense.amount),
                groupId: parseInt(id),
                payerId: loggedUser?.id ?? loggedUser?.sub,
            });
            setExpense({ description: '', amount: '' });
            loadData();
        } catch (err) {
            alert('Erro ao adicionar despesa.');
        } finally {
            setAddingExpense(false);
        }
    }

    async function handleSimplify() {
        setSimplifying(true);
        try {
            const res = await api.post(`/debts/group/${id}/calculate`);
            setDebts(res.data);
        } catch (err) {
            alert('Erro ao simplificar dívidas.');
        } finally {
            setSimplifying(false);
        }
    }

    async function handleSettle(debtId) {
        setSettlingId(debtId);
        try {
            const res = await api.put(`/debts/${debtId}/settle`);
            setDebts(debts.map(d => d.id === debtId ? res.data : d));
        } catch (err) {
            alert('Erro ao liquidar dívida.');
        } finally {
            setSettlingId(null);
        }
    }

    async function handleAddMember(userId) {
        try {
            await api.post(`/groups/${id}/members/${userId}`);
            loadData();
        } catch (err) {
            alert('Erro ao adicionar membro.');
        }
    }

    async function handleRemoveMember(userId) {
        try {
            await api.delete(`/groups/${id}/members/${userId}`);
            loadData();
        } catch (err) {
            alert('Erro ao remover membro.');
        }
    }

    if (loading) return (
        <div className="app-layout">
            <Sidebar />
            <main className="main-content"><div className="page-loading">Carregando...</div></main>
        </div>
    );

    if (!group) return (
        <div className="app-layout">
            <Sidebar />
            <main className="main-content"><div className="page-loading">Grupo não encontrado.</div></main>
        </div>
    );

    const memberIds = new Set(group.members?.map(m => m.id));
    const nonMembers = allUsers.filter(u =>
        !memberIds.has(u.id) &&
        (!memberSearch || u.name?.toLowerCase().includes(memberSearch.toLowerCase()) || u.email?.toLowerCase().includes(memberSearch.toLowerCase()))
    );

    const totalExpenses = expenses.reduce((sum, e) => sum + parseFloat(e.amount || 0), 0);
    const pendingDebts = debts.filter(d => !d.settled);
    const settledDebts = debts.filter(d => d.settled);

    return (
        <div className="app-layout">
            <Sidebar />
            <main className="main-content">
                <div className="gd-header">
                    <button className="back-btn" onClick={() => navigate('/dashboard')}>
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <line x1="19" y1="12" x2="5" y2="12"/>
                            <polyline points="12 19 5 12 12 5"/>
                        </svg>
                        Voltar
                    </button>
                    <div>
                        <h1 className="gd-title">{group.name}</h1>
                        {group.description && <p className="gd-desc">{group.description}</p>}
                    </div>
                    <div className="gd-stats">
                        <div className="stat-pill">
                            <span className="stat-label">Total</span>
                            <span className="stat-value">R$ {totalExpenses.toFixed(2)}</span>
                        </div>
                        <div className="stat-pill">
                            <span className="stat-label">Membros</span>
                            <span className="stat-value">{group.members?.length || 0}</span>
                        </div>
                    </div>
                </div>

                <div className="tab-bar">
                    {['despesas', 'dívidas', 'membros'].map(t => (
                        <button key={t} className={`tab-btn ${tab === t ? 'active' : ''}`} onClick={() => setTab(t)}>
                            {t.charAt(0).toUpperCase() + t.slice(1)}
                            {t === 'dívidas' && pendingDebts.length > 0 && (
                                <span className="tab-badge">{pendingDebts.length}</span>
                            )}
                        </button>
                    ))}
                </div>

                {tab === 'despesas' && (
                    <div className="tab-content animate-in">
                        <div className="expense-form-card">
                            <h3>Adicionar Despesa</h3>
                            <form onSubmit={handleAddExpense} className="expense-form">
                                <div className="field-group">
                                    <label>Descrição</label>
                                    <input
                                        placeholder="Ex: Jantar, Uber..."
                                        value={expense.description}
                                        onChange={e => setExpense({ ...expense, description: e.target.value })}
                                        required
                                    />
                                </div>
                                <div className="field-group">
                                    <label>Valor (R$)</label>
                                    <input
                                        type="number"
                                        step="0.01"
                                        min="0.01"
                                        placeholder="0,00"
                                        value={expense.amount}
                                        onChange={e => setExpense({ ...expense, amount: e.target.value })}
                                        required
                                    />
                                </div>
                                <button type="submit" className="btn-primary" disabled={addingExpense}>
                                    {addingExpense ? <span className="spinner" /> : '+ Adicionar'}
                                </button>
                            </form>
                        </div>

                        <div className="expenses-list">
                            {expenses.length === 0 ? (
                                <div className="empty-inline">Nenhuma despesa ainda. Adicione a primeira!</div>
                            ) : expenses.map((exp, i) => (
                                <div key={exp.id} className="expense-item animate-in" style={{ animationDelay: `${i * 0.05}s` }}>
                                    <div className="expense-icon">💸</div>
                                    <div className="expense-info">
                                        <span className="expense-desc">{exp.description}</span>
                                        <span className="expense-payer">Pago por {exp.payer?.name || 'Desconhecido'}</span>
                                    </div>
                                    <span className="expense-amount">R$ {parseFloat(exp.amount).toFixed(2)}</span>
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                {tab === 'dívidas' && (
                    <div className="tab-content animate-in">
                        <div className="debts-toolbar">
                            <p className="debts-info">
                                {pendingDebts.length} dívida{pendingDebts.length !== 1 ? 's' : ''} pendente{pendingDebts.length !== 1 ? 's' : ''}
                            </p>
                            <button className="btn-simplify" onClick={handleSimplify} disabled={simplifying}>
                                {simplifying ? <span className="spinner" /> : '✦ Simplificar dívidas'}
                            </button>
                        </div>

                        {debts.length === 0 ? (
                            <div className="empty-inline">Nenhuma dívida. Adicione despesas e clique em "Simplificar".</div>
                        ) : (
                            <div className="debts-list">
                                {pendingDebts.map(d => {
                                    const isDebtor = loggedUser && (d.debtor?.id === loggedUser.id || d.debtor?.id === loggedUser.sub);
                                    return (
                                    <div key={d.id} className="debt-item">
                                        <div className="debt-people">
                                            <span className="debt-person debtor">{d.debtor?.name || '?'}</span>
                                            <span className="debt-arrow">→</span>
                                            <span className="debt-person creditor">{d.creditor?.name || '?'}</span>
                                        </div>
                                        <div className="debt-right">
                                            <span className="debt-amount">R$ {parseFloat(d.amount).toFixed(2)}</span>
                                            {isDebtor ? (
                                                <button
                                                    className="btn-settle"
                                                    onClick={() => handleSettle(d.id)}
                                                    disabled={settlingId === d.id}
                                                >
                                                    {settlingId === d.id ? <span className="spinner-sm" /> : 'Pago ✓'}
                                                </button>
                                            ) : (
                                                <span className="badge-pending">Pendente</span>
                                            )}
                                        </div>
                                    </div>
                                    );
                                })}
                                {settledDebts.length > 0 && (
                                    <>
                                        <div className="debts-divider">Liquidadas</div>
                                        {settledDebts.map(d => (
                                            <div key={d.id} className="debt-item settled">
                                                <div className="debt-people">
                                                    <span className="debt-person">{d.debtor?.name || '?'}</span>
                                                    <span className="debt-arrow">→</span>
                                                    <span className="debt-person">{d.creditor?.name || '?'}</span>
                                                </div>
                                                <div className="debt-right">
                                                    <span className="debt-amount">R$ {parseFloat(d.amount).toFixed(2)}</span>
                                                    <span className="badge-settled">✓ Pago</span>
                                                </div>
                                            </div>
                                        ))}
                                    </>
                                )}
                            </div>
                        )}
                    </div>
                )}

                {tab === 'membros' && (
                    <div className="tab-content animate-in">
                        <div className="members-section">
                            <h3 className="section-title">Membros do grupo</h3>
                            <div className="members-list">
                                {group.members?.length === 0 ? (
                                    <div className="empty-inline">Nenhum membro ainda.</div>
                                ) : group.members?.map(m => (
                                    <div key={m.id} className="member-item">
                                        <div className="member-avatar">{m.name?.[0]?.toUpperCase() || '?'}</div>
                                        <div className="member-info">
                                            <span className="member-name">{m.name}</span>
                                            <span className="member-email">{m.email}</span>
                                        </div>
                                        {m.id !== group.owner?.id && (
                                            <button className="btn-remove" onClick={() => handleRemoveMember(m.id)}>✕</button>
                                        )}
                                        {m.id === group.owner?.id && <span className="owner-badge">dono</span>}
                                    </div>
                                ))}
                            </div>
                        </div>

                        <div className="add-member-section">
                            <h3 className="section-title">Adicionar membros</h3>
                            <input
                                className="member-search"
                                placeholder="Buscar por nome ou e-mail..."
                                value={memberSearch}
                                onChange={e => setMemberSearch(e.target.value)}
                            />
                            <div className="nonmembers-list">
                                {nonMembers.length === 0 ? (
                                    <div className="empty-inline">Nenhum usuário encontrado.</div>
                                ) : nonMembers.slice(0, 8).map(u => (
                                    <div key={u.id} className="member-item">
                                        <div className="member-avatar muted">{u.name?.[0]?.toUpperCase() || '?'}</div>
                                        <div className="member-info">
                                            <span className="member-name">{u.name}</span>
                                            <span className="member-email">{u.email}</span>
                                        </div>
                                        <button className="btn-add" onClick={() => handleAddMember(u.id)}>+ Adicionar</button>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>
                )}
            </main>
        </div>
    );
}
export default GroupDetails;
