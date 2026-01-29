
import React, { useState, useEffect } from 'react';
import { useAuth } from '@/context/AuthContext';
import { TripService } from '@/services/TripService';
import type { Trip } from '@/services/TripService';
import { ReservationService } from '@/services/ReservationService';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { toast } from 'sonner';
import { motion, AnimatePresence } from 'framer-motion';
import { Link } from 'react-router-dom';
import { VehicleService, type Vehicle } from '@/services/VehicleService';
import { Loader2, MapPin, DollarSign, Car, Users, Plus, List, CheckCircle2, AlertCircle } from 'lucide-react';
import { Navbar } from '@/components/Navbar';

export function PublishTripPage() {
    const { user, token } = useAuth();
    const [activeTab, setActiveTab] = useState<'publish' | 'list' | 'passengers'>('publish');
    const [isLoading, setIsLoading] = useState(false);
    
    // Reservations State for Driver
    const [driverReservations, setDriverReservations] = useState<any[]>([]);
    const [isLoadingReservations, setIsLoadingReservations] = useState(false);
    
    // Vehicle State
    const [verifiedVehicles, setVerifiedVehicles] = useState<Vehicle[]>([]);
    const [isLoadingVehicles, setIsLoadingVehicles] = useState(false);
    
    interface TripFormData extends Omit<Trip, 'placesDisponibles' | 'prix'> {
        placesDisponibles: number | string;
        prix: number | string;
    }

    // Form State
    const [tripData, setTripData] = useState<TripFormData>({
        driverId: user?.id || '',
        vehicleId: 'default-vehicle', // Placeholder as per plan
        depart: '',
        arrivee: '',
        dateDepart: '',
        placesDisponibles: 1,
        prix: 0,
    });

    // Edit Mode State
    const [isEditing, setIsEditing] = useState(false);
    const [editingId, setEditingId] = useState<string | null>(null);

    // Trips List State
    const [myTrips, setMyTrips] = useState<Trip[]>([]);
    const [isLoadingTrips, setIsLoadingTrips] = useState(false);

    useEffect(() => {
        if (user?.id) {
            setTripData(prev => ({ ...prev, driverId: user.id }));
        }
    }, [user]);

    // Fetch verified vehicles
    useEffect(() => {
        const fetchVehicles = async () => {
            if (!token || !user?.id) return;
            setIsLoadingVehicles(true);
            try {
                const vehicles = await VehicleService.getMyVehicles(token);
                // Filter verified vehicles
                const verified = Array.isArray(vehicles) 
                    ? vehicles.filter((v: any) => v.statutVerification === 'VERIFIE' && (v.driverId === user.id || v.userId === user.id))
                    : [];
                
                setVerifiedVehicles(verified);
                
                // Auto-select first verified vehicle if NOT editing
                if (verified.length > 0 && !isEditing) {
                    setTripData(prev => ({ ...prev, vehicleId: verified[0].id || '' }));
                }
            } catch (error) {
                console.error("Error fetching vehicles", error);
                toast.error("Oups ! Nous n'avons pas pu charger vos véhicules. Vérifiez votre connexion.");
            } finally {
                setIsLoadingVehicles(false);
            }
        };

        fetchVehicles();
    }, [token, user, isEditing]);

    useEffect(() => {
        if (activeTab === 'list' && user?.id && token) {
            fetchMyTrips();
        }
        if (activeTab === 'passengers' && user?.id && token) {
            fetchDriverReservations();
        }
    }, [activeTab, user, token]);

    const fetchDriverReservations = async () => {
        if (!user?.id || !token) return;
        setIsLoadingReservations(true);
        try {
            const response = await ReservationService.getDriverReservations(user.id, token);
            // Safer extraction: handles both { data: [] } and direct [] responses
            const data = Array.isArray(response) ? response : (response.data || []);
            setDriverReservations(Array.isArray(data) ? data : []);
        } catch (error: any) {
            console.error("Fetch Driver Reservations Error:", error);
            toast.error("Impossible de charger la liste des passagers pour le moment.");
        } finally {
            setIsLoadingReservations(false);
        }
    };

    const fetchMyTrips = async () => {
        if (!user?.id || !token) return;
        setIsLoadingTrips(true);
        try {
            const response = await TripService.getDriverTrips(user.id, token);
            const trips = Array.isArray(response) ? response : (response.data || []);
            setMyTrips(Array.isArray(trips) ? trips : []);
        } catch (error: any) {
            console.error("Fetch My Trips Error:", error);
            toast.error("Un problème est survenu lors de la récupération de vos trajets.");
        } finally {
            setIsLoadingTrips(false);
        }
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value, type } = e.target;
        let newValue: any = value;
        
        if (type === 'number') {
            // Only allow digits, or empty string (to allow clearing)
            if (value === "" || /^\d+$/.test(value)) {
                newValue = value === "" ? "" : Number(value);
            } else {
                return; // Ignore non-digit characters
            }
        } else if (name === 'depart' || name === 'arrivee') {
            newValue = typeof value === 'string' ? value.toUpperCase() : value;
        }

        setTripData(prev => ({
            ...prev,
            [name]: newValue
        }));
    };

    const getMinDateTime = () => {
        const now = new Date();
        now.setHours(now.getHours() + 2);
        // Format to YYYY-MM-DDThh:mm
        return now.toISOString().slice(0, 16);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!token) return;

        // Validation: Date must be at least 2 hours in the future
        const selectedDate = new Date(tripData.dateDepart);
        const minDate = new Date();
        minDate.setHours(minDate.getHours() + 2);

        if (selectedDate < minDate) {
            toast.error("Le départ doit être prévu au moins 2 heures à l'avance");
            return;
        }

        setIsLoading(true);
        const payload: Trip = {
            ...tripData,
            placesDisponibles: Number(tripData.placesDisponibles) || 0,
            prix: Number(tripData.prix) || 0
        };

        // Capacity validation
        const selectedVehicle = verifiedVehicles.find(v => v.id === tripData.vehicleId);
        if (selectedVehicle && (payload.placesDisponibles > selectedVehicle.places)) {
            toast.error(`Attention ! Ce véhicule ne peut transporter que ${selectedVehicle.places} passagers.`);
            setIsLoading(false);
            return;
        }

        try {
            if (isEditing && editingId) {
                await TripService.updateTrip(editingId, payload, token);
                toast.success('Génial ! Votre trajet a été mis à jour.');
            } else {
                await TripService.createTrip(payload, token);
                toast.success('Félicitations ! Votre trajet est maintenant en ligne.');
            }
            
            // Reset form
            setTripData({
                driverId: user?.id || '',
                vehicleId: 'default-vehicle',
                depart: '',
                arrivee: '',
                dateDepart: '',
                placesDisponibles: 1,
                prix: 0,
            });
            setIsEditing(false);
            setEditingId(null);
            
            // Switch to list view to see result
            setActiveTab('list');
            fetchMyTrips();

        } catch (error: any) {
            console.error("Submit Trip Error:", error);
            toast.error("Zut ! Une erreur est survenue lors de la publication. Vérifiez vos informations.");
        } finally {
            setIsLoading(false);
        }
    };

    const handleEdit = (trip: Trip) => {
        setTripData(trip as TripFormData);
        setIsEditing(true);
        setEditingId(trip.id || null);
        setActiveTab('publish');
    };

    return (
        <>
        <Navbar/>
        <div className="min-h-screen bg-[#020817] pt-24 pb-12 px-4 relative overflow-hidden">
             {/* Background Elements */}
            <div className="absolute inset-0 bg-grid-white/[0.02] pointer-events-none" />
            <div className="absolute top-0 right-0 p-12 bg-primary/10 w-[500px] h-[500px] rounded-full blur-[100px] pointer-events-none" />
            <div className="absolute bottom-0 left-0 p-12 bg-blue-500/10 w-[500px] h-[500px] rounded-full blur-[100px] pointer-events-none" />

            <div className="container mx-auto max-w-4xl relative z-10">
                <div className="flex flex-col md:flex-row justify-between items-center mb-10 gap-4">
                    <div>
                        <h1 className="text-3xl font-bold text-white mb-2">Gestion des Trajets</h1>
                        <p className="text-white/60">Publiez et gérez vos propositions de covoiturage.</p>
                    </div>
                </div>

                {/* Tabs Navigation */}
                <div className="flex space-x-1 bg-white/5 p-1 rounded-xl mb-8 w-fit">
                    <button
                        onClick={() => setActiveTab('publish')}
                        className={`flex items-center gap-2 px-6 py-2.5 rounded-lg text-sm font-medium transition-all ${
                            activeTab === 'publish'
                            ? 'bg-primary text-white shadow-lg shadow-primary/20'
                            : 'text-white/60 hover:text-white hover:bg-white/5'
                        }`}
                    >
                        {isEditing ? <Car className="w-4 h-4" /> : <Plus className="w-4 h-4" />}
                        {isEditing ? "Modifier le trajet" : "Publier un trajet"}
                    </button>
                    <button
                        onClick={() => setActiveTab('list')}
                        className={`flex items-center gap-2 px-6 py-2.5 rounded-lg text-sm font-medium transition-all ${
                            activeTab === 'list'
                            ? 'bg-primary text-white shadow-lg shadow-primary/20'
                            : 'text-white/60 hover:text-white hover:bg-white/5'
                        }`}
                    >
                        <List className="w-4 h-4" />
                        Mes Trajets
                    </button>
                    <button
                        onClick={() => setActiveTab('passengers')}
                        className={`flex items-center gap-2 px-6 py-2.5 rounded-lg text-sm font-medium transition-all ${
                            activeTab === 'passengers'
                            ? 'bg-primary text-white shadow-lg shadow-primary/20'
                            : 'text-white/60 hover:text-white hover:bg-white/5'
                        }`}
                    >
                        <Users className="w-4 h-4" />
                        Passagers
                    </button>
                </div>

                <AnimatePresence mode="wait">
                    {activeTab === 'publish' ? (
                        <motion.div
                            key="publish"
                            initial={{ opacity: 0, y: 20 }}
                            animate={{ opacity: 1, y: 0 }}
                            exit={{ opacity: 0, y: -20 }}
                            className="bg-white/5 backdrop-blur-xl border border-white/10 rounded-2xl p-6 md:p-8"
                        >
                            <h2 className="text-xl font-semibold text-white mb-6 flex items-center gap-2">
                                <Car className="w-5 h-5 text-primary" />
                                {isEditing ? "Modifier les détails du trajet" : "Nouveau Trajet"}
                            </h2>
                            <form onSubmit={handleSubmit} className="space-y-6">
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                    <div className="space-y-2">
                                        <label className="text-sm font-medium text-white/80">Lieu de départ</label>
                                        <div className="relative">
                                            <MapPin className="absolute left-3 top-2.5 w-5 h-5 text-white/40" />
                                            <Input
                                                name="depart"
                                                value={tripData.depart}
                                                onChange={handleChange}
                                                placeholder="Ex: Dakar"
                                                className="pl-10 bg-white/5 border-white/10 text-white placeholder:text-white/30 focus:border-primary/50"
                                                required
                                            />
                                        </div>
                                    </div>
                                    <div className="space-y-2">
                                        <label className="text-sm font-medium text-white/80">Lieu d'arrivée</label>
                                        <div className="relative">
                                            <MapPin className="absolute left-3 top-2.5 w-5 h-5 text-white/40" />
                                            <Input
                                                name="arrivee"
                                                value={tripData.arrivee}
                                                onChange={handleChange}
                                                placeholder="Ex: Saint-Louis"
                                                className="pl-10 bg-white/5 border-white/10 text-white placeholder:text-white/30 focus:border-primary/50"
                                                required
                                            />
                                        </div>
                                    </div>
                                    
                                    <div className="space-y-2">
                                        <label className="text-sm font-medium text-white/80">Date et Heure</label>
                                        <div className="relative">
                                            {/* Calendar icon creates input alignment issues with date type if inside, placing outside or adjusting padding */}
                                            <Input
                                                type="datetime-local"
                                                name="dateDepart"
                                                value={tripData.dateDepart}
                                                onChange={handleChange}
                                                min={getMinDateTime()}
                                                className="bg-white/5 border-white/10 text-white placeholder:text-white/30 focus:border-primary/50 scheme-dark"
                                                required
                                            />
                                        </div>
                                    </div>
                                    
                                    <div className="space-y-2">
                                        <label className="text-sm font-medium text-white/80">Véhicule</label>
                                        <div className="relative">
                                            {isLoadingVehicles ? (
                                                <div className="flex items-center gap-2 text-white/40 h-10 px-3 bg-white/5 border border-white/10 rounded-xl">
                                                    <Loader2 className="w-4 h-4 animate-spin" />
                                                    Chargement du véhicule...
                                                </div>
                                            ) : verifiedVehicles.length > 0 ? (
                                                <div className="flex items-center gap-3 p-3 bg-white/5 border border-white/10 rounded-xl">
                                                    <div className="w-10 h-10 rounded-lg bg-primary/20 flex items-center justify-center">
                                                        <Car className="w-5 h-5 text-primary" />
                                                    </div>
                                                    <div>
                                                        <p className="font-medium text-white">
                                                            {verifiedVehicles[0].marque} {verifiedVehicles[0].modele}
                                                        </p>
                                                        <div className="flex items-center gap-2">
                                                            <span className="text-xs text-white/40 bg-white/5 px-1.5 py-0.5 rounded">
                                                                {verifiedVehicles[0].immatriculation}
                                                            </span>
                                                            <span className="text-[10px] text-green-400 border border-green-500/20 bg-green-500/10 px-1.5 py-0.5 rounded-full flex items-center gap-1">
                                                                <CheckCircle2 className="w-3 h-3" /> Vérifié
                                                            </span>
                                                        </div>
                                                    </div>
                                                    <input type="hidden" name="vehicleId" value={tripData.vehicleId} />
                                                </div>
                                            ) : (
                                                <div className="p-4 bg-red-500/10 border border-red-500/20 rounded-xl text-red-200 text-sm">
                                                    <div className="flex items-center gap-2 mb-2 font-bold">
                                                        <AlertCircle className="w-4 h-4" />
                                                        Véhicule non disponible
                                                    </div>
                                                    <p className="mb-3 opacity-80">
                                                        Vous n'avez pas de véhicule vérifié. Veuillez ajouter un véhicule et attendre sa validation par un administrateur.
                                                    </p>
                                                    <Link to="/register-vehicle">
                                                        <Button variant="outline" size="sm" className="w-full border-red-500/50 text-red-400 hover:bg-red-500/10">
                                                            Ajouter un véhicule
                                                        </Button>
                                                    </Link>
                                                </div>
                                            )}
                                        </div>
                                    </div>

                                    <div className="space-y-2">
                                        <div className="flex justify-between items-center">
                                            <label className="text-sm font-medium text-white/80">Places disponibles</label>
                                            {verifiedVehicles.find(v => v.id === tripData.vehicleId) && (
                                                <span className="text-[10px] bg-primary/20 text-primary px-2 py-0.5 rounded-full font-bold uppercase tracking-wider">
                                                    Max: {verifiedVehicles.find(v => v.id === tripData.vehicleId)?.places}
                                                </span>
                                            )}
                                        </div>
                                        <div className="relative">
                                            <Users className="absolute left-3 top-2.5 w-5 h-5 text-white/40" />
                                            <Input
                                                type="number"
                                                name="placesDisponibles"
                                                min="1"
                                                max={verifiedVehicles.find(v => v.id === tripData.vehicleId)?.places}
                                                value={tripData.placesDisponibles}
                                                onChange={handleChange}
                                                className="pl-10 bg-white/5 border-white/10 text-white placeholder:text-white/30 focus:border-primary/50"
                                                required
                                            />
                                        </div>
                                    </div>

                                    <div className="space-y-2">
                                        <label className="text-sm font-medium text-white/80">Prix (FCFA)</label>
                                        <div className="relative">
                                            <DollarSign className="absolute left-3 top-2.5 w-5 h-5 text-white/40" />
                                            <Input
                                                type="number"
                                                name="prix"
                                                min="0"
                                                value={tripData.prix}
                                                onChange={handleChange}
                                                className="pl-10 bg-white/5 border-white/10 text-white placeholder:text-white/30 focus:border-primary/50"
                                                required
                                            />
                                        </div>
                                    </div>
                                </div>

                                <div className="pt-4 flex justify-end gap-4">
                                    {isEditing && (
                                        <Button
                                            type="button"
                                            variant="outline"
                                            onClick={() => {
                                                setIsEditing(false);
                                                setTripData({
                                                    driverId: user?.id || '',
                                                    vehicleId: 'default-vehicle',
                                                    depart: '',
                                                    arrivee: '',
                                                    dateDepart: '',
                                                    placesDisponibles: 1,
                                                    prix: 0,
                                                });
                                            }}
                                            className="border-white/10 text-white hover:bg-white/5"
                                        >
                                            Annuler
                                        </Button>
                                    )}
                                    <Button 
                                        type="submit" 
                                        disabled={isLoading}
                                        className="bg-primary hover:bg-primary-hover text-white px-8"
                                    >
                                        {isLoading && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
                                        {isEditing ? 'Mettre à jour' : 'Publier le trajet'}
                                    </Button>
                                </div>
                            </form>
                        </motion.div>
                    ) : activeTab === 'list' ? (
                        <motion.div
                             key="list"
                             initial={{ opacity: 0, y: 20 }}
                             animate={{ opacity: 1, y: 0 }}
                             exit={{ opacity: 0, y: -20 }}
                             className="space-y-4"
                        >
                            {isLoadingTrips ? (
                                <div className="flex justify-center py-20">
                                    <Loader2 className="w-10 h-10 text-primary animate-spin" />
                                </div>
                            ) : myTrips.length === 0 ? (
                                <div className="bg-white/5 border border-white/10 rounded-2xl p-12 text-center">
                                    <Car className="w-16 h-16 text-white/20 mx-auto mb-4" />
                                    <h3 className="text-xl font-medium text-white mb-2">Aucun trajet publié</h3>
                                    <p className="text-white/60 mb-6">Vous n'avez pas encore publié de trajet.</p>
                                    <Button 
                                        onClick={() => setActiveTab('publish')}
                                        variant="default"
                                    >
                                        Publier mon premier trajet
                                    </Button>
                                </div>
                            ) : (
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                    {myTrips.map((trip) => (
                                        <div 
                                            key={trip.id} 
                                            className="bg-white/5 border border-white/10 rounded-xl p-6 hover:bg-white/10 transition-all flex flex-col justify-between group"
                                        >
                                            <div>
                                                <div className="flex justify-between items-start mb-4">
                                                    <div className="flex items-center gap-3">
                                                        <div className="w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center text-primary">
                                                            <MapPin className="w-5 h-5" />
                                                        </div>
                                                        <div>
                                                            <p className="text-white font-semibold capitalize">{trip.depart} → {trip.arrivee}</p>
                                                            <p className="text-white/80 text-xs mt-1 capitalize">
                                                                {new Date(trip.dateDepart).toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long' })} à {new Date(trip.dateDepart).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                                            </p>
                                                        </div>
                                                    </div>
                                                    <span className="bg-green-500/20 text-green-500 text-xs font-bold px-2 py-1 rounded-full border border-green-500/20">
                                                        {trip.prix} FCFA
                                                    </span>
                                                </div>
                                                
                                                <div className="grid grid-cols-2 gap-4 mb-6">
                                                    <div className="bg-black/20 rounded-lg p-3">
                                                        <p className="text-white/40 text-xs mb-1">Places</p>
                                                        <p className="text-white font-medium flex items-center gap-2">
                                                            <Users className="w-4 h-4 text-primary" />
                                                            {trip.placesDisponibles}
                                                        </p>
                                                    </div>
                                                    <div className="bg-black/20 rounded-lg p-3">
                                                        <p className="text-white/40 text-xs mb-1">Véhicule</p>
                                                        <div className="flex items-center gap-2">
                                                            <Car className="w-4 h-4 text-primary shrink-0" />
                                                            <div className="min-w-0">
                                                                <p className="text-white font-medium text-xs truncate">
                                                                    {trip.vehicleMarque} {trip.vehicleModele}
                                                                </p>
                                                                <p className="text-[10px] text-white/40 font-mono">
                                                                    {trip.vehicleImmatriculation}
                                                                </p>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>

                                            <Button 
                                                variant="outline" 
                                                onClick={() => handleEdit(trip)}
                                                className="w-full border-white/10 text-white hover:bg-primary/20 hover:text-primary hover:border-primary/50 transition-all"
                                            >
                                                Modifier
                                            </Button>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </motion.div>
                    ) : (
                        <motion.div
                            key="passengers"
                            initial={{ opacity: 0, y: 20 }}
                            animate={{ opacity: 1, y: 0 }}
                            exit={{ opacity: 0, y: -20 }}
                            className="space-y-4"
                        >
                            {isLoadingReservations ? (
                                <div className="flex justify-center py-20">
                                    <Loader2 className="w-10 h-10 text-primary animate-spin" />
                                </div>
                            ) : driverReservations.length === 0 ? (
                                <div className="bg-white/5 border border-white/10 rounded-2xl p-12 text-center text-white/40">
                                    <Users className="w-16 h-16 mx-auto mb-4 opacity-20" />
                                    <h3 className="text-xl font-medium mb-2">Aucun passager</h3>
                                    <p>Personne n'a encore réservé sur vos trajets.</p>
                                </div>
                            ) : (
                                <div className="grid grid-cols-1 gap-4">
                                    {driverReservations.map((res) => (
                                        <div key={res.id} className="bg-white/5 border border-white/10 rounded-2xl p-6 hover:bg-white/[0.07] transition-all">
                                            <div className="flex flex-col md:flex-row justify-between gap-6">
                                                <div className="space-y-4 flex-1">
                                                    <div className="flex items-center gap-4">
                                                        <div className="w-12 h-12 rounded-full bg-primary/20 flex items-center justify-center text-primary text-lg font-bold">
                                                            {res.passengerFirstName?.[0]}{res.passengerLastName?.[0]}
                                                        </div>
                                                        <div>
                                                            <h3 className="text-lg font-bold text-white capitalize">
                                                                {res.passengerFirstName} {res.passengerLastName}
                                                            </h3>
                                                            <a 
                                                                href={`https://wa.me/${res.passengerPhone?.replace(/\D/g, '')}`}
                                                                target="_blank"
                                                                className="text-primary text-sm font-medium hover:underline flex items-center gap-1.5"
                                                            >
                                                                <span className="w-2 h-2 rounded-full bg-green-500 animate-pulse" />
                                                                {res.passengerPhone}
                                                            </a>
                                                        </div>
                                                    </div>

                                                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
                                                        <div className="flex items-center gap-3 text-white/60">
                                                            <MapPin className="w-4 h-4 text-primary" />
                                                            <span className="text-sm font-medium capitalize">{res.depart} → {res.arrivee}</span>
                                                        </div>
                                                        <div className="flex items-center gap-3 text-white/60">
                                                            <CheckCircle2 className="w-4 h-4 text-primary" />
                                                            <span className="text-sm font-medium">{res.places} place{res.places > 1 ? 's' : ''} réservée{res.places > 1 ? 's' : ''}</span>
                                                        </div>
                                                    </div>
                                                </div>

                                                <div className="flex flex-row md:flex-col justify-between md:text-right items-center md:items-end border-t md:border-t-0 md:border-l border-white/10 pt-4 md:pt-0 md:pl-6">
                                                    <div>
                                                        <p className="text-white font-bold capitalize">
                                                            {new Date(res.dateDepart).toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'short' })}
                                                        </p>
                                                        <p className="text-white/40 text-sm">
                                                            À {new Date(res.dateDepart).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                                        </p>
                                                    </div>
                                                    <span className={`text-[10px] font-bold px-3 py-1 rounded-full uppercase tracking-tighter ${
                                                        res.status === 'CONFIRMED' 
                                                            ? 'bg-green-500/10 text-green-400 border border-green-500/20' 
                                                            : 'bg-red-500/10 text-red-400 border border-red-500/20'
                                                    }`}>
                                                        {res.status}
                                                    </span>
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </motion.div>
                    )}
                </AnimatePresence>
            </div>
        </div>
        </>
    );
}
