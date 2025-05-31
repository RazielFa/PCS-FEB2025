/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javafxappescolar.dominio;

import java.sql.SQLException;
import javafxappescolar.modelo.dao.AlumnoDAO;
import javafxappescolar.modelo.pojo.ResultadoOperacion;

/**
 *
 * @author MSI
 */
public class AlumnoDM {
    public static ResultadoOperacion verificarEstadoMatricula(String matricula){
        ResultadoOperacion resultado = new ResultadoOperacion();
        if(matricula.startsWith("S")){
            try {
                boolean existe = AlumnoDAO.verificarExistenciaMatricula(matricula, 0);
                resultado.setError(existe);
                if(existe){
                    resultado.setMensaje("La matrícula ya existe en los registros.");
                }
            } catch (SQLException ex) {
                resultado.setError(true);
                resultado.setMensaje("Por el momento no se puede validar la matricula, inténtelo más tarde.");
            }
        }else{
            resultado.setError(true);
            resultado.setMensaje("La matrícula no tiene el formato correcto");
        }
        return resultado;
    }
}
