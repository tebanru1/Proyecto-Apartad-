package com.example.Controlador;

import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import com.example.DAO.IngresosDAO;
import com.example.Modelo.ingreso;

/**
 * Controlador principal para el manejo de ingresos
 * Proporciona validaciones y operaciones de negocio para el registro de ingresos
 */
public class IngresoController {
    
    private IngresosDAO ingresosDAO;
    private static final String PATRON_CEDULA = "^[0-9]{7,10}$"; // Patrón para validar cédula (7-10 dígitos)
    
    public IngresoController() {
        this.ingresosDAO = new IngresosDAO();
    }
    
    /**
     * Registra un nuevo ingreso con fecha y hora del sistema
     * @param cedula Cédula del usuario
     * @return true si el registro fue exitoso, false en caso contrario
     */
    public boolean registrarIngreso(String cedula) {
        // Validar cédula
        if (!validarCedula(cedula)) {
            JOptionPane.showMessageDialog(null, 
                "Cédula inválida. Debe contener entre 7 y 10 dígitos numéricos.", 
                "Error de Validación", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Obtener fecha y hora actual del sistema
        long tiempoActual = System.currentTimeMillis();
        Date fechaActual = new Date(tiempoActual);
        Time horaActual = new Time(tiempoActual);
        
        // Crear objeto ingreso
        ingreso nuevoIngreso = new ingreso(cedula, fechaActual, horaActual);
        
        // Intentar registrar en la base de datos
        boolean resultado = ingresosDAO.Ingreso(nuevoIngreso);
        
        if (resultado) {
            JOptionPane.showMessageDialog(null, 
                "Ingreso registrado exitosamente para la cédula: " + cedula + 
                "\nFecha: " + fechaActual.toString() + " Hora: " + horaActual.toString(), 
                "Registro Exitoso", 
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, 
                "Error al registrar el ingreso. Verifique la conexión a la base de datos.", 
                "Error de Registro", 
                JOptionPane.ERROR_MESSAGE);
        }
        
        return resultado;
    }
    
    /**
     * Registra un ingreso con la fecha y hora actual (alias del método principal)
     * @param cedula Cédula del usuario
     * @return true si el registro fue exitoso, false en caso contrario
     */
    public boolean registrarIngresoConFechaActual(String cedula) {
        return registrarIngreso(cedula);
    }
    
    /**
     * Valida el formato de la cédula
     * @param cedula Cédula a validar
     * @return true si es válida, false en caso contrario
     */
    private boolean validarCedula(String cedula) {
        if (cedula == null || cedula.trim().isEmpty()) {
            return false;
        }
        
        // Eliminar espacios y verificar patrón
        cedula = cedula.trim();
        return Pattern.matches(PATRON_CEDULA, cedula);
    }
    
    /**
     * Valida si una cédula tiene el formato correcto sin mostrar mensajes
     * @param cedula Cédula a validar
     * @return true si es válida, false en caso contrario
     */
    public boolean esCedulaValida(String cedula) {
        return validarCedula(cedula);
    }
    
    /**
     * Obtiene la fecha actual en formato String
     * @return Fecha actual en formato yyyy-MM-dd
     */
    public String obtenerFechaActual() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new java.util.Date());
    }
    
    /**
     * Obtiene la hora actual en formato String
     * @return Hora actual en formato HH:mm
     */
    public String obtenerHoraActual() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(new java.util.Date());
    }
    
    /**
     * Obtiene la fecha y hora actual en formato String
     * @return Fecha y hora actual en formato yyyy-MM-dd HH:mm
     */
    public String obtenerFechaHoraActual() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new java.util.Date());
    }
}
