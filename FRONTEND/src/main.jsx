import React from 'react'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import { createBrowserRouter, RouterProvider, Outlet } from 'react-router-dom'

import {SignUp, Login, VerifyOtp, ForgotPassword, ResetPassword, OAuthRedirectHandler} from "./auth/index.js"
import {Home, Dashboard, AuthLayout} from "./components/index.js"


const router = createBrowserRouter([
  {
    path:"/",
    element: <App/>,
    children: [
      {
        element: (
          <AuthLayout authentication= {false}>
            <Outlet />
          </AuthLayout>
        ), children: [
          {path: "/sign-in", element: <Login />},
          {path: "/sign-up", element: <SignUp />},
          {path: "/verify-otp", element: <VerifyOtp />},
          {path: "/forgot-password", element: <ForgotPassword />},
          {path: "/reset-password", element: <ResetPassword />},
        ]
      },


      {
        element: (
          <AuthLayout authentication= {true}>
            <Outlet />
          </AuthLayout>
        ), children: [
          
          {path: "/dashboard", element: <Dashboard />},
        ]
      },


      {path: "/", element: <Home/>},

      {path: "/auth/callback", element: <OAuthRedirectHandler />}

    ]
  }
])

createRoot(document.getElementById('root')).render(
  <StrictMode>
 <RouterProvider router={router}/>
  </StrictMode>,
)
