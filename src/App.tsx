import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './lib/AuthContext';
import { ThemeProvider } from './lib/ThemeContext';
import Navbar from './components/layout/Navbar';

// Lazy loading or direct imports
import Dashboard from './pages/Dashboard';
import LiveMap from './pages/LiveMap';
import ReportIssue from './pages/ReportIssue';
import Education from './pages/Education';
import Login from './pages/Login';
import AdminDashboard from './pages/AdminDashboard';

const ProtectedRoute: React.FC<{ children: React.ReactNode; adminOnly?: boolean }> = ({ children, adminOnly }) => {
  const { user, profile, loading } = useAuth();

  if (loading) return <div>Loading...</div>;
  if (!user) return <Navigate to="/login" />;
  if (adminOnly && profile?.role !== 'admin') return <Navigate to="/" />;

  return <>{children}</>;
};

export default function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <Router>
        <div className="min-h-screen flex flex-col">
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route
              path="*"
              element={
                <ProtectedRoute>
                  <Navbar />
                  <main className="flex-1 container mx-auto px-4 py-8 max-w-lg">
                    <Routes>
                      <Route path="/" element={<Dashboard />} />
                      <Route path="/map" element={<LiveMap />} />
                      <Route path="/report" element={<ReportIssue />} />
                      <Route path="/education" element={<Education />} />
                      <Route
                        path="/admin"
                        element={
                          <ProtectedRoute adminOnly>
                            <AdminDashboard />
                          </ProtectedRoute>
                        }
                      />
                    </Routes>
                  </main>
                </ProtectedRoute>
              }
            />
          </Routes>
        </div>
      </Router>
    </AuthProvider>
    </ThemeProvider>
  );
}
