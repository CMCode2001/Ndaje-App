import { useState } from "react";
import { Navbar } from "@/components/Navbar";
import { RoleSelector } from "@/components/RoleSelector";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { motion, AnimatePresence } from "framer-motion";
import { Mail, Lock, AlertCircle } from "lucide-react";

export function AuthPage() {
  const [role, setRole] = useState<"passenger" | "driver">("passenger");
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    // Simulate API call
    setTimeout(() => setIsLoading(false), 2000);
  };

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
          <div className="text-center space-y-2">
            <h1 className="text-3xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-white to-white/70">
              {role === "passenger" ? "Bon retour ! 👋" : "Bienvenue à bord ! 🚗"}
            </h1>
            <p className="text-white/60">Connectez-vous pour continuer</p>
          </div>

          <RoleSelector role={role} setRole={setRole} />

          <div className="p-8 rounded-3xl bg-white/5 backdrop-blur-xl border border-white/10 shadow-2xl relative overflow-hidden">
             {/* Glow effect */}
             <div className="absolute top-0 left-1/2 -translate-x-1/2 w-40 h-1 bg-primary/50 blur-lg rounded-full" />

            <form onSubmit={handleSubmit} className="space-y-6">
              <AnimatePresence mode="wait">
                <motion.div
                  key={role}
                  initial={{ opacity: 0, x: 20 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: -20 }}
                  transition={{ duration: 0.2 }}
                  className="space-y-4"
                >
                  <div className="space-y-2">
                    <label className="text-sm font-medium text-white/80 ml-1">Email</label>
                    <div className="relative group">
                      <Mail className="absolute left-4 top-3.5 w-5 h-5 text-white/40 group-hover:text-primary transition-colors duration-300" />
                      <Input type="email" placeholder="exemple@email.com" className="pl-12" required />
                    </div>
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium text-white/80 ml-1">Mot de passe</label>
                    <div className="relative group">
                      <Lock className="absolute left-4 top-3.5 w-5 h-5 text-white/40 group-hover:text-primary transition-colors duration-300" />
                      <Input type="password" placeholder="••••••••" className="pl-12" required />
                    </div>
                  </div>

                  {role === "driver" && (
                    <motion.div 
                      initial={{ opacity: 0, height: 0, scale: 0.95 }}
                      animate={{ opacity: 1, height: "auto", scale: 1 }}
                      className="bg-primary/10 p-4 rounded-xl flex items-start gap-3 text-sm text-primary-hover border border-primary/20"
                    >
                      <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
                      <p>Pour des raisons de sécurité, la validation du permis de conduire sera requise à la prochaine étape.</p>
                    </motion.div>
                  )}
                </motion.div>
              </AnimatePresence>

              <Button type="submit" className="w-full text-lg h-12 mt-2" disabled={isLoading}>
                {isLoading ? (
                  <div className="flex items-center gap-2">
                    <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                    <span>Connexion...</span>
                  </div>
                ) : (
                  role === "passenger" ? "Se connecter" : "Accéder à mon espace"
                )}
              </Button>
            </form>

            <div className="mt-8 text-center space-y-4">
              <a href="#" className="block text-sm text-white/40 hover:text-primary transition-colors">
                Mot de passe oublié ?
              </a>
              <div className="text-sm text-white/40">
                Pas encore de compte ? <a href="#" className="text-primary hover:underline font-medium">S'inscrire</a>
              </div>
            </div>
          </div>
        </motion.div>
      </main>
    </div>
  );
}
