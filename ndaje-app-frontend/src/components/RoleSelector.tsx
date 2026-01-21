import { motion } from "framer-motion";
import { User, Car } from "lucide-react";
import { cn } from "@/lib/utils";

interface RoleSelectorProps {
  role: "passenger" | "driver";
  setRole: (role: "passenger" | "driver") => void;
}

export function RoleSelector({ role, setRole }: RoleSelectorProps) {
  return (
    <div className="flex gap-4 w-full mb-8">
      <button
        onClick={() => setRole("passenger")}
        className={cn(
          "flex-1 p-4 rounded-xl border transition-all duration-300 flex flex-col items-center gap-3 relative overflow-hidden group",
          role === "passenger"
            ? "border-primary bg-primary/10"
            : "border-white/10 bg-white/5 hover:bg-white/10 hover:border-white/20"
        )}
      >
        <User className={cn("w-8 h-8 transition-colors duration-300", role === "passenger" ? "text-primary" : "text-white/60 group-hover:text-white")} />
        <span className={cn("font-medium transition-colors duration-300", role === "passenger" ? "text-primary" : "text-white/60 group-hover:text-white")}>
          Passager
        </span>
        {role === "passenger" && (
          <motion.div
            layoutId="activeRole"
            className="absolute inset-0 border-2 border-primary rounded-xl"
            initial={false}
            transition={{ type: "spring", stiffness: 500, damping: 30 }}
          />
        )}
      </button>

      <button
        onClick={() => setRole("driver")}
        className={cn(
          "flex-1 p-4 rounded-xl border transition-all duration-300 flex flex-col items-center gap-3 relative overflow-hidden group",
          role === "driver"
            ? "border-primary bg-primary/10"
            : "border-white/10 bg-white/5 hover:bg-white/10 hover:border-white/20"
        )}
      >
        <Car className={cn("w-8 h-8 transition-colors duration-300", role === "driver" ? "text-primary" : "text-white/60 group-hover:text-white")} />
        <span className={cn("font-medium transition-colors duration-300", role === "driver" ? "text-primary" : "text-white/60 group-hover:text-white")}>
          Conducteur
        </span>
        {role === "driver" && (
          <motion.div
            layoutId="activeRole"
            className="absolute inset-0 border-2 border-primary rounded-xl"
            initial={false}
            transition={{ type: "spring", stiffness: 500, damping: 30 }}
          />
        )}
      </button>
    </div>
  );
}
