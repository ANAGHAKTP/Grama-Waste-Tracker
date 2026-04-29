import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import { Navigation, Info, Clock, Route } from 'lucide-react';
import { motion } from 'motion/react';

// Fix leaflet default icon issue
const DefaultIcon = L.icon({
    iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
    shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41]
});
L.Marker.prototype.options.icon = DefaultIcon;

// Custom garbage truck icon would go here
const vehicleIcon = new L.Icon({
  iconUrl: 'https://cdn-icons-png.flaticon.com/512/2830/2830305.png',
  iconSize: [40, 40],
  iconAnchor: [20, 20]
});

const LiveMap: React.FC = () => {
  const [vehiclePos, setVehiclePos] = useState<[number, number]>([12.9716, 77.5946]); // Bangalore coord as default
  const [eta, setEta] = useState(12);

  useEffect(() => {
    // Simulate movement
    const interval = setInterval(() => {
      setVehiclePos(prev => [prev[0] + 0.0001, prev[1] + 0.0001]);
      setEta(prev => Math.max(1, prev - 1));
    }, 30000);

    return () => clearInterval(interval);
  }, []);

  return (
    <div className="h-[calc(100vh-140px)] flex flex-col -mx-4 -mt-8 relative">
      <MapContainer 
        center={vehiclePos} 
        zoom={15} 
        className="flex-1 w-full z-0"
        zoomControl={false}
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; OpenStreetMap contributors'
        />
        <Marker position={vehiclePos} icon={vehicleIcon}>
          <Popup>
            <div className="p-2">
              <h3 className="font-bold">Truck KA-01-1234</h3>
              <p className="text-xs text-gray-500">Speed: 15 km/h</p>
            </div>
          </Popup>
        </Marker>
      </MapContainer>

      {/* Floating UI Over Map */}
      <div className="absolute top-4 left-4 right-4 z-10 flex flex-col gap-4 pointer-events-none">
        <motion.div 
          initial={{ y: -20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          className="geometric-card bg-bg-secondary shadow-2xl border-border-dim flex items-center gap-6 pointer-events-auto"
        >
          <div className="w-12 h-12 border border-border-dim flex items-center justify-center text-accent-primary shrink-0">
            <Navigation className="w-6 h-6 animate-pulse" />
          </div>
          <div className="flex-1">
            <h3 className="font-bold text-text-primary leading-none mb-2 text-[10px] tracking-[0.2em] uppercase">Logistics Status</h3>
            <div className="flex items-center gap-4">
              <div className="flex items-center gap-2 text-text-tertiary font-mono text-xs">
                <Clock className="w-3 h-3" />
                <span>{eta} MIN</span>
              </div>
              <div className="w-1 h-1 bg-border-dim rounded-full" />
              <div className="flex items-center gap-2 text-text-tertiary font-mono text-xs">
                <Route className="w-3 h-3" />
                <span>0.8 KM</span>
              </div>
            </div>
          </div>
        </motion.div>
      </div>

      <div className="absolute bottom-32 left-4 right-4 z-10 pointer-events-none">
        <motion.div 
          initial={{ y: 20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          className="bg-bg-tertiary text-text-primary p-6 shadow-2xl pointer-events-auto rounded-geometric border border-border-dim"
        >
            <div className="flex items-start gap-4 mb-6">
                <div className="border border-border-dim p-2 shrink-0">
                    <Info className="w-5 h-5 text-accent-primary" />
                </div>
                <div className="space-y-1">
                    <h4 className="text-xs font-bold uppercase tracking-widest text-text-primary">System Protocol</h4>
                    <p className="text-[10px] text-text-tertiary leading-relaxed uppercase tracking-tighter">Please execute dry/wet segregation prior to collection arrival.</p>
                </div>
            </div>
            <button className="w-full bg-accent-primary text-white py-3 text-[10px] font-bold uppercase tracking-[0.2em] hover:opacity-90 transition-colors">
                Acknowledge
            </button>
        </motion.div>
      </div>
    </div>
  );
};

export default LiveMap;
