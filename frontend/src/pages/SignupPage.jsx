import SignupForm from '../components/forms/SignupForm'
import { Box } from '@mui/material'

const SignupPage = () => {
  return (
  <Box
    display="flex"
    justifyContent="center"
    alignItems="center"
    minHeight="100vh"
  >
    <Box sx={{ width: '100%', maxWidth: 400 }}>
      <SignupForm />
    </Box>
  </Box>
  )
}

export default SignupPage