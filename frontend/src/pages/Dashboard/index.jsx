import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './styles.css';
import {useNavigate} from 'react-router-dom';

const Dashboard = () => {
    const [user, setUser] = useState(null);
    const [search, setSearch] = useState('');
    const [results, setResults] = useState([]);
    const navigate = useNavigate();
    const token = localStorage.getItem('token');

    useEffect(() => {
        if(!token){
            window.location.href = './login';
            return;
        }

        axios.get('http://localhost:8081/api/users/me', {
            headers: {Authorization: `Bearer ${token}`}
        }).then(response => setUser(response.data)).catch(err => {
            if(err.response && err.response.status === 401 || err.response.status === 403){
                localStorage.removeItem('token');
                window.location.href = '/login';
            }
        });
    }, [token]);

    const handleSearch = async () => {
        if(!search){
            return;
        }

        try{
            const res = await axios.get(`http://localhost:8081/api/users/search?query=${search}`,{
                headers: {Authorization: `Baerer ${token}`}
            });
            setResults(res.data);
        } catch(err){
            console.error("Erro na busca", err);
        }
    };

    const addFriend = async (friendId) => {
        try{
            await axios.post(`http://localhost:8081/api/users/friends/${friendId}`, {}, {
                headers: {Authorization: `Baerer ${token}`}
            });
            alert("Amigo adicionado com sucesso!");
            setResults([]);
            setSearch('');
        } catch(err){
            alert("Erro ao adicionar amigo.")
        }
    };

    return (
        <div className="dash-container">
            <nav className="navbar">
                <h3>Rach-AI</h3>
                <button onClick={() => { localStorage.removeItem('token'); window.location.href = '/login'; }}>Sair</button>
            </nav>

            <main className="dash-content">
                {user ? <h1> Olá, {user.name}!</h1> : <p>Carregando...</p>}
                
                <div className="actions-grid">
                    <div className="card-item" onClick={() => navigate('/group/1')}>Gerenciar Grupos</div>
                    <div className="card-item">Dividir Conta</div>
                    <div className="card-item">Histórico</div>
                </div>

                <hr style={{ margin: '40px 0', opacity: 0.2 }} />

                <section className="card-item" style={{ cursor: 'default', width: '100%' }}>
                    <h4>Encontrar Amigos</h4>
                    <div style={{ display: 'flex', gap: '10px', marginTop: '15px' }}>
                        <input 
                            type="text" 
                            placeholder="Buscar por nome ou e-mail..." 
                            value={search}
                            onChange={e => setSearch(e.target.value)}
                            style={{ flex: 1, padding: '10px', borderRadius: '8px', border: '1px solid #ddd' }}
                        />
                        <button onClick={handleSearch} style={{ padding: '10px 20px', cursor: 'pointer' }}>Buscar</button>
                    </div>

                    <div style={{ marginTop: '20px', textAlign: 'left' }}>
                        {results.map(r => (
                            <div key={r.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '10px', borderBottom: '1px solid #eee' }}>
                                <span>{r.name} ({r.email})</span>
                                <button onClick={() => addFriend(r.id)} style={{ background: '#4CAF50', color: 'white', border: 'none', padding: '5px 10px', borderRadius: '4px', cursor: 'pointer' }}>
                                    + Adicionar
                                </button>
                            </div>
                        ))}
                    </div>
                </section>
            </main>
        </div>
    );
};

export default Dashboard;
