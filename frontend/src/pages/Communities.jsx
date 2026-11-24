
import { Plus, Search, UsersRound } from 'lucide-react'
import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import PageHeading from '../components/PageHeading'
import { useDecisions } from '../context/DecisionContext'
import { createCommunity } from '../services/communityService'

export default function Communities() {
  const { communities, joinedCommunities } = useDecisions()

  const [query, setQuery] = useState('')
  const [category, setCategory] = useState('All')
  const [showForm, setShowForm] = useState(false)
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [newCommunities, setNewCommunities] = useState([])
  const [error, setError] = useState('')
  const [creating, setCreating] = useState(false)

  const allCommunities = [...newCommunities, ...communities]

  const filtered = useMemo(() => allCommunities.filter((community) => {
    const communityCategory = community.category || 'General'
    const matchesCategory =
      category === 'All' || communityCategory === category

    const matchesQuery =
      `${community.name} ${community.description} ${communityCategory}`
        .toLowerCase()
        .includes(query.trim().toLowerCase())

    return matchesCategory && matchesQuery
  }), [allCommunities, category, query])

  const categories = [
    'All',
    ...new Set(
      allCommunities.map((community) => community.category || 'General')
    ),
  ]

  const handleCreate = async (event) => {
    event.preventDefault()
    setError('')

    if (!name.trim() || !description.trim()) {
      setError('Please enter both community name and description.')
      return
    }

    try {
      setCreating(true)

      const response = await createCommunity({
        name: name.trim(),
        description: description.trim(),
      })

      const created = response?.data ?? response

      if (!created?.id) {
        throw new Error('Community response was unexpected.')
      }

      setNewCommunities((current) => [
        {
          ...created,
          id: String(created.id),
          members: created.memberCount ?? 0,
          decisions: created.decisionCount ?? 0,
        },
        ...current,
      ])

      setName('')
      setDescription('')
      setShowForm(false)
      setCategory('All')
      setQuery('')
    } catch (err) {
      console.error('Create community failed:', err)
      setError(
        err.response?.data?.message ||
        err.message ||
        'Unable to create community.'
      )
    } finally {
      setCreating(false)
    }
  }

  return (
    <div className="page-block">
      <PageHeading
        title="Communities"
        description="Find people who are working through the same kinds of decisions."
        actions={
          <>
            <button
              type="button"
              className="button button--primary"
              onClick={() => {
                setShowForm(!showForm)
                setError('')
              }}
            >
              <Plus size={16} />
              Create Community
            </button>

            <Link to="/community" className="button button--outline">
              Community discussions
            </Link>
          </>
        }
      />

      {showForm && (
        <form className="panel-card form-panel" onSubmit={handleCreate}>
          <h2>Create a Community</h2>

          <label className="form-control">
            Community Name
            <input
              value={name}
              onChange={(event) => setName(event.target.value)}
              placeholder="e.g. Java Developers"
              maxLength={100}
            />
          </label>

          <label className="form-control">
            Description
            <textarea
              rows="3"
              value={description}
              onChange={(event) => setDescription(event.target.value)}
              placeholder="Describe what this community is about."
            />
          </label>

          {error && (
            <p className="form-alert" role="alert">{error}</p>
          )}

          <div className="form-footer-actions">
            <button
              type="button"
              className="button button--outline"
              onClick={() => {
                setShowForm(false)
                setError('')
              }}
            >
              Cancel
            </button>

            <button
              type="submit"
              className="button button--primary"
              disabled={creating}
            >
              {creating ? 'Creating...' : 'Create Community'}
            </button>
          </div>
        </form>
      )}

      <div className="explore-toolbar">
        <label className="search-field">
          <Search size={16} />
          <input
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Search communities"
          />
        </label>
      </div>

      <div className="filter-row">
        {categories.map((item) => (
          <button
            type="button"
            className={`filter-pill ${category === item ? 'active' : ''}`}
            key={item}
            onClick={() => setCategory(item)}
          >
            {item}
          </button>
        ))}
      </div>

      {filtered.length ? (
        <div className="community-grid">
          {filtered.map((community) => (
            <article className="panel-card community-card" key={community.id}>
              <div className="community-card-top">
                <span className="category-label">
                  {community.category || 'General'}
                </span>
                <span className="community-privacy">
                  {community.privacy || 'Community'}
                </span>
              </div>

              <h2>{community.name}</h2>
              <p>{community.description}</p>

              <div className="community-card-meta">
                <span>
                  <UsersRound size={14} />
                  {(community.members ?? community.memberCount ?? 0).toLocaleString()} members
                </span>
                <span>{community.decisions ?? community.decisionCount ?? 0} decisions</span>
              </div>

              <Link
                className={`button button--small ${
                  joinedCommunities.includes(String(community.id))
                    ? 'button--outline'
                    : 'button--primary'
                }`}
                to={`/communities/${community.id}`}
              >
                {joinedCommunities.includes(String(community.id))
                  ? 'View Community'
                  : 'Explore Community'}
              </Link>
            </article>
          ))}
        </div>
      ) : (
        <div className="panel-card empty-state">
          <h2>No communities found</h2>
          <p>Try a different search or category.</p>
        </div>
      )}
    </div>
  )
}
