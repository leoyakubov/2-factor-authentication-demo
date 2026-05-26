import React from "react";
import { BrowserRouter, Redirect, Route, Switch } from "react-router-dom";
import { AuthProvider } from "./auth/AuthContext";
import PrivateRoute from "./auth/PrivateRoute";
import PublicRoute from "./auth/PublicRoute";
import Signin from "./signin/Signin";
import Signup from "./signup/Signup";
import Profile from "./profile/Profile";
import QrCode from "./qrcode/QrCode";
import VerifyCode from "./verifycode/VerifyCode";

import "./App.css";

const App = (props) => {
  return (
    <div className="App">
      <AuthProvider>
        <BrowserRouter>
          <Switch>
            <PrivateRoute exact path="/" component={Profile} />
            <PublicRoute exact path="/login" component={Signin} />
            <PublicRoute exact path="/signup" component={Signup} />
            <PublicRoute exact path="/qrcode" component={QrCode} />
            <PublicRoute exact path="/verify" component={VerifyCode} />
            <Route render={() => <Redirect to="/login" />} />
          </Switch>
        </BrowserRouter>
      </AuthProvider>
    </div>
  );
};

export default App;
