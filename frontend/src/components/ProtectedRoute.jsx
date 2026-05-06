import { Navigate } from 'react-router-dom';

export function ProtectedRoute({ children }) {
    const token = localStorage.getItem('@RachAI:token');
    return token ? children : <Navigate to="/" />;
}