/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package javafxappescolar.controlador;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafxappescolar.JavaFXAppEscolar;
import javafxappescolar.interfaz.INotificacion;
import javafxappescolar.modelo.dao.AlumnoDAO;
import javafxappescolar.modelo.pojo.Alumno;
import javafxappescolar.modelo.pojo.ResultadoOperacion;
import javafxappescolar.utilidades.Utilidades;

/**
 * FXML Controller class
 *
 * @author MSI
 */
public class FXMLAdminAlumnoController implements Initializable, INotificacion {

    @FXML
    private TextField tfBuscar;
    @FXML
    private TableView<Alumno> tvAlumnos;
    @FXML
    private TableColumn colMatricula;
    @FXML
    private TableColumn colApePaterno;
    @FXML
    private TableColumn colApeMaterno;
    @FXML
    private TableColumn colNombre;
    @FXML
    private TableColumn colFacultad;
    @FXML
    private TableColumn colCarrera;
    private ObservableList<Alumno> alumnos;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabla();
        cargarInformacionTabla();
    }    
    
    private void configurarTabla(){
        colMatricula.setCellValueFactory(new PropertyValueFactory("matricula"));
        colNombre.setCellValueFactory(new PropertyValueFactory("nombre"));
        colApePaterno.setCellValueFactory(new PropertyValueFactory("apellidoPaterno"));
        colApeMaterno.setCellValueFactory(new PropertyValueFactory("apellidoMaterno"));
        colFacultad.setCellValueFactory(new PropertyValueFactory("facultad"));
        colCarrera.setCellValueFactory(new PropertyValueFactory("carrera"));
    }
    
    private void cargarInformacionTabla(){
        try {
            alumnos = FXCollections.observableArrayList();
            ArrayList<Alumno> alumnosDAO = AlumnoDAO.obtenerAlumnos();
            alumnos.addAll(alumnosDAO);
            tvAlumnos.setItems(alumnos);
        } catch (SQLException ex) {
            Utilidades.mostrarAlertaSimple(Alert.AlertType.ERROR, "Error al "
                    + "cargar", "Lo sentimos por el momento no se puede "
                            + "mostrar la información de los alumnos.");
            cerrarVentana();
        }
    }
    
    private void cerrarVentana(){
        ((Stage) tfBuscar.getScene().getWindow()).close();
    }

    @FXML
    private void btnClicAgregar(ActionEvent event) {
        irFormularioAlumno(false, null);
    }

    @FXML
    private void btnClicModificar(ActionEvent event) {
        Alumno alumno = tvAlumnos.getSelectionModel().getSelectedItem();
        if(alumno != null){
            irFormularioAlumno(true, alumno);
        }else{
            Utilidades.mostrarAlertaSimple(Alert.AlertType.WARNING, "No se seleccionó un alumno", "Selecciona un alumno de la tabla para editar");
        }
    }

    @FXML
    private void btnClicEliminar(ActionEvent event) {
        int pos = tvAlumnos.getSelectionModel().getSelectedIndex();
        if(pos >= 0){
            Alumno alumno = alumnos.get(pos);
            String confirmacion = String.format("¿Estás seguro de eliminar al alumno(a)? %s %s \nUna vez eliminado el registro no podrá ser recuperado.", alumno.getNombre(), alumno.getApellidoPaterno());
            if(Utilidades.mostrarConfirmacion("Eliminar alumno (a)", confirmacion)){
                eliminarAlumno(alumno.getIdAlumno());
            }
        }else{
            Utilidades.mostrarAlertaSimple(Alert.AlertType.WARNING, "No se seleccionó un alumno", "Selecciona un alumno de la tabla para eliminar");
        }
    }
    
    private void irFormularioAlumno(boolean esEdicion, Alumno alumnoEdicion){
        try {
            Stage escenarioFormulario = new Stage();
            FXMLLoader loader = new FXMLLoader(JavaFXAppEscolar.class.getResource("vista/FXMLFormularioAlumno.fxml"));
            Parent vista = loader.load();
            FXMLFormularioAlumnoController controlador = loader.getController();
            controlador.inicializarInformacion(esEdicion, alumnoEdicion, this);
            
            Scene escena = new Scene(vista);
            escenarioFormulario.setScene(escena);
            escenarioFormulario.setTitle("Formulario Alumno");
            escenarioFormulario.initModality(Modality.APPLICATION_MODAL);
            escenarioFormulario.showAndWait();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void operacionExitosa(String tipo, String nombreAlumno) {
        System.out.println("Operación: " + tipo + ", con el alumno(a): " + nombreAlumno);
        cargarInformacionTabla();
    }
    
    private void eliminarAlumno(int idAlumno){
        try {
            ResultadoOperacion resultado = AlumnoDAO.eliminarAlumno(idAlumno);
            if(!resultado.isError()){
                Utilidades.mostrarAlertaSimple(Alert.AlertType.INFORMATION, "Alumno(a) eliminado.", "El registro del alumno fue eliminado correctamente.");
                cargarInformacionTabla();
            }else{
                Utilidades.mostrarAlertaSimple(Alert.AlertType.ERROR, "Problemas al eliminar.", resultado.getMensaje());
            }
        } catch (SQLException ex) {
            Utilidades.mostrarAlertaSimple(Alert.AlertType.ERROR, "Problemas al eliminar", "Lo sentimos, por el momento no se puede eliminar.");
        }
    }
}
