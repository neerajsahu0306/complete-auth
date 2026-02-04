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
              className="text-xs font-medium text-zinc-500 uppercase tracking-wide inline-block mb-2 pl-1"
              htmlFor={id}
            >
              {label}
            </label>
          )}
          <input
            type={type}
            className={`
                    w-full  border border-zinc-800 text-white px-4 py-3 rounded-lg 
                    focus:outline-none focus:border-zinc-500 focus:ring-2 focus:ring-zinc-500 focus:scale-102
                    transition-all duration-300 placeholder:text-zinc-700
                    disabled:opacity-50 disabled:cursor-not-allowed text-sm h-10  font-inconsolata
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