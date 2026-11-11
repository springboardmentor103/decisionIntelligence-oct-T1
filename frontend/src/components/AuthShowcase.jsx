export default function AuthShowcase() {
  return (
    <aside className="auth-showcase">
      <a className="auth-brand" href="/login">
        <span className="brand-mark">D<span>!</span></span>
        <span>Decision<span className="brand-accent">Hub</span></span>
      </a>
      <div className="auth-showcase-content">
        <h2>Make Better Decisions <span>Together.</span></h2>
        <p>Turn ideas into informed decisions.</p>
        <svg className="decision-illustration" viewBox="0 0 390 260" role="img" aria-label="People collaboratively comparing decision options">
          <path d="M66 112 177 155M69 215 177 155M217 155 281 61M217 155 300 154M217 155 274 225" className="illustration-path" />
          <circle cx="191" cy="155" r="44" className="illustration-orbit" />
          <circle cx="191" cy="155" r="34" className="illustration-center" />
          <text x="191" y="168" textAnchor="middle" className="illustration-logo">D!</text>
          <g className="illustration-person illustration-person--teal"><circle cx="43" cy="96" r="20" /><circle cx="43" cy="91" r="7" /><path d="M30 109c1-10 25-10 26 0" /></g>
          <g className="illustration-person illustration-person--amber"><circle cx="58" cy="218" r="20" /><circle cx="58" cy="213" r="7" /><path d="M45 231c1-10 25-10 26 0" /></g>
          <g className="illustration-person illustration-person--pink"><circle cx="337" cy="222" r="20" /><circle cx="337" cy="217" r="7" /><path d="M324 235c1-10 25-10 26 0" /></g>
          <g className="illustration-option" transform="translate(252 24)"><rect width="100" height="52" rx="11" /><text x="14" y="20">Option A</text><rect x="14" y="32" width="72" height="7" rx="4" className="option-progress" /></g>
          <g className="illustration-option" transform="translate(270 128)"><rect width="100" height="52" rx="11" /><text x="14" y="20">Option B</text><rect x="14" y="32" width="60" height="7" rx="4" className="option-progress option-progress--navy" /></g>
          <g className="illustration-option" transform="translate(242 202)"><rect width="100" height="52" rx="11" /><text x="14" y="20">Option C</text><rect x="14" y="32" width="43" height="7" rx="4" className="option-progress option-progress--navy" /></g>
        </svg>
      </div>
      <p className="auth-copyright">© 2026 DecisionHub</p>
    </aside>
  )
}
