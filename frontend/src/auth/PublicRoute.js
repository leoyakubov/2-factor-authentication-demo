import React from "react";
import { Redirect, Route } from "react-router-dom";
import { useAuth } from "./AuthContext";

const PublicRoute = ({ component: Component, ...rest }) => {
  const { isAuthenticated, isChecking } = useAuth();

  return (
    <Route
      {...rest}
      render={(props) =>
        isChecking ? null : isAuthenticated ? <Redirect to="/" /> : <Component {...props} />
      }
    />
  );
};

export default PublicRoute;
