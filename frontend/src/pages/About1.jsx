import { Link } from "react-router-dom";
import {
  Target,
  Users,
  Lightbulb,
  ShieldCheck,
  HeartHandshake,
  Eye,
} from "lucide-react";

import { Footer } from "./Home";
import "./Home.css";

function About1() {
  return (
    <div className="home-page">

      <nav className="navbar">
        <Link to="/" className="dh-logo">
  <span className="dh-logo-icon">D</span>
  <span className="dh-logo-text">
    Decision<span>Hub</span>
  </span></Link>

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

      <section className="page-hero">
        <span className="tagline">ABOUT US</span>
        <h1>About DecisionHub</h1>
        <p>
          Bringing people together to make smarter, clearer,
          and more informed decisions.
        </p>
      </section>

      <section className="about-section">
        <div className="about-content">
          <span className="tagline">WHO WE ARE</span>
          <h2>Decisions Are Better When We Make Them Together</h2>
          <p>
            DecisionHub is a collaborative decision-making platform
            designed to help individuals and communities make informed
            decisions together.
          </p>
          <p>
            Our platform allows users to create decisions, compare
            alternatives, evaluate options, collaborate with others,
            and participate in voting.
          </p>
        </div>

        <div className="about-visual">
          <Users size={65} />
          <h3>One Platform. Better Decisions.</h3>
          <p>Connect, collaborate and choose together.</p>
        </div>
      </section>

      <section className="about-section mission-section">
        <div className="about-info-card">
          <Target size={35} />
          <h2>Our Mission</h2>
          <p>
            To make decision-making easier, smarter, and more collaborative
            by bringing people and ideas together.
          </p>
        </div>

        <div className="about-info-card">
          <Eye size={35} />
          <h2>Our Vision</h2>
          <p>
            To create a platform where every decision can be explored,
            discussed, evaluated, and understood transparently.
          </p>
        </div>
      </section>

      <section className="about-what">
        <div className="section-title">
          <span className="tagline">OUR PURPOSE</span>
          <h2>What We Do</h2>
          <p>We provide tools that support collaborative decisions.</p>
        </div>

        <div className="feature-grid">
          <Feature icon={<Lightbulb />} title="Create Decisions"
            text="Help users create and organize important decisions." />

          <Feature icon={<Users />} title="Collaborate"
            text="Encourage community discussions and idea sharing." />

          <Feature icon={<HeartHandshake />} title="Collect Votes"
            text="Allow community members to express their preferences." />

          <Feature icon={<ShieldCheck />} title="Transparency"
            text="Support clear and informed decision-making." />
        </div>
      </section>

      <section className="cta-section">
        <h2>Let's Make Better Decisions Together</h2>
        <p>Start exploring the possibilities with DecisionHub.</p>
        <Link to="/register" className="primary-btn">
          Join DecisionHub
        </Link>
      </section>

      <Footer />
    </div>
  );
}

function Feature({ icon, title, text }) {
  return (
    <div className="feature-card">
      <div className="feature-icon">{icon}</div>
      <h3>{title}</h3>
      <p>{text}</p>
    </div>
  );
}

export default About1;