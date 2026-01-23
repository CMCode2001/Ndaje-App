import { useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { motion } from "framer-motion";
import { Mail, Lock, User, Phone, Car, Users, AlertCircle } from "lucide-react";
import { toast } from "sonner";
import { useNavigate } from "react-router-dom";

interface InscriptionProps {
  onToggleAuth: () => void;
}

export function Inscription({ onToggleAuth }: InscriptionProps) {
  const { register } = useAuth();
  const [role, setRole] = useState<"passenger" | "driver">("passenger");
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    prenom: "",
    nom: "",
    email: "",
    telephone: "",
    password: ""
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      await register(formData, role);
      navigate("/");
      toast.success("Inscription réussie !", {
        description: `Bienvenue parmi nous, ${formData.prenom} ${formData.nom}.`,
      });
    } catch (error) {
      toast.error("Erreur d'inscription", {
        description: "Un compte existe déjà avec cet email ou les données sont invalides.",
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
          Créer un compte
        </h1>
        <p className="text-white/60">Rejoignez la communauté Ndaje</p>
      </div>

      {/* Role Toggle */}
      <div className="flex p-1 bg-white/5 backdrop-blur-lg rounded-2xl border border-white/10">
        <button
          onClick={() => setRole("passenger")}
          className={`flex-1 flex items-center justify-center gap-2 py-3 rounded-xl transition-all duration-300 ${
            role === "passenger" ? "bg-primary text-white shadow-lg" : "text-white/40 hover:text-white/60"
          }`}
        >
          <Users className="w-5 h-5" />
          <span className="font-medium">Passager</span>
        </button>
        <button
          onClick={() => setRole("driver")}
          className={`flex-1 flex items-center justify-center gap-2 py-3 rounded-xl transition-all duration-300 ${
            role === "driver" ? "bg-primary text-white shadow-lg" : "text-white/40 hover:text-white/60"
          }`}
        >
          <Car className="w-5 h-5" />
          <span className="font-medium">Conducteur</span>
        </button>
      </div>

      <div className="p-8 rounded-3xl bg-white/5 backdrop-blur-xl border border-white/10 shadow-2xl relative overflow-hidden">
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-40 h-1 bg-primary/50 blur-lg rounded-full" />

        <form onSubmit={handleSubmit} className="space-y-5">
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <label className="text-sm font-medium text-white/80 ml-1">Prénom</label>
              <div className="relative group">
                <User className="absolute left-4 top-3.5 w-5 h-5 text-white/40 group-hover:text-primary transition-colors" />
                <Input name="prenom" placeholder="Abdoulaye" className="pl-12" required onChange={handleChange} />
              </div>
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium text-white/80 ml-1">Nom</label>
              <div className="relative group">
                <User className="absolute left-4 top-3.5 w-5 h-5 text-white/40 group-hover:text-primary transition-colors" />
                <Input name="nom" placeholder="Ndiaye" className="pl-12" required onChange={handleChange} />
              </div>
            </div>
          </div>

          <div className="space-y-2">
            <label className="text-sm font-medium text-white/80 ml-1">Email</label>
            <div className="relative group">
              <Mail className="absolute left-4 top-3.5 w-5 h-5 text-white/40 group-hover:text-primary transition-colors" />
              <Input type="email" name="email" placeholder="exemple@email.com" className="pl-12" required onChange={handleChange} />
            </div>
          </div>

          <div className="space-y-2">
            <label className="text-sm font-medium text-white/80 ml-1">Téléphone</label>
            <div className="relative group">
              <Phone className="absolute left-4 top-3.5 w-5 h-5 text-white/40 group-hover:text-primary transition-colors" />
              <Input type="tel" name="telephone" placeholder="77..." className="pl-12" required onChange={handleChange} />
            </div>
          </div>

          <div className="space-y-2">
            <label className="text-sm font-medium text-white/80 ml-1">Mot de passe</label>
            <div className="relative group">
              <Lock className="absolute left-4 top-3.5 w-5 h-5 text-white/40 group-hover:text-primary transition-colors" />
              <Input type="password" name="password" placeholder="••••••••" className="pl-12" required onChange={handleChange} />
            </div>
          </div>

          {role === "driver" && (
            <motion.div 
              initial={{ opacity: 0, y: -10 }}
              animate={{ opacity: 1, y: 0 }}
              className="bg-primary/10 p-4 rounded-xl flex items-start gap-3 text-sm text-primary-hover border border-primary/20"
            >
              <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
              <p>En tant que conducteur, nous vérifierons vos documents après l'inscription.</p>
            </motion.div>
          )}

          <Button type="submit" className="w-full text-lg h-12 mt-4" disabled={isLoading}>
            {isLoading ? "Création du compte..." : "S'inscrire"}
          </Button>
        </form>

        <div className="mt-8 text-center">
          <div className="text-sm text-white/40">
            Déjà un compte ? <button onClick={onToggleAuth} className="text-primary hover:underline font-medium">Se connecter</button>
          </div>
        </div>
      </div>
    </div>
  );
}
