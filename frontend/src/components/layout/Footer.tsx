import { Link } from 'react-router-dom';

const Footer = () => (
  <footer className="bg-white py-20 px-6 md:px-16 relative z-10 text-slate-800 border-t border-slate-200">
    <div className="max-w-[1200px] mx-auto flex flex-col md:flex-row justify-between gap-16">
      {/* Brand & Copyright */}
      <div className="flex flex-col gap-6 w-full md:w-1/4">
        <div className="flex items-center gap-3">
          <img src="/intertec-logo.png" alt="Intertec" className="h-10 object-contain w-10 filter invert brightness-0" />
        </div>
        <span className="text-slate-500 text-[13px] font-medium">© 2026 Intertec AIOps - All rights reserved.</span>

        {/* Large subtle watermark text matching screenshot */}
        <div className="absolute bottom-0 left-1/2 -translate-x-1/2 text-[14vw] font-outfit font-extrabold text-slate-50 pointer-events-none leading-none select-none tracking-tighter">
          Intertec
        </div>
      </div>

      {/* Links Grid */}
      <div className="flex-1 grid grid-cols-2 md:grid-cols-4 gap-8 md:gap-4 relative z-10">
        <div>
          <h4 className="font-semibold mb-6 text-sm tracking-wide">Product</h4>
          <ul className="space-y-4 text-[13px] font-medium text-slate-500">
            <li><Link to="/pricing" className="hover:text-accentTheme transition-colors">Pricing & Plans</Link></li>
          </ul>
        </div>
        <div>
          <h4 className="font-semibold mb-6 text-sm tracking-wide">Company</h4>
          <ul className="space-y-4 text-[13px] font-medium text-slate-500">
            <li><Link to="/about" className="hover:text-accentTheme transition-colors">About us</Link></li>
            <li><Link to="/careers" className="hover:text-accentTheme transition-colors">Careers</Link></li>
          </ul>
        </div>
        <div>
          <h4 className="font-semibold mb-6 text-sm tracking-wide">Resources</h4>
          <ul className="space-y-4 text-[13px] font-medium text-slate-500">
            <li><Link to="/terms" className="hover:text-accentTheme transition-colors">Terms of service</Link></li>
            <li><Link to="/cookies" className="hover:text-accentTheme transition-colors">Cookie Policy</Link></li>
            <li><Link to="/faq" className="hover:text-accentTheme transition-colors">FAQ</Link></li>
            <li><Link to="/privacy" className="hover:text-accentTheme transition-colors">Privacy Policy</Link></li>
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

export default Footer;
