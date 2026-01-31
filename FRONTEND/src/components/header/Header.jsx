import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuthStore } from "../../store/authStore";
import axiosConfig from "../../api/api.config";
import { authService } from "../../auth/index";
function Header() {
  const { isAuthenticated, logout } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      // 1. Tell Server to clear HttpOnly Cookie
      await authService.logout();
    } catch (error) {
      console.error("Logout failed:", error);
    } finally {
      // 2. Client-side cleanup (Always run this, even if server errors)
      logout(); // Clear Zustand Store
      axiosConfig.removeToken(); // Clear Axios Memory Token
      navigate("/sign-in");
    }
  };

  return (
    <header className="fixed top-0 w-full z-50 border-b border-white/5 bg-black/60 backdrop-blur-xl transition-all duration-300">
      <div className="container mx-auto px-6 h-16 flex justify-between items-center">
        {/* Logo */}
        <Link
          to="/"
          className="text-lg font-semibold tracking-tight text-white transition-opacity duration-300 hover:opacity-80"
        >
          Nebula<span className="text-zinc-500">Auth</span>
        </Link>

        {/* Navigation */}
        <nav className="flex items-center gap-6 text-sm font-medium">
          <Link
            to="/"
            className="text-zinc-400 hover:text-white transition-colors duration-300 ease-out"
          >
            Home
          </Link>

          {isAuthenticated ? (
            <>
              <Link
                to="/dashboard"
                className="text-zinc-400 hover:text-white transition-colors duration-300 ease-out"
              >
                Dashboard
              </Link>
              <button
                onClick={handleLogout}
                className="text-zinc-400 hover:text-red-400 transition-colors duration-300 ease-out"
              >
                Sign out
              </button>
            </>
          ) : (
            <>
              <Link
                to="/sign-in"
                className="text-zinc-400 hover:text-white transition-colors duration-300 ease-out"
              >
                Log in
              </Link>

              <Link
                to="/sign-up"
                className="
                                  bg-white text-black px-5 py-2 rounded-full 
                                  transition-all duration-300 ease-out
                                  hover:bg-zinc-200 hover:shadow-[0_0_15px_rgba(255,255,255,0.3)] 
                                  active:scale-95
                              "
              >
                Sign Up
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}

export default Header;