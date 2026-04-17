import { motion } from 'framer-motion';

const AnimatedWaves = () => (
  <div className="fixed inset-0 z-0 pointer-events-none overflow-hidden opacity-60">
    <motion.svg className="absolute w-[200%] h-full top-0 left-0" viewBox="0 0 3200 800" preserveAspectRatio="none"
      animate={{ x: ["0%", "-50%"] }}
      transition={{ duration: 25, repeat: Infinity, ease: "linear" }}
    >
      <path d="M0,400 C400,100 400,700 800,400 S1200,700 1600,400 S2000,100 2400,400 S2800,700 3200,400" fill="none" stroke="rgba(133, 54, 148, 0.4)" strokeWidth="1.5" />
    </motion.svg>
    <motion.svg className="absolute w-[200%] h-full top-0 left-0" viewBox="0 0 3200 800" preserveAspectRatio="none"
      animate={{ x: ["-50%", "0%"] }}
      transition={{ duration: 30, repeat: Infinity, ease: "linear" }}
    >
      <path d="M0,500 C400,300 400,700 800,500 S1200,700 1600,500 S2000,300 2400,500 S2800,700 3200,500" fill="none" stroke="rgba(183, 46, 106, 0.2)" strokeWidth="2" />
    </motion.svg>
    <motion.svg className="absolute w-[200%] h-full top-0 left-0" viewBox="0 0 3200 800" preserveAspectRatio="none"
      animate={{ x: ["0%", "-50%"] }}
      transition={{ duration: 45, repeat: Infinity, ease: "linear" }}
    >
      <path d="M0,300 C400,500 400,100 800,300 S1200,100 1600,300 S2000,500 2400,300 S2800,100 3200,300" fill="none" stroke="rgba(250, 154, 46, 0.3)" strokeWidth="1" />
    </motion.svg>
    <motion.svg className="absolute w-[200%] h-full top-0 left-0" viewBox="0 0 3200 800" preserveAspectRatio="none"
      animate={{ x: ["-50%", "0%"] }}
      transition={{ duration: 35, repeat: Infinity, ease: "linear" }}
    >
      <path d="M0,600 C400,300 400,900 800,600 S1200,900 1600,600 S2000,300 2400,600 S2800,900 3200,600" fill="none" stroke="rgba(212, 38, 56, 0.15)" strokeWidth="3" />
    </motion.svg>
    <motion.svg className="absolute w-[200%] h-full top-0 left-0" viewBox="0 0 3200 800" preserveAspectRatio="none"
      animate={{ x: ["0%", "-50%"] }}
      transition={{ duration: 28, repeat: Infinity, ease: "linear" }}
    >
      <path d="M0,200 C300,400 500,400 800,200 S1300,0 1600,200 S1900,400 2400,200 S2900,0 3200,200" fill="none" stroke="rgba(142, 54, 148, 0.25)" strokeWidth="1.5" />
    </motion.svg>
  </div>
);

export default AnimatedWaves;
