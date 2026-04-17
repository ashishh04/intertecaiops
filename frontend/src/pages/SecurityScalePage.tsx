import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';
import {
  Shield, Eye, FileText, ArrowRight, CheckCircle2,
  Network, Cloud, Activity, BarChart2, Database,
  Lock, Key, Server, Cpu, Zap
} from 'lucide-react';

const fadeUp = (delay = 0) => ({
  initial: { opacity: 0, y: 24 },
  whileInView: { opacity: 1, y: 0 },
  viewport: { once: true },
  transition: { duration: 0.55, ease: 'easeOut', delay },
});

const Check = ({ children }: { children: React.ReactNode }) => (
  <li className="flex items-start gap-2 text-sm text-white/50">
    <CheckCircle2 size={14} className="text-[#b72e6a] shrink-0 mt-0.5" />
    <span>{children}</span>
  </li>
);

// ─────────────────────────────────────────────
// Security mesh icon row
// ─────────────────────────────────────────────
const SecurityMeshRow = () => (
  <div className="flex items-end justify-center gap-3 my-10">
    {[Network, Cpu, Cloud, Activity, BarChart2].map((Icon, i) => (
      <motion.div
        key={i}
        initial={{ opacity: 0, y: 16 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true }}
        transition={{ delay: i * 0.07, duration: 0.4 }}
        className={`rounded-xl border border-white/10 bg-white/[0.04] flex items-center justify-center ${
          i === 2 ? 'w-16 h-16' : 'w-12 h-12'
        }`}
      >
        <Icon size={i === 2 ? 24 : 18} className="text-white/50" />
      </motion.div>
    ))}
  </div>
);

