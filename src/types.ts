export type UserRole = 'citizen' | 'driver' | 'admin';

export interface UserProfile {
  uid: string;
  displayName: string;
  email: string;
  phoneNumber?: string;
  address?: string;
  role: UserRole;
  createdAt: string;
}

export interface LatLng {
  lat: number;
  lng: number;
}

export interface Vehicle {
  id: string;
  driverId: string;
  location: LatLng;
  status: 'idle' | 'active' | 'completed';
  lastUpdate: string;
  vehicleNumber: string;
  etaMinutes?: number;
}

export interface BlackspotReport {
  id: string;
  reporterId: string;
  photoUrl: string;
  description: string;
  location: LatLng;
  status: 'pending' | 'investigating' | 'resolved';
  severity: 'low' | 'medium' | 'high';
  createdAt: string;
  resolvedAt?: string;
}

export interface Schedule {
  id: string;
  dayOfWeek: string;
  route: string;
  expectedTime: string;
}

export interface WasteGuideline {
  id: string;
  title: string;
  content: string;
  category: 'dry' | 'wet' | 'hazardous' | 'e-waste';
  icon: string;
}
