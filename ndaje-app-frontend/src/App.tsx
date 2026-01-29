import { BrowserRouter, Routes, Route, useLocation, useNavigate } from "react-router-dom";
import { LandingPage } from "@/pages/LandingPage";
import { AuthPage } from "@/pages/AuthPage";
import { useEffect } from "react";
import { AboutPage } from "./pages/AboutPage";
import { TripsPages } from "./pages/TripsPages";
import AdminPage from "./pages/admin/Admin";
import { AuthProvider, useAuth } from "./context/AuthContext";
import { Toaster } from "sonner";
import { ProfilePage } from "./pages/ProfilePage";
import { RegisterVehiclePage } from "./pages/RegisterVehiclePage";
import { PublishTripPage } from "./pages/PublishTripPage";
import { MyReservationsPage } from "./pages/MyReservationsPage";
// ... imports

function VehicleGuard({ children }: { children: React.ReactNode }) {
    const { user, isAuthenticated } = useAuth();
    const location = useLocation();
    const navigate = useNavigate();

    useEffect(() => {
        if (isAuthenticated && user?.role === 'DRIVER' && (!user.vehicles || user.vehicles.length === 0)) {
            if (location.pathname !== '/register-vehicle') {
                navigate('/register-vehicle');
            }
        }
    }, [isAuthenticated, user, location, navigate]);

    return <>{children}</>;
}

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
        <Toaster position="bottom-right" richColors expand={true} />
        <VehicleGuard>
            <Routes>
            <Route path="/" element={<LandingPage />} /> 
            <Route path="/auth" element={<AuthPage />} />
            <Route path="/about" element={<AboutPage />} />
            <Route path="/trips" element={<TripsPages />} />
            <Route path="/publish" element={<PublishTripPage />} />
            <Route path="/admin" element={<AdminPage />} />
            <Route path="/profile" element={<ProfilePage />} />
            <Route path="/register-vehicle" element={<RegisterVehiclePage />} />
            <Route path="/my-reservations" element={<MyReservationsPage />} />
            <Route path="*" element={<LandingPage />} />
            </Routes>
        </VehicleGuard>
      </BrowserRouter>
    </AuthProvider>
  );
}
