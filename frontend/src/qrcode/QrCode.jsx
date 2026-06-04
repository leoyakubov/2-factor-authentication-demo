import { Button, Typography } from "antd";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import { DingtalkOutlined } from "@ant-design/icons";
import "./QrCode.css";

const QrCode = () => {
  const { Title } = Typography;
  const navigate = useNavigate();
  const location = useLocation();
  const imageUrl = location.state?.imageUrl;

  if (!imageUrl) {
    return <Navigate to="/signup" replace />;
  }

  return (
    <div className="qrcode-container">
      <DingtalkOutlined style={{ fontSize: 50 }} />
      <Title level={4}>Scan the QR code using an authenticator app</Title>
      <img src={imageUrl} alt="Two-factor authentication QR code" />
      <Button
        onClick={() => navigate("/login", { replace: true })}
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
