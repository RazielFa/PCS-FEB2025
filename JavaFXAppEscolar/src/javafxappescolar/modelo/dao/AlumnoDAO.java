/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javafxappescolar.modelo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javafxappescolar.modelo.ConexionBD;
import javafxappescolar.modelo.pojo.Alumno;
import javafxappescolar.modelo.pojo.ResultadoOperacion;

/**
 *
 * @author MSI
 */
public class AlumnoDAO {
    public static ArrayList<Alumno> obtenerAlumnos() throws SQLException{
        ArrayList<Alumno> alumnos = new ArrayList<Alumno>();
        Connection conexionBD = ConexionBD.abrirConexion();
        
        if(conexionBD != null){
            String consulta = "SELECT idAlumno, a.nombre, apellidoPaterno, apellidoMaterno, matricula, email, a.idCarrera, " +
                "fechaNacimiento, c.nombre AS 'carrera', c.idFacultad,f.nombre AS 'facultad' " +
                "FROM alumno a " +
                "INNER JOIN carrera c ON c.idCarrera = a.idCarrera " +
                "INNER JOIN facultad f ON f.idFacultad = c.idFacultad";
            PreparedStatement sentencia = conexionBD.prepareStatement(consulta);
            ResultSet resultado = sentencia.executeQuery();
            while(resultado.next()){
                alumnos.add(convertirRegistroAlumno(resultado));
            }
            sentencia.close();
            resultado.close();
            conexionBD.close();
        }else{
            throw new SQLException("Sin conexión en la BD");
        }
        return alumnos;
    }
    
    public static ResultadoOperacion registrarAlumno(Alumno alumno)throws SQLException{
        ResultadoOperacion resultado = new ResultadoOperacion();
        Connection conexionBD = ConexionBD.abrirConexion();
        if(conexionBD != null){
            String sentencia = "INSERT INTO alumno (nombre, apellidoPaterno, apellidoMaterno, "
                    + "matricula, email, idCarrera, fechaNacimiento, foto) "
                    + "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement prepararSentencia = conexionBD.prepareStatement(sentencia);
            prepararSentencia.setString(1, alumno.getNombre());
            prepararSentencia.setString(2, alumno.getApellidoPaterno());
            prepararSentencia.setString(3, alumno.getApellidoMaterno());
            prepararSentencia.setString(4, alumno.getMatricula());
            prepararSentencia.setString(5, alumno.getEmail());
            prepararSentencia.setInt(6, alumno.getIdCarrera());
            prepararSentencia.setString(7, alumno.getFechaNacimiento());
            prepararSentencia.setBytes(8, alumno.getFoto());
            int filasAfectadas = prepararSentencia.executeUpdate();
            if(filasAfectadas == 1){
                resultado.setError(false);
                resultado.setMensaje("Alumno(a) resgistrado correctamente");
            }else{
                resultado.setError(true);
                resultado.setMensaje("Lo sentimos :'v por el momento no se puede registrar la información del alumno(a), por favor inténtelo más tarde.");
            }
        }else{
            throw new SQLException("Sin conexión en la base de datos");
        }
        return resultado;
    }
    
    public static byte[] obtenerFotoAlumno(int idAlumno) throws SQLException {
        byte[] foto = null;
        Connection conexionBD = ConexionBD.abrirConexion();
        if(conexionBD != null){
            String consulta = "SELECT foto, FROM alumno WHERE id_alumno = ?";
            PreparedStatement sentencia = conexionBD.prepareStatement(consulta);
            sentencia.setInt(1, idAlumno);
            ResultSet resultado = sentencia.executeQuery();
            if(resultado.next()){
                foto = resultado.getBytes("foto");
            }else{
                throw new SQLException("Sin conexión a la base de datos");
            }
        }
        return foto;
    }
    
