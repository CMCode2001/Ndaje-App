import { useNavigate } from "react-router-dom";
import { useState } from "react";
import { Search, MapPin, Calendar } from "lucide-react";
import { Button } from "@/components/ui/button";

export function SearchBar() {
  const navigate = useNavigate();
  const [depart, setDepart] = useState("");
  const [arrivee, setArrivee] = useState("");
  const [date, setDate] = useState("");

  const handleSearch = () => {
    const params = new URLSearchParams();
    if (depart) params.append("depart", depart.toUpperCase());
    if (arrivee) params.append("arrivee", arrivee.toUpperCase());
    if (date) params.append("date", date);
    
    navigate(`/trips?${params.toString()}`);
  };
  return (
    <div className="w-full max-w-4xl bg-white rounded-2xl md:rounded-full p-2 md:p-2 flex flex-col md:flex-row shadow-2xl shadow-black/20 animate-in fade-in slide-in-from-bottom-4 duration-700">
      <div className="flex-1 px-6 py-4 md:py-2 border-b md:border-b-0 md:border-r border-gray-100 flex items-center gap-4 hover:bg-gray-50 transition-colors rounded-t-xl md:rounded-l-full group cursor-pointer relative">
        <div className="p-2 rounded-full bg-blue-50 group-hover:bg-blue-100 transition-colors">
          <MapPin className="w-5 h-5 text-primary transition-colors" />
        </div>
        <div className="flex flex-col flex-1">
          <label className="text-xs text-gray-400 font-semibold uppercase tracking-wider mb-0.5">Départ</label>
          <input 
            type="text" 
            placeholder="D'où partez-vous ?" 
            value={depart}
            onChange={(e) => setDepart(e.target.value.toUpperCase())}
            className="w-full bg-transparent border-none p-0 text-gray-800 placeholder:text-gray-400 focus:ring-0 font-medium sm:text-base outline-none truncate"
          />
        </div>
      </div>

      <div className="flex-1 px-6 py-4 md:py-2 border-b md:border-b-0 md:border-r border-gray-100 flex items-center gap-4 hover:bg-gray-50 transition-colors group cursor-pointer relative">
        <div className="p-2 rounded-full bg-blue-50 group-hover:bg-blue-100 transition-colors">
          <MapPin className="w-5 h-5 text-primary transition-colors" />
        </div>
        <div className="flex flex-col flex-1">
          <label className="text-xs text-gray-400 font-semibold uppercase tracking-wider mb-0.5">Arrivée</label>
          <input 
            type="text" 
            placeholder="Où allez-vous ?" 
            value={arrivee}
            onChange={(e) => setArrivee(e.target.value.toUpperCase())}
            className="w-full bg-transparent border-none p-0 text-gray-800 placeholder:text-gray-400 focus:ring-0 font-medium sm:text-base outline-none truncate"
          />
        </div>
      </div>

      <div className="flex-[0.7] px-6 py-4 md:py-2 flex items-center gap-4 hover:bg-gray-50 transition-colors group cursor-pointer relative">
        <div className="p-2 rounded-full bg-blue-50 group-hover:bg-blue-100 transition-colors">
          <Calendar className="w-5 h-5 text-primary transition-colors" />
        </div>
        <div className="flex flex-col flex-1">
          <label className="text-xs text-gray-400 font-semibold uppercase tracking-wider mb-0.5">Date</label>
          <input 
            type="date" 
            value={date}
            onChange={(e) => setDate(e.target.value)}
            className="w-full bg-transparent border-none p-0 text-gray-800 placeholder:text-gray-400 focus:ring-0 font-medium sm:text-base outline-none cursor-pointer"
          />
        </div>
      </div>

      <div className="p-1">
        <Button 
          onClick={handleSearch}
          size="lg" 
          className="w-full md:w-auto h-14 md:h-full rounded-xl md:rounded-full md:px-8 text-lg font-bold shadow-lg shadow-primary/30 hover:shadow-primary/50 transition-all duration-300 transform hover:scale-[1.02]"
        >
          <span className="md:hidden">Rechercher</span>
          <Search className="w-6 h-6 md:block hidden" />
        </Button>
      </div>
    </div>
  );
}
