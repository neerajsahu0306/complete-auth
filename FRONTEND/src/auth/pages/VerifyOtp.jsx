import React, { useState, useEffect } from "react";
import { useNavigate, useLocation, Link } from "react-router-dom";
import { Input, Button } from "../../components";
import { authService } from "../index";
import { useAuthStore } from "../../store/authStore";
import axiosConfig from "../../api/api.config";
function VerifyOtp() {
  const navigate = useNavigate();
  const location = useLocation();

  const setAuth = useAuthStore((state) => state.setAuth);
 

  const email = location.state?.email || "";
  const [otp, setOtp] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  // Timer State
  const [timer, setTimer] = useState(30);
  const [canResend, setCanResend] = useState(false);

  // 1. Protection: Bounce back if no email
  useEffect(() => {
    if (!email) {
      navigate("/sign-up", { replace: true });
    }
  }, [email, navigate]);

  // 2. Timer Logic
  useEffect(() => {
    let interval;
    if (timer > 0) {
      interval = setInterval(() => {
        setTimer((prev) => prev - 1);
      }, 1000);
    } else {
      setCanResend(true);
    }
    return () => clearInterval(interval);
  }, [timer]);

  // 3. Helper to handle OTP Change + Auto Submit
  const handleOtpChange = (e) => {
    const value = e.target.value.replace(/\D/g, ""); // Allow only numbers
    if (value.length <= 6) {
      setOtp(value);
      // Optional: Auto-submit when 6 digits are reached
      if (value.length === 6) {
        handleVerify(null, value);
      }
    }
  };

  const handleVerify = async (e, specificOtp = null) => {
    if (e) e.preventDefault();
    const otpToVerify = specificOtp || otp;

    if (otpToVerify.length !== 6) {
      setError("Please enter a valid 6-digit code");
      return;
    }

    setError("");
    setMessage("");
    setLoading(true);

    try {
     
      const response = await authService.verifyOtp({
        email: email,
        otp: otpToVerify,
      });

      const { accessToken } = response.data;

      // Update Axios Singleton immediately
      axiosConfig.setToken(accessToken);

      // Fetch User & Update Store
      const userRes = await authService.getCurrentUser();

      
      setAuth(userRes.data);

      navigate("/dashboard", { replace: true });
    } catch (err) {
     
      const msg = err.response?.data?.message || "Invalid or expired OTP";
      setError(msg);
      // Optional: Clear OTP on failure so they can retry easily
      setOtp("");
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    if (!canResend) return;

    setError("");
    setMessage("");
  

    try {
      
      await authService.resendOtp({ email: email });

      setMessage("A new code has been sent to your inbox.");
      setTimer(30);
      setCanResend(false);
    } catch (err) {
      setError("Failed to resend OTP. Please try again.");
    }
  };

  // If email is missing, render nothing while redirecting (prevents UI flash)
  if (!email) return null;

  return (
    <div className="flex justify-center items-center min-h-[80vh] w-full animate-fade-in-up">
      <div className="w-full max-w-100 p-8">
        <div className="mb-8 text-center">
          <h1 className="text-3xl font-bold text-white tracking-tight">
            Check your inbox
          </h1>
          <p className="text-zinc-500 mt-2 text-sm">
            We sent a code to{" "}
            <span className="text-white font-medium">{email}</span>
          </p>
        </div>

        {/* Success Message */}
        {message && (
          <div className="mb-6 p-4 bg-emerald-500/10 border border-emerald-500/20 rounded-lg text-emerald-400 text-xs text-center">
            {message}
          </div>
        )}

        {/* Error Message */}
        {error && (
          <div className="mb-6 p-4 bg-red-500/10 border border-red-500/20 rounded-lg text-red-400 text-xs text-center animate-shake">
            {error}
          </div>
        )}

        <form onSubmit={handleVerify} className="space-y-6">
          <div className="space-y-1 opacity-60 pointer-events-none">
            <Input
              label="Email"
              value={email}
              readOnly
              tabIndex={-1} // Skip tabbing
              className="bg-zinc-900/30 border-zinc-800 text-zinc-500"
            />
          </div>

          <div className="space-y-1">
            <Input
              label="Verification Code"
              placeholder="0 0 0 0 0 0"
              value={otp}
              onChange={handleOtpChange}
              className="text-center tracking-[0.5em] font-mono text-xl py-4 placeholder:tracking-[0.5em]"
              maxLength={6}
              autoFocus // Focus automatically on load
            />
          </div>

          <Button type="submit" loading={loading} disabled={otp.length !== 6}>
            Verify & Login
          </Button>
        </form>

        <div className="mt-8 flex flex-col items-center gap-4 text-sm text-zinc-600">
          <p className="flex items-center gap-1">
            Didn't receive it?{" "}
            <button
              onClick={handleResend}
              disabled={!canResend}
              className={`
                underline underline-offset-4 transition-colors
                ${
                  canResend
                    ? "text-zinc-400 hover:text-white cursor-pointer"
                    : "text-zinc-700 cursor-not-allowed no-underline opacity-50"
                }
              `}
            >
              {canResend ? "Resend code" : `Resend in ${timer}s`}
            </button>
          </p>

          <Link
            to="/sign-up"
            className="text-xs text-zinc-500 hover:text-red-400 transition-colors"
          >
            Wrong email? Create new account
          </Link>
        </div>
      </div>
    </div>
  );
}

export default VerifyOtp;