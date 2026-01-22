import { Navbar } from "@/components/Navbar";
import { SearchBar } from "@/components/SearchBar";
import { Button } from "@/components/ui/button";
import { useNavigate } from "react-router-dom";
import { Shield, Clock, Smile } from "lucide-react";
import HeroImage from "@/assets/svg/1.svg";
import { motion } from "framer-motion";

export function LandingPage() {
  const navigate = useNavigate();


  const fadeInLeft = {
    initial: { opacity: 0, x: -60 },
    animate: { opacity: 1, x: 0 },
    transition: { duration: 0.8, ease: "easeOut" }
  } as const;

  const fadeInRight = {
    initial: { opacity: 0, x: 60 },
    animate: { opacity: 1, x: 0 },
    transition: { duration: 0.8, ease: "easeOut" }
  } as const;

  const staggerContainer = {
    animate: {
      transition: {
        staggerChildren: 0.2
      }
    }
  };

  const scaleIn = {
    initial: { opacity: 0, scale: 0.8 },
    animate: { opacity: 1, scale: 1 },
    transition: { duration: 0.5 }
  };

  return (
    <div className="min-h-screen flex flex-col">
      <Navbar />
      
      {/* Hero Section */}
      <main className="flex-1 flex flex-col justify-center px-4 pt-20 pb-20 relative overflow-hidden">
        {/* Background Decorative Elements */}
        <div className="absolute inset-0 bg-gradient-to-r from-[#050814] to-[#1BA3EF] opacity-95 pointer-events-none" />
        <motion.div 
          initial={{ opacity: 0, scale: 0 }}
          animate={{ opacity: 0.3, scale: 1 }}
          transition={{ duration: 1.5, ease: "easeOut" }}
          className="absolute top-20 left-10 w-72 h-72 bg-primary/20 rounded-full blur-3xl pointer-events-none" 
        />
        <motion.div 
          initial={{ opacity: 0, scale: 0 }}
          animate={{ opacity: 0.2, scale: 1 }}
          transition={{ duration: 1.5, delay: 0.3, ease: "easeOut" }}
          className="absolute bottom-20 right-10 w-96 h-96 bg-purple-500/20 rounded-full blur-3xl pointer-events-none" 
        />

        <div className="container mx-auto px-4 relative z-10">
          <div className="flex flex-col lg:flex-row items-center gap-4 lg:gap-20">
            {/* Left Column: Illustration */}
            <motion.div 
              {...fadeInLeft}
              className="flex-1 flex justify-center lg:justify-start order-last lg:order-first mt-0 lg:mt-0"
            >
              <motion.img 
                src={HeroImage} 
                alt="Voyagez ensemble" 
                className="w-full max-w-lg xl:max-w-xl drop-shadow-2xl"
                whileHover={{ scale: 1.05 }}
                transition={{ duration: 0.3 }}
              />
            </motion.div>

            {/* Right Column: Content */}
            <motion.div 
              {...fadeInRight}
              className="flex-1 space-y-8"
            >
              <div className="space-y-6 text-center lg:text-left">
                <motion.h1 
                  initial={{ opacity: 0, y: 30 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.8, delay: 0.2 }}
                  className="text-4xl md:text-6xl lg:text-7xl font-bold fontLogo tracking-tight text-white drop-shadow-lg leading-tight"
                >
                  Voyagez mieux,<br />
                  <span className="bg-gradient-to-r from-primary to-blue-300 bg-clip-text text-transparent">ensemble.</span>
                </motion.h1>
                <motion.p 
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.8, delay: 0.4 }}
                  className="text-lg md:text-xl text-white/80 max-w-2xl mx-auto lg:mx-0 leading-relaxed font-light"
                >
                  La première plateforme de covoiturage premium au Sénégal. Connectez-vous, réservez, et voyagez en toute confiance.
                </motion.p>
              </div>
            </motion.div>
          </div>

          {/* Bottom Center: SearchBar */}
          <motion.div 
            initial={{ opacity: 0, y: 40 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, delay: 0.6 }}
            className="mt-0 w-full max-w-4xl mx-auto"
          >
             <SearchBar />
          </motion.div>
        </div>
      </main>

      {/* Features Section */}
      <section className="py-24 bg-white text-brand-dark relative z-10 rounded-t-[3rem] shadow-[0_-20px_40px_rgba(0,0,0,0.2)] -mt-10">
        <div className="container mx-auto px-4">
          <motion.div 
            initial={{ opacity: 0, y: 30 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true, margin: "-100px" }}
            transition={{ duration: 0.6 }}
            className="text-center mb-16 space-y-4"
          >
            <h2 className="text-3xl md:text-4xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-brand-dark to-brand-light">Pourquoi choisir Ndaje ?</h2>
            <p className="text-gray-500 max-w-2xl mx-auto">Une expérience repensée pour vous offrir confort, sécurité et simplicité à chaque kilomètre.</p>
          </motion.div>
          
          <motion.div 
            variants={staggerContainer}
            initial="initial"
            whileInView="animate"
            viewport={{ once: true, margin: "-100px" }}
            className="grid md:grid-cols-3 gap-12"
          >
            {[
              {
                icon: Clock,
                title: "Rapidité & Flexibilité",
                desc: "Trouvez un trajet en quelques secondes à l'heure qui vous convient."
              },
              {
                icon: Shield,
                title: "Sécurité Garantie",
                desc: "Profils vérifiés, avis certifiés et paiements sécurisés pour voyager sereinement."
              },
              {
                icon: Smile,
                title: "Convivialité Premium",
                desc: "Partagez plus qu'un trajet : une expérience humaine unique dans un cadre confortable."
              }
            ].map((feature, i) => (
              <motion.div 
                key={i} 
                variants={scaleIn}
                whileHover={{ y: -10, transition: { duration: 0.3 } }}
                className="flex flex-col items-center text-center space-y-6 group p-8 rounded-3xl hover:bg-gray-50 transition-colors duration-300 cursor-pointer"
              >
                <motion.div 
                  whileHover={{ rotate: 360, scale: 1.1 }}
                  transition={{ duration: 0.6 }}
                  className="p-5 rounded-2xl bg-primary/10 text-primary group-hover:bg-primary group-hover:text-white transition-all duration-300"
                >
                  <feature.icon className="w-8 h-8" />
                </motion.div>
                <div className="space-y-2">
                  <h3 className="text-xl font-bold text-gray-900">{feature.title}</h3>
                  <p className="text-gray-500 leading-relaxed">{feature.desc}</p>
                </div>
              </motion.div>
            ))}
          </motion.div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-32 relative overflow-hidden bg-brand-dark">
        <div className="absolute inset-0 bg-[#0e153a]" />
        <div className="absolute inset-0 bg-[url('https://www.transparenttextures.com/patterns/cubes.png')] opacity-5" />
        <motion.div 
          initial={{ opacity: 0, scale: 0 }}
          whileInView={{ opacity: 0.3, scale: 1 }}
          viewport={{ once: true }}
          transition={{ duration: 1 }}
          className="absolute top-0 right-0 w-96 h-96 bg-primary/20 rounded-full blur-3xl"
        />
        
        <motion.div 
          initial={{ opacity: 0, y: 40 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true, margin: "-100px" }}
          transition={{ duration: 0.8 }}
          className="container mx-auto px-4 relative z-10 text-center space-y-10"
        >
          <motion.h2 
            initial={{ opacity: 0, scale: 0.9 }}
            whileInView={{ opacity: 1, scale: 1 }}
            viewport={{ once: true }}
            transition={{ duration: 0.6 }}
            className="text-3xl md:text-5xl font-bold text-white"
          >
            Prêt à prendre la route ?
          </motion.h2>
          <motion.p 
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            viewport={{ once: true }}
            transition={{ duration: 0.6, delay: 0.2 }}
            className="text-white/60 max-w-xl mx-auto text-lg"
          >
            Rejoignez notre communauté de voyageurs dès aujourd'hui et redécouvrez le plaisir du covoiturage.
          </motion.p>
          <motion.div 
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.6, delay: 0.4 }}
            className="flex flex-col md:flex-row items-center justify-center gap-6"
          >
            <motion.div whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }}>
              <Button 
                size="lg" 
                className="px-10 text-lg h-14 rounded-full shadow-lg shadow-primary/30 w-full md:w-auto"
                onClick={() => navigate("/trips")}
              >
                Trouver un trajet
              </Button>
            </motion.div>
            <motion.div whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }}>
              <Button size="lg" variant="outline" className="px-10 text-lg h-14 rounded-full border-2 w-full md:w-auto">
                Devenir conducteur
              </Button>
            </motion.div>
          </motion.div>
        </motion.div>
      </section>
      
      {/* Footer */}
      <footer className="py-12 border-t border-white/5 bg-[#0b102b] text-center text-white/40 text-sm">
        <div className="container mx-auto px-4 flex flex-col md:flex-row justify-between items-center gap-6">
           <div className="font-bold text-2xl text-white/20">Ndaje-App.</div>
           <div className="flex gap-8">
             <a href="#" className="hover:text-white transition-colors">À propos</a>
             <a href="#" className="hover:text-white transition-colors">Sécurité</a>
             <a href="#" className="hover:text-white transition-colors">Aide</a>
           </div>
           <p>&copy; 2026 Ndaje App.</p>
        </div>
      </footer>
    </div>
  );
}
