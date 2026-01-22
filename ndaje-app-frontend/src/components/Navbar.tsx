import { Button } from "@/components/ui/button";
import { Link } from "react-router-dom";
import { Menu, X } from "lucide-react";
import * as React from "react";
import { motion, AnimatePresence } from "framer-motion";

export function Navbar() {
  const [isMenuOpen, setIsMenuOpen] = React.useState(false);

  // Lock body scroll when menu is open
  React.useEffect(() => {
    if (isMenuOpen) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "unset";
    }
    return () => {
      document.body.style.overflow = "unset";
    };
  }, [isMenuOpen]);

  return (
    <nav className="fixed top-0 left-0 right-0 z-50 bg-[#111b42]/80 backdrop-blur-md border-b border-white/10">
      <div className="container mx-auto px-4 h-16 flex items-center justify-between">
        <Link to="/" className="text-2xl font-bold text-white fontLogo z-50 relative">
          Ndaje-App.
        </Link>
        
        {/* Desktop Menu */}
        <div className="hidden md:flex items-center gap-8">
          <Link to="/about" className="text-sm font-medium text-white/70 hover:text-white transition-colors">
            Qui sommes-nous ?
          </Link>
          <Link to="/trips" className="text-sm font-medium text-white/70 hover:text-white transition-colors">
            Rechercher
          </Link>
          <Link to="/publish" className="text-sm font-medium text-white/70 hover:text-white transition-colors">
            Publier un trajet
          </Link>
          <Link to="/auth">
            <Button variant="default" size="sm" className="px-6">
              Connexion
            </Button>
          </Link>
        </div>

        {/* Mobile Toggle */}
        <button 
          onClick={() => setIsMenuOpen(!isMenuOpen)}
          className="md:hidden text-white hover:text-primary transition-colors z-50 relative p-2"
          aria-label="Toggle menu"
        >
          {isMenuOpen ? <X className="w-8 h-8" /> : <Menu className="w-8 h-8" />}
        </button>
      </div>

      {/* Mobile Menu Overlay */}
      <AnimatePresence>
        {isMenuOpen && (
          <>
            {/* Backdrop */}
            <motion.div 
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setIsMenuOpen(false)}
              className="fixed inset-0 bg-black/60 backdrop-blur-sm z-40 md:hidden"
            />
            
            {/* Slide-over Menu */}
            <motion.div 
              initial={{ x: "100%" }}
              animate={{ x: 0 }}
              exit={{ x: "100%" }}
              transition={{ type: "spring", damping: 30, stiffness: 300 }}
              className="fixed inset-0 h-screen w-screen bg-[#111A41] z-40 md:hidden flex flex-col justify-start items-center pt-28"
            >
              <div className="flex flex-col gap-8 items-center w-full px-8">
                <Link 
                  to="/about" 
                  className="text-2xl font-bold text-white hover:text-primary transition-colors"
                  onClick={() => setIsMenuOpen(false)}
                >
                  Qui sommes-nous ?
                </Link>
                <Link 
                  to="/trips" 
                  className="text-2xl font-bold text-white hover:text-primary transition-colors"
                  onClick={() => setIsMenuOpen(false)}
                >
                  Rechercher un trajet
                </Link>
                <Link 
                  to="/publish" 
                  className="text-2xl font-bold text-white hover:text-primary transition-colors"
                  onClick={() => setIsMenuOpen(false)}
                >
                  Publier un trajet
                </Link>
                <div className="pt-8 w-full max-w-xs space-y-4">
                  <Link to="/auth" onClick={() => setIsMenuOpen(false)} className="block w-full">
                    <Button variant="default" size="lg" className="w-full h-14 text-xl shadow-xl shadow-primary/20 rounded-xl">
                      Connexion
                    </Button>
                  </Link>
                  <Link to="/auth" onClick={() => setIsMenuOpen(false)} className="block">
                     <p className="text-center text-base text-white/50 pt-2">Pas encore de compte ? S'inscrire</p>
                  </Link>
                </div>
              </div>
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </nav>
  );
}
