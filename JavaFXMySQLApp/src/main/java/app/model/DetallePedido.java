package app.model;

/**
 * Modelo para la entidad DetallePedido.
 * Representa cada línea de un pedido (anotación o producto).
 */
public class DetallePedido {

    private int idDetallePedido;
    private int idPedido;
    private int idProducto; // Mantenemos, pero puede ser 0 o NULL en DB
    private String nombreProducto; // Usado para almacenar la "descripcion" o anotación
    private int cantidad;
    private double precioUnitario;
    private double subtotal;

    // Constructor completo (asumiendo que viene de un ResultSet)
    public DetallePedido(int idDetallePedido, int idPedido, int idProducto, String nombreProducto,
                         int cantidad, double precioUnitario, double subtotal) {
        this.idDetallePedido = idDetallePedido;
        this.idPedido = idPedido;
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    // Constructor para la creación de un nuevo detalle/anotación (sin ID generado aún)
    public DetallePedido(int idPedido, String nombreProducto, int cantidad, double precioUnitario) {
        this.idPedido = idPedido;
        this.idProducto = 0; // Por defecto a 0 si no se usa producto
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        // Calculamos el subtotal al construir
        this.subtotal = cantidad * precioUnitario;
    }

    // --- Getters y Setters ---

    public int getIdDetallePedido() {
        return idDetallePedido;
    }

    public void setIdDetallePedido(int idDetallePedido) {
        this.idDetallePedido = idDetallePedido;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    /**
     * Obtiene el nombre del producto o la anotación del detalle.
     * Este es el método que tu DAO está buscando.
     */
    public String getNombreProducto() {
        return nombreProducto;
    }

    /**
     * Establece el nombre del producto o la anotación del detalle.
     * Este es el método que tu DAO está buscando.
     */
    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        this.subtotal = this.cantidad * this.precioUnitario; // Recalcula subtotal
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
        this.subtotal = this.cantidad * this.precioUnitario; // Recalcula subtotal
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
}