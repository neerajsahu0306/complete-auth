import React, { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { Input, Button } from "../../components/index";
import { authService } from "../index";



function ResetPassword() {
  const navigate = useNavigate();
  const location = useLocation();


  const email = location.state?.email || "";

  const [otp, setOtp] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const passwordRegex =
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z0-9]).{8,}$/;

  useEffect(() => {
    if (!email) {
      navigate("/forgot-password");
    }
  }, [email, navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");

    // 1. Validate Password Strength locally
    if (!passwordRegex.test(newPassword)) {
      setError(
        "Password must be 8+ chars and include: Uppercase, Lowercase, Number, and a Special Character.",
      );
      return;
    }

    // 2. Validate OTP length locally
    if (otp.length !== 6) {
      setError("Please enter a valid 6-digit code.");
      return;
    }

    setLoading(true);

    try {
     
      await authService.resetPassword({
        email: email,
        otp: otp,
        newPassword: newPassword,
      });

      setMessage("Password reset successfully! Redirecting to login...");

      // Redirect after delay
      setTimeout(() => {
        navigate("/sign-in");
      }, 2000);
    } catch (error) {
      // Handle Backend Errors (String or Object)
      const errorMsg =
        error.response?.data?.message ||
        (typeof error.response?.data === "string"
          ? error.response?.data
          : "Failed to reset password. Please try again.");
      setError(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex justify-center items-center min-h-[80vh] w-full animate-fade-in-up">
      <div className="w-full max-w-100 p-8">
        <div className="mb-8 text-center">
          <h1 className="text-3xl font-bold text-white tracking-tight">
            Set New Password
          </h1>
          <p className="text-zinc-500 mt-2 text-sm">
            Enter the code sent to <span className="text-white">{email}</span>
          </p>
        </div>

        {message && (
          <div className="mb-6 p-4 bg-emerald-500/10 border border-emerald-500/20 rounded-lg text-emerald-400 text-xs text-center animate-pulse">
            {message}
          </div>
        )}

        {error && (
          <div className="mb-6 p-4 bg-red-500/5 border border-red-500/20 rounded-lg text-red-400 text-xs text-center animate-shake">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-1">
            <Input
              label="Verification Code"
              placeholder="0 0 0 0 0 0"
              value={otp}
              onChange={(e) => setOtp(e.target.value)}
              className="text-center tracking-[0.5em] font-mono text-lg"
              maxLength={6}
              required
            />
          </div>

          <div className="space-y-1 relative">
            <Input
              label="New Password"
              type={showPassword ? "text" : "password"}
              placeholder="Enter new password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
              className="pr-10"
            />
            <button
              type="button"
              className="absolute right-3 top-9 text-zinc-500 hover:text-zinc-400 transition-colors duration-300 ease-out cursor-pointer"
              onClick={() => setShowPassword(!showPassword)}
            >
              {!showPassword ? (
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  class="icon icon-tabler icons-tabler-outline icon-tabler-eye-closed"
                >
                  <path stroke="none" d="M0 0h24v24H0z" fill="none" />
                  <path d="M21 9c-2.4 2.667 -5.4 4 -9 4c-3.6 0 -6.6 -1.333 -9 -4" />
                  <path d="M3 15l2.5 -3.8" />
                  <path d="M21 14.976l-2.492 -3.776" />
                  <path d="M9 17l.5 -4" />
                  <path d="M15 17l-.5 -4" />
                </svg>
              ) : (
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  class="icon icon-tabler icons-tabler-outline icon-tabler-eye"
                >
                  <path stroke="none" d="M0 0h24v24H0z" fill="none" />
                  <path d="M10 12a2 2 0 1 0 4 0a2 2 0 0 0 -4 0" />
                  <path d="M21 12c-2.4 4 -5.4 6 -9 6c-3.6 0 -6.6 -2 -9 -6c2.4 -4 5.4 -6 9 -6c3.6 0 6.6 2 9 6" />
                </svg>
              )}
            </button>
            <p className="text-[10px] text-zinc-600 pl-1 leading-relaxed">
              must include 8+ chars, Upper, Lower, Number & Special. char.
            </p>
          </div>

          <Button
            type="submit"
            loading={loading}
            className=" bg-transparent text-white border border-white hover:bg-white/90 hover:text-black"
          >
            Reset Password
          </Button>
        </form>
      </div>
    </div>
  );
}

export default ResetPassword;