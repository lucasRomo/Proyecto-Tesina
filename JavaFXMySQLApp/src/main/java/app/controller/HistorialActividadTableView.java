package app.controller;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.IntegerProperty;

import java.sql.Timestamp;

/**
 * Modelo de datos para la visualización del Historial de Actividad en un JavaFX TableView.
 * Utiliza propiedades de JavaFX para permitir la vinculación (binding) reactiva a la tabla.
 */
public class HistorialActividadTableView {

    // 1. ID del Registro de Actividad
    private final IntegerProperty idRegAct = new SimpleIntegerProperty();
    // 2. Fecha y hora de la modificación
    private final ObjectProperty<Timestamp> fechaModificacion = new SimpleObjectProperty<>();
    // 3. Nombre completo del usuario responsable (resultado del JOIN)
    private final StringProperty nombreUsuario = new SimpleStringProperty();
    // 4. Tabla que fue afectada
    private final StringProperty tablaAfectada = new SimpleStringProperty();
    // 5. Columna dentro de la tabla que fue modificada
    private final StringProperty columnaAfectada = new SimpleStringProperty();
    // 6. ID del registro modificado dentro de la tabla afectada
    private final IntegerProperty idRegistroModificado = new SimpleIntegerProperty();
    // 7. Dato previo a la modificación
    private final StringProperty datoPrevio = new SimpleStringProperty();
    // 8. Nuevo dato (después de la modificación)
    private final StringProperty datoModificado = new SimpleStringProperty();

    /**
     * Constructor utilizado por HistorialActividadDAO.obtenerTodosLosRegistros().
     */
    public HistorialActividadTableView(int idRegAct, Timestamp fechaModificacion, String nombreUsuario,
                                       String tablaAfectada, String columnaAfectada, int idRegistroModificado,
                                       String datoPrevio, String datoModificado) {
        this.idRegAct.set(idRegAct);
        this.fechaModificacion.set(fechaModificacion);
        this.nombreUsuario.set(nombreUsuario);
        this.tablaAfectada.set(tablaAfectada);
        this.columnaAfectada.set(columnaAfectada);
        this.idRegistroModificado.set(idRegistroModificado);
        this.datoPrevio.set(datoPrevio);
        this.datoModificado.set(datoModificado);
    }

    // --- Getters de Propiedades (Usados por PropertyValueFactory) ---

    // La TableColumn del FXML usa 'fechaModificacion'
    public ObjectProperty<Timestamp> fechaModificacionProperty() {
        return fechaModificacion;
    }

    // La TableColumn del FXML usa 'nombreUsuario'
    public StringProperty nombreUsuarioProperty() {
        return nombreUsuario;
    }

    // La TableColumn del FXML usa 'tablaAfectada'
    public StringProperty tablaAfectadaProperty() {
        return tablaAfectada;
    }

    // La TableColumn del FXML usa 'columnaAfectada'
    public StringProperty columnaAfectadaProperty() {
        return columnaAfectada;
    }

    // La TableColumn del FXML usa 'idRegistroModificado'
    public IntegerProperty idRegistroModificadoProperty() {
        return idRegistroModificado;
    }

    // La TableColumn del FXML usa 'datoPrevio'
    public StringProperty datoPrevioProperty() {
        return datoPrevio;
    }

    // La TableColumn del FXML usa 'datoModificado'
    public StringProperty datoModificadoProperty() {
        return datoModificado;
    }

    // Getters Simples (usados por CellFactory y otros)
    public Timestamp getFechaModificacion() {
        return fechaModificacion.get();
    }

    public String getnombreUsuario() {
        return nombreUsuario.get();
    }

    public String getTablaAfectada() {
        return tablaAfectada.get();
    }

    public String getColumnaAfectada() {
        return columnaAfectada.get();
    }

    public int getIdRegistroModificado() {
        return idRegistroModificado.get();
    }

    public String getDatoPrevio() {
        return datoPrevio.get();
    }

    public String getDatoModificado() {
        return datoModificado.get();
    }


    // ... otros getters simples
}