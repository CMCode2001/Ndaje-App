// export const BASE_URL_RESERVATION = "/api/reservations";
// export const BASE_URL_USERS = "/api/users";
// export const BASE_URL_TRIPS = "/api/trips";
// export const BASE_URL_VEHICLES = "/api/vehicules";
// export const BASE_URL_ADMIN = "/api/admin/users";
//========= API_URL POUR DOCKER ========
const API_URL = import.meta.env.VITE_API_URL;

export const BASE_URL_USERS = `${API_URL}/api/users`;
export const BASE_URL_TRIPS = `${API_URL}/api/trips`;
export const BASE_URL_RESERVATION = `${API_URL}/api/reservations`;
export const BASE_URL_VEHICLES = `${API_URL}/api/vehicules`;
export const BASE_URL_ADMIN = `${API_URL}/api/admin/users`;
//===================================

export interface Trip {
  id: any;
  driverId: any;
  depart: string;
  arrivee: string;
  dateDepart: string;
  placesDisponibles: number;
  prix: number;
  driverFirstName?: string;
  driverLastName?: string;
  driverPhone?: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export const fetchAvailableTrips = async (): Promise<ApiResponse<Trip[]>> => {
  const response = await fetch(`${BASE_URL_TRIPS}`);
  if (!response.ok) {
    throw new Error("Impossible de récupérer les trajets");
  }
  return response.json();
};

export const createReservation = async (reservationData: { tripId: any; nbPlaces: number }): Promise<ApiResponse<any>> => {
  const response = await fetch(`${BASE_URL_RESERVATION}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      // 'Authorization': `Bearer ${localStorage.getItem('token')}` // Placeholder for auth
    },
    body: JSON.stringify(reservationData),
  });
  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}));
    throw new Error(errorBody.message || "Erreur lors de la réservation");
  }
  return response.json();
};

export interface User {
  id: any;
  nom: string;
  prenom: string;
  email: string;
  telephone?: string;
}

export const fetchUserById = async (userId: any, token?: string): Promise<ApiResponse<User>> => {
  const headers: HeadersInit = {};
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  
  const response = await fetch(`${BASE_URL_USERS}/${userId}`, { headers });
  if (!response.ok) {
    throw new Error("Impossible de récupérer les informations de l'utilisateur");
  }
  return response.json();
};
