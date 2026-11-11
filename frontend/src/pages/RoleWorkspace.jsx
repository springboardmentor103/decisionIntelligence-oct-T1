export default function RoleWorkspace({ title, description }) {
  return (
    <div className="page-block">
      <div className="section-header">
        <div>
          <p className="eyebrow">Role workspace</p>
          <h1>{title}</h1>
          <p className="auth-subtitle">{description}</p>
        </div>
      </div>
      <section className="panel-card">
        This area is a placeholder for later milestone features.
      </section>
    </div>
  )
}
