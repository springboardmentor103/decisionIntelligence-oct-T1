import { Link } from "react-router-dom";
import {
  Users,
  GitCompareArrows,
  Vote,
  Target,
  Lightbulb,
  BarChart3,
  ArrowRight,
  CheckCircle2,
  MessageCircle,
  Bell,
  ShieldCheck,
} from "lucide-react";

import "./Home.css";

function Home() {
  return (
    <div className="home-page">

      {/* NAVBAR */}
      <nav className="navbar">
        <Link to="/" className="dh-logo">
  <span className="dh-logo-icon">D</span>
  <span className="dh-logo-text">
    Decision<span>Hub</span>
  </span>
</Link>

        <div className="nav-links">
          <Link to="/">Home</Link>
          <Link to="/about">About</Link>
          <Link to="/features">Features</Link>
        </div>

        <div className="nav-actions">
          <Link to="/login" className="login-link">Login</Link>
          <Link to="/register" className="nav-button">Get Started</Link>
        </div>
      </nav>

      {/* HERO SECTION */}
      <section className="hero">
        <div className="hero-content">
          <span className="tagline">
            .Collaborative Decision Intelligence
          </span>

          <h1>
            Make Better Decisions <span>Together.</span>
          </h1>

          <p>
            DecisionHub helps individuals and communities make smarter
            decisions by comparing alternatives, evaluating options,
            and collaborating with others.
          </p>

          <div className="hero-buttons">
            <Link to="/register" className="primary-btn">
              Get Started <ArrowRight size={18} />
            </Link>

            <Link to="/features" className="secondary-btn">
              Explore Features
            </Link>
          </div>
        </div>
      </section>
     
        
 {/* FEATURES PREVIEW */}
      <section className="features">
        <div className="section-title">
          <span className="tagline">WHAT WE OFFER
          </span>
          <h2>Everything You Need to Decide Better</h2>
          <p>Powerful tools that make collaborative decisions easier.</p>
        </div>

        <div className="feature-grid">
          <FeatureCard
            icon={<Lightbulb />}
            title="Decision Creation"
            description="Create and manage decisions with clear objectives."
          />

          <FeatureCard
            icon={<GitCompareArrows />}
            title="Option Comparison"
            description="Compare alternatives and evaluate different options."
          />

          <FeatureCard
            icon={<Users />}
            title="Community Collaboration"
            description="Share ideas and collaborate with your community."
          />

          <FeatureCard
            icon={<Vote />}
            title="Voting System"
            description="Collect votes and discover community preferences."
          />
        </div>

        <div className="center-button">
          <Link to="/features" className="primary-btn">
            View All Features <ArrowRight size={18} />
          </Link>
        </div>
      </section>
      {/* WORKFLOW */}
      <section className="workflow">
        <div className="section-title">
          <span className="tagline">SIMPLE PROCESS</span>
          <h2>A Simple Process Behind Better Decisions</h2>
          <p>Three simple steps to make informed decisions together.</p>
        </div>

        <div className="workflow-grid">

          <div className="workflow-card">
            <span className="step-number">01</span>
            <Lightbulb size={32} />
            <h3>Create a Decision</h3>
            <p>
              Create a decision and describe the problem you want to solve.
            </p>
          </div>

          <div className="workflow-card">
            <span className="step-number">02</span>
            <GitCompareArrows size={32} />
            <h3>Compare & Collaborate</h3>
            <p>
              Add alternatives, evaluate options using criteria,
              and discuss them with the community.
            </p>
          </div>

          <div className="workflow-card">
            <span className="step-number">03</span>
            <BarChart3 size={32} />
            <h3>Vote & Discover Results</h3>
            <p>
              Vote for your preferred options and view the final
              decision results.
            </p>
          </div>

        </div>
      </section>

      {/* CALL TO ACTION */}
      <section className="cta-section">
        <h2>Ready to Make Better Decisions?</h2>
        <p>Bring people together and turn ideas into informed decisions.</p>
        <Link to="/register" className="primary-btn">
          Get Started Today <ArrowRight size={18} />
        </Link>
      </section>

      {/* FOOTER */}
      <Footer />

    </div>
  );
}

function FeatureCard({ icon, title, description }) {
  return (
    <div className="feature-card">
      <div className="feature-icon">{icon}</div>
      <h3>{title}</h3>
      <p>{description}</p>
    </div>
  );
}

export function Footer() {
  return (
    <footer className="footer">
      <div className="footer-brand">
        <Link to="/" className="logo footer-logo">
          <span className="logo-icon">DH</span>
          <span>DecisionHub</span>
        </Link>
        <p>Make Better Decisions Together.</p>
      </div>

      <div className="footer-links">
        <Link to="/">Home</Link>
        <Link to="/about">About</Link>
        <Link to="/features">Features</Link>
        <Link to="/login">Login</Link>
      </div>

      <p className="copyright">
        © 2026 DecisionHub. All rights reserved.
      </p>
    </footer>
  );
}

export default Home;
