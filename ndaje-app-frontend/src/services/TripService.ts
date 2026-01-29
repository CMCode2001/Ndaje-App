import { BASE_URL_TRIPS } from "../api/api";

export interface Trip {
    id?: string;
    driverId: string;
    driverFirstName?: string;
    driverLastName?: string;
    driverPhone?: string;
    vehicleId?: string;
    vehicleMarque?: string;
    vehicleModele?: string;
    vehicleImmatriculation?: string;
    depart: string;
    arrivee: string;
    dateDepart: string;
    placesDisponibles: number;
    prix: number;
    statutTrajet?: string;
}

export const TripService = {
    createTrip: async (tripData: Trip, token: string) => {
        const response = await fetch(`${BASE_URL_TRIPS}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(tripData),
        });
        if (!response.ok) {
             const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || "Erreur lors de la création du trajet");
        }
        return response.json();
    },

    updateTrip: async (id: string, tripData: Trip, token: string) => {
        const response = await fetch(`${BASE_URL_TRIPS}/${id}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(tripData),
        });
        if (!response.ok) {
             const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || "Erreur lors de la modification du trajet");
        }
        return response.json();
    },

    getDriverTrips: async (driverId: string, token: string) => {
        console.log(`Fetching trips for driver ${driverId}...`);
        const response = await fetch(`${BASE_URL_TRIPS}/driver/${driverId}`, {
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });
        if (!response.ok) {
             const errorData = await response.json().catch(() => ({}));
             console.error("Error fetching trips:", errorData);
            throw new Error(errorData.message || "Erreur lors de la récupération des trajets");
        }
        const data = await response.json();
        console.log("Driver trips response:", data);
        return data;
    }
};
