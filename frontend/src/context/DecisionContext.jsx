/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useContext, useMemo, useState } from 'react'

const initialDecisions = [
  {
    id: 'decision-1',
    title: 'Final-Year Project Tech Stack',
    description: 'Our team needs to choose the stack for the final-year capstone. We want something we can ship in 12 weeks and defend confidently in the viva.',
    createdAt: '2026-10-28',
    category: 'Technology',
    deadline: '2026-11-02',
    status: 'Active',
    visibility: 'Community',
    creator: 'Aarav Mehta',
    isMine: true,
    participants: 48,
    options: [
      { title: 'React + Node.js', description: 'Flexible JavaScript stack', scores: { Cost: 7, Quality: 8, Benefits: 8, Risk: 6, Feasibility: 9 } },
      { title: 'Django + React', description: 'Python backend and React UI', scores: { Cost: 8, Quality: 7, Benefits: 7, Risk: 7, Feasibility: 7 } },
      { title: 'Next.js + Supabase', description: 'Full-stack React with managed data', scores: { Cost: 9, Quality: 7, Benefits: 8, Risk: 5, Feasibility: 8 } },
    ],
    criteria: [
      { name: 'Cost', weight: 25 },
      { name: 'Quality', weight: 25 },
      { name: 'Benefits', weight: 20 },
      { name: 'Risk', weight: 15 },
      { name: 'Feasibility', weight: 15 },
    ],
    votes: 48,
  },
  {
    id: 'decision-2',
    title: 'Where Should We Hold the Annual Tech Fest?',
    description: 'Choose the venue for the two-day inter-college tech fest, balancing capacity, cost and accessibility.',
    createdAt: '2026-10-30',
    category: 'Campus',
    deadline: '2026-10-25',
    status: 'Active',
    visibility: 'Community',
    creator: 'Meera Iyer',
    participants: 132,
    options: [
      { title: 'Main Auditorium', description: 'Central campus venue', scores: { Cost: 8, Capacity: 7, Accessibility: 9 } },
      { title: 'Convention Hall', description: 'Large off-campus hall', scores: { Cost: 5, Capacity: 9, Accessibility: 6 } },
    ],
    criteria: [
      { name: 'Cost', weight: 40 },
      { name: 'Capacity', weight: 35 },
      { name: 'Accessibility', weight: 25 },
    ],
    votes: 132,
  },
  {
    id: 'decision-3',
    title: 'Choosing a Laptop for Design Work',
    description: 'Help our design group choose a reliable laptop for studio work and prototyping.',
    category: 'Technology',
    deadline: '2026-09-30',
    status: 'Completed',
    visibility: 'Private',
    creator: 'Priya Nair',
    participants: 84,
    options: [
      { title: 'MacBook Pro', description: 'High performance and battery life', scores: { Cost: 5, Quality: 9, Benefits: 8 } },
      { title: 'Dell XPS', description: 'Premium Windows ultrabook', scores: { Cost: 7, Quality: 8, Benefits: 7 } },
      { title: 'Framework Laptop', description: 'Upgradeable and repairable', scores: { Cost: 8, Quality: 7, Benefits: 8 } },
    ],
    criteria: [
      { name: 'Cost', weight: 35 },
      { name: 'Quality', weight: 40 },
      { name: 'Benefits', weight: 25 },
    ],
    votes: 73,
    isMine: true,
  },
  {
    id: 'decision-4',
    title: 'Internship Offer: Startup vs. MNC',
    description: 'Compare two internship paths and decide where to spend the summer.',
    category: 'Career',
    deadline: '2026-11-15',
    status: 'Draft',
    visibility: 'Private',
    creator: 'You',
    isMine: true,
    participants: 0,
    options: [
      { title: 'Early-stage startup', description: 'Small team, broad ownership', scores: { Learning: 9, Stability: 5 } },
      { title: 'Large enterprise', description: 'Established team and process', scores: { Learning: 7, Stability: 9 } },
    ],
    criteria: [
      { name: 'Learning', weight: 60 },
      { name: 'Stability', weight: 40 },
    ],
    votes: 0,
  },
  {
    id: 'decision-5',
    title: 'Group Trip Destination for December',
    description: 'Pick a budget-friendly destination for the batch trip, five days and four nights.',
    createdAt: '2026-10-26',
    category: 'Travel',
    deadline: '2026-10-21',
    status: 'Active',
    visibility: 'Community',
    creator: 'Sneha Kulkarni',
    participants: 76,
    options: [
      { title: 'Goa', description: 'Beaches and relaxed evenings', scores: { Cost: 8, Travel: 9, Activities: 8 } },
      { title: 'Bali', description: 'Island adventure and culture', scores: { Cost: 6, Travel: 6, Activities: 9 } },
    ],
    criteria: [
      { name: 'Cost', weight: 40 },
      { name: 'Travel', weight: 30 },
      { name: 'Activities', weight: 30 },
    ],
    votes: 76,
  },
  {
    id: 'decision-6',
    title: 'Monthly Budget Tracking Tool',
    description: 'Which budgeting approach should the hostel committee adopt for shared expenses?',
    createdAt: '2026-10-22',
    category: 'Finance',
    deadline: '2026-10-05',
    status: 'Completed',
    visibility: 'Community',
    creator: 'Meera Iyer',
    participants: 64,
    options: [
      { title: 'Shared spreadsheet', description: 'A collaborative budget workbook', scores: { Cost: 9, Ease: 7 } },
      { title: 'Budget tracking app', description: 'A dedicated expense tracking tool', scores: { Cost: 7, Ease: 9 } },
    ],
    criteria: [
      { name: 'Cost', weight: 60 },
      { name: 'Ease', weight: 40 },
    ],
    votes: 0,
  },
]

