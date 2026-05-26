import React, { useEffect, useState } from "react";
import { Alert, Form, Input, Button, notification } from "antd";
import { Redirect } from "react-router-dom";
import { DingtalkOutlined } from "@ant-design/icons";
import { verify } from "../util/ApiUtil";
import { getVerifyErrorMessage } from "../util/authErrors";
import { useAuth } from "../auth/AuthContext";
import "./VerifyCode.css";

const VerifyCode = (props) => {
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState();
  const username = props.location?.state?.username;
  const auth = useAuth();

  useEffect(() => {
    if (!username) {
      props.history.replace("/login");
    }
  }, [props.history, username]);

  const onFinish = (values) => {
    setLoading(true);
    setErrorMessage(undefined);

    const verifyRequest = {
      code: values.code,
      username: username,
    };

    verify(verifyRequest)
      .then((response) => {
        auth.login(response.accessToken);
        props.history.push("/");
      })
      .catch((error) => {
        setErrorMessage(getVerifyErrorMessage(error));
        notification.error({
          message: "Error",
          description: error.body?.message || error.message || "Sorry! Something went wrong. Please try again!",
        });
      })
      .finally(() => setLoading(false));
  };

  if (!username) {
    return <Redirect to="/login" />;
  }

  return (
    <div className="login-container">
      <DingtalkOutlined style={{ fontSize: 50 }} />
      {errorMessage ? (
        <Alert
          style={{ marginBottom: 16, maxWidth: 420 }}
          type="error"
          showIcon
          message="Verification failed"
          description={errorMessage}
        />
      ) : null}
      <Form
        name="normal_login"
        className="login-form"
        initialValues={{ remember: true }}
        onFinish={onFinish}
      >
        <Form.Item
          name="code"
          rules={[{ required: true, message: "Code is required" }]}
        >
          <Input size="large" placeholder="Enter code" />
        </Form.Item>
        <Form.Item>
          <Button
            onClick={() => props.history.push("/login")}
            shape="round"
            size="large"
          >
            Cancel
          </Button>
          <Button
            shape="round"
            size="large"
            htmlType="submit"
            className="verify-form-button "
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
