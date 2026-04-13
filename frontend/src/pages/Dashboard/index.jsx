import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './styles.css';

const Dashboard = () => {
    const [user, setUser] = useState(null);

    useEffect(() => {
        const token = localStorage.getItem('token');
        if(!token){
            window.location.href = './login';
        }

        axios.get('http://localhost:8081/api/auth/me', {
            headers: {Authorization: `Bearer ${token}`}
        }).then(response => setUser(response.data)).catch(() => {
            localStorage.removeItem('token');
            window.location.href = './login';
        });
    }, []);

    return (
        <div className="dash-container">
            <nav className="navbar">
                <h3>Rach-AI</h3>
                <button onClick={() => {localStorage.removeItem('token'); window.location.href='/login';}}>Sair</button>
            </nav>

            <main className="dash-content">
                {user ? <h1> Olá, {user.name}!</h1> : <p>Carregando...</p>}
                <div className="actions-grid">
                    <div className="card-item">Gerenciar Grupos</div>
                    <div className="card-item">Dividir Conta</div>
                    <div className="card-item">Histórico</div>
                </div>
            </main>
        </div>
    );
};

export default Dashboard;
