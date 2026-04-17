import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';
import {
  MessageSquare, Cog, LifeBuoy, Building2,
  FileText, FileScan, Bot, ArrowRight, Zap,
  Search, Mic, Shield, Database, Globe
} from 'lucide-react';

// ─────────────────────────────────────────────
// Agent catalogue
// ─────────────────────────────────────────────
const AGENTS = [
  {
    id: 'smart-retrieval',
    category: 'Customer Support',
    title: 'Smart Retrieval',
    desc: 'Answer with verified citations across KBs, websites, and docs — over voice and chat. Reduces ticket deflection rate by up to 42%.',
    icon: Search,
    tags: ['RAG', 'Voice', 'Chat', 'Citations'],
    badge: 'Popular',
    badgeColor: 'text-purple-400 bg-purple-400/10 border-purple-400/20',
    accent: 'from-purple-500/10 to-purple-500/[0.02]',
  },
  {
    id: 'itsm-automation',
    category: 'IT Operations',
    title: 'ITSM Automation',
    desc: 'Create, update, prioritize, and close IT service requests with secure tool actions and full audit trails. Integrates with ServiceNow, Jira, and more.',
    icon: Cog,
    tags: ['Tool Runtime', 'RBAC', 'Audit', 'ServiceNow'],
    badge: 'Enterprise',
    badgeColor: 'text-blue-400 bg-blue-400/10 border-blue-400/20',
    accent: 'from-blue-500/10 to-blue-500/[0.02]',
  },
  {
    id: 'l1-support',
    category: 'IT Operations',
    title: 'L1 Support Agent',
    desc: 'SOP-guided troubleshooting with real-time knowledge search and automated remediation. Escalates with full context when confidence is low.',
    icon: LifeBuoy,
    tags: ['SOP Guided', 'Handover', 'Voice', 'Search'],
    badge: 'Live',
    badgeColor: 'text-fuchsia-400 bg-fuchsia-400/10 border-fuchsia-400/20',
    accent: 'from-fuchsia-500/10 to-fuchsia-500/[0.02]',
  },
  {
    id: 'hr-helpdesk',
    category: 'Human Resources',
    title: 'Conversational Helpdesk',
    desc: 'Policy answers, offer letter generation, leave & salary queries, and profile updates. Governed access to HR systems with redaction by role.',
    icon: Building2,
    tags: ['Policy RAG', 'Generation', 'RBAC', 'PII Redaction'],
    badge: 'New',
    badgeColor: 'text-purple-400 bg-purple-400/10 border-purple-400/20',
    accent: 'from-purple-500/10 to-purple-500/[0.02]',
  },
  {
    id: 'data-automation',
    category: 'Data Automation',
    title: 'Unstructured → Structured',
    desc: 'Extract entities, normalize messy inputs, and emit clean JSON or SQL-ready rows. Works on emails, PDFs, form data, and free-text streams.',
    icon: FileText,
    tags: ['Extraction', 'JSON', 'PDF', 'ETL'],
    badge: null,
    badgeColor: '',
    accent: 'from-orange-500/10 to-orange-500/[0.02]',
  },
  {
    id: 'ocr',
    category: 'Data Automation',
    title: 'OCR & Document Agent',
    desc: 'Read scanned docs, handwriting, and complex forms. Trigger downstream workflows from extracted data with confidence scoring.',
    icon: FileScan,
    tags: ['OCR', 'Handwriting', 'Forms', 'Workflows'],
    badge: null,
    badgeColor: '',
    accent: 'from-yellow-500/10 to-yellow-500/[0.02]',
  },
  {
    id: 'chat-widget',
    category: 'Channels',
    title: 'JuviAI Chat Widget',
    desc: 'Design, preview, and embed your branded chat agent using a single script tag. Customisable themes, RBAC, and full session analytics.',
    icon: MessageSquare,
    tags: ['Embed', 'Brandable', 'Analytics', 'No-code'],
    badge: 'Beta',
    badgeColor: 'text-rose-400 bg-rose-400/10 border-rose-400/20',
    accent: 'from-rose-500/10 to-rose-500/[0.02]',
  },
];

