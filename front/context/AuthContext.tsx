"use client";

import React, { createContext, useContext, useState, useEffect } from "react";
import { User, getStoredToken, setStoredToken, removeStoredToken, authApi } from "@/lib/api";

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  register: (data: { username: string; email: string; password: string; role: string }) => Promise<void>;
  logout: () => void;
  setSimulatedRole: (role: string) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  // Restore session on mount if token exists
  useEffect(() => {
    const savedToken = getStoredToken();
    const savedUserJson = localStorage.getItem("tequila_user_data");
    
    if (savedToken && savedUserJson) {
      try {
        setToken(savedToken);
        setUser(JSON.parse(savedUserJson));
      } catch (e) {
        removeStoredToken();
        localStorage.removeItem("tequila_user_data");
      }
    } else {
      // Default initial mock session for easy testing
      const defaultUser: User = {
        id: "usr-admin-01",
        username: "admin.jcuervo",
        email: "admin@tequilacuervo.com",
        roles: ["Administrator"],
      };
      setUser(defaultUser);
      setToken("mock-initial-token");
    }
    setIsLoading(false);
  }, []);

  const login = async (username: string, password: string) => {
    setIsLoading(true);
    try {
      const res = await authApi.login(username, password);
      setToken(res.token);
      setUser(res.user);
      setStoredToken(res.token);
      localStorage.setItem("tequila_user_data", JSON.stringify(res.user));
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (data: { username: string; email: string; password: string; role: string }) => {
    setIsLoading(true);
    try {
      const res = await authApi.register(data);
      setToken(res.token);
      setUser(res.user);
      setStoredToken(res.token);
      localStorage.setItem("tequila_user_data", JSON.stringify(res.user));
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    removeStoredToken();
    localStorage.removeItem("tequila_user_data");
  };

  const setSimulatedRole = (role: string) => {
    if (user) {
      const updatedUser = { ...user, roles: [role] };
      setUser(updatedUser);
      localStorage.setItem("tequila_user_data", JSON.stringify(updatedUser));
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token && !!user,
        isLoading,
        login,
        register,
        logout,
        setSimulatedRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
