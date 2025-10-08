package app.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.sql.Timestamp;

/**
 * Clase modelo para representar una fila en la TableView del historial de actividad.
 * Contiene los datos del registro de la base de datos y el nombre del usuario que lo modificó.
 */
public class HistorialActividadTableView {

    // Propiedades de JavaFX para enlace de datos (binding)
    private final IntegerProperty idRegAct;
    private final ObjectProperty<Timestamp> fechaModificacion;
    private final StringProperty nombreUsuario;
    private final StringProperty tablaAfectada;
    private final StringProperty columnaAfectada;
    private final IntegerProperty idRegistroModificado;
    private final StringProperty datoPrevioModificacion;
    private final StringProperty datoModificado;


    public HistorialActividadTableView(int idRegAct, Timestamp fechaModificacion, String nombreUsuario,
                                       String tablaAfectada, String columnaAfectada, int idRegistroModificado,
                                       String datoPrevioModificacion, String datoModificado) {

        this.idRegAct = new SimpleIntegerProperty(idRegAct);
        this.fechaModificacion = new SimpleObjectProperty<>(fechaModificacion);
        this.nombreUsuario = new SimpleStringProperty(nombreUsuario);
        this.tablaAfectada = new SimpleStringProperty(tablaAfectada);
        this.columnaAfectada = new SimpleStringProperty(columnaAfectada);
        this.idRegistroModificado = new SimpleIntegerProperty(idRegistroModificado);
        this.datoPrevioModificacion = new SimpleStringProperty(datoPrevioModificacion);
        this.datoModificado = new SimpleStringProperty(datoModificado);
    }

    // --- Getters para obtener valores directos ---

    public int getIdRegAct() {
        return idRegAct.get();
    }

    public Timestamp getFechaModificacion() {
        return fechaModificacion.get();
    }

    public String getNombreUsuario() {
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

    public String getDatoPrevioModificacion() {
        return datoPrevioModificacion.get();
    }

    public String getDatoModificado() {
        return datoModificado.get();
    }

    // --- Property Getters para enlace de datos (Binding) en JavaFX ---
    // (Estos son los métodos que usa la TableView para enlazar las columnas)

    public IntegerProperty idRegActProperty() {
        return idRegAct;
    }

    public ObjectProperty<Timestamp> fechaModificacionProperty() {
        return fechaModificacion;
    }

    public StringProperty nombreUsuarioProperty() {
        return nombreUsuario;
    }

    public StringProperty tablaAfectadaProperty() {
        return tablaAfectada;
    }

    public StringProperty columnaAfectadaProperty() {
        return columnaAfectada;
    }

    public IntegerProperty idRegistroModificadoProperty() {
        return idRegistroModificado;
    }

    public StringProperty datoPrevioModificacionProperty() {
        return datoPrevioModificacion;
    }

    public StringProperty datoModificadoProperty() {
        return datoModificado;
    }
}