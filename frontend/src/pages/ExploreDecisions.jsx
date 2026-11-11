import { Search } from 'lucide-react'
import { useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { DecisionCard, FilterPill } from '../components/DecisionCard'
import PageHeading from '../components/PageHeading'
import { useDecisions } from '../context/DecisionContext'

const categories = ['All', 'Technology', 'Campus', 'Career', 'Finance', 'Lifestyle', 'Travel']

export default function ExploreDecisions() {
  const { decisions } = useDecisions()
  const [searchParams] = useSearchParams()
  const [query, setQuery] = useState(searchParams.get('q') || '')
  const [category, setCategory] = useState('All')
  const [sort, setSort] = useState('Latest')

  const filteredDecisions = useMemo(() => {
    const search = query.trim().toLowerCase()
    const items = decisions.filter((decision) => {
      if (decision.visibility !== 'Community' || decision.status === 'Draft') return false
      const matchesSearch = !search || `${decision.title} ${decision.description} ${decision.category} ${decision.creator}`.toLowerCase().includes(search)
      const matchesCategory = category === 'All' || decision.category === category
      return matchesSearch && matchesCategory
    })

    return [...items].sort((a, b) => {
      if (sort === 'Most Popular') return b.participants - a.participants
      if (sort === 'Closing Soon') return new Date(a.deadline) - new Date(b.deadline)
      return new Date(b.createdAt || b.deadline) - new Date(a.createdAt || a.deadline)
    })
  }, [decisions, query, category, sort])

  return (
    <div className="page-block">
      <PageHeading title="Explore Decisions" description="Join open community decisions and share your view." />
      <div className="explore-toolbar">
        <label className="search-field">
          <Search size={16} />
          <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search decisions" />
        </label>
        <div className="sort-tabs" aria-label="Sort decisions">
          {['Latest', 'Most Popular', 'Closing Soon'].map((option) => (
            <FilterPill key={option} active={sort === option} onClick={() => setSort(option)}>{option}</FilterPill>
          ))}
        </div>
      </div>
      <div className="filter-row">
        {categories.map((item) => (
          <FilterPill key={item} active={category === item} onClick={() => setCategory(item)}>{item}</FilterPill>
        ))}
      </div>
      {filteredDecisions.length ? (
        <div className="decision-grid">
          {filteredDecisions.map((decision) => <DecisionCard key={decision.id} decision={decision} />)}
        </div>
      ) : (
        <div className="empty-state panel-card">
          <h2>No decisions found</h2>
          <p>Try a different search or category.</p>
        </div>
      )}
    </div>
  )
}
