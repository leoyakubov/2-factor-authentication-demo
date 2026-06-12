import { useEffect, useState } from "react";
import { Alert, Form, Input, Button } from "antd";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import { DingtalkOutlined } from "@ant-design/icons";
import { verify } from "../shared/api/apiClient";
import { getVerifyErrorMessage } from "../shared/auth/authErrors";
import { useAuth } from "../auth/AuthContext";
import "./VerifyCode.css";

const VerifyCode = () => {
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState();
  const navigate = useNavigate();
  const location = useLocation();
  const username = location.state?.username;
  const auth = useAuth();

  useEffect(() => {
    if (!username) {
      navigate("/login", { replace: true });
    }
  }, [navigate, username]);

  const onFinish = async (values) => {
    setLoading(true);
    setErrorMessage(undefined);

    const verifyRequest = {
      code: values.code,
      username: username,
    };

    try {
      await verify(verifyRequest);
      await auth.login();
      navigate("/", { replace: true });
    } catch (error) {
      setErrorMessage(getVerifyErrorMessage(error));
    } finally {
      setLoading(false);
    }
  };

  if (!username) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="login-container">
      <DingtalkOutlined style={{ fontSize: 50 }} />
      {errorMessage ? (
        <Alert
          style={{ marginBottom: 16, maxWidth: 420 }}
          type="error"
          showIcon
          title="Verification failed"
          description={errorMessage}
        />
      ) : null}
      <Form
        name="normal_login"
        className="login-form"
        initialValues={{ remember: true }}
        onFinish={onFinish}
      >
        <p style={{ marginBottom: 16, textAlign: "center" }}>
          Enter the 6-digit code from your authenticator app or one of your recovery codes.
        </p>
        <Form.Item
          name="code"
          rules={[{ required: true, message: "Code is required" }]}
        >
          <Input size="large" aria-label="Verification code" placeholder="Enter code" />
        </Form.Item>
        <Form.Item>
          <Button
            onClick={() => navigate("/login", { replace: true })}
            shape="round"
            size="large"
          >
            Cancel
          </Button>
          <Button
            shape="round"
            size="large"
            htmlType="submit"
            className="verify-form-button"
            loading={loading}
          >
            Verify
          </Button>
        </Form.Item>
      </Form>
    </div>
  );
};

export default VerifyCode;
