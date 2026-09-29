"use client";

import React, { createContext, useContext, useState, useEffect } from "react";
import { User, getStoredToken, setStoredToken, removeStoredToken, authApi } from "@/lib/api";

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  register: (data: { username: string; email: string; password: string; roleCode: string }) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  // Restore session on mount if a real token was saved by a previous login/register — no more
  // auto-logging in as a fake Administrator when there's no session (that bypassed RBAC
  // entirely, since the backend never even saw a request).
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

  const register = async (data: { username: string; email: string; password: string; roleCode: string }) => {
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
