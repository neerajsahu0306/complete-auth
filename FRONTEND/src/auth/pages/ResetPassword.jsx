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

          <div className="space-y-1">
            <Input
              label="New Password"
              type="password"
              placeholder="Strong password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
            />
            <p className="text-[10px] text-zinc-600 pl-1 leading-relaxed">
              * Must include uppercase, number & special char.
            </p>
          </div>

          <Button type="submit" loading={loading}>
            Reset Password
          </Button>
        </form>
      </div>
    </div>
  );
}

export default ResetPassword;