import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from "react";
import { getCurrentUser, logout as logoutRequest } from "../shared/api/apiClient";
import { logAuthEvent } from "../shared/logging/logger";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isChecking, setIsChecking] = useState(true);
  const [currentUser, setCurrentUser] = useState(null);
  const authCheckVersionRef = useRef(0);

  const refreshAuth = useCallback(async () => {
    const requestVersion = ++authCheckVersionRef.current;
    setIsChecking(true);

    try {
      const user = await getCurrentUser();
      if (requestVersion !== authCheckVersionRef.current) {
        return false;
      }
      setCurrentUser(user || null);
      setIsAuthenticated(true);
      logAuthEvent("session check succeeded");
    } catch (error) {
      if (requestVersion !== authCheckVersionRef.current) {
        return false;
      }
      setIsAuthenticated(false);
      setCurrentUser(null);
      logAuthEvent("session check failed", {
        status: error?.status,
        requestId: error?.requestId,
      });
      return false;
    } finally {
      if (requestVersion === authCheckVersionRef.current) {
        setIsChecking(false);
      }
    }

    return true;
  }, []);

  useEffect(() => {
    refreshAuth();
  }, [refreshAuth]);

  const login = useCallback(async () => {
    authCheckVersionRef.current += 1;
    try {
      const user = await getCurrentUser();
      setCurrentUser(user || null);
      logAuthEvent("session load succeeded after login");
    } catch (error) {
      setCurrentUser(null);
      logAuthEvent("session load failed after login", {
        status: error?.status,
        requestId: error?.requestId,
      });
    } finally {
      setIsAuthenticated(true);
      setIsChecking(false);
    }
  }, []);

  const logout = useCallback(async () => {
    authCheckVersionRef.current += 1;
    try {
      await logoutRequest();
      logAuthEvent("logout request succeeded");
    } catch (error) {
      logAuthEvent("logout request failed, clearing local session", {
        status: error?.status,
        requestId: error?.requestId,
      });
      // Clear local state even if the backend is unavailable.
    } finally {
      setIsAuthenticated(false);
      setIsChecking(false);
      setCurrentUser(null);
    }
  }, []);

  const value = useMemo(
    () => ({
      isAuthenticated,
      isChecking,
      currentUser,
      refreshAuth,
      login,
      logout,
    }),
    [currentUser, isAuthenticated, isChecking, refreshAuth, login, logout]
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
