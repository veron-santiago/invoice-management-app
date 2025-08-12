import { TextField, Button, Box, Checkbox, FormControlLabel, Alert, Typography, Link, InputAdornment, IconButton } from '@mui/material'
import { Visibility, VisibilityOff } from '@mui/icons-material'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useState, useEffect } from 'react'

const LoginForm = () => {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [stayLogged, setStayLogged] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [showPassword, setShowPassword] = useState(false)

  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()

  useEffect(() => {
    const message = searchParams.get('message')
    const verified = searchParams.get('verified')
    
    if (message && verified !== null) {
      const decodedMessage = decodeURIComponent(message)
      
      if (verified === 'true') {
        setSuccess(decodedMessage)
        setTimeout(() => setSuccess(''), 5000)
      } else {
        setError(decodedMessage)
        setTimeout(() => setError(''), 5000)
      }
      
      const newSearchParams = new URLSearchParams(searchParams)
      newSearchParams.delete('message')
      newSearchParams.delete('verified')
      setSearchParams(newSearchParams, { replace: true })
    }
  }, [searchParams, setSearchParams])

    const handleSubmit = async (e) => {
    e.preventDefault()
    setIsSubmitting(true)
    try {
      const response = await fetch('http://localhost:8080/auth/log-in', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ companyName: username || null, password: password || null, stayLogged }),
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
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Box component="form" onSubmit={handleSubmit}>
      <Typography variant="h4" component="h1" align="center" sx={{ mb: 3, fontWeight: 'bold' }}>
        Inicio de Sesión
      </Typography>
      <TextField
        label="Nombre de la compañía o Email"
        fullWidth
        margin="normal"
        value={username}
        onChange={(e) => setUsername(e.target.value)}
      />
      <TextField
        label="Contraseña"
        type={showPassword ? 'text' : 'password'}
        fullWidth
        margin="normal"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        InputProps={{
          endAdornment: (
            <InputAdornment position="end">
              <IconButton
                aria-label="toggle password visibility"
                onClick={() => setShowPassword(!showPassword)}
                edge="end"
              >
                {showPassword ? <VisibilityOff /> : <Visibility />}
              </IconButton>
            </InputAdornment>
          ),
        }}
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
                <Button type="submit" variant="contained" disabled={isSubmitting}>
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
