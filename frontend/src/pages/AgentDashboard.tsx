import { useState, useRef, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
  User, MessageSquare, BookOpen, WrapText, Wrench, LayoutGrid,
  Puzzle, Rocket, BarChart2, Settings, ChevronRight, ChevronDown,
  Plus, Zap, GitBranch, Bot, ArrowRight, CheckCircle2, AlertCircle,
  Play, Pause, RefreshCw, ExternalLink, Shield, Network, Cpu,
  FileText, Search, Cog, LifeBuoy, Building2, FileScan, Code2,
  Activity, Clock, Users, Send, X, Upload, Globe, Link2,
  TrendingUp, TrendingDown, Droplet, Moon, Footprints,
  Eye, Trash2, Edit3, Copy, CheckSquare, ChevronLeft,
} from 'lucide-react';

// ─────────────────────────────────────────────
// Types
// ─────────────────────────────────────────────
type SidebarSection = 'Profile' | 'Chat' | 'Knowledge' | 'Forms' | 'Builder';
type BuilderTab = 'Agents' | 'Workflows' | 'My Tools' | 'All Tools' | 'Widgets';

interface ChatMessage { role: 'user' | 'assistant'; content: string; time: string; }

// ─────────────────────────────────────────────
// Agent config
// ─────────────────────────────────────────────
const AGENT_CONFIG: Record<string, {
  title: string; subtitle: string; description: string;
  icon: React.ElementType; color: string; badge: string;
  model: string; status: 'Active' | 'Idle' | 'Draft';
  subAgents: { name: string; type: string; model: string; desc: string; status: 'Active' | 'Idle' | 'Draft' }[];
  tools: { name: string; category: string; desc: string; enabled: boolean }[];
  workflows: { name: string; trigger: string; steps: number; lastRun: string; status: 'success' | 'running' | 'failed' }[];
  stats: { label: string; value: string; delta: string; up: boolean }[];
  mockReplies: string[];
  knowledgeSources: { name: string; type: string; docs: number; synced: string; status: 'synced' | 'syncing' | 'error' }[];
  forms: { name: string; desc: string; fields: number; submissions: number }[];
  widgetTypes: string[];
}> = {
  'smart-retrieval': {
    title: 'Customer Support', subtitle: 'Smart Retrieval Agent',
    description: 'Answer with citations across KBs, websites, and docs—voice & chat.',
    icon: MessageSquare, color: 'from-blue-500/20 to-cyan-500/10', badge: 'Support',
    model: 'GPT-4o', status: 'Active',
    stats: [
      { label: 'Tickets Resolved', value: '4,821', delta: '+12%', up: true },
      { label: 'Avg. Handle Time', value: '1m 43s', delta: '-18%', up: true },
      { label: 'CSAT Score', value: '94.2%', delta: '+3.1%', up: true },
      { label: 'Deflection Rate', value: '67%', delta: '+8%', up: true },
    ],
    mockReplies: [
      'Based on our knowledge base, I can confirm that your account reset takes 2–4 hours. Here is the relevant KB article: [KB-3821] **Password Reset Policy**.',
      'I found 3 relevant articles. The most likely answer: your subscription renews on the 15th of each month. Would you like me to send the invoice link?',
      'Let me search our documentation... Found it! The API rate limit is 1,000 requests/min on the Standard plan. Citation: docs.juviai.io/api-limits.',
      'I\'m escalating this to a human agent with full context. You\'ll be connected in under 2 minutes.',
    ],
    subAgents: [
      { name: 'KnowledgeRetriever', type: 'LanguageAgent', model: 'GPT-4o', desc: 'Fetches cited answers from internal KBs and SharePoint.', status: 'Active' },
      { name: 'WebSearchAgent', type: 'ToolAgent', model: 'Claude 3.5', desc: 'Searches live web for up-to-date information.', status: 'Active' },
      { name: 'HandoverAgent', type: 'RouterAgent', model: 'GPT-4o-mini', desc: 'Detects escalation signals and hands off to human.', status: 'Idle' },
    ],
    tools: [
      { name: 'SharePoint Search', category: 'Knowledge', desc: 'Search enterprise SharePoint content.', enabled: true },
      { name: 'Confluence Fetch', category: 'Knowledge', desc: 'Retrieve pages from Confluence wiki.', enabled: true },
      { name: 'Web Search', category: 'Search', desc: 'Live Bing/Google search tool.', enabled: true },
      { name: 'Ticket Creator', category: 'ITSM', desc: 'Create support tickets in ServiceNow.', enabled: false },
    ],
    workflows: [
      { name: 'Inbound Query Flow', trigger: 'Chat / Voice', steps: 5, lastRun: '2 min ago', status: 'success' },
      { name: 'Escalation Handover', trigger: 'Sentiment < 0.3', steps: 3, lastRun: '14 min ago', status: 'success' },
      { name: 'KB Sync', trigger: 'Daily 2AM', steps: 2, lastRun: '6h ago', status: 'success' },
    ],
    knowledgeSources: [
      { name: 'Confluence Wiki', type: 'Confluence', docs: 1240, synced: '2h ago', status: 'synced' },
      { name: 'SharePoint Docs', type: 'SharePoint', docs: 3821, synced: 'Syncing...', status: 'syncing' },
      { name: 'Help Center Website', type: 'URL Crawl', docs: 580, synced: '1d ago', status: 'synced' },
      { name: 'Product FAQs PDF', type: 'PDF', docs: 45, synced: '3d ago', status: 'synced' },
    ],
    forms: [
      { name: 'Submit an Enquiry', desc: 'Customer contact/enquiry form with routing.', fields: 6, submissions: 4821 },
      { name: 'Feedback Form', desc: 'Post-resolution CSAT collection.', fields: 4, submissions: 2103 },
    ],
    widgetTypes: ['enquiry-form', 'search-index'],
  },
  'itsm-automation': {
    title: 'IT Operations', subtitle: 'ITSM Automation Agent',
    description: 'Create, track, and update IT service requests with secure tool actions.',
    icon: Cog, color: 'from-violet-500/20 to-purple-500/10', badge: 'IT Ops',
    model: 'Claude 3.5 Sonnet', status: 'Active',
    stats: [
      { label: 'Tickets Automated', value: '2,340', delta: '+22%', up: true },
      { label: 'MTTR', value: '8m 12s', delta: '-34%', up: true },
      { label: 'Auto-Resolution', value: '78%', delta: '+11%', up: true },
      { label: 'SLA Compliance', value: '99.1%', delta: '+0.4%', up: true },
    ],
    mockReplies: [
      'I\'ve created INC-00421 in ServiceNow for your VPN issue. Priority: High. Estimated resolution: 30 min. Tracking link sent to your email.',
      'Your change request CHG-00189 has been routed to the Change Advisory Board. Approval needed from 2 members. Expected: 4h.',
      'Incident INC-00418 has been auto-resolved using runbook RB-VPN-03. All 14 affected users have been notified.',
      'Running diagnostic on your reported issue... Found root cause: certificate expired on VPN gateway. Auto-renewing now.',
    ],
    subAgents: [
      { name: 'TriageAgent', type: 'LanguageAgent', model: 'Claude 3.5', desc: 'Classifies and prioritizes incoming incidents.', status: 'Active' },
      { name: 'ServiceNowAgent', type: 'ToolAgent', model: 'GPT-4o', desc: 'Creates and updates records in ServiceNow.', status: 'Active' },
      { name: 'ApprovalAgent', type: 'RouterAgent', model: 'GPT-4o-mini', desc: 'Routes change requests for human approval.', status: 'Idle' },
    ],
    tools: [
      { name: 'ServiceNow API', category: 'ITSM', desc: 'Full CRUD on ServiceNow incidents and requests.', enabled: true },
      { name: 'Jira Integration', category: 'ITSM', desc: 'Create and link Jira tickets.', enabled: true },
      { name: 'PagerDuty Alert', category: 'Monitoring', desc: 'Trigger and resolve PagerDuty alerts.', enabled: true },
      { name: 'Email Notifier', category: 'Comms', desc: 'Send structured email notifications.', enabled: true },
    ],
    workflows: [
      { name: 'Incident Triage', trigger: 'Email / Chat', steps: 6, lastRun: '1 min ago', status: 'running' },
      { name: 'Change Advisory', trigger: 'Change Request', steps: 4, lastRun: '32 min ago', status: 'success' },
      { name: 'Auto-Resolve P3', trigger: 'Known Issue Match', steps: 3, lastRun: '8 min ago', status: 'success' },
    ],
    knowledgeSources: [
      { name: 'ServiceNow KB', type: 'ServiceNow', docs: 920, synced: '30 min ago', status: 'synced' },
      { name: 'IT Runbooks', type: 'Confluence', docs: 340, synced: '1h ago', status: 'synced' },
      { name: 'Network Diagrams', type: 'SharePoint', docs: 88, synced: 'Syncing...', status: 'syncing' },
    ],
    forms: [
      { name: 'Raise a Ticket', desc: 'End-user IT support request form.', fields: 7, submissions: 2340 },
      { name: 'Change Request', desc: 'CAB change advisory board request.', fields: 9, submissions: 412 },
      { name: 'Access Request', desc: 'System/application access provisioning.', fields: 5, submissions: 894 },
    ],
    widgetTypes: ['infra-status', 'itsm-ticket'],
  },
  'l1-support': {
    title: 'IT Operations', subtitle: 'L1 Support Agent',
    description: 'SOP-guided troubleshooting, real-time search, and automation.',
    icon: LifeBuoy, color: 'from-orange-500/20 to-yellow-500/10', badge: 'L1 IT',
    model: 'Gemini 1.5 Pro', status: 'Active',
    stats: [
      { label: 'L1 Issues Resolved', value: '6,102', delta: '+19%', up: true },
      { label: 'Escalation Rate', value: '11%', delta: '-7%', up: true },
      { label: 'First Contact Res.', value: '81%', delta: '+5%', up: true },
      { label: 'Avg. Response', value: '28s', delta: '-12s', up: true },
    ],
    mockReplies: [
      'Following SOP-NET-14: Step 1 — Please restart your network adapter. Go to Device Manager → Network Adapters → Right-click → Disable, then Enable.',
      'I\'ve run a remote diagnostic on your machine. Found: DNS cache issue. Executing `ipconfig /flushdns` now. Please wait 10 seconds.',
      'Your issue matches Known Issue KI-0024 (Outlook crash after update). Applying patch KB5003637 to your machine remotely.',
      'This requires L2 escalation. I\'ve created INC-00488 with full diagnostic logs attached. L2 team notified via PagerDuty.',
    ],
    subAgents: [
      { name: 'SOPAgent', type: 'LanguageAgent', model: 'Gemini 1.5', desc: 'Follows structured SOP runbooks for issue resolution.', status: 'Active' },
      { name: 'DiagnosticAgent', type: 'ToolAgent', model: 'GPT-4o', desc: 'Runs diagnostic scripts and reads system logs.', status: 'Active' },
      { name: 'EscalationRouter', type: 'RouterAgent', model: 'GPT-4o-mini', desc: 'Decides when to escalate to L2/L3.', status: 'Idle' },
    ],
    tools: [
      { name: 'SOP Retriever', category: 'Knowledge', desc: 'Fetches relevant runbooks from knowledge base.', enabled: true },
      { name: 'Remote Desktop Tool', category: 'Diagnostic', desc: 'Initiates remote session for troubleshooting.', enabled: true },
      { name: 'Log Analyzer', category: 'Diagnostic', desc: 'Parses system and application logs.', enabled: true },
      { name: 'Password Reset', category: 'Action', desc: 'Self-service password reset via AD.', enabled: true },
    ],
    workflows: [
      { name: 'SOP Resolution Flow', trigger: 'Ticket / Voice', steps: 7, lastRun: '5 min ago', status: 'success' },
      { name: 'Diagnostic Run', trigger: 'Diagnosis Request', steps: 4, lastRun: '22 min ago', status: 'success' },
      { name: 'L2 Escalation', trigger: 'Unresolved > 10m', steps: 2, lastRun: '1h ago', status: 'success' },
    ],
    knowledgeSources: [
      { name: 'IT SOP Library', type: 'Confluence', docs: 280, synced: '1h ago', status: 'synced' },
      { name: 'Known Issue Database', type: 'ServiceNow', docs: 1120, synced: '20 min ago', status: 'synced' },
    ],
    forms: [
      { name: 'L1 Issue Report', desc: 'Self-service issue submission for end-users.', fields: 5, submissions: 6102 },
      { name: 'Remote Access Consent', desc: 'User authorization for remote diagnostic.', fields: 3, submissions: 1840 },
    ],
    widgetTypes: ['infra-status', 'enquiry-form'],
  },
  'hr-helpdesk': {
    title: 'HR', subtitle: 'Conversational Helpdesk Agent',
    description: 'Policy answers, letter generation, leave/salary/profile queries.',
    icon: Building2, color: 'from-rose-500/20 to-pink-500/10', badge: 'HR',
    model: 'GPT-4o', status: 'Active',
    stats: [
      { label: 'HR Queries Handled', value: '3,456', delta: '+28%', up: true },
      { label: 'Leave Requests', value: '892', delta: '+15%', up: true },
      { label: 'Letters Generated', value: '234', delta: '+41%', up: true },
      { label: 'Employee Satisfaction', value: '96.8%', delta: '+2.2%', up: true },
    ],
    mockReplies: [
      'Your leave balance as of today: **Annual Leave: 12 days**, **Sick Leave: 6 days**, **Casual Leave: 3 days**. Shall I submit a leave request?',
      'I\'ve generated your Employment Verification Letter. It\'s been sent to your registered email and is also available in your ESS portal.',
      'As per HR Policy **P-04.3**, maternity leave is **26 weeks** for the first child with full pay. Shall I initiate the request?',
      'Your April 2026 payslip has been generated. Gross: ₹1,24,500 | Deductions: ₹18,200 | Net: ₹1,06,300. Download link sent.',
    ],
    subAgents: [
      { name: 'PolicyAgent', type: 'LanguageAgent', model: 'GPT-4o', desc: 'Answers HR policy questions with citations.', status: 'Active' },
      { name: 'HRISAgent', type: 'ToolAgent', model: 'Claude 3.5', desc: 'Interfaces with HRIS for leave and payroll data.', status: 'Active' },
      { name: 'LetterDraftAgent', type: 'GenerationAgent', model: 'GPT-4o', desc: 'Generates employment letters and certificates.', status: 'Active' },
    ],
    tools: [
      { name: 'SAP SuccessFactors', category: 'HRIS', desc: 'Employee data, leave balance, payroll info.', enabled: true },
      { name: 'Workday API', category: 'HRIS', desc: 'Alternative HRIS connector.', enabled: false },
      { name: 'Letter Templates', category: 'Documents', desc: 'Pre-approved letter template library.', enabled: true },
      { name: 'Policy Doc Search', category: 'Knowledge', desc: 'Searches HR policy documents.', enabled: true },
    ],
    workflows: [
      { name: 'Leave Request Flow', trigger: 'Chat / Email', steps: 4, lastRun: '3 min ago', status: 'success' },
      { name: 'Letter Generation', trigger: 'Document Request', steps: 3, lastRun: '17 min ago', status: 'success' },
      { name: 'Policy QA', trigger: 'Policy Question', steps: 2, lastRun: '9 min ago', status: 'success' },
    ],
    knowledgeSources: [
      { name: 'HR Policy Handbook', type: 'PDF', docs: 220, synced: '2d ago', status: 'synced' },
      { name: 'SuccessFactors HRIS', type: 'API', docs: 15400, synced: '5 min ago', status: 'synced' },
      { name: 'Benefits & Perks Guide', type: 'SharePoint', docs: 48, synced: '1d ago', status: 'synced' },
    ],
    forms: [
      { name: 'Leave Application', desc: 'Multi-type leave request with manager approval.', fields: 7, submissions: 892 },
      { name: 'HR Query Form', desc: 'General HR enquiry routing form.', fields: 4, submissions: 2120 },
      { name: 'Document Request', desc: 'Employment letter and certificate requests.', fields: 3, submissions: 234 },
    ],
    widgetTypes: ['wellness', 'hr-request'],
  },
  'data-automation': {
    title: 'Data Automation', subtitle: 'Unstructured → Structured Agent',
    description: 'Extract entities, emit JSON/SQL; normalize messy inputs.',
    icon: FileText, color: 'from-teal-500/20 to-purple-500/10', badge: 'Data',
    model: 'Claude 3.5 Sonnet', status: 'Active',
    stats: [
      { label: 'Docs Processed', value: '18,400', delta: '+54%', up: true },
      { label: 'Extraction Accuracy', value: '98.3%', delta: '+1.1%', up: true },
      { label: 'Time Saved/Doc', value: '4.2 min', delta: '+0.8m', up: true },
      { label: 'Pipeline Errors', value: '0.2%', delta: '-0.5%', up: true },
    ],
    mockReplies: [
      'I\'ve extracted 14 entities from your uploaded invoice: Vendor, Amount, GST, PO Number, Line Items... Exporting to JSON now.',
      'Processing 340 emails from your inbox. Extracted: 340 sender names, 312 subject lines, 289 actionable items. CSV ready for download.',
      'Schema mapping complete. Your data has been normalized to the target SQL schema `orders_v2`. 0 validation errors. Ready to write.',
      'Anomaly detected in row 1,204: negative quantity value. Flagged for manual review. All other 18,399 rows passed QA.',
    ],
    subAgents: [
      { name: 'EntityExtractor', type: 'LanguageAgent', model: 'Claude 3.5', desc: 'Extracts named entities and structured fields.', status: 'Active' },
      { name: 'SchemaMapper', type: 'TransformAgent', model: 'GPT-4o', desc: 'Maps extracted data to target JSON/SQL schemas.', status: 'Active' },
      { name: 'ValidationAgent', type: 'QAAgent', model: 'GPT-4o-mini', desc: 'Validates output integrity and flags anomalies.', status: 'Active' },
    ],
    tools: [
      { name: 'Unstructured Parser', category: 'Processing', desc: 'Handles PDFs, DOCx, emails, HTML.', enabled: true },
      { name: 'JSON Emitter', category: 'Output', desc: 'Produces validated JSON output.', enabled: true },
      { name: 'SQL Writer', category: 'Output', desc: 'Writes data to target SQL tables.', enabled: true },
      { name: 'Data Quality Check', category: 'Validation', desc: 'Scores data quality and flags outliers.', enabled: true },
    ],
    workflows: [
      { name: 'Document Ingestion', trigger: 'File Upload / Email', steps: 5, lastRun: '1 min ago', status: 'running' },
      { name: 'Schema Transform', trigger: 'Post-Extraction', steps: 3, lastRun: '1 min ago', status: 'running' },
      { name: 'QA & Export', trigger: 'Post-Transform', steps: 2, lastRun: '2 min ago', status: 'success' },
    ],
    knowledgeSources: [
      { name: 'Schema Library', type: 'Internal DB', docs: 84, synced: '1h ago', status: 'synced' },
      { name: 'Sample Documents Vault', type: 'SharePoint', docs: 1200, synced: '30 min ago', status: 'synced' },
    ],
    forms: [
      { name: 'Upload & Process', desc: 'Document upload form with schema selection.', fields: 5, submissions: 18400 },
      { name: 'Export Config', desc: 'Configure output format and destination.', fields: 6, submissions: 4200 },
    ],
    widgetTypes: ['pipeline-status', 'shipment-tracker'],
  },
  'ocr': {
    title: 'Data Automation', subtitle: 'OCR Processing Agent',
    description: 'Read scans/handwriting/forms and trigger downstream actions.',
    icon: FileScan, color: 'from-amber-500/20 to-orange-500/10', badge: 'OCR',
    model: 'GPT-4o Vision', status: 'Active',
    stats: [
      { label: 'Pages Scanned', value: '92,000', delta: '+31%', up: true },
      { label: 'OCR Accuracy', value: '99.1%', delta: '+0.6%', up: true },
      { label: 'Forms Processed', value: '11,200', delta: '+44%', up: true },
      { label: 'Manual Review Rate', value: '1.8%', delta: '-2.1%', up: true },
    ],
    mockReplies: [
      'OCR complete for your uploaded scan. Extracted 48 fields with 99.4% confidence. Flagged 2 low-confidence fields for review.',
      'Handwriting recognized from form. Patient name: John Doe, DOB: 15/03/1984, Diagnosis code: J45.20. Routing to EMR system.',
      'Invoice scan processed. Vendor: Acme Corp, Amount: $4,820.00, PO: PO-2024-0891. Pushed to AP module for approval.',
      'Batch of 120 forms processed in 4.2 minutes. 118 passed QA, 2 flagged for manual review. Download report?',
    ],
    subAgents: [
      { name: 'VisionOCRAgent', type: 'VisionAgent', model: 'GPT-4o Vision', desc: 'Reads printed and handwritten text from images.', status: 'Active' },
      { name: 'FormParserAgent', type: 'StructureAgent', model: 'Claude 3.5', desc: 'Identifies and parses form fields.', status: 'Active' },
      { name: 'DownstreamTrigger', type: 'ActionAgent', model: 'GPT-4o-mini', desc: 'Triggers post-OCR workflows and API calls.', status: 'Idle' },
    ],
    tools: [
      { name: 'Azure Vision OCR', category: 'Vision', desc: 'Azure Cognitive OCR engine.', enabled: true },
      { name: 'Google Vision API', category: 'Vision', desc: 'Fallback OCR via Google Cloud.', enabled: false },
      { name: 'Form Recognizer', category: 'Forms', desc: 'Structured form field extraction.', enabled: true },
      { name: 'Downstream API', category: 'Actions', desc: 'Webhook to downstream systems.', enabled: true },
    ],
    workflows: [
      { name: 'Image Ingest & OCR', trigger: 'File Upload', steps: 4, lastRun: '4 min ago', status: 'success' },
      { name: 'Form Field Parse', trigger: 'Post-OCR', steps: 3, lastRun: '4 min ago', status: 'success' },
      { name: 'Downstream Action', trigger: 'Post-Parse', steps: 2, lastRun: '5 min ago', status: 'success' },
    ],
    knowledgeSources: [
      { name: 'Form Templates Library', type: 'Internal DB', docs: 320, synced: '2h ago', status: 'synced' },
      { name: 'OCR Training Data', type: 'Azure Blob', docs: 8400, synced: '1d ago', status: 'synced' },
    ],
    forms: [
      { name: 'Document Upload', desc: 'Batch or single document scan upload.', fields: 4, submissions: 11200 },
      { name: 'Review & Approve', desc: 'Manual review queue for low-confidence extractions.', fields: 3, submissions: 2020 },
    ],
    widgetTypes: ['ocr-uploader', 'pipeline-status'],
  },
  'chat-widget': {
    title: 'JuviAI', subtitle: 'Chat Widget Builder',
    description: 'Design, preview & embed your chat widget using a single script tag.',
    icon: Code2, color: 'from-indigo-500/20 to-blue-500/10', badge: 'Widget',
    model: 'GPT-4o-mini', status: 'Active',
    stats: [
      { label: 'Widgets Deployed', value: '142', delta: '+23', up: true },
      { label: 'Monthly Sessions', value: '84,200', delta: '+38%', up: true },
      { label: 'Avg. Engagement', value: '4m 12s', delta: '+22s', up: true },
      { label: 'Opt-in Rate', value: '73%', delta: '+6%', up: true },
    ],
    mockReplies: [
      'Your widget has been configured with the **JuviAI Dark** theme. Preview is live. Copy embed script: `<script src="cdn.juviai.io/widget/v2.js" data-id="wgt_abc123"></script>`',
      'Lead captured: Name: Sarah Chen | Email: sarah@acme.com | Query: "Enterprise pricing". Pushed to HubSpot CRM.',
      'Widget analytics for last 7 days: 4,821 sessions | 3,102 conversations | 78% resolution rate | 4.6/5.0 CSAT.',
      'A/B test results: Variant B (proactive message) has 34% higher engagement. Recommend promoting to 100% traffic.',
    ],
    subAgents: [
      { name: 'ChatAgent', type: 'LanguageAgent', model: 'GPT-4o-mini', desc: 'Primary chat agent for widget interactions.', status: 'Active' },
      { name: 'PersonalityLayer', type: 'StyleAgent', model: 'GPT-4o-mini', desc: 'Applies brand voice and tone to responses.', status: 'Active' },
      { name: 'LeadCapture', type: 'ActionAgent', model: 'GPT-4o-mini', desc: 'Captures lead info and routes to CRM.', status: 'Idle' },
    ],
    tools: [
      { name: 'Widget Preview API', category: 'Builder', desc: 'Real-time widget preview renderer.', enabled: true },
      { name: 'Script Tag Generator', category: 'Embed', desc: 'Generates single-line embed script.', enabled: true },
      { name: 'CRM Connector', category: 'Integration', desc: 'Pushes leads to HubSpot/Salesforce.', enabled: true },
      { name: 'Analytics Tracker', category: 'Analytics', desc: 'Tracks widget engagement metrics.', enabled: true },
    ],
    workflows: [
      { name: 'Widget Config Flow', trigger: 'User Config', steps: 3, lastRun: '12 min ago', status: 'success' },
      { name: 'Lead Capture', trigger: 'Contact Form', steps: 2, lastRun: '28 min ago', status: 'success' },
      { name: 'Analytics Sync', trigger: 'Hourly', steps: 1, lastRun: '43 min ago', status: 'success' },
    ],
    knowledgeSources: [
      { name: 'Product Docs', type: 'URL Crawl', docs: 240, synced: '6h ago', status: 'synced' },
      { name: 'Company FAQs', type: 'PDF', docs: 32, synced: '2d ago', status: 'synced' },
    ],
    forms: [
      { name: 'Widget Config', desc: 'Theme, personality, and channel setup form.', fields: 12, submissions: 142 },
      { name: 'Lead Capture Form', desc: 'Contact form embedded inside the widget.', fields: 5, submissions: 4821 },
    ],
    widgetTypes: ['chat-preview', 'portfolio'],
  },
};

