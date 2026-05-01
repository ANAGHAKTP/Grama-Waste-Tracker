import React, { useState } from 'react';
import { LayoutGrid, Leaf, XCircle, Zap, ShieldAlert, ChevronDown, CheckCircle2, Search, Sparkles, Loader2 } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { cn } from '../lib/utils';
import { GoogleGenAI } from '@google/genai';

const ai = new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY! });

const GUIDELINES = [
  {
    category: 'Wet Waste',
    icon: Leaf,
    color: 'bg-green-100 text-green-700',
    items: ['Fruit peels', 'Vegetable scraps', 'Leftover food', 'Used tea bags'],
    description: 'Organic waste that can be composted. Always use green bins.'
  },
  {
    category: 'Dry Waste',
    icon: Zap,
    color: 'bg-blue-100 text-blue-700',
    items: ['Plastics', 'Paper', 'Cardboard', 'Glass bottles', 'Metal cans'],
    description: 'Non-biodegradable waste. Ensure items are dry before disposal.'
  },
  {
    category: 'Hazardous',
    icon: ShieldAlert,
    color: 'bg-red-100 text-red-700',
    items: ['Used batteries', 'Tablets/Medicine', 'Paints', 'Cleaning chemicals'],
    description: 'Requires special handling. Wrap securely and keep separate.'
  },
  {
    category: 'Sanitary',
    icon: XCircle,
    color: 'bg-orange-100 text-orange-700',
    items: ['Used diapers', 'Sanitary napkins', 'Medical waste'],
    description: 'Should be wrapped in paper and marked with a red dot.'
  }
];

