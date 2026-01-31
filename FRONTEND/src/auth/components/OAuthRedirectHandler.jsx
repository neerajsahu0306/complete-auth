import React, { useEffect, useRef } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useAuthStore } from "../../store/authStore";
import authService from "../AuthService";
import axiosConfig from "../../api/api.config";

/**
 * DEV NOTE: OAuth Landing Page
 * ----------------------------
 * The user never actually "sees" this page. It is a logic processing station.
 * * REACT STRICT MODE FIX:
 * React 18 mounts components twice in dev mode. This causes the code to try
 * processing the token twice (which might fail).
 * We use 'processed.current' (useRef) to ensure it only runs once.
 */
function OAuthRedirectHandler() {
  const navigate = useNavigate();
  const location = useLocation();
  const processed = useRef(false);

  const setAuth = useAuthStore((state) => state.setAuth);

  useEffect(() => {
    // 1. Prevent double execution in React Strict Mode
    if (processed.current) return;
    processed.current = true;

    const params = new URLSearchParams(location.search);
    const accessToken = params.get("access");
    const error = params.get("error");

    if (accessToken) {
      // 1. Prime the Axios Instance
      axiosConfig.setToken(accessToken);

      /// 2. Validate & Store User
      authService
        .getCurrentUser()
        .then((response) => {
          setAuth(response.data);

          // "replace: true" prevents the user from clicking "Back" to this loading screen
          navigate("/dashboard", { replace: true });
        })
        .catch((err) => {
          console.error("Failed to fetch user context:", err);
          navigate("/sign-in", { replace: true });
        });
    } else {
      console.error("OAuth Error or Missing Token:", error);
      navigate("/sign-in", { replace: true });
    }
  }, [location, navigate, setAuth]);
  return (
    <>
      <div className="flex h-[80vh] w-full items-center justify-center">
        <div className="relative flex flex-col items-center gap-6">
          <div className="relative">
            <div className="absolute inset-0 rounded-full bg-white opacity-20 animate-ping"></div>
            <div className="relative w-4 h-4 bg-white rounded-full shadow-[0_0_15px_rgba(255,255,255,0.5)]"></div>
          </div>

          <p className="text-zinc-500 text-xs font-medium tracking-[0.2em] uppercase animate-pulse">
            Authenticating
          </p>
        </div>
      </div>
    </>
  );
}

export default OAuthRedirectHandler;