// ─────────────────────────────────────────────
// Widget components
// ─────────────────────────────────────────────
const InfraStatusWidget = () => (
  <div className="rounded-2xl border border-white/10 bg-[#080810] p-5 w-full">
    <div className="flex items-center gap-2 mb-1">
      <span className="text-[9px] font-bold uppercase tracking-widest text-white/30">Core Infrastructure</span>
      <span className="text-[9px] px-1.5 py-0.5 rounded bg-white/5 border border-white/10 text-white/30">Express · Pro</span>
    </div>
    <div className="flex items-center gap-1.5 mb-4">
      <span className="w-1.5 h-1.5 rounded-full bg-purple-400 animate-pulse" />
      <span className="text-[10px] font-bold text-purple-400 uppercase tracking-widest">All Systems Operational</span>
    </div>
    <div className="space-y-2.5">
      {[
        { name: 'API Gateway', env: 'PROD', uptime: '99.99%', incidents: 0, latency: '42ms' },
        { name: 'Auth Service', env: 'PROD', uptime: '99.50%', incidents: 1, latency: '350ms' },
        { name: 'Payment Processor', env: 'STG', uptime: '100%', incidents: 0, latency: '12ms' },
        { name: 'Search Index', env: 'PROD', uptime: '95.20%', incidents: 3, latency: '850ms' },
      ].map(svc => (
        <div key={svc.name} className="flex items-center justify-between border-b border-white/5 pb-2.5">
          <div className="flex items-center gap-2">
            <span className={`w-1.5 h-1.5 rounded-full ${svc.incidents > 0 ? 'bg-amber-400' : 'bg-purple-400'}`} />
            <span className="text-xs font-semibold text-white">{svc.name}</span>
            <span className={`text-[9px] px-1 py-0.5 rounded font-bold ${svc.env === 'PROD' ? 'bg-purple-500/10 text-purple-400' : 'bg-amber-500/10 text-amber-400'}`}>{svc.env}</span>
          </div>
          <div className="flex items-center gap-3 text-[10px] text-white/40">
            <span>{svc.uptime}</span>
            <span>{svc.incidents > 0 ? <span className="text-amber-400">{svc.incidents} Active</span> : '0'}</span>
            <span>{svc.latency}</span>
          </div>
        </div>
      ))}
    </div>
  </div>
);

