package com.sivalab.laboperations.controller;

import com.google.zxing.WriterException;
import com.sivalab.laboperations.entity.LabReport;
import com.sivalab.laboperations.entity.Sample;
import com.sivalab.laboperations.entity.Visit;
import com.sivalab.laboperations.service.BarcodeService;
import com.sivalab.laboperations.service.LabReportService;
import com.sivalab.laboperations.service.SampleService;
import com.sivalab.laboperations.service.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for barcode and QR code generation
 * Provides endpoints for generating various types of codes for lab operations
 */
@RestController
@RequestMapping("/barcodes")
@CrossOrigin(origins = "*")
public class BarcodeController {

    private final BarcodeService barcodeService;
    private final LabReportService labReportService;
    private final SampleService sampleService;
    private final VisitService visitService;

    @Autowired
    public BarcodeController(BarcodeService barcodeService, LabReportService labReportService,
                           SampleService sampleService, VisitService visitService) {
        this.barcodeService = barcodeService;
        this.labReportService = labReportService;
        this.sampleService = sampleService;
        this.visitService = visitService;
    }

    /**
     * Generate QR code for lab report
     * GET /barcodes/reports/{reportId}/qr
     */
    @GetMapping("/reports/{reportId}/qr")
    public ResponseEntity<byte[]> generateReportQRCode(@PathVariable Long reportId,
                                                      @RequestParam(defaultValue = "200") int size) {
        try {
            Optional<LabReport> reportOpt = labReportService.getReportById(reportId);
            if (reportOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            LabReport report = reportOpt.get();
            Visit visit = report.getVisit();
            String patientName = visit.getPatientDetails().get("name").asText();
            String patientId = visit.getPatientDetails().get("patientId").asText();

            String qrData = barcodeService.generateReportQRData(
                report.getUlrNumber(), patientName, patientId,
                report.getReportStatus().toString(), "/reports/view/" + report.getUlrNumber()
            );

            byte[] qrCode = barcodeService.generateQRCode(qrData, size);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("inline", "report_qr_" + report.getUlrNumber().replace("/", "_") + ".png");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(qrCode);

        } catch (WriterException | IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate barcode for ULR number
     * GET /barcodes/reports/{reportId}/barcode
     */
    @GetMapping("/reports/{reportId}/barcode")
    public ResponseEntity<byte[]> generateReportBarcode(@PathVariable Long reportId,
                                                       @RequestParam(defaultValue = "300") int width,
                                                       @RequestParam(defaultValue = "50") int height) {
        try {
            Optional<LabReport> reportOpt = labReportService.getReportById(reportId);
            if (reportOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            LabReport report = reportOpt.get();
            byte[] barcode = barcodeService.generateULRBarcode(report.getUlrNumber());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("inline", "ulr_barcode_" + report.getUlrNumber().replace("/", "_") + ".png");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(barcode);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate complete barcode package for lab report
     * GET /barcodes/reports/{reportId}/package
     */
    @GetMapping("/reports/{reportId}/package")
    public ResponseEntity<Map<String, String>> generateReportBarcodePackage(@PathVariable Long reportId) {
        try {
            Optional<LabReport> reportOpt = labReportService.getReportById(reportId);
            if (reportOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            LabReport report = reportOpt.get();
            Visit visit = report.getVisit();
            String patientName = visit.getPatientDetails().get("name").asText();
            String patientId = visit.getPatientDetails().get("patientId").asText();

            Map<String, byte[]> barcodes = barcodeService.generateReportBarcodePackage(
                report.getUlrNumber(), patientName, patientId, report.getReportStatus().toString()
            );

            // Convert byte arrays to base64 for JSON response
            Map<String, String> base64Barcodes = new java.util.HashMap<>();
            barcodes.forEach((key, value) -> {
                String base64 = java.util.Base64.getEncoder().encodeToString(value);
                base64Barcodes.put(key, "data:image/png;base64," + base64);
            });

            return ResponseEntity.ok(base64Barcodes);

        } catch (WriterException | IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate QR code for sample
     * GET /barcodes/samples/{sampleNumber}/qr
     */
    @GetMapping("/samples/{sampleNumber}/qr")
    public ResponseEntity<byte[]> generateSampleQRCode(@PathVariable String sampleNumber,
                                                      @RequestParam(defaultValue = "200") int size) {
        try {
            Optional<Sample> sampleOpt = sampleService.getSampleByNumber(sampleNumber);
            if (sampleOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Sample sample = sampleOpt.get();
            String qrData = barcodeService.generateSampleQRData(
                sample.getSampleNumber(),
                sample.getSampleType().toString(),
                sample.getCollectedBy(),
                sample.getCollectedAt().format(DateTimeFormatter.ISO_LOCAL_DATE),
                sample.getStatus().toString(),
                "/samples/view/" + sample.getSampleNumber()
            );

            byte[] qrCode = barcodeService.generateQRCode(qrData, size);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("inline", "sample_qr_" + sampleNumber + ".png");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(qrCode);

        } catch (WriterException | IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate barcode for sample number
     * GET /barcodes/samples/{sampleNumber}/barcode
     */
    @GetMapping("/samples/{sampleNumber}/barcode")
    public ResponseEntity<byte[]> generateSampleBarcode(@PathVariable String sampleNumber) {
        try {
            Optional<Sample> sampleOpt = sampleService.getSampleByNumber(sampleNumber);
            if (sampleOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            byte[] barcode = barcodeService.generateSampleBarcode(sampleNumber);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("inline", "sample_barcode_" + sampleNumber + ".png");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(barcode);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate QR code for visit
     * GET /barcodes/visits/{visitId}/qr
     */
    @GetMapping("/visits/{visitId}/qr")
    public ResponseEntity<byte[]> generateVisitQRCode(@PathVariable Long visitId,
                                                     @RequestParam(defaultValue = "200") int size) {
        try {
            Optional<Visit> visitOpt = visitService.getVisitById(visitId);
            if (visitOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Visit visit = visitOpt.get();
            String patientName = visit.getPatientDetails().get("name").asText();
            String patientId = visit.getPatientDetails().get("patientId").asText();

            String qrData = barcodeService.generateVisitQRData(
                visit.getVisitId(), patientName, patientId,
                visit.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE),
                visit.getStatus().toString(),
                "/visits/view/" + visit.getVisitId()
            );

            byte[] qrCode = barcodeService.generateQRCode(qrData, size);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("inline", "visit_qr_" + visitId + ".png");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(qrCode);

        } catch (WriterException | IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate barcode for visit ID
     * GET /barcodes/visits/{visitId}/barcode
     */
    @GetMapping("/visits/{visitId}/barcode")
    public ResponseEntity<byte[]> generateVisitBarcode(@PathVariable Long visitId) {
        try {
            Optional<Visit> visitOpt = visitService.getVisitById(visitId);
            if (visitOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            byte[] barcode = barcodeService.generateVisitBarcode(visitId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("inline", "visit_barcode_" + visitId + ".png");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(barcode);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate custom QR code with provided data
     * POST /barcodes/qr/custom
     */
    @PostMapping("/qr/custom")
    public ResponseEntity<byte[]> generateCustomQRCode(@RequestBody Map<String, Object> request) {
        try {
            String data = (String) request.get("data");
            Integer size = (Integer) request.getOrDefault("size", 200);

            if (data == null || data.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            byte[] qrCode = barcodeService.generateQRCode(data, size);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("inline", "custom_qr.png");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(qrCode);

        } catch (WriterException | IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate custom barcode with provided data
     * POST /barcodes/barcode/custom
     */
    @PostMapping("/barcode/custom")
    public ResponseEntity<byte[]> generateCustomBarcode(@RequestBody Map<String, Object> request) {
        try {
            String data = (String) request.get("data");
            String format = (String) request.getOrDefault("format", "CODE128");
            Integer width = (Integer) request.getOrDefault("width", 300);
            Integer height = (Integer) request.getOrDefault("height", 50);

            if (data == null || data.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            byte[] barcode;
            if ("CODE39".equalsIgnoreCase(format)) {
                barcode = barcodeService.generateCode39Barcode(data, width, height);
            } else {
                barcode = barcodeService.generateCode128Barcode(data, width, height);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("inline", "custom_barcode.png");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(barcode);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
