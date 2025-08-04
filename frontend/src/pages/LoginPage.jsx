import LoginForm from '../components/forms/LoginForm'
import { Box } from '@mui/material'

const LoginPage = () => {
  return (
  <Box
    display="flex"
    justifyContent="center"
    alignItems="center"
    minHeight="100vh"
  >
    <Box sx={{ width: '100%', maxWidth: 400 }}>
      <LoginForm />
    </Box>
  </Box>
  )
}

export default LoginPage