const EnquiryFormWidget = () => {
  const [form, setForm] = useState({ firstName: '', lastName: '', phone: '', email: '', city: '', type: '' });
  const [sent, setSent] = useState(false);
  return sent ? (
    <div className="rounded-2xl border border-purple-500/20 bg-purple-500/5 p-8 flex flex-col items-center gap-3 w-full">
      <CheckCircle2 size={32} className="text-purple-400" />
      <p className="text-sm font-bold text-white">Enquiry submitted!</p>
      <p className="text-xs text-white/40">We'll respond within 2 business hours.</p>
      <button onClick={() => { setSent(false); setForm({ firstName: '', lastName: '', phone: '', email: '', city: '', type: '' }); }} className="text-xs text-[#b72e6a] hover:underline">Submit another</button>
    </div>
  ) : (
    <div className="rounded-2xl border border-white/10 bg-[#080810] p-5 w-full">
      <h3 className="text-sm font-bold text-white mb-1">Submit an Enquiry</h3>
      <p className="text-[10px] text-white/30 mb-4">Please fill out the form below. All fields are mandatory.</p>
      <div className="grid grid-cols-2 gap-2 mb-2">
        <input placeholder="First Name *" value={form.firstName} onChange={e => setForm(f => ({ ...f, firstName: e.target.value }))} className="bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-xs text-white placeholder-white/25 outline-none focus:border-[#853694]/50" />
        <input placeholder="Last Name *" value={form.lastName} onChange={e => setForm(f => ({ ...f, lastName: e.target.value }))} className="bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-xs text-white placeholder-white/25 outline-none focus:border-[#853694]/50" />
      </div>
      <input placeholder="Phone Number *" value={form.phone} onChange={e => setForm(f => ({ ...f, phone: e.target.value }))} className="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-xs text-white placeholder-white/25 outline-none focus:border-[#853694]/50 mb-2" />
      <input placeholder="Email Address *" value={form.email} onChange={e => setForm(f => ({ ...f, email: e.target.value }))} className="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-xs text-white placeholder-white/25 outline-none focus:border-[#853694]/50 mb-2" />
      <input placeholder="City *" value={form.city} onChange={e => setForm(f => ({ ...f, city: e.target.value }))} className="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-xs text-white placeholder-white/25 outline-none focus:border-[#853694]/50 mb-2" />
      <select value={form.type} onChange={e => setForm(f => ({ ...f, type: e.target.value }))} className="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-xs text-white/60 outline-none focus:border-[#853694]/50 mb-4">
        <option value="">Select an enquiry type</option>
        <option>Technical Support</option><option>Billing</option><option>Sales</option><option>General</option>
      </select>
      <button onClick={() => setSent(true)} className="w-full bg-[#853694] hover:bg-[#6a2b77] text-white text-xs font-bold py-2.5 rounded-lg transition-all">Send</button>
    </div>
  );
};

