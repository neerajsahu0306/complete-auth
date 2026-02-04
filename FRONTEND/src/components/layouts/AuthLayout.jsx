import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthStore } from "../../store/authStore";
import {Loader} from "../index"
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
   <>
   <Loader/>
   </>
    );
  }

  return <>{children}</>;
}

export default AuthLayout;