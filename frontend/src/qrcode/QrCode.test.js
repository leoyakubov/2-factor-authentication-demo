import React from "react";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import QrCode from "./QrCode";

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

describe("QrCode", () => {
  const history = {
    push: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test("renders the qr code and continues to login", async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <QrCode
          history={history}
          location={{ state: { imageUrl: "data:image/png;base64,qr-code" } }}
        />
      </MemoryRouter>
    );

    expect(
      screen.getByText("Scan the QR code using an authenticator app")
    ).toBeInTheDocument();
    expect(
      screen.getByAltText("Two-factor authentication QR code")
    ).toHaveAttribute("src", "data:image/png;base64,qr-code");

    await user.click(
      screen.getByRole("button", { name: /continue to login/i })
    );

    expect(history.push).toHaveBeenCalledWith("/login");
  });

  test("redirects to signup when there is no qr image", () => {
    render(
      <MemoryRouter>
        <QrCode history={history} location={{ state: {} }} />
      </MemoryRouter>
    );

    expect(screen.getByTestId("redirect")).toHaveAttribute("data-path", "/signup");
  });
});
