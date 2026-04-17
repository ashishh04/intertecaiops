import { useState, useEffect } from 'react';
import {
  ChevronDown, Workflow, Search, Cog, Mic, MessageSquare, Mail,
  Users, Shield, Network, Cloud, Server, Zap, LifeBuoy, Building2,
  FileText, FileScan, ArrowRight
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { Link, useLocation } from 'react-router-dom';

const Navbar = () => {
  const [activeMenu, setActiveMenu] = useState<string | null>(null);
  const location = useLocation();

  // Close dropdown whenever the user navigates to a new page
  useEffect(() => {
    setActiveMenu(null);
  }, [location.pathname]);

  return (
    <nav
      className="w-full flex items-center justify-between px-6 md:px-10 py-5 absolute top-0 z-50 bg-background/50 backdrop-blur-md border-b border-white/5"
      onMouseLeave={() => setActiveMenu(null)}
    >
      <div className="flex items-center gap-8 text-sm font-semibold tracking-wide z-50">
        <div
          className="flex items-center gap-1 cursor-pointer hover:text-white/80 transition-colors py-4"
          onMouseEnter={() => setActiveMenu('platform')}
        >
          Platform <ChevronDown size={14} className={`mt-0.5 opacity-70 transition-transform duration-300 ${activeMenu === 'platform' ? 'rotate-180' : ''}`} />
        </div>
        <div
          className="flex items-center gap-1 cursor-pointer hover:text-white/80 transition-colors py-4"
          onMouseEnter={() => setActiveMenu('offerings')}
        >
          Agents <ChevronDown size={14} className={`mt-0.5 opacity-70 transition-transform duration-300 ${activeMenu === 'offerings' ? 'rotate-180' : ''}`} />
        </div>

        <AnimatePresence>
          {activeMenu === 'platform' && (
            <motion.div
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: 10, transition: { duration: 0.1 } }}
              transition={{ duration: 0.2 }}
              className="absolute top-14 left-0 w-[900px] bg-white rounded-2xl shadow-2xl overflow-hidden flex border border-black/10 text-slate-800"
            >
              {/* Left Panel */}
              <div className="w-1/3 bg-slate-50 p-8 flex flex-col justify-between border-r border-slate-100">
                <div>
                  <div className="inline-flex items-center gap-2 px-3 py-1 bg-white border border-slate-200 rounded-full text-xs font-semibold mb-6 shadow-sm">
                    <Zap size={14} className="text-slate-500" /> OmniAgent Platform
                  </div>
                  <h3 className="text-2xl font-semibold mb-3 tracking-tight">Enterprise-grade Agentic Platform</h3>
                  <p className="text-slate-500 text-sm leading-relaxed mb-8">
                    Build prompt-driven workflows and voice-enabled agents that think, act, and hand off safely—LLM-agnostic, scalable, and auditable.
                  </p>
                  <div className="relative rounded-xl overflow-hidden border border-slate-200 shadow-sm mb-6 bg-white aspect-video flex items-center justify-center">
                    <div className="absolute inset-0 bg-gradient-to-tr from-accentTheme/10 to-transparent"></div>
                    <img src="/PlatformMenuImg.png" className="w-full h-full object-cover relative z-10" alt="Platform Preview" />
                  </div>
                </div>
                <Link to="/platform" className="text-[#b72e6a] text-sm font-semibold hover:underline flex items-center gap-1">Explore the platform <ArrowRight size={14} /></Link>
              </div>

              {/* Right Panel Grid */}
              <div className="w-2/3 p-8 bg-white grid grid-cols-2 gap-x-8 gap-y-8">
                <div>
                  <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-6">Capabilities</h4>
                  <div className="space-y-6">
                    <Link to="/platform/capabilities" onClick={() => setActiveMenu(null)} className="flex gap-4 hover:opacity-80 transition-opacity group/cap">
                      <div className="mt-1 w-8 h-8 rounded border border-slate-200 bg-slate-50 flex items-center justify-center shrink-0 group-hover/cap:border-[#853694]/40 group-hover/cap:bg-[#853694]/5 transition-colors">
                        <Workflow size={16} className="text-slate-600 group-hover/cap:text-[#b72e6a] transition-colors" />
                      </div>
                      <div>
                        <h5 className="font-semibold text-sm mb-1 group-hover/cap:text-[#b72e6a] transition-colors">Prompt-Driven Workflows</h5>
                        <p className="text-xs text-slate-500">No-code orchestration over API/WebSocket/WebRTC.</p>
                      </div>
                    </Link>
                    <Link to="/platform/capabilities" onClick={() => setActiveMenu(null)} className="flex gap-4 hover:opacity-80 transition-opacity group/cap">
                      <div className="mt-1 w-8 h-8 rounded border border-slate-200 bg-slate-50 flex items-center justify-center shrink-0 group-hover/cap:border-[#853694]/40 group-hover/cap:bg-[#853694]/5 transition-colors">
                        <Search size={16} className="text-slate-600 group-hover/cap:text-[#b72e6a] transition-colors" />
                      </div>
                      <div>
                        <h5 className="font-semibold text-sm mb-1 group-hover/cap:text-[#b72e6a] transition-colors">Knowledge · Search · Crawl</h5>
                        <p className="text-xs text-slate-500">Bring docs, sites, and systems with citations.</p>
                      </div>
                    </Link>
                    <Link to="/platform/capabilities" onClick={() => setActiveMenu(null)} className="flex gap-4 hover:opacity-80 transition-opacity group/cap">
                      <div className="mt-1 w-8 h-8 rounded border border-slate-200 bg-slate-50 flex items-center justify-center shrink-0 group-hover/cap:border-[#853694]/40 group-hover/cap:bg-[#853694]/5 transition-colors">
                        <Cog size={16} className="text-slate-600 group-hover/cap:text-[#b72e6a] transition-colors" />
                      </div>
                      <div>
                        <h5 className="font-semibold text-sm mb-1 group-hover/cap:text-[#b72e6a] transition-colors">Tool Runtime</h5>
                        <p className="text-xs text-slate-500">Secure action execution with audit trails.</p>
                      </div>
                    </Link>
                  </div>

                  <div className="flex gap-2 mt-6">
                    <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full border border-slate-200 text-[11px] font-medium text-slate-600 bg-slate-50"><Mic size={12} /> Voice</span>
                    <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full border border-slate-200 text-[11px] font-medium text-slate-600 bg-slate-50"><MessageSquare size={12} /> Chat</span>
                    <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full border border-slate-200 text-[11px] font-medium text-slate-600 bg-slate-50"><Mail size={12} /> Email</span>
                  </div>
                </div>

                <div>
                  <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-6">Trust & Scale</h4>
                  <div className="space-y-6">
                    <div className="flex gap-4">
                      <div className="mt-1 w-8 h-8 rounded border border-slate-200 bg-slate-50 flex items-center justify-center shrink-0">
                        <Users size={16} className="text-slate-600" />
                      </div>
                      <div>
                        <h5 className="font-semibold text-sm mb-1">Human + AI</h5>
                        <p className="text-xs text-slate-500">Agent Assist, step-through, and safe handover.</p>
                      </div>
                    </div>
                    <div className="flex gap-4">
                      <div className="mt-1 w-8 h-8 rounded border border-slate-200 bg-slate-50 flex items-center justify-center shrink-0">
                        <Shield size={16} className="text-slate-600" />
                      </div>
                      <div>
                        <h5 className="font-semibold text-sm mb-1">Safe AI & Governance</h5>
                        <p className="text-xs text-slate-500">Security, policies, and observability by design.</p>
                      </div>
                    </div>
                    <div className="flex gap-4">
                      <div className="mt-1 w-8 h-8 rounded border border-slate-200 bg-slate-50 flex items-center justify-center shrink-0">
                        <Network size={16} className="text-slate-600" />
                      </div>
                      <div>
                        <h5 className="font-semibold text-sm mb-1">Integrations</h5>
                        <p className="text-xs text-slate-500">Connect any backend, data source, or tool.</p>
                      </div>
                    </div>
                  </div>

                  <div className="flex flex-wrap gap-2 mt-6">
                    <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full border border-slate-200 text-[11px] font-medium text-slate-600 bg-slate-50"><Cloud size={12} /> SaaS & Private</span>
                    <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full border border-slate-200 text-[11px] font-medium text-slate-600 bg-slate-50"><Server size={12} /> Reliability</span>
                    <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full border border-[#853694]/30 bg-[#853694]/5 text-[11px] font-bold text-[#b72e6a]"><Zap size={12} /> Rapid prototyping</span>
                  </div>
                </div>
              </div>
            </motion.div>
          )}

          {activeMenu === 'offerings' && (
            <motion.div
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: 10, transition: { duration: 0.1 } }}
              transition={{ duration: 0.2 }}
              className="absolute top-14 left-24 w-[950px] bg-white rounded-2xl shadow-2xl overflow-hidden flex border border-black/10 text-slate-800"
            >
              <div className="w-3/5 p-8 bg-white pr-12">
                <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-6">Solutions</h4>
                <div className="space-y-1">
                  {[
                    { id: 'smart-retrieval', icon: MessageSquare, title: 'Customer Support · Smart Retrieval', desc: 'Answer with citations across KBs, websites, and docs—voice & chat.' },
                    { id: 'itsm-automation', icon: Cog, title: 'IT Operations · ITSM Automation', desc: 'Create, track, and update requests with secure tool actions.' },
                    { id: 'l1-support', icon: LifeBuoy, title: 'IT Operations · L1 Support', desc: 'SOP-guided troubleshooting, real-time search, and automation.' },
                    { id: 'hr-helpdesk', icon: Building2, title: 'HR · Conversational Helpdesk', desc: 'Policy answers, letter generation, leave/salary/profile queries.' },
                    { id: 'data-automation', icon: FileText, title: 'Data Automation · Unstructured → Structured', desc: 'Extract entities, emit JSON/SQL; normalize messy inputs.' },
                    { id: 'ocr', icon: FileScan, title: 'Data Automation · OCR', desc: 'Read scans/handwriting/forms and trigger downstream actions.' },
                    { id: 'chat-widget', icon: MessageSquare, title: 'JuviAI Chat Widget', desc: 'Design, preview & embed your chat widget using a single script tag.' }
                  ].map((sol, i) => (
                    <Link
                      key={i}
                      to={`/solutions/${sol.id}`}
                      onClick={() => setActiveMenu(null)}
                      className="group flex items-center justify-between p-3 -mx-3 rounded-xl hover:bg-slate-50 transition-colors cursor-pointer"
                    >
                      <div className="flex gap-4 items-center">
                        <div className="w-8 h-8 rounded-full border border-slate-200 flex items-center justify-center shrink-0 bg-white shadow-sm">
                          <sol.icon size={14} className="text-slate-500" />
                        </div>
                        <div>
                          <h5 className="font-semibold text-sm mb-0.5 group-hover:text-[#b72e6a] transition-colors">{sol.title}</h5>
                          <p className="text-[11px] text-slate-500 leading-tight">{sol.desc}</p>
                        </div>
                      </div>
                      <ArrowRight size={14} className="text-slate-300 group-hover:text-[#b72e6a] transition-colors opacity-0 group-hover:opacity-100 -translate-x-2 group-hover:translate-x-0" />
                    </Link>
                  ))}
                </div>
                <div className="mt-4 pt-4 border-t border-slate-100">
                  <Link
                    to="/agents"
                    onClick={() => setActiveMenu(null)}
                    className="flex items-center gap-1.5 text-xs font-bold text-[#b72e6a] hover:underline"
                  >
                    Browse all agents <ArrowRight size={12} />
                  </Link>
                </div>
              </div>

              <div className="w-2/5 p-8 bg-slate-50 border-l border-slate-100 flex flex-col justify-center gap-4">
                <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-4">Spotlight</h4>

                {[
                  { type: 'Case study', typeColor: 'text-rose-600 bg-rose-50 border-rose-100', title: 'How a fintech deflected 42% tickets with Smart Retrieval', img: '/FinSol.png', to: '/platform/governance-handover' },
                  { type: 'Guide', typeColor: 'text-orange-600 bg-orange-50 border-orange-100', title: 'Designing safe human handover for voice agents', img: '/HumanHandover.png', to: '/platform/governance-handover' },
                  { type: 'New', typeColor: 'text-[#b72e6a] bg-[#853694]/10 border-[#853694]/20', title: 'ROI Calculator—see time & cost savings', img: '/RoiCalc.png', to: '/platform/security-scale' }
                ].map((spot, i) => (
                  <Link key={i} to={spot.to} onClick={() => setActiveMenu(null)} className="bg-white border text-left border-slate-200 rounded-xl p-4 flex gap-4 hover:shadow-md hover:border-[#853694]/50 transition-all cursor-pointer group">
                    <img src={spot.img} alt={spot.type} className="w-16 h-16 object-cover rounded-lg border border-slate-100 shadow-[inset_0_0_10px_rgba(0,0,0,0.02)] shrink-0" />
                    <div className="flex-1">
                      <span className={`inline-flex px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wide border mb-2 ${spot.typeColor}`}>{spot.type}</span>
                      <h5 className="text-sm font-semibold leading-tight mb-2 group-hover:text-[#b72e6a] transition-colors">{spot.title}</h5>
                      <span className="text-[11px] font-semibold text-slate-400 flex items-center gap-1 group-hover:text-[#b72e6a]">Read more <ArrowRight size={10} /></span>
                    </div>
                  </Link>
                ))}
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      <Link to="/" className="flex items-center gap-2 cursor-pointer font-bold text-2xl tracking-tight absolute left-1/2 -translate-x-1/2">
        <img src="/Juvi-logo.png" alt="JuviAI Logo" className="w-8 h-8 object-contain" />
        <span>Juvi<span className="font-light">AI</span></span>
      </Link>

      <div className="flex items-center gap-6 z-50">
        <Link to="/pricing" className="text-sm font-semibold tracking-wide cursor-pointer hover:text-white/80 transition-colors">
          Pricing
        </Link>
      </div>
    </nav>
  );
};

export default Navbar;
