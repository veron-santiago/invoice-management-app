import { TextField, InputAdornment } from '@mui/material'
import SearchIcon from '@mui/icons-material/Search'

const SearchInput = ({ value, onChange }) => {
  const handleChange = (e) => {
    onChange(e.target.value.toLowerCase().trim())
  }

  return (
    <TextField
      variant="outlined"
      size="small"
      placeholder="Buscar"
      value={value}
      onChange={handleChange}
      InputProps={{
        startAdornment: (
          <InputAdornment position="start">
            <SearchIcon />
          </InputAdornment>
        )
      }}
      sx={{ minWidth: 300 }}
    />
  )
}

export default SearchInput
