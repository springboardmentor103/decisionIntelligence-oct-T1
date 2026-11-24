/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { useAuth } from './AuthContext'

const demoDiscussions = [
  {
    id: 'discussion-1',
    decisionId: 'decision-1',
    decisionTitle: 'Final-Year Project Tech Stack',
    title: 'What should we prioritize when choosing our stack?',
    body: 'We have twelve weeks to ship. I am weighing team familiarity against long-term maintainability.',
    category: 'Technology',
    author: 'Priya Nair',
    authorId: 'sample-priya',
    initials: 'PN',
    createdAt: '2026-11-18T10:00:00.000Z',
    likes: 8,
    likedByMe: false,
    comments: [
      {
        id: 'comment-1',
        parentId: null,
        author: 'Rohan Das',
        authorId: 'sample-rohan',
        body: 'Start with the skills your team already has, then check whether the stack can meet the project requirements.',
        createdAt: '2026-11-18T11:00:00.000Z',
      },
      {
        id: 'comment-2',
        parentId: 'comment-1',
        author: 'Meera Iyer',
        authorId: 'sample-meera',
        body: 'Agreed. A small prototype can validate the riskiest part before you commit.',
        createdAt: '2026-11-18T12:00:00.000Z',
      },
    ],
  },
  {
    id: 'discussion-2',
    decisionId: 'decision-2',
    decisionTitle: 'Where Should We Hold the Annual Tech Fest?',
    title: 'How do we compare venue accessibility?',
    body: 'The auditorium is central, while the convention hall has more space. What accessibility needs should we include?',
    category: 'Campus',
    author: 'Karan Shah',
    authorId: 'sample-karan',
    initials: 'KS',
    createdAt: '2026-11-17T09:30:00.000Z',
    likes: 5,
    likedByMe: false,
    comments: [],
  },
]

const demoFeedback = [
  {
    id: 'feedback-1',
    decisionId: 'decision-1',
    decisionTitle: 'Final-Year Project Tech Stack',
    rating: 4,
    message: 'The options and criteria made it easier to compare trade-offs.',
    author: 'You',
    authorId: 'demo-user',
    submittedAt: '2026-11-18T09:00:00.000Z',
  },
]

const demoNotifications = [
  {
    id: 'notification-1',
    text: 'Rohan Das commented on the tech stack discussion.',
    type: 'reply',
    time: '2h ago',
    unread: true,
    href: '/community?decisionId=decision-1#discussion-1',
    createdAt: '2026-11-24T08:00:00.000Z',
  },
  {
    id: 'notification-2',
    text: 'Results are available for the campus venue poll.',
    type: 'results',
    time: 'Yesterday',
    unread: true,
    href: '/polls/poll-campus-event/results',
    createdAt: '2026-11-23T09:00:00.000Z',
  },
  {
    id: 'notification-3',
    text: 'Your feedback on the tech stack decision was saved.',
    type: 'feedback',
    time: '2 days ago',
    unread: false,
    href: '/feedback',
    createdAt: '2026-11-22T09:00:00.000Z',
  },
]

const demoModerationItems = [
  {
    id: 'report-1',
    contentType: 'Discussion',
    title: 'Repeated promotional links',
    content: 'This post contains repeated external promotions unrelated to the decision.',
    reportedBy: 'Community member',
    reason: 'Spam',
    status: 'Pending',
    createdAt: '2026-11-23T09:00:00.000Z',
    history: [],
  },
  {
    id: 'report-2',
    contentType: 'Comment',
    title: 'Unhelpful comment on venue decision',
    content: 'The comment targets another member rather than discussing the options.',
    reportedBy: 'Community member',
    reason: 'Harassment',
    status: 'Pending',
    createdAt: '2026-11-22T12:30:00.000Z',
    history: [],
  },
]

