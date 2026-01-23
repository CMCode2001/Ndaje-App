import { BrowserRouter, Routes, Route, useLocation } from "react-router-dom";
import { LandingPage } from "@/pages/LandingPage";
import { AuthPage } from "@/pages/AuthPage";
import { useEffect } from "react";
import { AboutPage } from "./pages/AboutPage";
import { TripsPages } from "./pages/TripsPages";
import AdminPage from "./pages/admin/Admin";
import { AuthProvider } from "./context/AuthContext";
import { Toaster } from "sonner";
import { ProfilePage } from "./pages/ProfilePage";

function ScrollToTop() {
  const { pathname } = useLocation();
  useEffect(() => {
    window.scrollTo(0, 0);
  }, [pathname]);
  return null;
}


export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <ScrollToTop />
        <Toaster position="top-right" richColors expand={true} />
        <Routes>
          <Route path="/" element={<LandingPage />} /> 
          <Route path="/auth" element={<AuthPage />} />
          <Route path="/about" element={<AboutPage />} />
          <Route path="/trips" element={<TripsPages />} />
          <Route path="/admin" element={<AdminPage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="*" element={<LandingPage />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