const initialPosts = [
  {
    id: 'post-1',
    author: 'Priya Nair',
    initials: 'PN',
    category: 'Career',
    time: '3h ago',
    title: 'How do you weigh criteria when everything feels important?',
    body: 'I keep giving every criterion the same weight. Any tips for forcing real trade-offs?',
    likes: 24,
    replies: 1,
    reply: 'Try ranking them first, then assign weights from the ranking.',
    replyAuthor: 'Rohan Das',
  },
  {
    id: 'post-2',
    author: 'Karan Shah',
    initials: 'KS',
    category: 'Technology',
    time: '1d ago',
    title: 'Is a weighted score enough, or should the community vote decide?',
    body: 'Our group disagrees: the highest-scoring option lost the poll. Which should we trust?',
    likes: 41,
    replies: 0,
  },
  {
    id: 'post-3',
    author: 'Meera Iyer',
    initials: 'MI',
    category: 'Campus',
    time: '2d ago',
    title: 'Tips for running a fair poll for a campus event',
    body: 'Looking for advice on deadlines, result visibility and avoiding bias in event polls.',
    likes: 17,
    replies: 0,
  },
]

const DecisionContext = createContext(null)

export function DecisionProvider({ children }) {
  const [decisions, setDecisions] = useState(initialDecisions)
  const [posts, setPosts] = useState(initialPosts)
  const [notifications, setNotifications] = useState([
    { id: 'notification-1', text: 'Reminder: cast your vote on “Where Should We Hold the Annual Tech Fest?”', time: '10 min ago', type: 'vote', unread: true },
    { id: 'notification-2', text: 'Priya Nair replied to your comment in “Final-Year Project Tech Stack”', time: '2h ago', type: 'reply', unread: true },
    { id: 'notification-3', text: 'Sneha Kulkarni invited you to “Group Trip Destination for December”', time: '5h ago', type: 'invite', unread: true },
    { id: 'notification-4', text: '“Group Trip Destination for December” closes in 3 days', time: 'Yesterday', type: 'deadline', unread: false },
    { id: 'notification-5', text: 'Results published for “Monthly Budget Tracking Tool”', time: '3 days ago', type: 'results', unread: false },
  ])
  const [votes, setVotes] = useState({})

  const saveDecision = useCallback((decision, shouldPublish = true) => {
    setDecisions((current) => {
      const existing = current.some((item) => item.id === decision.id)
      const saved = { ...decision, status: shouldPublish ? 'Active' : 'Draft' }
      return existing
        ? current.map((item) => (item.id === saved.id ? saved : item))
        : [saved, ...current]
    })
    return decision.id
  }, [])

  const castVote = useCallback((decisionId, optionTitle) => {
    setVotes((current) => ({ ...current, [decisionId]: optionTitle }))
    setDecisions((current) =>
      current.map((decision) =>
        decision.id === decisionId
          ? { ...decision, votes: (decision.votes || 0) + 1, participants: (decision.participants || 0) + 1 }
          : decision,
      ),
    )
  }, [])

  const toggleLike = useCallback((postId) => {
    setPosts((current) =>
      current.map((post) =>
        post.id === postId
          ? { ...post, liked: !post.liked, likes: post.likes + (post.liked ? -1 : 1) }
          : post,
      ),
    )
  }, [])

  const addPost = useCallback((post) => {
    setPosts((current) => [post, ...current])
  }, [])

  const addReply = useCallback((postId, reply, author) => {
    setPosts((current) => current.map((post) => post.id === postId
      ? { ...post, reply, replyAuthor: author, replies: post.replies + 1 }
      : post))
  }, [])

  const markAllRead = useCallback(() => {
    setNotifications((current) => current.map((notification) => ({ ...notification, unread: false })))
  }, [])

  const value = useMemo(
    () => ({
      decisions,
      posts,
      notifications,
      votes,
      saveDecision,
      castVote,
      toggleLike,
      addPost,
      addReply,
      markAllRead,
    }),
    [decisions, posts, notifications, votes, saveDecision, castVote, toggleLike, addPost, addReply, markAllRead],
  )

  return <DecisionContext.Provider value={value}>{children}</DecisionContext.Provider>
}

export function useDecisions() {
  const context = useContext(DecisionContext)
  if (!context) {
    throw new Error('useDecisions must be used inside DecisionProvider')
  }
  return context
}