    public static boolean verificarExistenciaMatricula(String matricula, int idAlumnoExcluir) throws SQLException{
        boolean existe = false;
        Connection conexionBD = ConexionBD.abrirConexion();
        if(conexionBD != null){
            String consulta = "SELECT COUNT(*) AS total FROM alumno WHERE matricula = ? AND idAlumno != ?";
            PreparedStatement sentencia = conexionBD.prepareStatement(consulta);
            sentencia.setString(1, matricula);
            sentencia.setInt(2, idAlumnoExcluir);
            ResultSet resultado = sentencia.executeQuery();
            if (resultado.next()) {
                existe = resultado.getInt(1) > 0;
            }
        }else{
            throw new SQLException("Sin conexión a la base de datos");
        }
        return existe;
    }
    
    public static ResultadoOperacion editarAlumno(Alumno alumno) throws SQLException {
        ResultadoOperacion resultadoOperacion = new ResultadoOperacion();
        Connection conexionBD = ConexionBD.abrirConexion();
        if(verificarExistenciaMatricula(alumno.getMatricula(), alumno.getIdAlumno())){
                throw new SQLException("Matrícula ya registrada en el sistema, ingrese una matrícula diferente");
        }
        if(conexionBD != null){
            String consulta = "UPDATE alumno SET nombre = ?, apellido_paterno = ?, apellido_materno = ?, matricula = ?, email = ?, fecha_nacimiento = ?, id_carrera =?, foto = ? WHERE id_alumno = ?";
            PreparedStatement sentencia = conexionBD.prepareCall(consulta);
            sentencia.setString(1, alumno.getNombre());
            sentencia.setString(2, alumno.getApellidoPaterno());
            sentencia.setString(3, alumno.getApellidoMaterno());
            sentencia.setString(4, alumno.getMatricula());
            sentencia.setString(5, alumno.getEmail());
            sentencia.setString(6, alumno.getFechaNacimiento());
            sentencia.setInt(7, alumno.getIdCarrera());
            sentencia.setBytes(8, alumno.getFoto());
            sentencia.setInt(9, alumno.getIdAlumno());
            
            int filasAfectadas = sentencia.executeUpdate(consulta);
            resultadoOperacion.setError(filasAfectadas <= 0);
            resultadoOperacion.setMensaje(resultadoOperacion.isError() ?
                    "Lo sentimos, no fue posible actualizar los datos del alumno" :
                    "La información del alumno fue actualizada correctamente");
        }else{
            throw new SQLException("Sin conexión a la base de datos");
        }
        return resultadoOperacion;
    }
    
    public static ResultadoOperacion eliminarAlumno(int idAlumno) throws SQLException{
        ResultadoOperacion resultadoOperacion = new ResultadoOperacion();
        Connection conexionBD = ConexionBD.abrirConexion();
        if(conexionBD != null){
            String consulta = "DELETE FROM alumno WHERE id_alumno = ?";
            PreparedStatement sentencia = conexionBD.prepareCall(consulta);
            sentencia.setInt(1, idAlumno);
            int filasAfectadas = sentencia.executeUpdate();
            resultadoOperacion.setError(filasAfectadas <= 0);
            resultadoOperacion.setMensaje(resultadoOperacion.isError() ?
                    "Lo sentimos, no fue posible eliminar al alumno" :
                    "La información del alumno fue eliminada correctamente");
        }else{
            throw new SQLException("Sin conexión a la base de datos");
        }
        return resultadoOperacion;
    }
    
    private static Alumno convertirRegistroAlumno(ResultSet resultado) throws SQLException{
        Alumno alumno = new Alumno();
        alumno.setIdAlumno(resultado.getInt("idAlumno"));
        alumno.setNombre(resultado.getString("nombre"));
        alumno.setApellidoPaterno(resultado.getString("apellidoPaterno"));
        alumno.setApellidoMaterno(resultado.getString("apellidoMaterno"));
        alumno.setApellidoMaterno(resultado.getString("apellidoMaterno"));
        alumno.setMatricula(resultado.getString("matricula"));
        alumno.setEmail(resultado.getString("email"));
        alumno.setIdCarrera(resultado.getInt("idCarrera"));
        alumno.setFechaNacimiento(resultado.getString("fechaNacimiento"));
        alumno.setCarrera(resultado.getString("carrera"));
        alumno.setIdFacultad(resultado.getInt("idFacultad"));
        alumno.setFacultad(resultado.getString("facultad"));
        
        return alumno;
    }
}