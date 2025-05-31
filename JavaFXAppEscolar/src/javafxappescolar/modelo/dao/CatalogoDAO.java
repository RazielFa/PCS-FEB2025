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
import javafxappescolar.modelo.pojo.Carrera;
import javafxappescolar.modelo.pojo.Facultad;

/**
 *
 * @author MSI
 */
public class CatalogoDAO {
        /**
     * Método que obtiene todas las facultades registradas en la base de datos.
     * @return Una lista de objetos de tipo Facultad.
     * @throws SQLException Si ocurre un error al conectar o consultar la base de datos.
     */
    public static ArrayList<Facultad> obtenerFacultad() throws SQLException{
        ArrayList<Facultad> facultades = new ArrayList<>();
        Connection conexionBD = ConexionBD.abrirConexion();
        if(conexionBD != null){
            String consulta = "SELECT idFacultad, nombre FROM facultad";
            PreparedStatement sentencia = conexionBD.prepareStatement(consulta);
            ResultSet resultado = sentencia.executeQuery();
            while(resultado.next()){
                facultades.add(convertirRegistroFacultad(resultado));
            }
            sentencia.close();
            resultado.close();
            conexionBD.close();
        }else{
            throw new SQLException("No se pudo conectar a la base de datos");
        }
        
        return facultades;
    }
    
        /**
     * Método que obtiene las carreras asociadas a una facultad específica.
     * @param idFacultad ID de la facultad.
     * @return Una lista de objetos de tipo Carrera pertenecientes a la facultad dada.
     * @throws SQLException Si ocurre un error al conectar o consultar la base de datos.
     */
    public static ArrayList<Carrera> obtenerCarreraPorFacultad(int idFacultad) throws SQLException{
        ArrayList<Carrera> carreras = new ArrayList<>();
        Connection conexionBD = ConexionBD.abrirConexion();
        if(conexionBD != null){
            String consulta = "SELECT idCarrera, nombre, codigo, idFacultad FROM carrera WHERE idFacultad = ?";
            PreparedStatement sentencia = conexionBD.prepareStatement(consulta);
            sentencia.setInt(1, idFacultad);
            ResultSet resultado = sentencia.executeQuery();
            while(resultado.next()){
                carreras.add(convertirRegistroCarrera(resultado));
            }
        }else{
            throw new SQLException("No se pudo conectar a la base de datos");
        }
        return carreras;
    }

        /**
     * Método auxiliar que convierte un registro de ResultSet en un objeto Facultad.
     * @param resultado El resultado de la consulta SQL.
     * @return Objeto de tipo Facultad con los datos del registro.
     * @throws SQLException Si ocurre un error al acceder a los datos del ResultSet.
     */
    private static Facultad convertirRegistroFacultad(ResultSet resultado) throws SQLException{
        Facultad facultad = new Facultad();
        facultad.setIdFacultad(resultado.getInt("idFacultad"));
        facultad.setNombre(resultado.getString("nombre"));
        return facultad;
    }

        /**
     * Método auxiliar que convierte un registro de ResultSet en un objeto Carrera.
     * @param resultado El resultado de la consulta SQL.
     * @return Objeto de tipo Carrera con los datos del registro.
     * @throws SQLException Si ocurre un error al acceder a los datos del ResultSet.
     */
    private static Carrera convertirRegistroCarrera(ResultSet resultado) throws SQLException{
        Carrera carrera = new Carrera();
        carrera.setIdCarrera(resultado.getInt("idCarrera"));
        carrera.setNombre(resultado.getString("nombre"));
        carrera.setCodigo(resultado.getString("codigo"));
        return carrera;
    }
}
