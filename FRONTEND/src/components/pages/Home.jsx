import React from 'react'
import { Link } from 'react-router-dom'
import {Button} from  "../index"
function Home() {
  return (
    <>
      <div className="flex flex-col items-center justify-center text-center space-y-8 mt-20 animate-fade-in-up">
        <h1 className="text-6xl font-bold text-white tracking-tight">
          Secure{" "}
          <span className="text-transparent bg-clip-text bg-linear-to-r from-cyan-400 to-purple-600">
            Authentication
          </span>
        </h1>
        <p className="text-zinc-400 max-w-2xl text-lg">
          A full-stack production ready authentication system featuring JWTs,
          HttpOnly Cookies, Spring Boot Security, and Gmail OTP verification.
        </p>
        <div className="flex gap-4">
          <Link to="/sign-up">
            <Button className="px-8 py-3 text-lg">Create Account</Button>
          </Link>
          <Link to="/sign-in">
            <button className="px-8 py-3 text-zinc-400 hover:text-white border border-zinc-800 rounded-lg hover:border-zinc-600 transition-all">
              Login
            </button>
          </Link>
        </div>
      </div>
    </>
  );
}

export default Home