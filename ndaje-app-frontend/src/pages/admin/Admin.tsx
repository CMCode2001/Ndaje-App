import { useState, useEffect } from "react";
import { AdminSidebar } from "./components/AdminSidebar";
import { StatsCard } from "./components/StatsCard";
import { Users, Car, TrendingUp, AlertCircle, Search, Loader2, Trash2, Power, CheckCircle } from "lucide-react";
import { Input } from "@/components/ui/input";
import { BASE_URL_ADMIN } from "@/api/api";
import { toast } from "sonner";
import { useAuth } from "@/context/AuthContext";
import { VehicleService, type Vehicle } from "@/services/VehicleService";
import { Switch } from "@/components/ui/switch";

interface UserResponse {
  id: string;
  prenom: string;
  nom: string;
  email: string;
  telephone: string;
  role: string;
  active?: boolean;
  actif?: boolean; // Backend field
  enabled?: boolean;
  dateCreation?: string;
}

export default function AdminPage() {
  const [activeView, setActiveView] = useState<'users' | 'verifications'>('users');
  
  // Users State
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [filteredUsers, setFilteredUsers] = useState<UserResponse[]>([]);
  
  // Vehicles State
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [filteredVehicles, setFilteredVehicles] = useState<Vehicle[]>([]);

  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [roleFilter, setRoleFilter] = useState<'ALL' | 'PASSENGER' | 'DRIVER'>('ALL');
  const [vehicleFilter, setVehicleFilter] = useState<'ALL' | 'VERIFIE' | 'EN_ATTENTE'>('ALL');

  const { token } = useAuth();
  
  // ... fetch functions same as before ... 
  const fetchUsers = async () => { /* ... */ 
    setLoading(true);
    try {
      const response = await fetch(BASE_URL_ADMIN, { headers: { 'Authorization': `Bearer ${token}` } });
      if (!response.ok) throw new Error("Erreur");
      const data = await response.json();
      setUsers(data);
      setFilteredUsers(data);
    } catch (e) { toast.error("Erreur chargement utilisateurs"); }
    finally { setLoading(false); }
  };

  const fetchVehicles = async () => {
      console.log("AdminPage: fetching vehicles...", { token: !!token });
      if (!token) return;
      setLoading(true);
      try {
          const data = await VehicleService.getAllVehicles(token);
          console.log("AdminPage: vehicles data received", data);
          setVehicles(data);
          setFilteredVehicles(data);
      } catch (error) {
          console.error("AdminPage error loading vehicles:", error);
          toast.error("Erreur chargement véhicules");
      } finally {
          setLoading(false);
      }
  };

  useEffect(() => {
    if (token) {
        if (activeView === 'users') fetchUsers();
        else fetchVehicles();
    }
  }, [token, activeView]);

  // Filters ...
  useEffect(() => {
     if (activeView !== 'users') return;
     const lowerQuery = searchQuery.toLowerCase();
     let result = users.filter(user => 
       (user.prenom + " " + user.nom).toLowerCase().includes(lowerQuery) ||
       user.email.toLowerCase().includes(lowerQuery)
     );
     if (roleFilter !== 'ALL') {
       result = result.filter(user => {
         const uRole = (user.role || '').toUpperCase();
         if (roleFilter === 'DRIVER') return uRole === 'DRIVER';
         if (roleFilter === 'PASSENGER') return uRole === 'PASSAGER' || uRole === 'PASSENGER';
         return uRole === roleFilter;
       });
     }
     setFilteredUsers(result);
  }, [searchQuery, roleFilter, users, activeView]);

  useEffect(() => {
      if (activeView !== 'verifications') return;
      const lowerQuery = searchQuery.toLowerCase();
      let result = vehicles.filter(v => 
          (v.marque + " " + v.modele).toLowerCase().includes(lowerQuery) || 
          v.immatriculation.toLowerCase().includes(lowerQuery)
      );
      if (vehicleFilter !== 'ALL') {
          result = result.filter(v => v.statutVerification === vehicleFilter);
      }
      setFilteredVehicles(result);
  }, [searchQuery, vehicleFilter, vehicles, activeView]);

  // ... Actions (toggleStatus, deleteUser, toggleVerification) same as before but ensure they use existing state ...
  const toggleStatus = async (id: string, currentStatus: boolean) => {
    try {
      const response = await fetch(`${BASE_URL_ADMIN}/${id}/status?active=${!currentStatus}`, {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (!response.ok) throw new Error();
      toast.success("Statut modifié");
      setUsers(users.map(u => u.id === id ? { ...u, active: !currentStatus, actif: !currentStatus, enabled: !currentStatus } : u));
    } catch { toast.error("Erreur modification statut"); }
  };
  
  const toggleVehicleVerification = async (vehicle: Vehicle, checked: boolean) => {
      if (!token || !vehicle.id) return;
      const newStatus = checked ? 'VERIFIE' : 'EN_ATTENTE';
      const oldStatus = vehicle.statutVerification;
      
      setVehicles(prev => prev.map(v => v.id === vehicle.id ? { ...v, statutVerification: newStatus } : v));

      try {
          await VehicleService.updateVehicle(vehicle.id, { ...vehicle, statutVerification: newStatus }, token);
          toast.success(`Véhicule ${checked ? 'vérifié' : 'mis en attente'}`);
      } catch (err) {
          setVehicles(prev => prev.map(v => v.id === vehicle.id ? { ...v, statutVerification: oldStatus } : v));
          toast.error("Erreur mise à jour");
      }
  };

  const deleteUser = async (id: string) => {
    if (!confirm("Sur ?")) return;
    try {
      const response = await fetch(`${BASE_URL_ADMIN}/${id}`, { method: 'DELETE', headers: { 'Authorization': `Bearer ${token}` } });
      if (!response.ok) throw new Error();
      toast.success("Utilisateur supprimé");
      setUsers(users.filter(u => u.id !== id));
    } catch { toast.error("Erreur suppression"); }
  };

  const getStatus = (user: UserResponse) => {
    if (typeof user.actif !== 'undefined') return user.actif;
    if (typeof user.active !== 'undefined') return user.active;
    return user.enabled !== false;
  };
  
  // Helpers for UI
  const getRoleLabel = (role: string) => {
    const r = (role || '').toUpperCase();
    if (r === 'DRIVER') return 'Conducteur';
    if (r === 'PASSAGER' || r === 'PASSENGER') return 'Passager';
    if (r === 'ADMIN') return 'Admin';
    return r || 'Inconnu';
  };

  const getRoleBadgeStyle = (role: string) => {
    const r = (role || '').toUpperCase();
    if (r === 'DRIVER') return 'bg-primary/20 text-primary border border-primary/20';
    if (r === 'PASSAGER' || r === 'PASSENGER') return 'bg-purple-500/20 text-purple-400 border border-purple-500/20';
    if (r === 'ADMIN') return 'bg-red-500/20 text-red-400 border border-red-500/20';
    return 'bg-white/10 text-white/40 border border-white/10';
  };

  const stats = [
    { label: "Total Utilisateurs", value: users.length.toString(), trend: "+12%", icon: Users, color: "primary" },
    { label: "Véhicules en Attente", value: vehicles.filter(v => v.statutVerification === 'EN_ATTENTE').length.toString(), trend: "Action Requise", icon: Car, color: "yellow-400" },
    { label: "Conducteurs", value: users.filter(u => (u.role || '').toUpperCase() === 'DRIVER').length.toString(), trend: "+5%", icon: Users, color: "green-400" },
    { label: "Inactifs", value: users.filter(u => !getStatus(u)).length.toString(), trend: "-2%", icon: AlertCircle, color: "red-400" },
  ];

  return (
    <div className="flex h-screen bg-[#050814] text-white">
      <AdminSidebar activeView={activeView} onViewChange={setActiveView} />
      
      <main className="flex-1 overflow-y-auto p-10 space-y-10">
        <header className="flex justify-between items-center">
          <div className="space-y-1">
            <h1 className="text-3xl font-bold">Administration</h1>
            <p className="text-white/40">Gestion globale de la plateforme Ndaje.</p>
          </div>
          <button 
            onClick={() => activeView === 'users' ? fetchUsers() : fetchVehicles()}
            className="p-2 bg-white/5 rounded-xl border border-white/10 hover:bg-white/10 transition-all"
            title="Rafraîchir"
          >
            <TrendingUp className="w-5 h-5 text-primary" />
          </button>
        </header>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {stats.map((stat, i) => (
            <StatsCard key={i} {...stat} icon={stat.icon} color={stat.color} />
          ))}
        </div>

        {activeView === 'users' ? (
        // USERS TABLE
        <div className="space-y-6">
           <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
              <div className="flex items-center gap-4">
                 <h2 className="text-xl font-bold">utilisateurs</h2>
                 <div className="flex p-1 bg-white/5 border border-white/10 rounded-xl space-x-1">
                    {(['ALL', 'DRIVER', 'PASSENGER'] as const).map(role => (
                       <button
                          key={role}
                          onClick={() => setRoleFilter(role)}
                          className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all ${
                             roleFilter === role ? "bg-primary text-white" : "text-white/40 hover:text-white"
                          }`}
                       >
                          {role === 'ALL' ? 'Tous' : role === 'DRIVER' ? 'Conducteurs' : 'Passagers'}
                       </button>
                    ))}
                 </div>
              </div>
              
              <div className="flex gap-3 w-full md:w-auto">
                 <div className="relative flex-1 md:w-64">
                    <Search className="absolute left-3 top-2.5 w-4 h-4 text-white/40" />
                    <Input 
                      placeholder="Nom, email..." 
                      value={searchQuery}
                      onChange={(e) => setSearchQuery(e.target.value)}
                      className="pl-10 bg-white/5 border-white/10 h-10 rounded-xl focus:ring-primary/20" 
                    />
                 </div>
              </div>
           </div>

           <div className="rounded-3xl border border-white/10 bg-white/5 backdrop-blur-md overflow-hidden relative min-h-[400px]">
              {loading && (
                <div className="absolute inset-0 bg-[#050814]/50 backdrop-blur-sm z-20 flex items-center justify-center">
                  <Loader2 className="w-8 h-8 text-primary animate-spin" />
                </div>
              )}

              <table className="w-full text-left">
                 <thead>
                    <tr className="border-b border-white/10 bg-white/5 fontLogo text-white/40">
                       <th className="px-6 py-4 text-xs font-bold uppercase tracking-wider">Utilisateur</th>
                       <th className="px-6 py-4 text-xs font-bold uppercase tracking-wider">Rôle</th>
                       <th className="px-6 py-4 text-xs font-bold uppercase tracking-wider">Statut</th>
                       <th className="px-6 py-4 text-xs font-bold uppercase tracking-wider">Date</th>
                       <th className="px-6 py-4 text-xs font-bold uppercase tracking-wider text-right">Actions</th>
                    </tr>
                 </thead>
                 <tbody className="divide-y divide-white/5">
                    {filteredUsers.length === 0 && !loading ? (
                      <tr>
                        <td colSpan={5} className="px-6 py-20 text-center text-white/40 italic">
                          Aucun utilisateur trouvé.
                        </td>
                      </tr>
                    ) : filteredUsers.map((user) => {
                       const isActive = getStatus(user);
                       return (
                       <tr key={user.id} className="hover:bg-white/5 transition-colors group">
                          <td className="px-6 py-4">
                             <div className="flex items-center gap-3">
                                <div className="w-10 h-10 rounded-full bg-gradient-to-br from-primary to-blue-400 flex items-center justify-center font-bold text-sm shadow-lg text-white">
                                   {user.prenom ? user.prenom.charAt(0) : ''}{user.nom ? user.nom.charAt(0) : ''}
                                </div>
                                <div>
                                   <p className="font-bold text-sm text-white">{user.prenom} {user.nom}</p>
                                   <p className="text-xs text-white/40">{user.email}</p>
                                </div>
                             </div>
                          </td>
                          <td className="px-6 py-4">
                             <span className={`px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider ${getRoleBadgeStyle(user.role)}`}>
                                {getRoleLabel(user.role)}
                             </span>
                          </td>
                          <td className="px-6 py-4">
                             <div className="flex items-center gap-2">
                                <div className={`w-2 h-2 rounded-full ring-4 ring-white/5 ${isActive ? 'bg-green-400' : 'bg-red-400'}`} />
                                <span className="text-sm text-white/60">{isActive ? 'Actif' : 'Inactif'}</span>
                             </div>
                          </td>
                          <td className="px-6 py-4 text-sm text-white/40 font-mono">
                             {user.dateCreation ? new Date(user.dateCreation).toLocaleDateString() : 'N/A'}
                          </td>
                          <td className="px-6 py-4 text-right pr-8">
                             <div className="flex justify-end gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                                <button 
                                  onClick={() => toggleStatus(user.id, isActive)}
                                  className={`p-2 rounded-lg transition-all ${isActive ? 'hover:bg-red-500/10 text-white/40 hover:text-red-400' : 'hover:bg-green-500/10 text-white/40 hover:text-green-400'}`}
                                  title={isActive ? "Désactiver" : "Activer"}
                                >
                                   <Power className="w-5 h-5" />
                                </button>
                                <button 
                                  onClick={() => deleteUser(user.id)}
                                  className="p-2 rounded-lg hover:bg-red-500/10 text-white/40 hover:text-red-500 transition-all"
                                  title="Supprimer"
                                >
                                   <Trash2 className="w-5 h-5" />
                                </button>
                             </div>
                          </td>
                       </tr>
                    );})}
                 </tbody>
              </table>
           </div>
        </div>
        ) : (
        // VERIFICATIONS TABLE
        <div className="space-y-6">
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
              <div className="flex items-center gap-4">
                 <h2 className="text-xl font-bold">Validations Véhicules</h2>
                 <div className="flex p-1 bg-white/5 border border-white/10 rounded-xl space-x-1">
                    {(['ALL', 'EN_ATTENTE', 'VERIFIE'] as const).map(status => (
                       <button
                          key={status}
                          onClick={() => setVehicleFilter(status)}
                          className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all ${
                             vehicleFilter === status ? "bg-primary text-white" : "text-white/40 hover:text-white"
                          }`}
                       >
                          {status === 'ALL' ? 'Tous' : status === 'EN_ATTENTE' ? 'En Attente' : 'Vérifiés'}
                       </button>
                    ))}
                 </div>
              </div>
              
              <div className="flex gap-3 w-full md:w-auto">
                 <div className="relative flex-1 md:w-64">
                    <Search className="absolute left-3 top-2.5 w-4 h-4 text-white/40" />
                    <Input 
                      placeholder="Marque, immatriculation..." 
                      value={searchQuery}
                      onChange={(e) => setSearchQuery(e.target.value)}
                      className="pl-10 bg-white/5 border-white/10 h-10 rounded-xl focus:ring-primary/20" 
                    />
                 </div>
              </div>
           </div>

           <div className="rounded-3xl border border-white/10 bg-white/5 backdrop-blur-md overflow-hidden relative min-h-[400px]">
              {loading && (
                <div className="absolute inset-0 bg-[#050814]/50 backdrop-blur-sm z-20 flex items-center justify-center">
                  <Loader2 className="w-8 h-8 text-primary animate-spin" />
                </div>
              )}

              <table className="w-full text-left">
                 <thead>
                    <tr className="border-b border-white/10 bg-white/5 fontLogo text-white/40">
                       <th className="px-6 py-4 text-xs font-bold uppercase tracking-wider">Véhicule</th>
                       <th className="px-6 py-4 text-xs font-bold uppercase tracking-wider">Détails</th>
                       <th className="px-6 py-4 text-xs font-bold uppercase tracking-wider">Statut</th>
                       <th className="px-6 py-4 text-xs font-bold uppercase tracking-wider text-right">Validation</th>
                    </tr>
                 </thead>
                 <tbody className="divide-y divide-white/5">
                    {filteredVehicles.length === 0 && !loading ? (
                      <tr>
                        <td colSpan={4} className="px-6 py-20 text-center text-white/40 italic">
                          Aucun véhicule trouvé.
                        </td>
                      </tr>
                    ) : filteredVehicles.map((vehicle) => {
                       const isVerified = vehicle.statutVerification === 'VERIFIE';
                       return (
                       <tr key={vehicle.id} className="hover:bg-white/5 transition-colors group">
                          <td className="px-6 py-4">
                             <div className="flex items-center gap-3">
                                <div className="w-10 h-10 rounded-xl bg-white/10 flex items-center justify-center">
                                   <Car className="w-5 h-5 text-white/80" />
                                </div>
                                <div>
                                   <p className="font-bold text-sm text-white">{vehicle.marque} {vehicle.modele}</p>
                                   <p className="text-xs text-white/40 bg-white/5 px-1.5 py-0.5 rounded inline-block mt-0.5">{vehicle.immatriculation}</p>
                                </div>
                             </div>
                          </td>
                          <td className="px-6 py-4">
                             <div className="text-sm">
                                <p><span className="text-white/40">Année:</span> {vehicle.annee}</p>
                                <p><span className="text-white/40">Couleur:</span> {vehicle.couleur}</p>
                                <p><span className="text-white/40">Places:</span> {vehicle.places}</p>
                             </div>
                          </td>
                          <td className="px-6 py-4">
                             <span className={`px-2 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider flex items-center gap-1 w-fit ${isVerified ? 'bg-green-500/20 text-green-400 border border-green-500/20' : 'bg-yellow-500/20 text-yellow-500 border border-yellow-500/20'}`}>
                                {isVerified ? <CheckCircle className="w-3 h-3" /> : <AlertCircle className="w-3 h-3" />}
                                {isVerified ? 'Vérifié' : 'En Attente'}
                             </span>
                          </td>
                          <td className="px-6 py-4 text-right pr-6">
                             <div className="flex justify-end items-center gap-2">
                                <span className="text-xs text-white/40 mr-2">{isVerified ? "Approuvé" : "Valider"}</span>
                                <Switch 
                                    checked={isVerified}
                                    onCheckedChange={(checked) => toggleVehicleVerification(vehicle, checked)}
                                    className="data-[state=checked]:bg-green-500"
                                />
                             </div>
                          </td>
                       </tr>
                    );})}
                 </tbody>
              </table>
           </div>
        </div>
        )}

      </main>
    </div>
  );
}
