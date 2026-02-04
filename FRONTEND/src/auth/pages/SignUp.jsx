import React, {useState} from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Input, Button } from '../../components'
import{ GoogleOAuthButton, authService} from "../index"
function SignUp() {

    const [formData, setFormData] = useState({
        email: "",
        password: ""
    })

    const [error, setError] = useState("")
    const [loading, setLoading] = useState(false)
    const [showPassword, setShowPassword] = useState(false)
    
    const navigate = useNavigate()

    const gmailRegex = /^[a-zA-Z0-9._%+-]+@gmail\.com$/;
    const passwordRegex =
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z0-9]).{8,}$/;

    const handleChange  = (e) => {
        const {name, value} = e.target
        setFormData((prev) => ({...prev, [name]: value}))
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        setError("")

        if (!gmailRegex.test(formData.email)) {
            setError("Only @gmail.com addresses are allowed.");
            return
        }

        if (!passwordRegex.test(formData.password)) {
          setError(
            "Password must be 8+ chars and include: Uppercase, Lowercase, Number, and a Special Character.",
          );
          return;
        }

        setLoading(true)

        try {
          const response = await authService.signup({
            email: formData.email,
            password: formData.password,
          });

          navigate("/verify-otp", { state: { email: formData.email } });


        } catch (error) {
            setError(
              error.response?.data?.message ||
                "Signup failed. Please try again.",
            );
        } finally {
            setLoading(false)
        }
    }


  return (
    <>
      <div className="flex  h-[calc(100vh-25px)] w-full items-center justify-center p-4 ">
        {/* Glass Container */}
        <div
          className="
                w-full max-w-100
                rounded-3xl 
               
                backdrop-blur-xl 
                
                shadow-[0_5px_10px_0_rgba(43,43,43,0.36)] 
                p-8 
                animate-fade-in-up
            "
        >
          {/* Header */}
          <div className="mb-4 text-center">
            <h4 className="text-xl font-semibold text-white/90 tracking-tight underline decoration-1 underline-offset-5 font-noto">
              Create Account
            </h4>
          </div>

          {/* Error Message */}
          {error && (
            <div className="mb-4 p-3 bg-red-500/10 border border-red-500/20 rounded-lg text-red-400 text-xs text-center animate-shake">
              {error}
            </div>
          )}

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-1">
              <Input
                label="Email"
                type="email"
                name="email"
                placeholder="example@gmail.com"
                value={formData.email}
                onChange={handleChange}
                required
              />
            </div>

            <div className="space-y-1 relative">
              <Input
                label="Password"
                type={showPassword ? "text" : "password"}
                name="password"
                placeholder="password"
                value={formData.password}
                onChange={handleChange}
                required
                className="pr-10"
              />

              <button
                type="button"
                className="absolute right-3 top-9 text-zinc-500 hover:text-zinc-400 transition-colors duration-300 ease-out cursor-pointer"
                onClick={() => setShowPassword(!showPassword)}
              >
                {!showPassword ? (
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    class="icon icon-tabler icons-tabler-outline icon-tabler-eye-closed"
                  >
                    <path stroke="none" d="M0 0h24v24H0z" fill="none" />
                    <path d="M21 9c-2.4 2.667 -5.4 4 -9 4c-3.6 0 -6.6 -1.333 -9 -4" />
                    <path d="M3 15l2.5 -3.8" />
                    <path d="M21 14.976l-2.492 -3.776" />
                    <path d="M9 17l.5 -4" />
                    <path d="M15 17l-.5 -4" />
                  </svg>
                ) : (
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    class="icon icon-tabler icons-tabler-outline icon-tabler-eye"
                  >
                    <path stroke="none" d="M0 0h24v24H0z" fill="none" />
                    <path d="M10 12a2 2 0 1 0 4 0a2 2 0 0 0 -4 0" />
                    <path d="M21 12c-2.4 4 -5.4 6 -9 6c-3.6 0 -6.6 -2 -9 -6c2.4 -4 5.4 -6 9 -6c3.6 0 6.6 2 9 6" />
                  </svg>
                )}
              </button>

              <p className="text-[11px] text-zinc-600 pl-1">
                * 8+ chars, Upper, Lower, Number & Special.
              </p>
            </div>

            <Button
              type="submit"
              loading={loading}
              className="  h-10 text-sm transform hover:scale-[1.01] w-full flex items-center justify-center font-bold py-3.5 rounded-lg 
              transition-all duration-300 ease-out 
              border border-zinc-800  text-zinc-400
              hover:bg-zinc-900 hover:text-zinc-100 hover:border-2 hover:border-zinc-700 
              shadow-none hover:shadow-none cursor-pointer "
            >
              Create Account
            </Button>
          </form>

          {/* Divider */}
          <div className="flex items-center gap-3 my-6 w-full">
            <div className="h-px flex-1 bg-white/10"></div>
            <span className="text-[10px] uppercase tracking-widest text-zinc-600 font-medium">
              Or continue with
            </span>
            <div className="h-px flex-1 bg-white/10"></div>
          </div>

          {/* Google Button */}
          <div className="transform transition-transform hover:scale-[1.01]">
            <GoogleOAuthButton
              text="Sign up with Google"
              className="h-10 text-sm"
            />
          </div>

          {/* Footer */}
          <p className="mt-6 text-center text-xs text-zinc-500">
            Already have an account?{" "}
            <Link
              to="/sign-in"
              className="text-zinc-400 hover:text-white font-medium transition-colors hover:underline  font-roboto"
            >
              Log in
            </Link>
          </p>
        </div>
      </div>
    </>
  );
}

export default SignUp