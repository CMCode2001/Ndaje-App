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
  updateUser: (data: Partial<User>) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const savedUser = localStorage.getItem('user');
    if (savedUser) {
      setUser(JSON.parse(savedUser));
    }
    // Token is now managed securely by HttpOnly cookies
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
        credentials: 'include',
        body: JSON.stringify(credentials),
      });

      const responseData = await response.json().catch(() => ({}));

      if (!response.ok) {
        throw new Error(responseData.message || 'Identifiants invalides');
      }

      const token = responseData.accessToken || responseData.token;
      const userId = responseData.id || responseData.user?.id || (token ? decodeJWT(token)?.sub : null);

      if (!token || !userId) {
        throw new Error('Informations de session incomplètes');
      }

      // Récupérer le profil complet depuis le backend pour avoir tous les champs (téléphone, etc.)
      let fullUserData = responseData.user || responseData.data || responseData;
      try {
        const profileResponse = await fetch(`${BASE_URL_USERS}/${userId}`, {
          // Token is now automatically sent via HttpOnly Cookie
          credentials: 'include'
        });
        if (profileResponse.ok) {
          fullUserData = await profileResponse.json();
        }
      } catch (err) {
        console.warn("Could not fetch full profile, falling back to login data", err);
      }

      const decoded = decodeJWT(token);
      const realmRoles = decoded?.realm_access?.roles || [];
      const detectedRole = realmRoles.find((r: string) => ['ADMIN', 'DRIVER', 'PASSAGER', 'PASSENGER'].includes(r)) || fullUserData.role || '';
      const normalizedRole = detectedRole === 'PASSENGER' ? 'PASSAGER' : detectedRole;

      const sessionUser = {
        ...fullUserData,
        id: userId,
        role: normalizedRole.toUpperCase(),
        // Assurer que les champs de base sont présents
        prenom: fullUserData.prenom || decoded?.given_name || '',
        nom: fullUserData.nom || decoded?.family_name || '',
        email: fullUserData.email || decoded?.email || ''
      };

      setUser(sessionUser);
      setToken(token); // On conserve en mémoire (state React) pour affichage si besoin, mais plus dans localStorage
      localStorage.setItem('user', JSON.stringify(sessionUser));
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
        credentials: 'include',
        body: JSON.stringify(data),
      });

      const result = await response.json().catch(() => ({}));

      if (!response.ok) {
        throw new Error(result.message || 'Erreur lors de l’inscription');
      }

      const token = result.accessToken || result.token;
      const userId = result.id || result.user?.id || (token ? decodeJWT(token)?.sub : null);
      const userData = result.user || result.data || result;
      const sessionUser = {
        ...userData,
        id: userId,
        role: (userData.role || '').toUpperCase()
      };

      setUser(sessionUser);
      setToken(token);
      localStorage.setItem('user', JSON.stringify(sessionUser));
    } finally {
      setIsLoading(false);
    }
  };

  const updateUser = async (updatedData: Partial<User>) => {
    if (!user || !token) return;
    setIsLoading(true);
    try {
      const response = await fetch(`${BASE_URL_USERS}/${user.id}/profile`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        credentials: 'include',
        body: JSON.stringify(updatedData),
      });

      const responseData = await response.json().catch(() => ({}));
      console.log("DEBUG: Profile Update Response:", responseData);

      if (!response.ok) {
        throw new Error(responseData.message || 'Erreur lors de la mise à jour');
      }

      // Utiliser la réponse du backend si elle contient l'utilisateur, sinon merger localement
      const updatedUserFromBackend = responseData.user || responseData.data || responseData;

      const sessionUser = {
        ...user, // Garder les infos actuelles (comme l'ID) si elles manquent dans la réponse
        ...updatedUserFromBackend,
        role: (updatedUserFromBackend.role || user.role || '').toUpperCase()
      };

      console.log("DEBUG: New Session User State:", sessionUser);

      setUser(sessionUser);
      localStorage.setItem('user', JSON.stringify(sessionUser));
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('user');

    // Notify backend to clear cookies and invalidate refresh token
    fetch(`${BASE_URL_USERS}/logout`, {
      method: 'POST',
      credentials: 'include'
    }).catch(err => console.error("Logout API failed", err));
  };

  return (
    <AuthContext.Provider value={{ user, token, isAuthenticated: !!user, isLoading, login, register, updateUser, logout }}>
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
