import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';
import {
  Brain, Mic, Zap, Shield, Network, Server, Cloud,
  ArrowRight, CheckCircle2, Workflow, Cog, Bot,
  Lock, Globe, Layers, Activity, GitBranch, MessageSquare
} from 'lucide-react';

// ─────────────────────────────────────────────
// Shared animation helpers
// ─────────────────────────────────────────────
const fadeUp = (delay = 0) => ({
  initial: { opacity: 0, y: 28 },
  whileInView: { opacity: 1, y: 0 },
  viewport: { once: true },
  transition: { duration: 0.6, ease: 'easeOut' as const, delay },
});

// ─────────────────────────────────────────────
// Section 1 — Hero
// ─────────────────────────────────────────────
const HeroSection = () => (
  <section className="pt-40 pb-24 px-6 md:px-16 relative z-10 text-center">
    <motion.div {...fadeUp(0)} className="inline-flex items-center gap-2 px-3 py-1 bg-white/5 border border-white/10 rounded-full text-xs font-semibold mb-8 text-[#b72e6a] uppercase tracking-widest">
      <Zap size={12} /> OmniAgent Platform
    </motion.div>

    <motion.h1 {...fadeUp(0.1)} className="text-5xl md:text-7xl font-outfit font-extrabold text-white tracking-tight leading-tight mb-6 max-w-4xl mx-auto">
      Enterprise-grade{' '}
      <span className="text-[#b72e6a]">Agentic Platform</span>
    </motion.h1>

    <motion.p {...fadeUp(0.2)} className="text-lg text-white/50 max-w-2xl mx-auto mb-10 leading-relaxed">
      Build prompt-driven workflows and voice-enabled agents that think, talk, and act with governed access across your tools and data.
    </motion.p>

    <motion.div {...fadeUp(0.3)} className="flex items-center justify-center gap-4 mb-20">
      <Link
        to="/platform/capabilities"
        className="bg-[#853694] hover:bg-[#6a2b77] text-white font-bold py-3.5 px-10 rounded-full transition-all shadow-[0_0_20px_rgba(133,54,148,0.4)] hover:shadow-[0_0_30px_rgba(133,54,148,0.6)] hover:scale-105 active:scale-95"
      >
        Explore capabilities
      </Link>
      <Link
        to="/"
        className="text-sm font-semibold text-white/50 hover:text-white transition-colors flex items-center gap-1 group"
      >
        Back to home <ArrowRight size={14} className="group-hover:translate-x-1 transition-transform" />
      </Link>
    </motion.div>

    {/* Three pillars */}
    <div className="grid grid-cols-1 md:grid-cols-3 gap-5 max-w-4xl mx-auto">
      {[
        { icon: Brain, title: 'Thinks', desc: 'Plan multi-step tasks with reasoning + memory across any data source.' },
        { icon: Mic, title: 'Talks', desc: 'Voice + chat channels with real-time handover and multilingual support.' },
        { icon: Cog, title: 'Acts', desc: 'Secure tool execution with audit trails, RBAC, and guardrails.' },
      ].map((item, i) => (
        <motion.div
          key={item.title}
          {...fadeUp(0.15 + i * 0.1)}
          className="rounded-2xl border border-white/8 bg-white/[0.025] p-8 text-left hover:border-[#853694]/20 hover:bg-white/[0.04] transition-all group"
        >
          <div className="w-10 h-10 rounded-xl bg-[#853694]/10 border border-[#853694]/20 flex items-center justify-center mb-5 group-hover:bg-[#853694]/20 transition-colors">
            <item.icon size={18} className="text-[#b72e6a]" />
          </div>
          <h3 className="text-lg font-bold text-white mb-2 font-outfit">{item.title}</h3>
          <p className="text-sm text-white/40 leading-relaxed">{item.desc}</p>
        </motion.div>
      ))}
    </div>
  </section>
);

