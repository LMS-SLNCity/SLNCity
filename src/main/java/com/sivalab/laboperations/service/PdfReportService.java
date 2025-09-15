package com.sivalab.laboperations.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.zxing.WriterException;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.sivalab.laboperations.entity.LabReport;
import com.sivalab.laboperations.entity.Visit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Service for generating NABL-compliant PDF reports
 */
@Service
public class PdfReportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final BarcodeService barcodeService;

    @Autowired
    public PdfReportService(BarcodeService barcodeService) {
        this.barcodeService = barcodeService;
    }
    
    /**
     * Generate PDF report from LabReport entity
     */
    public byte[] generatePdfReport(LabReport labReport) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        
        try {
            // Add header
            addReportHeader(document, labReport);
            
            // Add patient information
            addPatientInformation(document, labReport.getVisit());
            
            // Add test results
            addTestResults(document, labReport);
            
            // Add footer
            addReportFooter(document, labReport);
            
        } finally {
            document.close();
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Generate PDF using HTML template
     */
    public byte[] generatePdfFromHtml(LabReport labReport) throws IOException {
        String htmlContent = generateHtmlReport(labReport);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(htmlContent, baos);
        return baos.toByteArray();
    }
    
    /**
     * Add report header with lab information, ULR number, and barcodes
     */
    private void addReportHeader(Document document, LabReport labReport) {
        try {
            // Create header table with lab info and QR code
            Table mainHeaderTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}));
            mainHeaderTable.setWidth(UnitValue.createPercentValue(100));

            // Left side - Lab information
            Cell labInfoCell = new Cell();
            Paragraph labName = new Paragraph("SLN CITY LABORATORY")
                    .setFontSize(18)
                    .setBold();
            labInfoCell.add(labName);

            Paragraph labAddress = new Paragraph("NABL Accredited Laboratory\nAddress: Lab Address Here\nPhone: +91-XXXXXXXXXX")
                    .setFontSize(10);
            labInfoCell.add(labAddress);

            mainHeaderTable.addCell(labInfoCell);

            // Right side - QR Code
            Cell qrCell = new Cell();
            try {
                Visit visit = labReport.getVisit();
                String patientName = visit.getPatientDetails().get("name").asText();
                String patientId = visit.getPatientDetails().get("patientId").asText();

                String qrData = barcodeService.generateReportQRData(
                    labReport.getUlrNumber(), patientName, patientId,
                    labReport.getReportStatus().toString(), "/reports/view/" + labReport.getUlrNumber()
                );

                byte[] qrCodeBytes = barcodeService.generateQRCode(qrData, 120);
                Image qrImage = new Image(ImageDataFactory.create(qrCodeBytes));
                qrImage.setHorizontalAlignment(HorizontalAlignment.CENTER);
                qrCell.add(qrImage);

            } catch (Exception e) {
                // If QR code generation fails, add placeholder text
                qrCell.add(new Paragraph("QR Code\nUnavailable").setTextAlignment(TextAlignment.CENTER));
            }

            mainHeaderTable.addCell(qrCell);
            document.add(mainHeaderTable);

            // ULR Number and Report Info with Barcode
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{40, 40, 20}));
            infoTable.setWidth(UnitValue.createPercentValue(100));

            infoTable.addCell(new Cell().add(new Paragraph("ULR Number: " + labReport.getUlrNumber()).setBold()));
            infoTable.addCell(new Cell().add(new Paragraph("Report Type: " + labReport.getReportType()).setBold()));

            // ULR Barcode
            Cell barcodeCell = new Cell();
            try {
                byte[] barcodeBytes = barcodeService.generateULRBarcode(labReport.getUlrNumber());
                Image barcodeImage = new Image(ImageDataFactory.create(barcodeBytes));
                barcodeImage.setWidth(100);
                barcodeImage.setHorizontalAlignment(HorizontalAlignment.CENTER);
                barcodeCell.add(barcodeImage);
            } catch (Exception e) {
                barcodeCell.add(new Paragraph("Barcode\nUnavailable").setTextAlignment(TextAlignment.CENTER));
            }
            infoTable.addCell(barcodeCell);

            if (labReport.getGeneratedAt() != null) {
                infoTable.addCell(new Cell().add(new Paragraph("Report Date: " + labReport.getGeneratedAt().format(DATE_FORMATTER))));
            } else {
                infoTable.addCell(new Cell().add(new Paragraph("")));
            }

            if (labReport.getAuthorizedBy() != null) {
                infoTable.addCell(new Cell().add(new Paragraph("Authorized By: " + labReport.getAuthorizedBy())));
            } else {
                infoTable.addCell(new Cell().add(new Paragraph("")));
            }

            infoTable.addCell(new Cell().add(new Paragraph(""))); // Empty cell for alignment

            document.add(infoTable);
            document.add(new Paragraph("\n"));

        } catch (Exception e) {
            // Fallback to simple header if barcode generation fails
            addSimpleHeader(document, labReport);
        }
    }

    /**
     * Fallback simple header without barcodes
     */
    private void addSimpleHeader(Document document, LabReport labReport) {
        Paragraph labName = new Paragraph("SLN CITY LABORATORY")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18)
                .setBold();
        document.add(labName);

        Paragraph labAddress = new Paragraph("NABL Accredited Laboratory\nAddress: Lab Address Here\nPhone: +91-XXXXXXXXXX")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10);
        document.add(labAddress);

        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        headerTable.setWidth(UnitValue.createPercentValue(100));

        headerTable.addCell(new Cell().add(new Paragraph("ULR Number: " + labReport.getUlrNumber()).setBold()));
        headerTable.addCell(new Cell().add(new Paragraph("Report Type: " + labReport.getReportType()).setBold()));

        if (labReport.getGeneratedAt() != null) {
            headerTable.addCell(new Cell().add(new Paragraph("Report Date: " + labReport.getGeneratedAt().format(DATE_FORMATTER))));
        }
        if (labReport.getAuthorizedBy() != null) {
            headerTable.addCell(new Cell().add(new Paragraph("Authorized By: " + labReport.getAuthorizedBy())));
        }

        document.add(headerTable);
        document.add(new Paragraph("\n"));
    }
    
    /**
     * Add patient information section
     */
    private void addPatientInformation(Document document, Visit visit) throws IOException {
        Paragraph patientHeader = new Paragraph("PATIENT INFORMATION")
                .setBold()
                .setFontSize(14);
        document.add(patientHeader);
        
        // Get patient details from JSON
        JsonNode patientDetails = visit.getPatientDetails();
        
        Table patientTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        patientTable.setWidth(UnitValue.createPercentValue(100));
        
        addPatientField(patientTable, "Name", patientDetails.get("name"));
        addPatientField(patientTable, "Age", patientDetails.get("age"));
        addPatientField(patientTable, "Gender", patientDetails.get("gender"));
        addPatientField(patientTable, "Phone", patientDetails.get("phone"));
        addPatientField(patientTable, "Address", patientDetails.get("address"));
        addPatientField(patientTable, "Visit Date", visit.getCreatedAt().format(DATE_FORMATTER));
        
        document.add(patientTable);
        document.add(new Paragraph("\n"));
    }
    
    /**
     * Add test results section
     */
    private void addTestResults(Document document, LabReport labReport) throws IOException {
        Paragraph testsHeader = new Paragraph("LABORATORY RESULTS")
                .setBold()
                .setFontSize(14);
        document.add(testsHeader);

        if (labReport.getReportData() != null) {
            JsonNode reportData = labReport.getReportData();

            // Add test results
            if (reportData.has("tests")) {
                JsonNode tests = reportData.get("tests");
                for (JsonNode test : tests) {
                    // Add test name header
                    String testName = getJsonValue(test, "testName");
                    Paragraph testHeader = new Paragraph(testName)
                            .setBold()
                            .setFontSize(12);
                    document.add(testHeader);

                    // Create results table for this test
                    Table resultsTable = new Table(UnitValue.createPercentArray(new float[]{40, 20, 15, 25}));
                    resultsTable.setWidth(UnitValue.createPercentValue(100));

                    // Add table headers
                    resultsTable.addHeaderCell(new Cell().add(new Paragraph("Parameter").setBold()));
                    resultsTable.addHeaderCell(new Cell().add(new Paragraph("Result").setBold()));
                    resultsTable.addHeaderCell(new Cell().add(new Paragraph("Unit").setBold()));
                    resultsTable.addHeaderCell(new Cell().add(new Paragraph("Status").setBold()));

                    // Add test results if available
                    if (test.has("results")) {
                        JsonNode results = test.get("results");
                        results.fieldNames().forEachRemaining(fieldName -> {
                            if (!"conclusion".equals(fieldName)) {
                                JsonNode result = results.get(fieldName);
                                if (result.isObject()) {
                                    resultsTable.addCell(new Cell().add(new Paragraph(fieldName)));
                                    resultsTable.addCell(new Cell().add(new Paragraph(getJsonValue(result, "value"))));
                                    resultsTable.addCell(new Cell().add(new Paragraph(getJsonValue(result, "unit"))));
                                    resultsTable.addCell(new Cell().add(new Paragraph(getJsonValue(result, "status"))));
                                }
                            }
                        });

                        // Add conclusion if available
                        if (results.has("conclusion")) {
                            document.add(resultsTable);
                            Paragraph conclusion = new Paragraph("Conclusion: " + getJsonValue(results, "conclusion"))
                                    .setItalic()
                                    .setFontSize(10);
                            document.add(conclusion);
                        } else {
                            document.add(resultsTable);
                        }
                    } else {
                        resultsTable.addCell(new Cell().add(new Paragraph("No results available").setItalic()));
                        resultsTable.addCell(new Cell().add(new Paragraph("-")));
                        resultsTable.addCell(new Cell().add(new Paragraph("-")));
                        resultsTable.addCell(new Cell().add(new Paragraph("-")));
                        document.add(resultsTable);
                    }

                    document.add(new Paragraph("\n"));
                }
            } else {
                document.add(new Paragraph("No test results available.").setItalic());
            }
        } else {
            document.add(new Paragraph("No test results available.").setItalic());
        }

        document.add(new Paragraph("\n"));
    }
    
    /**
     * Add report footer with authorization and NABL compliance info
     */
    private void addReportFooter(Document document, LabReport labReport) {
        // Authorization section
        if (labReport.getAuthorizedBy() != null && labReport.getAuthorizedAt() != null) {
            Paragraph authSection = new Paragraph("AUTHORIZATION")
                    .setBold()
                    .setFontSize(12);
            document.add(authSection);
            
            Table authTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
            authTable.setWidth(UnitValue.createPercentValue(100));
            
            authTable.addCell(new Cell().add(new Paragraph("Authorized By: " + labReport.getAuthorizedBy())));
            authTable.addCell(new Cell().add(new Paragraph("Date: " + labReport.getAuthorizedAt().format(DATETIME_FORMATTER))));
            
            document.add(authTable);
        }
        
        // NABL compliance footer
        document.add(new Paragraph("\n"));
        Paragraph nablFooter = new Paragraph("This report is generated in compliance with NABL 112 requirements.\n" +
                "ULR Number: " + labReport.getUlrNumber() + " ensures unique identification.\n" +
                "Report Status: " + labReport.getReportStatus())
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic();
        document.add(nablFooter);
    }
    
    /**
     * Helper method to add patient fields to table
     */
    private void addPatientField(Table table, String label, JsonNode value) {
        table.addCell(new Cell().add(new Paragraph(label + ":").setBold()));
        table.addCell(new Cell().add(new Paragraph(value != null ? value.asText() : "N/A")));
    }
    
    /**
     * Helper method to add patient fields to table with string value
     */
    private void addPatientField(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label + ":").setBold()));
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "N/A")));
    }
    
    /**
     * Helper method to safely get JSON values
     */
    private String getJsonValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return field != null ? field.asText() : "N/A";
    }
    
    /**
     * Generate HTML template for PDF conversion
     */
    public String generateHtmlReport(LabReport labReport) throws IOException {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
        html.append(".header { text-align: center; border-bottom: 2px solid #000; padding-bottom: 10px; }");
        html.append(".patient-info { margin: 20px 0; }");
        html.append(".results-table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        html.append(".results-table th, .results-table td { border: 1px solid #000; padding: 8px; text-align: left; }");
        html.append(".results-table th { background-color: #f2f2f2; }");
        html.append(".footer { margin-top: 30px; text-align: center; font-size: 10px; }");
        html.append("</style>");
        html.append("</head><body>");
        
        // Header
        html.append("<div class='header'>");
        html.append("<h1>SLN CITY LABORATORY</h1>");
        html.append("<p>NABL Accredited Laboratory<br>Address: Lab Address Here<br>Phone: +91-XXXXXXXXXX</p>");
        html.append("<p><strong>ULR Number: ").append(labReport.getUlrNumber()).append("</strong></p>");
        html.append("</div>");
        
        // Patient Information
        html.append("<div class='patient-info'>");
        html.append("<h2>PATIENT INFORMATION</h2>");
        
        JsonNode patientDetails = labReport.getVisit().getPatientDetails();
        html.append("<table>");
        html.append("<tr><td><strong>Name:</strong></td><td>").append(getJsonValue(patientDetails, "name")).append("</td></tr>");
        html.append("<tr><td><strong>Age:</strong></td><td>").append(getJsonValue(patientDetails, "age")).append("</td></tr>");
        html.append("<tr><td><strong>Gender:</strong></td><td>").append(getJsonValue(patientDetails, "gender")).append("</td></tr>");
        html.append("<tr><td><strong>Phone:</strong></td><td>").append(getJsonValue(patientDetails, "phone")).append("</td></tr>");
        html.append("</table>");
        html.append("</div>");
        
        // Test Results
        html.append("<h2>LABORATORY RESULTS</h2>");
        if (labReport.getReportData() != null) {
            JsonNode reportData = labReport.getReportData();
            html.append("<table class='results-table'>");
            html.append("<thead><tr><th>Test Parameter</th><th>Result</th><th>Unit</th><th>Reference Range</th></tr></thead>");
            html.append("<tbody>");
            
            if (reportData.has("tests")) {
                JsonNode tests = reportData.get("tests");
                for (JsonNode test : tests) {
                    if (test.has("parameters")) {
                        JsonNode parameters = test.get("parameters");
                        for (JsonNode param : parameters) {
                            html.append("<tr>");
                            html.append("<td>").append(getJsonValue(param, "name")).append("</td>");
                            html.append("<td>").append(getJsonValue(param, "value")).append("</td>");
                            html.append("<td>").append(getJsonValue(param, "unit")).append("</td>");
                            html.append("<td>").append(getJsonValue(param, "range")).append("</td>");
                            html.append("</tr>");
                        }
                    }
                }
            }
            
            html.append("</tbody></table>");
        }
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p>This report is generated in compliance with NABL 112 requirements.</p>");
        html.append("<p>ULR Number: ").append(labReport.getUlrNumber()).append(" ensures unique identification.</p>");
        if (labReport.getAuthorizedBy() != null) {
            html.append("<p>Authorized By: ").append(labReport.getAuthorizedBy()).append("</p>");
        }
        html.append("</div>");
        
        html.append("</body></html>");
        
        return html.toString();
    }
    

}
