import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../../services/api';
import './Login.css';

export function Login() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    async function handleLogin(e) {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            const response = await api.post('/auth/login', { email, password });
            const token = response.data.token;
            if (token) {
                localStorage.setItem('@FrequentAI:token', token);
                navigate('/');
            }
        } catch (err) {
            setError(err.response?.data?.message || 'E-mail ou senha incorretos.');
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="login-root">
            <div className="login-left">
                <div className="login-brand">
                    <div className="login-logo-mark">F</div>
                    <span className="login-logo-text">FrequentAI</span>
                </div>
                <p className="login-tagline">Presença registrada.<br/>Risco antecipado.</p>
                <div className="login-decor">
                    <div className="decor-circle c1" />
                    <div className="decor-circle c2" />
                    <div className="decor-circle c3" />
                </div>
            </div>
            <div className="login-right">
                <div className="login-card animate-in">
                    <h2 className="login-title">Bem-vindo de volta</h2>
                    <p className="login-sub">Entre na sua conta para continuar</p>
                    {error && <div className="form-error">{error}</div>}
                    <form onSubmit={handleLogin} className="login-form">
                        <div className="field-group">
                            <label>E-mail</label>
                            <input
                                type="email"
                                placeholder="voce@email.com"
                                value={email}
                                onChange={e => setEmail(e.target.value)}
                                required
                            />
                        </div>
                        <div className="field-group">
                            <label>Senha</label>
                            <input
                                type="password"
                                placeholder="••••••••"
                                value={password}
                                onChange={e => setPassword(e.target.value)}
                                required
                            />
                        </div>
                        <button type="submit" className="btn-primary" disabled={loading}>
                            {loading ? <span className="spinner" /> : 'Entrar'}
                        </button>
                    </form>
                    <p className="login-footer">
                        Não tem conta? <Link to="/register">Criar conta</Link>
                    </p>
                </div>
            </div>
        </div>
    );
}
export default Login;