// ─────────────────────────────────────────────
// Main page
// ─────────────────────────────────────────────
const SecurityScalePage = () => (
  <main className="min-h-screen flex flex-col relative z-10">

    {/* ── Hero ── */}
    <section className="pt-40 pb-20 px-6 md:px-24 text-center max-w-4xl mx-auto w-full">
      <motion.p {...fadeUp(0)} className="text-xs font-bold uppercase tracking-widest text-white/30 mb-5">
        Trust, Security &amp; Scale
      </motion.p>
      <motion.h1 {...fadeUp(0.08)} className="text-5xl md:text-6xl font-outfit font-extrabold text-white tracking-tight leading-tight mb-6">
        Safe by design,<br />
        <span className="text-[#b72e6a]">performant at scale</span>
      </motion.h1>
      <motion.p {...fadeUp(0.16)} className="text-base text-white/45 max-w-2xl mx-auto leading-relaxed mb-10">
        RBAC, secrets isolation, and auditable actions. Horizontal scaling on Kubernetes with observability and predictable SLOs—without compromising control.
      </motion.p>
      <motion.div {...fadeUp(0.24)} className="flex items-center justify-center gap-4">
        <Link
          to="/platform/governance-handover"
          className="bg-[#853694] hover:bg-[#6a2b77] text-white font-bold py-3.5 px-10 rounded-full transition-all shadow-[0_0_20px_rgba(133,54,148,0.4)] hover:scale-105"
        >
          Explore security
        </Link>
        <button
          onClick={() => document.getElementById('scale-section')?.scrollIntoView({ behavior: 'smooth', block: 'start' })}
          className="border border-[#853694]/40 text-[#b72e6a] font-bold py-3.5 px-10 rounded-full hover:bg-[#853694]/10 transition-all"
        >
          See scaling model
        </button>
      </motion.div>
    </section>

    {/* ── 3 pillars ── */}
    <section className="pb-16 px-6 md:px-24 border-b border-white/5">
      <div className="max-w-5xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-6">
        {[
          { icon: Shield,   title: 'Zero-Trust posture',    desc: 'Per-tool scopes, least-privilege defaults, network policies, and explicit approvals.' },
          { icon: Eye,      title: 'Full auditability',     desc: 'Every invocation, input, output, and actor event is traced with immutable logs.' },
          { icon: FileText, title: 'Governance by design',  desc: 'Policy packs for PII, redaction-at-boundary, retention controls, and compliance evidence.' },
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

    {/* ── Security Mesh ── */}
    <section className="py-20 px-6 md:px-24 bg-[#050508] border-b border-white/5">
      <div className="max-w-4xl mx-auto text-center">
        <motion.p {...fadeUp(0)} className="text-xs font-bold uppercase tracking-widest text-white/30 mb-4">Security Mesh</motion.p>
        <motion.h2 {...fadeUp(0.08)} className="text-3xl md:text-4xl font-outfit font-bold text-white mb-4">
          Guardrails everywhere—tools, data, network
        </motion.h2>
        <motion.p {...fadeUp(0.16)} className="text-sm text-white/40 mb-2">
          A layered model: scopes and policies at the edge, encrypted paths in the middle, immutable evidence at the core.
        </motion.p>

        <SecurityMeshRow />

        <motion.div {...fadeUp(0.3)} className="flex flex-wrap justify-center gap-3 mt-4">
          {['AKV/KMS for secrets', 'TLS 1.2+ everywhere', 'Row/doc-level auth', 'Per-step approvals', 'Immutable audit log'].map(tag => (
            <span key={tag} className="inline-flex items-center gap-1.5 px-4 py-1.5 rounded-full border border-[#853694]/20 bg-[#853694]/5 text-xs font-semibold text-[#b72e6a]/80">
              <Zap size={9} /> {tag}
            </span>
          ))}
        </motion.div>
      </div>
    </section>

    {/* ── Secrets & RBAC columns ── */}
    <section className="py-20 px-6 md:px-24 border-b border-white/5">
      <div className="max-w-5xl mx-auto grid grid-cols-1 md:grid-cols-2 gap-6">

        {/* Secrets & Data Controls */}
        <motion.div
          {...fadeUp(0.1)}
          className="rounded-2xl border border-white/8 bg-white/[0.02] p-8 hover:border-[#853694]/20 hover:bg-white/[0.03] transition-all"
        >
          <div className="flex items-center gap-2 mb-4">
            <div className="w-8 h-8 rounded-lg bg-[#853694]/10 border border-[#853694]/20 flex items-center justify-center">
              <Key size={15} className="text-[#b72e6a]" />
            </div>
            <h2 className="text-base font-outfit font-bold text-white">Secrets &amp; Data Controls</h2>
          </div>
          <ul className="space-y-3 mb-5">
            <Check>Secrets in AKV/KMS, never exposed to user space</Check>
            <Check>Token redaction, field-level masking</Check>
            <Check>Row/doc-level authorization on retrieval</Check>
          </ul>
          <div className="border-t border-white/5 pt-5">
            <ul className="space-y-3">
              <Check>At-rest &amp; in-transit encryption (TLS 1.2+)</Check>
              <Check>Configurable retention and purge SLAs</Check>
              <Check>Isolated temp stores per workflow run</Check>
            </ul>
          </div>
        </motion.div>

        {/* RBAC & Approvals */}
        <motion.div
          {...fadeUp(0.2)}
          className="rounded-2xl border border-white/8 bg-white/[0.02] p-8 hover:border-[#853694]/20 hover:bg-white/[0.03] transition-all"
        >
          <div className="flex items-center gap-2 mb-4">
            <div className="w-8 h-8 rounded-lg bg-[#853694]/10 border border-[#853694]/20 flex items-center justify-center">
              <Lock size={15} className="text-[#b72e6a]" />
            </div>
            <h2 className="text-base font-outfit font-bold text-white">RBAC &amp; Approvals</h2>
          </div>
          <ul className="space-y-3">
            <Check>Roles for tools, data, and channels</Check>
            <Check>Per step approval gates for high risk actions</Check>
            <Check>Just-in-time elevation (expiring scopes)</Check>
          </ul>
        </motion.div>

      </div>
    </section>

    {/* ── Scale: Kubernetes ── */}
    <section id="scale-section" className="py-20 px-6 md:px-24 border-b border-white/5">
      <div className="max-w-5xl mx-auto">
        <motion.div {...fadeUp(0)} className="rounded-2xl border border-white/8 bg-white/[0.02] p-10 mb-12">
          <p className="text-xs font-bold uppercase tracking-widest text-white/30 mb-3">Scale</p>
          <h2 className="text-2xl font-outfit font-bold text-white mb-2">Kubernetes-native horizontal scaling</h2>
          <p className="text-sm text-white/40 mb-8">Stateless API, isolated workers, per-tenant namespaces — scale out by AgentHours with SLOs.</p>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {[
              {
                icon: Cpu, title: 'Workload model',
                items: ['HPA for workers; queue-backed fan-out', 'PodSecurity + NetworkPolicy boundaries', 'Node/pool isolation for premium tenants'],
              },
              {
                icon: Network, title: 'Networking',
                items: ['Strict outbound allow-lists; per-tool egress', 'mTLS service → worker; WAF + API gateway', 'Service mesh optional (sidecars)'],
              },
              {
                icon: Database, title: 'Data plane',
                items: ['OLTP for configs & runs', 'Object store for artifacts', 'Observability stores segregated'],
              },
            ].map((col, i) => (
              <motion.div key={col.title} {...fadeUp(0.1 + i * 0.08)}>
                <div className="flex items-center gap-2 mb-4">
                  <col.icon size={15} className="text-white/40" />
                  <h3 className="text-sm font-bold text-white">{col.title}</h3>
                </div>
                <ul className="space-y-2.5">
                  {col.items.map(item => <Check key={item}>{item}</Check>)}
                </ul>
              </motion.div>
            ))}
          </div>
        </motion.div>

        {/* Tracing & SLOs — two bordered cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {[
            {
              icon: Activity, title: 'Tracing & Budgets',
              desc: 'Step-level spans for tools and models. Budget per run, per tenant, and per channel.',
              items: ['Latency, tokens, cost per step', 'Correlated logs → spans → alerts', 'Anomaly flags on tool outputs'],
            },
            {
              icon: BarChart2, title: 'SLOs & Dashboards',
              desc: 'Golden signals for responsiveness, accuracy, and success rate — tied to alerting.',
              items: ['p50/p95 latency by workflow', 'Success & policy-block rates', 'Budget burn & saturation'],
            },
          ].map((col, i) => (
            <motion.div
              key={col.title}
              {...fadeUp(0.1 + i * 0.1)}
              className="rounded-2xl border border-white/8 bg-white/[0.02] p-8 hover:border-[#853694]/20 hover:bg-white/[0.03] transition-all"
            >
              <div className="flex items-center gap-2 mb-3">
                <div className="w-8 h-8 rounded-lg bg-[#853694]/10 border border-[#853694]/20 flex items-center justify-center">
                  <col.icon size={15} className="text-[#b72e6a]" />
                </div>
                <h3 className="text-base font-outfit font-bold text-white">{col.title}</h3>
              </div>
              <p className="text-sm text-white/40 mb-5 leading-relaxed">{col.desc}</p>
              <ul className="space-y-2.5">
                {col.items.map(item => <Check key={item}>{item}</Check>)}
              </ul>
            </motion.div>
          ))}
        </div>
      </div>
    </section>

    {/* ── Bottom CTA ── */}
    <div className="py-16 px-6 text-center">
      <motion.div {...fadeUp(0)}>
        <p className="text-xs text-white/30 uppercase tracking-widest mb-6 font-semibold">Start building securely</p>
        <div className="flex flex-wrap items-center justify-center gap-4">
          <Link
            to="/solutions/smart-retrieval"
            className="bg-[#853694] hover:bg-[#6a2b77] text-white font-bold py-3 px-8 rounded-full transition-all shadow-[0_0_20px_rgba(133,54,148,0.35)] hover:scale-105"
          >
            Explore Agents
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

export default SecurityScalePage;
