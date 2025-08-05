import { useEffect, useState } from 'react'
import { List, ListItem, ListItemButton, ListItemText, Box } from '@mui/material'
import ProfileAvatar from './ProfileAvatar'
import { Link, useLocation } from 'react-router-dom'

export default function SidebarMenu() {
  const location = useLocation()
  const [showMPButton, setShowMPButton] = useState(false)

  useEffect(() => {
    fetch('https://invoice-management-app-3g3w.onrender.com/companies', { 
      headers: {
        Authorization: `Bearer ${localStorage.getItem('token')}`
      } 
    })
      .then(res => res.json())
      .then(data => {
        if (!data.accessTokenIsPresent) setShowMPButton(true)
      })
  }, [])

  const menuItems = [
    { label: 'Facturas', path: '/bills' },
    { label: 'Productos', path: '/products' },
    { label: 'Clientes', path: '/customers' },
    { label: 'Mi Compañia', path: '/company' }
  ]

  return (
    <>
      <ProfileAvatar sx={{ width: 200, height: 200 }} />
      <List sx={{ width: '100%' }}>
        {menuItems.map(({ label, path }) => (
          <ListItem key={path} disablePadding>
            <ListItemButton
              component={Link}
              to={path}
              sx={{
                borderRadius: 2,
                bgcolor: location.pathname === path ? 'grey.100' : 'transparent',
                '&:hover': { bgcolor: 'grey.100' }
              }}
            >
              <ListItemText
                primary={label}
                primaryTypographyProps={{
                  fontSize: 14,
                  fontWeight: 500,
                  color: location.pathname === path ? 'text.primary' : 'text.secondary'
                }}
              />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
    </>
  )
}
