import { Button } from "@/components/ui/button";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { Menu, X, User, LogOut, LayoutDashboard, ChevronDown } from "lucide-react";
import * as React from "react";
import { motion, AnimatePresence } from "framer-motion";
import { useAuth } from "@/context/AuthContext";

export function Navbar() {
  const [isMenuOpen, setIsMenuOpen] = React.useState(false);
  const [isProfileOpen, setIsProfileOpen] = React.useState(false);
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const isActive = (path: string) => location.pathname === path;

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

  // Close dropdown on click outside
  React.useEffect(() => {
    const handleClickOutside = () => setIsProfileOpen(false);
    if (isProfileOpen) {
      window.addEventListener('click', handleClickOutside);
    }
    return () => window.removeEventListener('click', handleClickOutside);
  }, [isProfileOpen]);

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  return (
    <nav className="fixed top-0 left-0 right-0 z-50 bg-[#111b42]/80 backdrop-blur-md border-b border-white/10">
      <div className="container mx-auto px-4 h-16 flex items-center justify-between">
        <Link to="/" className="text-2xl font-bold text-white fontLogo z-50 relative">
          Ndaje-App.
        </Link>
        
        {/* Desktop Menu */}
        <div className="hidden md:flex items-center gap-8">
          <Link to="/about" className={`text-sm font-medium transition-colors ${isActive('/about') ? 'text-primary' : 'text-white/70 hover:text-white'}`}>
            Qui sommes-nous ?
          </Link>
          <Link to="/trips" className={`text-sm font-medium transition-colors ${isActive('/trips') ? 'text-primary' : 'text-white/70 hover:text-white'}`}>
            Rechercher
          </Link>
          <Link to="/publish" className={`text-sm font-medium transition-colors ${isActive('/publish') ? 'text-primary' : 'text-white/70 hover:text-white'}`}>
            Publier un trajet
          </Link>

          {!isAuthenticated ? (
            <Link to="/auth">
              <Button variant="default" size="sm" className="px-6">
                Connexion
              </Button>
            </Link>
          ) : (
            <div className="relative">
              <button 
                onClick={(e) => {
                  e.stopPropagation();
                  setIsProfileOpen(!isProfileOpen);
                }}
                className="flex items-center gap-3 p-1 pr-3 rounded-full hover:bg-white/5 transition-all group"
              >
                <div className="w-8 h-8 rounded-full bg-primary/20 flex items-center justify-center border border-primary/20 text-primary group-hover:bg-primary group-hover:text-white transition-all">
                  <User className="w-5 h-5" />
                </div>
                <div className="text-left hidden lg:block">
                  <p className="text-xs text-white/40 leading-none mb-1">Bonjour,</p>
                  <p className="text-sm font-bold text-white leading-none capitalize">
                    {user?.prenom} {user?.nom}
                  </p>
                </div>
                <ChevronDown className={`w-4 h-4 text-white/40 transition-transform duration-300 ${isProfileOpen ? 'rotate-180' : ''}`} />
              </button>

              <AnimatePresence>
                {isProfileOpen && (
                  <motion.div 
                    initial={{ opacity: 0, y: 10, scale: 0.95 }}
                    animate={{ opacity: 1, y: 0, scale: 1 }}
                    exit={{ opacity: 0, y: 10, scale: 0.95 }}
                    className="absolute right-0 mt-3 w-56 p-2 bg-[#0e153a] border border-white/10 backdrop-blur-xl rounded-2xl shadow-2xl z-50"
                  >
                    <div className="px-3 py-2 border-b border-white/5 mb-2">
                       <p className="text-xs text-white/40 mb-1">Connecté en tant que</p>
                       <p className="text-sm font-bold text-white truncate mb-1">{user?.email}</p>
                       <span className="inline-block px-2 py-0.5 rounded-full bg-green-500/20 text-[10px] font-bold text-green-500 uppercase border border-green-500/20">
                          {user?.role}
                       </span>
                    </div>

                    {user?.role !== 'ADMIN' ? (
                      <Link 
                        to="/profile"
                        className="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl hover:bg-white/5 text-white/70 hover:text-white transition-all text-sm"
                      >
                        <User className="w-4 h-4" />
                        Mon Profil
                      </Link>
                    ) : (
                      <Link 
                        to="/admin" 
                        className="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl hover:bg-primary/20 text-primary hover:text-primary-hover transition-all text-sm"
                      >
                        <LayoutDashboard className="w-4 h-4" />
                        Espace Admin
                      </Link>
                    )}

                    <button 
                      onClick={handleLogout}
                      className="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl hover:bg-red-500/10 text-red-400 hover:text-red-300 transition-all text-sm mt-1"
                    >
                      <LogOut className="w-4 h-4" />
                      Déconnexion
                    </button>
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
          )}
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
              <div className="flex flex-col gap-8 items-center w-full px-8 overflow-y-auto">
                {isAuthenticated && (
                  <div className="w-full flex flex-col items-center gap-4 mb-4 border-b border-white/10 pb-8">
                    <div className="w-20 h-20 rounded-full bg-primary/20 flex items-center justify-center border border-primary/20 text-primary">
                      <User className="w-10 h-10" />
                    </div>
                    <div className="text-center">
                      <p className="text-white font-bold text-2xl mb-1 capitalize">
                        {user?.prenom} {user?.nom}
                      </p>
                      <p className="text-white/40 text-sm mb-2">{user?.email}</p>
                      <span className="inline-block px-3 py-1 rounded-full bg-primary/20 text-xs font-bold text-primary uppercase border border-primary/20">
                        {user?.role}
                      </span>
                    </div>
                  </div>
                )}

                <Link 
                  to="/about" 
                  className={`text-2xl font-bold transition-colors ${isActive('/about') ? 'text-primary' : 'text-white hover:text-primary'}`}
                  onClick={() => setIsMenuOpen(false)}
                >
                  Qui sommes-nous ?
                </Link>
                <Link 
                  to="/trips" 
                  className={`text-2xl font-bold transition-colors ${isActive('/trips') ? 'text-primary' : 'text-white hover:text-primary'}`}
                  onClick={() => setIsMenuOpen(false)}
                >
                  Rechercher un trajet
                </Link>
                <Link 
                  to="/publish" 
                  className={`text-2xl font-bold transition-colors ${isActive('/publish') ? 'text-primary' : 'text-white hover:text-primary'}`}
                  onClick={() => setIsMenuOpen(false)}
                >
                  Publier un trajet
                </Link>
                
                {isAuthenticated && user?.role !== 'ADMIN' && (
                  <Link 
                    to="/profile" 
                    className={`text-2xl font-bold transition-colors ${isActive('/profile') ? 'text-primary' : 'text-white hover:text-primary'}`}
                    onClick={() => setIsMenuOpen(false)}
                  >
                    Mon Profil
                  </Link>
                )}

                {isAuthenticated && user?.role === 'ADMIN' && (
                  <Link 
                    to="/admin" 
                    className="text-2xl font-bold text-primary hover:text-primary-hover transition-colors"
                    onClick={() => setIsMenuOpen(false)}
                  >
                    Espace Admin
                  </Link>
                )}

                <div className="pt-8 w-full max-w-xs space-y-4 pb-10">
                  {!isAuthenticated ? (
                    <>
                      <Link to="/auth" onClick={() => setIsMenuOpen(false)} className="block w-full">
                        <Button variant="default" size="lg" className="w-full h-14 text-xl shadow-xl shadow-primary/20 rounded-xl">
                          Connexion
                        </Button>
                      </Link>
                      <Link to="/auth" onClick={() => setIsMenuOpen(false)} className="block">
                         <p className="text-center text-base text-white/50 pt-2">Pas encore de compte ? S'inscrire</p>
                      </Link>
                    </>
                  ) : (
                    <Button 
                      onClick={handleLogout}
                      variant="outline" 
                      size="lg" 
                      className="w-full h-14 text-xl border-red-500/20 text-red-500 hover:bg-red-500/10 rounded-xl flex items-center justify-center gap-3"
                    >
                      <LogOut className="w-6 h-6" />
                      Déconnexion
                    </Button>
                  )}
                </div>
              </div>
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </nav>
  );
}
