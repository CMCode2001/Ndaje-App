
import React, { useState } from 'react';
import { useAuth } from '@/context/AuthContext';
import { VehicleService } from '@/services/VehicleService';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Navbar } from '@/components/Navbar';
import { toast } from 'sonner';
import { motion } from 'framer-motion';
import { Loader2, Car, Calendar, Palette, Users, Hash, CheckCircle2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export function RegisterVehiclePage() {
    const { token, refreshUser } = useAuth();
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);
    
    const [formData, setFormData] = useState({
        marque: '',
        modele: '',
        immatriculation: '',
        couleur: '',
        places: 5,
        annee: new Date().getFullYear(),
    });

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value, type } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'number' ? Number(value) : value
        }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        console.log("Submit vehicle form...", { token: !!token, formData });
        
        if (!token) {
            toast.error("Erreur d'authentification", {
                description: "Votre session a expiré. Veuillez vous reconnecter."
            });
            return;
        }

        setIsLoading(true);
        try {
            await VehicleService.addVehicle(formData, token);
            await refreshUser(); // Refresh user context to get new vehicles
            toast.success('Véhicule enregistré avec succès', {
                description: 'Vous êtes maintenant prêt à publier des trajets !'
            });
            navigate('/profile'); // Or /publish
        } catch (error: any) {
            toast.error(error.message || "Erreur lors de l'enregistrement");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-[#020817] text-white">
            <Navbar />
            <div className="container mx-auto px-4 pt-32 pb-12 flex items-center justify-center min-h-[calc(100vh-80px)]">
                
                <div className="grid lg:grid-cols-2 gap-12 w-full max-w-6xl items-center">
                    
                    {/* Left Side - Visual & Info */}
                    <div className="hidden lg:block space-y-8">
                        <motion.div 
                            initial={{ opacity: 0, y: 20 }}
                            animate={{ opacity: 1, y: 0 }}
                            transition={{ duration: 0.5 }}
                        >
                            <h1 className="text-5xl font-bold mb-6 bg-gradient-to-r from-white to-white/60 bg-clip-text text-transparent leading-tight">
                                Finalisez votre profil Conducteur
                            </h1>
                            <p className="text-xl text-white/60 leading-relaxed max-w-lg">
                                Pour garantir la sécurité et la confiance sur la plateforme, nous avons besoin des détails de votre véhicule.
                            </p>
                        </motion.div>

                        <motion.div 
                            initial={{ opacity: 0, scale: 0.9 }}
                            animate={{ opacity: 1, scale: 1 }}
                            transition={{ delay: 0.2, duration: 0.5 }}
                            className="relative h-64 w-full bg-gradient-to-br from-primary/20 to-blue-600/5 rounded-3xl border border-white/10 overflow-hidden flex items-center justify-center group"
                        >
                             <div className="absolute inset-0 bg-[url('https://www.transparenttextures.com/patterns/carbon-fibre.png')] opacity-30"></div>
                             <Car className="w-32 h-32 text-primary/50 group-hover:scale-110 transition-transform duration-500" />
                             
                             {/* Floating Elements */}
                             <motion.div 
                                animate={{ y: [0, -10, 0] }}
                                transition={{ repeat: Infinity, duration: 4, ease: "easeInOut" }}
                                className="absolute top-8 right-8 bg-white/10 backdrop-blur-md p-3 rounded-xl border border-white/20"
                             >
                                 <CheckCircle2 className="w-6 h-6 text-green-400" />
                             </motion.div>
                        </motion.div>
                    </div>

                    {/* Right Side - Form */}
                    <motion.div 
                        initial={{ opacity: 0, x: 20 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ duration: 0.5 }}
                        className="bg-white/5 backdrop-blur-xl border border-white/10 rounded-[32px] p-8 md:p-10 shadow-2xl relative overflow-hidden"
                    >
                         {/* Glow Effect */}
                        <div className="absolute -top-32 -right-32 w-64 h-64 bg-primary/20 rounded-full blur-[100px] pointer-events-none" />

                        <div className="flex items-center gap-3 mb-8">
                            <div className="w-12 h-12 rounded-2xl bg-primary/20 flex items-center justify-center text-primary">
                                <Car className="w-6 h-6" />
                            </div>
                            <div>
                                <h2 className="text-2xl font-bold">Ajouter un véhicule</h2>
                                <p className="text-white/40 text-sm">Remplissez les informations ci-dessous</p>
                            </div>
                        </div>

                        <form onSubmit={handleSubmit} className="space-y-6">
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                <div className="space-y-2">
                                    <label className="text-sm font-medium text-white/80 ml-1">Marque</label>
                                    <Input 
                                        name="marque"
                                        value={formData.marque}
                                        onChange={handleChange}
                                        placeholder="Ex: Toyota"
                                        className="bg-white/5 border-white/10 h-12 rounded-xl focus:border-primary/50"
                                        required
                                    />
                                </div>
                                <div className="space-y-2">
                                    <label className="text-sm font-medium text-white/80 ml-1">Modèle</label>
                                    <Input 
                                        name="modele"
                                        value={formData.modele}
                                        onChange={handleChange}
                                        placeholder="Ex: Corolla"
                                        className="bg-white/5 border-white/10 h-12 rounded-xl focus:border-primary/50"
                                        required
                                    />
                                </div>
                            </div>

                            <div className="space-y-2">
                                <label className="text-sm font-medium text-white/80 ml-1">Immatriculation</label>
                                <div className="relative">
                                    <Hash className="absolute left-3 top-3.5 w-5 h-5 text-white/30" />
                                    <Input 
                                        name="immatriculation"
                                        value={formData.immatriculation}
                                        onChange={handleChange}
                                        placeholder="AA-123-BB"
                                        className="pl-10 bg-white/5 border-white/10 h-12 rounded-xl focus:border-primary/50 uppercase placeholder:normal-case"
                                        required
                                    />
                                </div>
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                                <div className="space-y-2">
                                    <label className="text-sm font-medium text-white/80 ml-1">Couleur</label>
                                    <div className="relative">
                                        <Palette className="absolute left-3 top-3.5 w-4 h-4 text-white/30" />
                                        <Input 
                                            name="couleur"
                                            value={formData.couleur}
                                            onChange={handleChange}
                                            placeholder="Gris"
                                            className="pl-9 bg-white/5 border-white/10 h-12 rounded-xl focus:border-primary/50"
                                            required
                                        />
                                    </div>
                                </div>
                                <div className="space-y-2">
                                    <label className="text-sm font-medium text-white/80 ml-1">Année</label>
                                    <div className="relative">
                                        <Calendar className="absolute left-3 top-3.5 w-4 h-4 text-white/30" />
                                        <Input 
                                            type="number"
                                            name="annee"
                                            value={formData.annee}
                                            onChange={handleChange}
                                            className="pl-9 bg-white/5 border-white/10 h-12 rounded-xl focus:border-primary/50"
                                            required
                                        />
                                    </div>
                                </div>
                                <div className="space-y-2">
                                    <label className="text-sm font-medium text-white/80 ml-1">Places</label>
                                    <div className="relative">
                                        <Users className="absolute left-3 top-3.5 w-4 h-4 text-white/30" />
                                        <Input 
                                            type="number"
                                            name="places"
                                            min="1"
                                            value={formData.places}
                                            onChange={handleChange}
                                            className="pl-9 bg-white/5 border-white/10 h-12 rounded-xl focus:border-primary/50"
                                            required
                                        />
                                    </div>
                                </div>
                            </div>

                            <Button 
                                type="submit" 
                                disabled={isLoading}
                                className="w-full h-14 rounded-xl text-lg font-bold bg-primary hover:bg-primary-hover shadow-xl shadow-primary/20 mt-4 transition-all"
                            >
                                {isLoading ? (
                                    <Loader2 className="w-6 h-6 animate-spin" />
                                ) : "Enregistrer le véhicule"}
                            </Button>
                        </form>
                    </motion.div>
                </div>
            </div>
        </div>
    );
}
