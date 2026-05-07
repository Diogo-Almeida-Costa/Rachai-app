import React, { useEffect, useState } from 'react';
import api from '../../services/api';
import { Sidebar } from '../../components/Sidebar';
import './Profile.css';

export function Profile() {
    const [profile, setProfile] = useState(null);
    const [form, setForm] = useState({ name: '', bio: '' });
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [saved, setSaved] = useState(false);

    useEffect(() => {
        api.get('/users/me').then(res => {
            setProfile(res.data);
            setForm({ name: res.data.name || '', bio: res.data.bio || '' });
        }).finally(() => setLoading(false));
    }, []);

    async function handleSave(e) {
        e.preventDefault();
        setSaving(true);
        try {
            const res = await api.put('/users/me', form);
            setProfile(res.data);
            setSaved(true);
            setTimeout(() => setSaved(false), 2500);
        } catch (err) {
            alert('Erro ao salvar perfil.');
        } finally {
            setSaving(false);
        }
    }

    if (loading) return (
        <div className="app-layout">
            <Sidebar />
            <main className="main-content"><div className="page-loading">Carregando...</div></main>
        </div>
    );

    return (
        <div className="app-layout">
            <Sidebar />
            <main className="main-content">
                <div className="profile-header">
                    <div className="profile-avatar">
                        {profile?.name?.[0]?.toUpperCase() || '?'}
                    </div>
                    <div>
                        <h1 className="profile-name">{profile?.name}</h1>
                        <p className="profile-email">{profile?.email}</p>
                    </div>
                </div>

                <div className="profile-card animate-in">
                    <h3 className="profile-card-title">Editar Perfil</h3>
                    <form onSubmit={handleSave} className="profile-form">
                        <div className="field-group">
                            <label>Nome</label>
                            <input
                                value={form.name}
                                onChange={e => setForm({ ...form, name: e.target.value })}
                                placeholder="Seu nome"
                            />
                        </div>
                        <div className="field-group">
                            <label>Bio</label>
                            <textarea
                                value={form.bio}
                                onChange={e => setForm({ ...form, bio: e.target.value })}
                                placeholder="Uma breve descrição sobre você..."
                                rows={4}
                            />
                        </div>
                        <div className="profile-actions">
                            {saved && <span className="saved-msg">✓ Salvo com sucesso!</span>}
                            <button type="submit" className="btn-primary" disabled={saving}>
                                {saving ? <span className="spinner" /> : 'Salvar alterações'}
                            </button>
                        </div>
                    </form>
                </div>

                <div className="profile-info-card">
                    <div className="info-row">
                        <span className="info-label">E-mail</span>
                        <span className="info-value">{profile?.email}</span>
                    </div>
                    <div className="info-row">
                        <span className="info-label">Membro desde</span>
                        <span className="info-value">
                            {profile?.createdAt
                                ? new Date(profile.createdAt).toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' })
                                : '—'}
                        </span>
                    </div>
                </div>
            </main>
        </div>
    );
}
export default Profile;