// ─────────────────────────────────────────────
// Section 2 — Triple Tradeoff
// ─────────────────────────────────────────────
const TripleTradeoffSection = () => (
  <section className="py-24 px-6 md:px-16 relative z-10">
    <div className="max-w-5xl mx-auto">
      <motion.div {...fadeUp(0)} className="text-center mb-16">
        <p className="text-xs uppercase tracking-widest text-white/30 mb-4 font-semibold">The Triple Tradeoff · Solved</p>
        <h2 className="text-4xl md:text-5xl font-outfit font-bold text-white leading-tight">
          One platform that <span className="text-[#b72e6a]">thinks</span>,{' '}
          <span className="text-[#b72e6a]">talks</span>,{' '}
          and <span className="text-[#b72e6a]">acts</span>
        </h2>
      </motion.div>

      {/* Limitation cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5 mb-8">
        {[
          { label: 'CHATBOTS', icon: MessageSquare, title: 'Talk, can\'t do', desc: 'Great at conversation, limited at taking action across systems.' },
          { label: 'WORKFLOWS', icon: GitBranch, title: 'Do, can\'t think', desc: 'Reliable execution, but brittle when the task needs reasoning or context.' },
          { label: 'AI AGENTS', icon: Bot, title: 'Think, can\'t act alone', desc: 'Reason over complex goals but struggle to securely call enterprise tools by themselves.' },
        ].map((item, i) => (
          <motion.div
            key={item.label}
            {...fadeUp(0.1 + i * 0.1)}
            className="rounded-2xl border border-white/5 bg-white/[0.02] p-7 relative overflow-hidden group hover:border-white/10 transition-all"
          >
            <div className="absolute top-4 right-4 opacity-5 group-hover:opacity-10 transition-opacity">
              <item.icon size={64} className="text-white" />
            </div>
            <p className="text-[10px] font-bold uppercase tracking-widest text-white/30 mb-3">{item.label}</p>
            <h3 className="text-lg font-bold text-white mb-2 font-outfit">{item.title}</h3>
            <p className="text-sm text-white/40 leading-relaxed">{item.desc}</p>
          </motion.div>
        ))}
      </div>

      {/* Unifying card */}
      <motion.div
        {...fadeUp(0.4)}
        className="rounded-2xl border border-[#853694]/20 bg-gradient-to-br from-accentTheme/10 to-accentTheme/[0.03] p-8 text-center"
      >
        <div className="flex items-center justify-center gap-3 mb-4">
          <img src="/Juvi-logo.png" alt="JuviAI" className="w-8 h-8 object-contain" />
          <span className="text-xl font-bold text-white font-outfit">JuviAI</span>
        </div>
        <p className="text-white/60 text-base max-w-xl mx-auto mb-6 leading-relaxed">
          A voice-enabled, prompt-driven agent platform that unifies reasoning, conversation, and secure tool orchestration into governed workflows.
        </p>
        <div className="flex flex-wrap items-center justify-center gap-3">
          {['Voice & Chat', 'Governed Access', 'SaaS / Private Cloud', 'Integrates with your APIs'].map(tag => (
            <span key={tag} className="inline-flex items-center gap-1.5 px-4 py-1.5 rounded-full border border-[#853694]/20 bg-[#853694]/5 text-xs font-semibold text-[#b72e6a]">
              <Zap size={10} /> {tag}
            </span>
          ))}
        </div>
      </motion.div>
    </div>
  </section>
);

// ─────────────────────────────────────────────
// Section 3 — Capabilities Grid
// ─────────────────────────────────────────────
const CapabilitiesSection = () => (
  <section className="py-24 px-6 md:px-16 relative z-10">
    <div className="max-w-5xl mx-auto">
      <motion.div {...fadeUp(0)} className="text-center mb-16">
        <p className="text-xs uppercase tracking-widest text-white/30 mb-4 font-semibold">Full Stack Capabilities</p>
        <h2 className="text-4xl font-outfit font-bold text-white">Everything you need, nothing you don't</h2>
      </motion.div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        {[
          { icon: Workflow, title: 'Prompt-Driven Workflows', desc: 'No-code orchestration over API, WebSocket, and WebRTC.', badge: 'Core' },
          { icon: Brain, title: 'Knowledge · Search · Crawl', desc: 'Bring docs, sites, and systems into context with citations.', badge: 'RAG' },
          { icon: Cog, title: 'Tool Runtime', desc: 'Secure action execution with scoped access and complete audit trails.', badge: 'Actions' },
          { icon: MessageSquare, title: 'Voice + Chat + Email', desc: 'Unified multi-channel agent surface for every interaction.', badge: 'Channels' },
          { icon: Shield, title: 'Safe AI & Governance', desc: 'RBAC, redaction, approvals, and observability by design.', badge: 'Trust' },
          { icon: Network, title: 'Integrations', desc: 'Connect any REST, GraphQL, WebSocket, or custom backend.', badge: 'Connect' },
        ].map((item, i) => (
          <motion.div
            key={item.title}
            {...fadeUp(0.05 * i)}
            className="rounded-2xl border border-white/5 bg-white/[0.02] p-6 hover:border-[#853694]/20 hover:bg-white/[0.04] transition-all group cursor-default"
          >
            <div className="flex items-start justify-between mb-4">
              <div className="w-9 h-9 rounded-lg bg-[#853694]/10 border border-[#853694]/20 flex items-center justify-center group-hover:bg-[#853694]/20 transition-colors">
                <item.icon size={16} className="text-[#b72e6a]" />
              </div>
              <span className="text-[10px] font-bold uppercase tracking-widest text-[#b72e6a]/60 bg-[#853694]/5 border border-[#853694]/10 px-2 py-0.5 rounded-full">{item.badge}</span>
            </div>
            <h3 className="text-sm font-bold text-white mb-2">{item.title}</h3>
            <p className="text-xs text-white/40 leading-relaxed">{item.desc}</p>
          </motion.div>
        ))}
      </div>
    </div>
  </section>
);

