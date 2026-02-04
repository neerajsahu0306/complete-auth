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
        border border-zinc-800 bg-zinc-900 text-zinc-400
        hover:bg-transparent hover:text-zinc-100 hover:border-zinc-700 
        shadow-none hover:shadow-none font-roboto
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



