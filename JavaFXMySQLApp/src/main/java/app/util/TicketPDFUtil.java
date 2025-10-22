package app.util;

import app.model.DetallePedido;
import app.model.Pedido;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.FileNotFoundException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TicketPDFUtil {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    // Eliminamos el DATE_FORMATTER ya que no se usa para la fecha de entrega estimada
    // private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Genera un ticket en formato PDF para un pedido específico y sus detalles.
     * El PDF se guarda en la ruta especificada.
     *
     * @param pedido El objeto Pedido con la información principal.
     * @param detalles La lista de DetallePedido con los productos.
     * @param dest La ruta de archivo donde se guardará el PDF (ej: /ruta/al/archivo.pdf).
     * @throws FileNotFoundException Si la ruta de destino no es válida.
     */
    public static void generarTicket(Pedido pedido, List<DetallePedido> detalles, String dest) throws FileNotFoundException {
        // Inicializa el escritor PDF (se encarga de escribir en el archivo)
        PdfWriter writer = new PdfWriter(dest);
        // Inicializa el documento PDF
        PdfDocument pdf = new PdfDocument(writer);
        // Inicializa el objeto Document de iText, que es el contenedor principal
        Document document = new Document(pdf);

        // --- TÍTULO Y ENCABEZADO ---
        document.add(new Paragraph("El Sur Centro de Copiado - Ticket de Pedido")
                .setFontSize(16).setBold().setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("ID Pedido: " + pedido.getIdPedido())
                .setFontSize(12).setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("Fecha Creación: " + pedido.getFechaCreacion().format(DATE_TIME_FORMATTER))
                .setFontSize(10).setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("\n--- Detalles del Cliente y Empleado ---\n")
                .setFontSize(10).setItalic().setTextAlignment(TextAlignment.LEFT));

        document.add(new Paragraph("Cliente: " + pedido.getNombreCliente()));
        document.add(new Paragraph("Empleado: " + pedido.getNombreEmpleado()));

        // --- ELIMINACIÓN DE LA FECHA DE ENTREGA ESTIMADA ---
        /* if (pedido.getFechaEntregaEstimada() != null) {
            document.add(new Paragraph("Entrega Estimada: " + pedido.getFechaEntregaEstimada().format(DATE_FORMATTER)));
        }
        */

        document.add(new Paragraph("\n--- Productos/Servicios ---\n")
                .setFontSize(10).setItalic().setTextAlignment(TextAlignment.LEFT));

        // --- TABLA DE DETALLES ---
        // Anchos de columnas: Cantidad(2), Descripción(5), P. Unitario(2), Subtotal(3)
        float[] columnWidths = {2, 5, 2, 3};
        Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();

        // Encabezados de la tabla
        table.addHeaderCell(new Paragraph("Cant.").setBold());
        table.addHeaderCell(new Paragraph("Descripción").setBold());
        table.addHeaderCell(new Paragraph("P. Unitario").setBold().setTextAlignment(TextAlignment.RIGHT));
        table.addHeaderCell(new Paragraph("Subtotal").setBold().setTextAlignment(TextAlignment.RIGHT));

        // Filas de detalles
        for (DetallePedido detalle : detalles) {
            // Celda 1: Cantidad
            table.addCell(new Paragraph(String.valueOf(detalle.getCantidad())));

            // Celda 2: Descripción
            table.addCell(new Paragraph(detalle.getDescripcion()));

            // Celda 3: Precio Unitario
            table.addCell(new Paragraph(String.format("$%.2f", detalle.getPrecioUnitario())).setTextAlignment(TextAlignment.RIGHT));

            // Celda 4: Subtotal
            table.addCell(new Paragraph(String.format("$%.2f", detalle.getSubtotal())).setTextAlignment(TextAlignment.RIGHT));
        }

        document.add(table);

        // --- RESUMEN DE PAGOS ---
        document.add(new Paragraph("\n=================================").setTextAlignment(TextAlignment.RIGHT));
        document.add(new Paragraph(String.format("Total: $%.2f", pedido.getMontoTotal()))
                .setFontSize(12).setBold().setTextAlignment(TextAlignment.RIGHT));
        document.add(new Paragraph(String.format("Monto Entregado: $%.2f", pedido.getMontoEntregado()))
                .setFontSize(10).setTextAlignment(TextAlignment.RIGHT));
        document.add(new Paragraph(String.format("Saldo Pendiente: $%.2f", pedido.getMontoTotal() - pedido.getMontoEntregado()))
                .setFontSize(10).setTextAlignment(TextAlignment.RIGHT));

        // --- INSTRUCCIONES ---
        if (pedido.getInstrucciones() != null && !pedido.getInstrucciones().trim().isEmpty()) {
            document.add(new Paragraph("\nInstrucciones Adicionales:").setFontSize(10).setBold());
            document.add(new Paragraph(pedido.getInstrucciones()).setFontSize(9));
        }

        // --- PIE DE PÁGINA ---
        document.add(new Paragraph("\nGracias por su compra!")
                .setFontSize(10).setTextAlignment(TextAlignment.CENTER).setItalic());

        // Cierra el documento y guarda el archivo
        document.close();
    }
}
