package app.model;

import javafx.beans.property.*;

public class Insumo {

    private final IntegerProperty idInsumo;
    private final StringProperty nombreInsumo;
    private final StringProperty descripcion;
    private final IntegerProperty stockMinimo;
    private final IntegerProperty stockActual;
    private final StringProperty estado;
    private final IntegerProperty idTipoProveedor;

    public Insumo(int idInsumo, String nombreInsumo, String descripcion, int stockMinimo, int stockActual, String estado, int idTipoProveedor) {
        this.idInsumo = new SimpleIntegerProperty(idInsumo);
        this.nombreInsumo = new SimpleStringProperty(nombreInsumo);
        this.descripcion = new SimpleStringProperty(descripcion);
        this.stockMinimo = new SimpleIntegerProperty(stockMinimo);
        this.stockActual = new SimpleIntegerProperty(stockActual);
        this.estado = new SimpleStringProperty(estado);
        this.idTipoProveedor = new SimpleIntegerProperty(idTipoProveedor);
    }

    // Getters para las propiedades de JavaFX
    public IntegerProperty idInsumoProperty() {
        return idInsumo;
    }

    public StringProperty nombreInsumoProperty() {
        return nombreInsumo;
    }

    public StringProperty descripcionProperty() {
        return descripcion;
    }

    public IntegerProperty stockMinimoProperty() {
        return stockMinimo;
    }

    public IntegerProperty stockActualProperty() {
        return stockActual;
    }

    public StringProperty estadoProperty() {
        return estado;
    }

    public IntegerProperty idTipoProveedorProperty() {
        return idTipoProveedor;
    }

    // Getters y Setters
    public int getIdInsumo() {
        return idInsumo.get();
    }

    public void setIdInsumo(int idInsumo) { this.idInsumo.set(idInsumo); }

    public String getNombreInsumo() { return nombreInsumo.get(); }

    public void setNombreInsumo(String nombreInsumo) {
        this.nombreInsumo.set(nombreInsumo);
    }

    public String getDescripcion() {
        return descripcion.get();
    }

    public void setDescripcion(String descripcion) {
        this.descripcion.set(descripcion);
    }

    public int getStockMinimo() {
        return stockMinimo.get();
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo.set(stockMinimo);
    }

    public int getStockActual() {
        return stockActual.get();
    }

    public void setStockActual(int stockActual) {
        this.stockActual.set(stockActual);
    }

    public String getEstado() {
        return estado.get();
    }

    public void setEstado(String estado) {
        this.estado.set(estado);
    }

    public int getIdTipoProveedor() {
        return idTipoProveedor.get();
    }

    public void setIdTipoProveedor(int idTipoProveedor) {
        this.idTipoProveedor.set(idTipoProveedor);
    }
}