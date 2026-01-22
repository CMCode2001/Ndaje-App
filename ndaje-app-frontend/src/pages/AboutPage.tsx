import { Navbar } from "@/components/Navbar";
import { Button } from "@/components/ui/button";
import { Mail, Phone, MapPin, ArrowRight, Shield, Globe } from "lucide-react";
import { motion } from "framer-motion";
import HeroImage from "@/assets/img/covoiturage1.png";

export function AboutPage() {
  const fadeIn = {
    initial: { opacity: 0, y: 30 },
    animate: { opacity: 1, y: 0 },
    transition: { duration: 0.8 }
  };

  return (
    <div className="min-h-screen flex flex-col bg-white overflow-hidden">
      <Navbar />
      
      {/* 1. SECTON A PROPOS (Hero) */}
      <section className="relative pt-32 pb-24 md:pt-48 md:pb-32 bg-brand-dark text-white overflow-hidden">
        {/* Abstract Background */}
        <div className="absolute inset-0 bg-gradient-to-b from-[#111A41] to-[#0d1229] z-0" />
        <div className="absolute top-0 right-0 w-[600px] h-[600px] bg-primary/10 rounded-full blur-[120px] pointer-events-none" />
        <div className="absolute bottom-0 left-0 w-[400px] h-[400px] bg-purple-500/10 rounded-full blur-[100px] pointer-events-none" />

        <div className="container mx-auto px-4 relative z-10">
          <motion.div 
            initial="initial"
            animate="animate"
            variants={fadeIn}
            className="max-w-4xl mx-auto text-center space-y-8"
          >
            <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-white/5 border border-white/10 text-primary text-sm font-medium backdrop-blur-sm">
              <Globe className="w-4 h-4" />
              <span>À propos de Ndaje</span>
            </div>
            
            <h1 className="text-4xl md:text-6xl lg:text-7xl font-bold fontLogo leading-tight tracking-tight">
              Réinventer le covoiturage<br />
              <span className="text-white/40">au Sénégal.</span>
            </h1>
            
            <p className="text-lg md:text-xl text-white/70 max-w-2xl mx-auto leading-relaxed font-light">
              Nous sommes bien plus qu'une plateforme de transport. Nous sommes une communauté bâtie sur la confiance, le partage et l'innovation pour transformer chaque trajet en une expérience unique.
            </p>
          </motion.div>
        </div>
      </section>

      {/* 2. SECTION NOTRE HISTOIRE */}
      <section className="py-24 relative bg-gray-50/50">
        <div className="container mx-auto px-4">
          <div className="flex flex-col lg:flex-row items-center gap-16 lg:gap-24">
            {/* Image/Illustration */}
            <motion.div 
              initial={{ opacity: 0, x: -50 }}
              whileInView={{ opacity: 1, x: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.8 }}
              className="flex-1 relative flex justify-center"
            >
              <div className="absolute inset-0 bg-gradient-to-tr from-primary/20 to-transparent rounded-full blur-3xl scale-90" />
              <img 
                src={HeroImage} 
                alt="Notre histoire - Ndaje App" 
                className="w-full max-w-lg drop-shadow-2xl relative z-10 hover:scale-[1.02] transition-transform duration-500"
              />
            </motion.div>

            {/* Content */}
            <div className="flex-1 space-y-8">
              <motion.div 
                initial={{ opacity: 0, x: 50 }}
                whileInView={{ opacity: 1, x: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.8, delay: 0.2 }}
                className="space-y-6"
              >
                <h2 className="text-sm font-bold text-primary uppercase tracking-widest">Notre Histoire</h2>
                <h3 className="text-3xl md:text-5xl font-bold text-brand-dark leading-tight">
                  Une vision née sur<br /> la route.
                </h3>
                <p className="text-gray-600 text-lg leading-relaxed">
                  L'aventure Ndaje a commencé par une simple observation : voyager entre les régions du Sénégal était souvent synonyme de chaos et d'insécurité.
                </p>
                <p className="text-gray-600 text-lg leading-relaxed">
                  Nous avons voulu créer une solution qui allie la technologie moderne à la "Teranga" sénégalaise. Aujourd'hui, Ndaje-App. permet à des milliers de personnes de se connecter, de partager leurs trajets et de réduire leur empreinte carbone, tout en voyageant confortablement.
                </p>
                <div className="pt-4 flex items-center gap-2 text-brand-dark font-medium">
                  <Shield className="w-5 h-5 text-primary" />
                  <span>Fière d'être 100% Sénégalaise</span>
                </div>
              </motion.div>
            </div>
          </div>
        </div>
      </section>

      {/* 3. SECTION NOUS CONTACTER */}
      <section className="py-24 bg-white relative">
        <div className="container mx-auto px-4 max-w-6xl">
          <motion.div 
             initial={{ opacity: 0, y: 30 }}
             whileInView={{ opacity: 1, y: 0 }}
             viewport={{ once: true }}
             transition={{ duration: 0.8 }}
             className="bg-brand-dark rounded-[3rem] p-8 md:p-16 relative overflow-hidden text-white shadow-2xl shadow-brand-dark/30"
          >
            {/* Decorative Overlay */}
            <div className="absolute top-0 right-0 w-full h-full bg-[url('https://www.transparenttextures.com/patterns/cubes.png')] opacity-5 pointer-events-none" />
            <div className="absolute -bottom-24 -left-24 w-64 h-64 bg-primary rounded-full blur-[100px] opacity-40" />
            
            <div className="grid md:grid-cols-2 gap-12 items-center relative z-10">
              <div className="space-y-8">
                 <h2 className="text-3xl md:text-5xl font-bold">Nous contacter</h2>
                 <p className="text-white/70 text-lg">
                   Une question ? Une suggestion ? Ou simplement envie de dire bonjour ? Notre équipe est là pour vous écouter.
                 </p>
                 
                 <div className="space-y-6 pt-4">
                   <div className="flex items-center gap-4 group cursor-pointer">
                     <div className="w-12 h-12 bg-white/10 rounded-full flex items-center justify-center group-hover:bg-primary transition-colors">
                       <Mail className="w-6 h-6" />
                     </div>
                     <div>
                       <p className="text-sm text-white/50">Email</p>
                       <p className="font-medium text-lg">contact@ndaje.sn</p>
                     </div>
                   </div>
                   
                   <div className="flex items-center gap-4 group cursor-pointer">
                     <div className="w-12 h-12 bg-white/10 rounded-full flex items-center justify-center group-hover:bg-primary transition-colors">
                       <Phone className="w-6 h-6" />
                     </div>
                     <div>
                       <p className="text-sm text-white/50">Téléphone</p>
                       <p className="font-medium text-lg">+221 77 000 00 00</p>
                     </div>
                   </div>

                   <div className="flex items-center gap-4 group cursor-pointer">
                     <div className="w-12 h-12 bg-white/10 rounded-full flex items-center justify-center group-hover:bg-primary transition-colors">
                       <MapPin className="w-6 h-6" />
                     </div>
                     <div>
                       <p className="text-sm text-white/50">Bureau</p>
                       <p className="font-medium text-lg">Dakar, Sénégal</p>
                     </div>
                   </div>
                 </div>
              </div>

              {/* Simple Form CTA */}
              <div className="bg-white/5 backdrop-blur-md rounded-3xl p-8 border border-white/10 shadow-xl">
                 <h3 className="text-xl font-bold mb-6">Envoyez-nous un message</h3>
                 <div className="space-y-4">
                   <input type="text" placeholder="Votre Nom" className="w-full bg-white/10 border-none rounded-xl p-4 text-white placeholder:text-white/40 focus:ring-2 focus:ring-primary outline-none transition-all" />
                   <input type="email" placeholder="Votre Email" className="w-full bg-white/10 border-none rounded-xl p-4 text-white placeholder:text-white/40 focus:ring-2 focus:ring-primary outline-none transition-all" />
                   <textarea rows={4} placeholder="Votre Message" className="w-full bg-white/10 border-none rounded-xl p-4 text-white placeholder:text-white/40 focus:ring-2 focus:ring-primary outline-none transition-all resize-none"></textarea>
                   <Button className="w-full h-14 text-lg rounded-xl shadow-lg shadow-primary/20 hover:scale-[1.02] transition-transform">
                     Envoyer le message <ArrowRight className="ml-2 w-5 h-5" />
                   </Button>
                 </div>
              </div>
            </div>
          </motion.div>
        </div>
      </section>

      {/* Footer minimaliste pour fermer la page */}
      <footer className="py-8 bg-white border-t border-gray-100 text-center text-gray-400 text-sm">
        <p>&copy; 2026 Ndaje App. Tous droits réservés.</p>
      </footer>
    </div>
  );
}
