import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthStore } from "../../store/authStore";

function AuthLayout({ children, authentication = true }) {
  const navigate = useNavigate();
  const [loader, setLoader] = useState(true);

 
  const authStatus = useAuthStore((state) => state.isAuthenticated);
  const isCheckingAuth = useAuthStore((state) => state.isCheckingAuth);

  useEffect(() => {
    //  CRITICAL: Do not redirect while the app is verifying the HttpOnly cookie
    if (isCheckingAuth) {
      return;
    }

    //  Routing Logic
    if (authentication && authStatus !== true) {
      // Needed Auth, but not logged in -> Login
      navigate("/sign-in");
    } else if (!authentication && authStatus === true) {
      // Public Page (Login/Signup), but already logged in -> Dashboard
      navigate("/dashboard");
    }

    setLoader(false);
  }, [authStatus, navigate, authentication, isCheckingAuth]);

  // Show Loader if checking auth OR local loader is active
  if (isCheckingAuth || loader) {
    return (
      <div className="flex flex-col justify-center items-center h-[80vh] gap-4">
        {/* Consistent Pulse Animation */}
        <div className="relative">
          <div className="absolute inset-0 rounded-full bg-zinc-500 opacity-20 animate-ping"></div>
          <div className="relative w-3 h-3 bg-zinc-400 rounded-full shadow-[0_0_10px_rgba(255,255,255,0.5)]"></div>
        </div>
        <h1 className="text-zinc-500 text-xs tracking-widest uppercase animate-pulse">
          Loading...
        </h1>
      </div>
    );
  }

  return <>{children}</>;
}

export default AuthLayout;