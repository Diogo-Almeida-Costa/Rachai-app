import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';
import styles from './Dashboard.module.css';

export function Dashboard() {
    const [groups, setGroups] = useState([]);
    const [groupName, setGroupName] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        api.get('/groups').then(res => setGroups(res.data)); // Lista grupos do GroupService
    }, []);

    async function createGroup(e) {
        e.preventDefault();
        const res = await api.post('/groups', { name: groupName });
        setGroups([...groups, res.data]);
        setGroupName('');
    }

    return (
        <div className={styles.layout}>
            <main className={styles.content}>
                <header><h1>Oi,👋</h1></header>
                <form onSubmit={createGroup} className={styles.newGroup}>
                    <input placeholder="Nome do Grupo" value={groupName} onChange={e => setGroupName(e.target.value)} />
                    <button type="submit">Novo Grupo</button>
                </form>
                <div className={styles.grid}>
                    {groups.map(g => (
                        <div key={g.id} className={styles.card} onClick={() => navigate(`/group/${g.id}`)}>
                            <h3>{g.name}</h3>
                            <span>{g.members?.length} membros</span>
                        </div>
                    ))}
                </div>
            </main>
        </div>
    );
}

export default Dashboard;