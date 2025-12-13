package com.example.Controlador;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.Button;

import java.awt.Desktop;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.example.DAO.AutorizacionesDAO;
import com.example.DAO.IngresosAdministrativosDAO;
import com.example.DAO.VisitanteDAO;
import com.example.Modelo.Autorizaciones;
import com.example.Modelo.Usuario;
import com.example.Modelo.ingresoAdministrativos;



public class InicioController implements Initializable, UsuarioReceptor {

    @FXML
    private AnchorPane ContenedorAutorizaciones,ContenedorImagen;
    @FXML
    private ImageView ImagenPerfil;
    @FXML VBox ContenedorIngresos;
    @FXML private Label visitasH;
    @FXML TextField TFnombre;
    @FXML TextField TFapellido;
    @FXML TextField TFcargo;
    @FXML TextField TFcedula;
    @FXML Button BtnSubirFoto;
    @FXML Label txtnombre, AdminHoy,salidasPendietes;
    @FXML Label txtgrado;
    @FXML Button btnregistrar;
    @FXML Button btnRecargar;
    @FXML HBox contenedorbotones;
    @FXML Button btnIngresar;
    @FXML Button btnSalida;



    private final VisitanteDAO visitanteDAO = new VisitanteDAO();
    private final AutorizacionesDAO dao = new AutorizacionesDAO();
    private final IngresosAdministrativosDAO IngresosAdministrativosDAO = new IngresosAdministrativosDAO();
    private File fotofuncionario;
    private Usuario usuario;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarAutorizaciones();
        cargarimagen();
        actualizarVisitasHoy();
        configurarEventos();
        configurarEventosTextField();
        mostrarInfoUsuario();
        cargarIngresosDelDia();
        iniciarActualizacionAutomatica();
        salidasPendietes();
        AdminLaborando();
    }
private void configurarEventos() {
    BtnSubirFoto.setOnAction(event -> CargarImagenPerfil());
    btnregistrar.setOnAction(event -> RegistrarAdministrativo());
    btnRecargar.setOnAction(event -> limpiarCampos());
    btnIngresar.setOnAction(event -> RegistroBiometrico());
    btnSalida.setOnAction(event -> RegistroBiometricoSalida());
}
private void configurarEventosTextField() {
    // Solo dígitos para cédula y máximo 10 caracteres
    TFcedula.textProperty().addListener((observable, valorAnterior, valorNuevo) -> {
        if (valorNuevo == null) return;
        String soloDigitos = valorNuevo.replaceAll("[^\\d]", "");
        
        if (!soloDigitos.equals(valorNuevo)) {
            int posicionCursor = TFcedula.getCaretPosition();
            TFcedula.setText(soloDigitos);
            TFcedula.positionCaret(Math.min(posicionCursor, soloDigitos.length()));
        }
        if (TFcedula.getText().length() > 10) {
            TFcedula.setText(TFcedula.getText().substring(0, 10));
        }
    });

    // Helper para limpiar y convertir a MAYÚSCULAS manteniendo la posición del cursor
    java.util.function.BiConsumer<TextField, String> aplicarTextoLimpioMayus = (campo, textoLimpio) -> {
        String textoMayus = textoLimpio.toUpperCase(java.util.Locale.getDefault());
        if (!textoMayus.equals(campo.getText())) {
            int posicionCursor = campo.getCaretPosition();
            campo.setText(textoMayus);
            campo.positionCaret(Math.min(posicionCursor, textoMayus.length()));
        }
    };

    // Nombres, apellidos y cargo: solo letras, espacios, guion y apóstrofo (soporta acentos) y en MAYÚSCULAS
    TFnombre.textProperty().addListener((obs, valorAnterior, valorNuevo) -> {
        if (valorNuevo == null) return;
        String textoLimpio = valorNuevo.replaceAll("[^\\p{L}\\s]", "");
        aplicarTextoLimpioMayus.accept(TFnombre, textoLimpio);
    });

    TFapellido.textProperty().addListener((obs, valorAnterior, valorNuevo) -> {
        if (valorNuevo == null) return;
        String textoLimpio = valorNuevo.replaceAll("[^\\p{L}\\s]", "");
        aplicarTextoLimpioMayus.accept(TFapellido, textoLimpio);
    });

    TFcargo.textProperty().addListener((obs, valorAnterior, valorNuevo) -> {
        if (valorNuevo == null) return;
        String textoLimpio = valorNuevo.replaceAll("[^\\p{L}\\s]", "");
        aplicarTextoLimpioMayus.accept(TFcargo, textoLimpio);
    });
}
@Override
 public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        mostrarInfoUsuario();
        if (usuario.getRol().equals("Administrador")) {
        Button[] botones = {btnregistrar, BtnSubirFoto};
        TextField[] campos = {TFnombre, TFapellido, TFcargo, TFcedula};
        for (Button boton : botones) {
            boton.setVisible(true);
            boton.setDisable(false);
            boton.setManaged(true);
        }
     for (TextField campo : campos) {
            campo.setEditable(true);
            campo.getStyleClass().add("text-field-editable");
        }

    } else {
        TextField[] campos = {TFnombre, TFapellido, TFcargo,};
        for (TextField campo : campos) {
            campo.setEditable(false);
            campo.getStyleClass().add("text-field-readonly");
            campo.setMouseTransparent(true);
        }
        btnregistrar.setDisable(true);
        contenedorbotones.setVisible(false);
        contenedorbotones.setManaged(false);
        BtnSubirFoto.setVisible(false);
        BtnSubirFoto.setDisable(true);
        BtnSubirFoto.setManaged(false);
    }
    }
