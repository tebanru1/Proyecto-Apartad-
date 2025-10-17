package com.example.Controlador;


import com.example.Modelo.Visitantes;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;  
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;



public class VisitanteController implements Initializable {
   @FXML
    private TextField txtcedula;
    @FXML
    private TextField txtnombre;
    @FXML
    private TextField txtapellido;
    @FXML
    private ComboBox<String> AREA;
    @FXML
    private ComboBox<String> ROL;
    @FXML
    private Button btnIngresar;
    @FXML
    private Button btnSalir;
    @FXML
    private TableView<Visitantes> tablaVisitantes;
    @FXML
    private TableColumn<Visitantes, Integer> columnaCedula;
    @FXML
    private TableColumn<Visitantes, String> columnaNombre;
    @FXML
    private TableColumn<Visitantes, String> columnaApellido;
    @FXML
    private TableColumn<Visitantes, String> columnaArea;
    @FXML
    private TableColumn<Visitantes, String> ColumnaRol;
    @FXML
    private TableColumn<Visitantes, Date> columnaFecha;
    @FXML
    private TableColumn<Visitantes, Time> Hora_ingreso;
    @FXML
    private TableColumn<Visitantes, Time> Hora_salida;

    private Conexion conexion;
    private ObservableList<Visitantes> listaVisitantes;
    private PreparedStatement pstmt;
    private Connection con;
    private java.sql.ResultSet rs;

    @Override
public void initialize(URL location, ResourceBundle resources) {
    try {
        conexion = new Conexion();
        
        configurarEventos();
        configurarTablaVisitantes();
        ConfigurarComboBox();
        configurarEventoTabla();
        cargarVisitantesDesdeBaseDeDatos();
        ConfigurarTextField();
       
    } catch (Exception e) {
      
    }
}
   private void configurarTablaVisitantes() {
    columnaCedula.setCellValueFactory(new PropertyValueFactory<>("cedula"));
    columnaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
    columnaApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
    columnaArea.setCellValueFactory(new PropertyValueFactory<>("area"));
    ColumnaRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
    columnaFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
    Hora_ingreso.setCellValueFactory(new PropertyValueFactory<>("horaIngreso"));
    Hora_salida.setCellValueFactory(new PropertyValueFactory<>("horaSalida"));

    listaVisitantes = FXCollections.observableArrayList();
    tablaVisitantes.setItems(listaVisitantes);
}

public void configurarEventos() {
    btnIngresar.setOnAction(event -> ingresarVisitante());
    btnSalir.setOnAction(event -> registrarSalidaVisitante());
}

public void ConfigurarComboBox() {
    AREA.getItems().addAll(
        "ATENCIÓN Y TRATAMIENTO", "COMANDO DE VIGILANCIA", "DIRECCIÓN", "DOMICILIARIA", "EXPENDIO", "JURIDICA", "PAGADURIA", "PABELLONES", "POLICIA JUDICIAL", "RANCHO", "RESEÑA", "SANIDAD", "OTRO"
    );
    ROL.getItems().addAll(
        "ABOGADO", "COMFENALCO", "DOCENTE", "DOMICILIARIO", "ESTUDIANTE", "FUTURASEO", "IGLESIA", "PONAL", "TRANSPORTADOR", "VISITANTE", "OTRO"
    );
    AREA.setPromptText("SELECCIONE ÁREA");
    ROL.setPromptText("SELECCIONE ROL");
}
public void ConfigurarTextField() {
    // Configurar TextField para aceptar solo números en txtcedula
    
    txtcedula.textProperty().addListener((observable, oldValue, newValue) -> {
        if (!newValue.matches("\\d*")) {
            txtcedula.setText(newValue.replaceAll("[^\\d]", ""));
        }
        if (txtcedula.getText().length() > 10) {
            String s = txtcedula.getText().substring(0, 10);
            txtcedula.setText(s);
        }
    });
    
}
public void ingresarVisitante() {
    try {
        // Validar campos obligatorios
        if (txtcedula.getText().isEmpty() || txtnombre.getText().isEmpty() || 
            txtapellido.getText().isEmpty() || AREA.getValue() == null || ROL.getValue() == null) {
            mostrarMensaje("Todos los campos son obligatorios.");
            return;
        }

        // Validar cédula: solo números y máximo 10 dígitos
        String cedulaTexto = txtcedula.getText();
        if (!cedulaTexto.matches("\\d{1,10}")) {
            mostrarMensaje("La cédula debe ser solo números y máximo 10 dígitos.");
            return;
        }

        int cedula = Integer.parseInt(cedulaTexto);
        String nombre = txtnombre.getText().toUpperCase();
        String apellido = txtapellido.getText().toUpperCase();
        String area = AREA.getValue().toString();
        String rol = ROL.getValue().toString();
        Date fecha = Date.valueOf(LocalDate.now());
        Time horaIngreso = Time.valueOf(LocalTime.now());
        Time horaSalida = null; // se registrará al salir

        Visitantes visitante = new Visitantes(cedula, nombre, apellido, area, rol, fecha, horaIngreso, horaSalida, null);
        
        GuardarVisitante(visitante);     // Guardar en base de datos
        listaVisitantes.add(visitante);  // Agregar a la lista observable

        mostrarMensaje("Visitante ingresado correctamente.");
        limpiarCampos();

    } catch (NumberFormatException e) {
        mostrarMensaje("La cédula debe ser un número válido.");
    } catch (Exception e) {
        System.out.println("Error al ingresar visitante: " + e.toString());
        mostrarMensaje("Error al ingresar visitante: " + e.getMessage());
    }
}

public void registrarSalidaVisitante() {
    try {
        int cedula = Integer.parseInt(txtcedula.getText());
        Time horaSalida = Time.valueOf(LocalTime.now());
        for (Visitantes visitante : listaVisitantes) {
            if (visitante.getCedula() == cedula) {
                if (visitante.getHoraSalida() == null) {
                    visitante.setHoraSalida(horaSalida);
                    SalidaVisitante(visitante);
                    tablaVisitantes.refresh();
                } else {
                    mostrarMensaje("Ya se ha registrado la salida para este visitante.");
                }
                break;
            }
        }
        limpiarCampos();
    } catch (Exception e) {
        System.out.println("Error al registrar salida: " + e.toString());
    }
}
private void limpiarCampos() {
    txtcedula.clear();
    txtnombre.clear();
    txtapellido.clear();
    AREA.setValue(null);
    ROL.setValue(null);
}
private void GuardarVisitante(Visitantes visitante) {
    String sql = "INSERT INTO visitantes (cedula, nombre, apellido, area, rol, fecha, horaIngreso, horaSalida, huella) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try {
        con=conexion.conectar();
        pstmt=con.prepareStatement(sql);
        pstmt.setInt(1, visitante.getCedula());
        pstmt.setString(2, visitante.getNombre());
        pstmt.setString(3, visitante.getApellido());
        pstmt.setString(4, visitante.getArea());
        pstmt.setString(5, visitante.getRol());
        pstmt.setDate(6, visitante.getFecha());
        pstmt.setTime(7, visitante.getHoraIngreso());
        pstmt.setTime(8, visitante.getHoraSalida());
        pstmt.setBytes(9, visitante.getHuella());
        pstmt.executeUpdate();
    } catch (Exception e) {
        System.out.println("Error al guardar visitante: " + e.toString());
    } finally {
        try {
            if (pstmt != null) {
                pstmt.close();
            }
        } catch (Exception e) {
            System.out.println("Error al cerrar recursos: " + e.toString());
        }
    }}
