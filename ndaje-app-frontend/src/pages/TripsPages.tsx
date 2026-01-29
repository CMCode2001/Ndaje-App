import React, { useEffect, useState, useMemo } from "react";
import { Navbar } from "@/components/Navbar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { MapPin, AlertCircle, Loader2, Filter, X, Calendar, Users, Map as MapIcon, Ticket, RefreshCw } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";
import { fetchAvailableTrips, type Trip } from "@/api/api";
import { ReservationService } from "@/services/ReservationService";
import { useAuth } from "@/context/AuthContext";
import { useNavigate, useSearchParams } from "react-router-dom";
import { toast } from "sonner";
import Illustration from "@/assets/svg/2.svg";
import { MapContainer, TileLayer, Marker, Popup, Polyline } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import Whatsapp from "@/assets/img/icons8-whatsapp.gif";
import { Share2 } from "lucide-react";

// Custom Marker Creator
const createCustomIcon = (type: 'depart' | 'arrivee' = 'depart') => {
  const color = type === 'depart' ? '#1ba3ef' : '#111b42';
  return L.divIcon({
    className: 'custom-div-icon',
    html: `<div style="
      background-color: ${color}; 
      width: 14px; 
      height: 14px; 
      border-radius: 50%; 
      border: 2px solid white; 
      box-shadow: 0 0 10px rgba(0,0,0,0.3);
    "></div>`,
    iconSize: [14, 14],
    iconAnchor: [7, 7],
    popupAnchor: [0, -7]
  });
};

interface FilterState {
  depart: string;
  arrivee: string;
  date: string;
}

