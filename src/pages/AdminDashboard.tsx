import React, { useEffect, useState } from 'react';
import { collection, query, orderBy, onSnapshot, updateDoc, doc } from 'firebase/firestore';
import { db, handleFirestoreError, OperationType } from '../lib/firebase';
import { BlackspotReport } from '../types';
import { motion, AnimatePresence } from 'motion/react';
import { AlertTriangle, CheckCircle, Clock, MapPin, ExternalLink, Shield, Sparkles, Loader2 } from 'lucide-react';
import { formatDate } from '../lib/utils';
import { cn } from '../lib/utils';
import { GoogleGenAI } from '@google/genai';

const ai = new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY! });

const AdminDashboard: React.FC = () => {
  const [reports, setReports] = useState<BlackspotReport[]>([]);
  const [loading, setLoading] = useState(true);
  const [summarizing, setSummarizing] = useState(false);
  const [aiSummary, setAiSummary] = useState<string>('');

  useEffect(() => {
    const q = query(collection(db, 'reports'), orderBy('createdAt', 'desc'));
    
    const unsubscribe = onSnapshot(q, (snapshot) => {
      const reportsData = snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      })) as BlackspotReport[];
      setReports(reportsData);
      setLoading(false);
    }, (error) => {
        handleFirestoreError(error, OperationType.LIST, 'reports');
    });

    return () => unsubscribe();
  }, []);

  const generateSummary = async () => {
    if (reports.length === 0 || summarizing) return;
    setSummarizing(true);
    try {
      const reportStatus = reports.map(r => `- ${r.description} (${r.status})`).join('\n');
      const response = await ai.models.generateContent({
        model: "gemini-3-flash-preview",
        contents: `Provide a high-level, executive summary for the Village Panchayat based on these reports:\n${reportStatus}. 
        Format: 2 sentences. Focused on status, primary waste issues, and recommendation for vehicle deployment.`,
      });
      setAiSummary(response.text || 'No summary available.');
    } catch (err) {
      console.error("AI Summary failed", err);
    } finally {
      setSummarizing(false);
    }
  };

  const handleResolve = async (id: string) => {
    try {
      await updateDoc(doc(db, 'reports', id), {
        status: 'resolved',
        resolvedAt: new Date().toISOString()
      });
    } catch (err) {
      handleFirestoreError(err, OperationType.UPDATE, `reports/${id}`);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'resolved': return 'bg-green-100 text-green-700 border-green-200';
      case 'investigating': return 'bg-blue-100 text-blue-700 border-blue-200';
      default: return 'bg-orange-100 text-orange-700 border-orange-200';
    }
  };

  return (
    <div className="pb-24 space-y-10">
      <header className="flex items-center justify-between border-b border-border-dim pb-8">
        <div>
          <div className="flex items-center gap-2 text-accent-primary mb-2">
             <Shield className="w-3 h-3" />
             <span className="text-[10px] font-bold uppercase tracking-[0.2em]">Panchayat Internal</span>
          </div>
          <h1 className="text-4xl font-display font-bold text-text-primary tracking-tight">Systems.Terminal</h1>
        </div>
        <div className="bg-bg-tertiary text-text-primary p-4 rounded-geometric shadow-xl border border-border-dim">
          <div className="flex flex-col items-end">
            <span className="text-[10px] font-bold text-text-tertiary uppercase tracking-widest">Active Units</span>
            <span className="text-xl font-mono font-bold">0x04</span>
          </div>
        </div>
      </header>

      {/* AI Executive Summary */}
      <section className="space-y-4">
        <div className="flex items-baseline justify-between">
          <h3 className="text-[10px] font-bold text-text-tertiary uppercase tracking-[0.2em]">Neural Intelligence</h3>
          <div className="h-px bg-border-dim flex-1 mx-4" />
          <button 
            onClick={generateSummary}
            disabled={summarizing || reports.length === 0}
            className="flex items-center gap-2 text-[10px] font-bold text-accent-primary uppercase tracking-widest hover:opacity-80 transition-colors disabled:opacity-50"
          >
            {summarizing ? <Loader2 className="w-3 h-3 animate-spin" /> : <Sparkles className="w-3 h-3" />}
            Generate Summary
          </button>
        </div>
        
        <AnimatePresence>
          {aiSummary && (
            <motion.div 
              initial={{ opacity: 0, y: -10 }}
              animate={{ opacity: 1, y: 0 }}
              className="geometric-card bg-bg-secondary border-accent-primary/20 p-6"
            >
              <div className="flex gap-4">
                <div className="bg-accent-primary p-2 text-white shrink-0 h-fit">
                  <Sparkles className="w-4 h-4" />
                </div>
                <div>
                  <h4 className="text-[10px] font-bold text-accent-primary uppercase tracking-widest mb-1">Executive Report</h4>
                  <p className="text-sm text-text-primary font-medium leading-relaxed italic">"{aiSummary}"</p>
                </div>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </section>

      <div className="geometric-grid grid-cols-2">
        <div className="space-y-1">
            <h4 className="text-3xl font-display font-bold text-text-primary">{reports.length}</h4>
            <p className="text-[10px] text-text-tertiary font-bold uppercase tracking-widest">Total Intake</p>
        </div>
        <div className="space-y-1 border-l border-border-dim">
            <h4 className="text-3xl font-display font-bold text-accent-primary">
                {reports.filter(r => r.status === 'pending').length}
            </h4>
            <p className="text-[10px] text-text-tertiary font-bold uppercase tracking-widest">Unresolved</p>
        </div>
      </div>

      <section>
        <div className="flex items-baseline justify-between mb-6">
          <h3 className="text-[10px] font-bold text-text-tertiary uppercase tracking-[0.2em]">Data Feed</h3>
          <div className="h-px bg-border-dim flex-1 mx-4" />
        </div>
        
        {loading ? (
            <div className="flex justify-center py-20 grayscale opacity-20">
                <Clock className="w-12 h-12 animate-spin text-text-tertiary" />
            </div>
        ) : (
            <div className="space-y-2">
            {reports.map((report) => (
                <motion.div 
                    layout
                    key={report.id} 
                    className="geometric-card bg-bg-secondary hover:bg-bg-tertiary border-border-dim"
                >
                    <div className="flex gap-6 items-start">
                        <div className="w-24 h-24 bg-bg-primary border border-border-dim p-1 shrink-0 rounded-sm">
                            <img 
                                src={report.photoUrl} 
                                className="w-full h-full object-cover rounded-sm grayscale hover:grayscale-0 transition-all duration-700" 
                                alt="Issue" 
                            />
                        </div>
                        <div className="flex-1 min-w-0 space-y-3">
                            <div className="flex items-center justify-between">
                                <span className={cn(
                                    "text-[9px] font-bold px-2 py-0.5 border uppercase tracking-wider",
                                    report.status === 'resolved' ? 'bg-accent-tertiary/10 text-accent-tertiary border-accent-tertiary/20' : 'bg-accent-error/10 text-accent-error border-accent-error/20'
                                )}>
                                    {report.status}
                                </span>
                                <span className="font-mono text-[9px] text-text-tertiary font-bold">{formatDate(report.createdAt)}</span>
                            </div>
                            <h4 className="font-bold text-text-primary text-sm tracking-tight leading-snug">{report.description || 'Null Description'}</h4>
                            <div className="flex items-center gap-2 text-text-tertiary text-[10px] font-mono uppercase">
                                <MapPin className="w-3 h-3" />
                                <span>COORD: 12.97 / 77.59</span>
                            </div>
                        </div>
                    </div>
                    
                    {report.status !== 'resolved' && (
                        <div className="mt-6 pt-4 border-t border-border-dim flex gap-2">
                            <button 
                                onClick={() => handleResolve(report.id)}
                                className="flex-1 bg-accent-primary text-white py-3 rounded-geometric text-[10px] font-bold uppercase tracking-widest hover:opacity-90 transition-colors flex items-center justify-center gap-2"
                            >
                                <CheckCircle className="w-4 h-4" />
                                Execute Resolution
                            </button>
                            <button className="border border-border-dim text-text-tertiary p-3 rounded-geometric hover:bg-bg-tertiary transition-colors">
                                <ExternalLink className="w-4 h-4" />
                            </button>
                        </div>
                    )}
                </motion.div>
            ))}
            </div>
        )}
      </section>
    </div>
  );
};

export default AdminDashboard;
