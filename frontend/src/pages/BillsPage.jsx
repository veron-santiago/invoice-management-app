import { Box } from '@mui/material'
import BillList from '../components/lists/BillList'
import SidebarMenu from '../components/SidebarMenu'

function BillsPage() {
  return (
    <Box display="flex" justifyContent="flex-start" mt={5} mx={5}>
      <Box width="65%" ml={8} mr={4}>
        <BillList />
      </Box>
      <Box flexGrow={1}>
        <SidebarMenu />
      </Box>
    </Box>
  )
}

export default BillsPage