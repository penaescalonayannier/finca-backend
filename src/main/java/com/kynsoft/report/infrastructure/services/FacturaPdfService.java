package com.kynsoft.report.infrastructure.services;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.kynsoft.report.domain.dto.ItemSalidaDto;
import com.kynsoft.report.domain.dto.SalidaDto;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class FacturaPdfService {

    private static final DeviceRgb HEADER_BG = new DeviceRgb(66, 139, 202);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(245, 245, 245);
    private static final DeviceRgb BORDER_COLOR = new DeviceRgb(200, 200, 200);
    private static final DeviceRgb TEXT_DARK = new DeviceRgb(51, 51, 51);

    public byte[] generarFactura(SalidaDto salida, String suministradorNombre, String suministradorCodigo,
                                  String suministradorDir, String suministradorMunicipio) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.LETTER);
        document.setMargins(30, 30, 30, 30);

        PdfFont fontBold = PdfFontFactory.createFont("Helvetica-Bold");
        PdfFont fontNormal = PdfFontFactory.createFont("Helvetica");

        // ========== FACTURA ==========
        generarPaginaFactura(document, salida, fontBold, fontNormal,
                suministradorNombre, suministradorCodigo, suministradorDir, suministradorMunicipio);

        // ========== LISTADO DE TRABAJADORES (continúa o nueva página según espacio) ==========
        generarPaginaTrabajadores(document, salida, fontBold, fontNormal);

        document.close();
        return baos.toByteArray();
    }

    private void generarPaginaFactura(Document document, SalidaDto salida, PdfFont fontBold, PdfFont fontNormal,
                                       String suministradorNombre, String suministradorCodigo,
                                       String suministradorDir, String suministradorMunicipio) {

        String tipoDoc = salida.getTipo().name().equals("VALE") ? "VALE DE SALIDA" : "FACTURA";

        // Título
        Paragraph titulo = new Paragraph(tipoDoc)
                .setFont(fontBold)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(titulo);

        // Número y fecha
        String fecha = salida.getFecha() != null
                ? salida.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "";
        Paragraph numeroFecha = new Paragraph(salida.getNumero() + "     Fecha: " + fecha)
                .setFont(fontNormal)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(numeroFecha);

        // Tabla de cabecera (Suministrador y Receptor)
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(10);

        // Suministrador
        Cell suministradorCell = new Cell()
                .setBorder(new SolidBorder(BORDER_COLOR, 1))
                .setPadding(6);
        suministradorCell.add(new Paragraph("SUMINISTRADOR").setFont(fontBold).setFontSize(9).setMarginBottom(3));
        suministradorCell.add(crearLineaInfo("Nombre:", "El Coloso S.A.", fontNormal));
        suministradorCell.add(crearLineaInfo("Código:", "", fontNormal));
        suministradorCell.add(crearLineaInfo("Dirección:", "Delicias, Puerto Padre, Las Tunas", fontNormal));
        suministradorCell.add(crearLineaInfo("Cuenta:", "", fontNormal));
        suministradorCell.add(crearLineaInfo("Finca:", salida.getFincaCode() + " " + salida.getFincaName(), fontNormal));
        headerTable.addCell(suministradorCell);

        // Receptor
        Cell receptorCell = new Cell()
                .setBorder(new SolidBorder(BORDER_COLOR, 1))
                .setPadding(6);
        receptorCell.add(new Paragraph("RECEPTOR").setFont(fontBold).setFontSize(9).setMarginBottom(3));
        receptorCell.add(crearLineaInfo("Destino:", formatDestino(salida.getDestino().name()), fontNormal));
        headerTable.addCell(receptorCell);

        document.add(headerTable);

        // Tabla de items
        Table itemsTable = new Table(UnitValue.createPercentArray(new float[]{8, 35, 12, 15, 15, 15}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(0);

        // Encabezados
        String[] headers = {"No.", "Producto", "U/M", "Cantidad", "Precio", "Importe"};
        for (String header : headers) {
            itemsTable.addHeaderCell(new Cell()
                    .add(new Paragraph(header).setFont(fontBold).setFontSize(8))
                    .setBackgroundColor(HEADER_BG)
                    .setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorder(new SolidBorder(BORDER_COLOR, 1))
                    .setPadding(4));
        }

        // Fila única con el producto
        List<ItemSalidaDto> items = salida.getItems();
        double totalGeneral = 0.0;
        int totalCantidad = 0;

        if (items != null && !items.isEmpty()) {
            for (ItemSalidaDto item : items) {
                double precio = item.getPrecio() != null ? item.getPrecio() : 0.0;
                totalGeneral += item.getCantidad() * precio;
                totalCantidad += item.getCantidad();
            }
        }

        Double precioUnitario = items != null && !items.isEmpty() ? items.get(0).getPrecio() : 0.0;
        if (precioUnitario == null) precioUnitario = 0.0;

        String productoDesc = salida.getProductoCode() + " - " + salida.getProductoName();

        itemsTable.addCell(crearCeldaTabla("1", fontNormal, TextAlignment.CENTER, ColorConstants.WHITE));
        itemsTable.addCell(crearCeldaTabla(productoDesc, fontNormal, TextAlignment.LEFT, ColorConstants.WHITE));
        itemsTable.addCell(crearCeldaTabla("UND", fontNormal, TextAlignment.CENTER, ColorConstants.WHITE));
        itemsTable.addCell(crearCeldaTabla(String.valueOf(totalCantidad), fontNormal, TextAlignment.CENTER, ColorConstants.WHITE));
        itemsTable.addCell(crearCeldaTabla(String.format("$%.2f", precioUnitario), fontNormal, TextAlignment.RIGHT, ColorConstants.WHITE));
        itemsTable.addCell(crearCeldaTabla(String.format("$%.2f", totalGeneral), fontNormal, TextAlignment.RIGHT, ColorConstants.WHITE));

        // Agregar fila de TOTAL directamente a la tabla de items
        itemsTable.addCell(new Cell(1, 5)
                .add(new Paragraph("TOTAL").setFont(fontBold).setFontSize(8))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(new SolidBorder(BORDER_COLOR, 1))
                .setPadding(3));
        itemsTable.addCell(new Cell()
                .add(new Paragraph(String.format("$%.2f", totalGeneral)).setFont(fontBold).setFontSize(8))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(new SolidBorder(BORDER_COLOR, 1))
                .setPadding(3));

        document.add(itemsTable);

        // Firmas
        Table firmasTable = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(0);

        firmasTable.addCell(crearCeldaFirma("Entregado", fontBold, fontNormal));
        firmasTable.addCell(crearCeldaFirma("Transportador", fontBold, fontNormal));
        firmasTable.addCell(crearCeldaFirma("Recibido", fontBold, fontNormal));
        firmasTable.addCell(crearCeldaFirma("Anotado", fontBold, fontNormal));

        document.add(firmasTable);
    }

    private void generarPaginaTrabajadores(Document document, SalidaDto salida, PdfFont fontBold, PdfFont fontNormal) {
        // Separador
        document.add(new Paragraph("\n").setMarginTop(10));

        // Título
        Paragraph titulo = new Paragraph("LISTADO DE TRABAJADORES")
                .setFont(fontBold)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(3);
        document.add(titulo);

        // Subtítulo con info de la salida
        String tipoDoc = salida.getTipo().name().equals("VALE") ? "Vale" : "Factura";
        Paragraph subtitulo = new Paragraph(tipoDoc + ": " + salida.getNumero() + " | Producto: " + salida.getProductoName() + " | Destino: " + formatDestino(salida.getDestino().name()))
                .setFont(fontNormal)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(6);
        document.add(subtitulo);

        // Tabla de trabajadores
        Table table = new Table(UnitValue.createPercentArray(new float[]{8, 47, 15, 15, 15}))
                .setWidth(UnitValue.createPercentValue(100));

        // Encabezados
        String[] headers = {"No.", "Trabajador", "Cantidad", "Precio", "Total"};
        for (String header : headers) {
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(header).setFont(fontBold).setFontSize(8).setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(HEADER_BG)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorder(new SolidBorder(BORDER_COLOR, 1))
                    .setPadding(3));
        }

        // Filas de trabajadores
        List<ItemSalidaDto> items = salida.getItems();
        double totalGeneral = 0.0;
        int totalCantidad = 0;

        if (items != null) {
            int num = 1;
            boolean alternar = false;
            for (ItemSalidaDto item : items) {
                double precio = item.getPrecio() != null ? item.getPrecio() : 0.0;
                double total = item.getCantidad() * precio;
                totalGeneral += total;
                totalCantidad += item.getCantidad();

                Color rowBg = alternar ? LIGHT_GRAY : ColorConstants.WHITE;
                alternar = !alternar;

                table.addCell(crearCeldaTabla(String.valueOf(num++), fontNormal, TextAlignment.CENTER, rowBg));
                table.addCell(crearCeldaTabla(item.getTrabajadorNombre() != null ? item.getTrabajadorNombre() : "", fontNormal, TextAlignment.LEFT, rowBg));
                table.addCell(crearCeldaTabla(String.valueOf(item.getCantidad()), fontNormal, TextAlignment.CENTER, rowBg));
                table.addCell(crearCeldaTabla(String.format("$%.2f", precio), fontNormal, TextAlignment.RIGHT, rowBg));
                table.addCell(crearCeldaTabla(String.format("$%.2f", total), fontNormal, TextAlignment.RIGHT, rowBg));
            }
        }

        document.add(table);

        // Totales
        Table totalTable = new Table(UnitValue.createPercentArray(new float[]{55, 15, 15, 15}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(0);

        totalTable.addCell(new Cell()
                .add(new Paragraph("TOTALES").setFont(fontBold).setFontSize(8).setFontColor(ColorConstants.WHITE))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(new SolidBorder(BORDER_COLOR, 1))
                .setBackgroundColor(HEADER_BG)
                .setPadding(3));
        totalTable.addCell(new Cell()
                .add(new Paragraph(String.valueOf(totalCantidad)).setFont(fontBold).setFontSize(8).setFontColor(ColorConstants.WHITE))
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(new SolidBorder(BORDER_COLOR, 1))
                .setBackgroundColor(HEADER_BG)
                .setPadding(3));
        totalTable.addCell(new Cell()
                .add(new Paragraph("").setFont(fontBold).setFontSize(8))
                .setBorder(new SolidBorder(BORDER_COLOR, 1))
                .setBackgroundColor(HEADER_BG)
                .setPadding(3));
        totalTable.addCell(new Cell()
                .add(new Paragraph(String.format("$%.2f", totalGeneral)).setFont(fontBold).setFontSize(8).setFontColor(ColorConstants.WHITE))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(new SolidBorder(BORDER_COLOR, 1))
                .setBackgroundColor(HEADER_BG)
                .setPadding(3));

        document.add(totalTable);
    }

    private Paragraph crearLineaInfo(String etiqueta, String valor, PdfFont font) {
        return new Paragraph(etiqueta + " " + (valor != null ? valor : ""))
                .setFont(font)
                .setFontSize(8)
                .setMarginBottom(1);
    }

    private Cell crearCeldaTabla(String texto, PdfFont font, TextAlignment alignment, Color bgColor) {
        return new Cell()
                .add(new Paragraph(texto).setFont(font).setFontSize(8))
                .setTextAlignment(alignment)
                .setBorder(new SolidBorder(BORDER_COLOR, 1))
                .setBackgroundColor(bgColor)
                .setPadding(3);
    }

    private Cell crearCeldaFirma(String titulo, PdfFont fontBold, PdfFont fontNormal) {
        Cell cell = new Cell()
                .setBorder(new SolidBorder(BORDER_COLOR, 0.5f))
                .setPadding(6)
                .setBackgroundColor(LIGHT_GRAY);

        // Título centrado
        cell.add(new Paragraph(titulo)
                .setFont(fontBold)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(8));

        // Campos de firma
        cell.add(new Paragraph("Nombre: ____________________")
                .setFont(fontNormal)
                .setFontSize(7)
                .setMarginBottom(4));
        cell.add(new Paragraph("Cargo: _____________________")
                .setFont(fontNormal)
                .setFontSize(7)
                .setMarginBottom(4));
        cell.add(new Paragraph("Firma: _____________________")
                .setFont(fontNormal)
                .setFontSize(7)
                .setMarginBottom(4));
        cell.add(new Paragraph("CI: ________________________")
                .setFont(fontNormal)
                .setFontSize(7));

        return cell;
    }

    private String formatDestino(String destino) {
        return switch (destino) {
            case "TRABAJADORES" -> "Trabajadores";
            case "COMEDOR" -> "Comedor";
            case "VENTA_ESTADO" -> "Venta Estado";
            case "POBLACION" -> "Población";
            case "INSUMO" -> "Insumo";
            case "OTROS" -> "Otros";
            default -> destino;
        };
    }
}
