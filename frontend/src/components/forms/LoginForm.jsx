import { TextField, Button, Box, Checkbox, FormControlLabel, Alert, Typography, Link } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import { useState } from 'react'

const LoginForm = () => {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [stayLogged, setStayLogged] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      const response = await fetch('https://invoice-management-app-3g3w.onrender.com/auth/log-in', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ companyName: username, password, stayLogged }),
      })
      const data = await response.json()

      if (response.ok && data.status) {
        setUsername('')
        setPassword('')
        setStayLogged(false)
        let mensaje = data.message || 'Sesión iniciada';
        setSuccess(mensaje)
        setTimeout(() => setSuccess(''), 3000)
        setError('')
        localStorage.setItem('token', data.jwt)
        navigate('/bills')
      } else {
        setError(data.message || data.companyName || data.password || 'Credenciales inválidas')
        setTimeout(() => setError(''), 3000)
      }
    } catch (error) {
      setError('Ha ocurrido un error')
      setTimeout(() => setError(''), 3000)
    }
  }

  return (
    <Box component="form" onSubmit={handleSubmit}>
      <TextField
        label="Nombre de la compañía o Email"
        fullWidth
        margin="normal"
        value={username}
        onChange={(e) => setUsername(e.target.value)}
      />
      <TextField
        label="Contraseña"
        type="password"
        fullWidth
        margin="normal"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
      />
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-around',
          mt: 2,
          mb: 2,
        }}
      >
        <FormControlLabel
          control={
            <Checkbox
              checked={stayLogged}
              onChange={(e) => setStayLogged(e.target.checked)}
              name="stayLogged"
            />
          }
          label="Recuérdame"
        />
        <Button type="submit" variant="contained">
          Enviar
        </Button>
      </Box>

      <Typography variant="body1" align="center" sx={{ mt: 3 }}>
        <Link href="/signup" style={{ textDecoration: 'underline', fontSize: '1rem' }}>
          ¿Compañía no registrada? Registrarla
        </Link>
      </Typography>

      <Box height="48px" mt={2}>
        {error && (
          <Alert severity="error" variant="standard">
            {error}
          </Alert>
        )}
      </Box>
      <Box height="48px" mt={2}>
        {success && (
          <Alert severity="success" variant="standard">
            {success}
          </Alert>
        )}
      </Box>
    </Box>
  )
}

export default LoginForm
