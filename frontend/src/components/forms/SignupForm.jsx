import { TextField, Button, Box, Typography, Link, Alert } from '@mui/material'
import { useState } from 'react'

const SignupForm = () => {
    const [username, setUsername] = useState('')
    const [password, setPassword] = useState('')
    const [email, setEmail] = useState('')
    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')

    const handleSubmit = async (e) => {
        e.preventDefault()
        try {
            const response = await fetch('https://invoice-management-app-3g3w.onrender.com/auth/sign-up', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ companyName: username, email, password })
            })
            const data = await response.json()
            if (response.ok && data.status) {
                let mensaje = data.message || 'Registro exitoso';
                setSuccess(mensaje)
                setTimeout(() => setSuccess(''), 3000)
                console.log('Registro exitoso', data)
            } else {
                let mensaje = data.message || data.companyName || data.email || data.password || 'Error en el registro'
                if (data.errors && Array.isArray(data.errors)) {
                    mensaje = data.errors.join(', ')
                }
                setError(mensaje)
                setTimeout(() => setError(''), 3000)
            }
        } catch (err) {
            setError('Ha ocurrido un error')
            setTimeout(() => setError(''), 3000)
        }
    }

    return (
        <Box component="form" onSubmit={handleSubmit}>
            <TextField
                label="Nombre de la compañia"
                fullWidth
                margin="normal"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
            />
            <TextField
                label="Correo electrónico"
                fullWidth
                margin="normal"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
            />
            <TextField
                label="Contraseña"
                type="password"
                fullWidth
                margin="normal"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
            />
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-around', mt: 2, mb: 2 }}>
                <Button type="submit" variant="contained" color="primary">
                    Enviar
                </Button>
            </Box>
            <Typography variant="body1" align="center" sx={{ mt: 3 }}>
                <Link href="/login" style={{ textDecoration: 'underline', fontSize: '1rem' }}>
                    ¿Compañia ya registrada? Ingresar
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

export default SignupForm
