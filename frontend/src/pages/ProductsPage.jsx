import { Box } from '@mui/material'
import SidebarMenu from '../components/SidebarMenu'
import ProductList from '../components/lists/ProductList'

function ProductsPage() {
  return (
    <Box display="flex" justifyContent="flex-start" mt={5} mx={5}>
      <Box width="65%" ml={8} mr={4}>
        <ProductList />
      </Box>
      <Box flexGrow={1}>
        <SidebarMenu />
      </Box>
    </Box>
  )
}

export default ProductsPage
