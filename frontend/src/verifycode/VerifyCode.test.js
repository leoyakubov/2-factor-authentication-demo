import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { notification } from "antd";
import VerifyCode from "./VerifyCode";
import { verify } from "../util/ApiUtil";

jest.mock("../util/ApiUtil", () => ({
  verify: jest.fn(),
}));

jest.mock("react-router-dom", () => {
  const actual = jest.requireActual("react-router-dom");

  return {
    ...actual,
    Redirect: ({ to }) => {
      const target = typeof to === "string" ? { pathname: to } : to;

      return (
        <div
          data-testid="redirect"
          data-path={target.pathname}
          data-state={JSON.stringify(target.state || {})}
        />
      );
    },
  };
});

describe("VerifyCode", () => {
  const history = {
    push: jest.fn(),
    replace: jest.fn(),
  };

  beforeEach(() => {
    localStorage.clear();
    jest.clearAllMocks();
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  const renderComponent = (state = { username: "demo" }) =>
    render(
      <MemoryRouter>
        <VerifyCode history={history} location={{ state }} />
      </MemoryRouter>
    );

  test("stores the token and navigates home after a valid code", async () => {
    const user = userEvent.setup();
    verify.mockResolvedValueOnce({ accessToken: "token-456" });

    renderComponent();

    await user.type(screen.getByPlaceholderText("Enter code"), "123456");
    await user.click(screen.getByRole("button", { name: /verify/i }));

    await waitFor(() =>
      expect(verify).toHaveBeenCalledWith({
        username: "demo",
        code: "123456",
      })
    );
    expect(localStorage.getItem("accessToken")).toBe("token-456");
    expect(history.push).toHaveBeenCalledWith("/");
  });

  test("shows a friendly error for an invalid code", async () => {
    const user = userEvent.setup();
    jest.spyOn(notification, "error").mockImplementation(() => {});
    verify.mockRejectedValueOnce({ status: 400 });

    renderComponent();

    await user.type(screen.getByPlaceholderText("Enter code"), "000000");
    await user.click(screen.getByRole("button", { name: /verify/i }));

    expect(
      await screen.findByText(
        "The verification code is incorrect. Please try again."
      )
    ).toBeInTheDocument();
  });

  test("redirects to login when the username is missing", () => {
    renderComponent({});

    expect(screen.getByTestId("redirect")).toHaveAttribute("data-path", "/login");
  });
});
