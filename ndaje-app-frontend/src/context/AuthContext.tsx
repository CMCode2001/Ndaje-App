import React, { createContext, useContext, useState, useEffect } from 'react';
import { BASE_URL_USERS } from '@/api/api';

interface User {
  id: string;
  email: string;
  prenom: string;
  nom: string;
  telephone: string;
  role: 'PASSENGER' | 'DRIVER' | 'ADMIN';
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: any) => Promise<void>;
  register: (data: any, role: 'passenger' | 'driver') => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const savedUser = localStorage.getItem('user');
    const savedToken = localStorage.getItem('token');
    if (savedUser) {
      setUser(JSON.parse(savedUser));
    }
    if (savedToken) {
      setToken(savedToken);
    }
    setIsLoading(false);
  }, []);

  const decodeJWT = (token: string) => {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(atob(base64).split('').map((c) => {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
      }).join(''));
      return JSON.parse(jsonPayload);
    } catch (e) {
      return null;
    }
  };

  const login = async (credentials: any) => {
    setIsLoading(true);
    try {
      const response = await fetch(`${BASE_URL_USERS}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(credentials),
      });

      const responseData = await response.json().catch(() => ({}));

      if (!response.ok) {
        throw new Error(responseData.message || 'Identifiants invalides');
      }

      // Si le backend renvoie juste un token, on décode les infos
      const token = responseData.accessToken || responseData.token;
      let userData = responseData.user || responseData.data || responseData;

      if (token && (!userData.prenom || !userData.nom)) {
        const decoded = decodeJWT(token);
        if (decoded) {
          userData = {
            ...userData,
            id: decoded.sub || userData.id,
            email: decoded.email || userData.email,
            prenom: decoded.prenom || decoded.given_name || userData.prenom,
            nom: decoded.nom || decoded.family_name || userData.nom,
            role: decoded.role || (decoded.realm_access?.roles?.includes('ADMIN') ? 'ADMIN' : decoded.roles?.[0] || userData.role)
          };
        }
      }
      
      const sessionUser = {
        ...userData,
        role: (userData.role || '').toUpperCase()
      };

      setUser(sessionUser);
      setToken(token);
      localStorage.setItem('user', JSON.stringify(sessionUser));
      localStorage.setItem('token', token);
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (data: any, role: 'passenger' | 'driver') => {
    setIsLoading(true);
    try {
      const response = await fetch(`${BASE_URL_USERS}/register/${role}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data),
      });

      const result = await response.json().catch(() => ({}));

      if (!response.ok) {
        throw new Error(result.message || 'Erreur lors de l’inscription');
      }

      const token = result.accessToken || result.token;
      const userData = result.user || result.data || result;
      const sessionUser = {
        ...userData,
        role: (userData.role || '').toUpperCase()
      };

      setUser(sessionUser);
      setToken(token);
      localStorage.setItem('user', JSON.stringify(sessionUser));
      if (token) localStorage.setItem('token', token);
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('user');
    localStorage.removeItem('token');
  };

  return (
    <AuthContext.Provider value={{ user, token, isAuthenticated: !!user, isLoading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
