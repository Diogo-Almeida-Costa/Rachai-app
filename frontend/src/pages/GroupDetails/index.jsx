import React, {useState, useEffect} from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import './styles.css';

const GroupDetails = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    const [expenses, setExpenses] = useState([]);
    const [debts, setDebts] = useState([]);

    const [myFriends, setMyFriends] = useState([]);

    const [description, setDescription] = useState('');
    const [amount, setAmount] = useState('');
    const [user, setUser] = useState(null);

    const token = localStorage.getItem('token');

    const loadData = async () => {
        const headers = {Authorization: `Bearer ${token}`};

        try{
            const resUser = await axios.get('http://localhost:8081/api/users/me', {headers});
            setUser(resUser.data);

            const resExp = await axios.get(`http://localhost:8081/api/expenses/group/${id}`, {headers});
            setExpenses(resExp.data);

            const resDebts = await axios.get(`http://localhost:8081/api/debts/group/${id}`, {headers});
            setDebts(resDebts.data);

            const resFriends = await axios.get('http://localhost:8081/api/users/Friends', {headers});
            setMyFriends(resFriends.data);
        } catch (err) {
            if(err.response?.status === 401 || err.response?.status === 403){
                localStorage.removeItem('token');
                window.location.href = '/login';
            }
        }
    };

    useEffect(() => { loadData(); }, [id]);

    const handleAddExpense = async (e) => {
        e.preventDefault();

        if(!amount || !description){
            return;
        }

        try{
            const headers = {Authorization: `Bearer ${token}`};

            await axios.post('http://localhost:8081/api/expenses', {
                description,
                amount: parseFloat(amount),
                groupId: id,
                payerId: user.id
            }, {headers});

            await axios.post(`http://localhost:8081/api/debts/calculate/${id}`, {}, {headers});

            setDescription('');
            setAmount('');
            loadData();
            alert("Despesa adicionada com sucesso!");
            } catch (err) {
                console.error("Erro ao adicionar despesa",err);
        }
    };

    const handleAddMember = async (friendId) => {
        try{
            const headers = {Authorization: `Baerer ${token}`};
            await axios.post(`http://localhost:8081/api/groups/${id}/members`, {id: friend}, {headers});
            loadData();
            alert("Membro adicionado ao grupo!");
        } catch(err){
            alert("Erro ao adicionar membro.");
        }
    };

    return (
        <div className="dash-container">
            <nav className="navbar">
                <h3>Rach-AI</h3>
                <button onClick={() => navigate('/dashboard')}>Voltar</button>
            </nav>

            <main className="dash-content">
                <header style={{ marginBottom: '20px' }}>
                    <h2>Painel do Grupo #{id}</h2>
                </header>

                {/* Adicionar Membros Amigos */}
                <section className="card-item" style={{ width: '100%', cursor: 'default', marginBottom: '20px', textAlign: 'left' }}>
                    <h4>Convidar Amigos</h4>
                    <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap', marginTop: '10px' }}>
                        {myFriends.length > 0 ? myFriends.map(friend => (
                            <button 
                                key={friend.id} 
                                onClick={() => handleAddMember(friend.id)}
                                style={{ padding: '5px 10px', borderRadius: '20px', border: '1px solid var(--primary)', cursor: 'pointer' }}
                            >
                                + {friend.name}
                            </button>
                        )) : <p style={{fontSize: '12px'}}>Adicione amigos no Dashboard primeiro!</p>}
                    </div>
                </section>

                <section className="card-item" style={{ width: '100%', cursor: 'default', marginBottom: '20px' }}>
                    <h4>Novo Gasto</h4>
                    <form onSubmit={handleAddExpense} style={{ display: 'flex', gap: '10px', marginTop: '10px' }}>
                        <input type="text" placeholder="Descrição" value={description} onChange={(e) => setDescription(e.target.value)} style={{ flex: 2, padding: '10px' }} />
                        <input type="number" placeholder="R$" value={amount} onChange={(e) => setAmount(e.target.value)} style={{ flex: 1, padding: '10px' }} />
                        <button type="submit">Lançar</button>
                    </form>
                </section>

                <div className="actions-grid">
                    <div className="card-item" style={{ cursor: 'default' }}>
                        <h4>Histórico</h4>
                        {expenses.map(exp => (
                            <div key={exp.id} className="history-item">
                                <span>{exp.description}</span><strong>R$ {exp.amount.toFixed(2)}</strong>
                            </div>
                        ))}
                    </div>

                    <div className="card-item debt-card-highlight" style={{ cursor: 'default' }}>
                        <h4>Acerto de Contas</h4>
                        {debts.map(debt => (
                            <div key={debt.id} className="debt-row">
                                {debt.debtor.name} deve R$ {debt.amount.toFixed(2)} para {debt.creditor.name}
                            </div>
                        ))}
                    </div>
                </div>
            </main>
        </div>
    );

};

export default GroupDetails;