import { useEffect, useState } from "react";
import { Alert, Form, Input, Button } from "antd";
import { Link, useNavigate } from "react-router-dom";
import {
  UserOutlined,
  LockOutlined,
  DingtalkOutlined,
} from "@ant-design/icons";
import { login } from "../shared/api/apiClient";
import { getSignInErrorMessage } from "../shared/auth/authErrors";
import { useAuth } from "../auth/AuthContext";
import "./Signin.css";

const Signin = () => {
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState();
  const auth = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!auth.isChecking && auth.isAuthenticated) {
      navigate("/", { replace: true });
    }
  }, [auth.isAuthenticated, auth.isChecking, navigate]);

  const onFinish = (values) => {
    setLoading(true);
    setErrorMessage(undefined);
    login(values)
      .then((response) => {
        if (response.mfa) {
          navigate("/verify", { state: { username: values.username } });
        } else {
          auth.login();
          navigate("/", { replace: true });
        }
      })
      .catch((error) => {
        setErrorMessage(getSignInErrorMessage(error));
      })
      .finally(() => setLoading(false));
  };

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
            aria-label="Username or email"
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
            aria-label="Password"
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
