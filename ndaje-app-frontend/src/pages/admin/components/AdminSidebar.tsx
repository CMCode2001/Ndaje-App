import type { LucideIcon } from "lucide-react";
import { motion } from "framer-motion";

interface SidebarItemProps {
  icon: LucideIcon;
  label: string;
  active?: boolean;
  variant?: 'default' | 'danger';
  onClick?: () => void;
}

export function SidebarItem({ icon: Icon, label, active, variant, onClick }: SidebarItemProps) {
  return (
    <button
      onClick={onClick}
      className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-300 ${
        active 
          ? "bg-primary text-white shadow-lg shadow-primary/20" 
          : variant === 'danger'
            ? "text-red-400 hover:text-red-500 hover:bg-red-500/10"
            : "text-white/60 hover:text-white hover:bg-white/5"
      }`}
    >
      <Icon className={`w-5 h-5 ${active ? "text-white" : variant === 'danger' ? "text-red-400" : "text-white/40"}`} />
      <span className="font-medium">{label}</span>
      {active && (
        <motion.div
          layoutId="active-pill"
          className="ml-auto w-1.5 h-1.5 rounded-full bg-white"
        />
      )}
    </button>
  );
}

import { Users, Car, Shield, Settings, LogOut, Home } from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { Link, useNavigate } from "react-router-dom";

export function AdminSidebar() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  return (
    <div className="w-64 h-full bg-white/5 backdrop-blur-xl border-r border-white/10 p-6 flex flex-col">
      <div className="flex items-center gap-3 px-2 mb-10">
        <div className="w-8 h-8 rounded-lg bg-primary flex items-center justify-center font-bold text-white italic">N</div>
        <span className="text-xl font-bold fontLogo text-white">Ndaje <span className="text-primary italic">Admin</span></span>
      </div>
      
      <div className="space-y-2 flex-1">
        <SidebarItem icon={Users} label="Utilisateurs" active />
        <SidebarItem icon={Car} label="Trajets" />
        <SidebarItem icon={Shield} label="Vérifications" />
        <SidebarItem icon={Settings} label="Paramètres" />
      </div>

      <div className="space-y-2 pt-6 border-t border-white/5">
        <Link to="/" className="w-full">
          <SidebarItem icon={Home} label="Retour au site" />
        </Link>
        <SidebarItem 
          icon={LogOut} 
          label="Déconnexion" 
          variant="danger"
          onClick={handleLogout}
        />
      </div>
    </div>
  );
}
