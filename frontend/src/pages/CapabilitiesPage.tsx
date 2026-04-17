import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  Workflow, Search, Cog, Mic, MessageSquare, Mail,
  ArrowRight, TerminalSquare, Database, Eye,
  BookOpen, Quote, Shield, Phone, Zap,
  CheckSquare, Activity, Lock
} from 'lucide-react';

// ─────────────────────────────────────────────
// Section data
// ─────────────────────────────────────────────
const SECTIONS = [
  {
    id: 'workflows',
    tab: 'Workflows',
    icon: Workflow,
    title: 'Prompt-Driven Workflows',
    desc: 'Visual orchestration or code: define steps, tools, and guardrails. Support for long-running tasks, retries, and human-in-the-loop pauses.',
    features: [
      { icon: TerminalSquare, title: 'No-code composer', desc: 'Drag steps, connect tools, configure approvals, and publish — no engineering required.' },
      { icon: Database, title: 'State & memory', desc: 'Store context between steps; resumable and idempotent so retries are safe.' },
      { icon: Eye, title: 'Observability', desc: 'Traces, logs, metrics, and per-step cost/time insights for every run.' },
    ],
  },
  {
    id: 'knowledge',
    tab: 'Knowledge · Search · Crawl',
    icon: Search,
    title: 'Knowledge · Search · Crawl',
    desc: 'Bring internal docs, sites, and stores into context. Structured/unstructured, embeddings + keyword, with citations and freshness controls.',
    features: [
      { icon: BookOpen, title: 'Unified indexing', desc: 'Docs, wikis, websites, KBs — schedulers with delta updates to stay fresh.' },
      { icon: Quote, title: 'Citations', desc: 'Ground answers with links/snippets and confidence metadata so agents never hallucinate.' },
      { icon: Lock, title: 'Governed access', desc: 'Row/doc-level permissions and redaction by role — sensitive data stays scoped.' },
    ],
  },
  {
    id: 'tool-runtime',
    tab: 'Tool Runtime',
    icon: Cog,
    title: 'Tool Runtime',
    desc: 'Safely execute actions against your systems with per-tool scopes, approvals, and full audit trails. Run anywhere with Kubernetes.',
    features: [
      { icon: Shield, title: 'Scopes & RBAC', desc: 'Per-tool policy, rate limits, and secrets isolation so no tool oversteps its bounds.' },
      { icon: CheckSquare, title: 'Approvals', desc: 'Step-through or delegated handoff; Slack/Email approvals baked into the runtime.' },
      { icon: Activity, title: 'Tracing', desc: 'Every invocation logged with inputs/outputs and actor — full forensic audit trail.' },
    ],
  },
  {
    id: 'channels',
    tab: 'Channels',
    icon: Mic,
    title: 'Channels',
    desc: 'Ship once, deliver over voice, chat, and email. Consistent governance across surfaces so every channel behaves the same way.',
    features: [
      { icon: Phone, title: 'Voice', desc: 'Low-latency, full-duplex, live handover — WebRTC and SIP compatible.' },
      { icon: MessageSquare, title: 'Chat', desc: 'Web widget + APIs; chat history + feedback loop baked in.' },
      { icon: Mail, title: 'Email', desc: 'Inbound processing + structured replies routed to the right agent.' },
    ],
    cta: { label: 'Governance & Handover', to: '/platform/governance-handover' },
  },
];

// ─────────────────────────────────────────────
// Scroll-spy sticky tabs
// ─────────────────────────────────────────────
const StickyTabs = ({ active, onTabClick }: { active: string; onTabClick: (id: string) => void }) => (
  <div className="sticky top-[72px] z-40 w-full bg-background/80 backdrop-blur-xl border-b border-white/5 flex justify-center">
    <div className="flex items-center gap-1 px-4 py-0 max-w-5xl w-full overflow-x-auto">
      {SECTIONS.map(s => (
        <button
          key={s.id}
          onClick={() => onTabClick(s.id)}
          className={`flex items-center gap-1.5 px-4 py-3.5 text-sm font-semibold whitespace-nowrap border-b-2 transition-all ${
            active === s.id
              ? 'border-[#853694] text-white'
              : 'border-transparent text-white/40 hover:text-white/70'
          }`}
        >
          <s.icon size={13} />
          {s.tab}
        </button>
      ))}
    </div>
  </div>
);

