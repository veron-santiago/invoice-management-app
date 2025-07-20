package io.github.veron_santiago.backend.service.exception;

public enum ErrorMessages {

    PRODUCT_NOT_FOUND("Producto no encontrado"),
    COMPANY_NOT_FOUND("Compañía no encontrada"),
    CUSTOMER_NOT_FOUND("Cliente no encontrado"),
    BILL_NOT_FOUND("Factura no encontrada"),
    BILL_LINE_NOT_FOUND("Linea de factura no encontrada"),
    PRODUCT_NAME_IS_EMPTY("El nombre del producto no puede estar vacío"),
    CUSTOMER_NAME_IS_EMPTY("El nombre del cliente no puede estar vacío"),
    PRODUCT_CODE_IS_EMPTY("El código del producto no puede estar vacío"),
    PRODUCT_PRICE_MUST_BE_POSITIVE("El precio del producto debe ser mayor que cero"),
    PRODUCT_EMPTY_FIELDS("Se debe declarar al menos un valor"),
    ACCESS_DENIED_READ("No tienes permiso para acceder a este recurso"),
    ACCESS_DENIED_UPDATE("No tienes permiso para modificar este recurso"),
    ACCESS_DENIED_DELETE("No tienes permiso para eliminar este recurso"),
    DUPLICATE_PRODUCT_IN_BILL("No se permiten productos repetidos en una factura");



    private final String message;

    ErrorMessages(String message) {
        this.message = message;
    }

    public String getMessage(){
        return message;
    }
}
