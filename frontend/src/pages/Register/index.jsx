import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../../services/api';
import './Register.css';

export function Register() {
    const [form, setForm] = useState({firstName: '',lastName: '', email: '', password: '', bio: '' });
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
            <div className="register">
                <div className="register-card animate-in">
                    <h2 className="login-title">Criar conta</h2>
                    <p className="login-sub">Grátis para sempre. Comece agora.</p>
                    {error && <div className="form-error">{error}</div>}
                    <form onSubmit={handleRegister} className="register-form">
                        <div className="field-group">
                            <label className="title-form"> Nome</label>
                            <input className="test"
                                placeholder="Nome"
                                value={form.firstName}
                                onChange={e => setForm({ ...form, firstName: e.target.value })}
                                required
                            />
                        </div>
                        <div className="field-group">
                            <label className="title-form">Sobrenome</label>
                            <input
                                placeholder="Sobrenome"
                                value={form.lastName}
                                onChange={e => setForm({ ...form, lastName: e.target.value })}
                                required
                            />
                        </div>
                        <div className="field-group">
                            <label className="title-form">E-mail</label>
                            <input
                                type="email"
                                placeholder="voce@email.com"
                                value={form.email}
                                onChange={e => setForm({ ...form, email: e.target.value })}
                                required
                            />
                        </div>
                        <div className="field-group">
                            <label className="title-form">Senha</label>
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
                    <p className="register-footer">
                        Já tem conta? <Link to="/">Entrar</Link>
                    </p>
                </div>
            </div>

            <div class="back">

            </div>
        </div>    
    );
}
export default Register;
