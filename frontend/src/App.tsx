import { Routes, Route } from 'react-router-dom';
import AnimatedWaves from './components/layout/AnimatedWaves';
import Navbar from './components/layout/Navbar';
import Footer from './components/layout/Footer';
import ScrollToTop from './components/ScrollToTop';
import LandingPage from './pages/LandingPage';
import AgentsPage from './pages/AgentsPage';
import CapabilityPage from './pages/CapabilityPage';
import AgentDashboard from './pages/AgentDashboard';
import PlatformPage from './pages/PlatformPage';
import CapabilitiesPage from './pages/CapabilitiesPage';
import GovernancePage from './pages/GovernancePage';
import SecurityScalePage from './pages/SecurityScalePage';

const App = () => {
  return (
    <div className="min-h-screen relative overflow-hidden bg-background flex flex-col font-sans text-foreground">
      {/* Background Animated Waves (shared across all pages) */}
      <AnimatedWaves />

      <ScrollToTop />
      <Navbar />

      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/agents" element={<AgentsPage />} />
        <Route path="/platform" element={<PlatformPage />} />
        <Route path="/platform/capabilities" element={<CapabilitiesPage />} />
        <Route path="/platform/governance-handover" element={<GovernancePage />} />
        <Route path="/platform/security-scale" element={<SecurityScalePage />} />
        <Route path="/capability/:id" element={<CapabilityPage />} />
        <Route path="/solutions/:agentId" element={<AgentDashboard />} />
        <Route path="*" element={<LandingPage />} />
      </Routes>

      <Footer />

      {/* Bottom right decorative icon */}
      <div className="fixed bottom-8 right-8 opacity-20 z-50 pointer-events-none">
        <img src="/intertec-logo.png" alt="Decor" className="w-12 h-12 object-contain" />
      </div>
    </div>
  );
};

export default App;
