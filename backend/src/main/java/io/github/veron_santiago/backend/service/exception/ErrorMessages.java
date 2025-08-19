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
    DUPLICATE_PRODUCT_IN_BILL("No se permiten productos repetidos en una factura"),
    DUPLICATE_CODE_IN_BILL("No se permiten códigos repetidos en una factura"),
    EMPTY_FIELD("El campo no puede estar vacío"),
    CONFLICT_COMPANY_NAME("El nombre ya se encuentra en uso"),
    INCORRECT_CURRENT_PASSWORD("Contraseña actual incorrecta"),
    PASSWORD_SAME_AS_CURRENT("La nueva contraseña debe ser distinta de la actual"),
    EMAIL_SAME_AS_CURRENT("El nuevo email debe ser distinto al actual"),
    ADDRESS_SAME_AS_CURRENT("La nueva dirección debe ser distinta a la actual"),
    EMAIL_ALREADY_IN_USE("El email ya se encuentra en uso"),
    PDF_GENERATE_ERROR("No se pudo generar el PDF. Intente nuevamente más tarde."),
    PRODUCT_NAME_ALREADY_EXISTS("Ya existe un producto con ese nombre"),
    PRODUCT_CODE_ALREADY_EXISTS("Ya existe un producto con ese código"),
    QR("Error al generar el QR de pago");





    private final String message;

    ErrorMessages(String message) {
        this.message = message;
    }

    public String getMessage(){
        return message;
    }
}
