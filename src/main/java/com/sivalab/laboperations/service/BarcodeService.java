package com.sivalab.laboperations.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.code39.Code39Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for generating QR codes and barcodes for lab operations
 * Supports various formats for different use cases:
 * - QR codes for comprehensive data (reports, patient info)
 * - Code128 for sample numbers and ULR numbers
 * - Code39 for visit IDs and simple identifiers
 */
@Service
public class BarcodeService {

    private static final int DEFAULT_QR_SIZE = 200;
    private static final int DEFAULT_BARCODE_WIDTH = 300;
    private static final int DEFAULT_BARCODE_HEIGHT = 50;

    /**
     * Generate QR code for comprehensive data (JSON format)
     * Used for: Lab reports, patient information, sample details
     */
    public byte[] generateQRCode(String data, int size) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
        
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, size, size, hints);
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(qrImage, "PNG", baos);
        return baos.toByteArray();
    }

    /**
     * Generate QR code with default size
     */
    public byte[] generateQRCode(String data) throws WriterException, IOException {
        return generateQRCode(data, DEFAULT_QR_SIZE);
    }

    /**
     * Generate Code128 barcode for alphanumeric data
     * Used for: Sample numbers, ULR numbers, batch numbers
     */
    public byte[] generateCode128Barcode(String data, int width, int height) throws IOException {
        Code128Bean bean = new Code128Bean();
        bean.setModuleWidth(0.3); // Width of the narrowest bar
        bean.setBarHeight(height * 0.8); // Height of bars (80% of total height)
        bean.setFontSize(8.0); // Font size for human readable text
        bean.setQuietZone(10.0); // Quiet zone on sides
        bean.doQuietZone(true);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapCanvasProvider canvas = new BitmapCanvasProvider(
            baos, "image/png", width, height, false, 0);
        
        bean.generateBarcode(canvas, data);
        canvas.finish();
        
        return baos.toByteArray();
    }

    /**
     * Generate Code128 barcode with default dimensions
     */
    public byte[] generateCode128Barcode(String data) throws IOException {
        return generateCode128Barcode(data, DEFAULT_BARCODE_WIDTH, DEFAULT_BARCODE_HEIGHT);
    }

    /**
     * Generate Code39 barcode for simple numeric/alphabetic data
     * Used for: Visit IDs, patient IDs, simple identifiers
     */
    public byte[] generateCode39Barcode(String data, int width, int height) throws IOException {
        Code39Bean bean = new Code39Bean();
        bean.setModuleWidth(0.3);
        bean.setBarHeight(height * 0.8);
        bean.setFontSize(8.0);
        bean.setQuietZone(10.0);
        bean.doQuietZone(true);
        bean.setChecksumMode(org.krysalis.barcode4j.ChecksumMode.CP_AUTO);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapCanvasProvider canvas = new BitmapCanvasProvider(
            baos, "image/png", width, height, false, 0);
        
        bean.generateBarcode(canvas, data);
        canvas.finish();
        
        return baos.toByteArray();
    }

    /**
     * Generate Code39 barcode with default dimensions
     */
    public byte[] generateCode39Barcode(String data) throws IOException {
        return generateCode39Barcode(data, DEFAULT_BARCODE_WIDTH, DEFAULT_BARCODE_HEIGHT);
    }

    /**
     * Generate QR code data for lab report
     * Contains: ULR number, patient info, report status, access URL
     */
    public String generateReportQRData(String ulrNumber, String patientName, String patientId, 
                                      String reportStatus, String accessUrl) {
        return String.format(
            "LAB_REPORT\n" +
            "ULR: %s\n" +
            "Patient: %s\n" +
            "ID: %s\n" +
            "Status: %s\n" +
            "URL: %s\n" +
            "Generated: %s",
            ulrNumber, patientName, patientId, reportStatus, accessUrl,
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }

    /**
     * Generate QR code data for sample
     * Contains: Sample number, type, collection info, status
     */
    public String generateSampleQRData(String sampleNumber, String sampleType, String collectedBy,
                                      String collectionDate, String status, String accessUrl) {
        return String.format(
            "LAB_SAMPLE\n" +
            "Sample: %s\n" +
            "Type: %s\n" +
            "Collected by: %s\n" +
            "Date: %s\n" +
            "Status: %s\n" +
            "URL: %s",
            sampleNumber, sampleType, collectedBy, collectionDate, status, accessUrl
        );
    }

    /**
     * Generate QR code data for patient visit
     * Contains: Visit ID, patient info, visit date, status
     */
    public String generateVisitQRData(Long visitId, String patientName, String patientId,
                                     String visitDate, String status, String accessUrl) {
        return String.format(
            "PATIENT_VISIT\n" +
            "Visit ID: %s\n" +
            "Patient: %s\n" +
            "Patient ID: %s\n" +
            "Date: %s\n" +
            "Status: %s\n" +
            "URL: %s",
            visitId, patientName, patientId, visitDate, status, accessUrl
        );
    }

    /**
     * Generate barcode for ULR number (Code128 format)
     * Removes special characters and formats for barcode compatibility
     */
    public byte[] generateULRBarcode(String ulrNumber) throws IOException {
        // Convert ULR format "SLN/2025/000001" to barcode-friendly format "SLN2025000001"
        String barcodeData = ulrNumber.replaceAll("[^A-Za-z0-9]", "");
        return generateCode128Barcode(barcodeData);
    }

    /**
     * Generate barcode for sample number (Code128 format)
     */
    public byte[] generateSampleBarcode(String sampleNumber) throws IOException {
        // Sample numbers are already barcode-friendly
        return generateCode128Barcode(sampleNumber);
    }

    /**
     * Generate barcode for visit ID (Code39 format)
     */
    public byte[] generateVisitBarcode(Long visitId) throws IOException {
        return generateCode39Barcode(String.valueOf(visitId));
    }

    /**
     * Generate comprehensive barcode package for a lab report
     * Returns map with different barcode types
     */
    public Map<String, byte[]> generateReportBarcodePackage(String ulrNumber, String patientName, 
                                                           String patientId, String reportStatus) 
            throws WriterException, IOException {
        Map<String, byte[]> barcodes = new HashMap<>();
        
        // QR code with comprehensive data
        String qrData = generateReportQRData(ulrNumber, patientName, patientId, reportStatus, 
                                           "/reports/view/" + ulrNumber);
        barcodes.put("qr_code", generateQRCode(qrData));
        
        // ULR barcode for quick scanning
        barcodes.put("ulr_barcode", generateULRBarcode(ulrNumber));
        
        // Patient ID barcode
        barcodes.put("patient_barcode", generateCode39Barcode(patientId));
        
        return barcodes;
    }

    /**
     * Generate comprehensive barcode package for a sample
     */
    public Map<String, byte[]> generateSampleBarcodePackage(String sampleNumber, String sampleType,
                                                           String collectedBy, String collectionDate,
                                                           String status) 
            throws WriterException, IOException {
        Map<String, byte[]> barcodes = new HashMap<>();
        
        // QR code with comprehensive data
        String qrData = generateSampleQRData(sampleNumber, sampleType, collectedBy, 
                                           collectionDate, status, "/samples/view/" + sampleNumber);
        barcodes.put("qr_code", generateQRCode(qrData));
        
        // Sample number barcode for quick scanning
        barcodes.put("sample_barcode", generateSampleBarcode(sampleNumber));
        
        return barcodes;
    }

    /**
     * Generate comprehensive barcode package for a visit
     */
    public Map<String, byte[]> generateVisitBarcodePackage(Long visitId, String patientName,
                                                          String patientId, String visitDate,
                                                          String status) 
            throws WriterException, IOException {
        Map<String, byte[]> barcodes = new HashMap<>();
        
        // QR code with comprehensive data
        String qrData = generateVisitQRData(visitId, patientName, patientId, visitDate, status,
                                          "/visits/view/" + visitId);
        barcodes.put("qr_code", generateQRCode(qrData));
        
        // Visit ID barcode
        barcodes.put("visit_barcode", generateVisitBarcode(visitId));
        
        // Patient ID barcode
        barcodes.put("patient_barcode", generateCode39Barcode(patientId));
        
        return barcodes;
    }
}
