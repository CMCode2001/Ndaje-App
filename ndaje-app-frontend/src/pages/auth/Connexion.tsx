import { useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Mail, Lock, Eye, EyeOff } from "lucide-react";
import { toast } from "sonner";
import { useNavigate } from "react-router-dom";

interface ConnexionProps {
  onToggleAuth: () => void;
}

export function Connexion({ onToggleAuth }: ConnexionProps) {
  const { login } = useAuth();
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: "",
    password: ""
  });
  const [showPassword, setShowPassword] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      await login(formData);
      navigate("/");
      toast.success("Connexion réussie", {
        description: "Ravi de vous revoir sur Ndaje !",
      });
    } catch (error) {
      toast.error("Erreur de connexion", {
        description: "Email ou mot de passe incorrect.",
      });
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  return (
    <div className="space-y-6">
      <div className="text-center space-y-2">
        <h1 className="text-3xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-white to-white/70">
          Bon retour ! 👋
        </h1>
        <p className="text-white/60">Connectez-vous à votre compte</p>
      </div>

      <div className="p-8 rounded-3xl bg-white/5 backdrop-blur-xl border border-white/10 shadow-2xl relative overflow-hidden">
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-40 h-1 bg-primary/50 blur-lg rounded-full" />

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <label className="text-sm font-medium text-white/80 ml-1">Email</label>
            <div className="relative group">
              <Mail className="absolute left-4 top-3.5 w-5 h-5 text-white/40 group-hover:text-primary transition-colors" />
              <Input type="email" name="email" placeholder="exemple@email.com" className="pl-12" required onChange={handleChange} />
            </div>
          </div>

          <div className="space-y-2">
            <label className="text-sm font-medium text-white/80 ml-1">Mot de passe</label>
            <div className="relative group">
              <Lock className="absolute left-4 top-3.5 w-5 h-5 text-white/40 group-hover:text-primary transition-colors" />
              <Input 
                type={showPassword ? "text" : "password"} 
                name="password" 
                placeholder="••••••••" 
                className="pl-12 pr-12" 
                required 
                onChange={handleChange} 
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-4 top-3.5 text-white/40 hover:text-white transition-colors"
              >
                {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
              </button>
            </div>
          </div>

          <Button type="submit" className="w-full text-lg h-12 mt-2" disabled={isLoading}>
            {isLoading ? "Connexion..." : "Se connecter"}
          </Button>
        </form>

        <div className="mt-8 text-center space-y-4">
          <button className="text-sm text-white/40 hover:text-primary transition-colors">
            Mot de passe oublié ?
          </button>
          <div className="text-sm text-white/40">
            Pas encore de compte ? <button onClick={onToggleAuth} className="text-primary hover:underline font-medium">S'inscrire</button>
          </div>
        </div>
      </div>
    </div>
  );
}
