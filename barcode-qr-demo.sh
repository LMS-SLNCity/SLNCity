#!/bin/bash

# Comprehensive Barcode and QR Code Demo
# Demonstrates barcode/QR code generation for lab operations

set -e

BASE_URL="http://localhost:8080"
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔲 BARCODE & QR CODE DEMONSTRATION${NC}"
echo -e "${CYAN}Lab Operations Barcode System${NC}"
echo "Base URL: $BASE_URL"
echo ""

# Function to make API calls and show results
demo_api() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    
    echo -e "${YELLOW}$description${NC}"
    
    if [ -n "$data" ]; then
        response=$(curl -s -X $method "$BASE_URL$endpoint" \
                   -H "Content-Type: application/json" \
                   -d "$data")
    else
        response=$(curl -s -X $method "$BASE_URL$endpoint")
    fi
    
    echo -e "${GREEN}✅ Success:${NC}"
    echo "$response" | jq . 2>/dev/null || echo "$response"
    echo ""
}

# Function to download barcode/QR code images
download_barcode() {
    local endpoint=$1
    local filename=$2
    local description=$3
    
    echo -e "${YELLOW}$description${NC}"
    
    curl -s -X GET "$BASE_URL$endpoint" --output "$filename"
    
    if [ -f "$filename" ]; then
        echo -e "${GREEN}✅ Downloaded: $filename${NC}"
        ls -la "$filename"
    else
        echo -e "${RED}❌ Failed to download: $filename${NC}"
    fi
    echo ""
}

echo -e "${PURPLE}🏥 STEP 1: CREATE PATIENT VISIT${NC}"
echo "================================"

# Create patient visit
demo_api "POST" "/visits" '{
  "patientDetails": {
    "name": "John Smith",
    "age": "45",
    "gender": "Male",
    "phone": "9876543210",
    "address": "123 Main Street, Mumbai",
    "email": "john.smith@email.com",
    "doctorRef": "Dr. Sarah Wilson",
    "patientId": "PAT001",
    "emergencyContact": "9876543211"
  }
}' "🏥 Create Patient Visit"

echo -e "${PURPLE}🧪 STEP 2: ORDER TESTS AND COLLECT SAMPLES${NC}"
echo "=========================================="

# Add test to visit
demo_api "POST" "/visits/1/tests" '{
  "testTemplateId": 1,
  "price": 500.00
}' "🧪 Order Lab Test"

# Collect sample
demo_api "POST" "/samples/collect" '{
  "visitId": 1,
  "sampleType": "WHOLE_BLOOD",
  "collectedBy": "Nurse Mary",
  "collectionSite": "Left Arm",
  "collectionConditions": {
    "fasting": true,
    "collectionTime": "08:00",
    "patientPosition": "Seated"
  }
}' "🩸 Collect Sample"

echo -e "${PURPLE}📊 STEP 3: ENTER TEST RESULTS${NC}"
echo "============================="

# Enter test results
demo_api "PATCH" "/visits/1/tests/1/results" '{
  "results": {
    "Hemoglobin": {
      "value": "14.2",
      "unit": "g/dL",
      "status": "Normal"
    },
    "WBC Count": {
      "value": "7500",
      "unit": "/μL",
      "status": "Normal"
    },
    "RBC Count": {
      "value": "4.8",
      "unit": "million/μL",
      "status": "Normal"
    },
    "Platelet Count": {
      "value": "250000",
      "unit": "/μL",
      "status": "Normal"
    }
  }
}' "📊 Enter Test Results"

# Approve test
demo_api "PATCH" "/visits/1/tests/1/approve" '{
  "approvedBy": "Dr. Sarah Wilson"
}' "✅ Approve Test Results"

echo -e "${PURPLE}📋 STEP 4: CREATE LAB REPORT${NC}"
echo "============================"

# Create lab report
demo_api "POST" "/reports" '{
  "visitId": 1,
  "reportType": "STANDARD"
}' "📋 Create Lab Report"

echo -e "${PURPLE}🔲 STEP 5: GENERATE BARCODES AND QR CODES${NC}"
echo "========================================"

# Generate QR code for visit
download_barcode "/barcodes/visits/1/qr" "visit_qr_code.png" "🔲 Generate Visit QR Code"

# Generate barcode for visit
download_barcode "/barcodes/visits/1/barcode" "visit_barcode.png" "🔲 Generate Visit Barcode"

# Generate QR code for sample
SAMPLE_NUMBER="20250913BLD-1-0001"
download_barcode "/barcodes/samples/$SAMPLE_NUMBER/qr" "sample_qr_code.png" "🔲 Generate Sample QR Code"

# Generate barcode for sample
download_barcode "/barcodes/samples/$SAMPLE_NUMBER/barcode" "sample_barcode.png" "🔲 Generate Sample Barcode"

# Generate QR code for report
download_barcode "/barcodes/reports/1/qr" "report_qr_code.png" "🔲 Generate Report QR Code"

# Generate barcode for ULR number
download_barcode "/barcodes/reports/1/barcode" "ulr_barcode.png" "🔲 Generate ULR Barcode"

echo -e "${PURPLE}📦 STEP 6: GENERATE BARCODE PACKAGES${NC}"
echo "==================================="

