import { create } from "zustand";


export const useAuthStore = create((set) => ({
  user: null,
  isAuthenticated: false,
  isLoading: false,
  isCheckingAuth: true,

  setAuth: (user) =>
    set({
      isAuthenticated: true,
      user: user,
      isCheckingAuth: false,
    }),

  setLoading: (loading) => set({ isLoading: loading }),

  setCheckingAuth: (checking) => set({ isCheckingAuth: checking }),

  logout: () =>
    set({
      isAuthenticated: false,
      user: null,
      isCheckingAuth: false,
    }),
}));
