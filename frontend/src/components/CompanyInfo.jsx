import { useState } from 'react'
import { Box, Typography, Button, TextField, IconButton, Alert, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material'
import EditIcon from '@mui/icons-material/Edit'
import SaveIcon from '@mui/icons-material/Save'
import CancelIcon from '@mui/icons-material/Cancel'
import ProfileAvatar from './ProfileAvatar'
import { useNavigate } from 'react-router-dom'

const CompanyInfo = ({ companyName, email, address }) => {
  const [hover, setHover] = useState(false)
  const [isEditingEmail, setIsEditingEmail] = useState(false)
  const [emailForm, setEmailForm] = useState(email || '')
  const [isEditingName, setIsEditingName] = useState(false)
  const [nameForm, setNameForm] = useState(companyName || '')
  const [nameError, setNameError] = useState('')
  const [nameSuccess, setNameSuccess] = useState('')
  const [emailError, setEmailError] = useState('')
  const [emailSuccess, setEmailSuccess] = useState('')
  const [isEditingAddress, setIsEditingAddress] = useState(false)
  const [addressForm, setAddressForm] = useState(address || '')
  const [addressError, setAddressError] = useState('')
  const [addressSuccess, setAddressSuccess] = useState('')
  const [isPasswordDialogOpen, setIsPasswordDialogOpen] = useState(false)
  const [actualPassword, setActualPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [passwordError, setPasswordError] = useState('')
  const [savingPassword, setSavingPassword] = useState(false)
  const [mpMessage, setMpMessage] = useState('');

  const navigate = useNavigate()

  // NUEVO ESTADO PARA POPUP LOGO
  const [isLogoDialogOpen, setIsLogoDialogOpen] = useState(false)
  const [selectedFile, setSelectedFile] = useState(null)
  const [previewUrl, setPreviewUrl] = useState('')
  const [filePath, setFilePath] = useState('')



  const handleLogout = () => {
    const confirmLogout = window.confirm('¿Está seguro de que desea cerrar sesión?')
    if (confirmLogout) {
      localStorage.removeItem('token')
      navigate('/login')
    }
  }

  const handleEditEmail = () => {
    setIsEditingEmail(true)
    setEmailForm(email || '')
    setEmailError('')
    setEmailSuccess('')
  }

  const handleEditName = () => {
    setIsEditingName(true)
    setNameForm(companyName || '')
    setNameError('')
    setNameSuccess('')
  }

  const handleCancelEditName = () => {
    setIsEditingName(false)
    setNameForm(companyName || '')
    setNameError('')
  }

  const handleCancelEditEmail = () => {
    setIsEditingEmail(false)
    setEmailForm(email || '')
    setEmailError('')
  }

  const handleSaveName = () => {
    const token = localStorage.getItem('token')
    const updateData = { name: nameForm.trim() }

    fetch('http://localhost:8080/companies/name', {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(updateData)
    })
      .then(res => {
        console.log(res)
        if (!res.ok) {
          return res.json().then(err => {
            if (err.errors && Array.isArray(err.errors)) {
              throw new Error(err.errors.join(', '))
            }
            throw new Error(err.name || err.message || 'Error al actualizar nombre')
          })
        }
        return res
      })
      .then(() => {
        setIsEditingName(false)
        setNameError('')
        setNameSuccess('Nombre actualizado con éxito.')
        setTimeout(() => window.location.reload(), 2000)
      })
      .catch(err => {
        setNameError(err.message)
      })
  }

  const handleSaveEmail = () => {
    const token = localStorage.getItem('token')
    const updateData = { email: emailForm.trim() }

    fetch('http://localhost:8080/companies/email', {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(updateData)
    })
      .then(res => {
        console.log(res)
        if (!res.ok) {
          return res.json().then(err => {
            if (err.errors && Array.isArray(err.errors)) {
              throw new Error(err.errors.join(', '))
            }
            throw new Error(err.email || err.message || 'Error al actualizar email')
          })
        }
        return res
      })
      .then(() => {
        setIsEditingEmail(false)
        setEmailError('')
        setEmailSuccess('Email actualizado con éxito. Se ha enviado una notificación al email anterior.')
        setTimeout(() => window.location.reload(), 2000)
      })
      .catch(err => {
        setEmailError(err.message)
      })
  }

  const handleEditAddress = () => {
    setIsEditingAddress(true)
    setAddressForm(address || '')
    setAddressError('')
    setAddressSuccess('')
  }

  const handleCancelEditAddress = () => {
    setIsEditingAddress(false)
    setAddressForm(address || '')
    setAddressError('')
  }

  const handleSaveAddress = () => {
    const token = localStorage.getItem('token')
    const updateData = { address: addressForm.trim() }

    fetch('http://localhost:8080/companies/address', {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(updateData)
    })
      .then(res => {
        if (!res.ok) {
          return res.json().then(err => {
            if (err.errors && Array.isArray(err.errors)) {
              throw new Error(err.errors.join(', '))
            }
            throw new Error(err.message || 'Error al actualizar dirección')
          })
        }
        return res
      })
      .then(() => {
        setIsEditingAddress(false)
        setAddressError('')
        setAddressSuccess('Dirección actualizada con éxito.')
        setTimeout(() => window.location.reload(), 2000)
      })
      .catch(err => {
        setAddressError(err.message)
      })
  }

  const openPasswordDialog = () => {
    setIsPasswordDialogOpen(true)
    setActualPassword('')
    setNewPassword('')
    setPasswordError('')
  }

  const closePasswordDialog = () => {
    setIsPasswordDialogOpen(false)
    setPasswordError('')
  }

  const handleSavePassword = () => {
    setSavingPassword(true)
    setPasswordError('')
    const token = localStorage.getItem('token')
    const data = { actualPassword, newPassword }

    fetch('http://localhost:8080/companies/password', {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(data)
    })
      .then(res => {
        if (!res.ok) {
          return res.json().then(err => {
            const msg =
              err.newPassword ||
              err.actualPassword ||
              err.message ||
              'Error al cambiar contraseña'
            throw new Error(msg)
          })
        }
        return res
      })    
      .then(() => {
        closePasswordDialog()
      })
      .catch(err => {
        setPasswordError(err.message)
      })
      .finally(() => setSavingPassword(false))
  }

  // FUNCIONES NUEVAS PARA LOGO
  const openLogoDialog = () => {
    setIsLogoDialogOpen(true)
    setSelectedFile(null)
    setPreviewUrl('')
    setFilePath('')
  }

  const closeLogoDialog = () => {
    setIsLogoDialogOpen(false)
    setSelectedFile(null)
    setPreviewUrl('')
    setFilePath('')
  }

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0]
      setSelectedFile(file)
      setPreviewUrl(URL.createObjectURL(file))
      setFilePath(file.name)
    }
  }

  const handleDrop = (e) => {
    e.preventDefault()
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      const file = e.dataTransfer.files[0]
      setSelectedFile(file)
      setPreviewUrl(URL.createObjectURL(file))
      setFilePath(file.name)
    }
  }

  const handleDragOver = (e) => {
    e.preventDefault()
  }

  const handleUploadLogo = () => {
    if (!selectedFile) return

    const formData = new FormData()
    formData.append('logo', selectedFile)

    const token = localStorage.getItem('token')

    fetch('http://localhost:8080/companies/logo', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: formData
    })
      .then(() => {
        closeLogoDialog()
        window.location.reload()
      })
      .catch(err => {
        console.error('Error uploading logo:', err)
      })
  }

  const handleConnectMercadoPago = async () => {
    const popup = window.open('', 'MercadoPagoAuth', 'width=600,height=700');

    const messageHandler = (event) => {
      if (event.data?.mpLinked) {
        setMpMessage('Cuenta vinculada correctamente');
        setTimeout(() => setMpMessage(''), 5000);
        popup.close();
        window.removeEventListener('message', messageHandler);
      } else if (event.data?.mpError) {
        setMpMessage('Error al vincular: ' + event.data.mpError);
        setTimeout(() => setMpMessage(''), 5000);
        popup.close();
        window.removeEventListener('message', messageHandler);
      }
    };

    window.addEventListener('message', messageHandler);

    try {
      const token = localStorage.getItem('token');
      const resp = await fetch('http://localhost:8080/mp/connect', {
        headers: { Authorization: 'Bearer ' + token },
      });
      if (!resp.ok) throw new Error('No se pudo obtener la URL de Mercado Pago');
      const url = await resp.text();
      popup.location.href = url;
    } catch (e) {
      setMpMessage('Error al conectar Mercado Pago');
      setTimeout(() => setMpMessage(''), 5000);
      popup.close();
      window.removeEventListener('message', messageHandler);
    }
  };
  
  

  return (
    <Box mt={3}>
      <Box display="flex" justifyContent="center" alignItems="center" gap={6}>
        <Box
          width="50%"
          display="flex"
          flexDirection="column"
          alignItems="center"
          justifyContent="center"
          onMouseEnter={() => setHover(true)}
          onMouseLeave={() => setHover(false)}
          sx={{ position: 'relative' }}
        >
          <Box sx={{ position: 'relative' }}>
            <ProfileAvatar sx={{ width:300, height:300 }} />
          </Box>

          <Box display="flex" alignItems="center" gap={1} mt={1}>
            <Typography fontWeight="bold" color="black">Nombre:</Typography>
            {isEditingName ? (
              <Box display="flex" alignItems="center" gap={1}>
                <TextField
                  size="small"
                  type="text"
                  value={nameForm}
                  onChange={(e) => setNameForm(e.target.value)}
                  placeholder="Nombre de la compañía"
                  inputProps={{ maxLength: 100 }}
                  sx={{ minWidth: 200 }}
                />
                <IconButton size="small" onClick={handleSaveName} color="primary" sx={{ '&:focus': { outline: 'none' } }}>
                  <SaveIcon fontSize="small" />
                </IconButton>
                <IconButton size="small" onClick={handleCancelEditName} color="secondary" sx={{ '&:focus': { outline: 'none' } }}>
                  <CancelIcon fontSize="small" />
                </IconButton>
              </Box>
            ) : (
              <Box display="flex" alignItems="center" gap={1}>
                <Typography color="gray">{nameForm}</Typography>
                <IconButton size="small" onClick={handleEditName} sx={{ '&:focus': { outline: 'none' }, opacity: 0.7, '&:hover': { opacity: 1 } }}>
                  <EditIcon fontSize="small" />
                </IconButton>
              </Box>
            )}
          </Box>

          <Box display="flex" alignItems="center" gap={1} mt={1}>
            <Typography fontWeight="bold" color="black">Email:</Typography>
            {isEditingEmail ? (
              <Box display="flex" alignItems="center" gap={1}>
                <TextField
                  size="small"
                  type="email"
                  value={emailForm}
                  onChange={(e) => setEmailForm(e.target.value)}
                  placeholder="correo@ejemplo.com"
                  inputProps={{ maxLength: 100 }}
                  sx={{ minWidth: 200 }}
                />
                <IconButton size="small" onClick={handleSaveEmail} color="primary" sx={{ '&:focus': { outline: 'none' } }}>
                  <SaveIcon fontSize="small" />
                </IconButton>
                <IconButton size="small" onClick={handleCancelEditEmail} color="secondary" sx={{ '&:focus': { outline: 'none' } }}>
                  <CancelIcon fontSize="small" />
                </IconButton>
              </Box>
            ) : (
              <Box display="flex" alignItems="center" gap={1}>
                <Typography color="gray">{email}</Typography>
                <IconButton size="small" onClick={handleEditEmail} sx={{ '&:focus': { outline: 'none' }, opacity: 0.7, '&:hover': { opacity: 1 } }}>
                  <EditIcon fontSize="small" />
                </IconButton>
              </Box>
            )}
          </Box>
          <Box display="flex" alignItems="center" gap={1} mt={1}>
            <Typography fontWeight="bold" color="black">Dirección:</Typography>
            {isEditingAddress ? (
              <Box display="flex" alignItems="center" gap={1}>
                <TextField
                  size="small"
                  type="text"
                  value={addressForm}
                  onChange={(e) => setAddressForm(e.target.value)}
                  placeholder="Dirección"
                  inputProps={{ maxLength: 200 }}
                  sx={{ minWidth: 200 }}
                />
                <IconButton size="small" onClick={handleSaveAddress} color="primary" sx={{ '&:focus': { outline: 'none' } }}>
                  <SaveIcon fontSize="small" />
                </IconButton>
                <IconButton size="small" onClick={handleCancelEditAddress} color="secondary" sx={{ '&:focus': { outline: 'none' } }}>
                  <CancelIcon fontSize="small" />
                </IconButton>
              </Box>
            ) : (
              <Box display="flex" alignItems="center" gap={1}>
                <Typography color="gray">{address}</Typography>
                <IconButton size="small" onClick={handleEditAddress} sx={{ '&:focus': { outline: 'none' }, opacity: 0.7, '&:hover': { opacity: 1 } }}>
                  <EditIcon fontSize="small" />
                </IconButton>
              </Box>
            )}
          </Box>
        </Box>
        <Box
          width="50%"
          display="flex"
          flexDirection="column"
          justifyContent="center"
          alignItems="center"
          gap={2}
        >
          <Button variant="outlined" onClick={openPasswordDialog}>Cambiar Contraseña</Button>
          <Button variant="outlined" onClick={openLogoDialog}>Cambiar Logo</Button>
          <Box sx={{ position: 'relative', display: 'inline-block' }}>
            <Button 
              variant="outlined" 
              onClick={handleConnectMercadoPago}
            >
              Vincular Mercado Pago
            </Button>
            {mpMessage && (
              <Box 
                sx={{ 
                  position: 'absolute',
                  top: '100%',
                  left: '50%',
                  transform: 'translateX(-50%)',
                  mt: 1,
                  zIndex: 1000,
                  width: '400px',
                  display: 'flex',
                  justifyContent: 'center'
                }}
              >
                <Alert 
                  severity={mpMessage.startsWith('Error') ? 'error' : 'success'} 
                  onClose={() => setMpMessage('')} 
                  sx={{ maxWidth: 600 }}
                >
                  {mpMessage}
                </Alert>
              </Box>
            )}
          </Box>
          <Button
            onClick={handleLogout}
            sx={{
              mt: 4,
              backgroundColor: '#f5655b',
              color: 'white',
              '&:hover': { backgroundColor: '#d32f2f' }
            }}
          >
            CERRAR SESIÓN
          </Button>
        </Box>
      </Box>

      <Dialog open={isPasswordDialogOpen} onClose={closePasswordDialog}>
        <DialogTitle>Cambiar contraseña</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="Contraseña actual"
            type="password"
            fullWidth
            value={actualPassword}
            onChange={e => setActualPassword(e.target.value)}
          />
          <TextField
            margin="dense"
            label="Nueva contraseña"
            type="password"
            fullWidth
            value={newPassword}
            onChange={e => setNewPassword(e.target.value)}
          />
          {passwordError && (
            <Typography variant="body2" color="error" sx={{ mt: 1 }}>
              {passwordError}
            </Typography>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={closePasswordDialog} disabled={savingPassword}>Cancelar</Button>
          <Button onClick={handleSavePassword} disabled={savingPassword}>Guardar</Button>
        </DialogActions>
      </Dialog>

      <Dialog
        open={isLogoDialogOpen}
        onClose={closeLogoDialog}
        PaperProps={{
          sx: {
            width: 400,
            height: 400,
            display: 'flex',
            flexDirection: 'column',
            p: 2,
            position: 'relative',
            overflow: 'hidden',
            boxSizing: 'border-box'
          }
        }}
      >
        <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', p: '8px 0' }}>
          <Box sx={{ flex: '1 1 auto' }}>
            <TextField
              value={filePath}
              InputProps={{
                readOnly: true
              }}
              size="small"
              sx={{ width: 'calc(100% - 30px)', mr: 1 }}
            />
          </Box>
          <Button
            variant="outlined"
            component="label"
            sx={{ minWidth: 100, p: 0, height: 32, fontSize: 12 }}
          >
            Explorar
            <input
              type="file"
              accept="image/*"
              hidden
              onChange={handleFileChange}
            />
          </Button>
        </DialogTitle>
        <DialogContent
          onDrop={handleDrop}
          onDragOver={handleDragOver}
          sx={{
            flexGrow: 1,
            border: '2px dashed #ccc',
            borderRadius: 1,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            position: 'relative',
            bgcolor: '#fafafa',
            userSelect: 'none',
            px: 1,
            overflow: 'hidden'
          }}
        >
          {previewUrl ? (
            <Box
              component="img"
              src={previewUrl}
              alt="Logo preview"
              sx={{
                maxWidth: '100%',
                maxHeight: '100%',
                objectFit: 'contain'
              }}
            />
          ) : (
            <Typography
              color="text.secondary"
              textAlign="center"
              sx={{ userSelect: 'none' }}
            >
              Arrastra o agrega una imagen
            </Typography>
          )}
        </DialogContent>
        <DialogActions sx={{ justifyContent: 'space-between', px: 1 }}>
          <Typography
            sx={{ fontSize: 12, color: 'text.secondary', userSelect: 'none', pl: 1 }}
          >
            La imagen se agregará a las facturas generadas
          </Typography>
          <Button
            variant="contained"
            onClick={handleUploadLogo}
            disabled={!selectedFile}
          >
            Agregar
          </Button>
        </DialogActions>
      </Dialog>

      {emailError && (
        <Box mt={2} display="flex" justifyContent="center">
          <Alert severity="error" onClose={() => setEmailError('')} sx={{ maxWidth: 600 }}>
            {emailError}
          </Alert>
        </Box>
      )}
      {addressError && (
        <Box mt={2} display="flex" justifyContent="center">
          <Alert severity="error" onClose={() => setAddressError('')} sx={{ maxWidth: 600 }}>
            {addressError}
          </Alert>
        </Box>
      )}
      {emailSuccess && (
        <Box mt={2} display="flex" justifyContent="center">
          <Alert severity="success" onClose={() => setEmailSuccess('')} sx={{ maxWidth: 600 }}>
            {emailSuccess}
          </Alert>
        </Box>
      )}
      {addressSuccess && (
        <Box mt={2} display="flex" justifyContent="center">
          <Alert severity="success" onClose={() => setAddressSuccess('')} sx={{ maxWidth: 600 }}>
            {addressSuccess}
          </Alert>
        </Box>
      )}
    </Box>
  )
}

export default CompanyInfo
