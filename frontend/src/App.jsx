import { Routes, Route } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import SignupPage from './pages/SignupPage'
import BillsPage from './pages/BillsPage'
import ProductsPage from './pages/ProductsPage'
import CustomersPage from './pages/CustomersPage'
import ProtectedRoute from './ProtectedRoute'
import CompanyPage from './pages/CompanyPage'
import BillGeneratorPage from './pages/BillGeneratorPage'

const App = () => {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />
      <Route element={<ProtectedRoute />}>
        <Route path='/bills' element={<BillsPage />} />
        <Route path='/bills/create' element={<BillGeneratorPage />} />
        <Route path='/products' element={<ProductsPage />} />
        <Route path='/customers' element={<CustomersPage />} />
        <Route path='/company' element={<CompanyPage />} />
      </Route>
    </Routes>
  )
}

export default App

