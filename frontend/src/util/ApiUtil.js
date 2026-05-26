const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || "http://localhost:8081";

const parseBody = async (response) => {
  const text = await response.text();

  if (!text) {
    return {};
  }

  try {
    return JSON.parse(text);
  } catch (error) {
    return { message: text };
  }
};

const request = async (options) => {
  const headers = new Headers();

  if (options.setContentType !== false) {
    headers.append("Content-Type", "application/json");
  }

  const accessToken = localStorage.getItem("accessToken");
  if (accessToken) {
    headers.append("Authorization", `Bearer ${accessToken}`);
  }

  const response = await fetch(options.url, {
    ...options,
    headers,
  });
  const body = await parseBody(response);

  if (!response.ok) {
    const error = new Error(body.message || response.statusText || "Request failed");
    error.status = response.status;
    error.body = body;
    throw error;
  }

  return body;
};

export function login(loginRequest) {
  return request({
    url: `${API_BASE_URL}/signin`,
    method: "POST",
    body: JSON.stringify(loginRequest),
  });
}

export function verify(verifyRequest) {
  return request({
    url: `${API_BASE_URL}/verify`,
    method: "POST",
    body: JSON.stringify(verifyRequest),
  });
}

export function signup(signupRequest) {
  return request({
    url: `${API_BASE_URL}/users`,
    method: "POST",
    body: JSON.stringify(signupRequest),
  });
}

export function getCurrentUser() {
  if (!localStorage.getItem("accessToken")) {
    return Promise.reject(new Error("No access token set."));
  }

  return request({
    url: `${API_BASE_URL}/users/me`,
    method: "GET",
  });
}
