import React from 'react'
import { Link } from 'react-router-dom'
import {Button, Loader} from  "../index"

function Home() {
  return (
    <>
      <div className="flex flex-col items-center justify-center text-center space-y-8 mt-20 animate-fade-in-up">
        <h1
          className="text-6xl    font-roboto text-zinc-500 font-extrabold"
          style={{
            WebkitTextStroke: "2px black",
            textShadow: "3px 3px 6px rgba(120, 120, 120, 0.6)",
          }}
        >
          Secure Authentication
        </h1>

        <p className="text-zinc-400 max-w-2xl text-lg font-inconsolata">
          A full-stack production ready authentication system featuring JWTs,
          HttpOnly Cookies, Spring Boot Security, and Gmail OTP verification.
        </p>
      </div>
    </>
  );
}

export default Home


