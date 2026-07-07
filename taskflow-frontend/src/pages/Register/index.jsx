import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../../services/api';
import '../Login/Login.css';

export function Register() {
    const [form, setForm] = useState({ name: '', email: '', password: '' });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    async function handleRegister(e) {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            await api.post('/api/taskflow/auth/register', form);
            navigate('/login');
        } catch (err) {
            setError(err.response?.data?.message || 'Erro ao cadastrar. Tente novamente.');
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="login-root">
            <div className="login-left">
                <div className="login-brand">
                    <div className="login-logo-mark">T</div>
                    <span className="login-logo-text">TaskFlow</span>
                </div>
                <p className="login-tagline">Menos caos.<br/>Mais entrega.</p>
                <div className="login-decor">
                    <div className="decor-circle c1" />
                    <div className="decor-circle c2" />
                </div>
            </div>
            <div className="login-right">
                <div className="login-card animate-in">
                    <h2 className="login-title">Criar conta</h2>
                    <p className="login-sub">Grátis para sempre. Comece agora.</p>
                    {error && <div className="form-error">{error}</div>}
                    <form onSubmit={handleRegister} className="login-form">
                        <div className="field-group">
                            <label>Nome</label>
                            <input
                                placeholder="Seu nome"
                                value={form.name}
                                onChange={e => setForm({ ...form, name: e.target.value })}
                                required
                            />
                        </div>
                        <div className="field-group">
                            <label>E-mail</label>
                            <input
                                type="email"
                                placeholder="voce@email.com"
                                value={form.email}
                                onChange={e => setForm({ ...form, email: e.target.value })}
                                required
                            />
                        </div>
                        <div className="field-group">
                            <label>Senha</label>
                            <input
                                type="password"
                                placeholder="Mínimo 6 caracteres"
                                value={form.password}
                                onChange={e => setForm({ ...form, password: e.target.value })}
                                required
                                minLength={6}
                            />
                        </div>
                        <button type="submit" className="btn-primary" disabled={loading}>
                            {loading ? <span className="spinner" /> : 'Criar conta'}
                        </button>
                    </form>
                    <p className="login-footer">
                        Já tem conta? <Link to="/login">Entrar</Link>
                    </p>
                </div>
            </div>
        </div>
    );
}
export default Register;
