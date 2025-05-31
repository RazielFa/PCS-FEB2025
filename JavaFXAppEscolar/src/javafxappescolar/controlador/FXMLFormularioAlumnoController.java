/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package javafxappescolar.controlador;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafxappescolar.dominio.AlumnoDM;
import javafxappescolar.interfaz.INotificacion;
import javafxappescolar.modelo.dao.AlumnoDAO;
import javafxappescolar.modelo.dao.CatalogoDAO;
import javafxappescolar.modelo.pojo.Alumno;
import javafxappescolar.modelo.pojo.Carrera;
import javafxappescolar.modelo.pojo.Facultad;
import javafxappescolar.modelo.pojo.ResultadoOperacion;
import javafxappescolar.utilidades.Utilidades;
import javax.imageio.ImageIO;

/**
 * FXML Controller class
 *
 * @author MSI
 */
public class FXMLFormularioAlumnoController implements Initializable {

    @FXML
    private ImageView ivFoto;
    @FXML
    private TextField tfNombre;
    @FXML
    private TextField tfApellidoPaterno;
    @FXML
    private TextField tfApellidoMaterno;
    @FXML
    private TextField tfMatricula;
    @FXML
    private TextField tfEmail;
    @FXML
    private DatePicker dpFechaNacimiento;
    @FXML
    private ComboBox<Facultad> cbFacultad;
    @FXML
    private ComboBox<Carrera> cbCarrera;
    