export function TripsPages() {
  const [trips, setTrips] = useState<Trip[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showFilters, setShowFilters] = useState(false);
  const [showMap, setShowMap] = useState(false);
  const [filters, setFilters] = useState<FilterState>({
    depart: "",
    arrivee: "",
    date: ""
  });

  const { user, token, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [bookingLoading, setBookingLoading] = useState<any | null>(null);
  const [bookingMessage, setBookingMessage] = useState<{ type: 'success' | 'error', text: string } | null>(null);
  const [selectedSeats, setSelectedSeats] = useState<Record<string, number>>({});

  // Sync search params with filters
  useEffect(() => {
    const departParam = searchParams.get("depart");
    const arriveeParam = searchParams.get("arrivee");
    const dateParam = searchParams.get("date");

    if (departParam || arriveeParam || dateParam) {
      setFilters({
        depart: departParam || "",
        arrivee: arriveeParam || "",
        date: dateParam || ""
      });
      // Automatically open filters if something is pre-filled
      setShowFilters(true);
    }
  }, [searchParams]);

  const handleShare = async (trip: Trip) => {
    const shareData = {
      title: 'Voyage Ndaje App',
      text: `Rejoignez-moi pour ce trajet de ${trip.depart} à ${trip.arrivee} le ${new Date(trip.dateDepart).toLocaleDateString('fr-FR')} !`,
      url: window.location.href,
    };

    try {
      if (navigator.share) {
        await navigator.share(shareData);
      } else {
        await navigator.clipboard.writeText(`${shareData.text} ${shareData.url}`);
        toast.info("Lien du trajet copié dans le presse-papier !");
      }
    } catch (err) {
      console.log('Error sharing', err);
    }
  };

  const fetchTripsData = async () => {
    console.log("Fetching trips data...");
    setLoading(true);
    setBookingMessage(null);
    try {
      const response = await fetchAvailableTrips();
      console.log("API Response:", response);
      // Handle both wrapped and unwrapped response
      const data = Array.isArray(response) ? response : (response.data || []);
      const success = Array.isArray(response) ? true : response.success;

      console.log("Parsed Data:", data, "Success:", success);

      if (success) {
        console.log("Setting trips:", data);
        setTrips(data);
      } else {
        const msg = (response as any).message || "Erreur inconnue";
        console.error("API success false:", msg);
        throw new Error(msg);
      }
    } catch (err: any) {
      console.error("API Error caught:", err);
      setError(err.message || "Une erreur est survenue");
      // Fallback Mock Data
      const mockData: Trip[] = [
        { id: 4, driverId: 104, depart: "Guediawaye", arrivee: "Plateau", dateDepart: "2026-02-22T05:53:00", prix: 1200, placesDisponibles: 4 },
        { id: 5, driverId: 105, depart: "Yoff", arrivee: "Point-E", dateDepart: "2026-02-25T05:53:00", prix: 1000, placesDisponibles: 3 },
      ];
      setTrips(mockData);
    } finally {
      setLoading(false);
    }
  };



  useEffect(() => {
    fetchTripsData();
  }, []);

  const handleReserve = async (tripId: any) => {
    if (!isAuthenticated) {
      toast.error("Veuillez vous connecter pour réserver un trajet");
      navigate("/auth");
      return;
    }

    const nbPlaces = selectedSeats[tripId] || 1;
    
    setBookingLoading(tripId);
    setBookingMessage(null);
    try {
      const response = await ReservationService.createReservation({
        tripId: Number(tripId),
        passengerId: String(user?.id),
        places: nbPlaces
      }, token || "");

      const success = response.success !== undefined ? response.success : true;

      if (success) {
        setBookingMessage({ type: 'success', text: "Réservation effectuée avec succès ! Redirection..." });
        toast.success("Réservation confirmée !");
        
        // Brief delay for the user to see the success state
        setTimeout(() => {
            navigate("/my-reservations");
        }, 1500);

        // Refresh trips anyway in case redirect fails or is slow
        fetchTripsData();
      } else {
        setBookingMessage({ type: 'error', text: response.message || "Erreur lors de la réservation" });
        toast.error(response.message || "Erreur lors de la réservation");
      }
    } catch (err: any) {
      if (err.message?.includes("401") || err.status === 401) {
          toast.error("Session expirée");
          logout();
          navigate("/auth");
      } else {
          setBookingMessage({ type: 'error', text: err.message || "Une erreur est survenue" });
          toast.error(err.message || "Une erreur est survenue");
      }
    } finally {
      setBookingLoading(null);
    }
  };

  // Reactive filtering using useMemo
  const filteredTrips = useMemo(() => {
    // 1. D'abord filtrer les trajets complets
    let result = trips.filter(trip => trip.placesDisponibles > 0);

    // 2. Appliquer les filtres de recherche
    if (filters.depart) {
      result = result.filter(trip => 
        trip.depart.toLowerCase().includes(filters.depart.toLowerCase())
      );
    }

    if (filters.arrivee) {
      result = result.filter(trip => 
        trip.arrivee.toLowerCase().includes(filters.arrivee.toLowerCase())
      );
    }

    if (filters.date) {
      result = result.filter(trip => {
        const tripDate = new Date(trip.dateDepart).toISOString().split('T')[0];
        return tripDate === filters.date;
      });
    }

    return result;
  }, [filters, trips]);

  const resetFilters = () => {
    setFilters({
      depart: "",
      arrivee: "",
      date: ""
    });
  };

  const container = {
    hidden: { opacity: 0 },
    show: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1
      }
    }
  };

  const item = {
    hidden: { opacity: 0, y: 20 },
    show: { opacity: 1, y: 0 }
  };

  // Case-insensitive coordinate database (keys in lowercase)
  const cityCoordinates: Record<string, [number, number]> = {
    "dakar": [14.6928, -17.4467],
    "saint-louis": [16.0179, -16.4897],
    "thies": [14.7886, -16.9260],
    "thiès": [14.7886, -16.9260],
    "touba": [14.8500, -15.8833],
    "mbour": [14.4167, -16.9667],
    "kaolack": [14.1333, -16.0667],
    "guediawaye": [14.7667, -17.4000],
    "guédiawaye": [14.7667, -17.4000],
    "pikine": [14.7500, -17.3833],
    "rufisque": [14.7167, -17.2667],
    "ziguinchor": [12.5833, -16.2667],
    "diourbel": [14.6500, -16.2333],
    "louga": [15.6167, -16.2167],
    "tambacounda": [13.7667, -13.6667],
    "kolda": [12.8833, -14.9500],
    "matam": [15.6500, -13.3333],
    "fatick": [14.3333, -16.4000],
    "kaffrine": [14.1000, -15.5500],
    "kédougou": [12.5500, -12.1833],
    "sédhiou": [12.7000, -15.5500],
    "plateau": [14.6708, -17.4381],
    "yoff": [14.7500, -17.4833],
    "point-e": [14.7167, -17.4667],
    "almadies": [14.7475, -17.5147],
    "parcelles assainies": [14.7523, -17.4394]
  };

  const getCoords = (cityName: string) => {
    if (!cityName) return null;
    return cityCoordinates[cityName.toLowerCase().trim()] || null;
  };

  return (
    <div className="min-h-screen flex flex-col relative text-brand-dark overflow-hidden">
      {/* Background Image with Overlay */}
      <div 
        className="fixed inset-0 z-0 bg-cover bg-center bg-no-repeat"
        style={{ 
          backgroundImage: `url('https://laviesenegalaise.com/wp-content/uploads/2024/11/Ouverture-a-la-circulation-de-lautopont-Front-de-Terre-a-Dakar.jpg')` 
        }}
      />
      <div className="fixed inset-0 z-0 bg-white/90 backdrop-blur-sm" />

      <Navbar />

      <main className="flex-1 pt-32 pb-20 container mx-auto px-4 relative z-10">
        {/* Header Section */}
        <div className="flex flex-col lg:flex-row justify-between items-start lg:items-end mb-8 md:mb-12 gap-6">
          <div className="space-y-3">
             <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-100 text-primary text-[10px] md:text-xs font-bold uppercase tracking-wider">
               {filteredTrips.length} Trajet{filteredTrips.length > 1 ? 's' : ''} disponible{filteredTrips.length > 1 ? 's' : ''}
             </div>
             <h1 className="text-3xl md:text-4xl lg:text-5xl font-bold text-brand-dark leading-tight">
               Où allez-vous <span className="text-primary">aujourd'hui ?</span>
             </h1>
             <p className="text-gray-500 max-w-xl text-sm md:text-base">
               Explorez les meilleurs trajets au meilleur prix. Conduisez moins, partagez plus.
             </p>
          </div>
          
          <div className="flex flex-wrap gap-2 md:gap-3 w-full lg:w-auto">
             <AnimatePresence>
               {(filters.depart || filters.arrivee || filters.date) && (
                 <motion.div
                   initial={{ opacity: 0, x: 20 }}
                   animate={{ opacity: 1, x: 0 }}
                   exit={{ opacity: 0, x: 20 }}
                 >
                   <Button 
                     variant="ghost" 
                     className="gap-2 rounded-xl h-12 text-red-500 hover:text-red-600 hover:bg-red-50"
                     onClick={resetFilters}
                   >
                     <X className="w-5 h-5" /> Réinitialiser
                   </Button>
                 </motion.div>
               )}
             </AnimatePresence>
             <Button 
               variant="outline" 
               size="icon"
               className="rounded-xl h-12 w-12 border-gray-200 text-gray-400 hover:text-primary hover:border-primary/20 hover:bg-blue-50"
               onClick={fetchTripsData}
               disabled={loading}
               title="Actualiser les trajets"
             >
               <RefreshCw className={`w-5 h-5 ${loading ? 'animate-spin' : ''}`} />
             </Button>
             <Button 
               variant="outline" 
               className="flex-1 lg:flex-none gap-2 rounded-xl h-11 md:h-12 border-gray-200 text-gray-600 hover:text-primary hover:border-primary/20 hover:bg-blue-50 text-sm"
               onClick={() => setShowFilters(!showFilters)}
             >
               <Filter className="w-4 h-4 md:w-5 md:h-5" /> Filtres
             </Button>
             <Button 
               className="flex-1 lg:flex-none gap-2 rounded-xl h-11 md:h-12 shadow-lg shadow-primary/20 text-sm"
               onClick={() => setShowMap(!showMap)}
             >
               <MapIcon className="w-4 h-4 md:w-5 md:h-5" /> {showMap ? "Liste" : "Carte"}
             </Button>
          </div>
        </div>

        {/* Booking Feedback */}
        <AnimatePresence>
          {bookingMessage && (
            <motion.div
              initial={{ opacity: 0, y: -20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
              className={`mb-6 p-4 rounded-2xl flex items-center gap-3 border ${
                bookingMessage.type === 'success' 
                  ? 'bg-green-50 text-green-700 border-green-100' 
                  : 'bg-red-50 text-red-700 border-red-100'
              }`}
            >
              {bookingMessage.type === 'success' ? <Ticket className="w-5 h-5" /> : <AlertCircle className="w-5 h-5" />}
              <p className="font-medium">{bookingMessage.text}</p>
              <Button 
                variant="ghost" 
                size="sm" 
                className="ml-auto" 
                onClick={() => setBookingMessage(null)}
              >
                <X className="w-4 h-4" />
              </Button>
            </motion.div>
          )}
        </AnimatePresence>

        {/* Filter Panel */}
        <AnimatePresence>
          {showFilters && (
            <motion.div
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: "auto" }}
              exit={{ opacity: 0, height: 0 }}
              className="mb-8 overflow-hidden"
            >
              <div className="bg-white rounded-3xl p-6 shadow-lg border border-gray-100">
                <div className="flex justify-between items-center mb-6">
                  <h3 className="text-xl font-bold text-brand-dark flex items-center gap-2">
                    <Filter className="w-5 h-5 text-primary" />
                    Filtrer les trajets
                  </h3>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => setShowFilters(false)}
                    className="text-gray-400 hover:text-gray-600"
                  >
                    <X className="w-5 h-5" />
                  </Button>
                </div>

                <div className="grid md:grid-cols-1 lg:grid-cols-3 gap-4">
                  <div className="space-y-2">
                    <label className="text-sm font-medium text-gray-600 flex items-center gap-2">
                      <MapPin className="w-4 h-4" />
                      Départ
                    </label>
                    <Input
                      placeholder="Ex: Dakar"
                      value={filters.depart}
                      onChange={(e) => setFilters({...filters, depart: e.target.value.toUpperCase()})}
                      className="rounded-xl bg-gray-50 border-gray-200 focus:bg-white focus:ring-primary/20 transition-all text-brand-dark placeholder:text-gray-400"
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium text-gray-600 flex items-center gap-2">
                      <MapPin className="w-4 h-4" />
                      Arrivée
                    </label>
                    <Input
                      placeholder="Ex: Saint-Louis"
                      value={filters.arrivee}
                      onChange={(e) => setFilters({...filters, arrivee: e.target.value.toUpperCase()})}
                      className="rounded-xl bg-gray-50 border-gray-200 focus:bg-white focus:ring-primary/20 transition-all text-brand-dark placeholder:text-gray-400"
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium text-gray-600 flex items-center gap-2">
                      <Calendar className="w-4 h-4" />
                      Date
                    </label>
                    <Input
                      type="date"
                      value={filters.date}
                      onChange={(e) => setFilters({...filters, date: e.target.value})}
                      className="rounded-xl bg-gray-50 border-gray-200 focus:bg-white focus:ring-primary/20 transition-all text-brand-dark placeholder:text-gray-400"
                    />
                  </div>
                </div>

                <div className="mt-6 flex gap-3">
                  <Button
                    variant="outline"
                    onClick={resetFilters}
                    className="rounded-xl"
                  >
                    Réinitialiser
                  </Button>
                  <Button
                    onClick={() => setShowFilters(false)}
                    className="rounded-xl"
                  >
                    Appliquer ({filteredTrips.length} résultat{filteredTrips.length > 1 ? 's' : ''})
                  </Button>
                </div>
              </div>
            </motion.div>
          )}
        </AnimatePresence>

        {/* Content Section */}
        {loading ? (
          <div className="flex justify-center items-center py-20">
            <Loader2 className="w-12 h-12 text-primary animate-spin" />
          </div>
        ) : error ? (
           <div className="bg-red-50 text-red-600 p-8 rounded-2xl flex flex-col items-center text-center space-y-4 max-w-lg mx-auto border border-red-100">
             <AlertCircle className="w-12 h-12" />
             <p className="font-medium text-lg">{error}</p>
             <Button variant="outline" onClick={() => window.location.reload()} className="border-red-200 hover:bg-red-100 text-red-700">
               Réessayer
             </Button>
           </div>
        ) : filteredTrips.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20 text-center space-y-6">
            <img src={Illustration} alt="Aucun trajet" className="w-64 md:w-80 opacity-80" />
            <div className="space-y-2">
               <h3 className="text-2xl font-bold text-brand-dark">Aucun trajet trouvé</h3>
               <p className="text-gray-500 max-w-md mx-auto">
                 Désolé, nous n'avons trouvé aucun trajet correspondant à vos critères.
               </p>
            </div>
            <Button size="lg" className="rounded-full px-8 shadow-xl shadow-primary/20" onClick={resetFilters}>
               Réinitialiser les filtres
            </Button>
          </div>
        ) : showMap ? (
          <div className="bg-white rounded-3xl overflow-hidden shadow-xl border border-gray-100 h-[500px] md:h-[600px]">
            <MapContainer
              center={[14.6928, -17.4467]}
              zoom={8}
              style={{ height: '100%', width: '100%' }}
              className="z-0"
            >
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />
              {filteredTrips.map((trip) => {
                const departCoords = getCoords(trip.depart);
                const arriveeCoords = getCoords(trip.arrivee);
                
                return (
                  <React.Fragment key={trip.id}>
                    {departCoords && (
                      <Marker position={departCoords} icon={createCustomIcon('depart')}>
                        <Popup className="premium-popup">
                          <div className="p-1 space-y-3 min-w-[200px]">
                            <div className="flex justify-between items-start border-b border-gray-100 pb-2">
                              <div>
                                <h3 className="font-bold text-brand-dark flex items-center gap-1">
                                  {trip.depart} <span className="text-gray-400">→</span> {trip.arrivee}
                                </h3>
                                <p className="text-xs text-gray-500">
                                  {new Date(trip.dateDepart).toLocaleDateString('fr-FR', { weekday: 'short', day: 'numeric', month: 'short' })} • {new Date(trip.dateDepart).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                </p>
                              </div>
                              <div className="text-right">
                                <p className="text-primary font-bold text-sm">{trip.prix} CFA</p>
                              </div>
                            </div>
                            
                            <div className="flex items-center gap-2">
                              <div className="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center text-gray-400">
                                <Users className="w-4 h-4" />
                              </div>
                              <div>
                                <p className="text-[10px] text-gray-400 uppercase font-bold">Conducteur</p>
                                <p className="text-xs font-bold text-brand-dark">
                                  {trip.driverFirstName} {trip.driverLastName}
                                </p>
                              </div>
                            </div>
                            {/* places */}
                            
                            {trip.placesDisponibles > 0 && (
                              <div className="flex items-center justify-between bg-gray-50 p-2 rounded-xl">
                                <span className="text-[10px] font-semibold text-gray-500 pl-1 uppercase">Places</span>
                                <div className="flex items-center gap-2">
                                  <button 
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      setSelectedSeats(prev => ({ ...prev, [trip.id]: Math.max(1, (prev[trip.id] || 1) - 1) }));
                                    }}
                                    className="w-6 h-6 rounded-lg bg-white border border-gray-200 flex items-center justify-center text-gray-600 hover:border-primary hover:text-primary transition-all text-xs"
                                  >-</button>
                                  <span className="font-bold text-brand-dark w-3 text-center text-xs">{selectedSeats[trip.id] || 1}</span>
                                  <button 
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      setSelectedSeats(prev => ({ ...prev, [trip.id]: Math.min(trip.placesDisponibles, (prev[trip.id] || 1) + 1) }));
                                    }}
                                    className="w-6 h-6 rounded-lg bg-white border border-gray-200 flex items-center justify-center text-gray-600 hover:border-primary hover:text-primary transition-all text-xs"
                                  >+</button>
                                </div>
                              </div>
                            )}

                            <div className="flex items-center gap-2">
                              <button
                                onClick={() => handleShare(trip)}
                                className="w-8 h-8 rounded-lg bg-white border border-gray-200 text-gray-400 flex items-center justify-center hover:text-primary hover:border-primary/20 transition shrink-0"
                                title="Partager ce trajet"
                              >
                                <Share2 className="w-4 h-4" />
                              </button>
                              
                              <Button 
                                size="sm" 
                                className="flex-1 h-8 rounded-lg text-xs"
                                onClick={() => handleReserve(trip.id)}
                              >
                                Réserver {selectedSeats[trip.id] > 1 ? `${selectedSeats[trip.id]} places` : ''}
                              </Button>
                            </div>
                          </div>
                        </Popup>
                      </Marker>
                    )}
                    
                    {departCoords && arriveeCoords && (
                      <Polyline 
                        positions={[departCoords, arriveeCoords]} 
                        pathOptions={{ 
                          color: '#1ba3ef', 
                          weight: 3, 
                          opacity: 0.6,
                          dashArray: '5, 10',
                          lineCap: 'round'
                        }} 
                      />
                    )}

                    {arriveeCoords && (
                       <Marker position={arriveeCoords} icon={createCustomIcon('arrivee')} />
                    )}
                  </React.Fragment>
                );
              })}
            </MapContainer>
          </div>
        ) : (
          <div className="flex flex-col lg:flex-row gap-8 items-start">
             
             {/* Main List */}
            <motion.div 
  variants={container}
  initial="hidden"
  animate="show"
  className="flex-1 grid md:grid-cols-2 lg:grid-cols-2 gap-6 w-full"
>
  {filteredTrips.map((trip) => (
    <motion.div key={trip.id} variants={item}>
      <div className="bg-white rounded-3xl p-5 shadow-sm border border-gray-100 hover:shadow-xl hover:shadow-primary/5 hover:-translate-y-1 transition-all duration-300 group cursor-pointer relative overflow-hidden">
        
        <div className="absolute top-0 left-0 w-1 h-full bg-primary transform scale-y-0 group-hover:scale-y-100 transition-transform duration-300 origin-bottom" />

        {/* HEADER */}
        <div className="flex justify-between items-start mb-4">
          <div className="space-y-1">
            <div className="font-bold text-lg text-brand-dark flex items-center gap-2">
              {new Date(trip.dateDepart).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              <span className="text-gray-300 mx-1">•</span>
              <span className="text-gray-500 font-medium text-sm capitalize">
                {new Date(trip.dateDepart).toLocaleDateString([], { weekday: 'long', day: 'numeric', month: 'short' })}
              </span>
            </div>
          </div>

          {/* PRIX + PLACES */}
          <div className="text-right">
            <div className="text-2xl font-bold text-primary fontLogo">
              {trip.prix.toLocaleString()} FCFA
            </div>
            <div className={`mt-1 text-xs font-bold inline-block px-3 py-1 rounded-full border
              ${trip.placesDisponibles > 0 
                ? 'bg-green-50 text-green-700 border-green-100' 
                : 'bg-gray-100 text-gray-500 border-gray-200'}`}
            >
              {trip.placesDisponibles > 0
                ? `${trip.placesDisponibles} place${trip.placesDisponibles > 1 ? 's' : ''} disponible${trip.placesDisponibles > 1 ? 's' : ''}`
                : 'Complet'}
            </div>
          </div>
        </div>

        {/* TRAJET */}
        <div className="relative pl-4 border-l-2 border-dashed border-gray-200 ml-2 space-y-5 py-1">
          <div className="relative">
            <div className="absolute -left-[21px] top-1 w-3 h-3 bg-white border-2 border-brand-dark rounded-full group-hover:border-primary transition-colors" />
            <h3 className="font-bold text-brand-dark text-lg">{trip.depart}</h3>
          </div>

          <div className="relative">
            <div className="absolute -left-[21px]   w-3 h-3 bg-brand-dark border-2 border-brand-dark rounded-full group-hover:bg-primary group-hover:border-primary transition-colors" />
            <h3 className="font-bold text-brand-dark text-lg">{trip.arrivee}</h3>
          </div>
        </div>

        {/* FOOTER */}
        <div className="mt-4 pt-4 border-t border-gray-50 flex flex-col sm:flex-row sm:items-end justify-between gap-4">
          
          {/* LEFT: CONDUCTEUR + SEATS */}
          <div className="flex-1 space-y-3">
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 md:w-10 md:h-10 bg-gray-100 rounded-full flex items-center justify-center text-gray-500 shrink-0">
                <Users className="w-5 h-5 md:w-6 md:h-6" />
              </div>
              <div className="min-w-0">
                <p className="text-sm font-bold text-brand-dark capitalize leading-tight truncate">
                  {trip.driverFirstName && trip.driverLastName 
                    ? `${trip.driverFirstName} ${trip.driverLastName}` 
                    : `Conducteur #${trip.driverId}`}
                </p>
                <div className="flex items-center text-[10px] text-yellow-500 gap-1 mt-0.5">
                  <span>★ 5.0</span>
                  <span className="text-gray-300">•</span>
                  <span className="text-gray-400 font-medium">Vérifié</span>
                </div>
              </div>
            </div>

            {trip.placesDisponibles > 0 && (
              <div className="flex items-center gap-2 bg-gray-50/50 p-1 rounded-xl w-fit">
                <span className="text-[10px] font-bold text-gray-400 px-1.5 uppercase tracking-wider">Places</span>
                <div className="flex items-center gap-2">
                  <button 
                    onClick={() => setSelectedSeats(prev => ({ ...prev, [trip.id]: Math.max(1, (prev[trip.id] || 1) - 1) }))}
                    className="w-7 h-7 rounded-lg bg-white border border-gray-200 flex items-center justify-center text-gray-600 hover:border-primary hover:text-primary transition-all text-xs"
                  >-</button>
                  <span className="font-bold text-brand-dark w-4 text-center text-xs">{selectedSeats[trip.id] || 1}</span>
                  <button 
                    onClick={() => setSelectedSeats(prev => ({ ...prev, [trip.id]: Math.min(trip.placesDisponibles, (prev[trip.id] || 1) + 1) }))}
                    className="w-7 h-7 rounded-lg bg-white border border-gray-200 flex items-center justify-center text-gray-600 hover:border-primary hover:text-primary transition-all text-xs"
                  >+</button>
                </div>
              </div>
            )}
          </div>

          {/* RIGHT: ACTIONS */}
          <div className="flex items-center gap-2 w-full sm:w-auto">
            <button
              onClick={() => handleShare(trip)}
              className="h-10 w-10 md:h-11 md:w-11 rounded-xl bg-white border border-gray-200 text-gray-400 flex items-center justify-center hover:text-primary hover:border-primary/20 hover:bg-blue-50 transition shrink-0"
              title="Partager ce trajet"
            >
              <Share2 className="w-5 h-5" />
            </button>

            <button
              onClick={() => handleReserve(trip.id)}
              disabled={trip.placesDisponibles === 0 || bookingLoading === trip.id}
              className={`flex-1 sm:flex-none flex items-center justify-center gap-2 px-4 md:px-6 h-10 md:h-11 rounded-xl text-sm font-bold transition
                ${trip.placesDisponibles > 0
                  ? 'bg-primary text-white hover:bg-primary/90'
                  : 'bg-gray-200 text-gray-400 cursor-not-allowed'}`}
            >
              {bookingLoading === trip.id ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                <Ticket className="w-4 h-4" />
              )}
              {bookingLoading === trip.id ? '...' : 'Réserver'}
            </button>

            <a
              href={`https://wa.me/2217${trip.driverPhone}?text=${encodeURIComponent(`Bonjour, ${trip.driverFirstName} ${trip.driverLastName} je voudrais avoir des informations supplémentaires à propos de votre trajet : ${trip.depart} - ${trip.arrivee}`)}`}
              target="_blank"
              rel="noopener noreferrer"
              className="h-10 w-10 md:h-11 md:w-11 rounded-xl bg-green-500 text-white flex items-center justify-center hover:bg-green-600 transition shrink-0"
            >
              <img src={Whatsapp} alt="Whatsapp"  />
            </a>
          </div>
        </div>
      </div>
    </motion.div>
  ))}
</motion.div>

             
             {/* Sidebar */}
             <div className="hidden lg:block w-80 sticky top-24 space-y-8">
                <div className="bg-brand-dark rounded-3xl p-8 text-white relative overflow-hidden shadow-2xl">
                   <div className="absolute top-0 right-0 w-32 h-32 bg-primary/20 rounded-full blur-3xl" />
                   <h3 className="text-xl font-bold mb-4 relative z-10">Conduisez en toute sécurité</h3>
                   <p className="text-white/70 text-sm mb-6 relative z-10">
                     Tous nos conducteurs sont vérifiés pour vous assurer un trajet serein.
                   </p>
                   <img src={Illustration} className="w-full relative z-10 drop-shadow-lg opacity-90" alt="Illustration" />
                </div>
             </div>
          </div>
        )}
      </main>
      
      <footer className="py-8 bg-white border-t border-gray-100 text-center text-gray-400 text-sm relative z-10">
        <p>&copy; 2026 Ndaje App.</p>
      </footer>
    </div>
  );
}
