import { useCallback, useEffect, useState } from "react";
import { Alert, Avatar, Button, Card, Divider, Tag, Typography } from "antd";
import { useNavigate } from "react-router-dom";
import { LogoutOutlined, MailOutlined, UserOutlined } from "@ant-design/icons";
import { getCurrentUser } from "../shared/api/apiClient";
import { getProfileErrorMessage } from "../shared/auth/authErrors";
import { useAuth } from "../auth/AuthContext";
import "./Profile.css";

const { Text, Title } = Typography;

const Profile = () => {
  const [currentUser, setCurrentUser] = useState({});
  const [errorMessage, setErrorMessage] = useState();
  const auth = useAuth();
  const navigate = useNavigate();

  const logout = useCallback(() => {
    auth.logout().finally(() => {
      navigate("/login", { replace: true });
    });
  }, [auth, navigate]);

  useEffect(() => {
    if (auth.isChecking) {
      return;
    }

    if (!auth.isAuthenticated) {
      navigate("/login", { replace: true });
      return;
    }

    getCurrentUser()
      .then((response) => {
        setCurrentUser(response);
      })
      .catch((error) => {
        if (error.status === 401) {
          logout();
          return;
        }

        setErrorMessage(getProfileErrorMessage(error));
      });
  }, [auth.isAuthenticated, auth.isChecking, logout, navigate]);

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
      {errorMessage ? (
        <Alert
          style={{ marginBottom: 16, maxWidth: 420 }}
          type="error"
          showIcon
          title="Profile load failed"
          description={errorMessage}
        />
      ) : null}
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
              {currentUser.name || currentUser.username}
            </Title>
            <div className="profile-summary">
              <Text className="profile-handle">@{currentUser.username}</Text>
              <Text className="profile-email">
                <MailOutlined /> {currentUser.email}
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
            <Text className="profile-detail-value">@{currentUser.username}</Text>
          </div>
          <div className="profile-detail-row">
            <Text className="profile-detail-label">Email</Text>
            <Text className="profile-detail-value">{currentUser.email}</Text>
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
