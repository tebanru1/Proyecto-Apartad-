package com.example.Controlador;

import com.example.DAO.VisitanteDAO;
import com.example.Modelo.Visitantes;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory; 
import javafx.scene.control.TableCell;
import javafx.util.Callback;
import java.text.DecimalFormat; 
import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JLabel;



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
    private TextField buscar;
    @FXML
    private Label VIngresada;
    @FXML
    private Label VSalir;
    @FXML
    private TableView<Visitantes> tablaVisitantes;
    @FXML
    private TableColumn<Visitantes, Long> columnaCedula;
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
    private ObservableList<Visitantes> listaVisitantes;
    private VisitanteDAO visitanteDAO;
    VisitanteDAO dao = new VisitanteDAO();
    

    @Override
public void initialize(URL location, ResourceBundle resources) {
    try {
        visitanteDAO = new VisitanteDAO(); // inicializar DAO
        configurarEventos();
        configurarTablaVisitantes();
        ConfigurarComboBox();
        configurarEventoTabla();
        cargarVisitantesDesdeBaseDeDatos();
        ConfigurarTextField();
        actualizarVisitasHoy();
        VisitantePorSalir();
       
    } catch (Exception e) {
      
    }
}


private void configurarTablaVisitantes() {
    DecimalFormat df = new DecimalFormat("#,###");

    columnaCedula.setCellFactory(new Callback<TableColumn<Visitantes, Long>, TableCell<Visitantes, Long>>() {
        @Override
        public TableCell<Visitantes, Long> call(TableColumn<Visitantes, Long> param) {
            return new TableCell<Visitantes, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(df.format(item).replace(',', '.'));
                    }
                }
            };
        }
});
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
   buscar.textProperty().addListener((observable, oldValue, newValue) -> {
        BuscarVisita(newValue);
    });
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

        long cedula = Long.parseLong(cedulaTexto);
        String nombre = txtnombre.getText().toUpperCase();
        String apellido = txtapellido.getText().toUpperCase();
        String area = AREA.getValue().toString();
        String rol = ROL.getValue().toString();
        Date fecha = Date.valueOf(LocalDate.now());
        Time horaIngreso = Time.valueOf(LocalTime.now());
        Time horaSalida = null; // se registrará al salir

        Visitantes visitante = new Visitantes(cedula, nombre, apellido, area, rol, fecha, horaIngreso, horaSalida, null);
        
        // Guardar usando DAO
        visitanteDAO.GuardarVisitante(visitante);
        listaVisitantes.add(visitante);
        VisitantePorSalir();
        actualizarVisitasHoy();
        mostrarMensaje("Visitante ingresado correctamente.");
        limpiarCampos();
        

    } catch (Exception e) {
        mostrarMensaje("Error al ingresar visitante: " + e.getMessage());
    }
}

public void registrarSalidaVisitante() {
    try {
        // Obtener la cédula del TextField
        String cedulaTexto = txtcedula.getText().trim();
        if (cedulaTexto.isEmpty()) {
            mostrarMensaje("Debe seleccionar o ingresar la cédula del visitante.");
            return;
        }

        long cedula = Long.parseLong(cedulaTexto);
        Time horaSalida = Time.valueOf(java.time.LocalTime.now());

        // Buscar la última entrada sin salida del visitante
        Visitantes ultimaEntradaPendiente = listaVisitantes.stream()
                .filter(v -> v.getCedula() == cedula && v.getHoraSalida() == null)
                .max((v1, v2) -> {
                    int fechaCompare = v1.getFecha().compareTo(v2.getFecha());
                    if (fechaCompare != 0) {
                        return fechaCompare;
                    } else {
                        return v1.getHoraIngreso().compareTo(v2.getHoraIngreso());
                    }
                })
                .orElse(null);

        if (ultimaEntradaPendiente == null) {
            mostrarMensaje("Ya se ha registrado la salida.");
        } else {
            ultimaEntradaPendiente.setHoraSalida(horaSalida);
            visitanteDAO.SalidaVisitanteDB(ultimaEntradaPendiente);
            tablaVisitantes.refresh();
            VisitantePorSalir();
            mostrarMensaje("Salida registrada correctamente.");
        }
        limpiarCampos();

    } catch (NumberFormatException e) {
        mostrarMensaje("Cédula inválida. Debe ser un número.");
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
public void cargarVisitantesDesdeBaseDeDatos() {
        try {
            listaVisitantes.clear();
            listaVisitantes.addAll(visitanteDAO.CargarVisitantesDB());
        } catch (Exception e) {
            System.out.println("Error al cargar visitantes: " + e.toString());
        }
    }
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
    private void mostrarMensaje(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
private void BuscarVisita(String busqueda) {
    try {
        listaVisitantes.clear();

        if (busqueda == null || busqueda.isEmpty()) {
            listaVisitantes.addAll(dao.CargarVisitantesDB());
        } else {
            // Búsqueda limpia
            busqueda = busqueda.trim();
            List<Visitantes> resultados = dao.buscarVisitantes(busqueda);

            if (!resultados.isEmpty()) {
                listaVisitantes.addAll(resultados);
            } // Si no hay resultados, opcionalmente no mostrar mensaje inmediato
        }

    } catch (Exception e) {
        mostrarMensaje("Error al buscar visitantes: " + e.getMessage());
        e.printStackTrace();
    }
}
private void actualizarVisitasHoy() {
    try {
        int total = dao.contarVisitasHoy();
        VIngresada.setText(String.valueOf(total));
    } catch (Exception e) {
        mostrarMensaje("Error al contar visitas: " + e.getMessage());
        e.printStackTrace();
    }
}
private void VisitantePorSalir() {
    try {
        int total = dao.VisitasFaltante();
        VSalir.setText(String.valueOf(total));
    } catch (Exception e) {
        mostrarMensaje("Error al contar visitas: " + e.getMessage());
        e.printStackTrace();
    }
}
}