import { useState, useEffect } from "react";
import { useAuth } from "@/context/AuthContext";
import { Navbar } from "@/components/Navbar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { User, Phone, Mail, Shield, Save, Camera, CheckCircle2 } from "lucide-react";
import { motion } from "framer-motion";
import { toast } from "sonner";

export function ProfilePage() {
  const { user, updateUser, isLoading: isAuthLoading, token } = useAuth();
  const [activeTab, setActiveTab] = useState<'general' | 'security'>('general');
  const [formData, setFormData] = useState({
    prenom: user?.prenom || "",
    nom: user?.nom || "",
    email: user?.email || "",
    telephone: user?.telephone || "",
  });

  // Mettre à jour le formulaire si les données utilisateur arrivent après le premier rendu
  useEffect(() => {
    if (user && !formData.prenom && !formData.nom) {
      setFormData({
        prenom: user.prenom || "",
        nom: user.nom || "",
        email: user.email || "",
        telephone: user.telephone || "",
      });
    }
  }, [user]);

  // Récupérer le profil complet pour garantir les données fraîches (téléphone, etc.)
  useEffect(() => {
    const fetchFullProfile = async () => {
      if (!user?.id || !token) return;
      
      const url = `/api/users/${user.id}`;
      
      try {
        const response = await fetch(url, {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        
        if (response.ok) {
          const data = await response.json();
          setFormData({
            prenom: data.prenom || "",
            nom: data.nom || "",
            email: data.email || "",
            telephone: data.telephone || "",
          });
        } else {
          // Fallback sur le contexte
          setFormData({
            prenom: user.prenom || "",
            nom: user.nom || "",
            email: user.email || "",
            telephone: user.telephone || "",
          });
        }
      } catch (err) {
        console.error("Fetch error:", err);
      }
    };

    fetchFullProfile();
  }, [user?.id, token]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await updateUser(formData);
      toast.success("Profil mis à jour", {
        description: "Vos informations ont été enregistrées avec succès."
      });
    } catch (error: any) {
      toast.error("Erreur", { description: error.message });
    }
  };

  if (isAuthLoading && !user) {
    return (
      <div className="min-h-screen bg-[#050814] flex items-center justify-center">
        <div className="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#050814] text-white">
      <Navbar />
      
      <main className="container mx-auto px-4 pt-32 pb-20">
        <div className="max-w-5xl mx-auto">
          {/* Hero Section */}
          <div className="relative mb-12">
            <div className="h-48 rounded-3xl bg-gradient-to-r from-primary/20 via-blue-500/10 to-purple-500/20 border border-white/5 overflow-hidden">
               <div className="absolute inset-0 bg-[url('https://www.transparenttextures.com/patterns/carbon-fibre.png')] opacity-20"></div>
            </div>
            
            <div className="absolute -bottom-10 left-10 flex items-end gap-6">
              <div className="relative group">
                <div className="w-32 h-32 rounded-3xl bg-[#0e153a] border-4 border-[#050814] flex items-center justify-center text-4xl font-bold shadow-2xl relative overflow-hidden">
                  <span className="bg-gradient-to-br from-white to-white/40 bg-clip-text text-transparent">
                    {user?.prenom?.charAt(0)}{user?.nom?.charAt(0)}
                  </span>
                  {/* <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center cursor-pointer">
                    <Camera className="w-8 h-8 text-white" />
                  </div> */}
                </div>
              </div>
              
              <div className="pb-4">
                <div className="flex items-center gap-3 mb-1">
                  <h1 className="text-3xl font-bold">{user?.prenom} {user?.nom}</h1>
                  <span className="px-3 py-1 rounded-full bg-green-500/20 text-[10px] font-bold text-green-500 uppercase border border-green-500/20 tracking-wider">
                    {user?.role}
                  </span>
                </div>
                <p className="text-white/40 flex items-center gap-2">
                  <Mail className="w-4 h-4" />
                  {user?.email}
                </p>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 mt-20">
            {/* Sidebar Controls */}
            <div className="space-y-4">
              <button 
                onClick={() => setActiveTab('general')}
                className={`w-full flex items-center gap-4 px-6 py-4 rounded-2xl transition-all duration-300 border ${
                  activeTab === 'general' 
                    ? "bg-primary/10 border-primary/20 text-white shadow-lg" 
                    : "bg-white/5 border-white/5 text-white/40 hover:text-white hover:bg-white/10"
                }`}
              >
                <User className="w-5 h-5" />
                <span className="font-semibold">Informations Personnelles</span>
              </button>
              {/* <button 
                onClick={() => setActiveTab('security')}
                className={`w-full flex items-center gap-4 px-6 py-4 rounded-2xl transition-all duration-300 border ${
                  activeTab === 'security' 
                    ? "bg-primary/10 border-primary/20 text-white shadow-lg" 
                    : "bg-white/5 border-white/5 text-white/40 hover:text-white hover:bg-white/10"
                }`}
              >
                <Shield className="w-5 h-5" />
                <span className="font-semibold">Sécurité et Mot de passe</span>
              </button> */}

              <div className="p-6 rounded-3xl bg-gradient-to-br from-white/5 to-transparent border border-white/10 mt-8">
                <div className="flex items-center gap-3 mb-4">
                  <CheckCircle2 className="w-5 h-5 text-green-400" />
                  <span className="text-sm font-bold text-white/80">Compte Vérifié</span>
                </div>
                <p className="text-xs text-white/40 leading-relaxed">
                  Votre identité a été vérifiée par l'équipe Ndaje. Vous pouvez profiter de toutes les fonctionnalités.
                </p>
              </div>
            </div>

            {/* Main Form Area */}
            <div className="lg:col-span-2">
              <motion.div 
                key={activeTab}
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                className="bg-white/5 backdrop-blur-xl border border-white/10 rounded-[32px] p-8 md:p-10 shadow-2xl"
              >
                {activeTab === 'general' ? (
                  <form onSubmit={handleUpdate} className="space-y-8">
                    <div className="flex items-center justify-between mb-4">
                      <div>
                        <h2 className="text-2xl font-bold mb-1">Détails du Profil</h2>
                        <p className="text-white/40 text-sm">Gérez vos informations de base et vos coordonnées.</p>
                      </div>
                      <div className="w-12 h-12 rounded-2xl bg-primary/10 flex items-center justify-center">
                        <User className="w-6 h-6 text-primary" />
                      </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div className="space-y-2">
                        <label className="text-sm font-medium text-white/60 ml-1">Prénom</label>
                        <div className="relative group">
                          <User className="absolute left-4 top-3.5 w-5 h-5 text-white/20 group-focus-within:text-primary transition-colors" />
                          <Input 
                            name="prenom"
                            value={formData.prenom}
                            onChange={handleChange}
                            className="bg-white/5 border-white/10 pl-12 h-14 rounded-2xl focus:ring-primary/20 focus:border-primary/40 transition-all" 
                          />
                        </div>
                      </div>
                      <div className="space-y-2">
                        <label className="text-sm font-medium text-white/60 ml-1">Nom</label>
                        <div className="relative group">
                          <User className="absolute left-4 top-3.5 w-5 h-5 text-white/20 group-focus-within:text-primary transition-colors" />
                          <Input 
                            name="nom"
                            value={formData.nom}
                            onChange={handleChange}
                            className="bg-white/5 border-white/10 pl-12 h-14 rounded-2xl focus:ring-primary/20 focus:border-primary/40 transition-all" 
                          />
                        </div>
                      </div>
                    </div>

                    <div className="space-y-2">
                      <label className="text-sm font-medium text-white/60 ml-1">Email</label>
                      <div className="relative group">
                        <Mail className="absolute left-4 top-3.5 w-5 h-5 text-white/20 group-focus-within:text-primary transition-colors" />
                        <Input 
                          name="email"
                          type="email"
                          value={formData.email}
                          onChange={handleChange}
                          className="bg-white/5 border-white/10 pl-12 h-14 rounded-2xl focus:ring-primary/20 focus:border-primary/40 transition-all" 
                        />
                      </div>
                    </div>

                    <div className="space-y-2">
                      <label className="text-sm font-medium text-white/60 ml-1">Téléphone</label>
                      <div className="relative group">
                        <Phone className="absolute left-4 top-3.5 w-5 h-5 text-white/20 group-focus-within:text-primary transition-colors" />
                        <Input 
                          name="telephone"
                          value={formData.telephone}
                          onChange={handleChange}
                          className="bg-white/5 border-white/10 pl-12 h-14 rounded-2xl focus:ring-primary/20 focus:border-primary/40 transition-all" 
                        />
                      </div>
                    </div>

                    <div className="pt-4">
                      <Button 
                        type="submit" 
                        disabled={isAuthLoading}
                        className="w-full md:w-auto px-10 h-14 rounded-2xl font-bold bg-primary hover:bg-primary-hover shadow-xl shadow-primary/20 transition-all flex items-center justify-center gap-3"
                      >
                        {isAuthLoading ? (
                          <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                        ) : (
                          <>
                            <Save className="w-5 h-5" />
                            Enregistrer les modifications
                          </>
                        )}
                      </Button>
                    </div>
                  </form>
                ) : (
                  <div className="space-y-8">
                    <div className="flex items-center justify-between mb-4">
                      <div>
                        <h2 className="text-2xl font-bold mb-1">Sécurité</h2>
                        <p className="text-white/40 text-sm">Mettez à jour votre mot de passe et protégez votre compte.</p>
                      </div>
                      <div className="w-12 h-12 rounded-2xl bg-primary/10 flex items-center justify-center">
                        <Shield className="w-6 h-6 text-primary" />
                      </div>
                    </div>

                    <div className="space-y-6">
                       <div className="p-6 rounded-3xl bg-blue-500/5 border border-blue-500/10 flex gap-4 items-start">
                          <Shield className="w-6 h-6 text-blue-400 shrink-0" />
                          <p className="text-sm text-blue-100/60 leading-relaxed">
                            Les fonctionnalités de changement de mot de passe sont en cours de maintenance. Contactez le support si vous avez besoin d'aide immédiate.
                          </p>
                       </div>
                       
                       <Button disabled className="w-full h-14 rounded-2xl bg-white/5 border border-white/10 text-white/40">
                          Changer le mot de passe
                       </Button>
                    </div>
                  </div>
                )}
              </motion.div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
