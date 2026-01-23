import { useState } from "react";
import { Navbar } from "@/components/Navbar";
import { motion, AnimatePresence } from "framer-motion";
import { Connexion } from "./auth/Connexion";
import { Inscription } from "./auth/Inscription";

export function AuthPage() {
  const [isLogin, setIsLogin] = useState(true);

  return (
    <div className="min-h-screen flex flex-col relative overflow-hidden">
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-[#1ba3ef]/20 via-transparent to-transparent opacity-50 pointer-events-none" />
      <Navbar />
      
      <main className="flex-1 flex flex-col items-center justify-center p-4 pt-24 pb-12">
        <motion.div 
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="w-full max-w-md space-y-8 relative z-10"
        >
          <AnimatePresence mode="wait">
            {isLogin ? (
              <motion.div
                key="login"
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: 20 }}
                transition={{ duration: 0.3 }}
              >
                <Connexion onToggleAuth={() => setIsLogin(false)} />
              </motion.div>
            ) : (
              <motion.div
                key="signup"
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -20 }}
                transition={{ duration: 0.3 }}
              >
                <Inscription onToggleAuth={() => setIsLogin(true)} />
              </motion.div>
            )}
          </AnimatePresence>
        </motion.div>
      </main>
    </div>
  );
}
