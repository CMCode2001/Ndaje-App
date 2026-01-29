import { useState, useEffect } from "react";
import { useAuth } from "@/context/AuthContext";
import { useNavigate } from "react-router-dom";
import { Navbar } from "@/components/Navbar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { User, Phone, Mail, Shield, Save, CheckCircle2, Menu, Car } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";
import { toast } from "sonner";
import { VehicleService } from "@/services/VehicleService";
import { Link } from "react-router-dom";

export function ProfilePage() {
  const { user, updateUser, logout, isLoading: isAuthLoading, token } = useAuth();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<'general' | 'security' | 'vehicle'>('general');
  const [showMobileMenu, setShowMobileMenu] = useState(false);
  const [vehicle, setVehicle] = useState<any>(null);
  const [formData, setFormData] = useState({
    prenom: user?.prenom || "",
    nom: user?.nom || "",
    email: user?.email || "",
    telephone: user?.telephone || "",
  });

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

  useEffect(() => {
    const fetchFullProfile = async () => {
      if (!user?.id || !token) return;
      
      const url = `/api/users/${user.id}`;
      
      try {
        const response = await fetch(url, {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        
        if (response.status === 401) {
            toast.error("Session expirée. Veuillez vous reconnecter.");
            logout();
            navigate("/auth");
            return;
        }

        if (response.ok) {
          const data = await response.json();
          setFormData({
            prenom: data.prenom || "",
            nom: data.nom || "",
            email: data.email || "",
            telephone: data.telephone || "",
          });
        }
      } catch (err) {
        console.error("Fetch error:", err);
      }
    };

    const fetchVehicle = async () => {
        if (!user?.id || !token || user.role !== 'DRIVER') return;
        try {
             // Assuming endpoint returns list, we take the first one or logic to display list? Prompt said "un autre champ pour qu'il voit son vehicule ajouté GET: /api/vehicules"
             // Assuming list based on typical context but prompt implies "son vehicule" (singular) but POST creates one. Let's assume list.
             const data = await VehicleService.getMyVehicles(token);
             if (Array.isArray(data)) {
                 const myVehicles = data.filter((v: any) => v.driverId === user.id || v.userId === user.id);
                 if (myVehicles.length > 0) {
                    setVehicle(myVehicles[0]);
                 }
             }
        } catch (err) {
            console.error(err);
        }
    }

    fetchFullProfile();
    fetchVehicle();
  }, [user?.id, token, user?.role]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleUpdate = async (e: React.MouseEvent) => {
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

  const handleTabChange = (tab: 'general' | 'security' | 'vehicle') => {
    setActiveTab(tab);
    setShowMobileMenu(false);
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
      
      <main className="container mx-auto px-4 sm:px-6 pt-24 sm:pt-28 md:pt-32 pb-12 sm:pb-16 md:pb-20">
        <div className="max-w-5xl mx-auto">
          {/* Hero Section - Responsive */}
          <div className="relative mb-16 sm:mb-20 md:mb-12">
            <div className="h-32 sm:h-40 md:h-48 rounded-2xl sm:rounded-3xl bg-gradient-to-r from-primary/20 via-blue-500/10 to-purple-500/20 border border-white/5 overflow-hidden">
               <div className="absolute inset-0 bg-[url('https://www.transparenttextures.com/patterns/carbon-fibre.png')] opacity-20"></div>
            </div>
            
            {/* Desktop Avatar & Info */}
            <div className="hidden sm:flex absolute -bottom-10 left-6 md:left-10 items-end gap-4 md:gap-6">
              <div className="relative group">
                <div className="w-24 h-24 md:w-32 md:h-32 rounded-2xl md:rounded-3xl bg-[#0e153a] border-4 border-[#050814] flex items-center justify-center text-3xl md:text-4xl font-bold shadow-2xl relative overflow-hidden">
                  <span className="bg-gradient-to-br from-white to-white/40 bg-clip-text text-transparent">
                    {user?.prenom?.charAt(0)}{user?.nom?.charAt(0)}
                  </span>
                </div>
              </div>
              
              <div className="pb-3 md:pb-4">
                <div className="flex items-center gap-2 md:gap-3 mb-1">
                  <h1 className="text-xl md:text-3xl font-bold">{user?.prenom} {user?.nom}</h1>
                  <span className="px-2 md:px-3 py-0.5 md:py-1 rounded-full bg-green-500/20 text-[9px] md:text-[10px] font-bold text-green-500 uppercase border border-green-500/20 tracking-wider">
                    {user?.role}
                  </span>
                </div>
                <p className="text-white/40 text-xs md:text-sm flex items-center gap-2">
                  <Mail className="w-3 h-3 md:w-4 md:h-4" />
                  {user?.email}
                </p>
              </div>
            </div>

            {/* Mobile Avatar & Info */}
            <div className="sm:hidden flex flex-col items-center -mt-12 px-4">
              <div className="relative group mb-4">
                <div className="w-24 h-24 rounded-2xl bg-[#0e153a] border-4 border-[#050814] flex items-center justify-center text-3xl font-bold shadow-2xl relative overflow-hidden">
                  <span className="bg-gradient-to-br from-white to-white/40 bg-clip-text text-transparent">
                    {user?.prenom?.charAt(0)}{user?.nom?.charAt(0)}
                  </span>
                </div>
              </div>
              
              <div className="text-center">
                <div className="flex items-center justify-center gap-2 mb-2">
                  <h1 className="text-xl font-bold">{user?.prenom} {user?.nom}</h1>
                  <span className="px-2 py-0.5 rounded-full bg-green-500/20 text-[9px] font-bold text-green-500 uppercase border border-green-500/20 tracking-wider">
                    {user?.role}
                  </span>
                </div>
                <p className="text-white/40 text-sm flex items-center justify-center gap-2">
                  <Mail className="w-3 h-3" />
                  <span className="break-all">{user?.email}</span>
                </p>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 md:gap-8 mt-8 sm:mt-12 md:mt-20">
            {/* Mobile Tab Selector */}
            <div className="lg:hidden">
              <button
                onClick={() => setShowMobileMenu(!showMobileMenu)}
                className="w-full flex items-center justify-between gap-4 px-5 py-4 rounded-2xl bg-white/5 border border-white/10 text-white"
              >
                <div className="flex items-center gap-3">
                  {activeTab === 'general' ? <User className="w-5 h-5" /> : <Shield className="w-5 h-5" />}
                  <span className="font-semibold text-sm">
                    {activeTab === 'general' ? 'Informations Personnelles' : activeTab === 'vehicle' ? 'Mon Véhicule' : 'Sécurité et Mot de passe'}
                  </span>
                </div>
                <Menu className="w-5 h-5" />
              </button>

              <AnimatePresence>
                {showMobileMenu && (
                  <motion.div
                    initial={{ opacity: 0, height: 0 }}
                    animate={{ opacity: 1, height: 'auto' }}
                    exit={{ opacity: 0, height: 0 }}
                    className="mt-2 space-y-2 overflow-hidden"
                  >
                    <button 
                      onClick={() => handleTabChange('general')}
                      className={`w-full flex items-center gap-4 px-5 py-4 rounded-2xl transition-all duration-300 border ${
                        activeTab === 'general' 
                          ? "bg-primary/10 border-primary/20 text-white shadow-lg" 
                          : "bg-white/5 border-white/5 text-white/40"
                      }`}
                    >
                      <User className="w-5 h-5" />
                      <span className="font-semibold text-sm">Informations Personnelles</span>
                    </button>
                    {user?.role === 'DRIVER' && (
                        <button 
                          onClick={() => handleTabChange('vehicle')}
                          className={`w-full flex items-center gap-4 px-5 py-4 rounded-2xl transition-all duration-300 border ${
                            activeTab === 'vehicle' 
                              ? "bg-primary/10 border-primary/20 text-white shadow-lg" 
                              : "bg-white/5 border-white/5 text-white/40"
                          }`}
                        >
                          <Car className="w-5 h-5" />
                          <span className="font-semibold text-sm">Mon Véhicule</span>
                        </button>
                    )}
                  </motion.div>
                )}
              </AnimatePresence>
            </div>

            {/* Desktop Sidebar Controls */}
            <div className="hidden lg:block space-y-4">
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
              
              {user?.role === 'DRIVER' && (
                <button 
                    onClick={() => setActiveTab('vehicle')}
                    className={`w-full flex items-center gap-4 px-6 py-4 rounded-2xl transition-all duration-300 border ${
                    activeTab === 'vehicle' 
                        ? "bg-primary/10 border-primary/20 text-white shadow-lg" 
                        : "bg-white/5 border-white/5 text-white/40 hover:text-white hover:bg-white/10"
                    }`}
                >
                    <Car className="w-5 h-5" />
                    <span className="font-semibold">Mon Véhicule</span>
                </button>
              )}

              <div className="p-5 md:p-6 rounded-2xl md:rounded-3xl bg-gradient-to-br from-white/5 to-transparent border border-white/10 mt-8">
                <div className="flex items-center gap-3 mb-3 md:mb-4">
                  <CheckCircle2 className="w-4 h-4 md:w-5 md:h-5 text-green-400" />
                  <span className="text-xs md:text-sm font-bold text-white/80">Compte Vérifié</span>
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
                className="bg-white/5 backdrop-blur-xl border border-white/10 rounded-2xl md:rounded-[32px] p-5 sm:p-6 md:p-8 lg:p-10 shadow-2xl"
              >
                {activeTab === 'general' ? (
                  <div className="space-y-6 md:space-y-8">
                    <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 mb-4">
                      <div>
                        <h2 className="text-xl md:text-2xl font-bold mb-1">Détails du Profil</h2>
                        <p className="text-white/40 text-xs md:text-sm">Gérez vos informations de base et vos coordonnées.</p>
                      </div>
                      <div className="w-10 h-10 md:w-12 md:h-12 rounded-xl md:rounded-2xl bg-primary/10 flex items-center justify-center shrink-0">
                        <User className="w-5 h-5 md:w-6 md:h-6 text-primary" />
                      </div>
                    </div>

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 md:gap-6">
                      <div className="space-y-2">
                        <label className="text-xs md:text-sm font-medium text-white/60 ml-1">Prénom</label>
                        <div className="relative group">
                          <User className="absolute left-3 md:left-4 top-3 md:top-3.5 w-4 h-4 md:w-5 md:h-5 text-white/20 group-focus-within:text-primary transition-colors" />
                          <Input 
                            name="prenom"
                            value={formData.prenom}
                            onChange={handleChange}
                            className="bg-white/5 border-white/10 pl-10 md:pl-12 h-12 md:h-14 rounded-xl md:rounded-2xl text-sm md:text-base focus:ring-primary/20 focus:border-primary/40 transition-all" 
                          />
                        </div>
                      </div>
                      <div className="space-y-2">
                        <label className="text-xs md:text-sm font-medium text-white/60 ml-1">Nom</label>
                        <div className="relative group">
                          <User className="absolute left-3 md:left-4 top-3 md:top-3.5 w-4 h-4 md:w-5 md:h-5 text-white/20 group-focus-within:text-primary transition-colors" />
                          <Input 
                            name="nom"
                            value={formData.nom}
                            onChange={handleChange}
                            className="bg-white/5 border-white/10 pl-10 md:pl-12 h-12 md:h-14 rounded-xl md:rounded-2xl text-sm md:text-base focus:ring-primary/20 focus:border-primary/40 transition-all" 
                          />
                        </div>
                      </div>
                    </div>

                    <div className="space-y-2">
                      <label className="text-xs md:text-sm font-medium text-white/60 ml-1">Email (non modifiable)</label>
                      <div className="relative group">
                        <Mail className="absolute left-3 md:left-4 top-3 md:top-3.5 w-4 h-4 md:w-5 md:h-5 text-white/20 group-focus-within:text-primary transition-colors" />
                        <Input 
                          disabled
                          name="email"
                          type="email"
                          value={formData.email}
                          onChange={handleChange}
                          className="bg-white/5 border-white/10 pl-10 md:pl-12 h-12 md:h-14 rounded-xl md:rounded-2xl text-sm md:text-base focus:ring-primary/20 focus:border-primary/40 transition-all" 
                        />
                      </div>
                    </div>

                    <div className="space-y-2">
                      <label className="text-xs md:text-sm font-medium text-white/60 ml-1">Téléphone</label>
                      <div className="relative group">
                        <Phone className="absolute left-3 md:left-4 top-3 md:top-3.5 w-4 h-4 md:w-5 md:h-5 text-white/20 group-focus-within:text-primary transition-colors" />
                        <Input 
                          name="telephone"
                          value={formData.telephone}
                          onChange={handleChange}
                          className="bg-white/5 border-white/10 pl-10 md:pl-12 h-12 md:h-14 rounded-xl md:rounded-2xl text-sm md:text-base focus:ring-primary/20 focus:border-primary/40 transition-all" 
                        />
                      </div>
                    </div>

                    <div className="pt-2 md:pt-4">
                      <Button 
                        onClick={handleUpdate} 
                        disabled={isAuthLoading}
                        className="w-full md:w-auto px-8 md:px-10 h-12 md:h-14 rounded-xl md:rounded-2xl text-sm md:text-base font-bold bg-primary hover:bg-primary-hover shadow-xl shadow-primary/20 transition-all flex items-center justify-center gap-2 md:gap-3"
                      >
                        {isAuthLoading ? (
                          <div className="w-4 h-4 md:w-5 md:h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                        ) : (
                          <>
                            <Save className="w-4 h-4 md:w-5 md:h-5" />
                            <span className="hidden sm:inline">Enregistrer les modifications</span>
                            <span className="sm:hidden">Enregistrer</span>
                          </>
                        )}
                      </Button>
                    </div>
                  </div>
                ) : activeTab === 'vehicle' ? (
                     <div className="space-y-6 md:space-y-8">
                         <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 mb-4">
                           <div>
                             <h2 className="text-xl md:text-2xl font-bold mb-1">Mon Véhicule</h2>
                             <p className="text-white/40 text-xs md:text-sm">Gérez les informations de votre véhicule.</p>
                           </div>
                           <div className="w-10 h-10 md:w-12 md:h-12 rounded-xl md:rounded-2xl bg-primary/10 flex items-center justify-center shrink-0">
                             <Car className="w-5 h-5 md:w-6 md:h-6 text-primary" />
                           </div>
                         </div>
 
                         {vehicle ? (
                             <div className="bg-white/5 rounded-2xl p-6 border border-white/10 space-y-6">
                                 <div className="flex items-center gap-4 border-b border-white/10 pb-6">
                                     <div className="w-16 h-16 rounded-full bg-primary/20 flex items-center justify-center">
                                         <Car className="w-8 h-8 text-primary" />
                                     </div>
                                     <div>
                                         <h3 className="text-xl font-bold text-white mb-1">{vehicle.marque} {vehicle.modele}</h3>
                                         <p className="text-white/50 bg-white/5 px-2 py-0.5 rounded text-sm inline-block">{vehicle.immatriculation}</p>
                                     </div>
                                 </div>
                                 <div className="grid grid-cols-2 gap-4">
                                     <div>
                                         <p className="text-white/40 text-xs mb-1">Couleur</p>
                                         <p className="font-medium">{vehicle.couleur}</p>
                                     </div>
                                     <div>
                                         <p className="text-white/40 text-xs mb-1">Année</p>
                                         <p className="font-medium">{vehicle.annee}</p>
                                     </div>
                                     <div>
                                         <p className="text-white/40 text-xs mb-1">Places</p>
                                         <p className="font-medium">{vehicle.places}</p>
                                     </div>
                                 </div>
                             </div>
                         ) : (
                             <div className="text-center py-10">
                                 <Car className="w-16 h-16 text-white/20 mx-auto mb-4" />
                                 <h3 className="text-lg font-medium text-white mb-2">Aucun véhicule enregistré</h3>
                                 <p className="text-white/40 mb-6">Vous devez ajouter un véhicule pour publier des trajets.</p>
                                 <Link to="/register-vehicle">
                                     <Button className="bg-primary text-white">Ajouter un véhicule</Button>
                                 </Link>
                             </div>
                         )}
                     </div>
                ) : (
                  <div className="space-y-6 md:space-y-8">
                    <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 mb-4">
                      <div>
                        <h2 className="text-xl md:text-2xl font-bold mb-1">Sécurité</h2>
                        <p className="text-white/40 text-xs md:text-sm">Mettez à jour votre mot de passe et protégez votre compte.</p>
                      </div>
                      <div className="w-10 h-10 md:w-12 md:h-12 rounded-xl md:rounded-2xl bg-primary/10 flex items-center justify-center shrink-0">
                        <Shield className="w-5 h-5 md:w-6 md:h-6 text-primary" />
                      </div>
                    </div>

                    <div className="space-y-4 md:space-y-6">
                       <div className="p-4 md:p-6 rounded-2xl md:rounded-3xl bg-blue-500/5 border border-blue-500/10 flex gap-3 md:gap-4 items-start">
                          <Shield className="w-5 h-5 md:w-6 md:h-6 text-blue-400 shrink-0 mt-0.5" />
                          <p className="text-xs md:text-sm text-blue-100/60 leading-relaxed">
                            Les fonctionnalités de changement de mot de passe sont en cours de maintenance. Contactez le support si vous avez besoin d'aide immédiate.
                          </p>
                       </div>
                       
                       <Button disabled className="w-full h-12 md:h-14 rounded-xl md:rounded-2xl text-sm md:text-base bg-white/5 border border-white/10 text-white/40">
                          Changer le mot de passe
                       </Button>
                    </div>
                  </div>
                )}
              </motion.div>

              {/* Mobile Verified Badge */}
              <div className="lg:hidden mt-6 p-5 rounded-2xl bg-gradient-to-br from-white/5 to-transparent border border-white/10">
                <div className="flex items-center gap-3 mb-3">
                  <CheckCircle2 className="w-4 h-4 text-green-400" />
                  <span className="text-xs font-bold text-white/80">Compte Vérifié</span>
                </div>
                <p className="text-xs text-white/40 leading-relaxed">
                  Votre identité a été vérifiée par l'équipe Ndaje. Vous pouvez profiter de toutes les fonctionnalités.
                </p>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}