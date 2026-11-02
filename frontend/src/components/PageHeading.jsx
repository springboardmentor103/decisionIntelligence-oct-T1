export default function PageHeading({ eyebrow, title, description, actions, className = '' }) {
  return (
    <div className={`page-heading ${className}`.trim()}>
      <div>
        {eyebrow ? <p className="eyebrow">{eyebrow}</p> : null}
        <h1>{title}</h1>
        {description ? <p className="page-description">{description}</p> : null}
      </div>
      {actions ? <div className="page-heading-actions">{actions}</div> : null}
    </div>
  )
}
