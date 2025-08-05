import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
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
  Button
} from '@mui/material'
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf'
import ArrowDropDownIcon from '@mui/icons-material/ArrowDropDown'
import NoteAddIcon from '@mui/icons-material/NoteAdd'
import dayjs from 'dayjs'
import SearchInput from '../SearchInput'

const BillList = () => {
  const [bills, setBills] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [rowsPerPage, setRowsPerPage] = useState(10)
  const [order, setOrder] = useState('desc')
  const [orderBy, setOrderBy] = useState('billNumber')
  const [searchQuery, setSearchQuery] = useState('')
  const navigate = useNavigate()

  useEffect(() => {
    const token = localStorage.getItem('token')

    fetch('https://invoice-management-app-3g3w.onrender.com/bills', {
      headers: { Authorization: `Bearer ${token}` }
    })
      .then(res => {
        if (!res.ok) throw new Error('Error al obtener facturas')
        return res.json()
      })
      .then(data => {
        setBills(data)
        setIsLoading(false)
      })
      .catch(err => {
        console.error(err)
        setIsLoading(false)
      })
  }, [])

  const formatNumber = (number) =>
    number.toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })

  const computeStatus = (bill) =>
    bill.paymentStatus
      ? 'Abonado'
      : dayjs().isAfter(bill.dueDate)
      ? 'QR Caducado'
      : 'Pendiente'

  const handleRequestSort = (property) => {
    const prop = property === 'issueDate' || property === 'dueDate' ? 'billNumber' : property
    const isAsc = orderBy === prop && order === 'asc'
    setOrder(isAsc ? 'desc' : 'asc')
    setOrderBy(prop)
  }

  const sortBills = (array) => {
    return [...array].sort((a, b) => {
      const getValue = (bill) => {
        switch (orderBy) {
          case 'billNumber':
            return bill.billNumber
          case 'totalAmount':
            return bill.totalAmount
          case 'customer.name':
            return bill.customerName.toLowerCase()
          case 'paymentStatus':
            return computeStatus(bill).toLowerCase()
          default:
            return bill.billNumber
        }
      }
      const aVal = getValue(a)
      const bVal = getValue(b)
      if (typeof aVal === 'string') {
        return order === 'asc' ? aVal.localeCompare(bVal) : bVal.localeCompare(aVal)
      }
      return order === 'asc' ? aVal - bVal : bVal - aVal
    })
  }

  const filterBills = (bills) => {
    const normalizedQuery = searchQuery.toLowerCase().replace(/\./g, '')

    return bills.filter((bill) => {
      const issueDate = dayjs(bill.issueDate).format('DD/MM/YYYY')
      const dueDate = dayjs(bill.dueDate).format('DD/MM/YYYY')
      const billNumber = bill.billNumber.toString().padStart(8, '0')
      const customerName = bill.customerName
      const status = computeStatus(bill)
      const totalAmount = formatNumber(bill.totalAmount).replace(/\./g, '')

      const values = [
        issueDate,
        dueDate,
        billNumber,
        customerName,
        totalAmount,
        status
      ]

      return values.some((val) =>
        val.toLowerCase().includes(normalizedQuery)
      )
    })
  }

  const handleDownloadPdf = async (billId) => {
    console.log('Iniciando descarga de PDF para bill ID:', billId)
    console.log('Tipo de billId:', typeof billId)
    console.log('billId es undefined?', billId === undefined)
    console.log('billId es null?', billId === null)
    
    try {
      const token = localStorage.getItem('token')
      console.log('Token obtenido:', token ? 'Sí' : 'No')
      
      const url = `http://localhost:8080/bills/${billId}/pdf`
      console.log('URL de descarga EXACTA:', url)
      console.log('URL de descarga length:', url.length)
      
      const response = await fetch(url, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      })

      console.log('Response status:', response.status)
      console.log('Response headers:', response.headers)

      if (!response.ok) {
        const errorText = await response.text()
        console.error('Error response:', errorText)
        throw new Error(`Error ${response.status}: ${errorText}`)
      }

      console.log('Convirtiendo a blob...')
      const blob = await response.blob()
      console.log('Blob creado, tamaño:', blob.size, 'bytes')
      
      const downloadUrl = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = downloadUrl
      link.download = `factura-${billId}.pdf`
      document.body.appendChild(link)
      console.log('Iniciando descarga...')
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(downloadUrl)
      console.log('Descarga completada')
    } catch (error) {
      console.error('Error completo downloading PDF:', error)
      alert(`Error al descargar PDF: ${error.message}`)
    }
  }

  return (
    <Box mt={3}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Button
          onClick={() => navigate("/bills/create")}
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
              FACTURA
            </Typography>
          </Box>
        </Button>
        <SearchInput value={searchQuery} onChange={setSearchQuery} />
      </Box>

      {!isLoading && (
        <TableContainer component={Paper}>
          <Table size="small">
            <TableHead sx={{ backgroundColor: '#f5f5f5' }}>
              <TableRow>
                <TableCell
                  sx={{ width: '100px', cursor: 'pointer' }}
                  onClick={() => handleRequestSort('issueDate')}
                >
                  Emisión <ArrowDropDownIcon sx={{ fontSize: 16 }} />
                </TableCell>
                <TableCell
                  sx={{ width: '128px', cursor: 'pointer' }}
                  onClick={() => handleRequestSort('dueDate')}
                >
                  Vencimiento <ArrowDropDownIcon sx={{ fontSize: 16 }} />
                </TableCell>
                <TableCell
                  sx={{ width: '110px', cursor: 'pointer' }}
                  onClick={() => handleRequestSort('billNumber')}
                >
                  Número <ArrowDropDownIcon sx={{ fontSize: 16 }} />
                </TableCell>
                <TableCell
                  sx={{ width: '200px', cursor: 'pointer' }}
                  onClick={() => handleRequestSort('customerName')}
                >
                  Receptor <ArrowDropDownIcon sx={{ fontSize: 16 }} />
                </TableCell>
                <TableCell
                  sx={{ width: '120px', cursor: 'pointer' }}
                  onClick={() => handleRequestSort('totalAmount')}
                >
                  Imp. Total <ArrowDropDownIcon sx={{ fontSize: 16 }} />
                </TableCell>
                <TableCell
                  sx={{ width: '130px', cursor: 'pointer' }}
                  onClick={() => handleRequestSort('paymentStatus')}
                >
                  Estado <ArrowDropDownIcon sx={{ fontSize: 16 }} />
                </TableCell>
                <TableCell align="center" sx={{ width: '30px' }} />
              </TableRow>
            </TableHead>
            <TableBody>
              {sortBills(filterBills(bills))
                .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                .map((bill) => {
                  const issueDate = dayjs(bill.issueDate).format('DD/MM/YYYY')
                  const dueDate = dayjs(bill.dueDate).format('DD/MM/YYYY')
                  const billNumber = bill.billNumber.toString().padStart(8, '0')
                  const status = computeStatus(bill)

                  return (
                    <TableRow key={bill.id}>
                      <TableCell>{issueDate}</TableCell>
                      <TableCell>{dueDate}</TableCell>
                      <TableCell>{billNumber}</TableCell>
                      <TableCell>{bill.customerName}</TableCell>
                      <TableCell>${formatNumber(bill.totalAmount)}</TableCell>
                      <TableCell>{status}</TableCell>
                      <TableCell align="center">
                        <IconButton
                          onClick={() => handleDownloadPdf(bill.id)}
                          sx={{
                            '&:focus': {
                              outline: 'none'
                            }
                          }}
                        >
                          <PictureAsPdfIcon />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  )
                })}
            </TableBody>
          </Table>

          <TablePagination
            rowsPerPageOptions={[5, 10, 25]}
            component="div"
            count={filterBills(bills).length}
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
    </Box>
  )
}

export default BillList
