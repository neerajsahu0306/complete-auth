import React, {useState} from "react";
import { Link, useNavigate } from "react-router-dom";
import { Input, Button } from "../../components";
import { authService } from "../index";
function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const gmailRegex = /^[a-zA-Z0-9._%+-]+@gmail\.com$/;

  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");

    if (!gmailRegex.test(email)) {
      setError("Only @gmail.com addresses are allowed.");
      return;
    }

    setLoading(true);

    try {
 
      const response = await authService.forgotPassword({ email: email });

      // Backend returns a simple string "OTP sent successfully..."
      setMessage(response.data);

      // Redirect after 2 seconds so user sees the success message
      setTimeout(() => {
        navigate("/reset-password", { state: { email: email } });
      }, 2000);
    } catch (error) {
      // Handle both String errors and JSON Error Objects from Spring Boot
      const errorMsg =
        error.response?.data?.message ||
        (typeof error.response?.data === "string"
          ? error.response?.data
          : "Failed to send OTP. Please try again.");
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
            Reset Password
          </h1>
          <p className="text-zinc-500 mt-2 text-sm">
            Enter your Gmail address to receive a verification code.
          </p>
        </div>

        {/* Success Message */}
        {message && (
          <div className="mb-6 p-4 bg-emerald-500/10 border border-emerald-500/20 rounded-lg text-emerald-400 text-xs text-center animate-pulse">
            {message}
          </div>
        )}

        {/* Error Message */}
        {error && (
          <div className="mb-6 p-4 bg-red-500/5 border border-red-500/20 rounded-lg text-red-400 text-xs text-center animate-shake">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-1">
            <Input
              label="Email"
              type="email"
              placeholder="name@gmail.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            <p className="text-[10px] text-zinc-600 pl-1">
              * Please use your{" "}
              <span className="text-zinc-500 font-medium">@gmail.com</span>{" "}
              address.
            </p>
          </div>

          <Button type="submit" loading={loading}>
            Send Reset Code
          </Button>
        </form>

        <div className="mt-8 text-center">
          <Link
            to="/sign-in"
            className="text-sm text-zinc-500 hover:text-white transition-colors"
          >
            ← Back to Login
          </Link>
        </div>
      </div>
    </div>
  );
}

export default ForgotPassword;