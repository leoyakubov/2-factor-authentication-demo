import { useCallback } from "react";
import { Avatar, Button, Card, Divider, Tag, Typography } from "antd";
import { useNavigate } from "react-router-dom";
import { LogoutOutlined, MailOutlined, UserOutlined } from "@ant-design/icons";
import { useAuth } from "../auth/AuthContext";
import "./Profile.css";

const { Text, Title } = Typography;

const Profile = () => {
  const auth = useAuth();
  const navigate = useNavigate();
  const currentUser = auth.currentUser || {};

  const logout = useCallback(() => {
    auth.logout().finally(() => {
      navigate("/login", { replace: true });
    });
  }, [auth, navigate]);

  const getInitials = () => {
    const label = currentUser.name || currentUser.username || "User";
    return label
      .split(" ")
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0].toUpperCase())
      .join("");
  };

  return (
    <div className="profile-container">
      <Card className="profile-card">
        <div className="profile-card-hero">
          <Avatar
            size={104}
            src={currentUser.profilePicture}
            className="user-avatar-circle"
            icon={currentUser.profilePicture ? <UserOutlined /> : undefined}
            aria-label="User avatar"
          >
            {!currentUser.profilePicture ? getInitials() : null}
          </Avatar>
          <div className="profile-card-copy">
            <Text className="profile-eyebrow">Signed in</Text>
            <Title level={3} className="profile-name">
              {currentUser.name || currentUser.username || "Signed in user"}
            </Title>
            <div className="profile-summary">
              <Text className="profile-handle">
                {currentUser.username ? `@${currentUser.username}` : "Username not available"}
              </Text>
              <Text className="profile-email">
                <MailOutlined /> {currentUser.email || "Email not available"}
              </Text>
              <div className="profile-security-status">
                <Text className="profile-detail-label">Two-factor authentication</Text>
                <Tag color={currentUser.mfaEnabled ? "green" : "default"} className="mfa-status-tag">
                  {currentUser.mfaEnabled ? "Enabled" : "Disabled"}
                </Tag>
              </div>
            </div>
          </div>
        </div>

        <Divider />

        <div className="profile-details">
          <div className="profile-detail-row">
            <Text className="profile-detail-label">Display name</Text>
            <Text className="profile-detail-value">{currentUser.name || "Not set"}</Text>
          </div>
          <div className="profile-detail-row">
            <Text className="profile-detail-label">Username</Text>
            <Text className="profile-detail-value">
              {currentUser.username ? `@${currentUser.username}` : "Not set"}
            </Text>
          </div>
          <div className="profile-detail-row">
            <Text className="profile-detail-label">Email</Text>
            <Text className="profile-detail-value">{currentUser.email || "Not set"}</Text>
          </div>
          <div className="profile-detail-row">
            <Text className="profile-detail-label">Two-factor authentication</Text>
            <Text className="profile-detail-value">
              {currentUser.mfaEnabled ? "Enabled" : "Disabled"}
            </Text>
          </div>
        </div>

        <div className="profile-actions">
          <Button
            key="logout"
            type="primary"
            danger
            icon={<LogoutOutlined />}
            onClick={logout}
            className="logout-button"
            aria-label="Logout"
          >
            Logout
          </Button>
        </div>
      </Card>
    </div>
  );
};

export default Profile;
