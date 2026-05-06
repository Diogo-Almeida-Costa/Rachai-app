import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8081/api',
});

// Adicionamos um log para você ver exatamente o que o back-end responde
api.interceptors.response.use(
    response => response,
    error => {
        console.error("Erro na API:", error.response?.data || error.message);
        return Promise.reject(error);
    }
);

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('@RachAI:token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export default api;