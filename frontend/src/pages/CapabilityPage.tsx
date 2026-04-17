import { useParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Zap, ArrowRight, CheckCircle2 } from 'lucide-react';

const CapabilityPage = () => {
  const { id } = useParams();
  
  // Format the ID into a readable title
  const title = id ? id.split('-').map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ') : 'Capability';

  return (
    <main className="pt-32 pb-20 px-6 md:px-16 min-h-screen flex flex-col items-center relative z-10">
      <motion.div 
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="max-w-[1000px] w-full"
      >
        <div className="inline-flex items-center gap-2 px-3 py-1 bg-white/5 border border-white/10 rounded-full text-xs font-semibold mb-6 text-[#b72e6a]">
          <Zap size={14} /> Service Detail
        </div>
        
        <h1 className="text-4xl md:text-6xl font-outfit font-extrabold mb-8 text-white tracking-tight leading-tight">
          {title}
        </h1>
        
        <div className="grid grid-cols-1 md:grid-cols-3 gap-12 mt-12">
            <div className="md:col-span-2">
                <p className="text-xl text-white/70 leading-relaxed mb-10">
                    Empower your enterprise with autonomous agentic execution for {title.toLowerCase()}. Our platform unifies reasoning, conversation, and secure tool orchestration into a seamless, governed workflow.
                </p>
                
                <div className="space-y-6 mb-12">
                    {[
                        "Enterprise-grade security and compliance",
                        "Seamless integration with 100+ SaaS platforms",
                        "Human-in-the-loop audit trails",
                        "Deploy to voice, chat, and email channels"
                    ].map((feature, i) => (
                        <div key={i} className="flex gap-4 items-center">
                            <div className="w-6 h-6 rounded-full bg-[#853694]/10 border border-[#853694] flex items-center justify-center shrink-0">
                                <CheckCircle2 size={14} className="text-[#b72e6a]" />
                            </div>
                            <span className="text-white/80">{feature}</span>
                        </div>
                    ))}
                </div>

                <div className="flex gap-4">
                    <button className="bg-[#853694] hover:bg-[#6a2b77] text-white font-bold py-4 px-10 rounded-full transition-all shadow-lg hover:scale-105 active:scale-95">
                        Get Started
                    </button>
                    <button className="bg-white/5 hover:bg-white/10 text-white font-bold py-4 px-10 rounded-full transition-all border border-white/10 hover:border-white/20">
                        View Sandbox
                    </button>
                </div>
            </div>

            <div className="space-y-6">
                <div className="p-6 rounded-2xl bg-[#0a0a0f] border border-white/5 shadow-2xl">
                    <h3 className="text-white font-bold mb-4 flex items-center gap-2">
                        <Zap size={18} className="text-[#b72e6a]" /> Related Services
                    </h3>
                    <ul className="space-y-3">
                        {['Prompt-Driven Workflows', 'ITSM Automation', 'L1 Support', 'Data Automation'].map(item => (
                            <li key={item} className="text-sm text-white/50 hover:text-[#b72e6a] cursor-pointer transition-colors flex items-center justify-between group">
                                {item} <ArrowRight size={12} className="opacity-0 group-hover:opacity-100 transition-opacity" />
                            </li>
                        ))}
                    </ul>
                </div>
                
                <div className="p-6 rounded-2xl bg-gradient-to-br from-accentTheme/20 to-transparent border border-[#853694]/20">
                    <h3 className="text-white font-bold mb-2">Need Help?</h3>
                    <p className="text-sm text-white/60 mb-4">Our solution architects are ready to help you design your agentic flow.</p>
                    <button className="text-[#b72e6a] text-sm font-bold flex items-center gap-1 hover:underline">
                        Contact Support <ArrowRight size={14} />
                    </button>
                </div>
            </div>
        </div>
      </motion.div>
    </main>
  );
};

export default CapabilityPage;
