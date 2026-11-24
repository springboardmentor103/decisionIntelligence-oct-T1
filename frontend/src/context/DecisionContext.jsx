/* eslint-disable react-refresh/only-export-components, react-hooks/set-state-in-effect, no-unused-vars */
import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import {
  getDecisionOptions,
  getPollDetails,
  getMyDecisions,
  getPublicDecisions,
  getCommunityPolls,
  deleteDecision,
} from '../services/decisionService'
import { useAuth } from './AuthContext'
const sampleDecisions = [
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

const loadStoredArray = (key) => {
  const stored = localStorage.getItem(key)
  if (!stored) return []

  const value = JSON.parse(stored)
  if (!Array.isArray(value)) {
    throw new Error(`Saved ${key} data must be an array.`)
  }
  return value
}

const initialDecisions = loadStoredArray('decisionhub-created-decisions').map(
  (decision) => ({ ...decision, isSample: false })
)

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

const initialPolls = [
  {
    id: 'poll-campus-event',
    title: 'Which workshop should we host next?',
    description: 'Help the campus tech community choose its next hands-on session.',
    options: ['AI for Everyone', 'Build with React', 'Career Portfolio Clinic'],
    votes: { 'AI for Everyone': 18, 'Build with React': 27, 'Career Portfolio Clinic': 12 },
    closesAt: '2026-12-15',
    creator: 'Meera Iyer',
  },
]

const initialCommunities = [
  {
    id: 'technology',
    name: 'Technology & Product',
    category: 'Technology',
    description: 'Compare tools, plan projects and make better product decisions together.',
    members: 1240,
    decisions: 38,
    privacy: 'Public',
  },
  {
    id: 'campus-life',
    name: 'Campus Life',
    category: 'Campus',
    description: 'Make campus events, clubs and student life work better for everyone.',
    members: 864,
    decisions: 26,
    privacy: 'Public',
  },
  {
    id: 'career-growth',
    name: 'Career Growth',
    category: 'Career',
    description: 'Discuss internships, career paths and the choices that shape your future.',
    members: 978,
    decisions: 31,
    privacy: 'Public',
  },
  {
    id: 'travel-planning',
    name: 'Travel Planning',
    category: 'Travel',
    description: 'Plan group trips with clear options, budgets and shared priorities.',
    members: 542,
    decisions: 19,
    privacy: 'Public',
  },
]

const DecisionContext = createContext(null)

export function DecisionProvider({ children }) {
  const { user } = useAuth()
const userId = user?.userId
  const [decisions, setDecisions] = useState([])
  const [posts, setPosts] = useState(initialPosts)
  const [polls, setPolls] = useState(initialPolls)
  const [communities] = useState(initialCommunities)
  const [joinedCommunities, setJoinedCommunities] = useState(['technology'])
  const [notifications, setNotifications] = useState([
    { id: 'notification-1', text: 'Reminder: cast your vote on “Where Should We Hold the Annual Tech Fest?”', time: '10 min ago', type: 'vote', unread: true },
    { id: 'notification-2', text: 'Priya Nair replied to your comment in “Final-Year Project Tech Stack”', time: '2h ago', type: 'reply', unread: true },
    { id: 'notification-3', text: 'Sneha Kulkarni invited you to “Group Trip Destination for December”', time: '5h ago', type: 'invite', unread: true },
    { id: 'notification-4', text: '“Group Trip Destination for December” closes in 3 days', time: 'Yesterday', type: 'deadline', unread: false },
    { id: 'notification-5', text: 'Results published for “Monthly Budget Tracking Tool”', time: '3 days ago', type: 'results', unread: false },
  ])
  const [votes, setVotes] = useState({})
const [activityEvents, setActivityEvents] = useState([])
const [loadedUserId, setLoadedUserId] = useState(null)
useEffect(() => {
  if (!userId) {
    setDecisions([])
    setVotes({})
    setActivityEvents([])
    setLoadedUserId(null)
    return
  }

  const savedDecisions = loadStoredArray(
    `decisionhub-created-decisions-${userId}`
  )

  const savedVotes = localStorage.getItem(
    `decisionhub-votes-${userId}`
  )

  const savedActivity = loadStoredArray(
    `decisionhub-activity-${userId}`
  )

  setDecisions(
    savedDecisions.map((decision) => ({
      ...decision,
      isSample: false,
    }))
  )

  setVotes(savedVotes ? JSON.parse(savedVotes) : {})
  setActivityEvents(savedActivity)
  setLoadedUserId(userId)
}, [userId])

useEffect(() => {
  let cancelled = false
  if (!userId) {
  setDecisions([])
  return () => {
    cancelled = true
  }
}

  const loadBackendDecisions = async () => {
    const results = await Promise.allSettled([
      getMyDecisions(),
      getPublicDecisions(),
    ])

    const mySucceeded = results[0].status === 'fulfilled'
    const publicSucceeded = results[1].status === 'fulfilled'

    // Keep existing local data if both API requests fail.
    if (!mySucceeded && !publicSucceeded) {
      console.error('Unable to load decisions from backend.')
      return
    }

    const myDecisions = mySucceeded ? results[0].value || [] : []
    const publicDecisions = publicSucceeded ? results[1].value || [] : []

    // Avoid showing the same decision twice.
    const decisionMap = new Map()

    publicDecisions.forEach((decision) => {
      decisionMap.set(String(decision.id), {
        decision,
        isMine: false,
      })
    })

    myDecisions.forEach((decision) => {
      decisionMap.set(String(decision.id), {
        decision,
        isMine: true,
      })
    })

    const backendDecisions = await Promise.all(
      Array.from(decisionMap.values()).map(async ({ decision, isMine }) => {
        let options = []

        try {
          options = await getDecisionOptions(decision.id)
        } catch (error) {
          console.error(
            `Unable to load options for decision ${decision.id}:`,
            error
          )
        }

        return {
          ...decision,
          createdAt: decision.createdAt?.slice(0, 10) || '',
          deadline: decision.deadline?.slice(0, 10) || '',
          visibility:
            decision.visibility === 'PUBLIC' ? 'Community' : 'Private',
          status:
  new Date(decision.deadline) < new Date()
    ? 'Completed'
    : decision.status === 'COMPLETED'
      ? 'Completed'
      : decision.status === 'ACTIVE'
        ? 'Active'
        : 'Draft',
          creator: decision.creatorName || (isMine ? 'You' : ''),
          isMine,
          isSample: false,
          participants: 0,
          votes: 0,
          options: options.map((option) => ({
            id: option.id,
            decisionId: option.decisionId,
            title: option.title,
            description: option.description || '',
            displayOrder: option.displayOrder,
            scores: {},
          })),
        }
      })
    )

    if (cancelled) return

    setDecisions((current) => {
      // Preserve locally stored information not included in backend DTOs.
      const existingMap = new Map(
        current.map((decision) => [String(decision.id), decision])
      )

      const loaded = backendDecisions.map((decision) => {
        const existing = existingMap.get(String(decision.id))

        return {
          ...existing,
          ...decision,
          criteria: existing?.criteria || [],
        }
      })

      // If one endpoint failed, retain existing records from local storage.
      if (!mySucceeded || !publicSucceeded) {
        const loadedIds = new Set(loaded.map((item) => String(item.id)))
        const remaining = current.filter(
          (item) => !loadedIds.has(String(item.id))
        )

        return [...loaded, ...remaining]
      }

      return loaded
    })
  }

  loadBackendDecisions()

  return () => {
    cancelled = true
  }
},[userId])

useEffect(() => {
  let cancelled = false

  if (!userId) {
    setPolls([])
    return
  }

  const loadCommunityPolls = async () => {
    try {
      const communityPolls = await getCommunityPolls()

      const loadedPolls = await Promise.all(
        (communityPolls || []).map(async (decision) => {
          console.log('Individual backend decision:', decision)
let details = null
          try {
            const response = await getPollDetails(decision.id)
            details = response?.data ?? response
          } catch (error) {
            console.error(
              `Unable to load poll ${decision.id}:`,
              error
            )
          }

          const pollOptions = details?.options || []

          const options = pollOptions.map((option) => option.title)

          const pollVotes = Object.fromEntries(
            pollOptions.map((option) => [
              option.title,
              option.voteCount || 0,
            ])
          )

          return {
            id: String(decision.id),
            title: decision.title,
            description: decision.description || '',
            creatorId: decision.creatorId,
            options,
            votes: pollVotes,
            closesAt: (
              details?.deadline ||
              decision.deadline ||
              ''
            ).slice(0, 10),
            creator: decision.creatorName || 'Community Member',
          }
        })
      )

      if (!cancelled) {
        setPolls(loadedPolls)
        console.log('Final polls loaded into context:', loadedPolls)
      }
    } catch (error) {
      console.error('Unable to load community polls:', error)
    }
  }

  loadCommunityPolls()

  return () => {
    cancelled = true
  }
}, [userId])


  useEffect(() => {
  if (loadedUserId !== userId || !userId) return

  localStorage.setItem(
    `decisionhub-created-decisions-${userId}`,
    JSON.stringify(decisions.filter((decision) => !decision.isSample))
  )
}, [decisions, userId, loadedUserId])

useEffect(() => {
  if (loadedUserId !== userId || !userId) return

  localStorage.setItem(
    `decisionhub-votes-${userId}`,
    JSON.stringify(votes)
  )
}, [votes, userId, loadedUserId])

useEffect(() => {
  if (loadedUserId !== userId || !userId) return

  localStorage.setItem(
    `decisionhub-activity-${userId}`,
    JSON.stringify(activityEvents)
  )
}, [activityEvents, userId, loadedUserId])
const removeDecision = useCallback((decisionId) => {
  setDecisions((current) =>
    current.filter(
      (decision) => String(decision.id) !== String(decisionId)
    )
  )
}, [])
  const saveDecision = useCallback((decision, shouldPublish = true) => {
    const existing = decisions.some((item) => item.id === decision.id)
    const saved = { ...decision, isSample: false, status: shouldPublish ? 'Active' : 'Draft' }
    setDecisions((current) => {
      return existing
        ? current.map((item) => (item.id === saved.id ? saved : item))
        : [saved, ...current]
    })
    if (!existing) {
      setActivityEvents((events) => [...events, {
        type: 'decision',
        title: saved.title,
        timestamp: new Date().toISOString(),
      }])
    }
    return decision.id
  }, [decisions])

  const castVote = useCallback((decisionId, optionTitle) => {
    if (votes[decisionId]) return false
    setVotes((current) => ({ ...current, [decisionId]: optionTitle }))
    const decision = decisions.find((item) => String(item.id) === decisionId)
    setActivityEvents((events) => [...events, {
      type: 'vote',
      title: decision?.title || 'a decision',
      timestamp: new Date().toISOString(),
    }])
    setDecisions((current) =>
      current.map((decision) =>
        decision.id === decisionId
          ? { ...decision, votes: (decision.votes || 0) + 1, participants: (decision.participants || 0) + 1 }
          : decision,
      ),
    )
    return true
  }, [decisions, votes])

  const createPoll = useCallback((poll) => {
    setPolls((current) => [poll, ...current])
  }, [])
  const deletePoll = useCallback(async (pollId) => {
  await deleteDecision(pollId)

  setPolls((current) =>
    current.filter((poll) => String(poll.id) !== String(pollId))
  )

  setDecisions((current) =>
    current.filter((decision) => String(decision.id) !== String(pollId))
  )
}, [])

  const castPollVote = useCallback((pollId, option) => {
    if (votes[`poll:${pollId}`]) return false
    setVotes((current) => ({ ...current, [`poll:${pollId}`]: option }))
    const poll = polls.find((item) => item.id === pollId)
    setActivityEvents((events) => [...events, {
      type: 'vote',
      title: poll?.title || 'a poll',
      timestamp: new Date().toISOString(),
    }])
    setPolls((current) => current.map((poll) => poll.id === pollId
      ? { ...poll, votes: { ...poll.votes, [option]: (poll.votes[option] || 0) + 1 } }
      : poll))
    return true
  }, [polls, votes])

  const joinCommunity = useCallback((communityId) => {
    setJoinedCommunities((current) => current.includes(communityId) ? current : [...current, communityId])
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
      polls,
      communities,
      joinedCommunities,
      notifications,
      votes,
      activityEvents,
      saveDecision,
      removeDecision,
      castVote,
      createPoll,
      deletePoll,
      castPollVote,
      joinCommunity,
      toggleLike,
      addPost,
      addReply,
      markAllRead,
    }),
    [decisions, posts, polls, communities, removeDecision, joinedCommunities, notifications, votes, activityEvents, saveDecision, castVote, createPoll, deletePoll, castPollVote, joinCommunity, toggleLike, addPost, addReply, markAllRead],
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
