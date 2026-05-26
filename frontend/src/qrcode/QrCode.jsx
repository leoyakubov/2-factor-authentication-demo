import React from "react";
import { Button, Typography } from "antd";
import { Redirect } from "react-router-dom";
import { DingtalkOutlined } from "@ant-design/icons";
import "./QrCode.css";

const QrCode = (props) => {
  const { Title } = Typography;
  const imageUrl = props.location?.state?.imageUrl;

  if (!imageUrl) {
    return <Redirect to="/signup" />;
  }

  return (
    <div className="qrcode-container">
      <DingtalkOutlined style={{ fontSize: 50 }} />
      <Title level={4}>Scan the QR code using an authenticator app</Title>
      <img src={imageUrl} alt="Two-factor authentication QR code" />
      <Button
        onClick={() => props.history.push("/login")}
        shape="round"
        className="login-form-button"
        size="large"
      >
        Continue to login
      </Button>
    </div>
  );
};

export default QrCode;
