import React, { createContext, useContext, useMemo, useState } from "react";
import { clearAccessToken, getAccessToken, setAccessToken } from "../util/authStorage";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [accessToken, setAccessTokenState] = useState(() => getAccessToken());

  const value = useMemo(
    () => ({
      accessToken,
      isAuthenticated: Boolean(accessToken),
      login: (token) => {
        setAccessToken(token);
        setAccessTokenState(token);
      },
      logout: () => {
        clearAccessToken();
        setAccessTokenState(null);
      },
    }),
    [accessToken]
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
