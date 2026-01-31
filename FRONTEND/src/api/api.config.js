import axios from "axios";
import { API_CONFIG, API_ENDPOINTS, HTTP_STATUS } from "./constants"

/**
 * DEV NOTE: Axios Wrapper (The Singleton)
 * ---------------------------------------
 * Handles all HTTP traffic.
 * * FEATURES:
 * 1. Auto-injects Access Token.
 * 2. Auto-refreshes Token on 401 errors.
 * 3. Queues requests while refreshing (prevents race conditions).
 */

class AxiosConfig {
  constructor() {
    this.client = axios.create({
      baseURL: API_CONFIG.BASE_URL,
      timeout: API_CONFIG.TIME_OUT,
      withCredentials: true, // REQUIRED: Sends Cookies (Refresh Token) with requests
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
      },
    });

    this.accessToken = null;
    this.isRefreshing = false; // Flags for the "Refresh Dance"
    this.refreshQueue = []; // Holds requests that are waiting for a new token

    this.setupInterceptors();
  }

  setToken(token) {
    this.accessToken = token;
  }

  getToken() {
    return this.accessToken;
  }

  removeToken() {
    this.accessToken = null;
  }

  /**
   * FLUSH QUEUE
   * Called after a token refresh finishes.
   * - If success: 'token' is the new access token.
   * - If error: 'error' is why it failed.
   */
  processQueue(error, token = null) {
    this.refreshQueue.forEach((prom) => {
      if (error) {
        prom.reject(error);
      } else {
        prom.resolve(token);
      }
    });
    this.refreshQueue = [];
  }

  setupInterceptors() {
    // --- REQUEST INTERCEPTOR ---
    // Before any request leaves the browser...
    this.client.interceptors.request.use(
      (config) => {
        const token = this.getToken();
        if (token) {
          // Attach the "ID Card" (Access Token)
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => Promise.reject(error),
    );

    // --- RESPONSE INTERCEPTOR ---
    // When the response comes back...
    this.client.interceptors.response.use(
      (response) => response, // 2xx Success -> Pass through
      async (error) => {
        const originalRequest = error.config;

        // SCENARIO 1: Not a 401 error? -> We can't fix it. Reject.
        if (
          !error.response ||
          error.response.status !== HTTP_STATUS.UNAUTHORIZED
        ) {
          return Promise.reject(error);
        }

        // SCENARIO 2: The Refresh Request itself failed? -> Critical failure. Logout.
        if (originalRequest.url.includes(API_ENDPOINTS.AUTH.REFRESH)) {
          this.handleLogout();
          return Promise.reject(error);
        }

        // SCENARIO 3: Already retried this request? -> Don't loop forever
        if (originalRequest._retry) {
          return Promise.reject(error);
        }

        // SCENARIO 4: Already refreshing? -> Queue this request.
        // If 5 API calls happen at once and token is expired, only the 1st one
        // triggers the refresh. The other 4 wait here.
        if (this.isRefreshing) {
          return new Promise((resolve, reject) => {
            this.refreshQueue.push({
              resolve: (token) => {
                originalRequest.headers.Authorization = `Bearer ${token}`;
                resolve(this.client(originalRequest));
              },
              reject: (err) => reject(err),
            });
          });
        }

        // SCENARIO 5: Token expired. Start the Refresh Process.
        originalRequest._retry = true;
        this.isRefreshing = true;

        try {
          // Call the Backend /refresh endpoint
          // Note: We create a NEW axios instance or use 'axios.post' directly
          // to avoid using the interceptors (infinite loop risk).
          const response = await axios.post(
            `${API_CONFIG.BASE_URL}${API_ENDPOINTS.AUTH.REFRESH}`,
            {},
            { withCredentials: true }, // Send the HttpOnly Cookie
          );

          const newToken = response.data.accessToken;
          this.setToken(newToken);

          // Success! Tell all queued requests "Here is the new token, go!"
          this.processQueue(null, newToken);
          this.isRefreshing = false;

          // Retry the original failed request
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
          return this.client(originalRequest);
        } catch (refreshError) {
          // Refresh failed (Cookie expired? Server down?). Logout user.
          this.processQueue(refreshError, null);
          this.isRefreshing = false;
          this.handleLogout();

          return Promise.reject(refreshError);
        }
      },
    );
  }

  /**
   * GLOBAL LOGOUT TRIGGER
   * Dispatches a custom event that React components (AuthContext) can listen to.
   * This allows us to redirect to /login even from this non-React file.
   */
  handleLogout() {
    this.removeToken();

    window.dispatchEvent(new Event("auth:logout"));
  }

  // Wrapper methods for cleaner usage in React components
  get(url, config = {}) {
    return this.client.get(url, config);
  }
  post(url, data, config = {}) {
    return this.client.post(url, data, config);
  }
  put(url, data, config = {}) {
    return this.client.put(url, data, config);
  }
  delete(url, config = {}) {
    return this.client.delete(url, config);
  }
  patch(url, data, config = {}) {
    return this.client.patch(url, data, config);
  }
}

// Export a single instance (Singleton Pattern)
const axiosConfig= new AxiosConfig();
export default axiosConfig;
