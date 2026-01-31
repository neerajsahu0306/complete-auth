import React from 'react'
import {API_CONFIG} from "../../api/constants"
import { Button } from '../../components'
function GoogleOAuthButton({ text = "Continue with Google", className = "" }) {
  
  const GOOGLE_AUTH_URL =
    "https://auth-practice.duckdns.org/oauth2/authorization/google";

  return (
    <>
      <a
        href={GOOGLE_AUTH_URL}
        className={`
        w-full flex items-center justify-center font-bold py-3.5 rounded-lg 
        transition-all duration-300 ease-out 
        border border-zinc-800 bg-zinc-900 text-zinc-300
        hover:bg-zinc-800 hover:text-white hover:border-zinc-700 
        shadow-none hover:shadow-none
        ${className}
      `}
      >
        <img
          src="https://www.svgrepo.com/show/475656/google-color.svg"
          alt="Google"
          className="w-5 h-5 mr-3"
        />
        {text}
      </a>
    </>
  );
}

export default GoogleOAuthButton



