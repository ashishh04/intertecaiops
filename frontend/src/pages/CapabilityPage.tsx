import { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Zap, ArrowRight, CheckCircle2, X, Send, ExternalLink } from 'lucide-react';

const RELATED_SERVICES = [
  { label: 'Prompt-Driven Workflows', to: '/solutions/smart-retrieval' },
  { label: 'ITSM Automation', to: '/solutions/itsm-automation' },
  { label: 'L1 Support', to: '/solutions/l1-support' },
  { label: 'Data Automation', to: '/solutions/data-automation' },
];

const SandboxModal = ({ onClose }: { onClose: () => void }) => (
  <motion.div
    initial={{ opacity: 0 }}
    animate={{ opacity: 1 }}
    exit={{ opacity: 0 }}
    className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-6"
    onClick={e => { if (e.target === e.currentTarget) onClose(); }}
  >
    <motion.div
      initial={{ scale: 0.95, opacity: 0 }}
      animate={{ scale: 1, opacity: 1 }}
      exit={{ scale: 0.95, opacity: 0 }}
      className="bg-[#0e0e1a] border border-white/10 rounded-2xl p-8 w-full max-w-md"
    >
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-[#853694]/10 border border-[#853694]/30 flex items-center justify-center">
            <ExternalLink size={15} className="text-[#b72e6a]" />
          </div>
          <h3 className="text-lg font-outfit font-bold text-white">View Sandbox</h3>
        </div>
        <button onClick={onClose} className="w-8 h-8 rounded-full bg-white/5 flex items-center justify-center hover:bg-white/10">
          <X size={14} className="text-white/50" />
        </button>
      </div>
      <p className="text-sm text-white/50 mb-6 leading-relaxed">
        The live sandbox lets you explore agent capabilities in an isolated test environment. Configure inputs, trigger workflows, and inspect outputs — without affecting production.
      </p>
      <div className="flex flex-col gap-3 mb-6">
        {['Interactive prompt testing', 'Mock tool integrations', 'Real-time trace viewer', 'Shareable session links'].map(f => (
          <div key={f} className="flex items-center gap-2 text-sm text-white/60">
            <CheckCircle2 size={14} className="text-[#b72e6a] shrink-0" /> {f}
          </div>
        ))}
      </div>
      <div className="flex gap-3">
        <button onClick={onClose} className="flex-1 py-2.5 rounded-full border border-white/10 text-white/50 text-sm font-bold hover:bg-white/5 transition-all">
          Cancel
        </button>
        <button
          onClick={() => { window.open('https://juviai.io/sandbox', '_blank'); onClose(); }}
          className="flex-1 py-2.5 rounded-full bg-[#853694] hover:bg-[#6a2b77] text-white text-sm font-bold transition-all flex items-center justify-center gap-2"
        >
          Open Sandbox <ExternalLink size={13} />
        </button>
      </div>
    </motion.div>
  </motion.div>
);

const ContactModal = ({ onClose }: { onClose: () => void }) => {
  const [form, setForm] = useState({ name: '', email: '', message: '' });
  const [sent, setSent] = useState(false);
  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-6"
      onClick={e => { if (e.target === e.currentTarget) onClose(); }}
    >
      <motion.div
        initial={{ scale: 0.95, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        exit={{ scale: 0.95, opacity: 0 }}
        className="bg-[#0e0e1a] border border-white/10 rounded-2xl p-8 w-full max-w-md"
      >
        <div className="flex items-center justify-between mb-6">
          <h3 className="text-lg font-outfit font-bold text-white">Contact Support</h3>
          <button onClick={onClose} className="w-8 h-8 rounded-full bg-white/5 flex items-center justify-center hover:bg-white/10">
            <X size={14} className="text-white/50" />
          </button>
        </div>
        {sent ? (
          <div className="flex flex-col items-center gap-3 py-8">
            <CheckCircle2 size={40} className="text-[#b72e6a]" />
            <p className="text-base font-bold text-white">Message sent!</p>
            <p className="text-sm text-white/40 text-center">A solution architect will reach out within 1 business day.</p>
            <button onClick={onClose} className="mt-2 text-xs text-[#b72e6a] hover:underline">Close</button>
          </div>
        ) : (
          <>
            <p className="text-sm text-white/50 mb-5">Our solution architects are ready to help you design your agentic flow.</p>
            <input
              placeholder="Your name *"
              value={form.name}
              onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
              className="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2.5 text-sm text-white placeholder-white/25 outline-none focus:border-[#853694]/50 mb-3"
            />
            <input
              placeholder="Work email *"
              value={form.email}
              onChange={e => setForm(f => ({ ...f, email: e.target.value }))}
              className="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2.5 text-sm text-white placeholder-white/25 outline-none focus:border-[#853694]/50 mb-3"
            />
            <textarea
              placeholder="How can we help?"
              value={form.message}
              onChange={e => setForm(f => ({ ...f, message: e.target.value }))}
              rows={3}
              className="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2.5 text-sm text-white placeholder-white/25 outline-none focus:border-[#853694]/50 mb-5 resize-none"
            />
            <button
              onClick={() => { if (form.name && form.email) setSent(true); }}
              disabled={!form.name || !form.email}
              className="w-full py-2.5 rounded-full bg-[#853694] hover:bg-[#6a2b77] text-white text-sm font-bold transition-all disabled:opacity-30 flex items-center justify-center gap-2"
            >
              Send Message <Send size={13} />
            </button>
          </>
        )}
      </motion.div>
    </motion.div>
  );
};

const CapabilityPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [showSandbox, setShowSandbox] = useState(false);
  const [showContact, setShowContact] = useState(false);

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
              <button
                onClick={() => navigate('/agents')}
                className="bg-[#853694] hover:bg-[#6a2b77] text-white font-bold py-4 px-10 rounded-full transition-all shadow-lg hover:scale-105 active:scale-95"
              >
                Get Started
              </button>
              <button
                onClick={() => setShowSandbox(true)}
                className="bg-white/5 hover:bg-white/10 text-white font-bold py-4 px-10 rounded-full transition-all border border-white/10 hover:border-white/20"
              >
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
                {RELATED_SERVICES.map(item => (
                  <li
                    key={item.label}
                    onClick={() => navigate(item.to)}
                    className="text-sm text-white/50 hover:text-[#b72e6a] cursor-pointer transition-colors flex items-center justify-between group"
                  >
                    {item.label} <ArrowRight size={12} className="opacity-0 group-hover:opacity-100 transition-opacity" />
                  </li>
                ))}
              </ul>
            </div>

            <div className="p-6 rounded-2xl bg-gradient-to-br from-accentTheme/20 to-transparent border border-[#853694]/20">
              <h3 className="text-white font-bold mb-2">Need Help?</h3>
              <p className="text-sm text-white/60 mb-4">Our solution architects are ready to help you design your agentic flow.</p>
              <button
                onClick={() => setShowContact(true)}
                className="text-[#b72e6a] text-sm font-bold flex items-center gap-1 hover:underline"
              >
                Contact Support <ArrowRight size={14} />
              </button>
            </div>
          </div>
        </div>
      </motion.div>

      <AnimatePresence>
        {showSandbox && <SandboxModal onClose={() => setShowSandbox(false)} />}
        {showContact && <ContactModal onClose={() => setShowContact(false)} />}
      </AnimatePresence>
    </main>
  );
};

export default CapabilityPage;
