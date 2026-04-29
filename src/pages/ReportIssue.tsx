import React, { useState } from 'react';
import { Camera, AlertCircle, CheckCircle2, ChevronRight, Upload, Loader2, Sparkles } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { storage, db, auth } from '../lib/firebase';
import { ref, uploadBytes, getDownloadURL } from 'firebase/storage';
import { collection, addDoc } from 'firebase/firestore';
import { GoogleGenAI } from '@google/genai';
import { cn } from '../lib/utils';
import { useNavigate } from 'react-router-dom';

const ai = new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY! });

const ReportIssue: React.FC = () => {
  const [image, setImage] = useState<File | null>(null);
  const [preview, setPreview] = useState<string | null>(null);
  const [description, setDescription] = useState('');
  const [analyzing, setAnalyzing] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [aiAnalysis, setAiAnalysis] = useState<string | null>(null);
  const navigate = useNavigate();

  const handleImageChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setImage(file);
      setPreview(URL.createObjectURL(file));
      analyzeImage(file);
    }
  };

  const analyzeImage = async (file: File) => {
    setAnalyzing(true);
    try {
      const reader = new FileReader();
      const base64Promise = new Promise<string>((resolve) => {
        reader.onloadend = () => {
          const base64String = (reader.result as string).split(',')[1];
          resolve(base64String);
        };
        reader.readAsDataURL(file);
      });

      const base64Data = await base64Promise;

      const response = await ai.models.generateContent({
        model: "gemini-3-flash-preview",
        contents: {
          parts: [
            { text: `Analyze this image of a waste/garbage issue. 
                      Classify the waste type and suggest prioritized actions.
                      Return JSON: { "type": "...", "action": "...", "severity": "low" | "medium" | "high" }` },
            {
              inlineData: {
                data: base64Data,
                mimeType: file.type
              }
            }
          ]
        },
        config: { responseMimeType: "application/json" }
      });

      const res = JSON.parse(response.text || '{}');
      setAiAnalysis(`Type: ${res.type} | Action: ${res.action} | Priority: ${res.severity.toUpperCase()}`);
    } catch (err) {
      console.error("AI Analysis failed", err);
    } finally {
      setAnalyzing(false);
    }
  };

  const handleSubmit = async () => {
    if (!image || !auth.currentUser) return;
    setSubmitting(true);

    try {
      // 1. Upload Image
      const storageRef = ref(storage, `reports/${Date.now()}_${image.name}`);
      await uploadBytes(storageRef, image);
      const photoUrl = await getDownloadURL(storageRef);

      // 2. Save to Firestore
      await addDoc(collection(db, 'reports'), {
        reporterId: auth.currentUser.uid,
        photoUrl,
        description,
        aiAnalysis,
        status: 'pending',
        severity: 'medium',
        location: { lat: 12.9716, lng: 77.5946 }, // Mock GPS
        createdAt: new Date().toISOString()
      });

      navigate('/');
    } catch (err) {
      console.error("Submission failed", err);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="pb-32 space-y-10">
      <header className="border-b border-border-dim pb-8">
        <h1 className="text-4xl font-display font-bold text-text-primary tracking-tight">Log.Blackspot</h1>
        <p className="text-text-tertiary text-[10px] font-bold uppercase tracking-[0.2em] mt-2">Panchayat Enforcement System</p>
      </header>

      <div className="space-y-8">
        {/* Photo Upload Area */}
        <div 
          onClick={() => document.getElementById('camera-input')?.click()}
          className={cn(
            "aspect-square border border-border-dim flex flex-col items-center justify-center transition-all cursor-pointer overflow-hidden relative group",
            preview ? "border-transparent bg-bg-secondary" : "bg-bg-secondary hover:bg-bg-tertiary"
          )}
          style={{ borderRadius: 'var(--radius-geometric)' }}
        >
          {preview ? (
            <>
                <img src={preview} className="w-full h-full object-cover grayscale group-hover:grayscale-0 transition-all duration-700" alt="Preview" />
                <div className="absolute inset-0 bg-slate-900/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                    <span className="text-white text-[10px] font-bold uppercase tracking-[0.3em]">Redefine Image</span>
                </div>
            </>
          ) : (
            <>
              <div className="border border-border-dim p-4 text-text-tertiary mb-4 group-hover:border-accent-primary group-hover:text-accent-primary transition-colors">
                <Camera className="w-6 h-6" />
              </div>
              <span className="text-[10px] font-bold uppercase tracking-[0.2em] text-text-primary">Initiate Capture</span>
              <p className="text-[9px] text-text-tertiary mt-2 uppercase tracking-tighter">Cam / Storage Input</p>
            </>
          )}
          <input 
            type="file" 
            id="camera-input" 
            accept="image/*" 
            capture="environment"
            className="hidden" 
            onChange={handleImageChange}
          />
        </div>

        {/* AI Analysis View */}
        <AnimatePresence>
            {(analyzing || aiAnalysis) && (
                <motion.div
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0 }}
                    className="geometric-card bg-bg-tertiary text-text-primary border border-accent-primary/20 shadow-xl shadow-black/40"
                >
                    <div className="flex items-center gap-2 mb-3 text-accent-primary">
                        <span className="text-[9px] font-bold uppercase tracking-[0.2em]">Neural Intelligence Analysis</span>
                    </div>
                    {analyzing ? (
                        <div className="flex items-center gap-3 py-2">
                            <Loader2 className="w-4 h-4 animate-spin text-accent-primary" />
                            <span className="text-[10px] text-text-secondary uppercase tracking-widest italic">Scanning infrastructure...</span>
                        </div>
                    ) : (
                        <p className="text-xs text-text-secondary leading-relaxed font-mono uppercase tracking-tight">{aiAnalysis}</p>
                    )}
                </motion.div>
            )}
        </AnimatePresence>

        <div className="space-y-4">
          <label className="text-[10px] font-bold uppercase tracking-[0.2em] text-text-tertiary">Structural Metadata</label>
          <textarea
            placeholder="ADDITIONAL CONTEXT..."
            className="w-full bg-bg-secondary border border-border-dim p-6 text-[10px] font-mono tracking-tight uppercase text-text-primary focus:bg-bg-tertiary focus:ring-0 focus:border-accent-primary transition-all outline-none"
            style={{ borderRadius: 'var(--radius-geometric)' }}
            rows={3}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </div>

        <div className="bg-amber-950/20 p-6 border border-amber-900/40 flex gap-4" style={{ borderRadius: 'var(--radius-geometric)' }}>
          <AlertCircle className="w-5 h-5 text-amber-500 shrink-0" />
          <div className="space-y-1">
            <p className="text-[10px] font-bold text-amber-500 uppercase tracking-widest">Geo-Lock Sync</p>
            <p className="text-[9px] text-amber-700 uppercase tracking-tighter font-medium">Automatic coordinate embedding enabled for verification.</p>
          </div>
        </div>

        <button
          disabled={!image || submitting || analyzing}
          onClick={handleSubmit}
          className={cn(
            "w-full py-5 font-bold uppercase tracking-[0.3em] text-[11px] transition-all relative overflow-hidden text-white",
            image && !submitting 
              ? "bg-accent-primary hover:opacity-90 shadow-lg shadow-accent-primary/20" 
              : "bg-bg-tertiary text-text-tertiary cursor-not-allowed"
          )}
          style={{ borderRadius: 'var(--radius-geometric)', border: 'none' }}
        >
          {submitting ? <Loader2 className="w-5 h-5 animate-spin mx-auto" /> : (
            <div className="flex items-center justify-center gap-3">
              <CheckCircle2 className="w-4 h-4" />
              <span>Submit to Root</span>
            </div>
          )}
        </button>
      </div>
    </div>
  );
};

export default ReportIssue;
