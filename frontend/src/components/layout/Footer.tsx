import { Link } from 'react-router-dom';

const Footer = () => (
  <footer className="bg-[#050508] py-16 px-6 md:px-16 relative z-10 text-white/60 border-t border-white/5 overflow-hidden">
    <div className="max-w-[1200px] mx-auto flex flex-col md:flex-row justify-between gap-16 relative">
      {/* Brand & Copyright */}
      <div className="flex flex-col gap-6 w-full md:w-1/4">
        <div className="flex items-center gap-3">
          <img src="/Juvi-logo.png" alt="JuviAI" className="h-8 object-contain" />
          <span className="text-xl font-bold text-white font-outfit">JuviAI</span>
        </div>
        <span className="text-white/40 text-[13px] font-medium">© 2026 JuviAI - All rights reserved.</span>
      </div>

      {/* Links Grid */}
      <div className="flex-1 grid grid-cols-2 md:grid-cols-4 gap-8 md:gap-4 relative z-10">
        <div>
          <h4 className="font-semibold mb-6 text-sm text-white tracking-wide">Product</h4>
          <ul className="space-y-4 text-[13px] font-medium text-white/50">
            <li><Link to="/pricing" className="hover:text-[#b72e6a] transition-colors">Pricing & Plans</Link></li>
          </ul>
        </div>
        <div>
          <h4 className="font-semibold mb-6 text-sm text-white tracking-wide">Company</h4>
          <ul className="space-y-4 text-[13px] font-medium text-white/50">
            <li><Link to="/about" className="hover:text-[#b72e6a] transition-colors">About us</Link></li>
            <li><Link to="/careers" className="hover:text-[#b72e6a] transition-colors">Careers</Link></li>
          </ul>
        </div>
        <div>
          <h4 className="font-semibold mb-6 text-sm text-white tracking-wide">Resources</h4>
          <ul className="space-y-4 text-[13px] font-medium text-white/50">
            <li><Link to="/terms" className="hover:text-[#b72e6a] transition-colors">Terms of service</Link></li>
            <li><Link to="/cookies" className="hover:text-[#b72e6a] transition-colors">Cookie Policy</Link></li>
            <li><Link to="/faq" className="hover:text-[#b72e6a] transition-colors">FAQ</Link></li>
            <li><Link to="/privacy" className="hover:text-[#b72e6a] transition-colors">Privacy Policy</Link></li>
          </ul>
        </div>
        <div>
          <h4 className="font-semibold mb-6 text-sm text-white tracking-wide">Social</h4>
          <div className="flex gap-4 items-center">
            <a href="#" className="w-8 h-8 rounded-full bg-white/5 flex items-center justify-center text-white/60 hover:bg-[#853694] hover:text-white transition-all" title="X">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><line x1="4" x2="20" y1="4" y2="20" /><line x1="20" x2="4" y1="4" y2="20" /></svg>
            </a>
            <a href="#" className="w-8 h-8 rounded-full bg-white/5 flex items-center justify-center text-white/60 hover:bg-[#853694] hover:text-white transition-all" title="Instagram">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><rect width="20" height="20" x="2" y="2" rx="5" ry="5" /><path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z" /><line x1="17.5" x2="17.51" y1="6.5" y2="6.5" /></svg>
            </a>
            <a href="#" className="w-8 h-8 rounded-full bg-white/5 flex items-center justify-center text-white/60 hover:bg-[#853694] hover:text-white transition-all" title="YouTube">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M2.5 17a24.12 24.12 0 0 1 0-10 2 2 0 0 1 1.4-1.4 49.56 49.56 0 0 1 16.2 0A2 2 0 0 1 21.5 7a24.12 24.12 0 0 1 0 10 2 2 0 0 1-1.4 1.4 49.55 49.55 0 0 1-16.2 0A2 2 0 0 1 2.5 17" /><path d="m10 15 5-3-5-3z" /></svg>
            </a>
            <a href="#" className="w-8 h-8 rounded-full bg-white/5 flex items-center justify-center text-white/60 hover:bg-[#853694] hover:text-white transition-all" title="LinkedIn">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M16 8a6 6 0 0 1 6 6v7h-4v-7a2 2 0 0 0-2-2 2 2 0 0 0-2 2v7h-4v-7a6 6 0 0 1 6-6z" /><rect width="4" height="12" x="2" y="9" /><circle cx="4" cy="4" r="2" /></svg>
            </a>
          </div>
        </div>
      </div>
    </div>
  </footer>
);

export default Footer;
