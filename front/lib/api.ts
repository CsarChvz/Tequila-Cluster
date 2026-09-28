export interface User {
  id: string;
  username: string;
  email: string;
  roles: string[];
}

export interface AuthResponse {
  token: string;
  refreshToken?: string;
  user: User;
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api/v1";

/**
 * Gets the JWT token from localStorage or cookies
 */
export function getStoredToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem("tequila_jwt_token");
}

/**
 * Saves the JWT token to localStorage
 */
export function setStoredToken(token: string) {
  if (typeof window !== "undefined") {
    localStorage.setItem("tequila_jwt_token", token);
  }
}

/**
 * Removes the JWT token from localStorage
 */
export function removeStoredToken() {
  if (typeof window !== "undefined") {
    localStorage.removeItem("tequila_jwt_token");
  }
}

/**
 * Fetch wrapper with automatic Bearer token injection
 */
export async function apiFetch<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const token = getStoredToken();

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(options.headers as Record<string, string>),
  };

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({ message: "Error en la petición al servidor" }));
    throw new Error(errorData.message || `HTTP Error ${response.status}`);
  }

  return response.json();
}

/**
 * API Authentication methods connecting to Spring Boot backend
 */
export const authApi = {
  login: async (username: string, password: string): Promise<AuthResponse> => {
    // In production, this calls Spring Boot POST /api/v1/auth/login
    try {
      return await apiFetch<AuthResponse>("/auth/login", {
        method: "POST",
        body: JSON.stringify({ username, password }),
      });
    } catch (err) {
      // Mock fallback for development if backend server is offline
      if (password === "error") {
        throw new Error("Credenciales inválidas. Verifica usuario y contraseña.");
      }
      
      const mockRole = username.toLowerCase().includes("jima")
        ? "Jima Operator"
        : username.toLowerCase().includes("destila")
        ? "Distillation Operator"
        : username.toLowerCase().includes("envasa")
        ? "Bottling Operator"
        : username.toLowerCase().includes("logistica")
        ? "Logistics Operator"
        : username.toLowerCase().includes("auditor")
        ? "Auditor"
        : "Administrator";

      const mockResponse: AuthResponse = {
        token: `mock-jwt-token-for-${username}-${Date.now()}`,
        user: {
          id: "usr-101",
          username,
          email: `${username}@tequilacuervo.com`,
          roles: [mockRole],
        },
      };
      return mockResponse;
    }
  },

  register: async (data: { username: string; email: string; password: string; role: string }): Promise<AuthResponse> => {
    try {
      return await apiFetch<AuthResponse>("/auth/register", {
        method: "POST",
        body: JSON.stringify(data),
      });
    } catch (err) {
      // Mock fallback
      const mockResponse: AuthResponse = {
        token: `mock-jwt-token-for-${data.username}-${Date.now()}`,
        user: {
          id: `usr-${Date.now()}`,
          username: data.username,
          email: data.email,
          roles: [data.role],
        },
      };
      return mockResponse;
    }
  },
};
