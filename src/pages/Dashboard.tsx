import React, { useState, useEffect } from 'react';
import { useAuth } from '../lib/AuthContext';
import { motion } from 'motion/react';
import { Clock, MapPin, AlertCircle, ChevronRight, LayoutGrid, Info, LogOut, Navigation, Sparkles, Sun, Moon } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import { formatDate } from '../lib/utils';
import { auth } from '../lib/firebase';
import { GoogleGenAI } from '@google/genai';
import { useTheme } from '../lib/ThemeContext';

const ai = new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY! });

const Dashboard: React.FC = () => {
  const { profile } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const navigate = useNavigate();
  const [dailyInsight, setDailyInsight] = useState<string>('');

  useEffect(() => {
    const fetchInsight = async () => {
      try {
        const response = await ai.models.generateContent({
          model: "gemini-3-flash-preview",
          contents: "Generate a 1-sentence helpful tip for a rural village resident about waste management, composting, or recycling. Be encouraging and simple.",
        });
        setDailyInsight(response.text || 'Keep your village clean, start composting today!');
      } catch (err) {
        console.error("Failed to fetch AI insight", err);
        setDailyInsight('Keep your village clean, start composting today!');
      }
    };
    fetchInsight();
  }, []);

  const handleSignOut = async () => {
    await auth.signOut();
    navigate('/login');
  };

  const QUICK_ACTIONS = [
    { to: '/map', label: 'Live Tracking', icon: MapPin, color: 'text-accent-primary' },
    { to: '/report', label: 'Report Issue', icon: AlertCircle, color: 'text-accent-error' },
    { to: '/education', label: 'Waste Guide', icon: Info, color: 'text-accent-secondary' },
    { to: '/', label: 'My Reports', icon: LayoutGrid, color: 'text-text-tertiary' },
  ];

  return (
    <div className="pb-24 space-y-10">
      <header className="flex justify-between items-start border-b border-border-dim pb-8">
        <div>
          <span className="text-[10px] font-bold tracking-[0.2em] uppercase text-text-tertiary">Unit Identifier</span>
          <h1 className="text-4xl font-display font-bold text-text-primary mt-2 tracking-tight">
            {profile?.displayName?.split(' ')[0] || 'User'}
          </h1>
          <p className="text-text-secondary text-sm mt-1 flex items-center gap-2">
            <MapPin className="w-3.5 h-3.5 text-accent-primary" />
            {profile?.address || 'Set your location'}
          </p>
        </div>
        <div className="flex gap-2">
          <button 
            onClick={toggleTheme}
            className="p-3 border border-border-dim hover:bg-bg-tertiary transition-colors rounded-geometric shadow-sm"
            title="Toggle Theme"
          >
            {theme === 'light' ? <Moon className="w-5 h-5 text-text-tertiary" /> : <Sun className="w-5 h-5 text-text-tertiary" />}
          </button>
          <button 
            onClick={handleSignOut}
            className="p-3 border border-border-dim hover:bg-bg-tertiary transition-colors rounded-geometric shadow-sm"
            title="Sign Out"
          >
            <LogOut className="w-5 h-5 text-text-tertiary" />
          </button>
        </div>
      </header>

      {/* AI Daily Insight */}
      {dailyInsight && (
        <motion.div 
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          className="bg-bg-secondary border border-accent-primary/20 p-5 flex gap-4 items-start shadow-sm"
          style={{ borderRadius: 'var(--radius-geometric)' }}
        >
          <div className="bg-accent-primary/10 p-2.5 text-accent-primary rounded-lg shrink-0">
            <Sparkles className="w-5 h-5" />
          </div>
          <div className="space-y-1.5">
            <h4 className="text-[10px] font-bold text-accent-primary uppercase tracking-widest leading-none">Panchayat Intelligence</h4>
            <p className="text-sm text-text-primary font-medium leading-relaxed italic">"{dailyInsight}"</p>
          </div>
        </motion.div>
      )}

      {/* Arrival Alert Card - Material 3 Style */}
      <motion.div
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        className="geometric-card bg-accent-primary text-white border-none shadow-xl shadow-accent-primary/20 p-7 overflow-hidden relative"
      >
        <div className="relative z-10 flex items-center justify-between">
          <div className="space-y-4">
            <div className="inline-flex items-center gap-2 px-3 py-1.5 bg-white/10 rounded-full">
              <Clock className="w-3.5 h-3.5 text-white" />
              <span className="text-[10px] font-bold uppercase tracking-widest">In Transit</span>
            </div>
            <div>
              <h2 className="text-4xl font-display font-medium tracking-tight">12 Minutes</h2>
              <p className="text-white/80 text-xs mt-1 font-mono uppercase tracking-widest">Market Road • Sector 04</p>
            </div>
          </div>
          <div className="w-16 h-16 bg-white/10 flex items-center justify-center rounded-2xl">
            <Navigation className="w-8 h-8 text-white" />
          </div>
        </div>
        
        {/* Decorative elements */}
        <div className="absolute top-0 right-0 w-32 h-32 bg-white/5 -translate-y-1/2 translate-x-1/2 rounded-full blur-3xl" />
      </motion.div>

      {/* Quick Actions Grid */}
      <section>
        <div className="flex items-baseline justify-between mb-6">
          <h3 className="text-[10px] font-bold text-text-tertiary uppercase tracking-[0.2em]">Services</h3>
          <div className="h-px bg-border-dim flex-1 mx-4" />
        </div>
        <div className="geometric-grid grid-cols-2">
          {QUICK_ACTIONS.map((action) => (
            <Link key={action.to} to={action.to} className="group">
              <div className="space-y-6">
                <div className={`${action.color} transition-transform group-hover:scale-110 duration-500`}>
                  <action.icon className="w-6 h-6" />
                </div>
                <div className="flex items-center justify-between">
                  <span className="font-bold text-text-primary text-sm">{action.label}</span>
                  <ChevronRight className="w-4 h-4 text-text-tertiary group-hover:text-text-primary transition-colors" />
                </div>
              </div>
            </Link>
          ))}
        </div>
      </section>

      {/* Schedule Summary - Geometric List */}
      <section>
        <div className="flex items-baseline justify-between mb-6">
          <h3 className="text-[10px] font-bold text-text-tertiary uppercase tracking-[0.2em]">Logistics</h3>
          <div className="h-px bg-border-dim flex-1 mx-4" />
          <button className="text-[10px] font-bold text-accent-primary uppercase tracking-wider">Expand</button>
        </div>
        <div className="divide-y divide-border-dim border-y border-border-dim">
          {[
            { day: 'MON', type: 'Dry Waste', time: '08:00', status: 'upcoming' },
            { day: 'WED', type: 'Wet Waste', time: '07:30', status: 'planned' },
            { day: 'FRI', type: 'Recyclables', time: '09:00', status: 'planned' },
          ].map((item) => (
            <div key={item.day} className="py-4 flex items-center justify-between group cursor-pointer hover:bg-bg-secondary transition-colors px-2">
              <div className="flex items-center gap-6">
                <span className="font-mono text-sm font-bold text-text-tertiary group-hover:text-text-primary transition-colors w-8">{item.day}</span>
                <div>
                  <h4 className="font-bold text-text-primary text-sm tracking-tight">{item.type}</h4>
                  <p className="text-[10px] text-text-tertiary font-mono italic uppercase">{item.time} am • collection</p>
                </div>
              </div>
              <div className="w-2 h-2 rounded-full bg-border-dim group-hover:bg-accent-primary transition-colors" />
            </div>
          ))}
        </div>
      </section>
    </div>
  );
};

export default Dashboard;
