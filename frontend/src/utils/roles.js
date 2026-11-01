const roleAliases = {
  administrator: 'admin',
  admin: 'admin',
  communitymoderator: 'moderator',
  moderator: 'moderator',
  user: 'user',
}

export const normalizeRoles = (roles) => {
  const roleList = Array.isArray(roles) ? roles : [roles]

  return [...new Set(
    roleList
      .filter((role) => typeof role === 'string')
      .map((role) => roleAliases[role.toLowerCase().replace(/[\s_-]+/g, '')])
      .filter(Boolean),
  )]
}
