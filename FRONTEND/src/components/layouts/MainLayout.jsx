import React from 'react'
import {Header, Footer} from "../index"
function MainLayout({children}) {
  return (
    <>
      <div className="flex flex-col min-h-screen relative z-10">
        <Header />

        
        <main className="flex-1 w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          {children}
        </main>

        <Footer />
      </div>
    </>
  );
}

export default MainLayout