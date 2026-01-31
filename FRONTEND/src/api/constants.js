
/**
 * DEV NOTE: API Constants
 * -----------------------
 * Centralized configuration for all network requests.
 * * * API_CONFIG:
 * - BASE_URL: Loaded from .env file (VITE_API_BASE_URL).
 * - RETRY_ATTEMPTS: If a request fails (network error), how many times to try again?
 * * * API_ENDPOINTS:
 * - A map of all backend routes. If the backend changes a URL (e.g. /auth/login -> /v2/auth/login),
 * we only need to update it here, not in 50 different React components.
 */


export const API_CONFIG = {
  BASE_URL: import.meta.env.VITE_API_BASE_URL,
  TIME_OUT: 1000,
  RETRY_ATTEMPTS: 2,
  RETRY_DELAY: 1000,
};

export const API_ENDPOINTS = {
  AUTH: {
    SIGNUP: "/auth/signup",
    VERIFY_OTP: "/auth/verify-otp",
    RESEND_OTP: "/auth/resend-otp",
    LOGIN: "/auth/login",
    REFRESH: "/auth/refresh",
    LOGOUT: "/auth/logout",
    FORGOT_PASSWORD: "/auth/forgot-password",
    RESET_PASSWORD: "/auth/reset-password",
  },
  USER: {
    HELLO: "/user/hello",
  },
  ADMIN: {
    HELLO: "/admin/hello",
  },
};

// Readable aliases for HTTP Status codes (Magic Numbers)
export const HTTP_STATUS = {
  OK: 200,
  CREATED: 201,
  NO_CONTENT: 204,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401, 
  FORBIDDEN: 403, 
  NOT_FOUND: 404,
  CONFLICT: 409, 
  INTERNAL_SERVER_ERROR: 500,
};

export const ERROR_MESSAGES = {
  NETWORK_ERROR: "Unable to connect to the server. Is the backend running?",
  UNAUTHORIZED: "Invalid credentials or session expired. Please login again.",
  FORBIDDEN: "Access Denied. You do not have admin permissions.",
  NOT_FOUND: "The requested resource was not found.",
  SERVER_ERROR: "Something went wrong on our end. Please try again later.",
  VALIDATION_ERROR: "Please check your input fields.",
  INVALID_OTP: "The OTP you entered is invalid or expired.",
  UNKNOWN_ERROR: "An unexpected error occurred.",
};