package app.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TipoProveedor {
    private final IntegerProperty idTipoProveedor;
    private final StringProperty descripcion;

    public TipoProveedor(int idTipoProveedor, String descripcion) {
        this.idTipoProveedor = new SimpleIntegerProperty(idTipoProveedor);
        this.descripcion = new SimpleStringProperty(descripcion);
    }

    public int getId() { return idTipoProveedor.get(); }
    public void setId(int id) { this.idTipoProveedor.set(id); }

    public String getDescripcion() { return descripcion.get(); }
    public void setDescripcion(String descripcion) { this.descripcion.set(descripcion); }

    @Override
    public String toString() {
        return getDescripcion();
    }
}