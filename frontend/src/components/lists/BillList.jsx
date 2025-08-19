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
  const API_URL = import.meta.env.VITE_API_URL;


  useEffect(() => {
    const token = localStorage.getItem('token')

    fetch(`${API_URL}/bills`, {
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



  const handleRequestSort = (property) => {
    const isAsc = orderBy === property && order === 'asc';
    setOrder(isAsc ? 'desc' : 'asc');
    setOrderBy(property);
  };

  const sortBills = (array) => {
    const sortedArray = [...array].sort((a, b) => {
      if (orderBy === 'dueDate') {
        const aHasDueDate = a.dueDate !== null;
        const bHasDueDate = b.dueDate !== null;

        if (aHasDueDate !== bHasDueDate) {
          return order === 'asc' ? (aHasDueDate ? 1 : -1) : (aHasDueDate ? -1 : 1);
        }

        return order === 'asc' ? a.billNumber - b.billNumber : b.billNumber - a.billNumber;
      }

      const getValue = (bill) => {
        switch (orderBy) {
          case 'billNumber':
            return bill.billNumber;
          case 'totalAmount':
            return bill.totalAmount;
          case 'customer.name':
            return bill.customerName.toLowerCase();
          // A default case is good practice, though we control orderBy.
          default:
            return bill.billNumber;
        }
      };

      const aVal = getValue(a);
      const bVal = getValue(b);

      if (typeof aVal === 'string') {
        return order === 'asc' ? aVal.localeCompare(bVal) : bVal.localeCompare(aVal);
      }
      return order === 'asc' ? aVal - bVal : bVal - aVal;
    });
    return sortedArray;
  };

  const filterBills = (bills) => {
    const normalizedQuery = searchQuery.toLowerCase().replace(/\./g, '')

    return bills.filter((bill) => {
      const issueDate = dayjs(bill.issueDate).format('DD/MM/YYYY')
      const dueDate = dayjs(bill.dueDate).format('DD/MM/YYYY')
      const billNumber = bill.billNumber.toString().padStart(8, '0')
      const customerName = bill.customerName
      const totalAmount = formatNumber(bill.totalAmount).replace(/\./g, '')

      const values = [
        issueDate,
        dueDate,
        billNumber,
        customerName,
        totalAmount
      ]

      return values.some((val) =>
        val.toLowerCase().includes(normalizedQuery)
      )
    })
  }

  const handleDownloadPdf = async (billId) => {
    const token = localStorage.getItem("token");
  const apiRes = await fetch(`${API_URL}/bills/${billId}/pdf`, {
    method: "GET",
    headers: { Authorization: `Bearer ${token}` }
  });

  if (!apiRes.ok) {
    throw new Error(`Error al pedir URL del PDF: ${apiRes.status}`);
  }

  const url = (await apiRes.text()).trim();

  try {
    const fileRes = await fetch(url, { method: "GET", mode: "cors" });
    if (!fileRes.ok) {
      window.open(url, "_blank");
      return;
    }

    const u = new URL(url);
    const parts = u.pathname.split("/");
    const filename = decodeURIComponent(parts[parts.length - 1]);

    const blob = await fileRes.blob();
    if (!blob || blob.size === 0) {
      window.open(url, "_blank");
      return;
    }

    const blobUrl = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = blobUrl;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(blobUrl);
  } catch (err) {
    window.open(url, "_blank");
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
                  sx={{ width: '120px', cursor: 'pointer' }}
                  onClick={() => handleRequestSort('issueDate')}
                >
                  Emisión <ArrowDropDownIcon sx={{ fontSize: 16 }} />
                </TableCell>
                <TableCell
                  sx={{ width: '140px', cursor: 'pointer' }}
                  onClick={() => handleRequestSort('dueDate')}
                >
                  Vencimiento <ArrowDropDownIcon sx={{ fontSize: 16 }} />
                </TableCell>
                <TableCell
                  sx={{ width: '130px', cursor: 'pointer' }}
                  onClick={() => handleRequestSort('billNumber')}
                >
                  Número <ArrowDropDownIcon sx={{ fontSize: 16 }} />
                </TableCell>
                <TableCell
                  sx={{ width: '250px', cursor: 'pointer' }}
                  onClick={() => handleRequestSort('customerName')}
                >
                  Receptor <ArrowDropDownIcon sx={{ fontSize: 16 }} />
                </TableCell>
                <TableCell
                  sx={{ width: '148px', cursor: 'pointer' }}
                  onClick={() => handleRequestSort('totalAmount')}
                >
                  Imp. Total <ArrowDropDownIcon sx={{ fontSize: 16 }} />
                </TableCell>
                <TableCell align="center" sx={{ width: '30px' }} >
                  Descargar
                </TableCell>
              
              </TableRow>
            </TableHead>
            <TableBody>
              {sortBills(filterBills(bills))
                .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                .map((bill) => {
                  const issueDate = dayjs(bill.issueDate).format('DD/MM/YYYY')
                  const dueDate = bill.dueDate == null ? '-' : dayjs(bill.dueDate).format('DD/MM/YYYY')
                  const billNumber = bill.billNumber.toString().padStart(8, '0')

                  return (
                    <TableRow key={bill.id}>
                      <TableCell>{issueDate}</TableCell>
                      <TableCell>{dueDate}</TableCell>
                      <TableCell>{billNumber}</TableCell>
                      <TableCell>{bill.customerName}</TableCell>
                      <TableCell>${formatNumber(bill.totalAmount)}</TableCell>
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
