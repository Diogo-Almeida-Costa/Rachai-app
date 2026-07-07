import React from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import './Sidebar.css';

export function Sidebar() {
    const location = useLocation();
    const navigate = useNavigate();

    function handleLogout() {
        localStorage.removeItem('@FrequentAI:token');
        navigate('/login');
    }

    return (
        <aside className="sidebar">
            <div className="sidebar-brand">
                <div className="sidebar-logo">F</div>
                <span>FrequentAI</span>
            </div>
            <nav className="sidebar-nav">
                <Link to="/" className={`nav-item ${location.pathname === '/' ? 'active' : ''}`}>
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                        <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
                        <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
                    </svg>
                    <span>Turmas</span>
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
                <span>Controle de Frequência</span>
            </div>
        </aside>
    );
}