// ─────────────────────────────────────────────
// Individual capability block
// ─────────────────────────────────────────────
const CapabilityBlock = ({
  section,
  sectionRef,
}: {
  section: typeof SECTIONS[0];
  sectionRef: (el: HTMLElement | null) => void;
}) => (
  <section
    id={section.id}
    ref={sectionRef}
    className="py-20 px-6 md:px-16 max-w-5xl mx-auto w-full"
  >
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true }}
      transition={{ duration: 0.5 }}
    >
      {/* Section header */}
      <div className="mb-10 pb-6 border-b border-white/8">
        <div className="flex items-center gap-3 mb-3">
          <div className="w-9 h-9 rounded-xl bg-[#853694]/10 border border-[#853694]/25 flex items-center justify-center">
            <section.icon size={16} className="text-[#b72e6a]" />
          </div>
          <h2 className="text-2xl font-outfit font-bold text-white">{section.title}</h2>
        </div>
        <p className="text-white/50 text-base leading-relaxed max-w-2xl">{section.desc}</p>
      </div>

      {/* Feature cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {section.features.map((feat, i) => (
          <motion.div
            key={feat.title}
            initial={{ opacity: 0, y: 16 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.45, delay: i * 0.08 }}
            className="rounded-2xl border border-white/5 bg-white/[0.02] p-6 hover:border-[#853694]/20 hover:bg-white/[0.04] transition-all group"
          >
            <div className="w-9 h-9 rounded-lg bg-white/5 border border-white/10 flex items-center justify-center mb-4 group-hover:bg-[#853694]/10 group-hover:border-[#853694]/20 transition-colors">
              <feat.icon size={16} className="text-white/50 group-hover:text-[#b72e6a] transition-colors" />
            </div>
            <h3 className="text-sm font-bold text-white mb-2">{feat.title}</h3>
            <p className="text-xs text-white/40 leading-relaxed">{feat.desc}</p>
          </motion.div>
        ))}
      </div>

      {/* Optional CTA */}
      {'cta' in section && section.cta && (
        <div className="mt-10">
          <Link
            to={section.cta.to}
            className="inline-flex items-center gap-2 px-6 py-3 rounded-full border border-[#853694]/30 bg-[#853694]/8 text-[#b72e6a] text-sm font-bold hover:bg-[#853694] hover:text-white hover:border-[#853694] transition-all group"
          >
            {section.cta.label} <ArrowRight size={14} className="group-hover:translate-x-1 transition-transform" />
          </Link>
        </div>
      )}
    </motion.div>
  </section>
);

// ─────────────────────────────────────────────
// Main Page
// ─────────────────────────────────────────────
const CapabilitiesPage = () => {
  const [activeTab, setActiveTab] = useState(SECTIONS[0].id);
  const sectionRefs = useRef<Record<string, HTMLElement | null>>({});

  // Scroll-spy: update active tab as sections enter viewport
  useEffect(() => {
    const observers: IntersectionObserver[] = [];
    SECTIONS.forEach(section => {
      const el = sectionRefs.current[section.id];
      if (!el) return;
      const observer = new IntersectionObserver(
        ([entry]) => { if (entry.isIntersecting) setActiveTab(section.id); },
        { rootMargin: '-30% 0px -60% 0px', threshold: 0 }
      );
      observer.observe(el);
      observers.push(observer);
    });
    return () => observers.forEach(o => o.disconnect());
  }, []);

  // Smooth scroll to section when tab clicked
  const handleTabClick = (id: string) => {
    const el = sectionRefs.current[id];
    if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
  };

  return (
    <main className="min-h-screen flex flex-col relative z-10">
      {/* Hero */}
      <section className="pt-40 pb-12 px-6 md:px-16 text-center">
        <motion.p
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          className="text-xs font-bold uppercase tracking-widest text-white/30 mb-5"
        >
          Capabilities
        </motion.p>
        <motion.h1
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.08 }}
          className="text-5xl md:text-6xl font-outfit font-extrabold text-white tracking-tight leading-tight mb-6 max-w-3xl mx-auto"
        >
          Everything you need to ship{' '}
          <span className="text-[#b72e6a]">agentic apps</span>
        </motion.h1>
        <motion.p
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.16 }}
          className="text-base text-white/45 max-w-2xl mx-auto leading-relaxed"
        >
          Compose AI workflows, index knowledge, execute tools with approvals, and deliver on voice, chat, and email — LLM-agnostic and observability-first.
        </motion.p>
      </section>

      {/* Sticky tab bar */}
      <StickyTabs active={activeTab} onTabClick={handleTabClick} />

      {/* Section blocks */}
      <div className="flex flex-col divide-y divide-white/[0.04]">
        {SECTIONS.map(section => (
          <CapabilityBlock
            key={section.id}
            section={section}
            sectionRef={el => { sectionRefs.current[section.id] = el; }}
          />
        ))}
      </div>

      {/* Bottom CTA */}
      <div className="py-20 px-6 text-center border-t border-white/5">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
        >
          <p className="text-sm text-white/30 mb-3 font-semibold uppercase tracking-widest">Ready to build?</p>
          <h2 className="text-3xl font-outfit font-bold text-white mb-6">Start with a use case</h2>
          <div className="flex flex-wrap items-center justify-center gap-4">
            <Link
              to="/solutions/smart-retrieval"
              className="bg-[#853694] hover:bg-[#6a2b77] text-white font-bold py-3 px-8 rounded-full transition-all shadow-[0_0_20px_rgba(133,54,148,0.35)] hover:scale-105"
            >
              Customer Support Agent
            </Link>
            <Link
              to="/solutions/itsm-automation"
              className="bg-white/5 border border-white/10 text-white font-bold py-3 px-8 rounded-full transition-all hover:bg-white/10"
            >
              ITSM Automation
            </Link>
            <Link
              to="/platform"
              className="text-[#b72e6a] font-semibold text-sm flex items-center gap-1 hover:underline"
            >
              ← Back to Platform
            </Link>
          </div>
        </motion.div>
      </div>
    </main>
  );
};

export default CapabilitiesPage;
