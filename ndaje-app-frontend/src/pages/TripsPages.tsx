import { useEffect, useState } from "react";
import { Navbar } from "@/components/Navbar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { MapPin, AlertCircle, Loader2, Filter, X, Calendar, DollarSign, Users, Map as MapIcon, Phone, Ticket } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";
import { BASE_URL_RESERVATION } from "@/api/api";
import Illustration from "@/assets/svg/2.svg";
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Fix for default marker icons in Leaflet
import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';

let DefaultIcon = L.icon({
  iconUrl: icon,
  shadowUrl: iconShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41]
});

L.Marker.prototype.options.icon = DefaultIcon;

interface Trip {
  id: number;
  conducteurId: number;
  depart: string;
  arrivee: string;
  dateDepart: string;
  placesDisponibles: number;
  prix: number;
}

interface ApiResponse {
  success: boolean;
  message: string;
  data: Trip[];
}

interface FilterState {
  depart: string;
  arrivee: string;
  date: string;
  maxPrice: string;
  minSeats: string;
}

export function TripsPages() {
  const [trips, setTrips] = useState<Trip[]>([]);
  const [filteredTrips, setFilteredTrips] = useState<Trip[]>([]);
  const [loading, setLoading] = useState(true);
  const [error] = useState<string | null>(null);
  const [showFilters, setShowFilters] = useState(false);
  const [showMap, setShowMap] = useState(false);
  const [filters, setFilters] = useState<FilterState>({
    depart: "",
    arrivee: "",
    date: "",
    maxPrice: "",
    minSeats: ""
  });

  useEffect(() => {
    const fetchTrips = async () => {
      try {
        const response = await fetch(`${BASE_URL_RESERVATION}/trips/available`);
        if (!response.ok) {
           throw new Error("Impossible de récupérer les trajets");
        }
        const apiResponse: ApiResponse = await response.json();
        if (apiResponse.success) {
            setTrips(apiResponse.data);
            setFilteredTrips(apiResponse.data);
        } else {
            throw new Error(apiResponse.message || "Erreur inconnue");
        }
      } catch (err) {
        console.error("API Error:", err);
         // Fallback Mock Data
        const mockData = [
          { id: 4, conducteurId: 104, depart: "Guediawaye", arrivee: "Plateau", dateDepart: "2026-02-22T05:53:00", prix: 1200, placesDisponibles: 4 },
          { id: 5, conducteurId: 105, depart: "Yoff", arrivee: "Point-E", dateDepart: "2026-02-25T05:53:00", prix: 1000, placesDisponibles: 3 },
        ];
        setTrips(mockData);
        setFilteredTrips(mockData);
      } finally {
        setLoading(false);
      }
    };

    fetchTrips();
  }, []);

  // Apply filters
  useEffect(() => {
    let result = [...trips];

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

    if (filters.maxPrice) {
      result = result.filter(trip => trip.prix <= parseInt(filters.maxPrice));
    }

    if (filters.minSeats) {
      result = result.filter(trip => trip.placesDisponibles >= parseInt(filters.minSeats));
    }

    setFilteredTrips(result);
  }, [filters, trips]);

  const resetFilters = () => {
    setFilters({
      depart: "",
      arrivee: "",
      date: "",
      maxPrice: "",
      minSeats: ""
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

  // Mock coordinates for Senegalese cities
  const cityCoordinates: Record<string, [number, number]> = {
    "Dakar": [14.6928, -17.4467],
    "Saint-Louis": [16.0179, -16.4897],
    "Thiès": [14.7886, -16.9260],
    "Touba": [14.8500, -15.8833],
    "Mbour": [14.4167, -16.9667],
    "Kaolack": [14.1333, -16.0667],
    "Guediawaye": [14.7667, -17.4000],
    "Plateau": [14.6708, -17.4381],
    "Yoff": [14.7500, -17.4833],
    "Point-E": [14.7167, -17.4667]
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
        <div className="flex flex-col md:flex-row justify-between items-end mb-12 gap-6">
          <div className="space-y-2">
             <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-100 text-primary text-xs font-bold uppercase tracking-wider">
               {filteredTrips.length} Trajet{filteredTrips.length > 1 ? 's' : ''} disponible{filteredTrips.length > 1 ? 's' : ''}
             </div>
             <h1 className="text-3xl md:text-5xl font-bold text-brand-dark">
               Où allez-vous <span className="text-primary">aujourd'hui ?</span>
             </h1>
             <p className="text-gray-500 max-w-xl">
               Explorez les meilleurs trajets au meilleur prix. Conduisez moins, partagez plus.
             </p>
          </div>
          
          <div className="flex gap-3">
             <Button 
               variant="outline" 
               className="gap-2 rounded-xl h-12 border-gray-200 text-gray-600 hover:text-primary hover:border-primary/20 hover:bg-blue-50"
               onClick={() => setShowFilters(!showFilters)}
             >
               <Filter className="w-5 h-5" /> Filtres
             </Button>
             <Button 
               className="gap-2 rounded-xl h-12 shadow-lg shadow-primary/20"
               onClick={() => setShowMap(!showMap)}
             >
               <MapIcon className="w-5 h-5" /> {showMap ? "Liste" : "Carte"}
             </Button>
          </div>
        </div>

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

                <div className="grid md:grid-cols-2 lg:grid-cols-5 gap-4">
                  <div className="space-y-2">
                    <label className="text-sm font-medium text-gray-600 flex items-center gap-2">
                      <MapPin className="w-4 h-4" />
                      Départ
                    </label>
                    <Input
                      placeholder="Ex: Dakar"
                      value={filters.depart}
                      onChange={(e) => setFilters({...filters, depart: e.target.value})}
                      className="rounded-xl bg-gray-50 border-gray-200 focus:bg-white focus:ring-primary/20 transition-all"
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
                      onChange={(e) => setFilters({...filters, arrivee: e.target.value})}
                      className="rounded-xl bg-gray-50 border-gray-200 focus:bg-white focus:ring-primary/20 transition-all"
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
                      className="rounded-xl bg-gray-50 border-gray-200 focus:bg-white focus:ring-primary/20 transition-all"
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium text-gray-600 flex items-center gap-2">
                      <DollarSign className="w-4 h-4" />
                      Prix max (CFA)
                    </label>
                    <Input
                      type="number"
                      placeholder="Ex: 5000"
                      value={filters.maxPrice}
                      onChange={(e) => setFilters({...filters, maxPrice: e.target.value})}
                      className="rounded-xl bg-gray-50 border-gray-200 focus:bg-white focus:ring-primary/20 transition-all"
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium text-gray-600 flex items-center gap-2">
                      <Users className="w-4 h-4" />
                      Places min
                    </label>
                    <Input
                      type="number"
                      placeholder="Ex: 2"
                      value={filters.minSeats}
                      onChange={(e) => setFilters({...filters, minSeats: e.target.value})}
                      className="rounded-xl bg-gray-50 border-gray-200 focus:bg-white focus:ring-primary/20 transition-all"
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
          <div className="bg-white rounded-3xl overflow-hidden shadow-xl border border-gray-100" style={{ height: '600px' }}>
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
                const departCoords = cityCoordinates[trip.depart];
                if (!departCoords) return null;
                
                return (
                  <Marker key={trip.id} position={departCoords}>
                    <Popup>
                      <div className="p-2">
                        <h3 className="font-bold text-brand-dark">{trip.depart} → {trip.arrivee}</h3>
                        <p className="text-sm text-gray-600">{new Date(trip.dateDepart).toLocaleString('fr-FR')}</p>
                        <p className="text-primary font-bold mt-1">{trip.prix} CFA</p>
                        <p className="text-xs text-gray-500">{trip.placesDisponibles} place{trip.placesDisponibles > 1 ? 's' : ''}</p>
                      </div>
                    </Popup>
                  </Marker>
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
      <div className="bg-white rounded-3xl p-6 shadow-sm border border-gray-100 hover:shadow-xl hover:shadow-primary/5 hover:-translate-y-1 transition-all duration-300 group cursor-pointer relative overflow-hidden">
        
        <div className="absolute top-0 left-0 w-1 h-full bg-primary transform scale-y-0 group-hover:scale-y-100 transition-transform duration-300 origin-bottom" />

        {/* HEADER */}
        <div className="flex justify-between items-start mb-6">
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
        <div className="relative pl-4 border-l-2 border-dashed border-gray-200 ml-2 space-y-8 py-2">
          <div className="relative">
            <div className="absolute -left-[21px] top-1 w-3 h-3 bg-white border-2 border-brand-dark rounded-full group-hover:border-primary transition-colors" />
            <h3 className="font-bold text-brand-dark text-lg">{trip.depart}</h3>
          </div>

          <div className="relative">
            <div className="absolute -left-[21px] top-1 w-3 h-3 bg-brand-dark border-2 border-brand-dark rounded-full group-hover:bg-primary group-hover:border-primary transition-colors" />
            <h3 className="font-bold text-brand-dark text-lg">{trip.arrivee}</h3>
          </div>
        </div>

        {/* FOOTER */}
        <div className="mt-8 pt-6 border-t border-gray-50 flex items-center justify-between">
          
          {/* CONDUCTEUR */}
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-gray-100 rounded-full flex items-center justify-center text-gray-500">
              <Users className="w-6 h-6" />
            </div>
            <div>
              <p className="text-sm font-bold text-brand-dark">
                Conducteur #{trip.conducteurId}
              </p>
              <div className="flex items-center text-xs text-yellow-500 gap-1">
                <span>★ 5.0</span>
                <span className="text-gray-300">•</span>
                <span className="text-gray-400">Verified</span>
              </div>
            </div>
          </div>

          {/* ACTIONS */}
          <div className="flex items-center gap-2">
            <button
              disabled={trip.placesDisponibles === 0}
              className={`flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-bold transition
                ${trip.placesDisponibles > 0
                  ? 'bg-primary text-white hover:bg-primary/90'
                  : 'bg-gray-200 text-gray-400 cursor-not-allowed'}`}
            >
              <Ticket className="w-4 h-4" />
              Réserver
            </button>

            <a
              href={`https://wa.me/2217XXXXXXX`}
              target="_blank"
              rel="noopener noreferrer"
              className="w-10 h-10 rounded-xl bg-green-500 text-white flex items-center justify-center hover:bg-green-600 transition"
            >
              <Phone className="w-5 h-5" />
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
