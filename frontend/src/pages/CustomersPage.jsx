import { Box } from '@mui/material'
import CustomerList from '../components/lists/CustomerList'
import SidebarMenu from '../components/SidebarMenu'

function CustomersPage() {
  return (
    <Box display="flex" justifyContent="flex-start" mt={5} mx={5}>
      <Box width="65%" ml={8} mr={4}>
        <CustomerList />
      </Box>
      <Box flexGrow={1}>
        <SidebarMenu />
      </Box>
    </Box>
  )
}

export default CustomersPage
