package app.model;

import javafx.beans.property.*;

public class Categoria {
    private final IntegerProperty idCategoria;
    private final StringProperty nombre;
    private final StringProperty descripcion;

    // Constructor completo
    public Categoria(int idCategoria, String nombre, String descripcion) {
        this.idCategoria = new SimpleIntegerProperty(idCategoria);
        this.nombre = new SimpleStringProperty(nombre);
        // Manejar el caso de que la descripción sea null (como en el constructor Sin Categoría)
        this.descripcion = new SimpleStringProperty(descripcion != null ? descripcion : "");
    }

    // Constructor para ChoiceBox/ComboBox (solo ID y nombre)
    public Categoria(int idCategoria, String nombre) {
        this(idCategoria, nombre, null);
    }

    // Constructor para manejar la opción "Sin Categoría"
    public Categoria() {
        this(0, "-- Sin Categoría --", null); // Nombre ajustado para el ComboBox
    }

    // --- Getters y Setters necesarios para el DAO y UI ---

    public int getIdCategoria() {
        return idCategoria.get();
    }

    // CORREGIDO: Setter para actualizar el ID después de guardar
    public void setIdCategoria(int idCategoria) {
        this.idCategoria.set(idCategoria);
    }

    public String getNombre() {
        return nombre.get();
    }

    // CORREGIDO: Getter necesario para CategoriaDAO.saveCategoria()
    public String getDescripcion() {
        return descripcion.get();
    }

    public StringProperty nombreProperty() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre.get();
    }
}