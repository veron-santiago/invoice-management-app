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

const ProductList = () => {
  const [products, setProducts] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [rowsPerPage, setRowsPerPage] = useState(10)
  const [order, setOrder] = useState('asc')
  const [orderBy, setOrderBy] = useState('name')
  const [searchQuery, setSearchQuery] = useState('')
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [productToDelete, setProductToDelete] = useState(null)
  const [editingProduct, setEditingProduct] = useState(null)
  const [editForm, setEditForm] = useState({ name: '', code: '', price: '' })
  const [updateError, setUpdateError] = useState('')
  const [showCreateForm, setShowCreateForm] = useState(false)
  const [createForm, setCreateForm] = useState({ name: '', code: '', price: '' })
  const [createError, setCreateError] = useState('')
  const [createSuccess, setCreateSuccess] = useState('')

  useEffect(() => {
    const token = localStorage.getItem('token')

    fetch('http://localhost:8080/products', {
      headers: { Authorization: `Bearer ${token}` }
    })
      .then(res => {
        if (!res.ok) throw new Error('Error al obtener productos')
        return res.json()
      })
      .then(data => {
        setProducts(data)
        setIsLoading(false)
      })
      .catch(err => {
        console.error(err)
        setIsLoading(false)
      })
  }, [])

  const formatNumber = (number) =>
    number.toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })

  const handleRequestSort = (property) => {
    const isAsc = orderBy === property && order === 'asc'
    setOrder(isAsc ? 'desc' : 'asc')
    setOrderBy(property)
  }

  const sortProducts = (array) => {
    return [...array].sort((a, b) => {
      const aVal = a[orderBy]
      const bVal = b[orderBy]
      if (typeof aVal === 'string') {
        return order === 'asc' ? aVal.localeCompare(bVal) : bVal.localeCompare(aVal)
      }
      return order === 'asc' ? aVal - bVal : bVal - aVal
    })
  }

  const filterProducts = (products) => {
    const normalizedQuery = searchQuery.toLowerCase().replace(/\./g, '')
    return products.filter((product) => {
      const name = product.name.toLowerCase()
      const price = formatNumber(product.price).replace(/\./g, '')
      return name.includes(normalizedQuery) || price.includes(normalizedQuery)
    })
  }

  const handleDeleteClick = (product) => {
    setProductToDelete(product)
    setDeleteDialogOpen(true)
  }

  const confirmDelete = () => {
    const token = localStorage.getItem('token')
    fetch(`http://localhost:8080/products/${productToDelete.id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token}` }
    })
      .then(res => {
        if (!res.ok) throw new Error('Error al eliminar producto')
        setProducts(products.filter(p => p.id !== productToDelete.id))
        setDeleteDialogOpen(false)
        setProductToDelete(null)
      })
      .catch(err => {
        console.error(err)
        setDeleteDialogOpen(false)
        setProductToDelete(null)
      })
  }

  const handleEditClick = (product) => {
    setEditingProduct(product.id)
    setEditForm({
      name: product.name ?? '',
      code: product.code ?? '',
      price: product.price.toString()
    })
    setUpdateError('')
  }

  const handleCancelEdit = () => {
    setEditingProduct(null)
    setEditForm({ name: '', code: '', price: '' })
    setUpdateError('')
  }

  const handleSaveEdit = () => {
    const token = localStorage.getItem('token')
    
    // Prepare update data with proper handling of optional code field
    const updateData = {
      name: editForm.name.trim(),
      code: editForm.code.trim() === '' ? '' : editForm.code.trim(), // Send empty string for optional field
      price: parseFloat(editForm.price)
    }
    
    console.log('Sending product update data:', updateData) // Debug log

    fetch(`http://localhost:8080/products/${editingProduct}`, {
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
            throw new Error(err.code || err.name || err.price || err.message || 'Error al actualizar producto')
          })
        }
        return res.json()
      })
      .then(updatedProduct => {
        console.log('Received updated product:', updatedProduct) // Debug log
        setProducts(products.map(p => 
          p.id === editingProduct ? updatedProduct : p
        ))
        setEditingProduct(null)
        setEditForm({ name: '', code: '', price: '' })
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
    setCreateForm({ name: '', code: '', price: '' })
    setCreateError('')
    setCreateSuccess('')
  }

  const handleCloseCreateForm = () => {
    setShowCreateForm(false)
    setCreateForm({ name: '', code: '', price: '' })
    setCreateError('')
    setCreateSuccess('')
  }

  const handleCreateFormChange = (field, value) => {
    setCreateForm(prev => ({ ...prev, [field]: value }))
  }

  const handleClearCreateField = (field) => {
    setCreateForm(prev => ({ ...prev, [field]: '' }))
  }

  const handleCreateProduct = () => {
    const token = localStorage.getItem('token')
    const createData = {
      name: createForm.name.trim(),
      code: createForm.code.trim() === '' ? '' : createForm.code.trim(), // Send empty string for optional field
      price: createForm.price.trim() === '' ? null : parseFloat(createForm.price)
    }
    
    console.log('Sending product create data:', createData) // Debug log

    // Validate required fields
    if (!createData.name) {
      setCreateError('El nombre del producto es obligatorio')
      return
    }

    fetch('http://localhost:8080/products', {
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
            throw new Error(err.code || err.name || err.price || err.message || 'Error al crear producto')
          })
        }
        return res.json()
      })
      .then(newProduct => {
        setProducts(prev => [...prev, newProduct])
        setCreateForm({ name: '', code: '', price: '' })
        setCreateError('')
        setCreateSuccess('Producto creado con éxito')
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
              PRODUCTO
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
                  <TableCell sx={{ width: '20%', fontWeight: 'bold' }}>
                    Código
                  </TableCell>
                  <TableCell sx={{ width: '45%', fontWeight: 'bold' }}>
                    Producto
                  </TableCell>
                  <TableCell sx={{ width: '25%', fontWeight: 'bold' }}>
                    Precio
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
                    <Box display="flex" alignItems="center">
                      <TextField
                        size="small"
                        value={createForm.code}
                        onChange={(e) => handleCreateFormChange('code', e.target.value)}
                        placeholder="Código (opcional)"
                        inputProps={{ maxLength: 8 }}
                        sx={{ flexGrow: 1 }}
                      />
                      {createForm.code && (
                        <IconButton
                          size="small"
                          onClick={() => handleClearCreateField('code')}
                          sx={{ ml: 1, opacity: 0.6, '&:hover': { opacity: 1 } }}
                        >
                          <ClearIcon fontSize="small" />
                        </IconButton>
                      )}
                    </Box>
                  </TableCell>
                  <TableCell>
                    <TextField
                      size="small"
                      value={createForm.name}
                      onChange={(e) => handleCreateFormChange('name', e.target.value)}
                      placeholder="Nombre del producto *"
                      inputProps={{ maxLength: 100 }}
                      fullWidth
                      required
                    />
                  </TableCell>
                  <TableCell>
                    <Box display="flex" alignItems="center">
                      <TextField
                        size="small"
                        type="number"
                        value={createForm.price}
                        onChange={(e) => handleCreateFormChange('price', e.target.value)}
                        placeholder="Precio"
                        inputProps={{ min: 0.01, max: 10000000, step: 0.01 }}
                        sx={{ flexGrow: 1 }}
                      />
                      {createForm.price && (
                        <IconButton
                          size="small"
                          onClick={() => handleClearCreateField('price')}
                          sx={{ ml: 1, opacity: 0.6, '&:hover': { opacity: 1 } }}
                        >
                          <ClearIcon fontSize="small" />
                        </IconButton>
                      )}
                    </Box>
                  </TableCell>
                  <TableCell align="center">
                    <IconButton
                      onClick={handleCreateProduct}
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
                  sx={{ cursor: 'pointer', width: '20%' }}
                  onClick={() => handleRequestSort('code')}
                >
                  Código <ArrowDropDownIcon sx={{ fontSize: 16 }} />
                </TableCell>
                <TableCell
                  sx={{ cursor: 'pointer', width: '45%' }}
                  onClick={() => handleRequestSort('name')}
                >
                  Producto <ArrowDropDownIcon sx={{ fontSize: 16 }} />
                </TableCell>
                <TableCell
                  sx={{ width: '25%', cursor: 'pointer' }}
                  onClick={() => handleRequestSort('price')}
                >
                  Precio <ArrowDropDownIcon sx={{ fontSize: 16 }} />
                </TableCell>
                <TableCell align="center" sx={{ width: '10%' }} />
              </TableRow>
            </TableHead>
            <TableBody>
              {sortProducts(filterProducts(products))
                .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
.map((product) => (
                  <TableRow key={product.id}>
                    <TableCell>
                      {editingProduct === product.id ? (
                        <Box display="flex" alignItems="center">
                          <TextField
                            size="small"
                            value={editForm.code}
                            onChange={(e) => handleFormChange('code', e.target.value)}
                            placeholder="Código (opcional)"
                            inputProps={{ maxLength: 8 }}
                            sx={{ flexGrow: 1 }}
                          />
                          {editForm.code && (
                            <IconButton
                              size="small"
                              onClick={() => handleClearField('code')}
                              sx={{ ml: 1, opacity: 0.6, '&:hover': { opacity: 1 } }}
                            >
                              <ClearIcon fontSize="small" />
                            </IconButton>
                          )}
                        </Box>
                      ) : (
                        product.code || '-'
                      )}
                    </TableCell>
                    <TableCell>
                      {editingProduct === product.id ? (
                        <TextField
                          size="small"
                          value={editForm.name}
                          onChange={(e) => handleFormChange('name', e.target.value)}
                          placeholder="Nombre del producto"
                          inputProps={{ maxLength: 100 }}
                          fullWidth
                        />
                      ) : (
                        product.name
                      )}
                    </TableCell>
                    <TableCell>
                      {editingProduct === product.id ? (
                        <TextField
                          size="small"
                          type="number"
                          value={editForm.price}
                          onChange={(e) => handleFormChange('price', e.target.value)}
                          placeholder="Precio"
                          inputProps={{ min: 0.01, max: 10000000, step: 0.01 }}
                        />
                      ) : (
                        `$${formatNumber(product.price)}`
                      )}
                    </TableCell>
                    <TableCell align="center">
                      <Box display="flex" justifyContent="center">
                        {editingProduct === product.id ? (
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
                              onClick={() => handleEditClick(product)}
                              sx={{ mr: 1, '&:focus': { outline: 'none' } }}
                            >
                              <EditIcon />
                            </IconButton>
                            <IconButton
                              onClick={() => handleDeleteClick(product)}
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
            count={filterProducts(products).length}
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
        <DialogTitle>Eliminar producto</DialogTitle>
        <DialogContent>
          <DialogContentText>
            ¿Está seguro de que quiere eliminar <b>{productToDelete?.name}</b>?
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

export default ProductList
