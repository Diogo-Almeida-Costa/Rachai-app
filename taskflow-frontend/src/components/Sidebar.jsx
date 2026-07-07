import React from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import './Sidebar.css';

export function Sidebar() {
    const location = useLocation();
    const navigate = useNavigate();

    function handleLogout() {
        localStorage.removeItem('@TaskFlow:token');
        navigate('/login');
    }

    return (
        <aside className="sidebar">
            <div className="sidebar-brand">
                <div className="sidebar-logo">T</div>
                <span>TaskFlow</span>
            </div>
            <nav className="sidebar-nav">
                <Link to="/" className={`nav-item ${location.pathname === '/' ? 'active' : ''}`}>
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                        <rect x="3" y="4" width="18" height="18" rx="2"/>
                        <path d="M3 10h18"/>
                        <path d="M8 2v4M16 2v4"/>
                    </svg>
                    <span>Projetos</span>
                </Link>
            </nav>
            <button className="nav-item logout-btn" onClick={handleLogout}>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                    <path d="M16 17l5-5-5-5"/>
                    <path d="M21 12H9"/>
                </svg>
                <span>Sair</span>
            </button>
            <div className="sidebar-footer">
                <span>Delegação Inteligente de Tarefas</span>
            </div>
        </aside>
    );
}
