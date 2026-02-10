import React ,{ useEffect, useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import {MainLayout, Loader} from "./components/index"
import {useAuthStore} from "./store/authStore"
import {authService} from "./auth/index"

function App() {
  // 1. We only need setAuth (it handles both user and boolean now)
  const setAuth = useAuthStore((state) => state.setAuth);

  // REMOVED: const setUser = ... (Your store doesn't export this anymore, which is fine!)
  const logout = useAuthStore((state) => state.logout);

  const [isCheckingSession, setIsCheckingSession] = useState(true);

  const location = useLocation();

  useEffect(() => {

    if (location.pathname.includes("/auth/callback")) {
      setIsCheckingSession(false);
      return;
  }
    const checkSession = async () => {
      try {
        const response = await authService.getCurrentUser();

        // 2. CORRECT USAGE: Pass the data object directly
        // This sets user={...} AND isAuthenticated=true
        setAuth(response.data);
      } catch (e) {
        // 3. Clear everything on failure
        logout();
      } finally {
        setIsCheckingSession(false);
      }
    };
    checkSession();
  }, [setAuth, logout, location.pathname]);

  if (isCheckingSession) {
    return (
    <>
    <Loader/>
    </>
    );
  }

  return (
    <>
      <div className="min-h-screen w-full bg-black relative">
        {/* Stellar Mist */}
        <div
          className="absolute inset-0 z-0"
          style={{
            background: `
       radial-gradient(ellipse 140% 50% at 15% 60%, rgba(124, 58, 237, 0.11), transparent 48%),
       radial-gradient(ellipse 90% 80% at 85% 25%, rgba(245, 101, 101, 0.09), transparent 58%),
       radial-gradient(ellipse 120% 65% at 40% 90%, rgba(34, 197, 94, 0.13), transparent 52%),
       radial-gradient(ellipse 100% 45% at 70% 5%, rgba(251, 191, 36, 0.07), transparent 42%),
       radial-gradient(ellipse 80% 75% at 90% 80%, rgba(168, 85, 247, 0.10), transparent 55%),
       #000000
     `,
          }}
        />
        {/* Your Content/Components */}
        <MainLayout>
          <Outlet />
        </MainLayout>
      </div>
    </>
  );
}

export default App


