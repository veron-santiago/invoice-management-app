import { useState, useEffect } from 'react'
import { Avatar, Box, Typography, CircularProgress } from '@mui/material'

const ProfileAvatar = ({ sx = {}, textSx = {} }) => {
  const [logoUrl, setLogoUrl] = useState(null)
  const [isLoading, setIsLoading] = useState(true)
  const API_URL = import.meta.env.VITE_API_URL;

  useEffect(() => {
    const token = localStorage.getItem('token')
    const headers = {
      Authorization: `Bearer ${token}`
    }
    const fetchLogo = fetch(`${API_URL}/companies/logo`, { headers })
      .then(res => {
        if (res.ok) {
          return res.blob()
        }
        return null
      })
      .then(blob => {
        if (blob) {
          const logoUrl = URL.createObjectURL(blob)
          setLogoUrl(logoUrl)
        }
      })
      .catch(err => {
        console.log('Logo no disponible:', err.message)
      })

    Promise.allSettled([fetchLogo])
      .finally(() => {
        setIsLoading(false)
      })

    return () => {
      if (logoUrl) {
        URL.revokeObjectURL(logoUrl)
      }
    }
  }, [])

  return (
    <Box display="flex" flexDirection="column" alignItems="center" mb={3}>
      <Avatar
        src={logoUrl}
        sx={{
          width: 200,
          height: 200,
          border: '1px solid #ccc',
          '& img': { objectFit: 'contain', padding: '20px' },
          ...sx
        }}
      />
    </Box>
  )
}

export default ProfileAvatar
