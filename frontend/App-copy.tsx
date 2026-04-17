import { useState } from 'react';
import {
    Play, ChevronDown, CheckCircle2,
    Search, Headphones, Cog, Layers, FileScan, Sparkles,
    Bot, Workflow, Brain, Mic, Shield, Server, Network, ShieldCheck,
    Pencil, Link as LinkIcon, Send, ArrowRight, ArrowLeft,
    MessageSquare, Mail, Users, Cloud, Zap, LifeBuoy, Building2, FileText, Database
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

const AnimatedWaves = () => (
    <div className="fixed inset-0 z-0 pointer-events-none overflow-hidden opacity-60">
        <motion.svg className="absolute w-[200%] h-full top-0 left-0" viewBox="0 0 3200 800" preserveAspectRatio="none"
            animate={{ x: ["0%", "-50%"] }}
            transition={{ duration: 25, repeat: Infinity, ease: "linear" }}
        >
            <path d="M0,400 C400,100 400,700 800,400 S1200,700 1600,400 S2000,100 2400,400 S2800,700 3200,400" fill="none" stroke="rgba(133, 54, 148, 0.4)" strokeWidth="1.5" />
        </motion.svg>
        <motion.svg className="absolute w-[200%] h-full top-0 left-0" viewBox="0 0 3200 800" preserveAspectRatio="none"
            animate={{ x: ["-50%", "0%"] }}
            transition={{ duration: 30, repeat: Infinity, ease: "linear" }}
        >
            <path d="M0,500 C400,300 400,700 800,500 S1200,700 1600,500 S2000,300 2400,500 S2800,700 3200,500" fill="none" stroke="rgba(133, 54, 148, 0.2)" strokeWidth="2" />
        </motion.svg>
        <motion.svg className="absolute w-[200%] h-full top-0 left-0" viewBox="0 0 3200 800" preserveAspectRatio="none"
            animate={{ x: ["0%", "-50%"] }}
            transition={{ duration: 45, repeat: Infinity, ease: "linear" }}
        >
            <path d="M0,300 C400,500 400,100 800,300 S1200,100 1600,300 S2000,500 2400,300 S2800,100 3200,300" fill="none" stroke="rgba(133, 54, 148, 0.3)" strokeWidth="1" />
        </motion.svg>
        <motion.svg className="absolute w-[200%] h-full top-0 left-0" viewBox="0 0 3200 800" preserveAspectRatio="none"
            animate={{ x: ["-50%", "0%"] }}
            transition={{ duration: 35, repeat: Infinity, ease: "linear" }}
        >
            <path d="M0,600 C400,300 400,900 800,600 S1200,900 1600,600 S2000,300 2400,600 S2800,900 3200,600" fill="none" stroke="rgba(133, 54, 148, 0.15)" strokeWidth="3" />
        </motion.svg>
        <motion.svg className="absolute w-[200%] h-full top-0 left-0" viewBox="0 0 3200 800" preserveAspectRatio="none"
            animate={{ x: ["0%", "-50%"] }}
            transition={{ duration: 28, repeat: Infinity, ease: "linear" }}
        >
            <path d="M0,200 C300,400 500,400 800,200 S1300,0 1600,200 S1900,400 2400,200 S2900,0 3200,200" fill="none" stroke="rgba(133, 54, 148, 0.25)" strokeWidth="1.5" />
        </motion.svg>
    </div>
);

const Navbar = () => {
    const [activeMenu, setActiveMenu] = useState<string | null>(null);

    return (
        <nav
            className="w-full flex items-center justify-between px-6 md:px-10 py-5 absolute top-0 z-50 bg-background/50 backdrop-blur-md border-b border-white/5"
            onMouseLeave={() => setActiveMenu(null)}
        >
            <div className="flex gap-8 text-sm font-medium tracking-wide items-center relative z-50">
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
                    Offerings <ChevronDown size={14} className={`mt-0.5 opacity-70 transition-transform duration-300 ${activeMenu === 'offerings' ? 'rotate-180' : ''}`} />
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
                                <a href="#" className="text-accentTheme text-sm font-semibold hover:underline flex items-center gap-1">Explore the platform <ArrowRight size={14} /></a>
                            </div>

                            {/* Right Panel Grid */}
                            <div className="w-2/3 p-8 bg-white grid grid-cols-2 gap-x-8 gap-y-8">
                                <div>
                                    <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-6">Capabilities</h4>
                                    <div className="space-y-6">
                                        <div className="flex gap-4">
                                            <div className="mt-1 w-8 h-8 rounded border border-slate-200 bg-slate-50 flex items-center justify-center shrink-0">
                                                <Workflow size={16} className="text-slate-600" />
                                            </div>
                                            <div>
                                                <h5 className="font-semibold text-sm mb-1">Prompt-Driven Workflows</h5>
                                                <p className="text-xs text-slate-500">No-code orchestration over API/WebSocket/WebRTC.</p>
                                            </div>
                                        </div>
                                        <div className="flex gap-4">
                                            <div className="mt-1 w-8 h-8 rounded border border-slate-200 bg-slate-50 flex items-center justify-center shrink-0">
                                                <Search size={16} className="text-slate-600" />
                                            </div>
                                            <div>
                                                <h5 className="font-semibold text-sm mb-1">Knowledge · Search · Crawl</h5>
                                                <p className="text-xs text-slate-500">Bring docs, sites, and systems with citations.</p>
                                            </div>
                                        </div>
                                        <div className="flex gap-4">
                                            <div className="mt-1 w-8 h-8 rounded border border-slate-200 bg-slate-50 flex items-center justify-center shrink-0">
                                                <Cog size={16} className="text-slate-600" />
                                            </div>
                                            <div>
                                                <h5 className="font-semibold text-sm mb-1">Tool Runtime</h5>
                                                <p className="text-xs text-slate-500">Secure action execution with audit trails.</p>
                                            </div>
                                        </div>
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
                                        <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full border border-accentTheme/30 bg-accentTheme/5 text-[11px] font-bold text-accentTheme"><Zap size={12} /> Rapid prototyping</span>
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
                                        { icon: MessageSquare, title: 'Customer Support · Smart Retrieval', desc: 'Answer with citations across KBs, websites, and docs—voice & chat.' },
                                        { icon: Cog, title: 'IT Operations · ITSM Automation', desc: 'Create, track, and update requests with secure tool actions.' },
                                        { icon: LifeBuoy, title: 'IT Operations · L1 Support', desc: 'SOP-guided troubleshooting, real-time search, and automation.' },
                                        { icon: Building2, title: 'HR · Conversational Helpdesk', desc: 'Policy answers, letter generation, leave/salary/profile queries.' },
                                        { icon: FileText, title: 'Data Automation · Unstructured → Structured', desc: 'Extract entities, emit JSON/SQL; normalize messy inputs.' },
                                        { icon: FileScan, title: 'Data Automation · OCR', desc: 'Read scans/handwriting/forms and trigger downstream actions.' },
                                        { icon: MessageSquare, title: 'JuviAI Chat Widget', desc: 'Design, preview & embed your chat widget using a single script tag.' }
                                    ].map((sol, i) => (
                                        <div key={i} className="group flex items-center justify-between p-3 -mx-3 rounded-xl hover:bg-slate-50 transition-colors cursor-pointer">
                                            <div className="flex gap-4 items-center">
                                                <div className="w-8 h-8 rounded-full border border-slate-200 flex items-center justify-center shrink-0 bg-white shadow-sm">
                                                    <sol.icon size={14} className="text-slate-500" />
                                                </div>
                                                <div>
                                                    <h5 className="font-semibold text-sm mb-0.5 group-hover:text-accentTheme transition-colors">{sol.title}</h5>
                                                    <p className="text-[11px] text-slate-500 leading-tight">{sol.desc}</p>
                                                </div>
                                            </div>
                                            <ArrowRight size={14} className="text-slate-300 group-hover:text-accentTheme transition-colors opacity-0 group-hover:opacity-100 -translate-x-2 group-hover:translate-x-0" />
                                        </div>
                                    ))}
                                </div>
                            </div>

                            <div className="w-2/5 p-8 bg-slate-50 border-l border-slate-100 flex flex-col justify-center gap-4">
                                <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-4">Spotlight</h4>

                                {[
                                    { type: 'Case study', typeColor: 'text-rose-600 bg-rose-50 border-rose-100', title: 'How a fintech deflected 42% tickets with Smart Retrieval', img: '/FinSol.png' },
                                    { type: 'Guide', typeColor: 'text-orange-600 bg-orange-50 border-orange-100', title: 'Designing safe human handover for voice agents', img: '/HumanHandover.png' },
                                    { type: 'New', typeColor: 'text-accentTheme bg-accentTheme/10 border-accentTheme/20', title: 'ROI Calculator—see time & cost savings', img: '/RoiCalc.png' }
                                ].map((spot, i) => (
                                    <div key={i} className="bg-white border text-left border-slate-200 rounded-xl p-4 flex gap-4 hover:shadow-md hover:border-accentTheme/50 transition-all cursor-pointer group">
                                        <img src={spot.img} alt={spot.type} className="w-16 h-16 object-cover rounded-lg border border-slate-100 shadow-[inset_0_0_10px_rgba(0,0,0,0.02)] shrink-0" />
                                        <div className="flex-1">
                                            <span className={`inline-flex px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wide border mb-2 ${spot.typeColor}`}>{spot.type}</span>
                                            <h5 className="text-sm font-semibold leading-tight mb-2 group-hover:text-accentTheme transition-colors">{spot.title}</h5>
                                            <span className="text-[11px] font-semibold text-slate-400 flex items-center gap-1 group-hover:text-accentTheme">Read more <ArrowRight size={10} /></span>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </motion.div>
                    )}
                </AnimatePresence>
            </div>

            <div className="flex items-center gap-2 cursor-pointer font-bold text-2xl tracking-tight absolute left-1/2 -translate-x-1/2">
                <img src="/juviai-logo.png" alt="JuviAI Logo" className="w-8 h-8 object-contain" />
                <span>JuviAI<span className="font-light">AIOps</span></span>
            </div>

            <div className="flex items-center gap-6 z-50">
                <div className="text-sm font-semibold tracking-wide cursor-pointer hover:text-white/80 transition-colors">
                    Pricing
                </div>
            </div>
        </nav>
    );
};



