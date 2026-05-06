import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import api from '../../services/api';
import styles from './GroupDetails.module.css';

export function GroupDetails() {
    const { id } = useParams();
    const [group, setGroup] = useState(null);
    const [debts, setDebts] = useState([]);
    const [expense, setExpense] = useState({ description: '', amount: '' });

    useEffect(() => {
        loadData();
    }, [id]);

    async function loadData() {
        try {
            const [gRes, dRes] = await Promise.all([
                api.get(`/groups/${id}`),
                api.get(`/debts/group/${id}`)
            ]);
            setGroup(gRes.data);
            setDebts(dRes.data);
        } catch (err) {
            console.error("Erro ao carregar dados");
        }
    }

    async function handleAddExpense(e) {
        e.preventDefault();
        try {
            // Mapeado exatamente para o ExpenseDTO do seu Java
            await api.post('/expenses', {
                description: expense.description,
                amount: parseFloat(expense.amount),
                groupId: parseInt(id),
                payerId: group.owner.id
            });
            setExpense({ description: '', amount: '' });
            loadData();
        } catch (err) {
            alert("Erro ao adicionar despesa.");
        }
    }

    async function handleSimplify() {
        try {
            // Chama o algoritmo de simplificação do DebtService
            const res = await api.post(`/debts/calculate/${id}`);
            setDebts(res.data);
            alert("Dívidas recalculadas!");
        } catch (err) {
            alert("Erro no recálculo.");
        }
    }

    if (!group) return <p>Carregando...</p>;

    return (
        <div className={styles.container}>
            <h1>{group.name}</h1>
            <div className={styles.mainGrid}>
                <section className={styles.card}>
                    <h3>Novo Gasto</h3>
                    <form onSubmit={handleAddExpense}>
                        <input placeholder="Descrição" value={expense.description} onChange={e => setExpense({...expense, description: e.target.value})} />
                        <input type="number" placeholder="Valor" value={expense.amount} onChange={e => setExpense({...expense, amount: e.target.value})} />
                        <button type="submit">Salvar</button>
                    </form>
                </section>
                <section className={styles.card}>
                    <div className={styles.cardHeader}>
                        <h3>Dívidas</h3>
                        <button onClick={handleSimplify} className={styles.btnAi}>✨ Simplificar</button>
                    </div>
                    {debts.map(debt => (
                        <div key={debt.id} className={styles.debtLine}>
                            <span>{debt.debtor.name} ➔ {debt.creditor.name}</span>
                            <strong>R$ {debt.amount.toFixed(2)}</strong>
                        </div>
                    ))}
                </section>
            </div>
        </div>
    );
}

export default GroupDetails;