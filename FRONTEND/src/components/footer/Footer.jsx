import React from 'react'
import { Link } from 'react-router-dom'
function Footer() {
  return (
    <>
      <footer className="w-full py-8 mt-auto border-t border-white/5  z-10 relative">
        <div className="container mx-auto px-6 flex flex-col md:flex-row justify-between items-center text-sm">
          <p className="text-zinc-600 mb-4 md:mb-0">
            © {new Date().getFullYear()} All rights reserved.
          </p>

          <div className="flex gap-8">
            <Link
              to="#"
              className="text-zinc-500 hover:text-zinc-200 transition-colors duration-300 ease-out"
            >
              Privacy Policy
            </Link>
            <Link
              to="#"
              className="text-zinc-500 hover:text-zinc-200 transition-colors duration-300 ease-out"
            >
              Terms of Service
            </Link>
            <a
              href="https://github.com/neerajsahu0306/complete-auth"
              target="_blank"
              rel="noopener noreferrer"
              className="text-zinc-500 hover:text-white transition-colors duration-300 ease-out"
            >
              GitHub
            </a>
          </div>
        </div>
      </footer>
    </>
  );
}

export default Footer