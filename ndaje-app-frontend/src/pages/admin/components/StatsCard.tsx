import { motion } from "framer-motion";
import type { LucideIcon } from "lucide-react";

interface StatsCardProps {
  label: string;
  value: string;
  trend: string;
  icon: LucideIcon;
  color: string;
}

export function StatsCard({ label, value, trend, icon: Icon, color }: StatsCardProps) {
  return (
    <motion.div
      whileHover={{ y: -5 }}
      className="p-6 rounded-3xl bg-white/5 backdrop-blur-md border border-white/10 relative overflow-hidden group"
    >
      <div className={`absolute top-0 right-0 w-24 h-24 bg-${color}/10 rounded-full blur-3xl group-hover:bg-${color}/20 transition-all`} />
      
      <div className="flex justify-between items-start relative z-10">
        <div className="space-y-1">
          <p className="text-white/40 text-sm font-medium">{label}</p>
          <h3 className="text-3xl font-bold text-white">{value}</h3>
        </div>
        <div className={`p-3 rounded-2xl bg-${color}/20 text-${color}`}>
          <Icon className="w-6 h-6" />
        </div>
      </div>
      
      <div className="mt-4 flex items-center gap-2 relative z-10">
        <span className="text-green-400 text-sm font-bold">{trend}</span>
        <span className="text-white/20 text-xs">depuis le mois dernier</span>
      </div>
    </motion.div>
  );
}
