import React ,{ useEffect, useState } from 'react'
import { Outlet } from 'react-router-dom'
import {MainLayout} from "./components/index"
import {useAuthStore} from "./store/authStore"
import {authService} from "./auth/index"

function App() {
  // 1. We only need setAuth (it handles both user and boolean now)
  const setAuth = useAuthStore((state) => state.setAuth);

  // REMOVED: const setUser = ... (Your store doesn't export this anymore, which is fine!)
  const logout = useAuthStore((state) => state.logout);

  const [isCheckingSession, setIsCheckingSession] = useState(true);

  useEffect(() => {
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
  }, [setAuth, logout]);

  if (isCheckingSession) {
    return (
      <div className="flex justify-center items-center h-screen bg-black text-white">
        <div className="flex flex-col items-center gap-4">
          <div className="w-8 h-8 border-t-2 border-white rounded-full animate-spin"></div>
          <p className="animate-pulse text-zinc-500 text-sm tracking-widest">
            LOADING SESSION...
          </p>
        </div>
      </div>
    );
  }

  return (
    <>
      <div className="min-h-screen w-full bg-black relative overflow-hidden">
        <div
          className="absolute inset-0 z-0 pointer-events-none"
          style={{
            background: `
       radial-gradient(
         circle at top,
         rgba(255, 255, 255, 0.08) 0%,
         rgba(255, 140, 250, 0.08) 20%,
         rgba(0, 0, 0, 0.0) 60%
       )
     `,
          }}
        />
        {/* Your Content Here */}

        <MainLayout>
          <Outlet />
        </MainLayout>
      </div>
    </>
  );
}

export default App