// ─────────────────────────────────────────────
// Agent card
// ─────────────────────────────────────────────
const AgentCard = ({ agent, index }: { agent: typeof AGENTS[0]; index: number }) => (
  <motion.div
    initial={{ opacity: 0, y: 28 }}
    whileInView={{ opacity: 1, y: 0 }}
    viewport={{ once: true }}
    transition={{ duration: 0.5, delay: index * 0.06, ease: 'easeOut' }}
  >
    <Link
      to={`/solutions/${agent.id}`}
      className={`group flex flex-col h-full rounded-2xl border border-white/8 bg-gradient-to-br ${agent.accent} p-7 hover:border-white/20 hover:scale-[1.015] transition-all duration-300`}
    >
      {/* Header */}
      <div className="flex items-start justify-between mb-5">
        <div className="w-11 h-11 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center group-hover:bg-white/10 transition-colors">
          <agent.icon size={20} className="text-white/70 group-hover:text-white transition-colors" />
        </div>
        {agent.badge && (
          <span className={`text-[10px] font-bold uppercase tracking-widest px-2.5 py-1 rounded-full border ${agent.badgeColor}`}>
            {agent.badge}
          </span>
        )}
      </div>

      {/* Category + Title */}
      <p className="text-[10px] font-bold uppercase tracking-widest text-white/30 mb-1">{agent.category}</p>
      <h2 className="text-lg font-outfit font-bold text-white mb-3 group-hover:text-[#b72e6a] transition-colors">{agent.title}</h2>

      {/* Description */}
      <p className="text-sm text-white/45 leading-relaxed mb-6 flex-1">{agent.desc}</p>

      {/* Tags */}
      <div className="flex flex-wrap gap-2 mb-5">
        {agent.tags.map(tag => (
          <span key={tag} className="px-2.5 py-0.5 text-[11px] font-semibold rounded-full border border-white/8 bg-white/[0.03] text-white/50">
            {tag}
          </span>
        ))}
      </div>

      {/* CTA row */}
      <div className="flex items-center gap-1.5 text-[#b72e6a] text-xs font-bold group-hover:gap-3 transition-all">
        Explore agent <ArrowRight size={13} />
      </div>
    </Link>
  </motion.div>
);

// ─────────────────────────────────────────────
// Stats bar
// ─────────────────────────────────────────────
const StatsBar = () => (
  <div className="grid grid-cols-2 md:grid-cols-4 gap-6 mb-20 max-w-3xl mx-auto">
    {[
      { value: '7', label: 'Ready-to-deploy agents' },
      { value: '15+', label: 'LLM providers' },
      { value: '42%', label: 'Avg ticket deflection' },
      { value: '< 5 min', label: 'Time to first response' },
    ].map((stat, i) => (
      <motion.div
        key={stat.label}
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 + i * 0.08, duration: 0.5 }}
        className="text-center"
      >
        <p className="text-3xl font-outfit font-extrabold text-white mb-1">{stat.value}</p>
        <p className="text-xs text-white/35 font-medium leading-tight">{stat.label}</p>
      </motion.div>
    ))}
  </div>
);

// ─────────────────────────────────────────────
// Main page
// ─────────────────────────────────────────────
const AgentsPage = () => (
  <main className="min-h-screen flex flex-col relative z-10">

    {/* ── Hero ── */}
    <section className="pt-40 pb-14 px-6 md:px-16 text-center">
      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        className="inline-flex items-center gap-2 px-3 py-1 bg-white/5 border border-white/10 rounded-full text-xs font-bold mb-8 text-[#b72e6a] uppercase tracking-widest"
      >
        <Zap size={11} /> Agent Directory
      </motion.div>

      <motion.h1
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.08 }}
        className="text-5xl md:text-6xl font-outfit font-extrabold text-white tracking-tight leading-tight mb-6 max-w-3xl mx-auto"
      >
        Every agent. One platform.<br />
        <span className="text-[#b72e6a]">Pick your use case.</span>
      </motion.h1>

      <motion.p
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.16 }}
        className="text-base text-white/45 max-w-xl mx-auto leading-relaxed mb-14"
      >
        Pre-built, governed, and LLM-agnostic agents for support, IT ops, HR, and data — ready to deploy in voice, chat, or email.
      </motion.p>

      <StatsBar />
    </section>

    {/* ── Agent Grid ── */}
    <section className="pb-24 px-6 md:px-16">
      <div className="max-w-6xl mx-auto grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {AGENTS.map((agent, i) => (
          <AgentCard key={agent.id} agent={agent} index={i} />
        ))}
      </div>
    </section>

    {/* ── Bottom CTA ── */}
    <div className="py-16 px-6 text-center border-t border-white/5">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true }}
      >
        <p className="text-xs text-white/30 uppercase tracking-widest mb-4 font-semibold">Not sure which agent fits?</p>
        <h2 className="text-2xl font-outfit font-bold text-white mb-6">Start with Customer Support</h2>
        <div className="flex flex-wrap items-center justify-center gap-4">
          <Link
            to="/solutions/smart-retrieval"
            className="bg-[#853694] hover:bg-[#6a2b77] text-white font-bold py-3 px-8 rounded-full transition-all shadow-[0_0_20px_rgba(133,54,148,0.35)] hover:scale-105"
          >
            Try Smart Retrieval
          </Link>
          <Link
            to="/platform"
            className="text-white/50 font-semibold text-sm flex items-center gap-1 hover:text-white transition-colors"
          >
            View Platform <ArrowRight size={13} />
          </Link>
        </div>
      </motion.div>
    </div>

  </main>
);

export default AgentsPage;
