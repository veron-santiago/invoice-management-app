import { useState, useEffect } from 'react'
import { Avatar, Box, Typography, CircularProgress } from '@mui/material'

const ProfileAvatar = ({ sx = {}, textSx = {} }) => {
  const [logoUrl, setLogoUrl] = useState(null)
  const [isLoading, setIsLoading] = useState(true)
  const API_URL = import.meta.env.VITE_API_URL;

  useEffect(() => {
    const controller = new AbortController()

    const fetchLogo = async () => {
      try {
        const token = localStorage.getItem('token')
        const res = await fetch(`${API_URL}/companies/logo`, {
          headers: { Authorization: `Bearer ${token}` },
          signal: controller.signal
        })

        if (!res.ok || res.status === 204) {
          setLogoUrl(null)
          return
        }

        const url = await res.text()
        setLogoUrl(url || null)
      } catch (err) {
        if (err.name !== 'AbortError') console.log('Logo no disponible:', err.message)
        setLogoUrl(null)
      } finally {
        setIsLoading(false)
      }
    }

    fetchLogo()

    return () => controller.abort()
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
