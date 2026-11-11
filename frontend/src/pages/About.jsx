import { Lightbulb, ListChecks, Scale, Target, UsersRound, ChartNoAxesColumnIncreasing } from 'lucide-react'
import { Link } from 'react-router-dom'
import PageHeading from '../components/PageHeading'

const features = [
  { icon: Lightbulb, title: 'The problem', body: 'Group decisions often happen in chats and spreadsheets. Opinions get lost, criteria stay implicit, and the loudest voice wins instead of the best option.' },
  { icon: ListChecks, title: 'How it works', body: 'Create a decision, list alternatives, define weighted criteria, then let people discuss and vote. DecisionHub keeps everything in one structured place.' },
  { icon: Scale, title: 'Why compare alternatives', body: 'Comparing several options against shared criteria exposes trade-offs, reduces bias and makes the final choice easier to explain and defend.' },
  { icon: UsersRound, title: 'Community and voting', body: 'Community decisions are open for discussion and one vote per person. Private decisions stay with the people you invite.' },
]
const steps = ['Define the decision', 'List the options', 'Set criteria and weights', 'Discuss with others', 'Vote and review results']
const benefits = [
  { icon: Target, title: 'Structure', body: 'Turn vague choices into clear options and criteria.' },
  { icon: UsersRound, title: 'Collaborate', body: 'Bring teammates and the community into the process.' },
  { icon: ChartNoAxesColumnIncreasing, title: 'Understand', body: 'Read results with charts and score comparisons.' },
]

export default function About() {
  return (
    <div className="about-page">
      <section className="about-intro">
        <PageHeading title="About DecisionHub" description="Empowering people to make informed decisions through collaboration and structured analysis." />
      </section>
      <div className="about-feature-grid">
        {features.map(({ icon: Icon, title, body }) => (
          <article className="panel-card about-feature" key={title}>
            <span className="about-icon"><Icon size={18} /></span>
            <div><h2>{title}</h2><p>{body}</p></div>
          </article>
        ))}
      </div>
      <section className="process-section">
        <h2>The decision-making process</h2>
        <div className="process-steps">
          {steps.map((step, index) => <div key={step}><span>{index + 1}</span><strong>{step}</strong></div>)}
        </div>
      </section>
      <section className="about-benefits">
        <h2>What you can do</h2>
        <div className="benefit-grid">
          {benefits.map(({ icon: Icon, title, body }) => <article className="panel-card" key={title}><span className="benefit-icon"><Icon size={20} /></span><h3>{title}</h3><p>{body}</p></article>)}
        </div>
        <Link className="button button--primary" to="/create-decision">Get Started</Link>
      </section>
    </div>
  )
}
