import React, { Suspense, lazy } from "react";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./auth/AuthContext";
import PrivateRoute from "./auth/PrivateRoute";
import PublicRoute from "./auth/PublicRoute";

import "./App.css";

const Signin = lazy(() => import("./signin/Signin"));
const Signup = lazy(() => import("./signup/Signup"));
const Profile = lazy(() => import("./profile/Profile"));
const QrCode = lazy(() => import("./qrcode/QrCode"));
const VerifyCode = lazy(() => import("./verifycode/VerifyCode"));

const App = () => {
  return (
    <div className="App">
      <AuthProvider>
        <BrowserRouter>
          <Suspense fallback={null}>
            <Routes>
              <Route
                path="/"
                element={
                  <PrivateRoute>
                    <Profile />
                  </PrivateRoute>
                }
              />
              <Route
                path="/login"
                element={
                  <PublicRoute>
                    <Signin />
                  </PublicRoute>
                }
              />
              <Route
                path="/signup"
                element={
                  <PublicRoute>
                    <Signup />
                  </PublicRoute>
                }
              />
              <Route
                path="/qrcode"
                element={
                  <PublicRoute>
                    <QrCode />
                  </PublicRoute>
                }
              />
              <Route
                path="/verify"
                element={
                  <PublicRoute>
                    <VerifyCode />
                  </PublicRoute>
                }
              />
              <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
          </Suspense>
        </BrowserRouter>
      </AuthProvider>
    </div>
  );
};

export default App;
