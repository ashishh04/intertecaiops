import { useState, useRef, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import {
  Play, CheckCircle2, Search, Headphones, Cog,
  Database, FileScan, Bot, Workflow, Brain,
  Mic, Shield, Network, Pencil, Link as LinkIcon,
  Send, ShieldCheck, Sparkles, Layers, ArrowRight,
  ArrowLeft, Building2, ChevronDown, Server, X, Pause, Volume2
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

const VoiceDemoModal = ({ onClose, avatarName }: { onClose: () => void, avatarName: string }) => {
  const [playing, setPlaying] = useState(true);
  const audioRef = useRef<HTMLAudioElement>(null);

  useEffect(() => {
    if (audioRef.current) {
      if (playing) {
        audioRef.current.play().catch(e => console.error("Audio playback failed", e));
      } else {
        audioRef.current.pause();
      }
    }
  }, [playing]);

  const msgs = [
    { role: 'bot', text: `Hello! I'm ${avatarName}. How can I assist you today?` },
    { role: 'user', text: 'Can you help me reset my password?' },
    { role: 'bot', text: "Of course! I'll guide you through the process. First, please verify your employee ID." },
  ];
  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-6"
      onClick={e => { if (e.target === e.currentTarget) onClose(); }}
    >
      <motion.div
        initial={{ scale: 0.95, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        exit={{ scale: 0.95, opacity: 0 }}
        className="bg-[#0a0a10] border border-white/10 rounded-2xl w-full max-w-md overflow-hidden"
      >
        <audio ref={audioRef} src={`/${avatarName.toLowerCase()}.mp3`} onEnded={() => setPlaying(false)} />
        {/* Header */}
        <div className="flex items-center justify-between px-5 py-4 bg-[#853694]/10 border-b border-white/5">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-full bg-[#853694] flex items-center justify-center shadow-[0_0_12px_rgba(133,54,148,0.6)]">
              <Mic size={14} className="text-white" />
            </div>
            <div>
              <p className="text-sm font-bold text-white">Voice Session · {avatarName}</p>
              <div className="flex items-center gap-1.5">
                <span className="w-1.5 h-1.5 rounded-full bg-purple-400 animate-pulse" />
                <span className="text-[10px] text-purple-400 font-semibold">{playing ? 'Live' : 'Paused'}</span>
              </div>
            </div>
          </div>
          <button onClick={onClose} className="w-7 h-7 rounded-full bg-white/5 flex items-center justify-center hover:bg-white/10">
            <X size={13} className="text-white/50" />
          </button>
        </div>
        {/* Chat transcript */}
        <div className="p-5 space-y-3">
          {msgs.map((m, i) => (
            <motion.div
              key={i}
              initial={{ opacity: 0, y: 6 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: i * 0.3 }}
              className={`flex ${m.role === 'user' ? 'justify-end' : 'justify-start'}`}
            >
              <div className={`max-w-[80%] px-4 py-2.5 rounded-2xl text-sm ${
                m.role === 'user' ? 'bg-[#853694]/25 border border-[#853694]/30 text-white' : 'bg-white/[0.05] border border-white/8 text-white/70'
              }`}>{m.text}</div>
            </motion.div>
          ))}
          {/* Animated waveform */}
          {playing && (
            <div className="flex items-center justify-center gap-1 pt-2">
              {[...Array(14)].map((_, i) => (
                <div
                  key={i}
                  className="w-1 bg-[#853694] rounded-full animate-pulse"
                  style={{ height: `${8 + Math.sin(i * 0.8) * 10 + 8}px`, animationDelay: `${i * 0.07}s` }}
                />
              ))}
            </div>
          )}
        </div>
        {/* Controls */}
        <div className="flex items-center justify-center gap-4 px-5 py-4 border-t border-white/5">
          <button
            onClick={() => setPlaying(p => !p)}
            className="flex items-center gap-2 px-5 py-2 rounded-full bg-[#853694] hover:bg-[#6a2b77] text-white text-xs font-bold transition-all"
          >
            {playing ? <><Pause size={12} /> Pause</> : <><Play size={12} /> Resume</>}
          </button>
          <button className="flex items-center gap-2 px-5 py-2 rounded-full bg-white/5 border border-white/10 text-white/60 text-xs font-bold hover:bg-white/10 transition-all">
            <Volume2 size={12} /> Mute
          </button>
        </div>
      </motion.div>
    </motion.div>
  );
};

const HeroSection = () => {
  const [activeAvatar, setActiveAvatar] = useState('');
  const [selectedAvatar, setSelectedAvatar] = useState('Lyra');
  const [showVoiceDemo, setShowVoiceDemo] = useState(false);
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
    <section className="relative flex-1 flex items-center justify-between px-6 md:px-16 pt-32 pb-20 z-10 max-w-[1600px] mx-auto w-full min-h-screen">
      <div className="flex-1 max-w-[650px] pr-10">
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6 }}>
          <p className="text-xs font-bold tracking-[0.2em] text-white/50 mb-4 uppercase font-outfit">The OmniAgent Platform</p>
          <h1 className="text-5xl font-outfit font-extrabold leading-[1.1] tracking-tight mb-6">
            World's first <br />
            <span className="text-[#b72e6a] drop-shadow-[0_0_15px_rgba(133,54,148,0.4)]">PromptCode - Full Stack Agentic</span> Automation Platform
          </h1>
          <p className="text-lg text-white/70 leading-relaxed mb-8 max-w-[580px]">
            Use Agents to build Agents with prompts, not blocks. Design governed workflows, connect secure tools, and deploy to chat, voice, and email in minutes. Enterprise-grade trust with audit trails and human-in-the-loop.
          </p>

          <div className="flex flex-wrap gap-3 mb-10">
            {["No-code workflows", "LLM-agnostic (15+)", "Secure tool access", "Audit & governance", "Voice · Chat · Email"].map(badge => (
              <div key={badge} className="px-4 py-1.5 rounded-full border border-white/10 bg-white/5 text-xs text-white/80 font-medium">{badge}</div>
            ))}
          </div>

          <Link to="/agents" className="inline-block bg-[#853694] hover:bg-[#6a2b77] text-white font-bold py-3 px-8 rounded-full transition-all shadow-[0_0_20px_rgba(133,54,148,0.3)] hover:shadow-[0_0_30px_rgba(133,54,148,0.5)] transform hover:scale-105 active:scale-95">
            Explore our Agents now..!
          </Link>
        </motion.div>
      </div>

      <div className="flex-1 flex justify-center items-center">
        <motion.div initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} transition={{ duration: 0.8, delay: 0.2 }} className="relative flex flex-col items-center w-full max-w-[450px]">
          <img src="/Juvi-logo.png" alt="JuviAI Logo" className="w-12 h-12 object-contain mb-2 backdrop-blur-sm" />
          <div className="text-[11px] font-bold tracking-[0.2em] text-white/40 mb-2 uppercase">Voice Session</div>
          <h2 className="text-3xl font-semibold mb-1">Speak with {selectedAvatar}</h2>
          <p className="text-white/50 text-sm mb-4">English (United States)</p>

          <div className="flex items-center gap-2 bg-white/5 border border-white/10 px-3 py-1 rounded-full mb-12">
            <CheckCircle2 size={12} className="text-[#b72e6a] fill-accentTheme/20" />
            <span className="text-xs text-white/70 font-medium">Ready</span>
          </div>

          <div className="relative mb-16">
            <div className="absolute inset-0 rounded-full border border-white/10 animate-ping opacity-20 scale-150"></div>
            <div className="absolute inset-0 rounded-full border border-[#853694]/40 animate-pulse opacity-30 scale-125"></div>
            <button
              onClick={() => setShowVoiceDemo(true)}
              className="relative w-16 h-16 bg-white/10 border border-white/20 text-white rounded-full flex items-center justify-center backdrop-blur-md shadow-2xl hover:bg-white/20 hover:scale-110 transition-all duration-300"
            >
              <Play fill="currentColor" size={24} className="translate-x-0.5" />
            </button>
          </div>

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
                  <div className="absolute -bottom-2 left-1/2 -translate-x-1/2 w-4 h-4 rotate-45 bg-background border-b border-r border-white/10 -z-10"></div>
                </motion.div>
              )}
            </AnimatePresence>

            <div className="flex gap-4 items-center justify-center" onMouseLeave={() => setActiveAvatar(prev => prev === 'lang' ? 'lang' : '')}>
              {avatars.map((avatar) => {
                const active = avatar.name === activeAvatar;
                return (
                  <div key={avatar.name}
                    className="flex flex-col items-center gap-2 relative group"
                    onMouseEnter={() => setActiveAvatar(avatar.name)}
                    onClick={() => setSelectedAvatar(avatar.name)}
                  >
                    <div className={`w-10 h-10 rounded-full border-2 overflow-hidden transition-all duration-300 ${active ? 'border-[#853694] scale-110 shadow-[0_0_15px_rgba(133,54,148,0.5)]' : 'border-transparent opacity-50 hover:opacity-100 cursor-pointer'}`}>
                      <img src={`/${avatar.name}.png`} alt={avatar.name} className="w-full h-full object-cover" />
                    </div>
                    <span className={`text-[10px] font-medium transition-colors ${active ? 'text-white' : 'text-white/40'}`}>{avatar.name}</span>
                  </div>
                );
              })}
            </div>
          </div>

          <div className="relative mt-2" onMouseLeave={() => setActiveAvatar(prev => prev === 'lang' ? '' : prev)}>
            <div
              className="text-sm text-white/40 flex items-center gap-1 cursor-pointer hover:text-white/70 transition-colors"
              onClick={() => setActiveAvatar(activeAvatar === 'lang' ? '' : 'lang')}
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
                  className="absolute top-full right-0 pt-2 w-48 z-50"
                >
                  <div className="max-h-64 overflow-y-auto bg-white rounded-lg shadow-xl shadow-black/50 border border-white/10 py-2 custom-scrollbar pointer-events-auto">
                    {[
                      'Afrikaans', 'Albanian', 'Amharic', 'Arabic', 'Armenian', 'Azerbaijani', 'Basque', 'Belarusian', 'Bengali', 'Bosnian',
                      'Bulgarian', 'Catalan', 'Cebuano', 'Chichewa', 'Chinese (Mandarin)', 'Chinese (Traditional)', 'Corsican', 'Croatian',
                      'Czech', 'Danish', 'Dutch', 'English (Australia)', 'English (Canada)', 'English (India)', 'English (UK)', 'English (US)',
                      'Esperanto', 'Estonian', 'Filipino', 'Finnish', 'French', 'Frisian', 'Galician', 'Georgian', 'German', 'Greek', 'Gujarati',
                      'Haitian Creole', 'Hausa', 'Hawaiian', 'Hebrew', 'Hindi', 'Hmong', 'Hungarian', 'Icelandic', 'Igbo', 'Indonesian', 'Irish',
                      'Italian', 'Japanese', 'Javanese', 'Kannada', 'Kazakh', 'Khmer', 'Kinyarwanda', 'Korean', 'Kurdish (Kurmanji)', 'Kyrgyz',
                      'Lao', 'Latin', 'Latvian', 'Lithuanian', 'Luxembourgish', 'Macedonian', 'Malagasy', 'Malay', 'Malayalam', 'Maltese', 'Maori',
                      'Marathi', 'Mongolian', 'Myanmar (Burmese)', 'Nepali', 'Norwegian', 'Odia (Oriya)', 'Pashto', 'Persian', 'Polish',
                      'Portuguese', 'Punjabi', 'Romanian', 'Russian', 'Samoan', 'Scots Gaelic', 'Serbian', 'Sesotho', 'Shona', 'Sindhi', 'Sinhala',
                      'Slovak', 'Slovenian', 'Somali', 'Spanish', 'Sundanese', 'Swahili', 'Swedish', 'Tajik', 'Tamil', 'Tatar', 'Telugu', 'Thai',
                      'Turkish', 'Turkmen', 'Ukrainian', 'Urdu', 'Uyghur', 'Uzbek', 'Vietnamese', 'Welsh', 'Xhosa', 'Yiddish', 'Yoruba', 'Zulu'
                    ].map(lang => (
                      <div
                        key={lang}
                        onClick={() => { setSelectedLang(lang); setActiveAvatar(''); }}
                        className={`px-4 py-2 text-xs cursor-pointer transition-colors ${lang === selectedLang ? 'bg-[#853694]/10 text-[#b72e6a] font-semibold' : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'}`}
                      >
                        {lang}
                      </div>
                    ))}
                  </div>
                </motion.div>
              )}
            </AnimatePresence>
          </div>
        </motion.div>
      </div>
      <AnimatePresence>
        {showVoiceDemo && <VoiceDemoModal onClose={() => setShowVoiceDemo(false)} avatarName={selectedAvatar} />}
      </AnimatePresence>
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
    <section className="relative overflow-hidden py-8 border-y border-white/5 z-10">
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
    { id: "appstore",  x: -240, y: -110, size: 64 },
    { id: "whatsapp", x: -240, y:  110, size: 64 },
    { id: "messenger",x: -340, y:    0, size: 56 },
    { id: "handshake",x: -145, y:    0, size: 56 },
    { id: "android",  x:  240, y: -110, size: 64 },
    { id: "userchat", x:  240, y:  110, size: 64 },
    { id: "telegram", x:  340, y:    0, size: 56 },
    { id: "crm",      x:  145, y:    0, size: 56 },
  ];

  return (
    <section className="py-32 relative overflow-hidden flex flex-col items-center justify-center min-h-[640px] bg-[#050508] z-10">
      <div className="absolute inset-0 z-0 flex items-center justify-center pointer-events-none">
        <svg className="absolute w-[900px] h-[460px] overflow-visible" viewBox="-450 -230 900 460">
          {/* Background grid */}
          <g stroke="rgba(133, 54, 148, 0.08)" strokeWidth="1" fill="none">
            {[-360,-240,-140,-40,40,140,240,360].map(x => <line key={x} x1={x} y1="-400" x2={x} y2="400" />)}
            {[-200,-110,0,110,200].map(y => <line key={y} x1="-900" y1={y} x2="900" y2={y} />)}
          </g>

          {/* Connection lines — solid, stronger */}
          <g stroke="rgba(133, 54, 148, 0.45)" strokeWidth="1.5" strokeDasharray="5 5" fill="none">
            <path d="M 0,0 L -145,0 L -340,0" />
            <path d="M -145,0 L -240,-110" />
            <path d="M -145,0 L -240,110" />
            <path d="M 0,0 L 145,0 L 340,0" />
            <path d="M 145,0 L 240,-110" />
            <path d="M 145,0 L 240,110" />
          </g>

          {/* Center hub — 3 animated rings */}
          <motion.circle cx="0" cy="0" r="38" stroke="rgba(133,54,148,1)" strokeWidth="2" fill="rgba(133,54,148,0.06)" strokeDasharray="3 9"
            animate={{ rotate: 360 }} transition={{ duration: 18, repeat: Infinity, ease: 'linear' }} style={{ originX: '0px', originY: '0px' }}
          />
          <motion.circle cx="0" cy="0" r="58" stroke="rgba(133,54,148,0.5)" strokeWidth="1.5" fill="none" strokeDasharray="6 10"
            animate={{ rotate: -360 }} transition={{ duration: 26, repeat: Infinity, ease: 'linear' }} style={{ originX: '0px', originY: '0px' }}
          />
          <motion.circle cx="0" cy="0" r="78" stroke="rgba(133,54,148,0.18)" strokeWidth="1" fill="none" strokeDasharray="60 100"
            animate={{ rotate: 360 }} transition={{ duration: 40, repeat: Infinity, ease: 'linear' }} style={{ originX: '0px', originY: '0px' }}
          />

          {/* Flying data packets */}
          <DataPacket d="M -240,-110 L -145,0 L 0,0" delay={0}   duration={2.4} />
          <DataPacket d="M 0,0 L -145,0 L -240,110"  delay={1.5} duration={2.1} />
          <DataPacket d="M -340,0 L -145,0 L 0,0"    delay={0.5} duration={2.9} />
          <DataPacket d="M 0,0 L 145,0 L 240,-110"   delay={0.2} duration={2.7} />
          <DataPacket d="M 240,110 L 145,0 L 0,0"    delay={1.2} duration={2.3} />
          <DataPacket d="M 0,0 L 145,0 L 340,0"      delay={0.8} duration={3.1} />
        </svg>
      </div>

      <div className="relative z-10 w-full max-w-[900px] h-[460px]">
        {/* Center hub */}
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[80px] h-[80px] rounded-full border-2 border-[#853694] bg-[#060609] flex items-center justify-center shadow-[0_0_50px_rgba(133,54,148,0.6)] z-20">
          <img src="/icons/center-logo.png" alt="Hub" className="w-[55%] h-[55%] object-contain drop-shadow-[0_0_12px_rgba(133,54,148,0.8)]" onError={(e) => { e.currentTarget.src = '/Juvi-logo.png'; }} />
        </div>

        {/* Distributed nodes */}
        {staticNodes.map(n => (
          <div
            key={n.id}
            className="absolute rounded-full border-2 border-white/10 bg-[#0e0e18] flex items-center justify-center shadow-lg hover:scale-115 hover:border-[#853694]/60 hover:shadow-[0_0_22px_rgba(133,54,148,0.4)] transition-all duration-300 cursor-pointer overflow-hidden group"
            style={{
              width: n.size, height: n.size,
              left: `calc(50% + ${n.x}px)`, top: `calc(50% + ${n.y}px)`,
              transform: 'translate(-50%, -50%)'
            }}
          >
            <img
              src={`/icons/${n.id}.png`}
              alt={n.id}
              className="w-[52%] h-[52%] object-contain group-hover:scale-110 transition-transform duration-300"
              onError={(e) => { (e.target as HTMLImageElement).style.opacity = '0'; }}
            />
            <div className="absolute inset-0 flex items-center justify-center -z-10">
              <span className="text-[8px] uppercase font-bold text-white/30 tracking-wider">{n.id.substring(0, 3)}</span>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
};

const UseCaseSection = () => {
  const [currentIndex, setCurrentIndex] = useState(0);
  const navigate = useNavigate();
  const cases = [
    { title: "Smart Information Retrieval Agent", path: '/solutions/smart-retrieval', icon: Search, desc: "Search internal wikis, SharePoint, and the web to deliver cited answers over voice or chat.", tags: ["Structured + unstructured data", "Reference citations", "Multi-source retrieval"] },
    { title: "Automated Service Management", path: '/solutions/itsm-automation', icon: Headphones, desc: "Create, track, and update service requests hands-free; streamline end-to-end ITSM flows.", tags: ["Create/Track/Update", "Chat or voice", "Works with any ITSM backend"] },
    { title: "Intelligent L1 IT Support", path: '/solutions/l1-support', icon: Cog, desc: "Resolve common tech issues using SOPs, live web search, and secure actions across systems.", tags: ["SOP-guided flows", "Routine task automation", "Real-time search"] },
    { title: "Conversational HR Helpdesk", path: '/solutions/hr-helpdesk', icon: Building2, desc: "Answer policy questions, generate letters, and access leave/salary/profile info instantly.", tags: ["Policy QA", "Document generation", "HRIS lookups"] },
    { title: "Unstructured → Structured Data", path: '/solutions/data-automation', icon: Database, desc: "Convert resumes, forms, and messages into JSON/SQL tables for analytics and workflows.", tags: ["Ingest documents", "Entity extraction", "JSON / SQL outputs"] },
    { title: "Image & Document OCR Automation", path: '/solutions/ocr', icon: FileScan, desc: "Read scans and handwritten notes to auto-update backend systems and reduce manual toil.", tags: ["OCR from images", "Form understanding", "Action on visual data"] },
    { title: "Autonomous Research & Analysis", path: '/solutions/smart-retrieval', icon: Search, desc: "Delegate deep web + proprietary research to agents that summarize and draft reports.", tags: ["Connect to web + DBs", "Auto-generated reports", "Concise summaries"] }
  ];

  const handleNext = () => setCurrentIndex((prev) => (prev + 1) % cases.length);
  const handlePrev = () => setCurrentIndex((prev) => (prev - 1 + cases.length) % cases.length);

  return (
    <section className="py-24 px-6 md:px-16 w-full text-center z-10 relative overflow-hidden">
      <div className="text-xs tracking-widest uppercase text-gray-400 mb-2 font-outfit">What you can launch in weeks</div>
      <h2 className="text-3xl md:text-5xl font-outfit font-semibold leading-tight text-white mb-16">
        Outcome-driven agents you can <span className="text-[#b72e6a]">ship fast</span>
      </h2>

      <div className="relative max-w-[900px] mx-auto mb-12">
        {/* Card carousel */}
        <div className="relative h-[440px] flex items-center justify-center">
          {/* Arrows — pushed to screen edges */}
          <button
            className="absolute -left-16 top-1/2 -translate-y-1/2 w-11 h-11 rounded-full bg-white/5 border border-white/10 text-white flex items-center justify-center hover:bg-white/15 hover:border-white/20 z-20 transition-all shadow-xl"
            onClick={handlePrev}
          >
            <ArrowLeft size={18} />
          </button>
          <button
            className="absolute -right-16 top-1/2 -translate-y-1/2 w-11 h-11 rounded-full bg-white/5 border border-white/10 text-white flex items-center justify-center hover:bg-white/15 hover:border-white/20 z-20 transition-all shadow-xl"
            onClick={handleNext}
          >
            <ArrowRight size={18} />
          </button>

          {cases.map((c, i) => {
            const diff = (i - currentIndex + cases.length) % cases.length;
            let offset = diff;
            if (diff > cases.length / 2) offset = diff - cases.length;
            const isCenter = offset === 0;
            const scale = isCenter ? 1 : 0.92;
            const zIndex = isCenter ? 10 : 5 - Math.abs(offset);
            const xTranslate = offset * 88 + '%';
            const opacity = isCenter ? 1 : Math.abs(offset) === 1 ? 0.35 : 0.15;
            return (
              <motion.div
                key={i}
                initial={false}
                animate={{ scale, x: xTranslate, zIndex, opacity }}
                transition={{ duration: 0.5, ease: 'easeOut' }}
                className={`absolute w-full max-w-[540px] rounded-3xl border text-center bg-[#0a0a0f] p-10 transition-all ${
                  isCenter
                    ? 'border-white/15 shadow-[0_0_80px_rgba(0,0,0,0.9),0_0_30px_rgba(133,54,148,0.08)]'
                    : 'border-white/5'
                }`}
              >
                <div className="flex items-center justify-center gap-4 mb-6">
                  <div className={`w-12 h-12 rounded-xl border flex items-center justify-center shadow-inner ${
                    isCenter ? 'bg-[#853694]/10 border-[#853694]/30' : 'bg-white/5 border-white/10'
                  }`}>
                    <c.icon size={20} className={isCenter ? 'text-[#b72e6a]' : 'text-white/70'} />
                  </div>
                  <h3 className="text-[22px] font-semibold text-white tracking-tight font-outfit">{c.title}</h3>
                </div>
                <p className="text-[15px] text-gray-400 mb-8 max-w-[400px] mx-auto leading-relaxed">{c.desc}</p>
                <div className="flex flex-wrap justify-center gap-3 mb-10">
                  {c.tags.map(t => (
                    <span key={t} className="px-4 py-1.5 rounded-full border border-white/10 bg-white/5 text-[12px] text-white/70 font-medium tracking-wide">
                      {t}
                    </span>
                  ))}
                </div>
                <button
                  onClick={() => navigate(c.path)}
                  className={`flex justify-center items-center gap-2 text-[13px] font-semibold mx-auto transition-all group ${
                    isCenter ? 'text-white hover:text-[#b72e6a]' : 'text-white/40 pointer-events-none'
                  }`}
                >
                  Explore this use case <ArrowRight size={14} className="group-hover:translate-x-1 transition-transform" />
                </button>
              </motion.div>
            );
          })}
        </div>
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
      One platform that <span className="text-[#b72e6a]">thinks, talks, and acts</span>
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
    <div className="rounded-2xl border border-[#853694]/30 bg-[#853694]/5 p-8 text-center relative overflow-hidden backdrop-blur-sm shadow-[0_0_30px_rgba(133,54,148,0.1)]">
      <img src="/Juvi-logo.png" alt="Logo" className="h-8 mx-auto mb-4 opacity-80" />
      <p className="text-white/80 max-w-2xl mx-auto mb-6">A voice-enabled, prompt-driven agent platform that unifies reasoning, conversation, and secure tool orchestration into governed workflows.</p>
      <div className="flex flex-wrap justify-center gap-3">
        {[{ icon: Mic, t: "Voice & Chat" }, { icon: Shield, t: "Governed Access" }, { icon: Server, t: "SaaS / Private Cloud" }, { icon: Network, t: "Integrates with your APIs" }].map(b => (
          <span key={b.t} className="inline-flex items-center gap-1.5 rounded-full border border-white/10 bg-white/5 px-3 py-1.5 text-xs text-gray-300">
            <b.icon size={14} className="text-[#b72e6a]" /> {b.t}
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
      <img
        src="/OmniAgent.png"
        alt="OmniAgent Flow Dashboard"
        className="w-full h-full object-contain opacity-60 group-hover:opacity-95 drop-shadow-[0_0_15px_rgba(133,54,148,0.1)] group-hover:drop-shadow-[0_0_30px_rgba(133,54,148,0.25)] transition-all duration-700 mt-2"
      />
      <div className="absolute right-1/4 top-1/2 -translate-y-1/2 w-32 h-32 rounded-full bg-[#853694]/10 blur-[60px] -z-10 group-hover:bg-[#853694]/20 transition-colors duration-700 animate-pulse"></div>
    </motion.div>
    <div className="relative">
      <div className="absolute top-1/2 right-10 -translate-y-1/2 opacity-[0.03] pointer-events-none scale-150">
        <img src="/Juvi-logo.png" alt="watermark" className="w-[400px] h-[400px] object-cover filter brightness-200 grayscale" />
      </div>
      <div className="space-y-10 relative z-10 w-full md:pl-8">
        {[
          { title: "Design in plain English", desc: "Describe outcomes and guardrails — we stitch the flow.", icon: Pencil },
          { title: "Bind secure tools", desc: "Attach APIs and actions with scoped, governed access.", icon: LinkIcon },
          { title: "Deploy to channels", desc: "Ship to web, voice, or email — same flow, new surfaces.", icon: Send },
          { title: "Govern & audit", desc: "Every action is logged with context, approvals, and trace.", icon: ShieldCheck },
        ].map((step, i) => (
          <div key={i} className="relative">
            <div className="absolute -left-10 md:-left-12 top-1 w-7 h-7 rounded-full bg-[#853694]/10 border-2 border-[#853694] flex items-center justify-center shadow-[0_0_15px_rgba(133,54,148,0.4)]">
              <step.icon size={12} className="text-[#b72e6a]" />
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
          <div className="mb-4 text-[#b72e6a] bg-[#853694]/10 w-max p-2 rounded-lg border border-[#853694]/20">
            <f.icon size={20} />
          </div>
          <h3 className="text-base font-semibold text-white mb-2">{f.title}</h3>
          <p className="text-sm text-gray-400">{f.desc}</p>
        </div>
      ))}
    </div>
    <div className="mt-16">
      <Link to="/agents" className="bg-[#853694] hover:bg-[#6a2b77] text-white font-bold py-3 px-8 rounded-full transition-all shadow-[0_0_20px_rgba(133,54,148,0.3)] hover:scale-105 inline-flex items-center gap-2">
        Get started <ArrowRight size={18} />
      </Link>
    </div>
  </section>
);

const LandingPage = () => {
  return (
    <>
      <HeroSection />
      <LogoMarquee />
      <NetworkSection />
      <UseCaseSection />
      <TripleTradeoffSection />
      <StepsSection />
      <FeaturesGrid />
    </>
  );
};

export default LandingPage;