private void SalidaVisitante(Visitantes visitante) {
    String sql = "UPDATE visitantes SET horaSalida = ? WHERE cedula = ?";
    try {
        con=conexion.conectar();
        pstmt = con.prepareStatement(sql);
        pstmt.setTime(1, visitante.getHoraSalida());
        pstmt.setInt(2, visitante.getCedula());
        pstmt.executeUpdate();
    } catch (Exception e) {
        System.out.println("Error al registrar salida: " + e.toString());
    } finally {
        try {
            if (pstmt != null) {
                pstmt.close();
            }
        } catch (Exception e) {
            System.out.println("Error al cerrar recursos: " + e.toString());
        }
    }}
    private void configurarEventoTabla() {
        tablaVisitantes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtcedula.setText(String.valueOf(newSelection.getCedula()));
                txtnombre.setText(newSelection.getNombre());
                txtapellido.setText(newSelection.getApellido());
                AREA.setValue(newSelection.getArea());
                ROL.setValue(newSelection.getRol());
            }
        });
    }
    public ObservableList<Visitantes> getListaVisitantes() {
        return listaVisitantes;
        }
        public void cargarVisitantesDesdeBaseDeDatos() {
        String sql = "SELECT * FROM visitantes";
        try {
            con = conexion.conectar();
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                int cedula = rs.getInt("cedula");
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido");
                String area = rs.getString("area");
                String rol = rs.getString("rol");
                Date fecha = rs.getDate("fecha");
                Time horaIngreso = rs.getTime("horaIngreso");
                Time horaSalida = rs.getTime("horaSalida");
                byte[] huella = rs.getBytes("huella");
                Visitantes visitante = new Visitantes(cedula, nombre, apellido, area, rol, fecha, horaIngreso, horaSalida, huella);
                listaVisitantes.add(visitante);
            }
        } catch (Exception e) {
            System.out.println("Error al cargar visitantes: " + e.toString());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                System.out.println("Error al cerrar recursos: " + e.toString());
            }
        }
    }
    private void mostrarMensaje(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}