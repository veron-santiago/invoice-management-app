import {
  Alert,
  Box,
  IconButton,
  Container,
  TextField,
  Checkbox,
  FormControlLabel,
  Typography,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Snackbar
} from "@mui/material";
import Autocomplete from "@mui/material/Autocomplete";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import CloseIcon from "@mui/icons-material/Close";
import { useNavigate } from "react-router-dom";
import { useState, useEffect, useRef, useCallback } from "react";

export default function BillGeneratorPage() {
  const navigate = useNavigate();
  const [customers, setCustomers] = useState([]);
  const [products, setProducts] = useState([]);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [address, setAddress] = useState("");
  const [sendEmail, setSendEmail] = useState(false);
  const [items, setItems] = useState([{ product: "", code: "", price: "", quantity: "" }]);
  const [confirmDialogOpen, setConfirmDialogOpen] = useState(false);
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);
  const nameRef = useRef(null);
  const emailRef = useRef(null);
  const productRefs = useRef([]);
  const codeRefs = useRef([]);
  const priceRefs = useRef([]);
  const quantityRefs = useRef([]);
  const [errorMessage, setErrorMessage] = useState("");
  const [showError, setShowError] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});
  const [serviceError, setServiceError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  // Auto-dismiss alerts after 5 seconds
  useEffect(() => {
    if (Object.keys(fieldErrors).length > 0) {
      const timer = setTimeout(() => {
        setFieldErrors({});
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [fieldErrors]);

  useEffect(() => {
    if (serviceError) {
      const timer = setTimeout(() => {
        setServiceError("");
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [serviceError]);

  useEffect(() => {
    if (successMessage) {
      const timer = setTimeout(() => {
        setSuccessMessage("");
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [successMessage]);

  const resetForm = () => {
    setName("");
    setEmail("");
    setAddress("");
    setSendEmail(false);
    setItems([{ product: "", code: "", price: "", quantity: "" }]);
  };

  const handleNameKeyDown = (e) => {
    if (e.key === "Tab") {
      e.preventDefault();
      const val = name.trim().toLowerCase();
      const match = customers.find((c) => c.name && c.name.toLowerCase().startsWith(val));
      if (match) {
        setName(match.name);
        if (match.email) setEmail(match.email);
        if (match.address) setAddress(match.address);
      }
      emailRef.current.focus();
    }
  };

  const handleItemChange = (index, field, value) => {
    const updated = [...items];
    if (field === "price") {
      value = value.replace(",", ".");
      if (!/^\d*\.?\d{0,2}$/.test(value)) return;
    }
    if (field === "quantity") {
      value = value.replace(/\D/g, "");
    }
    updated[index][field] = value;
    const priceParsed = parseFloat(updated[index].price);
    const quantityParsed = parseInt(updated[index].quantity, 10);
    const validPrice = !isNaN(priceParsed) && priceParsed >= 0.01;
    const validQuantity = !isNaN(quantityParsed) && quantityParsed > 0;
    const price = validPrice ? priceParsed : 0;
    const quantity = validQuantity ? quantityParsed : 1;
    setItems(updated);
  };

  const selectProduct = (index, product) => {
    const updated = [...items];
    updated[index].product = product.name || "";
    updated[index].code = product.code || "";
    updated[index].price = product.price?.toString() || "";
    const priceParsed = parseFloat(updated[index].price);
    const quantityParsed = parseInt(updated[index].quantity, 10) || 1;
    const validPrice = !isNaN(priceParsed) && priceParsed >= 0.01;
    const price = validPrice ? priceParsed : 0;
    setItems(updated);
  };

  const addItem = () => {
    setItems(prev => {
      if (prev.length >= 17) {
        alert("No se puede agregar más de 17 líneas en la factura.");
        return prev;
      }
      return [...prev, { product: "", code: "", price: "", quantity: "" }];
    });
    setTimeout(() => {
      const last = productRefs.current.length - 1;
      productRefs.current[last]?.focus();
    }, 0);
  };
  

  const handleGenerateInvoice = () => {
    setConfirmDialogOpen(true);
  };

  const handleConfirmGenerate = () => {
    setConfirmDialogOpen(false);
    
    // Clear previous errors and success messages
    setFieldErrors({});
    setServiceError("");
    setSuccessMessage("");
    setErrorMessage("");
    setShowError(false);
  
    const token = localStorage.getItem("token");
  
    const formattedItems = items
    .filter((item) => {
      const name = item.product?.trim();
      const code = item.code?.trim();
      const price = item.price?.toString().trim();
      const quantity = item.quantity?.toString().trim();

      return name !== "" || code !== "" || price !== "" || quantity !== "";
    })
    .map((item) => ({
      name: item.product?.trim() === "" ? null : item.product?.trim(),
      code: item.code?.trim() === "" ? null : item.code?.trim(),
      quantity:
        item.quantity?.toString().trim() === "" ? null : parseInt(item.quantity, 10),
      price:
        item.price?.toString().trim() === ""
          ? null
          : parseFloat(item.price.replace(",", "."))
    }));
    

    const payload = {
      customerName: name,
      customerEmail: email,
      customerAddress: address,
      billLineRequests: formattedItems,
      sendEmail: sendEmail,
      includeQr: false
    };
  
    fetch("http://localhost:8080/bills", {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json"
      },
      body: JSON.stringify(payload)
    })
    .then( async (response) => {
      if (!response.ok) {
        const e = await response.json();
        console.log(e);
        
        // Handle different types of errors
        const newFieldErrors = {};
        let newServiceError = "";
        
        // Check if it's a service error (has message property)
        if (e.message) {
          newServiceError = e.message;
        } else {
          // Handle field validation errors
          
          // Check customer field errors
          if (e.customerName) {
            newFieldErrors.customerName = e.customerName;
          }
          if (e.customerEmail) {
            newFieldErrors.customerEmail = e.customerEmail;
          }
          if (e.customerAddress) {
            newFieldErrors.customerAddress = e.customerAddress;
          }
          
          // Check bill line errors
          const billLineError = searchErrorInBillLines(e);
          if (billLineError) {
            const { error, index, field } = billLineError;
            newFieldErrors[`billLine_${index}_${field}`] = error;
          }
          
          // Check general billLineRequests error (like min/max size)
          if (e.billLineRequests && typeof e.billLineRequests === 'string') {
            newServiceError = e.billLineRequests;
          }
          
          // If no specific field errors found but we have other errors, treat as service error
          if (Object.keys(newFieldErrors).length === 0 && !newServiceError) {
            newServiceError = "Error al generar la factura";
          }
        }
        
        setFieldErrors(newFieldErrors);
        setServiceError(newServiceError);
        
        throw new Error("Validation failed");
      }
      return response.json();
    })
    .then((data) => {
      console.log("Factura generada:", data);
      // Show success message and reset form
      setSuccessMessage("¡Factura generada!");
      resetForm();
    })
    .catch((error) => {
      console.error("Error al generar la factura:", error);
    });

    console.log("JSON enviado:", payload);
  };

  const searchErrorInBillLines = (error) => {
    for (const key in error) {
      if (key.startsWith("billLineRequests[")) {
        return {
          error : error[key], 
          index : key.replace("billLineRequests[", "")[0],
          field : key.replace("billLineRequests[", "").replace("].", "").slice(1)
        };
      }
    }
    return false;
  };
  
  const handleCancelGenerate = () => {
    setConfirmDialogOpen(false);
  };

  const checkForUnsavedChanges = useCallback(() => {
    const hasCustomerData = name.trim() !== "" || email.trim() !== "" || address.trim() !== "";
    const hasItemData = items.some(item => 
      item.product.trim() !== "" || 
      item.code.trim() !== "" || 
      item.price.trim() !== "" || 
      item.quantity.trim() !== ""
    );
    const hasData = hasCustomerData || hasItemData;
    setHasUnsavedChanges(hasData);
    return hasData;
  }, [name, email, address, items]);

  const handleBeforeUnload = useCallback((e) => {
    if (hasUnsavedChanges) {
      e.preventDefault();
      e.returnValue = "¿Está seguro de que desea salir? Los datos no guardados se perderán.";
      return e.returnValue;
    }
  }, [hasUnsavedChanges]);

  const handleBackClick = () => {
    if (hasUnsavedChanges) {
      const confirmed = window.confirm("¿Está seguro de que desea salir? Los datos no guardados se perderán.");
      if (confirmed) {
        navigate(-1);
      }
    } else {
      navigate(-1);
    }
  };

  useEffect(() => {
    const plusHandler = (e) => {
      if (e.key === "+") {
        e.preventDefault();
        addItem();
      }
    };
    document.addEventListener("keydown", plusHandler, true);
    return () => document.removeEventListener("keydown", plusHandler, true);
  }, []);

  useEffect(() => {
    checkForUnsavedChanges();
  }, [checkForUnsavedChanges]);
  useEffect(() => {
    window.addEventListener("beforeunload", handleBeforeUnload);
    return () => {
      window.removeEventListener("beforeunload", handleBeforeUnload);
    };
  }, [handleBeforeUnload]);

  useEffect(() => {
    const token = localStorage.getItem("token");
    fetch("http://localhost:8080/customers", { headers: { Authorization: `Bearer ${token}` } })
      .then((r) => r.json())
      .then(setCustomers)
      .catch(() => {});
    fetch("http://localhost:8080/products", { headers: { Authorization: `Bearer ${token}` } })
      .then((r) => r.json())
      .then(setProducts)
      .catch(() => {});
  }, []);

  const handleKeyDown = (e, idx, col) => {
    if (e.key === "Tab") {
      e.preventDefault();
      if (col === "code") {
        const val = items[idx].code.trim().toLowerCase();
        const match = products.find((p) => p.code && p.code.toLowerCase().startsWith(val));
        if (match) selectProduct(idx, match);
        if (codeRefs.current[idx + 1]) {
          codeRefs.current[idx + 1]?.focus();
        } else {
          productRefs.current[idx]?.focus();
        }
      } else if (col === "product") {
        const val = items[idx].product.trim().toLowerCase();
        const match = products.find((p) => p.name && p.name.toLowerCase().startsWith(val));
        if (match) selectProduct(idx, match);
        if (productRefs.current[idx + 1]) {
          productRefs.current[idx + 1]?.focus();
        } else {
          priceRefs.current[idx]?.focus();
        }
      } else if (col === "price") {
        if (priceRefs.current[idx + 1]) {
          priceRefs.current[idx + 1]?.focus();
        } else {
          quantityRefs.current[idx]?.focus();
        }
      } else if (col === "quantity") {
        if (quantityRefs.current[idx + 1]) {
          quantityRefs.current[idx + 1]?.focus();
        } else {
          codeRefs.current[idx + 1]?.focus();
        }
      }
    } else if (e.key === "Enter") {
      e.preventDefault();
      if (col === "code") {
        if (codeRefs.current[idx + 1]) {
          codeRefs.current[idx + 1]?.focus();
        } else {
          productRefs.current[idx]?.focus();
        }
      } else if (col === "product") {
        if (productRefs.current[idx + 1]) {
          productRefs.current[idx + 1]?.focus();
        } else {
          priceRefs.current[idx]?.focus();
        }
      } else if (col === "price") {
        if (priceRefs.current[idx + 1]) {
          priceRefs.current[idx + 1]?.focus();
        } else {
          quantityRefs.current[idx]?.focus();
        }
      } else if (col === "quantity") {
        if (quantityRefs.current[idx + 1]) {
          quantityRefs.current[idx + 1]?.focus();
        } else {
          codeRefs.current[idx + 1]?.focus();
        }
      }
    }
  };

  const removeItem = (index) => {
    setItems(prev => prev.filter((_, i) => i !== index));
  };


  return (
    <Box sx={{ display: "flex", minHeight: "100%", backgroundColor: "#f5f5f5", flexDirection: "column" }}>
      <Box sx={{ width: "15vw", display: "flex", justifyContent: "center", mt: 4, position: "relative", zIndex: 1 }}>
        <IconButton onClick={handleBackClick} sx={{ "&:focus": { outline: "none" } }}>
          <ArrowBackIcon />
        </IconButton>
      </Box>
      <Container maxWidth={false} sx={{ width: "70vw", backgroundColor: "white", p: 4, boxShadow: 3, borderRadius: 2, mb: 8 }}>
        <Typography sx={{ color: "#b3b3b3", fontSize: 18, mb: 2 }}>DATOS DEL RECEPTOR</Typography>
        <Box sx={{ display: "flex", alignItems: "center", mb: 2, position: "relative" }}>
          <Typography sx={{ color: "black", mr: 2 }}>Nombre / Razón Social:</Typography>
          <Box sx={{ position: "relative" }}>
            <Autocomplete
              options={customers}
              getOptionLabel={(o) => (typeof o === "string" ? o : o.name || "")}
              freeSolo
              inputValue={name}
              onInputChange={(_, v) => setName(v)}
              onChange={(_, v) => {
                if (v && typeof v === "object") {
                  setName(v.name || "");
                  if (v.email) setEmail(v.email);
                  if (v.address) setAddress(v.address);
                }
              }}
              renderInput={(params) => (
                <TextField
                  {...params}
                  placeholder="Cliente"
                  variant="outlined"
                  size="small"
                  inputRef={(el) => (nameRef.current = el)}
                  onKeyDown={handleNameKeyDown}
                />
              )}
              sx={{ width: 300, "& .MuiInputBase-input::placeholder": { color: "#B3B3B3", opacity: 1 } }}
            />
            {fieldErrors.customerName && (
              <Alert severity="error" sx={{ position: "absolute", top: 45, left: 0, zIndex: 1000, width: 300 }}>
                {fieldErrors.customerName}
              </Alert>
            )}
          </Box>
        </Box>
        <Box sx={{ display: "flex", alignItems: "center", mb: 2, position: "relative" }}>
          
          <Typography sx={{ color: "black", mr: 2 }}>Correo (opcional):</Typography>
          <Box sx={{ position: "relative", mr: 2 }}>
            <TextField
              placeholder="Correo"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              size="small"
              inputRef={(el) => (emailRef.current = el)}
              sx={{ width: 250, "& .MuiInputBase-input::placeholder": { color: "#B3B3B3", opacity: 1 } }}
            />
            {fieldErrors.customerEmail && (
              <Alert severity="error" sx={{ position: "absolute", top: 45, left: 0, zIndex: 1000, width: 250 }}>
                {fieldErrors.customerEmail}
              </Alert>
            )}
          </Box>
          
          <FormControlLabel control={<Checkbox checked={sendEmail} onChange={() => setSendEmail(!sendEmail)} size="small" />} label="Enviar la factura a este correo" />
        </Box>
        <Box sx={{ display: "flex", alignItems: "center", mb: 4, position: "relative" }}>
          <Typography sx={{ color: "black", mr: 2 }}>Dirección (opcional):</Typography>
          <Box sx={{ position: "relative" }}>
            <TextField
              placeholder="Dirección"
              value={address}
              onChange={(e) => setAddress(e.target.value)}
              size="small"
              sx={{ width: 400, "& .MuiInputBase-input::placeholder": { color: "#B3B3B3", opacity: 1 } }}
            />
            {fieldErrors.customerAddress && (
              <Alert severity="error" sx={{ position: "absolute", top: 45, left: 0, zIndex: 1000, width: 400 }}>
                {fieldErrors.customerAddress}
              </Alert>
            )}
          </Box>
        </Box>
        <Box sx={{ width: "100%", display: "flex", flexDirection: "column", alignItems: "center" }}>
          <Typography sx={{ color: "#b3b3b3", fontSize: 18, mb: 2, textAlign: "center" }}>Ítems de la factura</Typography>
          <Box sx={{ display: "flex", gap: 2, mb: 1 }}>
            <Typography sx={{ width: 120, fontWeight: "bold", textAlign: "center" }}>Código</Typography>
            <Typography sx={{ width: 250, fontWeight: "bold", textAlign: "center" }}>Producto/Servicio</Typography>
            <Typography sx={{ width: 120, fontWeight: "bold", textAlign: "center" }}>Precio Unit.</Typography>
            <Typography sx={{ width: 100, fontWeight: "bold", textAlign: "center" }}>Cantidad</Typography>
            <Typography sx={{ width: 120, fontWeight: "bold", textAlign: "center" }}>Total</Typography>
            <IconButton sx={{ height: 10 , visibility: "hidden"}}>
                <CloseIcon />
            </IconButton>
          </Box>
          {items.map((item, i) => (
            <Box key={i} sx={{ display: "flex", gap: 2, mb: 1, position: "relative" }}>
              <Autocomplete
                freeSolo
                options={products}
                getOptionLabel={(o) => (typeof o === "string" ? o : o.code || "")}
                inputValue={item.code}
                onInputChange={(_, v, reason) => {
                  if (reason === "input") handleItemChange(i, "code", v);
                  if (reason === "clear") handleItemChange(i, "code", "");
                }}
                onChange={(_, v) => {
                  if (v && typeof v === "object") {
                    selectProduct(i, v);
                  }
                }}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    variant="outlined"
                    size="small"
                    inputRef={(el) => (codeRefs.current[i] = el)}
                    onKeyDown={(e) => handleKeyDown(e, i, "code")}
                  />
                )}
                sx={{ width: 120 }}
              />
              {fieldErrors[`billLine_${i}_code`] && (
                <Alert severity="error" sx={{ position: "absolute", top: 45, left: 0, zIndex: 1000, width: 280 }}>
                  {fieldErrors[`billLine_${i}_code`]}
                </Alert>
              )}
              <Autocomplete
                freeSolo
                options={products}
                getOptionLabel={(o) => (typeof o === "string" ? o : o.name || "")}
                inputValue={item.product}
                onInputChange={(_, v, reason) => {
                  if (reason === "input") handleItemChange(i, "product", v);
                  if (reason === "clear") handleItemChange(i, "product", "");
                }}
                onChange={(_, v) => {
                  if (v && typeof v === "object") {
                    selectProduct(i, v);
                  }
                }}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    variant="outlined"
                    size="small"
                    inputRef={(el) => (productRefs.current[i] = el)}
                    onKeyDown={(e) => handleKeyDown(e, i, "product")}
                  />
                )}
                sx={{ width: 250 }}
              />
              {fieldErrors[`billLine_${i}_name`] && (
                <Alert severity="error" sx={{ position: "absolute", top: 45, left: 140, zIndex: 1000, maxWidth: 250 }}>
                  {fieldErrors[`billLine_${i}_name`]}
                </Alert>
              )}
              <TextField
                value={item.price}
                onChange={(e) => handleItemChange(i, "price", e.target.value)}
                size="small"
                sx={{ width: 120 }}
                inputRef={(el) => (priceRefs.current[i] = el)}
                onKeyDown={(e) => handleKeyDown(e, i, "price")}
              />
              {fieldErrors[`billLine_${i}_price`] && (
                <Alert severity="error" sx={{ position: "absolute", top: 45, left: 410, zIndex: 1000, width: 280 }}>
                  {fieldErrors[`billLine_${i}_price`]}
                </Alert>
              )}
              <TextField
                value={item.quantity}
                onChange={(e) => handleItemChange(i, "quantity", e.target.value)}
                size="small"
                sx={{ width: 100 }}
                inputRef={(el) => (quantityRefs.current[i] = el)}
                onKeyDown={(e) => handleKeyDown(e, i, "quantity")}
              />
              {fieldErrors[`billLine_${i}_quantity`] && (
                <Alert severity="error" sx={{ position: "absolute", top: 45, left: 550, zIndex: 1000, width: 280 }}>
                  {fieldErrors[`billLine_${i}_quantity`]}
                </Alert>
              )}
              <TextField
                value={
                  (() => {
                    const price = parseFloat(item.price.replace(",", ".")) || 0;
                    const quantity = parseInt(item.quantity, 10) || 0;
                    return (price * quantity).toFixed(2);
                  })()
                }
                size="small"
                sx={{ width: 120 }}
                InputProps={{ readOnly: true }}
              />

              <IconButton onClick={() => removeItem(i)}>
                <CloseIcon />
              </IconButton>
            </Box>
          ))}
          <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", width: "70%", mt: 2 }}>
            <Button onClick={addItem} variant="outlined">Agregar Ítem</Button>
            <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
              <Typography sx={{ fontWeight: "bold", fontSize: 16 }}>Importe Total:</Typography>
              <TextField
                value={`$${items
                  .reduce((sum, item) => {
                    const price = parseFloat(item.price.replace(",", ".")) || 0;
                    const quantity = parseInt(item.quantity, 10) || 0;
                    return sum + price * quantity;
                  }, 0)
                .toFixed(2)}`}
                size="small"
                sx={{ width: 200 }}
                InputProps={{ readOnly: true }}
              />

            </Box>
          </Box>
          
          {serviceError && (
            <Box sx={{ display: "flex", justifyContent: "center", mt: 2, mb: 2 }}>
              <Alert severity="error" sx={{ maxWidth: "70%" }}>
                {serviceError}
              </Alert>
            </Box>
          )}
          
          <Box sx={{ display: "flex", justifyContent: "center", mt: 4, mb: 2, position: "relative" }}>
            {successMessage && (
              <Alert severity="success" sx={{ position: "absolute", top: -70, left: "50%", transform: "translateX(-50%)", zIndex: 1000, minWidth: "200px", maxWidth: "300px", textAlign: "center" }}>
                {successMessage}
              </Alert>
            )}
            <Button 
              onClick={handleGenerateInvoice}
              variant="contained" 
              color="primary"
              size="large"
              sx={{ px: 4, py: 1.5, fontSize: 16 }}
            >
              Generar Factura
            </Button>
          </Box>
        </Box>
      </Container>
      <Box sx={{ width: "15vw" }} />
      
      
      <Dialog
        open={confirmDialogOpen}
        onClose={handleCancelGenerate}
        aria-labelledby="confirm-dialog-title"
        aria-describedby="confirm-dialog-description"
      >
        <DialogTitle id="confirm-dialog-title">
          Confirmar Generación de Factura
        </DialogTitle>
        <DialogContent>
          <DialogContentText id="confirm-dialog-description">
            ¿Está seguro que desea generar esta factura? Una vez generada, no podrá ser modificada.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCancelGenerate} color="secondary">
            Cancelar
          </Button>
          <Button onClick={handleConfirmGenerate} color="primary" variant="contained">
            Confirmar
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
    
  );
}
