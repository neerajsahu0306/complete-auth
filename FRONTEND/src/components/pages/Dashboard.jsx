import React, { useEffect, useState } from "react";

import { useNavigate } from "react-router-dom";
import { useAuthStore } from "../../store/authStore";
import { authService } from "../../auth/index";
import { Button } from "../index";
import axiosConfig from "../../api/api.config";

function Dashboard() {
  const [welcomeMessage, setWelcomeMessage] = useState("");
  const navigate = useNavigate();

  // Import store actions
  const logout = useAuthStore((state) => state.logout);

  useEffect(() => {
    // Fetch the "Hello User" message from backend
    authService
      .getCurrentUser()
      .then((response) => {
        // Backend returns: { "message": "Hello User..." }
        setWelcomeMessage(response.data.message);
      })
      .catch((error) => {
        console.error("Failed to fetch user data", error);
        // AuthLayout usually handles the redirect, but this is a good safety net
        navigate("/sign-in");
      });
  }, [navigate]);

 

  if (!welcomeMessage) {
    return (
      <div className="flex justify-center items-center min-h-screen bg-black text-white">
        <div className="animate-pulse tracking-widest text-zinc-500 text-sm">
          LOADING...
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-black text-white p-4">
      {/* Animated Welcome Message */}
      <h1 className="text-3xl md:text-5xl font-bold mb-8 text-center animate-fade-in-up bg-linear-to-r from-white to-zinc-500 bg-clip-text text-transparent">
        {welcomeMessage}
      </h1>

      
    </div>
  );
}

export default Dashboard;