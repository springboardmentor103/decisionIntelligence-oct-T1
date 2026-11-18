import { ArrowLeft, Check, LockKeyhole, UsersRound } from 'lucide-react'
import { Link, useParams } from 'react-router-dom'
import { DecisionCard } from '../components/DecisionCard'
import PageHeading from '../components/PageHeading'
import { useDecisions } from '../context/DecisionContext'

export default function CommunityDetails() {
  const { communityId } = useParams()
  const { communities, joinedCommunities, joinCommunity, decisions } = useDecisions()
  const community = communities.find((item) => item.id === communityId)

  if (!community) return <div className="page-block"><div className="panel-card empty-state"><h2>Community not found</h2><Link to="/communities" className="button button--primary">Browse communities</Link></div></div>

  const joined = joinedCommunities.includes(community.id)
  const communityDecisions = decisions.filter((decision) => decision.category === community.category && decision.visibility === 'Community' && decision.status !== 'Draft')

  return (
    <div className="page-block">
      <Link to="/communities" className="back-link"><ArrowLeft size={15} /> All communities</Link>
      <PageHeading title={community.name} description={community.description} actions={
        joined
          ? <span className="joined-label"><Check size={15} /> Joined</span>
          : <button type="button" className="button button--primary" onClick={() => joinCommunity(community.id)}>Join Community</button>
      } />
      <div className="community-detail-stats">
        <span><UsersRound size={15} /> {community.members.toLocaleString()} members</span>
        <span>{community.decisions} decisions</span>
        <span>{community.privacy === 'Private' ? <LockKeyhole size={14} /> : null}{community.privacy}</span>
      </div>
      <section className="panel-card community-welcome">
        <h2>{joined ? 'Welcome to the community' : 'About this community'}</h2>
        <p>{joined ? `You’re a member of ${community.name}. Explore decisions and share your perspective.` : 'Join this community to take part in conversations and decisions with members.'}</p>
        {!joined ? <button type="button" className="button button--primary" onClick={() => joinCommunity(community.id)}>Join Community</button> : <Link to="/community" className="button button--outline">Browse discussions</Link>}
      </section>
      <div className="panel-heading community-decisions-heading"><div><h2>Related decisions</h2><p>Open decisions in the {community.category} category.</p></div></div>
      {communityDecisions.length ? <div className="decision-grid">{communityDecisions.map((decision) => <DecisionCard key={decision.id} decision={decision} />)}</div> : <div className="panel-card empty-state"><p>No open decisions in this community yet.</p></div>}
    </div>
  )
}
