import { Search, UsersRound } from 'lucide-react'
import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import PageHeading from '../components/PageHeading'
import { useDecisions } from '../context/DecisionContext'

export default function Communities() {
  const { communities, joinedCommunities } = useDecisions()
  const [query, setQuery] = useState('')
  const [category, setCategory] = useState('All')
  const filtered = useMemo(() => communities.filter((community) => {
    const matchesCategory = category === 'All' || community.category === category
    const matchesQuery = `${community.name} ${community.description} ${community.category}`.toLowerCase().includes(query.trim().toLowerCase())
    return matchesCategory && matchesQuery
  }), [communities, category, query])
  const categories = ['All', ...new Set(communities.map((community) => community.category))]

  return (
    <div className="page-block">
      <PageHeading
        title="Communities"
        description="Find people who are working through the same kinds of decisions."
        actions={<Link to="/community" className="button button--outline">Community discussions</Link>}
      />
      <div className="explore-toolbar">
        <label className="search-field"><Search size={16} /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search communities" /></label>
      </div>
      <div className="filter-row">
        {categories.map((item) => <button type="button" className={`filter-pill ${category === item ? 'active' : ''}`} key={item} onClick={() => setCategory(item)}>{item}</button>)}
      </div>
      {filtered.length ? <div className="community-grid">
        {filtered.map((community) => <article className="panel-card community-card" key={community.id}>
          <div className="community-card-top"><span className="category-label">{community.category}</span><span className="community-privacy">{community.privacy}</span></div>
          <h2>{community.name}</h2>
          <p>{community.description}</p>
          <div className="community-card-meta"><span><UsersRound size={14} /> {community.members.toLocaleString()} members</span><span>{community.decisions} decisions</span></div>
          <Link className={`button button--small ${joinedCommunities.includes(community.id) ? 'button--outline' : 'button--primary'}`} to={`/communities/${community.id}`}>{joinedCommunities.includes(community.id) ? 'View Community' : 'Explore Community'}</Link>
        </article>)}
      </div> : <div className="panel-card empty-state"><h2>No communities found</h2><p>Try a different search or category.</p></div>}
    </div>
  )
}
