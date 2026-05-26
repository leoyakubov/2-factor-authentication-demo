import React, { useEffect, useState } from "react";
import { Button, Card, Avatar } from "antd";
import { LogoutOutlined, UserOutlined } from "@ant-design/icons";
import { getCurrentUser } from "../util/ApiUtil";
import "./Profile.css";

const { Meta } = Card;

const Profile = (props) => {
  const [currentUser, setCurrentUser] = useState({});

  useEffect(() => {
    if (localStorage.getItem("accessToken") === null) {
      props.history.push("/login");
      return;
    }

    loadCurrentUser();
  }, [props.history]);

  const loadCurrentUser = () => {
    getCurrentUser()
      .then((response) => {
        setCurrentUser(response);
      })
      .catch((error) => {
        if (error.status === 401) {
          logout();
          return;
        }

        console.error(error);
      });
  };

  const logout = () => {
    localStorage.removeItem("accessToken");
    props.history.push("/login");
  };

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
      <Card
        style={{ width: 420, border: "1px solid #e1e0e0" }}
        actions={[
          <Button
            key="logout"
            type="text"
            icon={<LogoutOutlined />}
            onClick={logout}
            className="logout-button"
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
