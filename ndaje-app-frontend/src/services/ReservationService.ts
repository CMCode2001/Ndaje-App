import { BASE_URL_RESERVATION } from "../api/api";

export interface ReservationRequest {
    tripId: number;
    passengerId: string;
    places: number;
}

export const ReservationService = {
    createReservation: async (reservationData: ReservationRequest, token: string) => {
        const response = await fetch(`${BASE_URL_RESERVATION}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(reservationData),
        });
        
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || "Erreur lors de la réservation");
        }
        
        return response.json();
    },

    getPassengerReservations: async (passengerId: string, token: string) => {
        const response = await fetch(`${BASE_URL_RESERVATION}/passenger/${passengerId}`, {
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || "Erreur lors de la récupération des réservations");
        }
        return response.json();
    },

    updateReservation: async (id: any, places: number, token: string) => {
        const response = await fetch(`${BASE_URL_RESERVATION}/${id}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({ places }),
        });
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || "Erreur lors de la modification de la réservation");
        }
        return response.json();
    },

    cancelReservation: async (id: any, token: string) => {
        const response = await fetch(`${BASE_URL_RESERVATION}/${id}/cancel`, {
            method: "PATCH",
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || "Erreur lors de l'annulation de la réservation");
        }
        return response.json();
    },

    getDriverReservations: async (driverId: string, token: string) => {
        const response = await fetch(`${BASE_URL_RESERVATION}/driver/${driverId}`, {
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || "Erreur lors de la récupération des passagers");
        }
        return response.json();
    }
};
