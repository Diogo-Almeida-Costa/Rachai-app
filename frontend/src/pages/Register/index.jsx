import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';
import styles from './Register.module.css';

export function Register() {
    const [user, setUser] = useState({ name: '', email: '', password: '', bio: '' });
    const navigate = useNavigate();

    async function handleRegister(e) {
        e.preventDefault();
        try {
            await api.post('/auth/register', user); // Envia User para o Back-end
            alert('Cadastro realizado!');
            navigate('/');
        } catch (err) {
            alert('Erro ao cadastrar.');
        }
    }

    return (
        <div className={styles.container}>
            <form onSubmit={handleRegister} className={styles.card}>
                <h2>Cadastre-se no RachAI</h2>
                <input placeholder="Nome" onChange={e => setUser({...user, name: e.target.value})} required />
                <input placeholder="E-mail" onChange={e => setUser({...user, email: e.target.value})} required />
                <input type="password" placeholder="Senha" onChange={e => setUser({...user, password: e.target.value})} required />
                <button type="submit">Criar Conta</button>
            </form>
        </div>
    );
}

export default Register;