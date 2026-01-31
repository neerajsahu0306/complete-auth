import React, {useState} from 'react'
import { useAuthStore } from '../../store/authStore'
import {authService, GoogleOAuthButton} from "../index"
import axiosConfig from '../../api/api.config'
import {Input, Button} from "../../components/index"
import { Link, useNavigate } from 'react-router-dom'


function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const navigate = useNavigate();

  
  const setAuth = useAuthStore((state) => state.setAuth);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const response = await authService.login({
        email: email,
        password: password,
      });

      const { accessToken } = response.data;

      if (axiosConfig.setToken) {
        axiosConfig.setToken(accessToken);
      }

      const userRes = await authService.getCurrentUser();

     
      setAuth(userRes.data);

      navigate("/dashboard", { replace: true });
    } catch (err) {
      const msg = err.response?.data?.message || "Invalid credentials";
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <div className="flex justify-center items-center min-h-[80vh] w-full animate-fade-in-up">
        <div className="w-full max-w-100 p-8">
          <div className="mb-10 text-center">
            <h1 className="text-3xl font-bold text-white tracking-tight">
              Welcome back!
            </h1>
            <p className="text-zinc-500 mt-2 text-sm">
              Please Enter your credentials .
            </p>
          </div>

          {error && (
            <div className="mb-6 p-4 bg-red-500/5 border border-red-500/20 rounded-lg text-red-400 text-sm text-center">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="space-y-1">
              <Input
                label="Email"
                placeholder="name@gmail.com"
                type="email"
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

            <div className="space-y-2">
              <div className="flex justify-between items-center px-1">
                <label className="text-xs font-medium text-zinc-500 uppercase tracking-widest">
                  Password
                </label>
                <Link
                  to="/forgot-password"
                  className="text-xs text-zinc-500 hover:text-white transition-colors"
                >
                  Forgot?
                </Link>
              </div>
              <Input
                type="password"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>

            <Button type="submit" loading={loading}>
              Sign In
            </Button>
          </form>

          <div className="relative my-10">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-zinc-800"></div>
            </div>
            <div className="relative flex justify-center text-xs uppercase tracking-widest">
              <span className="bg-black px-4 text-zinc-600">
                Or continue with
              </span>
            </div>
          </div>

          <GoogleOAuthButton text="Sign in with Google" />

          <p className="mt-10 text-center text-sm text-zinc-600">
            Don't have an account?{" "}
            <Link
              to="/sign-up"
              className="text-zinc-400 hover:text-white underline underline-offset-4 transition-colors"
            >
              Create one
            </Link>
          </p>
        </div>
      </div>
    </>
  );
}

export default Login