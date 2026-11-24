
import api from './api'
export const createCommunity = async (community) => {
  const response = await api.post('/communities', community)
  return response.data
}

// Get all communities
export const getAllCommunities = async () => {
  const response = await api.get('/communities')
  return response.data
}

// Get communities joined by the logged-in user
export const getMyCommunities = async () => {
  const response = await api.get('/communities/my')
  return response.data
}

// Get community details
export const getCommunityDetails = async (communityId) => {
  const response = await api.get(`/communities/${communityId}`)
  return response.data
}

// Join a community
export const joinCommunityApi = async (communityId) => {
  const response = await api.post(`/communities/${communityId}/join`)
  return response.data
}

// Leave a community
export const leaveCommunityApi = async (communityId) => {
  const response = await api.post(`/communities/${communityId}/leave`)
  return response.data
}

// Get community members
export const getCommunityMembers = async (communityId) => {
  const response = await api.get(`/communities/${communityId}/members`)
  return response.data
}