private void mostrarInfoUsuario() {
        if (usuario != null && txtnombre != null) {
            txtnombre.setText(usuario.getNombre() + " " + usuario.getApellido());
            txtgrado.setText(usuario.getGrado());
        }
    }
private void cargarIngresosDelDia() {
    ContenedorIngresos.getChildren().clear(); // limpiar VBox

    try {
        List<ingresoAdministrativos> lista = IngresosAdministrativosDAO.listarTodos();
        SimpleDateFormat formato12Horas = new SimpleDateFormat("hh:mm a");

        LocalDate hoy = LocalDate.now(); // fecha actual

        // Crear lista de eventos (ingreso o salida) para ordenar
        List<EventoIngreso> eventos = new ArrayList<>();

        for (ingresoAdministrativos reg : lista) {
            if (reg.getFecha() != null && reg.getFecha().toLocalDate().equals(hoy)) {
                // Evento ingreso
                if (reg.getHoraIngreso() != null) {
                    eventos.add(new EventoIngreso(reg, "INGRESO", reg.getHoraIngreso(), "#28a745"));
                }
                // Evento salida
                if (reg.getHoraSalida() != null) {
                    eventos.add(new EventoIngreso(reg, "SALIDA", reg.getHoraSalida(), "#dc3545"));
                }
            }
        }

        // Ordenar por hora descendente (última hora primero)
        eventos.sort((e1, e2) -> e2.getHora().compareTo(e1.getHora()));

        // Generar tarjetas
        for (EventoIngreso ev : eventos) {
            AnchorPane tarjeta = crearTarjeta(
                    ev.getRegistro().getFotoFuncionario(),
                    ev.getRegistro().getNombre() + " " + ev.getRegistro().getApellido(),
                    ev.getRegistro().getCargo(),
                    ev.getTipo(),
                    new Date(ev.getHora().getTime()),
                    ev.getColor(),
                    formato12Horas
            );
            ContenedorIngresos.getChildren().add(tarjeta);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}

// Clase auxiliar para representar un evento (ingreso o salida)
private static class EventoIngreso {
    private final ingresoAdministrativos registro;
    private final String tipo;
    private final Time hora;
    private final String color;

    public EventoIngreso(ingresoAdministrativos registro, String tipo, Time hora, String color) {
        this.registro = registro;
        this.tipo = tipo;
        this.hora = hora;
        this.color = color;
    }

    public ingresoAdministrativos getRegistro() { return registro; }
    public String getTipo() { return tipo; }
    public Time getHora() { return hora; }
    public String getColor() { return color; }
}


// Método auxiliar para crear una tarjeta
private AnchorPane crearTarjeta(byte[] fotoBytes, String nombre, String cargo, String tipo, Date hora, String colorFondo, SimpleDateFormat formato12Horas) {
    AnchorPane tarjeta = new AnchorPane();
    tarjeta.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #d0d0d0; -fx-border-radius: 12;");
    tarjeta.setPrefHeight(85);
    tarjeta.setPrefWidth(580);

    // FOTO CIRCULAR
    ImageView foto = new ImageView();
    foto.setFitWidth(55);
    foto.setFitHeight(55);
    try {
        if (fotoBytes != null) {
            foto.setImage(new Image(new ByteArrayInputStream(fotoBytes)));
        } else {
            foto.setImage(new Image(getClass().getResourceAsStream("/com/example/default_user.png")));
        }
    } catch (Exception e) {
        foto.setImage(new Image(getClass().getResourceAsStream("/com/example/default_user.png")));
    }
    Circle clip = new Circle(27.5, 27.5, 27.5);
    foto.setClip(clip);
    AnchorPane.setLeftAnchor(foto, 15.0);
    AnchorPane.setTopAnchor(foto, 15.0);

    // NOMBRE + CARGO
    Label lblNombre = new Label(nombre);
    lblNombre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
    Label lblCargo = new Label(cargo);
    lblCargo.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
    VBox centro = new VBox(lblNombre, lblCargo);
    centro.setSpacing(3);
    AnchorPane.setLeftAnchor(centro, 90.0);
    AnchorPane.setTopAnchor(centro, 22.0);

    // TIPO + HORA
    Label lblTipo = new Label(tipo);
    lblTipo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color:" + colorFondo + "; -fx-text-fill: white; -fx-padding: 2 6 2 6; -fx-background-radius: 6;");
    Label lblHora = new Label(formato12Horas.format(hora));
    lblHora.setStyle("-fx-font-size: 13px;");
    VBox derecha = new VBox(lblTipo, lblHora);
    derecha.setSpacing(3);
    derecha.setStyle("-fx-alignment: center-right;");
    AnchorPane.setRightAnchor(derecha, 20.0);
    AnchorPane.setTopAnchor(derecha, 22.0);

    tarjeta.getChildren().addAll(foto, centro, derecha);
    return tarjeta;
}


private void cargarAutorizaciones() {
        try {
            List<Autorizaciones> lista = dao.listarTodas();
            VBox contenedor = new VBox(8);
            contenedor.getStyleClass().add("card-autorizaciones-lista");
            contenedor.setPadding(new Insets(10));
            contenedor.setAlignment(Pos.TOP_CENTER);

            for (Autorizaciones a : lista) {
                HBox fila = new HBox(10);
                fila.setAlignment(Pos.CENTER_LEFT);
                fila.setPrefWidth(Double.MAX_VALUE);
                fila.getStyleClass().add("card-autorizaciones-fila");

                // --- Icono PDF ---
                InputStream iconStream = getClass().getResourceAsStream("/com/example/pdf.png");
                ImageView pdfIcon = new ImageView();
                if (iconStream != null) {
                    pdfIcon.setImage(new Image(iconStream));
                } 
                pdfIcon.setFitWidth(24);
                pdfIcon.setFitHeight(24);

                // --- Descripción ---
                javafx.scene.control.Label lblDescripcion = new javafx.scene.control.Label(a.getDescripcion());
                lblDescripcion.getStyleClass().add("card-autorizaciones-descripcion");
                lblDescripcion.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(lblDescripcion, Priority.ALWAYS);

                // --- Fecha ---
                javafx.scene.control.Label lblFecha = new javafx.scene.control.Label(
                        a.getFechaGeneracion() != null ? a.getFechaGeneracion() : ""
                );
                lblFecha.getStyleClass().add("card-autorizaciones-fecha");

                // --- Ensamble ---
                fila.getChildren().addAll(pdfIcon, lblDescripcion, lblFecha);
                autorizacionespdf(fila, a);
                contenedor.getChildren().add(fila);
            }

            // --- Scroll ---
            ScrollPane scroll = new ScrollPane(contenedor);
            scroll.getStyleClass().add("card-autorizaciones-scroll");
            scroll.setFitToWidth(true);

            ContenedorAutorizaciones.getChildren().setAll(scroll);
            AnchorPane.setTopAnchor(scroll, 0.0);
            AnchorPane.setBottomAnchor(scroll, 0.0);
            AnchorPane.setLeftAnchor(scroll, 0.0);
            AnchorPane.setRightAnchor(scroll, 0.0);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error al cargar autorizaciones", e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    

private void autorizacionespdf(HBox fila, Autorizaciones autorizacion) {
        fila.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                try {
                    byte[] datosPDF = dao.obtenerPDFPorId(autorizacion.getId());
                    if (datosPDF != null && datosPDF.length > 0) {
                        File archivoTemporal = File.createTempFile("autorizacion_" + autorizacion.getId(), ".pdf");
                        try (FileOutputStream fos = new FileOutputStream(archivoTemporal)) {
                            fos.write(datosPDF);
                        }
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().open(archivoTemporal);
                        } else {
                            mostrarAlerta("Advertencia", "El sistema no soporta apertura automática de PDF.", Alert.AlertType.WARNING);
                        }
                    } else {
                        mostrarAlerta("Sin PDF", "No se encontró un archivo PDF para esta autorización.", Alert.AlertType.WARNING);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mostrarAlerta("Error al abrir PDF", e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }
private void cargarimagen() {
    try {
        // fondo con cover centrado
        ContenedorImagen.setStyle(
            "-fx-background-image: url('/com/example/Carcel.jpg'); " +
            "-fx-background-size: cover; " +
            "-fx-background-position: center center; " +
            "-fx-background-repeat: no-repeat;"
        );

        // clip redondeado que recorta la imagen al tamaño del contenedor (responsive)
        Rectangle clip = new Rectangle();
        double radius = 40; // ajustar radio de esquinas
        clip.setArcWidth(radius);
        clip.setArcHeight(radius);
        clip.widthProperty().bind(ContenedorImagen.widthProperty());
        clip.heightProperty().bind(ContenedorImagen.heightProperty());
        ContenedorImagen.setClip(clip);

    } catch (Exception e) {
        e.printStackTrace();
        mostrarAlerta("Error al cargar la imagen", e.getMessage(), Alert.AlertType.ERROR);
    }
}

private void actualizarVisitasHoy(){
try {
    int visitasPresente = visitanteDAO.VisitasFaltante();
    visitasH.setText(String.valueOf(visitasPresente));
} catch (Exception e) {
    e.printStackTrace();
    mostrarAlerta("Error al actualizar visitas", e.getMessage(), Alert.AlertType.ERROR);
}}

private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
public void limpiarCampos() {
        TFcedula.clear();
        TFnombre.clear();
        TFapellido.clear();
        TFcargo.clear();
        fotofuncionario = null;
        Image ImagenporDefecto = new Image(getClass().getResourceAsStream("/com/example/usuario.png"));
        ImagenPerfil.setImage(ImagenporDefecto);
    }
private void CargarImagenPerfil() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen de Perfil");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Archivos de Imagen", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(ImagenPerfil.getScene().getWindow());
        if (selectedFile != null) {
            fotofuncionario = selectedFile;
            Image image = new Image(selectedFile.toURI().toString());
            ImagenPerfil.setImage(image);
            double radio = Math.min(ImagenPerfil.getFitWidth(), ImagenPerfil.getFitHeight()) / 2;
            Circle clip = new Circle(radio);
            clip.setCenterX(ImagenPerfil.getFitWidth() / 2);
            clip.setCenterY(ImagenPerfil.getFitHeight() / 2);
            ImagenPerfil.setClip(clip);

        }
    }
private void mostrarMensaje(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    private void RegistrarAdministrativo() {
    try {
        
        if (TFcedula.getText().isEmpty() || TFnombre.getText().isEmpty() 
                || TFapellido.getText().isEmpty() || TFcargo.getText().isEmpty()) {
            mostrarMensaje("Por favor complete todos los campos requeridos.");
            return; // salir del método si falta algún campo
        }

    
        String cedulat = TFcedula.getText();
        if (!cedulat.matches("\\d{1,10}")) {
            mostrarMensaje("La cédula debe contener hasta 10 dígitos.");
            return;
        }

        
        if (fotofuncionario == null) {
            mostrarMensaje("Debe seleccionar una imagen de perfil antes de registrar.");
            return;
        }

        
        String cedula = TFcedula.getText();
        String nombre = TFnombre.getText();
        String apellido = TFapellido.getText();
        String cargo = TFcargo.getText();

        
        byte[] fotoFuncionario = java.nio.file.Files.readAllBytes(fotofuncionario.toPath());

        
        ingresoAdministrativos in = new ingresoAdministrativos(cedula, nombre, apellido, cargo, fotoFuncionario);
        IngresosAdministrativosDAO.registrar(in);

        mostrarMensaje("Administrativo registrado correctamente.");
        limpiarCampos();

    } catch (Exception e) {
        e.printStackTrace();
        mostrarAlerta("Error al registrar administrativo", e.getMessage(), Alert.AlertType.ERROR);
    }
}
@FXML
private void buscarAdministrativo() {
    try {
        String cedula = TFcedula.getText().trim();

        if (cedula.isEmpty()) {
            mostrarMensaje("Ingrese la cédula para buscar.");
            return;
        }
        ingresoAdministrativos temp=new ingresoAdministrativos();
        temp.setCedula(cedula);
        ingresoAdministrativos admin = IngresosAdministrativosDAO.buscarIngreso(temp);

        if (admin != null) {
            // Mostrar datos en los TextField
            TFnombre.setText(admin.getNombre());
            TFapellido.setText(admin.getApellido());
            TFcargo.setText(admin.getCargo());

            // Mostrar la foto si existe
            byte[] foto = admin.getFotoFuncionario();
            if (foto != null) {
                Image image = new Image(new ByteArrayInputStream(foto));
                ImagenPerfil.setImage(image);

                // Hacer imagen redonda
                double radio = Math.min(ImagenPerfil.getFitWidth(), ImagenPerfil.getFitHeight()) / 2;
                Circle clip = new Circle(radio);
                clip.setCenterX(ImagenPerfil.getFitWidth() / 2);
                clip.setCenterY(ImagenPerfil.getFitHeight() / 2);
                ImagenPerfil.setClip(clip);
            } else {
                // Imagen por defecto
                Image defaultImage = new Image(getClass().getResourceAsStream("/com/example/usuario.png"));
                ImagenPerfil.setImage(defaultImage);
            }

            fotofuncionario = null;

        } else {
            mostrarMensaje("No se encontró administrativo con esa cédula.");
            limpiarCampos();
        }

    } catch (Exception e) {
        e.printStackTrace();
        mostrarAlerta("Error al buscar administrativo", e.getMessage(), Alert.AlertType.ERROR);
    }
}


 private void RegistroBiometricoSalida() {
    try {
        String cedula = TFcedula.getText().trim();
        if (cedula.isEmpty()) {
            mostrarMensaje("Debe ingresar una cédula");
            return;
        }

        // Verificar si existe la cédula en la tabla de Administrativos
        if (!IngresosAdministrativosDAO.existeCedula(cedula)) {
            mostrarMensaje("No se encontró un registro con esa cédula");
            return;
        }

        // Buscar el último ingreso pendiente de salida
        ingresoAdministrativos ultimoIngreso = IngresosAdministrativosDAO.buscarUltimoIngresoPendiente(cedula);

        if (ultimoIngreso != null) {
            // Abrir ventana de registro biométrico y pasar el ingreso
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/registroBiometrico.fxml"));
            Parent registroRoot = loader.load();

            IngresoBiometricoController controller = loader.getController();
            controller.setCedula(cedula);
            controller.setIngreso(ultimoIngreso); // Pasar los datos del ingreso
            controller.setUsuario(this.usuario);  // Pasar usuario actual si lo necesitas

            Stage stage = new Stage();
            stage.setScene(new Scene(registroRoot));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.setTitle("REGISTRO BIOMÉTRICO");
            stage.getIcons().add(new Image(ClassLoader.getSystemResourceAsStream("com/example/Logo_Institucional.png")));
            stage.show();

        } else {
            mostrarMensaje("No existe un ingreso pendiente para esta cédula.");
        }

    } catch (IOException e) {
        e.printStackTrace();
        mostrarMensaje("Error al abrir la ventana de registro biométrico");
    } catch (Exception e) {
        e.printStackTrace();
        mostrarMensaje("Error al verificar el ingreso pendiente");
    }
}
private void RegistroBiometrico() {
    try {
        String cedula = TFcedula.getText().trim();
        if (cedula.isEmpty()) {
            mostrarMensaje("Debe ingresar una cédula");
            return;
        }

        // Verificar si existe la cédula en la tabla de Administrativos
        if (!IngresosAdministrativosDAO.existeCedula(cedula)) {
            mostrarMensaje("No se encontró un registro con esa cédula");
            return;
        }
            // Abrir ventana de registro biométrico y pasar el ingreso
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/registroBiometrico.fxml"));
            Parent registroRoot = loader.load();

            IngresoBiometricoController controller = loader.getController();
            controller.setCedula(cedula);
            controller.setUsuario(this.usuario);  // Pasar usuario actual si lo necesitas

            Stage stage = new Stage();
            stage.setScene(new Scene(registroRoot));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.setTitle("REGISTRO BIOMÉTRICO");
            stage.getIcons().add(new Image(ClassLoader.getSystemResourceAsStream("com/example/Logo_Institucional.png")));
            stage.show();

    } catch (IOException e) {
        e.printStackTrace();
        mostrarMensaje("Error al abrir la ventana de registro biométrico");
    } catch (Exception e) {
        e.printStackTrace();
        mostrarMensaje("Error al verificar el ingreso pendiente");
    }
}
public void iniciarActualizacionAutomatica() {
    Timeline timeline = new Timeline(
        new KeyFrame(Duration.seconds(2), event -> cargarIngresosDelDia()),
        new KeyFrame(Duration.seconds(2), event -> actualizarVisitasHoy()),
        new KeyFrame(Duration.seconds(1), event -> AdminLaborando()),
        new KeyFrame(Duration.seconds(2), event -> salidasPendietes())
    );
    timeline.setCycleCount(Timeline.INDEFINITE); 
    timeline.play();
}
private void AdminLaborando(){
    try {
        int AdminL=IngresosAdministrativosDAO.contarAdmin();
        AdminHoy.setText(String.valueOf(AdminL));
    } catch (Exception e) {
        System.out.println(e.toString());
    }
}

private void salidasPendietes(){
    try {
        VisitanteDAO VisitanteDAO=new VisitanteDAO();

        int total1=IngresosAdministrativosDAO.contarAdmin();
        int total2=VisitanteDAO.VisitasFaltante();
        int PendientesSalida=total1+total2;
        salidasPendietes.setText(String.valueOf(PendientesSalida));
        
    } catch (Exception e) {
        System.out.println(e.toString());
    }
}

}