const WellnessWidget = () => {
  const days = ['M', 'T', 'W', 'T', 'F', 'S', 'S'];
  const active = [0, 1, 2, 3, 4];
  return (
    <div className="rounded-2xl border border-white/10 bg-[#080810] p-5 w-full">
      <div className="flex items-center gap-4 mb-4">
        <span className="text-sm font-bold text-white">Wellness</span>
        <div className="flex gap-2 text-[11px]">
          <button className="text-[#b72e6a] font-bold">Daily</button>
          <button className="text-white/30">Weekly</button>
        </div>
      </div>
      <p className="text-base font-bold text-white mb-0.5">Hi, Alex!</p>
      <p className="text-xs text-white/40 mb-4">You're on a 5-day streak! Keep up the healthy habits.</p>
      <div className="flex gap-2 mb-5">
        {days.map((d, i) => (
          <div key={i} className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold ${active.includes(i) ? 'bg-[#853694] text-white' : 'bg-white/5 text-white/30'}`}>{d}</div>
        ))}
      </div>
      {[
        { label: 'Water Intake', icon: Droplet, val: 5, max: 8, unit: 'glasses', color: 'bg-blue-400' },
        { label: 'Sleep', icon: Moon, val: 6.5, max: 8.0, unit: 'hrs', color: 'bg-indigo-400' },
        { label: 'Steps', icon: Activity, val: 8432, max: 10000, unit: 'steps', color: 'bg-rose-400' },
      ].map(item => (
        <div key={item.label} className="mb-3">
          <div className="flex items-center justify-between mb-1">
            <div className="flex items-center gap-1.5 text-xs text-white/60"><item.icon size={11} />{item.label}</div>
            <span className="text-xs text-white/40">{item.val.toLocaleString()} / {item.max.toLocaleString()} {item.unit}</span>
          </div>
          <div className="h-1.5 rounded-full bg-white/5 overflow-hidden">
            <div className={`h-full rounded-full ${item.color}`} style={{ width: `${(item.val / item.max) * 100}%` }} />
          </div>
        </div>
      ))}
    </div>
  );
};

const ShipmentTrackerWidget = () => (
  <div className="rounded-2xl border border-white/10 bg-[#080810] p-5 w-full">
    <div className="flex items-center justify-between mb-1">
      <span className="text-xs font-bold text-white">SHP-847291</span>
      <span className="text-[10px] px-2 py-0.5 rounded-full bg-amber-500/10 border border-amber-500/20 text-amber-400 font-bold">Delayed</span>
    </div>
    <p className="text-[10px] text-white/30 mb-3">FedEx Freight – North Am.</p>
    <div className="grid grid-cols-2 gap-3 mb-4">
      <div className="bg-white/[0.03] rounded-xl p-3"><p className="text-[9px] text-white/30 mb-1">On-Time %</p><p className="text-lg font-bold text-white">94%</p></div>
      <div className="bg-white/[0.03] rounded-xl p-3"><p className="text-[9px] text-white/30 mb-1">Est. Arrival</p><p className="text-sm font-bold text-white">Oct 24, 14:30</p></div>
    </div>
    <p className="text-[9px] font-bold uppercase tracking-wider text-white/30 mb-3">Tracking Timeline</p>
    <div className="space-y-2.5">
      {[
        { label: 'Picked up', time: 'Oct 22, 08:00', done: true },
        { label: 'Arrived at sorting facility', time: 'Oct 23, 11:45', done: true },
        { label: 'Weather delay reported', time: 'Oct 23, 18:00', done: false, warn: true },
        { label: 'Estimated Delivery', time: 'Oct 24, 14:30', done: false },
      ].map((step, i) => (
        <div key={i} className="flex items-start gap-2.5">
          <div className={`w-2.5 h-2.5 rounded-full mt-0.5 shrink-0 ${step.done ? 'bg-purple-400' : step.warn ? 'bg-amber-400' : 'bg-white/15'}`} />
          <div>
            <p className={`text-xs font-semibold ${step.warn ? 'text-amber-400' : step.done ? 'text-white' : 'text-white/30'}`}>{step.label}</p>
            <p className="text-[10px] text-white/25">{step.time}</p>
          </div>
        </div>
      ))}
    </div>
  </div>
);

const PortfolioWidget = () => (
  <div className="rounded-2xl border border-white/10 bg-gradient-to-br from-slate-800 to-slate-900 p-5 w-full">
    <div className="flex items-center justify-between mb-4">
      <span className="text-[9px] font-bold uppercase tracking-widest text-white/40">Portfolio Value</span>
      <div className="flex items-center gap-1.5">
        <span className="w-2 h-2 rounded-full bg-amber-400" />
        <span className="text-[10px] text-white/50">Risk: Moderate</span>
      </div>
    </div>
    <p className="text-3xl font-bold text-white mb-1">$124,592.50</p>
    <div className="flex items-center gap-2 mb-5">
      <span className="flex items-center gap-1 text-xs font-bold text-purple-400 bg-purple-400/10 px-2 py-0.5 rounded-full">
        <TrendingUp size={10} /> $1,240.00 [1.01%]
      </span>
      <span className="text-[10px] text-white/30">Today</span>
    </div>
    {/* Mini sparkline SVG */}
    <svg viewBox="0 0 200 40" className="w-full h-10 mb-1">
      <polyline points="0,35 30,28 60,30 90,15 120,18 150,8 180,12 200,5" fill="none" stroke="#853694" strokeWidth="2" strokeLinecap="round" />
      <polyline points="0,35 30,28 60,30 90,15 120,18 150,8 180,12 200,5 200,40 0,40" fill="url(#pg)" opacity="0.15" />
      <defs><linearGradient id="pg" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor="#853694" /><stop offset="100%" stopColor="#853694" stopOpacity="0" /></linearGradient></defs>
    </svg>
  </div>
);

const ChatPreviewWidget = () => {
  const msgs = [
    { role: 'bot', text: 'Hi! 👋 How can I help you today?' },
    { role: 'user', text: 'I need help with my order.' },
    { role: 'bot', text: 'Sure! Please share your order number and I\'ll look it up right away.' },
  ];
  return (
    <div className="rounded-2xl border border-white/10 bg-[#080810] w-full overflow-hidden">
      <div className="flex items-center gap-2 px-4 py-3 bg-[#853694]/10 border-b border-white/5">
        <div className="w-6 h-6 rounded-full bg-[#853694] flex items-center justify-center"><Zap size={11} className="text-white" /></div>
        <span className="text-xs font-bold text-white">JuviAI Assistant</span>
        <span className="ml-auto flex items-center gap-1 text-[10px] text-purple-400"><span className="w-1.5 h-1.5 rounded-full bg-purple-400 animate-pulse" />Online</span>
      </div>
      <div className="p-4 space-y-2.5 bg-[#050508]">
        {msgs.map((m, i) => (
          <div key={i} className={`flex ${m.role === 'user' ? 'justify-end' : 'justify-start'}`}>
            <div className={`max-w-[75%] px-3 py-2 rounded-xl text-xs ${m.role === 'user' ? 'bg-[#853694]/20 text-white' : 'bg-white/5 border border-white/8 text-white/70'}`}>{m.text}</div>
          </div>
        ))}
      </div>
      <div className="flex items-center gap-2 px-3 py-3 border-t border-white/5">
        <input className="flex-1 bg-white/5 rounded-lg px-3 py-1.5 text-xs text-white placeholder-white/25 outline-none" placeholder="Type a message..." />
        <button className="w-7 h-7 rounded-lg bg-[#853694] flex items-center justify-center"><Send size={11} className="text-white" /></button>
      </div>
    </div>
  );
};

const OCRUploaderWidget = () => {
  const [dragging, setDragging] = useState(false);
  const [uploaded, setUploaded] = useState(false);
  return (
    <div className="rounded-2xl border border-white/10 bg-[#080810] p-5 w-full">
      <h3 className="text-sm font-bold text-white mb-1">Document OCR Upload</h3>
      <p className="text-[10px] text-white/30 mb-4">Upload scans, PDFs, or images for instant extraction.</p>
      {uploaded ? (
        <div className="text-center py-6">
          <CheckCircle2 size={28} className="text-purple-400 mx-auto mb-2" />
          <p className="text-sm font-bold text-white">Processing complete</p>
          <p className="text-xs text-white/40 mb-3">48 fields extracted · 99.4% confidence</p>
          <button onClick={() => setUploaded(false)} className="text-xs text-[#b72e6a] hover:underline">Upload another</button>
        </div>
      ) : (
        <div
          onDragOver={e => { e.preventDefault(); setDragging(true); }}
          onDragLeave={() => setDragging(false)}
          onDrop={e => { e.preventDefault(); setDragging(false); setUploaded(true); }}
          onClick={() => setUploaded(true)}
          className={`border-2 border-dashed rounded-xl p-8 flex flex-col items-center gap-3 cursor-pointer transition-all ${dragging ? 'border-[#853694] bg-[#853694]/5' : 'border-white/10 hover:border-[#853694]/40'}`}
        >
          <Upload size={24} className="text-white/30" />
          <p className="text-xs text-white/50 text-center">Drag & drop files here or <span className="text-[#b72e6a] font-bold">browse</span></p>
          <p className="text-[10px] text-white/25">PDF, PNG, JPG, TIFF · Max 50MB</p>
        </div>
      )}
    </div>
  );
};

const PipelineStatusWidget = () => (
  <div className="rounded-2xl border border-white/10 bg-[#080810] p-5 w-full">
    <div className="flex items-center justify-between mb-4">
      <span className="text-sm font-bold text-white">Pipeline Status</span>
      <span className="flex items-center gap-1 text-[10px] text-blue-400"><span className="w-1.5 h-1.5 rounded-full bg-blue-400 animate-pulse" />Running</span>
    </div>
    <div className="space-y-3">
      {[
        { name: 'Document Ingestion', pct: 100, status: 'done' },
        { name: 'Entity Extraction', pct: 78, status: 'running' },
        { name: 'Schema Mapping', pct: 0, status: 'pending' },
        { name: 'QA & Export', pct: 0, status: 'pending' },
      ].map(step => (
        <div key={step.name}>
          <div className="flex items-center justify-between mb-1">
            <span className="text-xs text-white/70">{step.name}</span>
            <span className={`text-[10px] font-bold ${step.status === 'done' ? 'text-purple-400' : step.status === 'running' ? 'text-blue-400' : 'text-white/20'}`}>{step.status === 'done' ? '✓' : step.status === 'running' ? `${step.pct}%` : '—'}</span>
          </div>
          <div className="h-1.5 rounded-full bg-white/5">
            <div className={`h-full rounded-full transition-all ${step.status === 'done' ? 'bg-purple-400' : step.status === 'running' ? 'bg-blue-400' : 'bg-white/5'}`} style={{ width: `${step.pct}%` }} />
          </div>
        </div>
      ))}
    </div>
    <div className="mt-4 pt-4 border-t border-white/5 flex items-center justify-between text-[10px] text-white/30">
      <span>18,399 rows processed</span><span>1 anomaly flagged</span>
    </div>
  </div>
);

const HRRequestWidget = () => {
  const [form, setForm] = useState({ name: '', type: '', from: '', to: '', reason: '' });
  const [submitted, setSubmitted] = useState(false);
  return submitted ? (
    <div className="rounded-2xl border border-purple-500/20 bg-purple-500/5 p-8 flex flex-col items-center gap-3 w-full">
      <CheckCircle2 size={28} className="text-purple-400" />
      <p className="text-sm font-bold text-white">Request submitted!</p>
      <p className="text-xs text-white/40">Your manager will receive an approval notification.</p>
      <button onClick={() => setSubmitted(false)} className="text-xs text-[#b72e6a] hover:underline">New request</button>
    </div>
  ) : (
    <div className="rounded-2xl border border-white/10 bg-[#080810] p-5 w-full">
      <h3 className="text-sm font-bold text-white mb-1">HR Request</h3>
      <p className="text-[10px] text-white/30 mb-4">Submit leave, document, or HR policy requests.</p>
      <input placeholder="Your Name *" value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} className="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-xs text-white placeholder-white/25 outline-none focus:border-[#853694]/50 mb-2" />
      <select value={form.type} onChange={e => setForm(f => ({ ...f, type: e.target.value }))} className="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-xs text-white/60 outline-none focus:border-[#853694]/50 mb-2">
        <option value="">Request Type *</option>
        <option>Annual Leave</option><option>Sick Leave</option><option>Work From Home</option><option>Document Request</option><option>HR Policy Query</option>
      </select>
      <div className="grid grid-cols-2 gap-2 mb-2">
        <input type="date" value={form.from} onChange={e => setForm(f => ({ ...f, from: e.target.value }))} className="bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-xs text-white/60 outline-none focus:border-[#853694]/50" />
        <input type="date" value={form.to} onChange={e => setForm(f => ({ ...f, to: e.target.value }))} className="bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-xs text-white/60 outline-none focus:border-[#853694]/50" />
      </div>
      <textarea placeholder="Reason (optional)" value={form.reason} onChange={e => setForm(f => ({ ...f, reason: e.target.value }))} rows={2} className="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-xs text-white placeholder-white/25 outline-none focus:border-[#853694]/50 mb-3 resize-none" />
      <button onClick={() => setSubmitted(true)} className="w-full bg-[#853694] hover:bg-[#6a2b77] text-white text-xs font-bold py-2.5 rounded-lg transition-all">Submit Request</button>
    </div>
  );
};

const ISTMTicketWidget = () => (
  <div className="rounded-2xl border border-white/10 bg-[#080810] p-5 w-full">
    <div className="flex items-center justify-between mb-4">
      <span className="text-sm font-bold text-white">Recent Tickets</span>
      <span className="text-[10px] text-[#b72e6a] font-bold cursor-pointer hover:underline">View all</span>
    </div>
    <div className="space-y-2">
      {[
        { id: 'INC-00421', title: 'VPN Connection Issue', priority: 'High', status: 'In Progress', time: '2m ago' },
        { id: 'INC-00420', title: 'Outlook sync failure', priority: 'Medium', status: 'Resolved', time: '12m ago' },
        { id: 'CHG-00189', title: 'DB maintenance window', priority: 'Low', status: 'Pending Approval', time: '32m ago' },
      ].map(t => (
        <div key={t.id} className="flex items-center justify-between p-3 rounded-xl bg-white/[0.02] border border-white/5 hover:border-white/10 transition-all">
          <div>
            <div className="flex items-center gap-2 mb-0.5">
              <span className="text-[10px] text-white/30 font-mono">{t.id}</span>
              <span className={`text-[9px] font-bold px-1.5 py-0.5 rounded ${t.priority === 'High' ? 'bg-rose-500/10 text-rose-400' : t.priority === 'Medium' ? 'bg-amber-500/10 text-amber-400' : 'bg-white/5 text-white/30'}`}>{t.priority}</span>
            </div>
            <p className="text-xs font-semibold text-white">{t.title}</p>
          </div>
          <div className="text-right">
            <p className={`text-[10px] font-bold ${t.status === 'Resolved' ? 'text-purple-400' : t.status === 'In Progress' ? 'text-blue-400' : 'text-amber-400'}`}>{t.status}</p>
            <p className="text-[10px] text-white/25">{t.time}</p>
          </div>
        </div>
      ))}
    </div>
  </div>
);

const WIDGET_COMPONENT_MAP: Record<string, { label: string; component: React.ReactNode }> = {
  'infra-status': { label: 'Infrastructure Status', component: <InfraStatusWidget /> },
  'enquiry-form': { label: 'Enquiry Form', component: <EnquiryFormWidget /> },
  'wellness': { label: 'Wellness Tracker', component: <WellnessWidget /> },
  'shipment-tracker': { label: 'Shipment Tracker', component: <ShipmentTrackerWidget /> },
  'portfolio': { label: 'Portfolio Value', component: <PortfolioWidget /> },
  'chat-preview': { label: 'Chat Widget Preview', component: <ChatPreviewWidget /> },
  'ocr-uploader': { label: 'OCR Uploader', component: <OCRUploaderWidget /> },
  'pipeline-status': { label: 'Pipeline Status', component: <PipelineStatusWidget /> },
  'hr-request': { label: 'HR Request Form', component: <HRRequestWidget /> },
  'itsm-ticket': { label: 'Ticket Dashboard', component: <ISTMTicketWidget /> },
  'search-index': { label: 'Search Index', component: <InfraStatusWidget /> },
};

// ─────────────────────────────────────────────
// Status badge
// ─────────────────────────────────────────────
const StatusBadge = ({ status }: { status: string }) => {
  const map: Record<string, { color: string; dot: string }> = {
    Active:  { color: 'bg-purple-500/15 text-purple-400 border-purple-500/30', dot: 'bg-purple-400' },
    Idle:    { color: 'bg-white/5 text-white/40 border-white/10', dot: 'bg-white/30' },
    Draft:   { color: 'bg-yellow-500/10 text-yellow-400 border-yellow-500/20', dot: 'bg-yellow-400' },
    success: { color: 'bg-purple-500/15 text-purple-400 border-purple-500/30', dot: 'bg-purple-400' },
    synced:  { color: 'bg-purple-500/15 text-purple-400 border-purple-500/30', dot: 'bg-purple-400' },
    running: { color: 'bg-blue-500/15 text-blue-400 border-blue-500/30', dot: 'bg-blue-400 animate-pulse' },
    syncing: { color: 'bg-blue-500/15 text-blue-400 border-blue-500/30', dot: 'bg-blue-400 animate-pulse' },
    failed:  { color: 'bg-red-500/15 text-red-400 border-red-500/30', dot: 'bg-red-400' },
    error:   { color: 'bg-red-500/15 text-red-400 border-red-500/30', dot: 'bg-red-400' },
  };
  const s = map[status] ?? map.Idle;
  return (
    <span className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full border text-[10px] font-bold uppercase tracking-wide ${s.color}`}>
      <span className={`w-1.5 h-1.5 rounded-full ${s.dot}`} />
      {status}
    </span>
  );
};

// ─────────────────────────────────────────────
// Chat Tab
// ─────────────────────────────────────────────
const ChatTab = ({ config }: { config: typeof AGENT_CONFIG[string] }) => {
  const [messages, setMessages] = useState<ChatMessage[]>([
    { role: 'assistant', content: `Hi! I'm the **${config.subtitle}**. How can I help you today?`, time: 'just now' },
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const bottomRef = useRef<HTMLDivElement>(null);
  const replyIdx = useRef(0);

  useEffect(() => { bottomRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [messages]);

  const send = () => {
    if (!input.trim() || loading) return;
    const userMsg: ChatMessage = { role: 'user', content: input, time: 'just now' };
    setMessages(m => [...m, userMsg]);
    setInput('');
    setLoading(true);
    setTimeout(() => {
      const reply = config.mockReplies[replyIdx.current % config.mockReplies.length];
      replyIdx.current++;
      setMessages(m => [...m, { role: 'assistant', content: reply, time: 'just now' }]);
      setLoading(false);
    }, 1200 + Math.random() * 800);
  };

  const renderContent = (text: string) =>
    text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>').replace(/`(.*?)`/g, '<code class="bg-white/10 px-1 py-0.5 rounded text-[#b72e6a]">$1</code>');

  return (
    <div className="flex flex-col h-[calc(100vh-280px)] min-h-[400px]">
      <div className="flex-1 overflow-y-auto space-y-4 pr-2 mb-4 custom-scrollbar">
        {messages.map((msg, i) => (
          <motion.div key={i} initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
            {msg.role === 'assistant' && (
              <div className="w-7 h-7 rounded-full bg-[#853694]/10 border border-[#853694]/20 flex items-center justify-center mr-2.5 mt-1 shrink-0">
                <Zap size={12} className="text-[#b72e6a]" />
              </div>
            )}
            <div className={`max-w-[75%] px-4 py-3 rounded-2xl text-sm leading-relaxed ${msg.role === 'user' ? 'bg-[#853694]/20 border border-[#853694]/30 text-white rounded-tr-sm' : 'bg-white/[0.04] border border-white/8 text-white/80 rounded-tl-sm'}`}
              dangerouslySetInnerHTML={{ __html: renderContent(msg.content) }}
            />
          </motion.div>
        ))}
        {loading && (
          <div className="flex items-start gap-2.5">
            <div className="w-7 h-7 rounded-full bg-[#853694]/10 border border-[#853694]/20 flex items-center justify-center shrink-0"><Zap size={12} className="text-[#b72e6a]" /></div>
            <div className="bg-white/[0.04] border border-white/8 px-4 py-3 rounded-2xl rounded-tl-sm flex items-center gap-1.5">
              {[0, 1, 2].map(i => <span key={i} className="w-1.5 h-1.5 bg-white/40 rounded-full animate-bounce" style={{ animationDelay: `${i * 0.15}s` }} />)}
            </div>
          </div>
        )}
        <div ref={bottomRef} />
      </div>
      <div className="flex items-center gap-3 bg-white/[0.03] border border-white/8 rounded-2xl px-4 py-3">
        <input
          value={input} onChange={e => setInput(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && send()}
          placeholder={`Ask the ${config.subtitle}...`}
          className="flex-1 bg-transparent text-sm text-white placeholder-white/25 outline-none"
        />
        <button id="chat-send-btn" onClick={send} disabled={loading || !input.trim()} className="w-8 h-8 rounded-full bg-[#853694] flex items-center justify-center disabled:opacity-30 hover:bg-[#6a2b77] transition-all">
          <Send size={13} className="text-white" />
        </button>
      </div>
      <div className="flex flex-wrap gap-2 mt-3">
        {['What can you do?', 'Show me recent activity', 'Help with a new request'].map(q => (
          <button key={q} onClick={() => { setInput(q); setTimeout(() => { const btn = document.getElementById('chat-send-btn'); btn?.click(); }, 50); }} className="text-[11px] px-3 py-1.5 rounded-full border border-white/10 bg-white/[0.02] text-white/40 hover:text-white/70 hover:border-[#853694]/30 transition-all">{q}</button>
        ))}
      </div>
    </div>
  );
};

// ─────────────────────────────────────────────
// Knowledge Tab
// ─────────────────────────────────────────────
const KnowledgeTab = ({ config }: { config: typeof AGENT_CONFIG[string] }) => {
  const [query, setQuery] = useState('');
  const [showAddSource, setShowAddSource] = useState(false);
  const [addedSource, setAddedSource] = useState('');
  const filtered = config.knowledgeSources.filter(s => s.name.toLowerCase().includes(query.toLowerCase()) || s.type.toLowerCase().includes(query.toLowerCase()));
  const typeIcon: Record<string, React.ElementType> = {
    Confluence: BookOpen, SharePoint: Network, PDF: FileText,
    'URL Crawl': Globe, 'Internal DB': Cpu, API: Network,
    'Azure Blob': Network, ServiceNow: Cog,
  };
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-sm font-semibold text-white/70">{config.knowledgeSources.length} Knowledge Sources</h3>
          <p className="text-xs text-white/30 mt-0.5">Connected data sources powering this agent's answers</p>
        </div>
        <button onClick={() => setShowAddSource(true)} className="flex items-center gap-2 px-4 py-2 rounded-full bg-[#853694] hover:bg-[#6a2b77] text-white text-xs font-bold transition-all">
          <Plus size={14} /> Add Source
        </button>
      </div>
      {addedSource && (
        <div className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-purple-500/10 border border-purple-500/20 text-xs font-semibold text-purple-400">
          <CheckCircle2 size={13} /> {addedSource} connected successfully!
        </div>
      )}
      <div className="relative">
        <Search size={14} className="absolute left-4 top-1/2 -translate-y-1/2 text-white/25" />
        <input value={query} onChange={e => setQuery(e.target.value)} placeholder="Search sources..." className="w-full bg-white/[0.03] border border-white/8 rounded-xl pl-10 pr-4 py-2.5 text-sm text-white placeholder-white/25 outline-none focus:border-[#853694]/40 transition-colors" />
      </div>
      <div className="space-y-3">
        {filtered.map((src, i) => {
          const Icon = typeIcon[src.type] ?? FileText;
          return (
            <motion.div key={src.name} initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.05 }}
              className="flex items-center justify-between p-5 rounded-2xl border border-white/5 bg-white/[0.02] hover:bg-white/[0.04] hover:border-white/10 transition-all group cursor-pointer">
              <div className="flex items-center gap-4">
                <div className="w-10 h-10 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center group-hover:border-[#853694]/30 transition-colors">
                  <Icon size={16} className="text-white/40 group-hover:text-[#b72e6a] transition-colors" />
                </div>
                <div>
                  <p className="text-sm font-bold text-white">{src.name}</p>
                  <div className="flex items-center gap-2 mt-0.5">
                    <span className="text-[10px] text-white/30 uppercase tracking-wider">{src.type}</span>
                    <span className="text-white/10">·</span>
                    <span className="text-[10px] text-white/30">{src.docs.toLocaleString()} docs</span>
                    <span className="text-white/10">·</span>
                    <span className="text-[10px] text-white/30">Synced {src.synced}</span>
                  </div>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <StatusBadge status={src.status} />
                <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                  <button onClick={() => { const el = document.getElementById('dash-toast'); if (el) { el.textContent = `↻ Refreshing ${src.name}...`; el.className = 'fixed bottom-6 right-6 z-50 px-5 py-2.5 rounded-full bg-[#1a1a2e] border border-[#853694]/40 text-sm font-semibold text-white shadow-xl'; setTimeout(() => { if (el) el.className = 'fixed bottom-6 right-6 z-50 hidden'; }, 2500); }}} className="w-7 h-7 rounded-lg bg-white/5 flex items-center justify-center hover:bg-white/10"><RefreshCw size={11} className="text-white/50" /></button>
                  <button onClick={() => { const el = document.getElementById('dash-toast'); if (el) { el.textContent = `🗑️ ${src.name} removed`; el.className = 'fixed bottom-6 right-6 z-50 px-5 py-2.5 rounded-full bg-rose-900/80 border border-rose-500/30 text-sm font-semibold text-white shadow-xl'; setTimeout(() => { if (el) el.className = 'fixed bottom-6 right-6 z-50 hidden'; }, 2500); }}} className="w-7 h-7 rounded-lg bg-white/5 flex items-center justify-center hover:bg-rose-500/10"><Trash2 size={11} className="text-white/50 hover:text-rose-400" /></button>
                </div>
              </div>
            </motion.div>
          );
        })}
      </div>
      <div className="rounded-2xl border border-white/5 bg-white/[0.02] p-5">
        <p className="text-xs font-semibold text-white/40 mb-3 uppercase tracking-widest">Total Index Coverage</p>
        <div className="flex items-center gap-4">
          <div className="text-2xl font-bold text-white">{config.knowledgeSources.reduce((a, b) => a + b.docs, 0).toLocaleString()}</div>
          <div className="text-xs text-white/40">documents indexed across {config.knowledgeSources.length} sources</div>
        </div>
      </div>
      <AnimatePresence>
        {showAddSource && (
          <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
            className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-6"
            onClick={e => { if (e.target === e.currentTarget) setShowAddSource(false); }}>
            <motion.div initial={{ scale: 0.95 }} animate={{ scale: 1 }} exit={{ scale: 0.95 }}
              className="bg-[#0e0e1a] border border-white/10 rounded-2xl p-7 w-full max-w-md">
              <div className="flex items-center justify-between mb-5">
                <h3 className="text-base font-outfit font-bold text-white">Add Knowledge Source</h3>
                <button onClick={() => setShowAddSource(false)} className="w-7 h-7 rounded-full bg-white/5 flex items-center justify-center hover:bg-white/10"><X size={13} className="text-white/50" /></button>
              </div>
              <p className="text-xs text-white/40 mb-4">Select a source type to connect to this agent's knowledge base.</p>
              {['Confluence Wiki', 'SharePoint Site', 'PDF / Documents', 'URL Crawl', 'Database / API'].map(src => (
                <button key={src} onClick={() => { setAddedSource(src); setShowAddSource(false); setTimeout(() => setAddedSource(''), 3500); }}
                  className="w-full text-left px-4 py-3 mb-2 rounded-xl border border-white/8 bg-white/[0.02] text-sm text-white/60 hover:border-[#853694]/30 hover:text-white hover:bg-[#853694]/5 transition-all">
                  {src}
                </button>
              ))}
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};

// ─────────────────────────────────────────────
// Forms Tab
// ─────────────────────────────────────────────
const FormsTab = ({ config }: { config: typeof AGENT_CONFIG[string] }) => {
  const [selected, setSelected] = useState<number | null>(null);
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-sm font-semibold text-white/70">{config.forms.length} Forms Configured</h3>
          <p className="text-xs text-white/30 mt-0.5">Embeddable data-collection forms for this agent</p>
        </div>
        <button className="flex items-center gap-2 px-4 py-2 rounded-full bg-[#853694] hover:bg-[#6a2b77] text-white text-xs font-bold transition-all">
          <Plus size={14} /> Create Form
        </button>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {config.forms.map((form, i) => (
          <motion.div key={form.name} initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.08 }}
            className={`rounded-2xl border p-5 cursor-pointer transition-all group ${selected === i ? 'border-[#853694]/40 bg-[#853694]/5' : 'border-white/5 bg-white/[0.02] hover:border-white/10 hover:bg-white/[0.04]'}`}
            onClick={() => setSelected(selected === i ? null : i)}
          >
            <div className="flex items-start justify-between mb-3">
              <div className="w-9 h-9 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center group-hover:border-[#853694]/30 transition-colors">
                <WrapText size={15} className="text-white/40 group-hover:text-[#b72e6a] transition-colors" />
              </div>
              <div className="flex gap-1">
                <button className="w-7 h-7 rounded-lg bg-white/5 flex items-center justify-center hover:bg-white/10"><Eye size={11} className="text-white/50" /></button>
                <button className="w-7 h-7 rounded-lg bg-white/5 flex items-center justify-center hover:bg-white/10"><Copy size={11} className="text-white/50" /></button>
                <button className="w-7 h-7 rounded-lg bg-white/5 flex items-center justify-center hover:bg-white/10"><Edit3 size={11} className="text-white/50" /></button>
              </div>
            </div>
            <p className="text-sm font-bold text-white mb-1">{form.name}</p>
            <p className="text-xs text-white/40 mb-4">{form.desc}</p>
            <div className="flex items-center gap-4 text-[11px] text-white/30">
              <span className="flex items-center gap-1"><CheckSquare size={10} /> {form.fields} fields</span>
              <span className="flex items-center gap-1"><Activity size={10} /> {form.submissions.toLocaleString()} submissions</span>
            </div>
            {selected === i && (
              <div className="mt-4 pt-4 border-t border-white/5">
                <p className="text-[10px] font-bold uppercase tracking-widest text-white/30 mb-2">Embed snippet</p>
                <div className="bg-[#08080f] rounded-xl p-3 font-mono text-[10px] text-purple-400/80 flex items-center justify-between gap-2">
                  <span className="truncate">{`<iframe src="https://agent.juviai.io/forms/${form.name.toLowerCase().replace(/\s/g, '-')}" />`}</span>
                  <button onClick={() => { navigator.clipboard.writeText(`<iframe src="https://agent.juviai.io/forms/${form.name.toLowerCase().replace(/\s/g, '-')}" />`); const el = document.getElementById('dash-toast'); if (el) { el.textContent = '\u2713 Copied to clipboard'; el.className = 'fixed bottom-6 right-6 z-50 px-5 py-2.5 rounded-full bg-[#1a1a2e] border border-[#853694]/40 text-sm font-semibold text-white shadow-xl'; setTimeout(() => { if (el) el.className = 'fixed bottom-6 right-6 z-50 hidden'; }, 2000); }}} className="shrink-0 hover:text-[#b72e6a]"><Copy size={11} /></button>
                </div>
              </div>
            )}
          </motion.div>
        ))}
      </div>
    </div>
  );
};

// ─────────────────────────────────────────────
// Widgets Tab
// ─────────────────────────────────────────────
const WidgetsTab = ({ config }: { config: typeof AGENT_CONFIG[string] }) => {
  const [showCreate, setShowCreate] = useState(false);
  const [activeWidget, setActiveWidget] = useState<string | null>(null);
  const available = config.widgetTypes.map(wt => WIDGET_COMPONENT_MAP[wt]).filter(Boolean);

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="text-[10px] font-bold uppercase tracking-widest text-[#b72e6a] bg-[#853694]/10 px-2 py-0.5 rounded-full border border-[#853694]/20">Widget Studio · Beta</span>
          </div>
          <h3 className="text-lg font-outfit font-bold text-white">Build once. Render anywhere.</h3>
          <p className="text-xs text-white/40 mt-1">Click any widget to preview and configure. Embed via script tag.</p>
        </div>
        <div className="flex items-center gap-2">
          <button className="flex items-center gap-2 px-4 py-2 rounded-full bg-white/5 border border-white/10 text-white/70 text-xs font-semibold hover:bg-white/10 transition-all">
            <RefreshCw size={12} /> Refresh Widgets
          </button>
          <button onClick={() => setShowCreate(true)} className="flex items-center gap-2 px-4 py-2 rounded-full bg-[#853694] hover:bg-[#6a2b77] text-white text-xs font-bold transition-all shadow-[0_0_15px_rgba(133,54,148,0.3)]">
            <Plus size={14} /> Create Widget
          </button>
        </div>
      </div>

      {/* Widget gallery label */}
      <div>
        <p className="text-xs font-semibold text-white/30 uppercase tracking-widest mb-4">Widget Gallery <span className="text-white/15">· {available.length} live widgets</span></p>
        {available.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 gap-4 text-center">
            <div className="w-14 h-14 rounded-2xl bg-white/5 border border-white/10 flex items-center justify-center">
              <Puzzle size={24} className="text-white/20" />
            </div>
            <p className="text-sm text-white/40">No widgets configured yet.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {available.map((w, i) => (
              <motion.div key={i} initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.08 }}
                className="group"
              >
                <div className="flex items-center justify-between mb-2">
                  <p className="text-xs font-bold text-white/60">{w.label}</p>
                  <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                    <button onClick={() => { const el = document.getElementById('dash-toast'); if (el) { el.textContent = `👁️ Previewing ${w.label}`; el.className = 'fixed bottom-6 right-6 z-50 px-5 py-2.5 rounded-full bg-[#1a1a2e] border border-[#853694]/40 text-sm font-semibold text-white shadow-xl'; setTimeout(() => { if (el) el.className = 'fixed bottom-6 right-6 z-50 hidden'; }, 2500); }}} className="text-[10px] flex items-center gap-1 px-2 py-1 rounded-lg bg-white/5 text-white/40 hover:text-white/70"><Eye size={10} /> Preview</button>
                    <button onClick={() => { const code = `<script src="https://cdn.juviai.io/widget/${w.label.toLowerCase().replace(/\s/g,'-')}.js"></script>`; navigator.clipboard.writeText(code); const el = document.getElementById('dash-toast'); if (el) { el.textContent = '✓ Embed code copied!'; el.className = 'fixed bottom-6 right-6 z-50 px-5 py-2.5 rounded-full bg-[#1a1a2e] border border-[#853694]/40 text-sm font-semibold text-white shadow-xl'; setTimeout(() => { if (el) el.className = 'fixed bottom-6 right-6 z-50 hidden'; }, 2500); }}} className="text-[10px] flex items-center gap-1 px-2 py-1 rounded-lg bg-white/5 text-white/40 hover:text-white/70"><Copy size={10} /> Embed</button>
                  </div>
                </div>
                <div className="hover:scale-[1.01] transition-transform duration-300">{w.component}</div>
              </motion.div>
            ))}
          </div>
        )}
      </div>

      {/* Create Widget Modal */}
      <AnimatePresence>
        {showCreate && (
          <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
            className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-6"
            onClick={e => { if (e.target === e.currentTarget) setShowCreate(false); }}>
            <motion.div initial={{ scale: 0.95, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} exit={{ scale: 0.95, opacity: 0 }}
              className="bg-[#0e0e1a] border border-white/10 rounded-2xl p-8 w-full max-w-lg max-h-[85vh] overflow-y-auto">
              <div className="flex items-center justify-between mb-6">
                <h3 className="text-lg font-outfit font-bold text-white">Create Widget</h3>
                <button onClick={() => setShowCreate(false)} className="w-8 h-8 rounded-full bg-white/5 flex items-center justify-center hover:bg-white/10"><X size={14} className="text-white/50" /></button>
              </div>
              <p className="text-xs text-white/40 mb-5">Select a widget type for <span className="text-[#b72e6a] font-bold">{config.title}</span></p>
              <div className="grid grid-cols-2 gap-3 mb-6">
                {Object.entries(WIDGET_COMPONENT_MAP).map(([key, val]) => (
                  <button key={key} onClick={() => setActiveWidget(key)}
                    className={`rounded-xl border p-3 text-left text-xs font-semibold transition-all ${activeWidget === key ? 'border-[#853694]/50 bg-[#853694]/10 text-[#b72e6a]' : 'border-white/8 bg-white/[0.02] text-white/50 hover:border-white/15 hover:text-white/70'}`}>
                    {val.label}
                  </button>
                ))}
              </div>
              <div className="flex gap-3">
                <button onClick={() => setShowCreate(false)} className="flex-1 py-2.5 rounded-full border border-white/10 text-white/50 text-xs font-bold hover:bg-white/5 transition-all">Cancel</button>
                <button onClick={() => setShowCreate(false)} disabled={!activeWidget}
                  className="flex-1 py-2.5 rounded-full bg-[#853694] hover:bg-[#6a2b77] text-white text-xs font-bold transition-all disabled:opacity-30">
                  Add Widget
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};

// ─────────────────────────────────────────────
// Agents Tab
// ─────────────────────────────────────────────
const AgentsTab = ({ config }: { config: typeof AGENT_CONFIG[string] }) => (
  <div className="space-y-8">
    <div className="flex items-center justify-between">
      <h3 className="text-sm font-semibold text-white/70">{config.subAgents.length} Sub-agents configured</h3>
      <button className="flex items-center gap-2 px-4 py-2 rounded-full bg-[#853694] hover:bg-[#6a2b77] text-white text-xs font-bold transition-all">
        <Plus size={14} /> Create Sub-Agent
      </button>
    </div>
    <div className="rounded-2xl border border-[#853694]/20 bg-[#853694]/5 p-5">
      <div className="flex items-center gap-3 mb-3">
        <div className="w-9 h-9 rounded-xl bg-[#853694]/10 border border-[#853694]/30 flex items-center justify-center"><Bot size={16} className="text-[#b72e6a]" /></div>
        <div>
          <div className="flex items-center gap-2">
            <span className="text-sm font-bold text-white">agent</span>
            <span className="px-2 py-0.5 rounded bg-white/10 text-[10px] text-white/50">LanguageAgent</span>
            <span className="px-2 py-0.5 rounded bg-rose-500/10 border border-rose-500/20 text-[10px] text-rose-400 font-bold">Entry</span>
          </div>
          <div className="text-[11px] text-white/40 mt-0.5">Parent: None · {config.model}</div>
        </div>
      </div>
      <p className="text-xs text-white/50">Primary orchestration agent for {config.title} — {config.subtitle}.</p>
    </div>
    <div className="space-y-4">
      {config.subAgents.map((agent, i) => (
        <motion.div key={agent.name} initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.05 }}
          className="rounded-2xl border border-white/5 bg-white/[0.02] hover:bg-white/[0.04] hover:border-white/10 p-5 transition-all cursor-pointer group">
          <div className="flex items-start justify-between">
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center group-hover:border-[#853694]/30 transition-colors">
                <Cpu size={16} className="text-white/40 group-hover:text-[#b72e6a] transition-colors" />
              </div>
              <div>
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="text-sm font-bold text-white">{agent.name}</span>
                  <span className="px-2 py-0.5 rounded bg-white/10 text-[10px] text-white/50">{agent.type}</span>
                  <StatusBadge status={agent.status} />
                </div>
                <div className="text-[11px] text-white/40 mt-0.5">Model: {agent.model}</div>
              </div>
            </div>
            <ArrowRight size={14} className="text-white/20 group-hover:text-[#b72e6a] transition-all -translate-x-1 group-hover:translate-x-0 opacity-0 group-hover:opacity-100" />
          </div>
          <p className="text-xs text-white/50 mt-3 ml-12">{agent.desc}</p>
        </motion.div>
      ))}
    </div>
  </div>
);

// ─────────────────────────────────────────────
// Workflows Tab
// ─────────────────────────────────────────────
const WorkflowsTab = ({ config }: { config: typeof AGENT_CONFIG[string] }) => {
  type WFStatus = 'success' | 'running' | 'failed';
  const [wfStatuses, setWfStatuses] = useState<Record<string, WFStatus>>(
    Object.fromEntries(config.workflows.map(w => [w.name, w.status]))
  );
  const [showNewWF, setShowNewWF] = useState(false);
  const [newWFName, setNewWFName] = useState('');

  const toggleWF = (name: string) => {
    setWfStatuses(s => ({ ...s, [name]: s[name] === 'running' ? 'success' : 'running' }));
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-sm font-semibold text-white/70">{config.workflows.length} Active Workflows</h3>
          <p className="text-xs text-white/30 mt-0.5">Agentic flow orchestration for {config.title}</p>
        </div>
        <button onClick={() => setShowNewWF(true)} className="flex items-center gap-2 px-4 py-2 rounded-full bg-[#853694] hover:bg-[#6a2b77] text-white text-xs font-bold transition-all">
          <Plus size={14} /> New Workflow
        </button>
      </div>
      <div className="space-y-4">
        {config.workflows.map((wf, i) => {
          const status = wfStatuses[wf.name] ?? wf.status;
          return (
            <motion.div key={wf.name} initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.05 }}
              className="rounded-2xl border border-white/5 bg-white/[0.02] hover:bg-white/[0.04] p-5 transition-all group cursor-pointer">
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center group-hover:border-[#853694]/30 transition-colors">
                    <GitBranch size={15} className="text-white/40 group-hover:text-[#b72e6a] transition-colors" />
                  </div>
                  <div>
                    <p className="text-sm font-bold text-white">{wf.name}</p>
                    <p className="text-[11px] text-white/30 mt-0.5">Trigger: {wf.trigger} · {wf.steps} steps</p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <StatusBadge status={status} />
                  <button onClick={() => toggleWF(wf.name)} className="w-7 h-7 rounded-full bg-white/5 border border-white/10 hover:border-[#853694]/30 flex items-center justify-center transition-colors">
                    {status === 'running' ? <Pause size={12} className="text-white/60" /> : <Play size={12} className="text-white/60" />}
                  </button>
                </div>
              </div>
              <div className="flex items-center gap-4 ml-12 text-[11px] text-white/30">
                <span className="flex items-center gap-1"><Clock size={10} /> Last run: {wf.lastRun}</span>
                <span className="flex items-center gap-1"><CheckCircle2 size={10} className="text-purple-400" /> All checks passed</span>
              </div>
            </motion.div>
          );
        })}
      </div>
      <AnimatePresence>
        {showNewWF && (
          <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
            className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-6"
            onClick={e => { if (e.target === e.currentTarget) setShowNewWF(false); }}>
            <motion.div initial={{ scale: 0.95 }} animate={{ scale: 1 }} exit={{ scale: 0.95 }}
              className="bg-[#0e0e1a] border border-white/10 rounded-2xl p-7 w-full max-w-md">
              <div className="flex items-center justify-between mb-5">
                <h3 className="text-base font-outfit font-bold text-white">New Workflow</h3>
                <button onClick={() => setShowNewWF(false)} className="w-7 h-7 rounded-full bg-white/5 flex items-center justify-center hover:bg-white/10"><X size={13} className="text-white/50" /></button>
              </div>
              <input value={newWFName} onChange={e => setNewWFName(e.target.value)} placeholder="Workflow name *" className="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2.5 text-sm text-white placeholder-white/25 outline-none focus:border-[#853694]/50 mb-3" />
              <select className="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2.5 text-sm text-white/60 outline-none mb-5">
                <option>Select trigger</option>
                <option>Chat / Voice</option>
                <option>Email Inbound</option>
                <option>Schedule (Cron)</option>
                <option>Webhook</option>
              </select>
              <div className="flex gap-3">
                <button onClick={() => setShowNewWF(false)} className="flex-1 py-2.5 rounded-full border border-white/10 text-white/50 text-xs font-bold hover:bg-white/5">Cancel</button>
                <button onClick={() => { setShowNewWF(false); setNewWFName(''); }} disabled={!newWFName} className="flex-1 py-2.5 rounded-full bg-[#853694] hover:bg-[#6a2b77] text-white text-xs font-bold disabled:opacity-30">Create Workflow</button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};

// ─────────────────────────────────────────────
// Tools Tab
// ─────────────────────────────────────────────
const ToolsTab = ({ config }: { config: typeof AGENT_CONFIG[string] }) => {
  const [toolStates, setToolStates] = useState<Record<string, boolean>>(
    Object.fromEntries(config.tools.map(t => [t.name, t.enabled]))
  );
  const [showAddTool, setShowAddTool] = useState(false);
  const [addedMsg, setAddedMsg] = useState('');

  const toggle = (name: string) => setToolStates(s => ({ ...s, [name]: !s[name] }));

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-sm font-semibold text-white/70">{config.tools.length} Tools Available</h3>
          <p className="text-xs text-white/30 mt-0.5">Secure, scoped tool actions for this agent</p>
        </div>
        <button onClick={() => setShowAddTool(true)} className="flex items-center gap-2 px-4 py-2 rounded-full bg-white/5 border border-white/10 text-white text-xs font-bold hover:bg-white/10 transition-all">
          <Plus size={14} /> Add Tool
        </button>
      </div>
      {addedMsg && (
        <div className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-purple-500/10 border border-purple-500/20 text-xs font-semibold text-purple-400">
          <CheckCircle2 size={13} /> {addedMsg}
        </div>
      )}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {config.tools.map((tool, i) => {
          const enabled = toolStates[tool.name] ?? tool.enabled;
          return (
            <motion.div key={tool.name} initial={{ opacity: 0, scale: 0.97 }} animate={{ opacity: 1, scale: 1 }} transition={{ delay: i * 0.05 }}
              className={`rounded-2xl border p-5 transition-all group ${enabled ? 'border-[#853694]/20 bg-[#853694]/5 hover:border-[#853694]/40' : 'border-white/5 bg-white/[0.02] hover:bg-white/[0.04]'}`}>
              <div className="flex items-start justify-between mb-3">
                <div className="flex items-center gap-3">
                  <div className={`w-8 h-8 rounded-lg border flex items-center justify-center ${enabled ? 'bg-[#853694]/10 border-[#853694]/30' : 'bg-white/5 border-white/10'}`}>
                    <Wrench size={14} className={enabled ? 'text-[#b72e6a]' : 'text-white/40'} />
                  </div>
                  <div>
                    <p className="text-sm font-bold text-white">{tool.name}</p>
                    <p className="text-[10px] text-white/30 uppercase tracking-wider">{tool.category}</p>
                  </div>
                </div>
                <button onClick={() => toggle(tool.name)} className={`w-9 h-5 rounded-full border transition-all ${enabled ? 'bg-[#853694] border-[#853694]' : 'bg-white/5 border-white/10'}`}>
                  <div className={`w-3.5 h-3.5 rounded-full bg-white shadow m-0.5 transition-all ${enabled ? 'translate-x-4' : 'translate-x-0'}`} />
                </button>
              </div>
              <p className="text-xs text-white/40">{tool.desc}</p>
            </motion.div>
          );
        })}
      </div>
      <AnimatePresence>
        {showAddTool && (
          <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
            className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-6"
            onClick={e => { if (e.target === e.currentTarget) setShowAddTool(false); }}>
            <motion.div initial={{ scale: 0.95 }} animate={{ scale: 1 }} exit={{ scale: 0.95 }}
              className="bg-[#0e0e1a] border border-white/10 rounded-2xl p-7 w-full max-w-md">
              <div className="flex items-center justify-between mb-5">
                <h3 className="text-base font-outfit font-bold text-white">Add Tool</h3>
                <button onClick={() => setShowAddTool(false)} className="w-7 h-7 rounded-full bg-white/5 flex items-center justify-center hover:bg-white/10"><X size={13} className="text-white/50" /></button>
              </div>
              <p className="text-xs text-white/40 mb-4">Connect a new tool to this agent. Scoped access will be auto-configured.</p>
              {['REST API Connector', 'GraphQL Endpoint', 'Webhook Trigger', 'Database Query', 'File Storage'].map(t => (
                <button key={t} onClick={() => { setAddedMsg(`${t} added successfully!`); setShowAddTool(false); setTimeout(() => setAddedMsg(''), 3000); }}
                  className="w-full text-left px-4 py-3 mb-2 rounded-xl border border-white/8 bg-white/[0.02] text-sm text-white/60 hover:border-[#853694]/30 hover:text-white hover:bg-[#853694]/5 transition-all">
                  {t}
                </button>
              ))}
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};

// ─────────────────────────────────────────────
// Stats bar
// ─────────────────────────────────────────────
const StatsBar = ({ config }: { config: typeof AGENT_CONFIG[string] }) => (
  <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
    {config.stats.map((stat, i) => (
      <motion.div key={stat.label} initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.07 }}
        className="rounded-2xl border border-white/5 bg-white/[0.02] p-5">
        <p className="text-xs text-white/30 mb-1">{stat.label}</p>
        <p className="text-2xl font-bold text-white tracking-tight">{stat.value}</p>
        <p className={`text-xs font-semibold mt-1 ${stat.up ? 'text-purple-400' : 'text-rose-400'}`}>{stat.delta} this month</p>
      </motion.div>
    ))}
  </div>
);

// ─────────────────────────────────────────────
// Sidebar
// ─────────────────────────────────────────────
const SIDEBAR_TOP = [
  { icon: User, label: 'Profile' as SidebarSection },
  { icon: MessageSquare, label: 'Chat' as SidebarSection },
  { icon: BookOpen, label: 'Knowledge' as SidebarSection },
  { icon: WrapText, label: 'Forms' as SidebarSection },
];
const BUILDER_CHILDREN: { icon: React.ElementType; label: BuilderTab }[] = [
  { icon: Bot, label: 'Agents' }, { icon: GitBranch, label: 'Workflows' },
  { icon: Wrench, label: 'My Tools' }, { icon: LayoutGrid, label: 'All Tools' },
  { icon: Puzzle, label: 'Widgets' },
];
const BOTTOM_ITEMS = [
  { icon: Rocket, label: 'Deployments' }, { icon: BarChart2, label: 'Analytics' }, { icon: Settings, label: 'Settings' },
];

const Sidebar = ({ section, setSection, builderTab, setBuilderTab }:
  { section: SidebarSection; setSection: (s: SidebarSection) => void; builderTab: BuilderTab; setBuilderTab: (t: BuilderTab) => void }) => {
  const [builderOpen, setBuilderOpen] = useState(true);
  return (
    <aside className="w-[220px] shrink-0 min-h-full bg-[#080810]/80 backdrop-blur-xl border-r border-white/5 flex flex-col py-6 gap-1 z-10">
      {SIDEBAR_TOP.map(item => (
        <button key={item.label} onClick={() => setSection(item.label)}
          className={`flex items-center gap-3 px-5 py-2.5 text-sm rounded-lg mx-2 transition-all text-left ${section === item.label ? 'bg-[#853694]/10 text-[#b72e6a] font-semibold' : 'text-white/40 hover:text-white/80 hover:bg-white/5'}`}>
          <item.icon size={16} /> {item.label}
        </button>
      ))}
      <div className="mx-2 mt-1">
        <button onClick={() => { setBuilderOpen(o => !o); setSection('Builder'); }}
          className={`w-full flex items-center gap-3 px-3 py-2.5 text-sm rounded-lg transition-all ${section === 'Builder' ? 'text-white font-semibold' : 'text-white/50 hover:text-white hover:bg-white/5'}`}>
          <Code2 size={16} /><span className="flex-1 text-left">Builder</span>
          {builderOpen ? <ChevronDown size={14} /> : <ChevronRight size={14} />}
        </button>
        <AnimatePresence>
          {builderOpen && (
            <motion.div initial={{ height: 0, opacity: 0 }} animate={{ height: 'auto', opacity: 1 }} exit={{ height: 0, opacity: 0 }} transition={{ duration: 0.2 }} className="overflow-hidden pl-4 mt-1 space-y-0.5">
              {BUILDER_CHILDREN.map(child => (
                <button key={child.label} onClick={() => { setSection('Builder'); setBuilderTab(child.label); }}
                  className={`w-full flex items-center gap-3 px-3 py-2.5 text-sm rounded-lg transition-all text-left ${section === 'Builder' && builderTab === child.label ? 'bg-[#853694]/10 text-[#b72e6a] font-semibold border-l-2 border-[#853694]' : 'text-white/40 hover:text-white/80 hover:bg-white/5'}`}>
                  <child.icon size={14} /> {child.label}
                </button>
              ))}
            </motion.div>
          )}
        </AnimatePresence>
      </div>
      <div className="flex-1" />
      {BOTTOM_ITEMS.map(item => (
        <button key={item.label} onClick={() => { const el = document.getElementById('dash-toast'); if (el) { el.textContent = `${item.label} — coming soon`; el.className = 'fixed bottom-6 right-6 z-50 px-5 py-2.5 rounded-full bg-[#1a1a2e] border border-[#853694]/40 text-sm font-semibold text-white shadow-xl'; setTimeout(() => { if (el) el.className = 'fixed bottom-6 right-6 z-50 hidden'; }, 2500); }}} className="flex items-center gap-3 px-5 py-2.5 text-sm text-white/30 hover:text-white/70 hover:bg-white/5 rounded-lg mx-2 transition-all text-left">
          <item.icon size={16} /> {item.label}
        </button>
      ))}
    </aside>
  );
};

// ─────────────────────────────────────────────
// Main Dashboard
// ─────────────────────────────────────────────
const AgentDashboard = () => {
  const { agentId } = useParams<{ agentId: string }>();
  const [section, setSection] = useState<SidebarSection>('Builder');
  const [builderTab, setBuilderTab] = useState<BuilderTab>('Agents');

  const config = agentId ? AGENT_CONFIG[agentId] : null;
  if (!config) {
    return (
      <main className="pt-24 pb-20 px-8 min-h-screen flex flex-col items-center justify-center relative z-10">
        <AlertCircle size={48} className="text-white/20 mb-4" />
        <h1 className="text-2xl font-bold text-white mb-2">Agent Not Found</h1>
        <p className="text-white/40 mb-6">The agent <code className="text-[#b72e6a] bg-[#853694]/10 px-2 py-0.5 rounded">{agentId}</code> doesn't exist.</p>
        <Link to="/agents" className="flex items-center gap-2 px-6 py-2.5 rounded-full bg-[#853694] text-white font-bold text-sm hover:bg-[#6a2b77] transition-all">
          <ChevronLeft size={14} /> Back to Agents
        </Link>
      </main>
    );
  }

  const TabIcon = config.icon;

  const renderSection = () => {
    if (section === 'Chat') return <ChatTab config={config} />;
    if (section === 'Knowledge') return <KnowledgeTab config={config} />;
    if (section === 'Forms') return <FormsTab config={config} />;
    if (section === 'Profile') return (
      <div className="space-y-6">
        <div className="rounded-2xl border border-white/5 bg-white/[0.02] p-6">
          <h3 className="text-sm font-bold text-white mb-4">Agent Profile</h3>
          <div className="space-y-3 text-sm">
            {[['Name', `${config.title} · ${config.subtitle}`], ['Model', config.model], ['Status', config.status], ['Badge', config.badge], ['Sub-agents', config.subAgents.length.toString()], ['Workflows', config.workflows.length.toString()]].map(([k, v]) => (
              <div key={k} className="flex items-center justify-between py-2 border-b border-white/5">
                <span className="text-white/40">{k}</span><span className="text-white font-semibold">{v}</span>
              </div>
            ))}
          </div>
        </div>
        <div className="rounded-2xl border border-white/5 bg-white/[0.02] p-6">
          <h3 className="text-sm font-bold text-white mb-2">Description</h3>
          <p className="text-sm text-white/50 leading-relaxed">{config.description}</p>
        </div>
      </div>
    );
    // Builder: render builder tab content
    switch (builderTab) {
      case 'Agents': return <AgentsTab config={config} />;
      case 'Workflows': return <WorkflowsTab config={config} />;
      case 'My Tools': case 'All Tools': return <ToolsTab config={config} />;
      case 'Widgets': return <WidgetsTab config={config} />;
    }
  };

  const sectionLabel = section === 'Builder' ? builderTab : section;

  return (
    <main className="pt-[72px] min-h-screen flex relative z-10">
      {/* Global toast */}
      <div id="dash-toast" className="fixed bottom-6 right-6 z-50 hidden" />

      <Sidebar section={section} setSection={setSection} builderTab={builderTab} setBuilderTab={setBuilderTab} />
      <div className="flex-1 overflow-auto">
        {/* Header */}
        <div className={`bg-gradient-to-r ${config.color} border-b border-white/5 px-8 py-6`}>
          <div className="flex items-start justify-between">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-2xl bg-white/5 border border-white/10 flex items-center justify-center backdrop-blur-sm">
                <TabIcon size={22} className="text-white/80" />
              </div>
              <div>
                <div className="flex items-center gap-3 mb-1">
                  <p className="text-xs font-bold uppercase tracking-widest text-white/40">{config.badge}</p>
                  <StatusBadge status={config.status} />
                </div>
                <h1 className="text-xl font-bold text-white tracking-tight leading-none">
                  {config.title} <span className="font-light text-white/60">· {config.subtitle}</span>
                </h1>
                <div className="flex items-center gap-2 mt-1.5">
                  <span className="text-xs text-white/30 flex items-center gap-1"><Cpu size={10} /> {config.model}</span>
                  <span className="text-white/10">·</span>
                  <span className="text-xs text-white/30 flex items-center gap-1"><Users size={10} /> {config.subAgents.length} sub-agents</span>
                  <span className="text-white/10">·</span>
                  <span className="text-xs text-white/30 flex items-center gap-1"><Activity size={10} /> {config.workflows.length} workflows</span>
                </div>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <button onClick={() => { const el = document.getElementById('dash-toast'); if (el) { el.textContent = '✓ Sync complete — knowledge sources refreshed'; el.className = 'fixed bottom-6 right-6 z-50 px-5 py-2.5 rounded-full bg-[#1a1a2e] border border-[#853694]/40 text-sm font-semibold text-white shadow-xl transition-all'; setTimeout(() => { if (el) el.className = 'fixed bottom-6 right-6 z-50 hidden'; }, 3000); }}} className="flex items-center gap-2 px-4 py-2 rounded-full bg-white/5 border border-white/10 text-white/70 text-xs font-semibold hover:bg-white/10 hover:text-white transition-all"><RefreshCw size={12} /> Sync</button>
              <button onClick={() => { const el = document.getElementById('dash-toast'); if (el) { el.textContent = '🚀 Deploy initiated — agent going live'; el.className = 'fixed bottom-6 right-6 z-50 px-5 py-2.5 rounded-full bg-[#1a1a2e] border border-[#853694]/40 text-sm font-semibold text-white shadow-xl transition-all'; setTimeout(() => { if (el) el.className = 'fixed bottom-6 right-6 z-50 hidden'; }, 3000); }}} className="flex items-center gap-2 px-4 py-2 rounded-full bg-white/5 border border-white/10 text-white/70 text-xs font-semibold hover:bg-white/10 hover:text-white transition-all"><ExternalLink size={12} /> Deploy</button>
              <button onClick={() => { const el = document.getElementById('dash-toast'); if (el) { el.textContent = '⚡ Agent is now running'; el.className = 'fixed bottom-6 right-6 z-50 px-5 py-2.5 rounded-full bg-[#853694]/90 border border-[#853694] text-sm font-semibold text-white shadow-xl transition-all'; setTimeout(() => { if (el) el.className = 'fixed bottom-6 right-6 z-50 hidden'; }, 3000); }}} className="flex items-center gap-2 px-4 py-2 rounded-full bg-[#853694] hover:bg-[#6a2b77] text-white text-xs font-bold transition-all shadow-[0_0_15px_rgba(133,54,148,0.3)]"><Play size={12} /> Run Agent</button>
            </div>
          </div>
        </div>

        {/* Content */}
        <div className="px-8 py-6">
          <StatsBar config={config} />

          {/* Builder tab strip - only show when in Builder section */}
          {section === 'Builder' && (
            <div className="flex items-center gap-1 mb-6 border-b border-white/5">
              {(['Agents', 'Workflows', 'My Tools', 'All Tools', 'Widgets'] as BuilderTab[]).map(tab => (
                <button key={tab} onClick={() => setBuilderTab(tab)}
                  className={`px-4 py-2.5 text-sm font-semibold transition-all relative -mb-px ${builderTab === tab ? 'text-white border-b-2 border-[#853694]' : 'text-white/40 hover:text-white/70'}`}>
                  {tab}
                </button>
              ))}
            </div>
          )}

          <AnimatePresence mode="wait">
            <motion.div key={`${section}-${builderTab}`} initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -8 }} transition={{ duration: 0.18 }}>
              {renderSection()}
            </motion.div>
          </AnimatePresence>
        </div>
      </div>
    </main>
  );
};

export default AgentDashboard;