const HeroSection = () => {
    const [activeAvatar, setActiveAvatar] = useState('Lyra');
    const [selectedLang, setSelectedLang] = useState('English (US)');
    const avatars = [
        { name: 'Lyra', title: 'Calm · Clear · Intelligent', desc: 'Professional · Reassuring', tags: ['Tutorials', 'Knowledge-bases', 'Healthcare', 'Education'] },
        { name: 'Orin', title: 'Empathetic · Warm', desc: 'Friendly · Conversational', tags: ['Support', 'HR', 'Onboarding'] },
        { name: 'Nico', title: 'Direct · Concise', desc: 'Technical · Precise', tags: ['DevOps', 'ITSM', 'Engineering'] },
        { name: 'Zia', title: 'Energetic · Bright', desc: 'Engaging · Upbeat', tags: ['Sales', 'Marketing', 'Events'] },
        { name: 'Jax', title: 'Authoritative · Calm', desc: 'Secure · Firm', tags: ['Security', 'Finance', 'Compliance'] }
    ];

    const currentAvatarInfo = avatars.find(a => a.name === activeAvatar);

    return (
        <section className="relative flex-1 flex items-center justify-between px-6 md:px-16 pt-32 pb-20 relative z-10 max-w-[1600px] mx-auto w-full min-h-screen">
            {/* Left Side Content */}
            <div className="flex-1 max-w-[650px] pr-10">
                <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6 }}>
                    <p className="text-xs font-bold tracking-[0.2em] text-white/50 mb-4 uppercase font-outfit">The OmniAgent Platform</p>
                    <h1 className="text-5xl font-outfit font-extrabold leading-[1.1] tracking-tight mb-6">
                        World's first <br />
                        <span className="text-accentTheme drop-shadow-[0_0_15px_rgba(133,54,148,0.4)]">PromptCode - Full Stack Agentic</span> Automation Platform
                    </h1>
                    <p className="text-lg text-white/70 leading-relaxed mb-8 max-w-[580px]">
                        Use Agents to build Agents with prompts, not blocks. Design governed workflows, connect secure tools, and deploy to chat, voice, and email in minutes. Enterprise-grade trust with audit trails and human-in-the-loop.
                    </p>

                    <div className="flex flex-wrap gap-3 mb-10">
                        {["No-code workflows", "LLM-agnostic (15+)", "Secure tool access", "Audit & governance", "Voice · Chat · Email"].map(badge => (
                            <div key={badge} className="px-4 py-1.5 rounded-full border border-white/10 bg-white/5 text-xs text-white/80 font-medium">{badge}</div>
                        ))}
                    </div>

                    <button className="bg-accentTheme hover:bg-[#6a2b77] text-white font-bold py-3 px-8 rounded-full transition-all shadow-[0_0_20px_rgba(133,54,148,0.3)] hover:shadow-[0_0_30px_rgba(133,54,148,0.5)] transform hover:scale-105 active:scale-95">
                        Join the waitlist
                    </button>
                </motion.div>
            </div>

            {/* Right Side Glass UI */}
            <div className="flex-1 flex justify-center items-center">
                <motion.div initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} transition={{ duration: 0.8, delay: 0.2 }} className="relative flex flex-col items-center w-full max-w-[450px]">
                    <img src="/juviai-logo.png" alt="JuviAI Logo" className="w-12 h-12 object-contain mb-2 backdrop-blur-sm" />
                    <div className="text-[11px] font-bold tracking-[0.2em] text-white/40 mb-2 uppercase">Voice Session</div>
                    <h2 className="text-3xl font-semibold mb-1">Speak with Lyra</h2>
                    <p className="text-white/50 text-sm mb-4">English (United States)</p>

                    <div className="flex items-center gap-2 bg-white/5 border border-white/10 px-3 py-1 rounded-full mb-12">
                        <CheckCircle2 size={12} className="text-accentTheme fill-accentTheme/20" />
                        <span className="text-xs text-white/70 font-medium">Ready</span>
                    </div>

                    {/* Play Button */}
                    <div className="relative mb-16">
                        <div className="absolute inset-0 rounded-full border border-white/10 animate-ping opacity-20 scale-150"></div>
                        <div className="absolute inset-0 rounded-full border border-accentTheme/40 animate-pulse opacity-30 scale-125"></div>
                        <button className="relative w-16 h-16 bg-white/10 border border-white/20 text-white rounded-full flex items-center justify-center backdrop-blur-md shadow-2xl hover:bg-white/20 hover:scale-110 transition-all duration-300">
                            <Play fill="currentColor" size={24} className="translate-x-0.5" />
                        </button>
                    </div>

                    {/* Avatars Row */}
                    <div className="relative mb-6">
                        <AnimatePresence mode="wait">
                            {currentAvatarInfo && (
                                <motion.div
                                    key={currentAvatarInfo.name}
                                    initial={{ opacity: 0, scale: 0.9, y: 10 }}
                                    animate={{ opacity: 1, scale: 1, y: 0 }}
                                    exit={{ opacity: 0, scale: 0.9, y: 10 }}
                                    className="absolute bottom-[100%] mb-4 left-1/2 -translate-x-1/2 w-80 bg-background/80 backdrop-blur-xl border border-white/10 p-4 rounded-xl shadow-2xl z-20 pointer-events-none"
                                >
                                    <div className="text-white font-semibold mb-1">{currentAvatarInfo.name}</div>
                                    <div className="text-white/80 text-xs mb-1">{currentAvatarInfo.title}</div>
                                    <div className="text-white/50 text-[11px] mb-3">{currentAvatarInfo.desc}</div>
                                    <div className="flex flex-wrap gap-1.5">
                                        {currentAvatarInfo.tags.map(tag => (
                                            <span key={tag} className="text-[10px] px-2 py-0.5 rounded bg-white/10 text-white/70">
                                                {tag}
                                            </span>
                                        ))}
                                    </div>
                                    {/* Tooltip triangle */}
                                    <div className="absolute -bottom-2 left-1/2 -translate-x-1/2 w-4 h-4 rotate-45 bg-background border-b border-r border-white/10 -z-10"></div>
                                </motion.div>
                            )}
                        </AnimatePresence>

                        <div className="flex gap-4 items-center justify-center">
                            {avatars.map((avatar) => {
                                const active = avatar.name === activeAvatar;
                                return (
                                    <div key={avatar.name}
                                        className="flex flex-col items-center gap-2 relative group"
                                        onMouseEnter={() => setActiveAvatar(avatar.name)}
                                    >
                                        <div className={`w-10 h-10 rounded-full border-2 overflow-hidden transition-all duration-300 ${active ? 'border-accentTheme scale-110 shadow-[0_0_15px_rgba(133,54,148,0.5)]' : 'border-transparent opacity-50 hover:opacity-100 cursor-pointer'}`}>
                                            <img src={`/${avatar.name}.png`} alt={avatar.name} className="w-full h-full object-cover" />
                                        </div>
                                        <span className={`text-[10px] font-medium transition-colors ${active ? 'text-white' : 'text-white/40'}`}>{avatar.name}</span>
                                    </div>
                                );
                            })}
                        </div>
                    </div>

                    <div className="relative mt-2">
                        <div
                            className="text-sm text-white/40 flex items-center gap-1 cursor-pointer hover:text-white/70 transition-colors"
                            onClick={() => setActiveAvatar(activeAvatar === 'lang' ? 'Lyra' : 'lang')}
                        >
                            {selectedLang} <ChevronDown size={14} className={`transition-transform duration-300 ${activeAvatar === 'lang' ? 'rotate-180' : ''}`} />
                        </div>

                        <AnimatePresence>
                            {activeAvatar === 'lang' && (
                                <motion.div
                                    initial={{ opacity: 0, y: 10, scale: 0.95 }}
                                    animate={{ opacity: 1, y: 0, scale: 1 }}
                                    exit={{ opacity: 0, y: 10, scale: 0.95 }}
                                    transition={{ duration: 0.15 }}
                                    className="absolute top-full right-0 mt-4 w-48 max-h-64 overflow-y-auto bg-white rounded-lg shadow-xl shadow-black/50 border border-white/10 z-50 py-2 custom-scrollbar"
                                >
                                    {[
                                        'Arabic', 'Bengali', 'Chinese (Mandarin)', 'Dutch',
                                        'English (Australia)', 'English (India)', 'English (UK)', 'English (US)'
                                    ].map(lang => (
                                        <div
                                            key={lang}
                                            onClick={() => { setSelectedLang(lang); setActiveAvatar(''); }}
                                            className={`px-4 py-2 text-xs cursor-pointer transition-colors ${lang === selectedLang ? 'bg-accentTheme/10 text-accentTheme font-semibold' : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'}`}
                                        >
                                            {lang}
                                        </div>
                                    ))}
                                </motion.div>
                            )}
                        </AnimatePresence>
                    </div>
                </motion.div>
            </div>
        </section>
    );
};

