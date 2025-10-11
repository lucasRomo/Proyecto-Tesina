package app.model;

import javafx.beans.property.*;

/**
 * Clase de modelo para la entidad Categoria.
 */
public class Categoria {
    private final IntegerProperty idCategoria;
    private final StringProperty nombre;
    private final StringProperty descripcion;

    public Categoria(int idCategoria, String nombre, String descripcion) {
        this.idCategoria = new SimpleIntegerProperty(idCategoria);
        this.nombre = new SimpleStringProperty(nombre);
        this.descripcion = new SimpleStringProperty(descripcion);
    }

    // --- Getters para Properties ---
    public IntegerProperty idCategoriaProperty() {
        return idCategoria;
    }

    public StringProperty nombreProperty() {
        return nombre;
    }

    public StringProperty descripcionProperty() {
        return descripcion;
    }

    // --- Getters para valores primitivos ---
    public int getIdCategoria() {
        return idCategoria.get();
    }

    public String getNombre() {
        return nombre.get();
    }

    public String getDescripcion() {
        return descripcion.get();
    }

    // Sobreescribir toString para mostrar el nombre en ComboBoxes
    @Override
    public String toString() {
        return getNombre();
    }
}