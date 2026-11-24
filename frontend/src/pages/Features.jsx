import { Link } from "react-router-dom";
import {
  Lightbulb,
  GitCompareArrows,
  Target,
  Users,
  Vote,
  BarChart3,
  Bell,
  ShieldCheck,
} from "lucide-react";

//import { Footer } from "./Home";
import "./Home.css";

const features = [
  {
    icon: <Lightbulb />,
    title: "Decision Creation",
    description:
      "Create and manage decisions by describing problems and objectives.",
  },
  {
    icon: <GitCompareArrows />,
    title: "Option Comparison",
    description:
      "Compare multiple alternatives to understand their differences.",
  },
  {
    icon: <Target />,
    title: "Criteria Evaluation",
    description:
      "Evaluate available options using selected criteria.",
  },
  {
    icon: <Users />,
    title: "Community Collaboration",
    description:
      "Discuss decisions, exchange ideas, and collaborate with others.",
  },
  {
    icon: <Vote />,
    title: "Voting System",
    description:
      "Allow community members to vote for their preferred options.",
  },
  {
    icon: <BarChart3 />,
    title: "Results & Analytics",
    description:
      "View voting outcomes and decision summaries.",
  },
  {
    icon: <Bell />,
    title: "Notifications",
    description:
      "Stay updated about decision activities and community interactions.",
  },
  {
    icon: <ShieldCheck />,
    title: "User Roles",
    description:
      "Support different access levels for users, moderators, and administrators.",
  },
];

function Features() {
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
        <span className="tagline">OUR FEATURES</span>
        <h1>Everything You Need to Decide Better</h1>
        <p>
          Explore the tools that make decision-making
          collaborative, transparent, and effective.
        </p>
      </section>

      <section className="features features-page">
        <div className="feature-grid">
          {features.map((feature, index) => (
            <div className="feature-card" key={index}>
              <div className="feature-icon">{feature.icon}</div>
              <h3>{feature.title}</h3>
              <p>{feature.description}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="cta-section">
        <h2>Start Making Smarter Decisions</h2>
        <p>Bring your ideas and community together with DecisionHub.</p>
        <Link to="/register" className="primary-btn">
          Get Started
        </Link>
      </section>

      {/* <Footer /> */}
    </div>
  );
}

export default Features;
/*function Features() {
  return <h1>DecisionHub Features Page</h1>;
}

export default Features;*/