const LogoMarquee = () => {
    const logos = [
        { name: 'AimKart', file: 'aimkart.png' },
        { name: 'Talent Synergy', file: 'talentSynergy.png' },
        { name: 'Network Science', file: 'networkScience.png' },
        { name: 'BadgeFree', file: 'badgeFree.png' },
        { name: 'FiSense', file: 'fisense.png' },
        { name: 'Genus', file: 'genus.png' },
        { name: 'ngageTalent', file: 'ngageTalent.svg' }
    ];
    return (
        <section className="relative overflow-hidden py-8 border-y border-white/5">
            <p className="text-center text-xs uppercase tracking-widest text-gray-400 mb-6">Trusted by forward-thinking teams</p>
            <div className="relative flex overflow-hidden group" style={{ maskImage: 'linear-gradient(to right, transparent, black 10%, black 90%, transparent)' }}>
                <div className="flex w-max animate-wave-slow items-center whitespace-nowrap">
                    {[...logos, ...logos, ...logos].map((logo, i) => (
                        <div key={i} className="flex items-center gap-3 px-8 text-white/50 hover:text-white/90 transition-colors cursor-pointer">
                            <img src={`/${logo.file}`} alt={logo.name} className="h-8 object-contain filter invert opacity-50 hover:opacity-100 transition-opacity" />
                        </div>
                    ))}
                </div>
            </div>
        </section>
    );
};

