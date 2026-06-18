import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../../services/api';
import './Login.css';
import { Mail, Lock, Eye, EyeOff } from 'lucide-react'; 

export function Login() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false); // Novo estado
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    async function handleLogin(e) {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            const response = await api.post('/auth/login', { email, password });
            const token = typeof response.data === 'string' ? response.data : response.data.token;
            if (token) {
                localStorage.setItem('@RachAI:token', token);
                navigate('/dashboard');
            }
        } catch (err) {
            setError('E-mail ou senha incorretos.');
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="login-root">
            {/* LADO ESQUERDO */}
            <div className="login-left">
                <div className="login-brand">
                    <div className="login-logo-mark">%</div>
                    <span className="login-logo-text">RachAI</span>
                </div>
                
                <div className="login-content-left">
                    <h1 className="login-tagline">
                        Dividir ficou<br/>
                        <span className="text-highlight">mais fácil.</span>
                    </h1>
                    <p className="login-description">
                        A IA que organiza, calcula e<br/>
                        resolve para você.
                    </p>
                    
                    {/* Substitua pelo seu SVG/Imagem do robô */}
                    <div className="login-illustration">
                         <img src="/public/robot.png" alt="RachAI Robot" />
                    </div>
                </div>
            </div>

            {/* LADO DIREITO */}
            <div className="login-right">
                <div className="top-right-link">
                    Ainda não tem conta? <Link to="/register">Cadastre-se</Link>
                </div>

                <div className="login-card animate-in">
                    <h2 className="login-title">Bem-vindo de volta!</h2>
                    <p className="login-sub">Faça login para continuar</p>
                    
                    {error && <div className="form-error">{error}</div>}
                    
                    <form onSubmit={handleLogin} className="login-form">
                        <div className="field-group">
                            <label>E-mail</label>
                            <div className="input-wrapper">
                                <Mail className="input-icon" size={18} />
                                <input
                                    type="email"
                                    placeholder="exemplo@email.com"
                                    value={email}
                                    onChange={e => setEmail(e.target.value)}
                                    required
                                />
                            </div>
                        </div>

                        <div className="field-group">
                            <label>Senha</label>
                            <div className="input-wrapper">
                                <Lock className="input-icon" size={18} />
                                <input
                                    type={showPassword ? "text" : "password"}
                                    placeholder="Sua senha"
                                    value={password}
                                    onChange={e => setPassword(e.target.value)}
                                    required
                                />
                                <button 
                                    type="button" 
                                    className="toggle-password"
                                    onClick={() => setShowPassword(!showPassword)}
                                >
                                    {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                                </button>
                            </div>
                        </div>

                        <div className="forgot-password">
                            <Link to="/forgot-password">Esqueceu sua senha?</Link>
                        </div>

                        <button type="submit" className="btn-primary" disabled={loading}>
                            {loading ? <span className="spinner" /> : 'Entrar'}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
}

export default Login;