const demoReports = [
  {
    id: 'report-row-1',
    type: 'Decision report',
    title: 'Final-Year Project Tech Stack',
    detail: 'Decision participation summary',
    status: 'Available',
    date: '2026-11-18',
  },
  {
    id: 'report-row-2',
    type: 'Poll results',
    title: 'Which workshop should we host next?',
    detail: 'Poll results and option totals',
    status: 'Available',
    date: '2026-11-20',
  },
  {
    id: 'report-row-3',
    type: 'Community participation',
    title: 'Technology & Product',
    detail: 'Community participation summary',
    status: 'Available',
    date: '2026-11-22',
  },
]

const readArray = (key, initialValue) => {
  const saved = localStorage.getItem(key)
  if (!saved) return initialValue

  const parsed = JSON.parse(saved)
  if (!Array.isArray(parsed)) {
    throw new Error(`Saved ${key} data must be an array.`)
  }
  return parsed
}

const makeId = (prefix) => `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`

const FeatureContext = createContext(null)

export function FeatureProvider({ children }) {
  const { user } = useAuth()
  const userId = user?.id || 'demo-user'
  const [discussions, setDiscussions] = useState(() => readArray('decisionhub-feature-discussions', demoDiscussions))
  const [feedback, setFeedback] = useState(() => readArray('decisionhub-feature-feedback', demoFeedback))
  const [notifications, setNotifications] = useState(() => readArray('decisionhub-feature-notifications', demoNotifications))
  const [moderationItems, setModerationItems] = useState(() => readArray('decisionhub-feature-moderation', demoModerationItems))
  const [reportRows, setReportRows] = useState(() => readArray('decisionhub-feature-reports', demoReports))

  useEffect(() => {
    localStorage.setItem('decisionhub-feature-discussions', JSON.stringify(discussions))
  }, [discussions])
  useEffect(() => {
    localStorage.setItem('decisionhub-feature-feedback', JSON.stringify(feedback))
  }, [feedback])
  useEffect(() => {
    localStorage.setItem('decisionhub-feature-notifications', JSON.stringify(notifications))
  }, [notifications])
  useEffect(() => {
    localStorage.setItem('decisionhub-feature-moderation', JSON.stringify(moderationItems))
  }, [moderationItems])
  useEffect(() => {
    localStorage.setItem('decisionhub-feature-reports', JSON.stringify(reportRows))
  }, [reportRows])

  const addNotification = useCallback((text, type, href) => {
    setNotifications((current) => [{
      id: makeId('notification'),
      text,
      type,
      time: 'Just now',
      unread: true,
      href,
      createdAt: new Date().toISOString(),
    }, ...current])
  }, [])

  const createDiscussion = useCallback((draft) => {
    const discussion = {
      ...draft,
      id: makeId('discussion'),
      author: user?.name || 'You',
      authorId: userId,
      initials: (user?.name || 'You').split(/\s+/).slice(0, 2).map((part) => part[0]).join('').toUpperCase(),
      createdAt: new Date().toISOString(),
      likes: 0,
      likedByMe: false,
      comments: [],
    }
    setDiscussions((current) => [discussion, ...current])
    return discussion
  }, [user, userId])

  const editDiscussion = useCallback((discussionId, updates) => {
    const discussion = discussions.find((item) => item.id === discussionId)
    if (!discussion || discussion.authorId !== userId) {
      throw new Error('You can only edit your own discussions.')
    }
    setDiscussions((current) => current.map((item) => item.id === discussionId
      ? { ...item, ...updates, updatedAt: new Date().toISOString() }
      : item))
  }, [discussions, userId])

  const deleteDiscussion = useCallback((discussionId) => {
    const discussion = discussions.find((item) => item.id === discussionId)
    if (!discussion || discussion.authorId !== userId) {
      throw new Error('You can only delete your own discussions.')
    }
    setDiscussions((current) => current.filter((item) => item.id !== discussionId))
  }, [discussions, userId])

  const toggleDiscussionLike = useCallback((discussionId) => {
    setDiscussions((current) => current.map((item) => item.id === discussionId
      ? { ...item, likedByMe: !item.likedByMe, likes: Math.max(0, item.likes + (item.likedByMe ? -1 : 1)) }
      : item))
  }, [])

  const addComment = useCallback((discussionId, body, parentId = null) => {
    const discussion = discussions.find((item) => item.id === discussionId)
    if (!discussion) throw new Error('Discussion not found.')
    const comment = {
      id: makeId('comment'),
      parentId,
      author: user?.name || 'You',
      authorId: userId,
      body,
      createdAt: new Date().toISOString(),
    }
    setDiscussions((current) => current.map((item) => item.id === discussionId
      ? { ...item, comments: [...item.comments, comment] }
      : item))
    addNotification(`A new reply was added to “${discussion.title}”.`, 'reply', `/community?decisionId=${discussion.decisionId}#${discussionId}`)
  }, [addNotification, discussions, user, userId])

  const createFeedback = useCallback((draft) => {
    const entry = {
      ...draft,
      id: makeId('feedback'),
      author: user?.name || 'You',
      authorId: userId,
      submittedAt: new Date().toISOString(),
    }
    setFeedback((current) => [entry, ...current])
    addNotification(`Your feedback on “${entry.decisionTitle}” was saved.`, 'feedback', '/feedback')
  }, [addNotification, user, userId])

  const editFeedback = useCallback((feedbackId, updates) => {
    const existing = feedback.find((item) => item.id === feedbackId)
    if (!existing || existing.authorId !== userId) {
      throw new Error('You can only edit your own feedback.')
    }
    setFeedback((current) => current.map((item) => item.id === feedbackId
      ? { ...item, ...updates, updatedAt: new Date().toISOString() }
      : item))
  }, [feedback, userId])

  const submitReport = useCallback((discussion, reason, details) => {
    const createdAt = new Date().toISOString()
    const id = makeId('report')
    const item = {
      id,
      contentType: 'Discussion',
      title: discussion.title,
      content: details || discussion.body,
      reportedBy: user?.name || 'You',
      reason,
      status: 'Pending',
      createdAt,
      history: [],
    }
    setModerationItems((current) => [item, ...current])
    setReportRows((current) => [{
      id,
      type: 'Community participation',
      title: discussion.decisionTitle,
      detail: `Community content report: ${reason}`,
      status: 'Under review',
      date: createdAt.slice(0, 10),
    }, ...current])
    addNotification(`A discussion was reported for ${reason.toLowerCase()}.`, 'report', '/moderation')
  }, [addNotification, user])

  const resolveModerationItem = useCallback((itemId, status) => {
    if (!['Approved', 'Rejected'].includes(status)) {
      throw new Error('Choose a valid moderation action.')
    }
    const action = {
      status,
      moderator: user?.name || 'Moderator',
      at: new Date().toISOString(),
    }
    setModerationItems((current) => current.map((item) => item.id === itemId
      ? { ...item, status, history: [...item.history, action] }
      : item))
    setReportRows((current) => current.map((row) => row.id === itemId
      ? { ...row, status }
      : row))
  }, [user])

  const markNotificationRead = useCallback((notificationId) => {
    setNotifications((current) => current.map((item) => item.id === notificationId
      ? { ...item, unread: false }
      : item))
  }, [])

  const markAllNotificationsRead = useCallback(() => {
    setNotifications((current) => current.map((item) => ({ ...item, unread: false })))
  }, [])

  const value = useMemo(() => ({
    discussions,
    feedback,
    notifications,
    moderationItems,
    reportRows,
    userId,
    createDiscussion,
    editDiscussion,
    deleteDiscussion,
    toggleDiscussionLike,
    addComment,
    createFeedback,
    editFeedback,
    submitReport,
    resolveModerationItem,
    markNotificationRead,
    markAllNotificationsRead,
  }), [
    discussions,
    feedback,
    notifications,
    moderationItems,
    reportRows,
    userId,
    createDiscussion,
    editDiscussion,
    deleteDiscussion,
    toggleDiscussionLike,
    addComment,
    createFeedback,
    editFeedback,
    submitReport,
    resolveModerationItem,
    markNotificationRead,
    markAllNotificationsRead,
  ])

  return <FeatureContext.Provider value={value}>{children}</FeatureContext.Provider>
}

export function useFeatures() {
  const context = useContext(FeatureContext)
  if (!context) throw new Error('useFeatures must be used inside FeatureProvider')
  return context
}