const DataPacket = ({ d, delay, duration = 3 }: { d: string, delay: number, duration?: number }) => (
    <circle r="3" fill="#853694" filter="drop-shadow(0 0 4px #853694)">
        <animateMotion dur={`${duration}s`} repeatCount="indefinite" begin={`${delay}s`} path={d} />
    </circle>
);

const NetworkSection = () => {
    const staticNodes = [
        { id: "appstore", x: -240, y: -100, size: 56 },
        { id: "whatsapp", x: -240, y: 100, size: 56 },
        { id: "messenger", x: -320, y: 0, size: 48 },
        { id: "handshake", x: -140, y: 0, size: 48 },
        { id: "android", x: 240, y: -100, size: 56 },
        { id: "userchat", x: 240, y: 100, size: 56 },
        { id: "telegram", x: 320, y: 0, size: 48 },
        { id: "crm", x: 140, y: 0, size: 48 },
    ];

    return (
        <section className="py-32 relative overflow-hidden flex flex-col items-center justify-center min-h-[600px] bg-[#050508]">
            <div className="absolute inset-0 z-0 flex items-center justify-center pointer-events-none">
                <svg className="absolute w-[800px] h-[400px] overflow-visible" viewBox="-400 -200 800 400">
                    {/* Faint Green Background Grid Lines */}
                    <g stroke="rgba(133, 54, 148, 0.15)" strokeWidth="1" fill="none">
                        <line x1="-240" y1="-400" x2="-240" y2="400" />
                        <line x1="-140" y1="-400" x2="-140" y2="400" />
                        <line x1="140" y1="-400" x2="140" y2="400" />
                        <line x1="240" y1="-400" x2="240" y2="400" />
                        <line x1="-800" y1="-100" x2="800" y2="-100" />
                        <line x1="-800" y1="0" x2="800" y2="0" />
                        <line x1="-800" y1="100" x2="800" y2="100" />
                    </g>

                    {/* Hourglass Connection Lines */}
                    <g stroke="rgba(133, 54, 148, 0.3)" strokeWidth="1.5" strokeDasharray="4 4" fill="none">
                        <path d="M 0,0 L -140,0 L -320,0" />
                        <path d="M -140,0 L -240,-100" />
                        <path d="M -140,0 L -240,100" />
                        <path d="M 0,0 L 140,0 L 320,0" />
                        <path d="M 140,0 L 240,-100" />
                        <path d="M 140,0 L 240,100" />
                    </g>

                    {/* Central Hub Animated Rings */}
                    <motion.circle cx="0" cy="0" r="45" stroke="rgba(133, 54, 148, 0.8)" strokeWidth="2" fill="none" strokeDasharray="4 12"
                        animate={{ rotate: 360 }} transition={{ duration: 20, repeat: Infinity, ease: "linear" }} style={{ originX: '0px', originY: '0px' }}
                    />
                    <motion.circle cx="0" cy="0" r="65" stroke="rgba(133, 54, 148, 0.2)" strokeWidth="1.5" fill="none" strokeDasharray="80 160"
                        animate={{ rotate: -360 }} transition={{ duration: 15, repeat: Infinity, ease: "linear" }} style={{ originX: '0px', originY: '0px' }}
                    />

                    {/* Flying Data Packets (Dots) */}
                    <DataPacket d="M -240,-100 L -140,0 L 0,0" delay={0} duration={2.5} />
                    <DataPacket d="M 0,0 L -140,0 L -240,100" delay={1.5} duration={2.2} />
                    <DataPacket d="M -320,0 L -140,0 L 0,0" delay={0.5} duration={3} />

                    <DataPacket d="M 0,0 L 140,0 L 240,-100" delay={0.2} duration={2.8} />
                    <DataPacket d="M 240,100 L 140,0 L 0,0" delay={1.2} duration={2.4} />
                    <DataPacket d="M 0,0 L 140,0 L 320,0" delay={0.8} duration={3.2} />
                </svg>
            </div>

            <div className="relative z-10 w-full max-w-[800px] h-[400px]">
                {/* Center Hub Overlay Image */}
                <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[72px] h-[72px] rounded-full border border-accentTheme bg-[#0a0a0f] flex items-center justify-center shadow-[0_0_30px_rgba(133,54,148,0.4)]">
                    <img src="/icons/center-logo.png" alt="Center Hub" className="w-[60%] h-[60%] object-contain drop-shadow-[0_0_10px_rgba(133,54,148,0.6)]" onError={(e) => { e.currentTarget.src = '/juviai-logo.png' }} />
                </div>

                {/* Distributed Nodes */}
                {staticNodes.map(n => (
                    <div
                        key={n.id}
                        className="absolute rounded-full border border-white/10 bg-[#12121a] flex items-center justify-center shadow-lg hover:scale-110 hover:border-accentTheme/50 hover:shadow-[0_0_15px_rgba(133,54,148,0.3)] transition-all cursor-pointer overflow-hidden group"
                        style={{
                            width: n.size, height: n.size,
                            left: `calc(50% + ${n.x}px)`, top: `calc(50% + ${n.y}px)`,
                            transform: 'translate(-50%, -50%)'
                        }}
                    >
                        <img
                            src={`/icons/${n.id}.png`}
                            alt={n.id}
                            className="w-1/2 h-1/2 object-contain group-hover:scale-110 transition-transform"
                            onError={(e) => {
                                (e.target as HTMLImageElement).style.opacity = '0';
                            }}
                        />
                        {/* Fallback solid shape if icon hasn't been uploaded yet */}
                        <div className="absolute inset-0 flex flex-col items-center justify-center -z-10 bg-white/5 opacity-50">
                            <span className="text-[9px] uppercase font-bold text-white/40 tracking-wider mix-blend-overlay">{n.id.substring(0, 3)}</span>
                        </div>
                    </div>
                ))}
            </div>
        </section>
    );
};

const UseCaseSection = () => {
    const [currentIndex, setCurrentIndex] = useState(0);
    const cases = [
        { title: "Smart Information Retrieval Agent", icon: Search, desc: "Search internal wikis, SharePoint, and the web to deliver cited answers over voice or chat.", tags: ["Structured + unstructured data", "Reference citations", "Multi-source retrieval"] },
        { title: "Automated Service Management", icon: Headphones, desc: "Create, track, and update service requests hands-free; streamline end-to-end ITSM flows.", tags: ["Create/Track/Update", "Chat or voice", "Works with any ITSM backend"] },
        { title: "Intelligent L1 IT Support", icon: Cog, desc: "Resolve common tech issues using SOPs, live web search, and secure actions across systems.", tags: ["SOP-guided flows", "Routine task automation", "Real-time search"] },
        { title: "Conversational HR Helpdesk", icon: Building2, desc: "Answer policy questions, generate letters, and access leave/salary/profile info instantly.", tags: ["Policy QA", "Document generation", "HRIS lookups"] },
        { title: "Unstructured → Structured Data", icon: Database, desc: "Convert resumes, forms, and messages into JSON/SQL tables for analytics and workflows.", tags: ["Ingest documents", "Entity extraction", "JSON / SQL outputs"] },
        { title: "Image & Document OCR Automation", icon: FileScan, desc: "Read scans and handwritten notes to auto-update backend systems and reduce manual toil.", tags: ["OCR from images", "Form understanding", "Action on visual data"] },
        { title: "Autonomous Research & Analysis", icon: Search, desc: "Delegate deep web + proprietary research to agents that summarize and draft reports.", tags: ["Connect to web + DBs", "Auto-generated reports", "Concise summaries"] }
    ];

    const handleNext = () => setCurrentIndex((prev) => (prev + 1) % cases.length);
    const handlePrev = () => setCurrentIndex((prev) => (prev - 1 + cases.length) % cases.length);

    return (
        <section className="py-24 px-6 md:px-16 w-full text-center z-10 relative overflow-hidden">
            <div className="text-xs tracking-widest uppercase text-gray-400 mb-2 font-outfit">What you can launch in weeks</div>
            <h2 className="text-3xl md:text-5xl font-outfit font-semibold leading-tight text-white mb-16">
                Outcome-driven agents you can <span className="text-accentTheme">ship fast</span>
            </h2>

            <div className="relative max-w-[900px] mx-auto mb-12">
                {/* Carousel view tracking active card */}
                <div className="relative h-[420px] flex items-center justify-center">
                    {cases.map((c, i) => {
                        const diff = (i - currentIndex + cases.length) % cases.length;
                        let offset = diff;
                        if (diff > cases.length / 2) offset = diff - cases.length;

                        const isCenter = offset === 0;
                        const scale = isCenter ? 1 : 0.95;
                        const zIndex = isCenter ? 10 : 5 - Math.abs(offset);
                        const xTranslate = offset * 85 + '%';
                        const opacity = isCenter ? 1 : 0.4;

                        return (
                            <motion.div
                                key={i}
                                initial={false}
                                animate={{ scale, x: xTranslate, zIndex, opacity }}
                                transition={{ duration: 0.5, ease: "easeOut" }}
                                className={`absolute w-full max-w-[550px] rounded-3xl border text-center transition-all bg-[#0a0a0f] p-10 ${isCenter ? 'border-white/10 shadow-2xl' : 'border-white/5 shadow-none'}`}
                            >
                                <div className="flex items-center justify-center gap-4 mb-6">
                                    <div className="w-12 h-12 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center shadow-inner">
                                        <c.icon size={20} className="text-white/90" />
                                    </div>
                                    <h3 className="text-[22px] font-semibold text-white tracking-tight">{c.title}</h3>
                                </div>

                                <p className="text-[15px] text-gray-400 mb-8 max-w-[400px] mx-auto leading-relaxed">{c.desc}</p>

                                <div className="flex flex-wrap justify-center gap-3 mb-10">
                                    {c.tags.map(t => (
                                        <span key={t} className="px-4 py-1.5 rounded-full border border-white/10 bg-white/5 text-[12px] text-white/70 font-medium tracking-wide">
                                            {t}
                                        </span>
                                    ))}
                                </div>

                                <button className="flex justify-center items-center gap-2 text-[13px] text-white/70 hover:text-white transition-colors mx-auto font-semibold group">
                                    Explore this use case <ArrowRight size={14} className="group-hover:translate-x-1 transition-transform" />
                                </button>
                            </motion.div>
                        )
                    })}

                    {/* Floating Carousel Buttons inside the container width */}
                    <button className="absolute left-4 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-[#0a0a0f] border border-white/10 text-white flex items-center justify-center hover:bg-white/10 z-20 transition-all shadow-lg" onClick={handlePrev}>
                        <ArrowLeft size={16} />
                    </button>
                    <button className="absolute right-4 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-[#0a0a0f] border border-white/10 text-white flex items-center justify-center hover:bg-white/10 z-20 transition-all shadow-lg" onClick={handleNext}>
                        <ArrowRight size={16} />
                    </button>
                </div>

                {/* Carousel Dots */}
                <div className="flex justify-center gap-2 mt-8 items-center">
                    {cases.map((_, i) => (
                        <div key={i} onClick={() => setCurrentIndex(i)} className={`h-[2px] cursor-pointer transition-all duration-300 ${i === currentIndex ? 'w-8 bg-white' : 'w-6 bg-white/20 hover:bg-white/40'}`}></div>
                    ))}
                </div>
            </div>
        </section>
    );
};

const TripleTradeoffSection = () => (
    <section className="py-24 px-6 md:px-16 max-w-[1200px] mx-auto text-center relative z-10">
        <div className="text-xs tracking-widest uppercase text-gray-400 mb-2 font-outfit">The Triple Tradeoff – Solved</div>
        <h2 className="text-3xl md:text-5xl font-outfit font-semibold leading-tight text-white mb-12">
            One platform that <span className="text-accentTheme">thinks, talks, and acts</span>
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8 text-left">
            {[
                { title: "CHATBOTS", subtitle: "Talk, can't do", desc: "Great at conversation, limited at taking action across systems.", icon: Bot },
                { title: "WORKFLOWS", subtitle: "Do, can't think", desc: "Reliable execution, but brittle when the task needs reasoning or context.", icon: Workflow },
                { title: "AI AGENTS", subtitle: "Think, can't act alone", desc: "Reason over complex goals but struggle to securely call enterprise tools by themselves.", icon: Brain },
            ].map((item, i) => (
                <div key={i} className="relative rounded-2xl border border-white/10 bg-white/5 p-6 overflow-hidden hover:border-white/20 transition-all">
                    <item.icon size={120} className="absolute -right-6 -bottom-6 text-white/5" />
                    <div className="text-xs uppercase tracking-wider text-gray-400">{item.title}</div>
                    <div className="text-base text-white font-medium mb-4">{item.subtitle}</div>
                    <p className="text-sm text-gray-400 relative z-10 w-4/5">{item.desc}</p>
                </div>
            ))}
        </div>

        {/* Combined solution card */}
        <div className="rounded-2xl border border-accentTheme/30 bg-accentTheme/5 p-8 text-center relative overflow-hidden backdrop-blur-sm shadow-[0_0_30px_rgba(133,54,148,0.1)]">
            <img src="/juviai-logo.png" alt="Logo" className="h-8 mx-auto mb-4 opacity-80" />
            <p className="text-white/80 max-w-2xl mx-auto mb-6">A voice-enabled, prompt-driven agent platform that unifies reasoning, conversation, and secure tool orchestration into governed workflows.</p>
            <div className="flex flex-wrap justify-center gap-3">
                {[{ icon: Mic, t: "Voice & Chat" }, { icon: Shield, t: "Governed Access" }, { icon: Server, t: "SaaS / Private Cloud" }, { icon: Network, t: "Integrates with your APIs" }].map(b => (
                    <span key={b.t} className="inline-flex items-center gap-1.5 rounded-full border border-white/10 bg-white/5 px-3 py-1.5 text-xs text-gray-300">
                        <b.icon size={14} className="text-accentTheme" /> {b.t}
                    </span>
                ))}
            </div>
        </div>
    </section>
);

const StepsSection = () => (
    <section className="py-24 px-6 md:px-16 max-w-[1200px] mx-auto grid grid-cols-1 md:grid-cols-2 gap-16 items-center relative z-10">
        <motion.div
            className="relative rounded-[2rem] border border-white/5 bg-[#0a0a0f]/50 backdrop-blur-md p-8 md:p-12 aspect-square md:aspect-[4/3] flex flex-col items-center justify-center isolate group w-full shadow-[0_0_40px_rgba(133,54,148,0.05)] hover:border-white/10 transition-colors"
            animate={{ y: [0, -12, 0] }}
            transition={{ duration: 6, repeat: Infinity, ease: "easeInOut" }}
        >
            <div className="absolute top-6 left-8 text-[11px] font-semibold tracking-widest uppercase text-white/40 z-20 mix-blend-difference">OmniAgent Flow</div>

            {/* Seamlessly blended Image graphic */}
            <img
                src="/OmniAgent.png"
                alt="OmniAgent Flow Dashboard"
                className="w-full h-full object-contain opacity-60 group-hover:opacity-95 drop-shadow-[0_0_15px_rgba(133,54,148,0.1)] group-hover:drop-shadow-[0_0_30px_rgba(133,54,148,0.25)] transition-all duration-700 mt-2"
            />

            {/* Breathing orb accent behind image */}
            <div className="absolute right-1/4 top-1/2 -translate-y-1/2 w-32 h-32 rounded-full bg-accentTheme/10 blur-[60px] -z-10 group-hover:bg-accentTheme/20 transition-colors duration-700 animate-pulse"></div>
        </motion.div>

        <div className="relative">
            {/* Watermark logo */}
            <div className="absolute top-1/2 right-10 -translate-y-1/2 opacity-[0.03] pointer-events-none scale-150">
                <img src="/juviai-logo.png" alt="watermark" className="w-[400px] h-[400px] object-cover filter brightness-200 grayscale" />
            </div>

            <div className="space-y-10 relative z-10 w-full md:pl-8">
                {[
                    { title: "Design in plain English", desc: "Describe outcomes and guardrails — we stitch the flow.", icon: Pencil },
                    { title: "Bind secure tools", desc: "Attach APIs and actions with scoped, governed access.", icon: LinkIcon },
                    { title: "Deploy to channels", desc: "Ship to web, voice, or email — same flow, new surfaces.", icon: Send },
                    { title: "Govern & audit", desc: "Every action is logged with context, approvals, and trace.", icon: ShieldCheck },
                ].map((step, i) => (
                    <div key={i} className="relative">
                        <div className="absolute -left-10 md:-left-12 top-1 w-7 h-7 rounded-full bg-accentTheme/10 border-2 border-accentTheme flex items-center justify-center shadow-[0_0_15px_rgba(133,54,148,0.4)]">
                            <step.icon size={12} className="text-accentTheme" />
                        </div>
                        <h3 className="text-lg font-semibold text-white mb-2">{step.title}</h3>
                        <p className="text-[15px] text-gray-400 max-w-md">{step.desc}</p>
                    </div>
                ))}
            </div>
        </div>
    </section>
);

const FeaturesGrid = () => (
    <section className="py-24 px-6 md:px-16 max-w-[1400px] mx-auto text-center relative z-10">
        <div className="text-xs tracking-widest uppercase text-gray-400 mb-2">Why JuviAI</div>
        <h2 className="text-3xl md:text-5xl font-semibold leading-tight text-white mb-12">Built for business teams, trusted by engineering</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[
                { title: "Prompt-driven workflows", desc: "Build flows in plain English — no code.", icon: Sparkles },
                { title: "Faster go-to-market", desc: "Launch new use cases in 50% less time.", icon: Layers },
                { title: "Effortless integration", desc: "Connect to your existing APIs & backends.", icon: Network },
                { title: "Multilingual & voice-ready", desc: "Text or natural voice — your choice.", icon: Mic },
                { title: "Flexible deployment", desc: "SaaS or private cloud for compliance.", icon: Shield },
                { title: "Human handover", desc: "Escalate to live agents when needed.", icon: Headphones },
                { title: "Instant knowledge mining", desc: "Fetch answers from internal/external docs.", icon: Search },
                { title: "Rapid prototyping", desc: "Get a tailored demo in < 2 weeks.", icon: Sparkles },
                { title: "Voice + OCR built-in", desc: "No third-party add-ons required.", icon: FileScan },
            ].map((f, i) => (
                <div key={i} className="rounded-2xl border border-white/5 bg-white/5 p-6 text-left hover:bg-white/10 transition-colors">
                    <div className="mb-4 text-accentTheme bg-accentTheme/10 w-max p-2 rounded-lg border border-accentTheme/20">
                        <f.icon size={20} />
                    </div>
                    <h3 className="text-base font-semibold text-white mb-2">{f.title}</h3>
                    <p className="text-sm text-gray-400">{f.desc}</p>
                </div>
            ))}
        </div>
        <div className="mt-16">
            <button className="bg-accentTheme hover:bg-[#6a2b77] text-white font-bold py-3 px-8 rounded-full transition-all shadow-[0_0_20px_rgba(133,54,148,0.3)] hover:scale-105 inline-flex items-center gap-2">
                Get started <ArrowRight size={18} />
            </button>
        </div>
    </section>
);

