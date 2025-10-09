package app.model;

/**
 * Modelo para la entidad DetallePedido.
 * Representa cada línea de un pedido (anotación o producto).
 * * Se utiliza 'descripcion' como propiedad pública para ser compatible con TableView
 * en JavaFX y para almacenar la anotación manual o el nombre del producto.
 */
public class DetallePedido {

    private int idDetallePedido;
    private int idPedido;
    private int idProducto; // Mantenemos, 0 si es una anotación manual (NULL en DB)
    private String descripcion; // Propiedad pública compatible con FXML TableView
    private int cantidad;
    private double precioUnitario;
    private double subtotal;

    // Constructor vacío requerido para algunas operaciones de JavaFX/Hibernate
    public DetallePedido() {
        this.idProducto = 0; // Por defecto a 0 para anotaciones
    }

    // Constructor completo (asumiendo que viene de un ResultSet)
    public DetallePedido(int idDetallePedido, int idPedido, int idProducto, String descripcion,
                         int cantidad, double precioUnitario, double subtotal) {
        this.idDetallePedido = idDetallePedido;
        this.idPedido = idPedido;
        this.idProducto = idProducto;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    // Constructor para la creación de un nuevo detalle/anotación (sin ID generado aún)
    public DetallePedido(int idPedido, String descripcion, int cantidad, double precioUnitario) {
        this.idPedido = idPedido;
        this.idProducto = 0; // Indica que es una anotación (no está en la tabla Producto)
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        // Calculamos el subtotal al construir
        calcularSubtotal();
    }

    /**
     * Recalcula y establece el subtotal basado en la cantidad y el precio unitario.
     */
    public void calcularSubtotal() {
        this.subtotal = this.cantidad * this.precioUnitario;
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
     * NOTA: Este Getter y Setter se llaman 'descripcion' para coincidir
     * con la columna de la base de datos y la PropertyValueFactory en el FXML.
     */
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        calcularSubtotal(); // Recalcula subtotal al cambiar la cantidad
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
        calcularSubtotal(); // Recalcula subtotal al cambiar el precio
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        // Normalmente no se establece directamente, se calcula.
        // Se mantiene el setter para compatibilidad o si se necesita forzar un valor.
        this.subtotal = subtotal;
    }
}