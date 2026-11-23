import api from './api'

export const createDecision = async (decision) => {
  const response = await api.post('/decisions', decision)
  return response.data.data
}
export const updateDecision = async (decisionId, decision) => {
  const response = await api.put(
    `/decisions/${decisionId}`,
    decision
  )
  return response.data.data
}

export const addDecisionOption = async (decisionId, option) => {
  const response = await api.post(
    `/decisions/${decisionId}/options`,
    option
  )
  return response.data.data
}

export const getMyDecisions = async () => {
  const response = await api.get('/decisions/my')
  return response.data.data
}

export const getPublicDecisions = async () => {
  const response = await api.get('/decisions/public')
  return response.data.data
}
export const getCommunityPolls = async () => {
  const response = await api.get('/decisions/community-polls')
  return response.data.data
}
export const getDecisionOptions = async (decisionId) => {
  const response = await api.get(`/decisions/${decisionId}/options`)
  return response.data.data
}
export const getPollDetails = async (decisionId) => {
  const response = await api.get(`/decisions/${decisionId}/poll`)
  return response.data
}
export const submitVote = async (decisionId, selections) => {
  const response = await api.post(
    `/decisions/${decisionId}/poll/vote`,
    { selections }
  )
  return response.data
}
export const getVoteStatus = async (decisionId) => {
  const response = await api.get(
    `/decisions/${decisionId}/poll/my-vote`
  )
  return response.data
}
export const deleteDecision = async (decisionId) => {
  const response = await api.delete(`/decisions/${decisionId}`)
  return response.data
}
export const getDecisionCriteria = async (decisionId) => {
  const response = await api.get(`/decisions/${decisionId}/criteria`)
  return response.data.data
}
