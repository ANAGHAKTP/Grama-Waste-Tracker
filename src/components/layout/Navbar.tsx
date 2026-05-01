import React from 'react';
import { NavLink } from 'react-router-dom';
import { Home, Map, Camera, BookOpen, User as UserIcon, ShieldAlert } from 'lucide-react';
import { useAuth } from '../../lib/AuthContext';
import { cn } from '../../lib/utils';
import { motion } from 'motion/react';

const Navbar: React.FC = () => {
  const { profile } = useAuth();

  const navItems = [
    { to: '/', icon: Home, label: 'Hub' },
    { to: '/map', icon: Map, label: 'Live' },
    { to: '/report', icon: Camera, label: 'Log' },
    { to: '/education', icon: BookOpen, label: 'Docs' },
  ];

  if (profile?.role === 'admin') {
    navItems.push({ to: '/admin', icon: ShieldAlert, label: 'Root' });
  }

  return (
    <nav className="fixed bottom-6 left-1/2 -translate-x-1/2 w-[calc(100%-3rem)] max-w-sm bg-bg-secondary text-text-primary p-2 border border-border-dim shadow-2xl z-50 rounded-geometric md:left-6 md:top-1/2 md:-translate-y-1/2 md:translate-x-0 md:w-20 md:h-auto md:py-8">
      <div className="flex justify-between items-center md:flex-col md:gap-8">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              cn(
                'flex-1 flex flex-col items-center gap-1 py-1 transition-all relative group',
                isActive ? 'text-accent-primary' : 'text-text-tertiary hover:text-text-secondary'
              )
            }
          >
            {({ isActive }) => (
              <>
                <div className="relative px-5 py-1">
                  <item.icon className={cn("w-6 h-6 transition-transform", isActive ? "scale-110" : "scale-100")} />
                  {isActive && (
                    <motion.div 
                      layoutId="active-indicator"
                      className="absolute inset-0 bg-accent-primary/10 rounded-full -z-10"
                      transition={{ type: 'spring', bounce: 0.2, duration: 0.5 }}
                    />
                  )}
                </div>
                <span className={cn(
                  "text-[10px] font-bold uppercase tracking-widest mt-1",
                  isActive ? "opacity-100" : "opacity-70"
                )}>
                  {item.label}
                </span>
              </>
            )}
          </NavLink>
        ))}
      </div>
    </nav>
  );
};

export default Navbar;
