import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../../services/api';
import './Register.css';

export function Register() {
    const [form, setForm] = useState({ name: '', email: '', password: '', bio: '' });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    async function handleRegister(e) {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            await api.post('/auth/register', form);
            navigate('/');
        } catch (err) {
            setError(err.response?.data?.message || 'Erro ao cadastrar. Tente novamente.');
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="register-root">
            <div className="register-left">
                <div className="register-brand">
                    <div className="login-logo-mark">%</div>
                    <span className="login-logo-text">RachAI</span>
                </div>
                <p className="register-tagline">Divida tudo.<br/>Guarde o que importa.</p>
                <div className="decor-circle c1" />
                <div className="decor-circle c2" />
            </div>
            <div className="register-right">
                <div className="register-card animate-in">
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
                        Já tem conta? <Link to="/">Entrar</Link>
                    </p>
                </div>
            </div>
        </div>
    );
}
export default Register;
