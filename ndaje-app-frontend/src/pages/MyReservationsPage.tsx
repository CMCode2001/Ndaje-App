import { useEffect, useMemo, useState } from "react";
import { Navbar } from "@/components/Navbar";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/context/AuthContext";
import { useNavigate } from "react-router-dom";
import { ReservationService } from "@/services/ReservationService";
import { motion, AnimatePresence } from "framer-motion";
import { toast } from "sonner";
import {
  MapPin,
  Ticket,
  Trash2,
  Edit2,
  Loader2,
  Users,
  X
} from "lucide-react";

/* ===================== TYPES ===================== */

type ReservationStatus = "CONFIRMED" | "PENDING" | "CANCELLED";

interface Reservation {
  id: string;
  passengerId: string;
  tripId: string;
  depart: string;
  arrivee: string;
  dateDepart: string;
  reservationDate: string;
  places: number;
  status: ReservationStatus;
}

/* ===================== COMPONENT ===================== */

export function MyReservationsPage() {
  const { user, token, logout } = useAuth();
  const navigate = useNavigate();

  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState<string | null>(null);

  const [editReservation, setEditReservation] = useState<Reservation | null>(null);
  const [cancelReservation, setCancelReservation] = useState<Reservation | null>(null);
  const [places, setPlaces] = useState<number | string>(1);

  /* ===================== FETCH ===================== */

  const fetchReservations = async () => {
    if (!user?.id || !token) return;
    setLoading(true);

    try {
      const response = await ReservationService.getPassengerReservations(
        user.id,
        token
      );
      setReservations(response.data || response);
    } catch (error: any) {
      if (error.status === 401) {
        toast.error("Session expirée");
        logout();
        navigate("/auth");
      } else {
        toast.error("Impossible de récupérer vos réservations");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReservations();
  }, [user, token]);

  /* ===================== ACTIONS ===================== */

  const confirmCancel = async () => {
    if (!cancelReservation) return;

    setActionLoading(cancelReservation.id);
    try {
      await ReservationService.cancelReservation(
        cancelReservation.id,
        token!
      );
      toast.success("Réservation annulée");
      fetchReservations();
    } catch {
      toast.error("Erreur lors de l'annulation");
    } finally {
      setActionLoading(null);
      setCancelReservation(null);
    }
  };

  const confirmUpdatePlaces = async () => {
    const finalPlaces = Number(places);
    if (!editReservation || isNaN(finalPlaces) || finalPlaces < 1) {
      toast.error("Nombre de places invalide");
      return;
    }

    setActionLoading(editReservation.id);
    try {
      await ReservationService.updateReservation(
        editReservation.id,
        finalPlaces,
        token!
      );
      toast.success("Réservation mise à jour");
      fetchReservations();
    } catch {
      toast.error("Erreur lors de la modification");
    } finally {
      setActionLoading(null);
      setEditReservation(null);
    }
  };

  const sortedReservations = useMemo(() => {
    return [...reservations].sort((a, b) => {
      // Priority: CONFIRMED > PENDING > CANCELLED
      const order: Record<string, number> = {
        CONFIRMED: 1,
        PENDING: 2,
        CANCELLED: 3,
      };
      return (order[a.status] || 99) - (order[b.status] || 99);
    });
  }, [reservations]);

  /* ===================== UI ===================== */

  return (
    <div className="min-h-screen bg-[#020817] text-white">
      <Navbar />

      <main className="container mx-auto pt-32 pb-20 px-4 max-w-5xl">
        <header className="mb-10">
          <h1 className="text-4xl font-bold mb-2">Mes Réservations</h1>
          <p className="text-white/60">
            Gérez vos trajets et consultez votre historique
          </p>
        </header>

        {loading ? (
          <div className="flex justify-center py-20">
            <Loader2 className="w-10 h-10 animate-spin text-primary" />
          </div>
        ) : reservations.length === 0 ? (
          <div className="rounded-2xl border border-white/10 bg-white/5 p-12 text-center">
            <Ticket className="mx-auto mb-4 h-14 w-14 text-white/20" />
            <h3 className="text-xl font-semibold mb-2">
              Aucune réservation
            </h3>
            <p className="text-white/60 mb-6">
              Vous n'avez pas encore réservé de trajet
            </p>
            <Button onClick={() => navigate("/trips")}>
              Rechercher un trajet
            </Button>
          </div>
        ) : (
          <div className="space-y-6">
            {sortedReservations.map((res) => (
              <motion.div
                key={res.id}
                initial={{ opacity: 0, y: 15 }}
                animate={{ opacity: 1, y: 0 }}
                className="rounded-2xl border border-white/10 bg-white/5 p-6"
              >
                {/* HEADER: STATUS + PLACES */}
                <div className="flex justify-between items-center mb-6">
                  <span
                    className={`px-3 py-1 rounded-full text-[10px] font-bold tracking-wider ${
                      res.status === "CONFIRMED"
                        ? "bg-green-500/10 text-green-400 border border-green-500/20"
                        : res.status === "CANCELLED"
                        ? "bg-red-500/10 text-red-400 border border-red-500/20"
                        : "bg-blue-500/10 text-blue-400 border border-blue-500/20"
                    }`}
                  >
                    {res.status}
                  </span>

                  <div className="flex items-center gap-2 px-3 py-1 rounded-xl bg-white/5 border border-white/10 shadow-lg">
                    <Users className="w-3.5 h-3.5 text-primary" />
                    <span className="text-xs font-bold text-white">
                      {res.places} Place{res.places > 1 ? "s" : ""}
                    </span>
                  </div>
                </div>

                {/* CONTENT */}
                <div className="space-y-5">
                  <div className="flex items-center gap-4">
                    <div className="w-10 h-10 rounded-2xl bg-primary/10 flex items-center justify-center shrink-0">
                      <MapPin className="w-5 h-5 text-primary" />
                    </div>
                    <div>
                      <p className="text-xl font-bold tracking-tight text-white leading-none">
                        {res.depart} → {res.arrivee}
                      </p>
                      <p className="text-[10px] text-white/30 uppercase font-bold tracking-widest mt-1.5">Itinéraire</p>
                    </div>
                  </div>

                  <div className="flex items-start gap-4">
                    <div className="w-10 h-10 rounded-2xl bg-white/5 flex items-center justify-center shrink-0 text-white/40">
                      <Ticket className="w-5 h-5" />
                    </div>
                    <div>
                      <p className="font-bold text-white capitalize leading-none">
                        {new Date(res.dateDepart).toLocaleDateString("fr-FR", {
                          weekday: "long",
                          day: "numeric",
                          month: "long",
                          year: "numeric",
                        })}
                      </p>
                      <p className="text-sm text-white/50 mt-1 font-medium">
                        À{" "}
                        {new Date(res.dateDepart).toLocaleTimeString("fr-FR", {
                          hour: "2-digit",
                          minute: "2-digit",
                        })}
                      </p>
                    </div>
                  </div>
                </div>

                {/* ACTIONS */}
                {res.status !== "CANCELLED" && (
                  <div className="mt-6 flex gap-3">
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => {
                        setEditReservation(res);
                        setPlaces(res.places);
                      }}
                    >
                      <Edit2 className="w-4 h-4 mr-2" />
                      Modifier
                    </Button>

                    <Button
                      size="sm"
                      variant="outline"
                      className="border-red-500/30 text-red-500 hover:bg-red-500/10"
                      onClick={() => setCancelReservation(res)}
                    >
                      <Trash2 className="w-4 h-4 mr-2" />
                      Annuler
                    </Button>
                  </div>
                )}
              </motion.div>
            ))}
          </div>
        )}
      </main>

      {/* ===================== MODALS ===================== */}

      <AnimatePresence>
        {(editReservation || cancelReservation) && (
          <div className="fixed inset-0 bg-black/70 z-50 flex items-center justify-center">
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              className="bg-[#020817] border border-white/10 rounded-2xl p-6 w-full max-w-md"
            >
              <div className="flex justify-between items-center mb-4">
                <h3 className="text-lg font-semibold">
                  {editReservation
                    ? "Modifier les places"
                    : "Annuler la réservation"}
                </h3>
                <button
                  onClick={() => {
                    setEditReservation(null);
                    setCancelReservation(null);
                  }}
                >
                  <X />
                </button>
              </div>

              {editReservation ? (
                <>
                  <input
                    type="number"
                    min={1}
                    value={places}
                    onChange={(e) => setPlaces(e.target.value === "" ? "" : Number(e.target.value))}
                    className="w-full mb-6 rounded-lg bg-white/5 border border-white/10 px-4 py-2"
                  />
                  <Button
                    className="w-full"
                    onClick={confirmUpdatePlaces}
                    disabled={actionLoading === editReservation.id}
                  >
                    Confirmer
                  </Button>
                </>
              ) : (
                <>
                  <p className="text-white/60 mb-6">
                    Êtes-vous sûr de vouloir annuler cette réservation ?
                  </p>
                  <Button
                    className="w-full bg-red-500 hover:bg-red-600"
                    onClick={confirmCancel}
                    disabled={actionLoading === cancelReservation?.id}
                  >
                    Confirmer l’annulation
                  </Button>
                </>
              )}
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
}
