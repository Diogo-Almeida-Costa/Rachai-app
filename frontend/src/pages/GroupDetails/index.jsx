import React, {useState, useEffect} from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import './styles.css';

const GroupDetails = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    const [expenses, setExpenses] = useState([]);
    const [debts, setDebts] = useState([]);

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
        } catch (err) {
            console.error("Erro ao carregar dados", err);
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
            loadData();
            alert("Despesa adicionada com sucesso!");
            } catch (err) {
                console.error("Erro ao adicionar despesa",err);
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
                    <p>Aqui você gerencia os gastos e vê os acertos.</p>
                </header>

                {/* Formulário de entrada */}
                <section className="card-item" style={{ width: '100%', cursor: 'default', marginBottom: '20px' }}>
                    <h4>💸 Novo Gasto</h4>
                    <form onSubmit={handleAddExpense} style={{ display: 'flex', gap: '10px', marginTop: '10px' }}>
                        <input 
                            type="text" 
                            placeholder="Ex: Pizza, Combustível..." 
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            style={{ flex: 2, padding: '10px', borderRadius: '4px', border: '1px solid #ccc' }}
                        />
                        <input 
                            type="number" 
                            placeholder="Valor R$" 
                            value={amount}
                            onChange={(e) => setAmount(e.target.value)}
                            style={{ flex: 1, padding: '10px', borderRadius: '4px', border: '1px solid #ccc' }}
                        />
                        <button type="submit" style={{ padding: '10px 20px', cursor: 'pointer' }}>Lançar</button>
                    </form>
                </section>

                <div className="actions-grid">
                    <div className="card-item" style={{ cursor: 'default' }}>
                        <h4>📝 Histórico</h4>
                        {expenses.length > 0 ? expenses.map(exp => (
                            <div key={exp.id} style={{ padding: '8px 0', borderBottom: '1px solid #eee' }}>
                                <span>{exp.description}</span> - <strong>R$ {exp.amount.toFixed(2)}</strong>
                            </div>
                        )) : <p>Nenhum gasto registrado.</p>}
                    </div>

                    <div className="card-item" style={{ cursor: 'default', borderLeft: '5px solid #4CAF50' }}>
                        <h4>🤝 Acerto de Contas</h4>
                        {debts.length > 0 ? debts.map(debt => (
                            <div key={debt.id} style={{ padding: '8px 0' }}>
                                <b>{debt.debtor.name}</b> deve 💸 <b>R$ {debt.amount.toFixed(2)}</b> para <b>{debt.creditor.name}</b>
                            </div>
                        )) : <p>Tudo quitado por enquanto!</p>}
                    </div>
                </div>
            </main>
        </div>
    );

};

export default GroupDetails;