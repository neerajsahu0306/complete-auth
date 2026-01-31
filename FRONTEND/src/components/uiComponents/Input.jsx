import React, {useId} from "react";

const Input = React.forwardRef( function input( {
    label,
    type = "text",
    className = "",
    ...props
}, ref) {
    const id = useId();
    return (
      <>
        <div className="w-full space-y-2">
          {label && (
            <label
              className="text-xs font-medium text-zinc-500 uppercase tracking-widest inline-block mb-1 pl-1"
              htmlFor={id}
            >
              {label}
            </label>
          )}
          <input
            type={type}
            className={`
                    w-full bg-zinc-900/50 border border-zinc-800 text-white px-4 py-3 rounded-lg 
                    focus:outline-none focus:border-zinc-500 focus:ring-1 focus:ring-zinc-500 
                    transition-all duration-300 placeholder:text-zinc-700
                    disabled:opacity-50 disabled:cursor-not-allowed
                    ${className}
                `}
            ref={ref}
            {...props}
            id={id}
          />
        </div>
      </>
    );
})

export default Input;