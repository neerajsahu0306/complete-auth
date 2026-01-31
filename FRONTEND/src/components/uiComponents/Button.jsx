import React from "react";

export default function Button({
  children,
  type = "button",
  bgColor = "bg-white",
  textColor = "text-black",
  className = "",
  loading = false,
  ...props
}) {
  return (
    <button
      type={type}
      disabled={loading}
      className={`
                w-full flex items-center justify-center font-bold py-3.5 rounded-lg 
                transition-all duration-300 ease-out 
                disabled:opacity-50 disabled:cursor-not-allowed 
                shadow-[0_0_20px_rgba(255,255,255,0.1)] hover:shadow-[0_0_25px_rgba(255,255,255,0.2)]
                hover:bg-zinc-200
                ${bgColor} ${textColor} ${className}
            `}
      {...props}
    >
      {loading ? (
        <span className="animate-pulse">Processing...</span>
      ) : (
        children
      )}
    </button>
  );
}




