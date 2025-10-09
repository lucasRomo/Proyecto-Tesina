package app.model;

import javafx.beans.property.*;

/**
 * Clase de modelo para la entidad Producto.
 * Utiliza JavaFX Properties para facilitar el binding con la interfaz de usuario.
 */
public class Producto {

    private final IntegerProperty idProducto;
    private final StringProperty nombreProducto;
    private final StringProperty descripcion;
    private final DoubleProperty precio;
    private final IntegerProperty stock;
    private final IntegerProperty idCategoria; // Se mantiene como int/IntegerProperty

    // Constructor completo
    public Producto(int idProducto, String nombreProducto, String descripcion, double precio, int stock, int idCategoria) {
        this.idProducto = new SimpleIntegerProperty(idProducto);
        this.nombreProducto = new SimpleStringProperty(nombreProducto);
        this.descripcion = new SimpleStringProperty(descripcion);
        this.precio = new SimpleDoubleProperty(precio);
        this.stock = new SimpleIntegerProperty(stock);
        this.idCategoria = new SimpleIntegerProperty(idCategoria);
    }

    // Constructor para nuevos productos (sin ID)
    public Producto(String nombreProducto, String descripcion, double precio, int stock, int idCategoria) {
        this(0, nombreProducto, descripcion, precio, stock, idCategoria);
    }

    // Constructor para productos sin categoría (asigna 0 a idCategoria)
    public Producto(String nombreProducto, String descripcion, double precio, int stock) {
        this(0, nombreProducto, descripcion, precio, stock, 0); // idCategoria = 0 para NULL en DB
    }

    // --- Getters para Properties ---
    public IntegerProperty idProductoProperty() {
        return idProducto;
    }

    public StringProperty nombreProductoProperty() {
        return nombreProducto;
    }

    public StringProperty descripcionProperty() {
        return descripcion;
    }

    public DoubleProperty precioProperty() {
        return precio;
    }

    public IntegerProperty stockProperty() {
        return stock;
    }

    public IntegerProperty idCategoriaProperty() {
        return idCategoria;
    }

    // --- Getters para valores primitivos ---
    public int getIdProducto() {
        return idProducto.get();
    }

    public String getNombreProducto() {
        return nombreProducto.get();
    }

    public String getDescripcion() {
        return descripcion.get();
    }

    public double getPrecio() {
        return precio.get();
    }

    public int getStock() {
        return stock.get();
    }

    /**
     * Obtiene el ID de la categoría. Si es 0, el DAO lo interpretará como NULL.
     * @return ID de la categoría (0 si no tiene).
     */
    public int getIdCategoria() {
        return idCategoria.get();
    }

    // --- Setters ---
    public void setIdProducto(int idProducto) {
        this.idProducto.set(idProducto);
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto.set(nombreProducto);
    }

    public void setDescripcion(String descripcion) {
        this.descripcion.set(descripcion);
    }

    public void setPrecio(double precio) {
        this.precio.set(precio);
    }

    public void setStock(int stock) {
        this.stock.set(stock);
    }

    /**
     * Establece el ID de la categoría. Use 0 para indicar que no hay categoría.
     * @param idCategoria El ID de la categoría.
     */
    public void setIdCategoria(int idCategoria) {
        this.idCategoria.set(idCategoria);
    }
}