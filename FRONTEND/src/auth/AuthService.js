import axiosConfig from "../api/api.config";
import { API_ENDPOINTS } from "../api/constants";


/**
 * DEV NOTE: Service Layer
 * -----------------------
 * Keeps the UI clean. Components call 'authService.login()', 
 * not 'axios.post("http://...")'.
 */
export class AuthService {
  // --- Authentication Flow ---
  signup(userData) {
    // Expects: { email, password }
    return axiosConfig.post(API_ENDPOINTS.AUTH.SIGNUP, userData);
  }

  verifyOtp(data) {
    // Expects: { email, otp }
    return axiosConfig.post(API_ENDPOINTS.AUTH.VERIFY_OTP, data);
  }

  resendOtp(data) {
    // Expects: { email }
    return axiosConfig.post(API_ENDPOINTS.AUTH.RESEND_OTP, data);
  }

  login(credentials) {
    // Expects: { email, password }
    return axiosConfig.post(API_ENDPOINTS.AUTH.LOGIN, credentials);
  }

  logout() {
    // The backend uses the HttpOnly cookie, so no args needed here.
    return axiosConfig.post(API_ENDPOINTS.AUTH.LOGOUT);
  }

  // --- Password Recovery ---
  forgotPassword(data) {
    // Expects: { email }
    return axiosConfig.post(API_ENDPOINTS.AUTH.FORGOT_PASSWORD, data);
  }

  resetPassword(data) {
    // Expects: { email, otp, newPassword }
    return axiosConfig.post(API_ENDPOINTS.AUTH.RESET_PASSWORD, data);
  }

  // --- Protected Resources ---
  getCurrentUser() {
    return axiosConfig.get(API_ENDPOINTS.USER.HELLO);
  }

  getAdminMessage() {
    return axiosConfig.get(API_ENDPOINTS.ADMIN.HELLO);
  }
}

// Singleton export
const authService = new AuthService();
export default authService;