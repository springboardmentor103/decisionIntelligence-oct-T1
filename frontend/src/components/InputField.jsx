export default function InputField({
  label,
  id,
  name,
  type = 'text',
  value,
  onChange,
  error,
  placeholder,
  autoComplete,
}) {
  return (
    <div className="field-group">
      {label ? (
        <label className="field-label" htmlFor={id}>
          {label}
        </label>
      ) : null}
      <input
        id={id}
        name={name}
        type={type}
        className={`form-input ${error ? 'is-invalid' : ''}`.trim()}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        autoComplete={autoComplete}
        aria-invalid={Boolean(error)}
      />
      {error ? <span className="field-error">{error}</span> : null}
    </div>
  )
}
