import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import Profile from "./Profile";
import { getCurrentUser } from "../util/ApiUtil";

jest.mock("../util/ApiUtil", () => ({
  getCurrentUser: jest.fn(),
}));

describe("Profile", () => {
  const history = {
    push: jest.fn(),
  };

  beforeEach(() => {
    localStorage.clear();
    jest.clearAllMocks();
  });

  const renderComponent = () =>
    render(
      <MemoryRouter>
        <Profile history={history} />
      </MemoryRouter>
    );

  test("redirects to login when there is no access token", async () => {
    renderComponent();

    await waitFor(() => {
      expect(history.push).toHaveBeenCalledWith("/login");
    });
  });

  test("renders the current user and logs out cleanly", async () => {
    const user = userEvent.setup();
    localStorage.setItem("accessToken", "token-123");
    getCurrentUser.mockResolvedValueOnce({
      name: "Galileo Fin",
      username: "galileo",
      profilePicture: undefined,
    });

    renderComponent();

    expect(await screen.findByText("Galileo Fin")).toBeInTheDocument();
    expect(screen.getByText("@galileo")).toBeInTheDocument();
    expect(screen.getByText("GF")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: /logout/i }));

    expect(localStorage.getItem("accessToken")).toBeNull();
    expect(history.push).toHaveBeenCalledWith("/login");
  });

  test("clears the token when the current session is unauthorized", async () => {
    localStorage.setItem("accessToken", "token-123");
    getCurrentUser.mockRejectedValueOnce({ status: 401 });

    renderComponent();

    await waitFor(() => {
      expect(localStorage.getItem("accessToken")).toBeNull();
      expect(history.push).toHaveBeenCalledWith("/login");
    });
  });
});
