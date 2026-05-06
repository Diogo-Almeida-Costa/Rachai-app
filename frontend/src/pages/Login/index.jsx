import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../../services/api';
import styles from './Login.module.css';

export function Login() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    async function handleLogin(e) {
        e.preventDefault();
        try {
            // No seu back-end, o AuthService retorna apenas o Token (String)
            const response = await api.post('/auth/login', { email, password });
            
            // Verificação: Se vier como objeto {token: "..."} ou string pura
            const token = typeof response.data === 'string' ? response.data : response.data.token;

            if (token) {
                localStorage.setItem('@RachAI:token', token);
                navigate('/dashboard');
            }
        } catch (err) {
            alert("Falha na autenticação: Verifique e-mail e senha.");
        }
    }

    return (
        <div className={styles.container}>
            <div className={styles.leftSide}>
                <h1 className={styles.logo}>% RachAI</h1>
                <p className={styles.slogan}>Sua IA de divisões financeiras.</p>
            </div>
            <div className={styles.rightSide}>
                <form onSubmit={handleLogin} className={styles.formCard}>
                    <h2>Bem-vindo de volta!</h2>
                    <input type="email" placeholder="E-mail" onChange={e => setEmail(e.target.value)} required />
                    <input type="password" placeholder="Senha" onChange={e => setPassword(e.target.value)} required />
                    <button type="submit" className={styles.btnLogin}>Entrar</button>
                </form>
                <p>Não tem conta? <Link to="/register">Cadastre-se</Link></p>
            </div>
        </div>
    );
}

export default Login;