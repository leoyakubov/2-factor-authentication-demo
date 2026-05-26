import React, { useEffect, useState } from "react";
import { Alert, Form, Input, Button } from "antd";
import { Link, Redirect } from "react-router-dom";
import {
  UserOutlined,
  LockOutlined,
  DingtalkOutlined,
} from "@ant-design/icons";
import { login } from "../util/ApiUtil";
import "./Signin.css";

const Signin = (props) => {
  const [loading, setLoading] = useState(false);
  const [redirect, setRedirect] = useState();
  const [username, setUsername] = useState();
  const [errorMessage, setErrorMessage] = useState();

  useEffect(() => {
    if (localStorage.getItem("accessToken") !== null) {
      props.history.push("/");
    }
  }, [props.history]);

  const onFinish = (values) => {
    setLoading(true);
    setErrorMessage(undefined);
    login(values)
      .then((response) => {
        if (response.mfa) {
          setUsername(values.username);
          setRedirect("/verify");
        } else {
          localStorage.setItem("accessToken", response.accessToken);
          props.history.push("/");
        }
      })
      .catch((error) => {
        if (error.status === 401) {
          setErrorMessage("We couldn't log you in. Check your username or email and password.");
        } else if (error.status === 404) {
          setErrorMessage("We couldn't find an account with that username or email.");
        } else if (error.status === 400) {
          setErrorMessage("Please check your login details and try again.");
        } else {
          setErrorMessage("We couldn't reach the server. Please try again in a moment.");
        }
        console.error("Sign in failed", error);
      })
      .finally(() => setLoading(false));
  };

  if (redirect) {
    return (
      <Redirect to={{ pathname: redirect, state: { username: username } }} />
    );
  }

  return (
    <div className="login-container">
      <DingtalkOutlined style={{ fontSize: 50 }} />
      <Form
        name="normal_login"
        className="login-form"
        initialValues={{ remember: true }}
        onFinish={onFinish}
      >
        <Form.Item
          name="username"
          rules={[{ required: true, message: "Please input your username or email!" }]}
        >
          <Input
            size="large"
            prefix={<UserOutlined className="site-form-item-icon" />}
            placeholder="Username or email"
          />
        </Form.Item>
        <Form.Item
          name="password"
          rules={[{ required: true, message: "Please input your Password!" }]}
        >
          <Input
            size="large"
            prefix={<LockOutlined className="site-form-item-icon" />}
            type="password"
            placeholder="Password"
          />
        </Form.Item>
        <Form.Item>
          <Button
            shape="round"
            size="large"
            htmlType="submit"
            className="login-form-button"
            loading={loading}
          >
            Log in
          </Button>
        </Form.Item>
        {errorMessage ? (
          <Alert
            style={{ marginBottom: 16 }}
            type="error"
            showIcon
            message="Sign in failed"
            description={errorMessage}
          />
        ) : null}
        Not a member yet? <Link to="/signup">Sign up</Link>
      </Form>
    </div>
  );
};

export default Signin;
