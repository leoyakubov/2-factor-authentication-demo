import { getCurrentUser, login, logout } from "./apiClient";

describe("ApiUtil", () => {
  beforeEach(() => {
    global.fetch = jest.fn();
    document.cookie = "XSRF-TOKEN=test-xsrf";
  });

  afterEach(() => {
    document.cookie = "XSRF-TOKEN=; Max-Age=0";
    jest.restoreAllMocks();
  });

  test("login posts credentials to the signin endpoint with cookies enabled", async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      status: 200,
      text: async () =>
        JSON.stringify({ mfa: false }),
    });

    const response = await login({ username: "demo", password: "secret" });

    expect(fetch).toHaveBeenCalledWith(
      "http://localhost:8081/signin",
      expect.objectContaining({
        method: "POST",
        body: JSON.stringify({ username: "demo", password: "secret" }),
        credentials: "include",
      })
    );
    expect(fetch.mock.calls[0][1].headers.get("Authorization")).toBeNull();
    expect(fetch.mock.calls[0][1].headers.get("X-XSRF-TOKEN")).toBe("test-xsrf");
    expect(response).toEqual({ mfa: false });
  });

  test("getCurrentUser requests the current profile with cookies enabled", async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      status: 200,
      text: async () =>
        JSON.stringify({ username: "demo", name: "Demo User" }),
    });

    const response = await getCurrentUser();

    expect(fetch).toHaveBeenCalledWith(
      "http://localhost:8081/users/me",
      expect.objectContaining({
        method: "GET",
        headers: expect.any(Headers),
        credentials: "include",
      })
    );
    expect(fetch.mock.calls[0][1].headers.get("Authorization")).toBeNull();
    expect(response).toEqual({ username: "demo", name: "Demo User" });
  });

  test("logout posts to the logout endpoint with cookies enabled", async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      status: 204,
      text: async () => "",
    });

    const response = await logout();

    expect(fetch).toHaveBeenCalledWith(
      "http://localhost:8081/logout",
      expect.objectContaining({
        method: "POST",
        credentials: "include",
      })
    );
    expect(fetch.mock.calls[0][1].headers.get("Authorization")).toBeNull();
    expect(fetch.mock.calls[0][1].headers.get("X-XSRF-TOKEN")).toBe("test-xsrf");
    expect(response).toEqual({});
  });
});
