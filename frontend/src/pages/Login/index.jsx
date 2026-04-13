import React, { useState } from 'react';
import axios from 'axios';
import '../../styles/auth.css';

const Login = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');

    const handleLogin = async (e) => {
        e.preventDefault();
        try{
            const response = await axios.post('http://localhost:8081/api/auth/login', {
                email,
                password
            });

            localStorage.setItem('token', response.data.token);
            window.location.href = './dashboard';
        } catch (err) {
            setError(err.response?.data || 'Erro ao realizar login');
        }
    };

    return (
        <div className="auth-container">
            <form className="auth-card" onSubmit={handleLogin}>
                <h2>Rach-AI</h2>
                <p>Realize seu login</p>
                <input type="email" placeholder="E-mail" onChange={e => setEmail(e.target.value)} required/>
                <input type="password" placeholder="Senha" onChange={e => setPassword(e.target.value)} required/>
                {error && <span className="error-msg">{error}</span>}
                <button type="submit">Entrar</button>
                <a href="./register">Não tem uma conta? Cadastre-se</a>
            </form>
        </div>
    );
    
};

export default Login;