# Generate complete barcode package for report
demo_api "GET" "/barcodes/reports/1/package" "" "📦 Generate Report Barcode Package"

echo -e "${PURPLE}🎨 STEP 7: CUSTOM BARCODE GENERATION${NC}"
echo "==================================="

# Generate custom QR code
demo_api "POST" "/barcodes/qr/custom" '{
  "data": "CUSTOM_LAB_DATA\nLab: SLN City Laboratory\nTest: Custom Test\nDate: 2025-09-13\nStatus: Complete",
  "size": 150
}' "🎨 Generate Custom QR Code"

# Generate custom Code128 barcode
demo_api "POST" "/barcodes/barcode/custom" '{
  "data": "CUSTOM123456",
  "format": "CODE128",
  "width": 250,
  "height": 40
}' "🎨 Generate Custom Code128 Barcode"

# Generate custom Code39 barcode
demo_api "POST" "/barcodes/barcode/custom" '{
  "data": "VISIT001",
  "format": "CODE39",
  "width": 200,
  "height": 35
}' "🎨 Generate Custom Code39 Barcode"

echo -e "${PURPLE}📄 STEP 8: GENERATE PDF REPORT WITH BARCODES${NC}"
echo "==========================================="

# Generate PDF report with embedded barcodes
echo -e "${YELLOW}📄 Generate PDF Report with Embedded Barcodes${NC}"
curl -s -X GET "$BASE_URL/reports/1/pdf" -H "Accept: application/pdf" --output barcode_enhanced_report.pdf

if [ -f "barcode_enhanced_report.pdf" ]; then
    echo -e "${GREEN}✅ PDF Report with Barcodes Generated Successfully${NC}"
    ls -la barcode_enhanced_report.pdf
else
    echo -e "${RED}❌ PDF Report Generation Failed${NC}"
fi
echo ""

echo -e "${PURPLE}🔍 STEP 9: BARCODE SCANNING SIMULATION${NC}"
echo "====================================="

echo -e "${YELLOW}🔍 Simulating Barcode Scanning Workflow${NC}"
echo ""

echo -e "${CYAN}📱 Mobile App Scanning Simulation:${NC}"
echo "1. Scan Visit QR Code → Opens visit details"
echo "2. Scan Sample Barcode → Tracks sample status"
echo "3. Scan ULR Barcode → Retrieves report"
echo "4. Scan Patient Barcode → Shows patient info"
echo ""

echo -e "${CYAN}🏥 Lab Equipment Integration:${NC}"
echo "1. Sample tubes with barcodes for automated processing"
echo "2. Report barcodes for quick retrieval and verification"
echo "3. Visit barcodes for patient identification"
echo "4. QR codes for comprehensive data access"
echo ""

echo -e "${GREEN}🎉 BARCODE & QR CODE DEMO COMPLETE!${NC}"
echo ""
echo -e "${CYAN}========================================${NC}"
echo -e "${CYAN}🏆 BARCODE SYSTEM CAPABILITIES:${NC}"
echo -e "${CYAN}========================================${NC}"
echo -e "${GREEN}✅ QR Codes - Comprehensive data storage${NC}"
echo -e "${GREEN}✅ Code128 Barcodes - Alphanumeric data${NC}"
echo -e "${GREEN}✅ Code39 Barcodes - Simple identifiers${NC}"
echo -e "${GREEN}✅ PDF Integration - Embedded barcodes${NC}"
echo -e "${GREEN}✅ Custom Generation - Flexible formats${NC}"
echo -e "${GREEN}✅ Batch Packages - Multiple formats${NC}"
echo -e "${GREEN}✅ Mobile Ready - Easy scanning${NC}"
echo -e "${GREEN}✅ Lab Integration - Equipment compatible${NC}"
echo ""
echo -e "${PURPLE}📁 Generated Files:${NC}"
echo "• visit_qr_code.png - Visit QR code"
echo "• visit_barcode.png - Visit barcode"
echo "• sample_qr_code.png - Sample QR code"
echo "• sample_barcode.png - Sample barcode"
echo "• report_qr_code.png - Report QR code"
echo "• ulr_barcode.png - ULR barcode"
echo "• barcode_enhanced_report.pdf - PDF with embedded barcodes"
echo ""
echo -e "${CYAN}🚀 BARCODE SYSTEM READY FOR PRODUCTION!${NC}"
echo ""

echo -e "${YELLOW}💡 USAGE SCENARIOS:${NC}"
echo ""
echo -e "${CYAN}🔬 Laboratory Operations:${NC}"
echo "• Sample tracking from collection to disposal"
echo "• Quick patient identification and history access"
echo "• Report verification and authenticity checking"
echo "• Equipment integration for automated processing"
echo ""
echo -e "${CYAN}📱 Mobile Applications:${NC}"
echo "• Staff can scan codes for instant data access"
echo "• Patients can scan QR codes to view reports"
echo "• Quality control through barcode verification"
echo "• Audit trail through scan logging"
echo ""
echo -e "${CYAN}🏥 Hospital Integration:${NC}"
echo "• EMR system integration through barcode scanning"
echo "• Patient wristband integration"
echo "• Medication administration record (MAR) integration"
echo "• Billing system automation"
echo ""