// ─────────────────────────────────────────────
// Section 4 — Built for Real World
// ─────────────────────────────────────────────
const RealWorldSection = () => (
  <section className="py-24 px-6 md:px-16 relative z-10">
    <div className="max-w-5xl mx-auto">
      <motion.div {...fadeUp(0)} className="text-center mb-16">
        <p className="text-xs uppercase tracking-widest text-white/30 mb-4 font-semibold">Why JuviAI</p>
        <h2 className="text-4xl md:text-5xl font-outfit font-bold text-white">Built for real-world operations</h2>
      </motion.div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
        {[
          { icon: Lock, title: 'Governed by design', desc: 'RBAC, approvals, redaction, and complete audit trails for every action.' },
          { icon: Cloud, title: 'Deploy anywhere', desc: 'SaaS or Private Cloud on your own VPC with fully managed secrets.' },
          { icon: Globe, title: 'Connect everything', desc: 'Open tool runtime; integrate REST, GraphQL, WebSocket, and custom backends.' },
        ].map((item, i) => (
          <motion.div key={item.title} {...fadeUp(0.1 + i * 0.1)} className="text-left">
            <div className="w-10 h-10 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center mb-4">
              <item.icon size={18} className="text-white/60" />
            </div>
            <h3 className="text-base font-bold text-white mb-2">{item.title}</h3>
            <p className="text-sm text-white/40 leading-relaxed">{item.desc}</p>
          </motion.div>
        ))}
      </div>

      {/* Tags row */}
      <motion.div {...fadeUp(0.4)} className="flex flex-wrap items-center justify-center gap-3 mb-12">
        {['Agent Assist', 'Voice & Chat', 'Approvals', 'Kubernetes', 'APIs & Tools'].map(tag => (
          <span key={tag} className="inline-flex items-center gap-1.5 px-4 py-1.5 rounded-full border border-white/10 bg-white/[0.03] text-xs font-semibold text-white/50">
            {tag}
          </span>
        ))}
      </motion.div>

      {/* CTA */}
      <motion.div {...fadeUp(0.5)} className="text-center">
        <Link
          to="/platform/security-scale"
          className="inline-flex items-center gap-2 px-8 py-3.5 rounded-full border border-[#853694]/40 bg-[#853694]/10 text-[#b72e6a] font-bold text-sm hover:bg-[#853694] hover:text-white hover:border-[#853694] transition-all duration-300 group"
        >
          Trust, Security & Scale <ArrowRight size={14} className="group-hover:translate-x-1 transition-transform" />
        </Link>
      </motion.div>
    </div>
  </section>
);

// ─────────────────────────────────────────────
// Section 5 — Deployment options
// ─────────────────────────────────────────────
const DeploySection = () => (
  <section className="py-24 px-6 md:px-16 relative z-10 border-t border-white/5">
    <div className="max-w-5xl mx-auto">
      <motion.div {...fadeUp(0)} className="text-center mb-16">
        <p className="text-xs uppercase tracking-widest text-white/30 mb-4 font-semibold">Deployment</p>
        <h2 className="text-4xl font-outfit font-bold text-white">Ship on your terms</h2>
      </motion.div>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {[
          {
            icon: Cloud,
            title: 'SaaS — Fully Managed',
            desc: 'Get started in minutes. We handle infrastructure, scaling, uptime, and security patches.',
            tags: ['Zero ops', 'Auto-scale', '99.9% SLA'],
            highlight: false,
          },
          {
            icon: Server,
            title: 'Private Cloud / On-Premise',
            desc: 'Deploy on your own VPC or data centre. Secrets never leave your perimeter.',
            tags: ['BYOC', 'Air-gapped option', 'VPC-native'],
            highlight: true,
          },
        ].map((item, i) => (
          <motion.div
            key={item.title}
            {...fadeUp(0.1 + i * 0.1)}
            className={`rounded-2xl border p-8 ${
              item.highlight
                ? 'border-[#853694]/25 bg-[#853694]/5'
                : 'border-white/5 bg-white/[0.02]'
            }`}
          >
            <div className={`w-10 h-10 rounded-xl border flex items-center justify-center mb-5 ${
              item.highlight ? 'bg-[#853694]/15 border-[#853694]/30' : 'bg-white/5 border-white/10'
            }`}>
              <item.icon size={18} className={item.highlight ? 'text-[#b72e6a]' : 'text-white/50'} />
            </div>
            <h3 className="text-lg font-bold text-white mb-2 font-outfit">{item.title}</h3>
            <p className="text-sm text-white/40 leading-relaxed mb-6">{item.desc}</p>
            <div className="flex flex-wrap gap-2">
              {item.tags.map(tag => (
                <span key={tag} className={`px-3 py-1 rounded-full text-xs font-semibold border ${
                  item.highlight
                    ? 'border-[#853694]/25 bg-[#853694]/10 text-[#b72e6a]'
                    : 'border-white/10 bg-white/5 text-white/50'
                }`}>{tag}</span>
              ))}
            </div>
          </motion.div>
        ))}
      </div>
    </div>
  </section>
);

// ─────────────────────────────────────────────
// Main page
// ─────────────────────────────────────────────
const PlatformPage = () => (
  <main className="min-h-screen flex flex-col">
    <HeroSection />
    <TripleTradeoffSection />
    <CapabilitiesSection />
    <RealWorldSection />
    <DeploySection />
  </main>
);

export default PlatformPage;