const Footer = () => (
    <footer className="bg-white py-20 px-6 md:px-16 relative z-10 text-slate-800 border-t border-slate-200">
        <div className="max-w-[1200px] mx-auto flex flex-col md:flex-row justify-between gap-16">
            {/* Brand & Copyright */}
            <div className="flex flex-col gap-6 w-full md:w-1/4">
                <div className="flex items-center gap-3">
                    <img src="/juviai-logo.png" alt="JuviAI" className="h-10 object-contain w-10 filter invert brightness-0" />
                </div>
                <span className="text-slate-500 text-[13px] font-medium">© 2026 JuviAI AIOps - All rights reserved.</span>

                {/* Large subtle watermark text matching screenshot */}
                <div className="absolute bottom-0 left-1/2 -translate-x-1/2 text-[14vw] font-outfit font-extrabold text-slate-50 pointer-events-none leading-none select-none tracking-tighter">
                    JuviAI
                </div>
            </div>

            {/* Links Grid */}
            <div className="flex-1 grid grid-cols-2 md:grid-cols-4 gap-8 md:gap-4 relative z-10">
                <div>
                    <h4 className="font-semibold mb-6 text-sm tracking-wide">Product</h4>
                    <ul className="space-y-4 text-[13px] font-medium text-slate-500">
                        <li><a href="#" className="hover:text-accentTheme transition-colors">Pricing & Plans</a></li>
                    </ul>
                </div>
                <div>
                    <h4 className="font-semibold mb-6 text-sm tracking-wide">Company</h4>
                    <ul className="space-y-4 text-[13px] font-medium text-slate-500">
                        <li><a href="#" className="hover:text-accentTheme transition-colors">About us</a></li>
                        <li><a href="#" className="hover:text-accentTheme transition-colors">Careers</a></li>
                    </ul>
                </div>
                <div>
                    <h4 className="font-semibold mb-6 text-sm tracking-wide">Resources</h4>
                    <ul className="space-y-4 text-[13px] font-medium text-slate-500">
                        <li><a href="#" className="hover:text-accentTheme transition-colors">Terms of service</a></li>
                        <li><a href="#" className="hover:text-accentTheme transition-colors">Cookie Policy</a></li>
                        <li><a href="#" className="hover:text-accentTheme transition-colors">FAQ</a></li>
                        <li><a href="#" className="hover:text-accentTheme transition-colors">Privacy Policy</a></li>
                    </ul>
                </div>
                <div>
                    <h4 className="font-semibold mb-6 text-sm tracking-wide">Social</h4>
                    <div className="flex gap-4 text-accentTheme bg-white items-center">
                        <a href="#" className="hover:opacity-70 transition-opacity" title="X">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><line x1="4" x2="20" y1="4" y2="20" /><line x1="20" x2="4" y1="4" y2="20" /></svg>
                        </a>
                        <a href="#" className="hover:opacity-70 transition-opacity" title="Instagram">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><rect width="20" height="20" x="2" y="2" rx="5" ry="5" /><path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z" /><line x1="17.5" x2="17.51" y1="6.5" y2="6.5" /></svg>
                        </a>
                        <a href="#" className="hover:opacity-70 transition-opacity" title="YouTube">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M2.5 17a24.12 24.12 0 0 1 0-10 2 2 0 0 1 1.4-1.4 49.56 49.56 0 0 1 16.2 0A2 2 0 0 1 21.5 7a24.12 24.12 0 0 1 0 10 2 2 0 0 1-1.4 1.4 49.55 49.55 0 0 1-16.2 0A2 2 0 0 1 2.5 17" /><path d="m10 15 5-3-5-3z" /></svg>
                        </a>
                        <a href="#" className="hover:opacity-70 transition-opacity" title="LinkedIn">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M16 8a6 6 0 0 1 6 6v7h-4v-7a2 2 0 0 0-2-2 2 2 0 0 0-2 2v7h-4v-7a6 6 0 0 1 6-6z" /><rect width="4" height="12" x="2" y="9" /><circle cx="4" cy="4" r="2" /></svg>
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </footer>
);

const App = () => {
    return (
        <div className="min-h-screen relative overflow-hidden bg-background flex flex-col font-sans text-foreground">
            {/* Background Animated Waves (Shared across the whole layout) */}
            <AnimatedWaves />

            <Navbar />
            <HeroSection />
            <LogoMarquee />
            <NetworkSection />
            <UseCaseSection />
            <TripleTradeoffSection />
            <StepsSection />
            <FeaturesGrid />
            <Footer />

            {/* Bottom right decorative icon */}
            <div className="fixed bottom-8 right-8 opacity-20 z-50 pointer-events-none">
                <img src="/juviai-logo.png" alt="Decor" className="w-12 h-12 object-contain" />
            </div>
        </div>
    );
};

export default App;
