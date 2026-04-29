import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';
import {
  Users, Lock, Shield, ArrowRight, CheckCircle2,
  FileText, Download, AlertTriangle, ChevronRight
} from 'lucide-react';

const fadeUp = (delay = 0) => ({
  initial: { opacity: 0, y: 24 },
  whileInView: { opacity: 1, y: 0 },
  viewport: { once: true },
  transition: { duration: 0.55, ease: 'easeOut' as const, delay },
});

// ─────────────────────────────────────────────
// Governance & Handover page
// ─────────────────────────────────────────────
const GovernancePage = () => (
  <main className="min-h-screen flex flex-col relative z-10">

    {/* ── Hero ── */}
    <section className="pt-40 pb-20 px-6 md:px-24 text-center max-w-4xl mx-auto w-full">
      <motion.p {...fadeUp(0)} className="text-xs font-bold uppercase tracking-widest text-white/30 mb-5">
        Trust &amp; Safety
      </motion.p>
      <motion.h1 {...fadeUp(0.08)} className="text-5xl md:text-6xl font-outfit font-extrabold text-white tracking-tight leading-tight mb-6">
        Human + AI: governed execution<br />and safe handover
      </motion.h1>
      <motion.p {...fadeUp(0.16)} className="text-base text-white/45 max-w-2xl mx-auto leading-relaxed">
        Agent Assist, step-through approvals, and explicit handover flows ensure operators remain in control while agents do the heavy lifting.
      </motion.p>
    </section>

    {/* ── 3 pillars ── */}
    <section className="pb-16 px-6 md:px-24 border-b border-white/5">
      <div className="max-w-5xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-6">
        {[
          { icon: Users,  title: 'Agent Assist',    desc: 'Co-pilot mode with suggested actions — the human approves before anything runs.' },
          { icon: Lock,   title: 'Scoped Actions',  desc: 'Per-tool RBAC, rate limits, and timeouts so no action oversteps its mandate.' },
          { icon: Shield, title: 'Approvals',       desc: 'Required checkpoints for sensitive steps; Slack / Email gate-keepers built-in.' },
        ].map((item, i) => (
          <motion.div key={item.title} {...fadeUp(0.1 + i * 0.1)}
            className="rounded-2xl border border-white/5 bg-white/[0.02] p-7 hover:border-[#853694]/20 hover:bg-white/[0.04] transition-all group"
          >
            <div className="w-10 h-10 rounded-xl bg-[#853694]/10 border border-[#853694]/20 flex items-center justify-center mb-5 group-hover:bg-[#853694]/20 transition-colors">
              <item.icon size={18} className="text-[#b72e6a]" />
            </div>
            <h3 className="text-base font-bold text-white mb-2">{item.title}</h3>
            <p className="text-sm text-white/40 leading-relaxed">{item.desc}</p>
          </motion.div>
        ))}
      </div>
    </section>

    {/* ── Handover flows + Audit & policies ── */}
    <section className="py-20 px-6 md:px-24">
      <div className="max-w-5xl mx-auto grid grid-cols-1 md:grid-cols-2 gap-12">

        {/* Handover flows */}
        <motion.div {...fadeUp(0.1)}>
          <h2 className="text-2xl font-outfit font-bold text-white mb-3">Handover flows</h2>
          <p className="text-sm text-white/40 leading-relaxed mb-6">
            Seamless switch between AI and human operator with full context — transcripts, decisions, and remaining next steps.
          </p>
          {/* YAML snippet */}
          <div className="rounded-2xl border border-white/8 bg-[#08080f] p-6 font-mono text-xs text-purple-400/80 leading-6 overflow-auto">
            <p className="text-white/30 mb-2"># handover trigger config</p>
            <p><span className="text-white/60">handover:</span></p>
            <p>&nbsp;&nbsp;<span className="text-white/60">when:</span> [<span className="text-yellow-400">"risk&gt;high"</span>, <span className="text-yellow-400">"confidence&lt;0.6"</span>, <span className="text-yellow-400">"policy.blocked"</span>]</p>
            <p>&nbsp;&nbsp;<span className="text-white/60">transfer:</span></p>
            <p>&nbsp;&nbsp;&nbsp;&nbsp;<span className="text-white/60">to:</span> <span className="text-sky-400">"queue://l1-support"</span></p>
            <p>&nbsp;&nbsp;&nbsp;&nbsp;<span className="text-white/60">payload:</span> [<span className="text-yellow-400">"thread"</span>, <span className="text-yellow-400">"attachments"</span>, <span className="text-yellow-400">"suggestedNextStep"</span>]</p>
            <p>&nbsp;&nbsp;&nbsp;&nbsp;<span className="text-white/60">fallback:</span> <span className="text-sky-400">"notify://oncall"</span></p>
          </div>
        </motion.div>

        {/* Audit & policies */}
        <motion.div {...fadeUp(0.2)}>
          <h2 className="text-2xl font-outfit font-bold text-white mb-3">Audit &amp; policies</h2>
          <p className="text-sm text-white/40 leading-relaxed mb-6">
            Every step is logged. Exportable audit, policy packs for PII, and redact-on-write for sensitive fields.
          </p>
          <div className="space-y-3">
            {[
              { icon: FileText,    label: 'Audit trail',    desc: 'Immutable per-step event log with actor, input, output, and latency.' },
              { icon: Shield,      label: 'Policy packs',   desc: 'Pre-built GDPR, HIPAA, and SOC-2 starter packs you can extend.' },
              { icon: AlertTriangle, label: 'PII redaction', desc: 'Auto-mask credit cards, SSNs, emails before they reach LLM context.' },
              { icon: Download,    label: 'Export',         desc: 'Full audit bundles as JSON/CSV for compliance and forensics.' },
            ].map(item => (
              <div key={item.label} className="flex items-start gap-3 p-4 rounded-xl border border-white/5 bg-white/[0.02] hover:border-[#853694]/20 hover:bg-white/[0.03] transition-all">
                <div className="w-8 h-8 rounded-lg bg-[#853694]/10 border border-[#853694]/15 flex items-center justify-center shrink-0 mt-0.5">
                  <item.icon size={13} className="text-[#b72e6a]" />
                </div>
                <div>
                  <p className="text-sm font-bold text-white mb-0.5">{item.label}</p>
                  <p className="text-xs text-white/40 leading-relaxed">{item.desc}</p>
                </div>
              </div>
            ))}
          </div>
        </motion.div>
      </div>
    </section>

    {/* ── CTA to Security & Scale ── */}
    <div className="py-16 px-6 text-center border-t border-white/5">
      <motion.div {...fadeUp(0)}>
        <p className="text-xs text-white/30 uppercase tracking-widest mb-6 font-semibold">Next: Infrastructure trust</p>
        <Link
          to="/platform/security-scale"
          className="inline-flex items-center gap-2 px-8 py-3.5 rounded-full border border-[#853694]/35 bg-[#853694]/8 text-[#b72e6a] font-bold text-sm hover:bg-[#853694] hover:text-white hover:border-[#853694] transition-all duration-300 group"
        >
          Security &amp; Scale <ArrowRight size={14} className="group-hover:translate-x-1 transition-transform" />
        </Link>
      </motion.div>
    </div>

  </main>
);

export default GovernancePage;
