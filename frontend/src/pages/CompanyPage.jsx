import { useState, useEffect } from 'react'
import { Box, Typography, Button, CircularProgress } from '@mui/material'
import SidebarMenu from '../components/SidebarMenu'
import CompanyInfo from '../components/CompanyInfo'

function CompanyPage() {
  const [companyData, setCompanyData] = useState(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const token = localStorage.getItem('token')
    
    fetch('https://invoice-management-app-3g3w.onrender.com/companies', {
      headers: { Authorization: `Bearer ${token}` }
    })
      .then(res => {
        if (!res.ok) throw new Error('Error al obtener datos de la empresa')
        return res.json()
      })
      .then(data => {
        console.log('Company data received:', data) // Debug log
        setCompanyData(data)
        setIsLoading(false)
      })
      .catch(err => {
        console.error('Error fetching company data:', err)
        setError(err.message)
        setIsLoading(false)
      })
  }, [])

  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
        <CircularProgress />
      </Box>
    )
  }

  if (error) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
        <Typography color="error">Error: {error}</Typography>
      </Box>
    )
  }

  return (
    <Box display="flex" justifyContent="flex-start" mt={5} mx={5}>
      <Box width="65%" ml={8} mr={4}>
        <CompanyInfo 
          companyName={companyData?.companyName} 
          email={companyData?.email}
          address={companyData?.address}
        />
      </Box>
      <Box flexGrow={1}>
        <SidebarMenu />
      </Box>
    </Box>
  )
}

export default CompanyPage
