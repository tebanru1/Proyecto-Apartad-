package com.example.Controlador;

import com.example.Modelo.ingresoAdministrativos;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReporteAdministrativosPDF {

    public void generarPDF(List<ingresoAdministrativos> lista, File archivo, TipoFiltro tipoFiltro,String valorFiltro) throws Exception {

        Document documento = new Document(PageSize.A4.rotate(), 20, 20, 10, 20);
        PdfWriter writer = PdfWriter.getInstance(documento, new FileOutputStream(archivo));

        HeaderFooterEvento event = new HeaderFooterEvento();
        writer.setPageEvent(event);

        documento.open();

        // Encabezado inicial
        documento.add(event.crearHeader());
        documento.add(Chunk.NEWLINE);
        documento.add(Chunk.NEWLINE);
        documento.add(new Paragraph(" "));

        // ============================================================
        // TABLA PRINCIPAL
        // ============================================================
        PdfPTable tabla = new PdfPTable(8);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{2f, 4f, 5f, 4f, 4f, 4f, 4f, 4f});

        Font cellFont = new Font(Font.FontFamily.HELVETICA, 9);

        agregarCeldaHeader(tabla, "NO.");
        agregarCeldaHeader(tabla, "CÉDULA");
        agregarCeldaHeader(tabla, "NOMBRE COMPLETO");
        agregarCeldaHeader(tabla, "CARGO");
        agregarCeldaHeader(tabla, "FECHA");
        agregarCeldaHeader(tabla, "INGRESO");
        agregarCeldaHeader(tabla, "SALIDA");
        agregarCeldaHeader(tabla, "USUARIO");

        // CONTENIDO
        int contador = 1;
        for (ingresoAdministrativos a : lista) {

            tabla.addCell(celdaContenido(String.valueOf(contador++), cellFont));
            tabla.addCell(celdaContenido(a.getCedula(), cellFont));
            tabla.addCell(celdaContenido(a.getNombre() + " " + a.getApellido(), cellFont));
            tabla.addCell(celdaContenido(a.getCargo(), cellFont));
            tabla.addCell(celdaContenido(a.getFecha().toString(), cellFont));
            tabla.addCell(celdaContenido(a.getHoraIngreso().toString(), cellFont));

            tabla.addCell(celdaContenido(
                    a.getHoraSalida() != null ? a.getHoraSalida().toString() : "PENDIENTE",
                    cellFont));

            tabla.addCell(celdaContenido(
                    a.getUsuario() != null ? a.getUsuario() : "-",
                    cellFont));
        }

        documento.add(tabla);
        documento.close();
    }

    // HEADER DE LA TABLA (solo línea inferior)
    private void agregarCeldaHeader(PdfPTable tabla, String texto) {
        Font font = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));

        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);

        celda.setBorder(Rectangle.BOTTOM); // Solo línea inferior
        celda.setBorderWidthBottom(1.2f);

        celda.setPaddingTop(6f);
        celda.setPaddingBottom(6f);

        tabla.addCell(celda);
    }

    // CELDA DE CONTENIDO SIN BORDES Y CENTRADA
    private PdfPCell celdaContenido(String texto, Font font) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setBorder(Rectangle.NO_BORDER);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celda.setPadding(5f);
        return celda;
    }

    // ============================================================
    // HEADER Y FOOTER (APARECE EN TODAS LAS PÁGINAS)
    // ============================================================
    class HeaderFooterEvento extends PdfPageEventHelper {

        Font tituloFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
        Font subFont = new Font(Font.FontFamily.HELVETICA, 13, Font.NORMAL);
        Font fechaFont = new Font(Font.FontFamily.HELVETICA, 10);

        PdfPTable crearHeader() {

            try {

                PdfPTable header = new PdfPTable(3);
                header.setWidthPercentage(100);
                header.setWidths(new float[]{3f, 10f, 3f}); // Para centrar título

                // LOGO
                PdfPCell celdaLogo;
                try {
                    URL urlLogo = getClass().getResource("/com/example/Logo_Institucional.png");
                    Image logo = (urlLogo != null) ? Image.getInstance(urlLogo) : null;
                    if (logo != null) {
                        logo.scaleAbsolute(80, 80);
                        celdaLogo = new PdfPCell(logo, false);
                    } else {
                        celdaLogo = new PdfPCell(new Phrase(""));
                    }
                } catch (Exception e) {
                    celdaLogo = new PdfPCell(new Phrase(""));
                }
                celdaLogo.setBorder(Rectangle.NO_BORDER);
                celdaLogo.setHorizontalAlignment(Element.ALIGN_LEFT);
                header.addCell(celdaLogo);

                // TÍTULO PRINCIPAL
                Paragraph titulo = new Paragraph(
                        "INSTITUTO NACIONAL PENITENCIARIO Y CARCELARIO INPEC\n" +
                        "CPMS APARTADÓ",
                        tituloFont
                );
                titulo.setAlignment(Element.ALIGN_CENTER);

                // SUBTÍTULO CENTRADO
                Paragraph subtitulo = new Paragraph(
                        "REPORTE DE ADMINISTRATIVOS REGISTRADOS",
                        subFont
                );
                subtitulo.setAlignment(Element.ALIGN_CENTER);
                subtitulo.setSpacingBefore(10f);

                PdfPCell celdaCentro = new PdfPCell();
                celdaCentro.addElement(titulo);
                celdaCentro.addElement(subtitulo);

                celdaCentro.setBorder(Rectangle.NO_BORDER);
                celdaCentro.setHorizontalAlignment(Element.ALIGN_CENTER);
                celdaCentro.setVerticalAlignment(Element.ALIGN_MIDDLE);
                header.addCell(celdaCentro);

                // FECHA / HORA
                String fechaHora = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a"));

                PdfPCell celdaFecha = new PdfPCell(new Phrase(fechaHora, fechaFont));
                celdaFecha.setBorder(Rectangle.NO_BORDER);
                celdaFecha.setHorizontalAlignment(Element.ALIGN_RIGHT);
                celdaFecha.setVerticalAlignment(Element.ALIGN_MIDDLE);
                header.addCell(celdaFecha);

                return header;

            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {

            try {
                // Encabezado
                PdfPTable header = crearHeader();
                header.writeSelectedRows(
                        0, -1,
                        document.leftMargin(),
                        document.getPageSize().getHeight() - 10,
                        writer.getDirectContent()
                );



            } catch (Exception e) {
                // ignorar
            }
        }
    }
}
