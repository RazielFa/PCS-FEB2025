/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javafxappescolar.modelo.pojo;

/**
 *
 * @author MSI
 */
public class Carrera {
    private int idCarrera;
    private String nombre;
    private String codigo;

    public Carrera() {
    }

    public Carrera(int idCarrera, String nombre, String codigo) {
        this.idCarrera = idCarrera;
        this.nombre = nombre;
        this.codigo = codigo;
    }

    public int getIdCarrera() {
        return idCarrera;
    }

    public void setIdCarrera(int idCarrera) {
        this.idCarrera = idCarrera;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
    
    @Override
    public String toString(){
        return "("+codigo+") " + nombre;
    }
}
