import { useCallback, useEffect, useState } from "react";
import { Alert, Button, Card, Avatar } from "antd";
import { useNavigate } from "react-router-dom";
import { LogoutOutlined, UserOutlined } from "@ant-design/icons";
import { getCurrentUser } from "../shared/api/apiClient";
import { getProfileErrorMessage } from "../shared/auth/authErrors";
import { useAuth } from "../auth/AuthContext";
import "./Profile.css";

const { Meta } = Card;

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
          message="Profile load failed"
          description={errorMessage}
        />
      ) : null}
      <Card
        style={{ width: 420, border: "1px solid #e1e0e0" }}
        actions={[
          <Button
            key="logout"
            type="text"
            icon={<LogoutOutlined />}
            onClick={logout}
            className="logout-button"
            aria-label="Logout"
          >
            Logout
          </Button>,
        ]}
      >
        <Meta
          avatar={
            <Avatar
              size={96}
              src={currentUser.profilePicture}
              className="user-avatar-circle"
              icon={<UserOutlined />}
              aria-label="User avatar"
            >
              {!currentUser.profilePicture ? getInitials() : null}
            </Avatar>
          }
          title={currentUser.name || currentUser.username}
          description={"@" + currentUser.username}
        />
      </Card>
    </div>
  );
};

export default Profile;
