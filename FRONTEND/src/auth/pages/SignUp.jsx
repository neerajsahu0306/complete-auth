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
      <div className="flex justify-center items-center min-h-[80vh] w-full animate-fade-in-up">
        <div className="w-full max-w-100 p-8">
          <div className="mb-10 text-center">
            <h1 className="text-3xl font-bold text-white tracking-tight">
              Create Account
            </h1>
          </div>

          {error && (
            <div className="mb-6 p-4 bg-red-500/5 border border-red-500/20 rounded-lg text-red-400 text-xs text-center animate-shake">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="space-y-1">
              <Input
                label="Email"
                type="email"
                name="email"
                placeholder="name@gmail.com"
                value={formData.email}
                onChange={handleChange}
                required
              />
              <p className="text-[10px] text-zinc-600 pl-1">
                * Please use your{" "}
                <span className="text-zinc-500 font-medium">@gmail.com</span>{" "}
                address.
              </p>
            </div>

            <div className="space-y-1">
              <Input
                label="Password"
                type="password"
                name="password"
                placeholder="Strong password"
                value={formData.password}
                onChange={handleChange}
                required
              />
              <p className="text-[10px] text-zinc-600 pl-1 leading-relaxed">
                * Must include uppercase, number & special char.
              </p>
            </div>

            <Button type="submit" loading={loading}>
              Create Account
            </Button>
          </form>

          <div className="relative my-10">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-zinc-800"></div>
            </div>
            <div className="relative flex justify-center text-xs uppercase tracking-widest">
              <span className="bg-black px-4 text-zinc-600">
                Or continue with
              </span>
            </div>
          </div>

          <GoogleOAuthButton text="Sign up with Google" />

          <p className="mt-10 text-center text-sm text-zinc-600">
            Already have an account?{" "}
            <Link
              to="/sign-in"
              className="text-zinc-400 hover:text-white underline underline-offset-4 transition-colors"
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