const Education: React.FC = () => {
  const [expanded, setExpanded] = useState<string | null>(null);
  const [query, setQuery] = useState('');
  const [searching, setSearching] = useState(false);
  const [aiResult, setAiResult] = useState<{ category: string; instruction: string } | null>(null);

  const handleAiSearch = async () => {
    if (!query.trim() || searching) return;
    setSearching(true);
    setAiResult(null);
    try {
      const response = await ai.models.generateContent({
        model: "gemini-3-flash-preview",
        contents: `Classify which waste category this item belongs to: "${query}". 
        Categories: Wet Waste, Dry Waste, Hazardous, Sanitary, or E-Waste.
        Provide the answer in JSON format: { "category": "...", "instruction": "..." } 
        where instruction is a short (1 sentence) disposal rule.`,
        config: { responseMimeType: "application/json" }
      });
      
      const res = JSON.parse(response.text || '{}');
      setAiResult(res);
    } catch (err) {
      console.error("AI Search failed", err);
    } finally {
      setSearching(false);
    }
  };

  return (
    <div className="pb-32 space-y-12">
      <header className="border-b border-border-dim pb-8">
        <h1 className="text-4xl font-display font-bold text-text-primary tracking-tight">Docs.Protocol</h1>
        <p className="text-text-tertiary text-[10px] font-bold uppercase tracking-[0.2em] mt-2">Public Segregation Standards</p>
      </header>

      {/* AI Segregation Assistant */}
      <section className="space-y-4">
        <div className="flex items-baseline justify-between">
           <h3 className="text-[10px] font-bold text-text-tertiary uppercase tracking-[0.2em]">Neural Assistant</h3>
           <div className="h-px bg-border-dim flex-1 mx-4" />
        </div>
        
        <div className="geometric-card bg-bg-secondary border-border-dim shadow-sm space-y-4">
          <div className="flex gap-2">
            <div className="relative flex-1">
              <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-text-tertiary" />
              <input 
                type="text"
                placeholder="DISPOSAL QUERY: e.g. 'Old Battery'..."
                className="w-full bg-bg-primary border border-border-dim py-4 pl-12 pr-4 text-[10px] font-mono tracking-tight uppercase text-text-primary focus:bg-bg-tertiary focus:ring-0 focus:border-accent-primary transition-all outline-none rounded-sm"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleAiSearch()}
              />
            </div>
            <button 
              onClick={handleAiSearch}
              disabled={searching || !query}
              className="bg-accent-primary text-white px-6 rounded-sm hover:opacity-90 transition-all disabled:bg-bg-tertiary disabled:text-text-tertiary border-none"
              style={{ borderRadius: 'var(--radius-geometric)' }}
            >
              {searching ? <Loader2 className="w-5 h-5 animate-spin" /> : <Sparkles className="w-5 h-5" />}
            </button>
          </div>

          <AnimatePresence>
            {aiResult && (
              <motion.div 
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: 'auto' }}
                exit={{ opacity: 0, height: 0 }}
                className="overflow-hidden"
              >
                <div className="pt-4 border-t border-border-dim flex items-start gap-4">
                  <div className="bg-accent-primary/10 p-2 text-accent-primary">
                    <Sparkles className="w-4 h-4" />
                  </div>
                  <div>
                    <h4 className="text-[10px] font-bold uppercase tracking-widest text-text-tertiary mb-1">AI Recommendation</h4>
                    <p className="text-xs font-bold text-text-primary tracking-tight">
                      <span className="text-accent-primary uppercase mr-2">[{aiResult.category}]</span>
                      {aiResult.instruction}
                    </p>
                  </div>
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </section>

      <div className="space-y-4">
        {GUIDELINES.map((guide) => (
          <div key={guide.category} className="border border-border-dim overflow-hidden bg-bg-secondary hover:bg-bg-tertiary transition-colors" style={{ borderRadius: 'var(--radius-geometric)' }}>
            <button 
              onClick={() => setExpanded(expanded === guide.category ? null : guide.category)}
              className="w-full p-6 flex items-center justify-between text-left group"
            >
              <div className="flex items-center gap-6">
                <div className={cn("p-4 border border-border-dim transition-colors bg-bg-primary", expanded === guide.category && "border-accent-primary text-accent-primary")}>
                  <guide.icon className="w-6 h-6" />
                </div>
                <div>
                  <h3 className="text-sm font-bold text-text-primary uppercase tracking-widest">{guide.category}</h3>
                  <p className="text-[9px] text-text-tertiary uppercase tracking-tighter mt-1 font-mono">{guide.description}</p>
                </div>
              </div>
              <ChevronDown className={cn("w-4 h-4 text-text-tertiary transition-transform group-hover:text-text-primary", expanded === guide.category && "rotate-180")} />
            </button>

            <AnimatePresence>
              {expanded === guide.category && (
                <motion.div
                  initial={{ height: 0 }}
                  animate={{ height: 'auto' }}
                  exit={{ height: 0 }}
                  className="overflow-hidden"
                >
                  <div className="px-6 pb-8 pt-2">
                    <div className="h-px bg-border-dim mb-8" />
                    <div className="geometric-grid grid-cols-2">
                      {guide.items.map((item) => (
                        <div key={item} className="flex items-center gap-3">
                          <CheckCircle2 className="w-3 h-3 text-accent-tertiary" />
                          <span className="text-[10px] font-bold text-text-secondary uppercase tracking-tight">{item}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                </motion.div>
              )}
            </AnimatePresence>
          </div>
        ))}
      </div>

      {/* Rewards call to action - Geometric Design */}
      <div className="bg-bg-tertiary p-8 text-white relative overflow-hidden border border-border-dim" style={{ borderRadius: 'var(--radius-geometric)' }}>
        <div className="relative z-10 space-y-6">
          <div>
            <h3 className="text-xl font-display font-medium tracking-tight mb-2 text-text-primary">Incentive Tier 02</h3>
            <p className="text-[10px] text-text-tertiary uppercase tracking-[0.15em] leading-relaxed max-w-[240px]">
                Maintain protocol compliance to aggregate village fair validation credits.
            </p>
          </div>
          
          <div className="space-y-3">
            <div className="bg-white/5 h-1 w-full overflow-hidden">
                <div className="bg-accent-primary h-full w-2/3" />
            </div>
            <div className="flex justify-between items-baseline">
                <span className="text-[10px] font-mono text-text-tertiary font-bold uppercase tracking-widest">Progress</span>
                <span className="text-xs font-mono font-bold tracking-tighter text-accent-primary">450 / 600 UNIT</span>
            </div>
          </div>
        </div>
        
        {/* Abstract background shape */}
        <div className="absolute top-0 right-0 w-32 h-32 bg-accent-primary/10 -translate-y-1/2 translate-x-1/2 rotate-45" />
      </div>
    </div>
  );
};

export default Education;