    ObservableList<Facultad> facultades;
    ObservableList<Carrera> carreras;
    File archivoFoto;
    INotificacion observador;
    Alumno alumnoEdicion;
    boolean esEdicion;
    @FXML
    private Button btnSeleccionarFoto;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarFacultades();
        seleccionarFacultad();
        btnSeleccionarFoto.setOnAction(event -> mostrarDialogoSeleccionFoto());
    }  
    
    public void inicializarInformacion(boolean esEdicion, Alumno alumnoEdicion, INotificacion observador){
        this.esEdicion = esEdicion;
        this.alumnoEdicion = alumnoEdicion;
        this.observador = observador;
        if(esEdicion){
            cargarInformacionEdicion();
        }
    }
    
    private void cargarInformacionEdicion(){
        tfMatricula.setText(alumnoEdicion.getMatricula());
        tfNombre.setText(alumnoEdicion.getNombre());
        tfApellidoPaterno.setText(alumnoEdicion.getApellidoPaterno());
        tfApellidoMaterno.setText(alumnoEdicion.getApellidoMaterno());
        tfEmail.setText(alumnoEdicion.getEmail());
        dpFechaNacimiento.setValue(alumnoEdicion.getFechaNacimiento() != null ?
                LocalDate.parse(alumnoEdicion.getFechaNacimiento()) : LocalDate.now());
        tfMatricula.setDisable(false);
        cbFacultad.getSelectionModel().select(obtnerPosicionFacultad(alumnoEdicion.getIdFacultad()));
        cbCarrera.getSelectionModel().select(obtenerPosicionCarrera(alumnoEdicion.getIdCarrera()));
        
        try {
            byte[] foto = AlumnoDAO.obtenerFotoAlumno(alumnoEdicion.getIdAlumno());
            ByteArrayInputStream input = new ByteArrayInputStream(foto);
            Image image = new Image(input);
            ivFoto.setImage(image);
            alumnoEdicion.setFoto(foto);
        } catch (SQLException ex) {
            Logger.getLogger(FXMLFormularioAlumnoController.class.getName()).log(Level.SEVERE, null, ex);
        }catch(NullPointerException e){
            Logger.getLogger(FXMLFormularioAlumnoController.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    private void cargarFacultades(){
        try {
            facultades = FXCollections.observableArrayList();
            List<Facultad> facultadesDAO = CatalogoDAO.obtenerFacultad();
            facultades.addAll(facultadesDAO);
            cbFacultad.setItems(facultades);
        } catch (SQLException ex) {
            Utilidades.mostrarAlertaSimple(Alert.AlertType.NONE, "Error al cargar", "Lo sentimos, por el momento no se puede mostrar la información de las facultades");
            cerrarVentana();
        }
    }
    
    private void seleccionarFacultad(){
        cbFacultad.valueProperty().addListener(new ChangeListener<Facultad>() {
            @Override
            public void changed(ObservableValue<? extends Facultad> observable, Facultad oldValue, Facultad newValue) {
                if(newValue != null){
                    cargarCarreras(newValue.getIdFacultad());
                }
            }
        });
    }
    
    private void cargarCarreras(int idFacultad){
        try {
            carreras = FXCollections.observableArrayList();
            List<Carrera> carrerasDAO = CatalogoDAO.obtenerCarreraPorFacultad(idFacultad);
            carreras.addAll(carrerasDAO);
            cbCarrera.setItems(carreras);
        } catch (SQLException ex) {
            Utilidades.mostrarAlertaSimple(Alert.AlertType.NONE, "Error al cargar", "Lo sentimos, por el momento no se puede mostrar la información de las carreras");
            cerrarVentana();
        }
        
    }
    
    @FXML
    private void clicSeleccionarFoto(ActionEvent event) {
        mostrarDialogoSeleccionFoto();
    }

    @FXML
    private void clicGuardar(ActionEvent event) {
        if(validarCampos()){
            try {
                if(!esEdicion){
                    ResultadoOperacion resultado = AlumnoDM.verificarEstadoMatricula(tfMatricula.getText());
                    if(!resultado.isError()){
                        Alumno alumno = obtenerAlumnoNuevo();
                        guardarAlumno(alumno);
                    }else{
                        Utilidades.mostrarAlertaSimple(Alert.AlertType.WARNING, "Verificar datos", resultado.getMensaje());
                    }
                    
                }else{
                    Alumno alumnoEdicion = obtenerAlumnoEdicion();
                    modificarAlumno(alumnoEdicion);
                }
            } catch (IOException ex) {
                Utilidades.mostrarAlertaSimple(Alert.AlertType.ERROR, "Error en foto", "Lo sentimos, la foto seleccionada no puede ser guardada.");
            }
        }
    }

    @FXML
    private void clicCancelar(ActionEvent event) {
        Utilidades.getEscenarioComponent(cbCarrera).close();
    }
    
    private void cerrarVentana() {
        Utilidades.getEscenarioComponent(cbCarrera).close();
    }
    
    private void mostrarDialogoSeleccionFoto(){
        FileChooser dialogoSeleccion = new FileChooser();
        dialogoSeleccion.setTitle("Selecciona una foto");
        FileChooser.ExtensionFilter filtroImg = new FileChooser.ExtensionFilter("Archivos jpg (.jpg)", "*.jpg");
        dialogoSeleccion.getExtensionFilters().add(filtroImg);
        //Todo filtro
        archivoFoto = dialogoSeleccion.showOpenDialog(Utilidades.getEscenarioComponent(tfNombre));
        if(archivoFoto != null){
            mostrarFotoPerfil(archivoFoto);
        }
    }
    
    private void mostrarFotoPerfil(File archivoFoto){
        try{
            BufferedImage bufferImg = ImageIO.read(archivoFoto);
            Image imagen = SwingFXUtils.toFXImage(bufferImg, null);
            ivFoto.setImage(imagen);
        }catch (IOException ex) {
            ex.printStackTrace();
        }

    }
    
    private boolean validarCampos(){
        Map<TextField, Integer> longitudesMaximas = new HashMap<>();
        longitudesMaximas.put(tfApellidoMaterno, 15);
        longitudesMaximas.put(tfApellidoPaterno, 100);
        longitudesMaximas.put(tfMatricula, 50);
        longitudesMaximas.put(tfNombre, 50);
      
        
        List<TextField> camposConError = new ArrayList<>();
        
        for (Map.Entry<TextField, Integer> entry : longitudesMaximas.entrySet()) {
            TextField field = entry.getKey();
            int longitudMaxima = entry.getValue();
            String texto = field.getText() == null ? "" : field.getText().trim();

            if (texto.isEmpty()) {
                camposConError.add(field);
                field.setStyle("-fx-border-color: red;"); // Resaltar campo vacío
            } else if (texto.length() > longitudMaxima) {
                camposConError.add(field);
                field.setText(""); //Limpiar campo
                field.setStyle("-fx-border-color: orange;"); // Resaltar campo con longitud excedida
            } else {
                field.setStyle(""); // Restablecer estilo si es válido
            }
        }
        
        if (!camposConError.isEmpty()) {
            StringBuilder mensaje = new StringBuilder("Errores en los siguientes campos:\n");
            for (TextField field : camposConError) {
                mensaje.append("- ").append(field.getId() != null ? field.getId() : field.getPromptText())
                      .append("\n");
            }
            Utilidades.mostrarAlertaSimple(Alert.AlertType.WARNING, "Error de validación", mensaje.toString());
            return false;
        }
        return true;    
    }
    
    private Alumno obtenerAlumnoNuevo() throws IOException{
        Alumno alumno = new Alumno();
        alumno.setNombre(tfNombre.getText());
        alumno.setApellidoPaterno(tfApellidoPaterno.getText());
        alumno.setApellidoMaterno(tfApellidoMaterno.getText());
        alumno.setEmail(tfEmail.getText());
        alumno.setMatricula(tfMatricula.getText());
        alumno.setFechaNacimiento(dpFechaNacimiento.getValue().toString());
        Carrera carrera = cbCarrera.getSelectionModel().getSelectedItem();
        alumno.setIdCarrera(carrera.getIdCarrera());
        byte[] foto = Files.readAllBytes(archivoFoto.toPath());
        alumno.setFoto(foto);
        return alumno;
    }
    
    private void guardarAlumno(Alumno alumno){
        try {
            ResultadoOperacion resultadoInsertar = AlumnoDAO.registrarAlumno(alumno);
            if(!resultadoInsertar.isError()){
                Utilidades.mostrarAlertaSimple(Alert.AlertType.INFORMATION, "Alumno(a) registrado", "El alumno(a)"
                        + alumno.getNombre() + " fue registrado con éxito.");
                Utilidades.getEscenarioComponent(tfNombre).close();
                observador.operacionExitosa("insertar", alumno.getNombre());
            }else{
                Utilidades.mostrarAlertaSimple(Alert.AlertType.ERROR, "Error al registrar", resultadoInsertar.getMensaje());
            }
        } catch (SQLException ex) {
            Utilidades.mostrarAlertaSimple(Alert.AlertType.ERROR, "Error de conexión", "No hay conexión con la base de datos");
        }
    }
    
    private int obtnerPosicionFacultad(int idFacultad){
        for (int i = 0; i < facultades.size(); i++) {
            if(facultades.get(i).getIdFacultad() == idFacultad){
                return i;
            }
        }
        return -1;
    }
    
    private int obtenerPosicionCarrera(int idCarrera){
        for (int i = 0; i < carreras.size(); i++) {
            if(carreras.get(i).getIdCarrera() == idCarrera){
                return i;
            }
        }
        return -1;
    }
    
    private void modificarAlumno(Alumno alumno){
        try {
            ResultadoOperacion resultadoOperacion = AlumnoDAO.editarAlumno(alumno);
            if(!resultadoOperacion.isError()){
                Utilidades.mostrarAlertaSimple(Alert.AlertType.INFORMATION, "Alumno" +alumno.getNombre() 
                        +"registrado con exito",resultadoOperacion.getMensaje());
                Utilidades.getEscenarioComponent(cbCarrera).close();
                observador.operacionExitosa("insertar", alumno.getNombre());
            }else{
                Utilidades.mostrarAlertaSimple(Alert.AlertType.ERROR, "No se pudo guardar al alumno",
                        resultadoOperacion.getMensaje());
            }
        } catch (SQLException ex) {
            Logger.getLogger(FXMLFormularioAlumnoController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private Alumno obtenerAlumnoEdicion() throws IOException{
        Alumno alumno = new Alumno();
        alumno.setIdAlumno(alumnoEdicion.getIdAlumno());
        alumno.setNombre(tfNombre.getText());
        alumno.setApellidoPaterno(tfApellidoPaterno.getText());
        alumno.setApellidoMaterno(tfApellidoMaterno.getText());
        alumno.setMatricula(tfMatricula.getText());
        alumno.setEmail(tfEmail.getText());
        alumno.setFechaNacimiento(dpFechaNacimiento.getValue().toString());
        alumno.setIdCarrera(cbCarrera.getSelectionModel().getSelectedItem().getIdCarrera());
        if(archivoFoto != null){
            byte[] foto = Files.readAllBytes(archivoFoto.toPath());
            alumno.setFoto(foto);
        }else{
            alumno.setFoto(alumnoEdicion.getFoto());
        }
        return alumno;
    }
}