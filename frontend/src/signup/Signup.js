import React, { useEffect, useState } from "react";
import { Alert, Form, Input, Button, Checkbox } from "antd";
import { Link } from "react-router-dom";
import { DingtalkOutlined } from "@ant-design/icons";
import { signup } from "../util/ApiUtil";
import { getSignUpErrorMessage } from "../util/authErrors";
import { useAuth } from "../auth/AuthContext";
import "./Signup.css";

const Signup = (props) => {
  const [loading, setLoading] = useState(false);
  const [created, setCreated] = useState(false);
  const [qrImageUrl, setQrImageUrl] = useState();
  const [errorMessage, setErrorMessage] = useState();
  const auth = useAuth();

  useEffect(() => {
    if (auth.isAuthenticated) {
      props.history.push("/");
    }
  }, [auth.isAuthenticated, props.history]);

  const onFinish = (values) => {
    setLoading(true);
    setErrorMessage(undefined);
    signup(values)
      .then((response) => {
        setCreated(true);
        setQrImageUrl(response.mfa ? response.secretImageUri : undefined);
      })
      .catch((error) => {
        setErrorMessage(getSignUpErrorMessage(error));
      })
      .finally(() => setLoading(false));
  };

  return (
    <div className="login-container">
      <DingtalkOutlined style={{ fontSize: 50 }} />
      {created ? (
        <div className="login-form">
          <Alert
            style={{ marginBottom: 16 }}
            type="success"
            showIcon
            message="Account created"
            description="Your account was created successfully. You can log in now."
          />
          {qrImageUrl ? (
            <div style={{ marginBottom: 16, textAlign: "center" }}>
              <p style={{ marginBottom: 8 }}>
                Scan this QR code with your authenticator app before logging in.
              </p>
              <img
                src={qrImageUrl}
                alt="Two-factor authentication QR code"
                style={{ maxWidth: "100%", marginBottom: 16 }}
              />
            </div>
          ) : null}
          <Button
            type="primary"
            shape="round"
            size="large"
            block
            onClick={() => props.history.push("/login")}
          >
            Login
          </Button>
        </div>
      ) : (
        <Form
          name="normal_login"
          className="login-form"
          initialValues={{ remember: true }}
          onFinish={onFinish}
        >
          {errorMessage ? (
            <Alert
              style={{ marginBottom: 16 }}
              type="error"
              showIcon
              message="Sign up failed"
              description={errorMessage}
            />
          ) : null}
          <Form.Item
            name="name"
            rules={[{ required: true, message: "Please input your name!" }]}
          >
            <Input size="large" placeholder="Name" />
          </Form.Item>
          <Form.Item
            name="username"
            rules={[{ required: true, message: "Please input your Username!" }]}
          >
            <Input size="large" placeholder="Username" />
          </Form.Item>
          <Form.Item
            name="email"
            rules={[{ required: true, message: "Please input your email!" }]}
          >
            <Input size="large" placeholder="Email" />
          </Form.Item>
          <Form.Item
            name="password"
            rules={[{ required: true, message: "Please input your Password!" }]}
          >
            <Input size="large" type="password" placeholder="Password" />
          </Form.Item>
          <Form.Item name="mfa" valuePropName="checked">
            <Checkbox>Enable two-factor authentication</Checkbox>
          </Form.Item>
          <Form.Item>
            <Button
              shape="round"
              size="large"
              htmlType="submit"
              className="login-form-button"
              loading={loading}
            >
              Signup
            </Button>
          </Form.Item>
          Already a member? <Link to="/login">Log in</Link>
        </Form>
      )}
    </div>
  );
};

export default Signup;
