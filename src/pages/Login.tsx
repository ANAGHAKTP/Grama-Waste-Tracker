import React, { useState } from 'react';
import { signInWithPopup, GoogleAuthProvider } from 'firebase/auth';
import { auth, db } from '../lib/firebase';
import { doc, getDoc, setDoc } from 'firebase/firestore';
import { useNavigate } from 'react-router-dom';
import { Trash2, ShieldCheck, Leaf, Loader2, AlertCircle } from 'lucide-react';
import { motion } from 'motion/react';
import { cn } from '../lib/utils';

const Login: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleLogin = async () => {
    if (loading) return;
    setLoading(true);
    setError(null);

    try {
      const provider = new GoogleAuthProvider();
      const result = await signInWithPopup(auth, provider);
      const user = result.user;

      // Check if user profile exists
      const docRef = doc(db, 'users', user.uid);
      const docSnap = await getDoc(docRef);

      if (!docSnap.exists()) {
        await setDoc(docRef, {
          uid: user.uid,
          displayName: user.displayName,
          email: user.email,
          role: 'citizen',
          createdAt: new Date().toISOString(),
        });
      }

      navigate('/');
    } catch (err: any) {
      console.error('Login Error:', err);
      // Don't show error if user just closed the popup
      if (err.code !== 'auth/popup-closed-by-user' && err.code !== 'auth/cancelled-popup-request') {
        setError(err.message || 'An error occurred during sign in.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col items-center justify-center p-6 bg-bg-primary text-center selection:bg-accent-primary/20">
      <motion.div
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        className="mb-16 space-y-8"
      >
        <div className="w-24 h-24 bg-bg-tertiary border border-border-dim flex items-center justify-center mx-auto shadow-2xl rotate-45 transform-gpu group cursor-pointer hover:rotate-90 transition-transform duration-700">
          <Trash2 className="w-10 h-10 text-accent-primary -rotate-45 group-hover:-rotate-90 transition-transform duration-700" />
        </div>
        
        <div className="space-y-2">
          <h1 className="text-5xl font-display font-bold text-text-primary tracking-tighter uppercase">Grama.Waste</h1>
          <div className="flex items-center justify-center gap-4">
             <div className="h-px bg-border-dim flex-1 max-w-[40px]" />
             <span className="text-[10px] font-bold text-text-tertiary uppercase tracking-[0.3em]">Smart Village Hub</span>
             <div className="h-px bg-border-dim flex-1 max-w-[40px]" />
          </div>
        </div>
      </motion.div>

      <div className="geometric-grid grid-cols-2 max-w-xs w-full mb-12">
        <div className="flex flex-col items-center py-6">
          <ShieldCheck className="w-5 h-5 text-accent-primary mb-3" />
          <span className="text-[10px] font-bold uppercase tracking-widest text-text-primary">Identity</span>
        </div>
        <div className="flex flex-col items-center py-6">
          <Leaf className="w-5 h-5 text-accent-primary mb-3" />
          <span className="text-[10px] font-bold uppercase tracking-widest text-text-primary">Sustainability</span>
        </div>
      </div>

      {error && (
        <motion.div 
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          className="mb-8 p-5 border border-accent-error/20 bg-accent-error/10 text-accent-error text-xs font-medium flex items-center gap-3 max-w-xs"
        >
          <AlertCircle className="w-4 h-4 shrink-0" />
          <p className="text-left leading-relaxed">{error}</p>
        </motion.div>
      )}

      <div className="w-full max-w-xs space-y-4">
        <button
          id="login-button"
          onClick={handleLogin}
          disabled={loading}
          className={cn(
            "w-full py-5 px-6 font-bold transition-all flex items-center justify-center gap-4 border border-border-dim relative overflow-hidden group",
            loading 
              ? "bg-bg-secondary text-text-tertiary cursor-not-allowed" 
              : "bg-bg-tertiary text-text-primary hover:bg-bg-primary"
          )}
        >
          {loading ? (
            <Loader2 className="w-5 h-5 animate-spin" />
          ) : (
            <img src="https://www.google.com/favicon.ico" className="w-4 h-4 grayscale group-hover:grayscale-0 transition-all" alt="Google" />
          )}
          <span className="uppercase text-xs tracking-[0.2em]">{loading ? 'Processing' : 'Access Hub'}</span>
        </button>

        <p className="text-[9px] text-text-tertiary uppercase tracking-widest font-bold">
          © 2026 GRAMA-WASTE SYSTEMS • v1.0
        </p>
      </div>
    </div>
  );
};

export default Login;
