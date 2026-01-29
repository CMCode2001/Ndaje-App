import { BASE_URL_VEHICLES } from "../api/api";

export interface Vehicle {
    id?: string;
    marque: string;
    modele: string;
    immatriculation: string;
    couleur: string;
    places: number;
    annee: number;
    driverId?: string;
    statutVerification?: 'EN_ATTENTE' | 'VERIFIE' | 'REJETE';
    user?: any; // To hold driver info if returned
}

export const VehicleService = {
    addVehicle: async (vehicleData: Vehicle, token: string) => {
        const response = await fetch(`${BASE_URL_VEHICLES}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(vehicleData),
        });
        if (!response.ok) {
             const errorData = await response.json().catch(() => ({}));
             console.error("Vehicle API Error:", errorData);
            throw new Error(errorData.message || "Erreur lors de l'enregistrement du véhicule");
        }
        return response.json();
    },

    getMyVehicles: async (token: string) => {
        const response = await fetch(`${BASE_URL_VEHICLES}`, {
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });
        if (!response.ok) {
             const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || "Erreur lors de la récupération des véhicules");
        }
        return response.json();
    },

    getAllVehicles: async (token: string) => {
        console.log("Fetching all vehicles...", { url: BASE_URL_VEHICLES, token });
        const response = await fetch(`${BASE_URL_VEHICLES}`, {
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });
        if (!response.ok) {
            console.error("Error fetching vehicles:", response.status, response.statusText);
            throw new Error("Erreur lors de la récupération des véhicules");
        }
        const data = await response.json();
        console.log("All vehicles fetched:", data);
        return data;
    },

    updateVehicle: async (id: string, vehicleData: Partial<Vehicle>, token: string) => {
        const response = await fetch(`${BASE_URL_VEHICLES}/${id}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(vehicleData),
        });
        if (!response.ok) throw new Error("Erreur lors de la mise à jour du véhicule");
        return response.json();
    }
};
