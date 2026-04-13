import React, { useState } from 'react';
import axios from 'axios';
import '../../styles/auth.css';

const SignUp = () => {
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');

    const handleRegister = async (e) => {
        e.preventDefault();
        try {
            await axios.post('http://localhost:8081/api/auth/register', {name, email, password});
            alert("Conta criada com sucesso! Faça login agora.");
            window.location.href = './login';
        } catch(err) {
            setError(err.response?.data || 'Erro ao criar conta');
        }
    };

    return (
        <div className="auth-container">
            <form className="auth-card" onSubmit={handleRegister}>
                <h2>Criar Conta</h2>
                <input type="text" placeholder="Nome" onChange={e => setName(e.target.value)} required/>
                <input type="email" placeholder="E-mail" onChange={e => setEmail(e.target.value)} required/>
                <input type="password" placeholder="Senha" onChange={e => setPassword(e.target.value)} required/>
                {error && <span className="error-msg">{error}</span>}
                <button type="submit">Registrar</button>
                <a href="./login">Já tem uma conta? Faça login</a>
            </form>
        </div>
    );
};

export default SignUp;