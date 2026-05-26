import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { getCurrentUser, logout as logoutRequest } from "../util/ApiUtil";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isChecking, setIsChecking] = useState(true);

  const refreshAuth = useCallback(async () => {
    setIsChecking(true);

    try {
      await getCurrentUser();
      setIsAuthenticated(true);
    } catch (error) {
      setIsAuthenticated(false);
      return false;
    } finally {
      setIsChecking(false);
    }

    return true;
  }, []);

  useEffect(() => {
    refreshAuth();
  }, [refreshAuth]);

  const login = useCallback(() => {
    setIsAuthenticated(true);
    setIsChecking(false);
  }, []);

  const logout = useCallback(async () => {
    try {
      await logoutRequest();
    } catch (error) {
      // Clear local state even if the backend is unavailable.
    } finally {
      setIsAuthenticated(false);
      setIsChecking(false);
    }
  }, []);

  const value = useMemo(
    () => ({
      isAuthenticated,
      isChecking,
      refreshAuth,
      login,
      logout,
    }),
    [isAuthenticated, isChecking, refreshAuth, login, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }

  return context;
}
