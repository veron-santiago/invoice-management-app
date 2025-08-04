import { useState, useEffect } from 'react'
import {
  Box,
  Typography,
  CircularProgress,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  TableContainer,
  Paper,
  TablePagination,
  IconButton,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  TextField,
  Alert
} from '@mui/material'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import SaveIcon from '@mui/icons-material/Save'
import CancelIcon from '@mui/icons-material/Cancel'
import ClearIcon from '@mui/icons-material/Clear'
import ArrowDropDownIcon from '@mui/icons-material/ArrowDropDown'
import NoteAddIcon from '@mui/icons-material/NoteAdd'
import SearchInput from '../SearchInput'

const CustomerList = () => {
  const [customers, setCustomers] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [rowsPerPage, setRowsPerPage] = useState(10)
  const [order, setOrder] = useState('asc')
  const [orderBy, setOrderBy] = useState('name')
  const [searchQuery, setSearchQuery] = useState('')
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [customerToDelete, setCustomerToDelete] = useState(null)
  const [editingCustomer, setEditingCustomer] = useState(null)
  const [editForm, setEditForm] = useState({ name: '', email: '', address: '' })
  const [updateError, setUpdateError] = useState('')
  const [showCreateForm, setShowCreateForm] = useState(false)
  const [createForm, setCreateForm] = useState({ name: '', email: '', address: '' })
  const [createError, setCreateError] = useState('')
  const [createSuccess, setCreateSuccess] = useState('')

  useEffect(() => {
    const token = localStorage.getItem('token')

    fetch('http://localhost:8080/customers', {
      headers: { Authorization: `Bearer ${token}` }
    })
      .then(res => {
        if (!res.ok) throw new Error('Error al obtener clientes')
        return res.json()
      })
      .then(data => {
        setCustomers(data)
        setIsLoading(false)
      })
      .catch(err => {
        console.error(err)
        setIsLoading(false)
      })
  }, [])

  const handleRequestSort = (property) => {
    const isAsc = orderBy === property && order === 'asc'
    setOrder(isAsc ? 'desc' : 'asc')
    setOrderBy(property)
  }

  const sortCustomers = (array) => {
    return [...array].sort((a, b) => {
      const aVal = a[orderBy] || ''
      const bVal = b[orderBy] || ''
      if (typeof aVal === 'string') {
        return order === 'asc' ? aVal.localeCompare(bVal) : bVal.localeCompare(aVal)
      }
      return order === 'asc' ? aVal - bVal : bVal - aVal
    })
  }

  const filterCustomers = (customers) => {
    const normalizedQuery = searchQuery.toLowerCase()
    return customers.filter((customer) => {
      const name = customer.name?.toLowerCase() || ''
      const email = customer.email?.toLowerCase() || ''
      const address = customer.address?.toLowerCase() || ''
      return name.includes(normalizedQuery) || email.includes(normalizedQuery) || address.includes(normalizedQuery)
    })
  }

  const handleDeleteClick = (customer) => {
    setCustomerToDelete(customer)
    setDeleteDialogOpen(true)
  }

  const confirmDelete = () => {
    const token = localStorage.getItem('token')
    fetch(`http://localhost:8080/customers/${customerToDelete.id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token}` }
    })
      .then(res => {
        if (!res.ok) throw new Error('Error al eliminar cliente')
        setCustomers(customers.filter(c => c.id !== customerToDelete.id))
        setDeleteDialogOpen(false)
        setCustomerToDelete(null)
      })
      .catch(err => {
        console.error(err)
        setDeleteDialogOpen(false)
        setCustomerToDelete(null)
      })
  }

  const handleEditClick = (customer) => {
    setEditingCustomer(customer.id)
    setEditForm({
      name: customer.name || '',
      email: customer.email || '',
      address: customer.address || ''
    })
    setUpdateError('')
  }

  const handleCancelEdit = () => {
    setEditingCustomer(null)
    setEditForm({ name: '', email: '', address: '' })
    setUpdateError('')
  }

  const handleSaveEdit = () => {
    const token = localStorage.getItem('token')
    
    // Prepare update data with proper handling of optional fields
    const updateData = {
      name: editForm.name.trim() === '' ? '' : editForm.name.trim(), // Required field
      email: editForm.email.trim() === '' ? null : editForm.email.trim(), // Send empty string for optional field
      address: editForm.address.trim() === '' ? null : editForm.address.trim() // Send empty string for optional field
    }
    
    console.log('Sending update data:', updateData) // Debug log

    fetch(`http://localhost:8080/customers/${editingCustomer}`, {
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
            // Handle validation errors from backend
            if (err.errors && Array.isArray(err.errors)) {
              throw new Error(err.errors.join(', '))
            }
            throw new Error(err.name || err.email || err.address || err.message || 'Error al actualizar cliente')
          })
        }
        return res.json()
      })
      .then(updatedCustomer => {
        console.log('Received updated customer:', updatedCustomer) // Debug log
        setCustomers(customers.map(c => 
          c.id === editingCustomer ? updatedCustomer : c
        ))
        setEditingCustomer(null)
        setEditForm({ name: '', email: '', address: '' })
        setUpdateError('')
      })
      .catch(err => {
        console.error(err)
        setUpdateError(err.message)
      })
  }

  const handleFormChange = (field, value) => {
    setEditForm(prev => ({ ...prev, [field]: value }))
  }

  const handleClearField = (field) => {
    setEditForm(prev => ({ ...prev, [field]: '' }))
  }

  const handleShowCreateForm = () => {
    setShowCreateForm(true)
    setCreateForm({ name: '', email: '', address: '' })
    setCreateError('')
    setCreateSuccess('')
  }

  const handleCloseCreateForm = () => {
    setShowCreateForm(false)
    setCreateForm({ name: '', email: '', address: '' })
    setCreateError('')
    setCreateSuccess('')
  }

  const handleCreateFormChange = (field, value) => {
    setCreateForm(prev => ({ ...prev, [field]: value }))
  }

  const handleClearCreateField = (field) => {
    setCreateForm(prev => ({ ...prev, [field]: '' }))
  }

  const handleCreateCustomer = () => {
    const token = localStorage.getItem('token')
    const createData = {
      name: createForm.name.trim(),
      email: createForm.email.trim() === '' ? null : createForm.email.trim(),
      address: createForm.address.trim() === '' ? null : createForm.address.trim()
    }

    // Validate required fields
    if (!createData.name) {
      setCreateError('El nombre del cliente es obligatorio')
      return
    }

    fetch('http://localhost:8080/customers', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(createData)
    })
      .then(res => {
        if (!res.ok) {
          return res.json().then(err => {
            // Handle validation errors from backend
            if (err.errors && Array.isArray(err.errors)) {
              throw new Error(err.errors.join(', '))
            }
            throw new Error(err.name || err.email || err.address || err.message || 'Error al crear cliente')
          })
        }
        return res.json()
      })
      .then(newCustomer => {
        setCustomers(prev => [...prev, newCustomer])
        setCreateForm({ name: '', email: '', address: '' })
        setCreateError('')
        setCreateSuccess('Cliente creado con éxito')
        // Clear success message after 3 seconds
        setTimeout(() => setCreateSuccess(''), 3000)
      })
      .catch(err => {
        console.error(err)
        setCreateError(err.message)
        setCreateSuccess('')
      })
  }

  return (
    <Box mt={3}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Button
          onClick={handleShowCreateForm}
          disableElevation
          sx={{
            display: 'flex',
            alignItems: 'center',
            bgcolor: '#e0e0e0',
            p: 2.5,
            borderRadius: 3,
            width: 'fit-content',
            boxShadow: 1,
            '&:hover': { bgcolor: '#d5d5d5' },
            textTransform: 'none',
            color: 'black',
            outline: 'none',
            '&:focus': {
              outline: 'none'
            }
          }}
        >
          <Box mr={2} display="flex" alignItems="center">
            <NoteAddIcon sx={{ fontSize: 60, color: 'black' }} />
          </Box>
          <Box>
            <Typography variant="h6" lineHeight={1.2} sx={{ color: 'black' }}>
              AGREGAR
            </Typography>
            <Typography variant="h6" lineHeight={1.2} sx={{ color: 'black' }}>
              CLIENTE
            </Typography>
          </Box>
        </Button>
        <SearchInput value={searchQuery} onChange={setSearchQuery} />
      </Box>

      {showCreateForm && (
        <Box mb={3}>
          <TableContainer component={Paper} sx={{ mb: 2 }}>
            <Table size="small">
              <TableHead sx={{ backgroundColor: '#f5f5f5' }}>
                <TableRow>
                  <TableCell sx={{ width: '33.33%', fontWeight: 'bold' }}>
                    Cliente
                  </TableCell>
                  <TableCell sx={{ width: '33.33%', fontWeight: 'bold' }}>
                    Correo
                  </TableCell>
                  <TableCell sx={{ width: '33.33%', fontWeight: 'bold' }}>
                    Dirección
                  </TableCell>
                  <TableCell align="center" sx={{ width: '10%' }}>
                    <IconButton
                      onClick={handleCloseCreateForm}
                      size="small"
                      sx={{ '&:focus': { outline: 'none' } }}
                    >
                      <CancelIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                <TableRow>
                  <TableCell>
                    <TextField
                      size="small"
                      value={createForm.name}
                      onChange={(e) => handleCreateFormChange('name', e.target.value)}
                      placeholder="Nombre del cliente *"
                      inputProps={{ maxLength: 100 }}
                      fullWidth
                      required
                    />
                  </TableCell>
                  <TableCell>
                    <Box display="flex" alignItems="center">
                      <TextField
                        size="small"
                        type="email"
                        value={createForm.email}
                        onChange={(e) => handleCreateFormChange('email', e.target.value)}
                        placeholder="Correo (opcional)"
                        inputProps={{ maxLength: 100 }}
                        sx={{ flexGrow: 1 }}
                      />
                      {createForm.email && (
                        <IconButton
                          size="small"
                          onClick={() => handleClearCreateField('email')}
                          sx={{ ml: 1, opacity: 0.6, '&:hover': { opacity: 1 } }}
                        >
                          <ClearIcon fontSize="small" />
                        </IconButton>
                      )}
                    </Box>
                  </TableCell>
                  <TableCell>
                    <Box display="flex" alignItems="center">
                      <TextField
                        size="small"
                        value={createForm.address}
                        onChange={(e) => handleCreateFormChange('address', e.target.value)}
                        placeholder="Dirección (opcional)"
                        inputProps={{ maxLength: 200 }}
                        sx={{ flexGrow: 1 }}
                      />
                      {createForm.address && (
                        <IconButton
                          size="small"
                          onClick={() => handleClearCreateField('address')}
                          sx={{ ml: 1, opacity: 0.6, '&:hover': { opacity: 1 } }}
                        >
                          <ClearIcon fontSize="small" />
                        </IconButton>
                      )}
                    </Box>
                  </TableCell>
                  <TableCell align="center">
                    <IconButton
                      onClick={handleCreateCustomer}
                      color="primary"
                      sx={{ '&:focus': { outline: 'none' } }}
                    >
                      <SaveIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              </TableBody>
            </Table>
          </TableContainer>
          
          {createError && (
            <Box mb={2}>
              <Alert severity="error" onClose={() => setCreateError('')}>
                {createError}
              </Alert>
            </Box>
          )}
          
          {createSuccess && (
            <Box mb={2}>
              <Alert severity="success" onClose={() => setCreateSuccess('')}>
                {createSuccess}
              </Alert>
            </Box>
          )}
        </Box>
      )}

      {!isLoading && (
        <TableContainer component={Paper}>
          <Table size="small">
            <TableHead sx={{ backgroundColor: '#f5f5f5' }}>
              <TableRow>
                <TableCell
                  sx={{ cursor: 'pointer', width: '33.33%' }}
                  onClick={() => handleRequestSort('name')}
                >
                  Cliente <ArrowDropDownIcon sx={{ fontSize: 16 }} />
                </TableCell>
                <TableCell
                  sx={{ width: '33.33%', cursor: 'pointer' }}
                  onClick={() => handleRequestSort('email')}
                >
                  Correo <ArrowDropDownIcon sx={{ fontSize: 16 }} />
                </TableCell>
                <TableCell
                  sx={{ width: '33.33%', cursor: 'pointer' }}
                  onClick={() => handleRequestSort('address')}
                >
                  Dirección <ArrowDropDownIcon sx={{ fontSize: 16 }} />
                </TableCell>
                <TableCell align="center" sx={{ width: '10%' }} />
              </TableRow>
            </TableHead>
            <TableBody>
              {sortCustomers(filterCustomers(customers))
                .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
.map((customer) => (
                  <TableRow key={customer.id}>
                    <TableCell>
                      {editingCustomer === customer.id ? (
                        <TextField
                          size="small"
                          value={editForm.name}
                          onChange={(e) => handleFormChange('name', e.target.value)}
                          placeholder="Nombre del cliente"
                          inputProps={{ maxLength: 100 }}
                          fullWidth
                          required
                        />
                      ) : (
                        customer.name
                      )}
                    </TableCell>
                    <TableCell>
                      {editingCustomer === customer.id ? (
                        <Box display="flex" alignItems="center">
                          <TextField
                            size="small"
                            type="email"
                            value={editForm.email}
                            onChange={(e) => handleFormChange('email', e.target.value)}
                            placeholder="Correo (opcional)"
                            inputProps={{ maxLength: 100 }}
                            sx={{ flexGrow: 1 }}
                          />
                          {editForm.email && (
                            <IconButton
                              size="small"
                              onClick={() => handleClearField('email')}
                              sx={{ ml: 1, opacity: 0.6, '&:hover': { opacity: 1 } }}
                            >
                              <ClearIcon fontSize="small" />
                            </IconButton>
                          )}
                        </Box>
                      ) : (
                        customer.email || '-'
                      )}
                    </TableCell>
                    <TableCell>
                      {editingCustomer === customer.id ? (
                        <Box display="flex" alignItems="center">
                          <TextField
                            size="small"
                            value={editForm.address}
                            onChange={(e) => handleFormChange('address', e.target.value)}
                            placeholder="Dirección (opcional)"
                            inputProps={{ maxLength: 200 }}
                            sx={{ flexGrow: 1 }}
                          />
                          {editForm.address && (
                            <IconButton
                              size="small"
                              onClick={() => handleClearField('address')}
                              sx={{ ml: 1, opacity: 0.6, '&:hover': { opacity: 1 } }}
                            >
                              <ClearIcon fontSize="small" />
                            </IconButton>
                          )}
                        </Box>
                      ) : (
                        customer.address || '-'
                      )}
                    </TableCell>
                    <TableCell align="center">
                      <Box display="flex" justifyContent="center">
                        {editingCustomer === customer.id ? (
                          <>
                            <IconButton 
                              onClick={handleSaveEdit}
                              sx={{ mr: 1, '&:focus': { outline: 'none' } }}
                              color="primary"
                            >
                              <SaveIcon />
                            </IconButton>
                            <IconButton
                              onClick={handleCancelEdit}
                              sx={{ '&:focus': { outline: 'none' } }}
                              color="secondary"
                            >
                              <CancelIcon />
                            </IconButton>
                          </>
                        ) : (
                          <>
                            <IconButton 
                              onClick={() => handleEditClick(customer)}
                              sx={{ mr: 1, '&:focus': { outline: 'none' } }}
                            >
                              <EditIcon />
                            </IconButton>
                            <IconButton
                              onClick={() => handleDeleteClick(customer)}
                              sx={{ '&:focus': { outline: 'none' } }}
                            >
                              <DeleteIcon />
                            </IconButton>
                          </>
                        )}
                      </Box>
                    </TableCell>
                  </TableRow>
                ))}
            </TableBody>
          </Table>

          <TablePagination
            rowsPerPageOptions={[5, 10, 25]}
            component="div"
            count={filterCustomers(customers).length}
            rowsPerPage={rowsPerPage}
            page={page}
            onPageChange={(_, newPage) => setPage(newPage)}
            onRowsPerPageChange={(e) => {
              setRowsPerPage(parseInt(e.target.value, 10))
              setPage(0)
            }}
          />
        </TableContainer>
      )}

      {isLoading && (
        <Box display="flex" justifyContent="center" mt={4}>
          <CircularProgress />
        </Box>
      )}

      {updateError && (
        <Box mt={2}>
          <Alert severity="error" onClose={() => setUpdateError('')}>
            {updateError}
          </Alert>
        </Box>
      )}

      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>Eliminar cliente</DialogTitle>
        <DialogContent>
          <DialogContentText>
            ¿Está seguro de que quiere eliminar a <b>{customerToDelete?.name}</b>?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>Cancelar</Button>
          <Button onClick={confirmDelete} color="error">Eliminar</Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}

export default CustomerList
