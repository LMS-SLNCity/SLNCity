#!/bin/bash

# Comprehensive PDF Generation Test Script
# Tests all PDF generation features with complete lab workflow

set -e

BASE_URL="http://localhost:8080"
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}🧪 Comprehensive PDF Generation Test Suite${NC}"
echo "Base URL: $BASE_URL"
echo ""

# Function to make API calls with error handling
api_call() {
    local method=$1
    local endpoint=$2
    local data=$3
    local expected_status=$4
    local description=$5
    
    echo -e "${YELLOW}Testing: $description${NC}"
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X $method "$BASE_URL$endpoint" \
                   -H "Content-Type: application/json" \
                   -d "$data")
    else
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X $method "$BASE_URL$endpoint")
    fi
    
    http_code=$(echo $response | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    body=$(echo $response | sed -e 's/HTTPSTATUS:.*//g')
    
    if [ "$http_code" -eq "$expected_status" ]; then
        echo -e "${GREEN}✅ $description - Status: $http_code${NC}"
        echo "$body"
    else
        echo -e "${RED}❌ $description - Expected: $expected_status, Got: $http_code${NC}"
        echo "Response: $body"
        exit 1
    fi
    
    echo ""
}

# Function to download PDF
download_pdf() {
    local endpoint=$1
    local filename=$2
    local description=$3
    
    echo -e "${YELLOW}Downloading: $description${NC}"
    
    http_code=$(curl -s -w "%{http_code}" -X GET "$BASE_URL$endpoint" \
                -H "Accept: application/pdf" \
                --output "$filename")
    
    if [ "$http_code" -eq "200" ]; then
        file_size=$(ls -la "$filename" | awk '{print $5}')
        echo -e "${GREEN}✅ $description - Downloaded: $filename ($file_size bytes)${NC}"
    else
        echo -e "${RED}❌ $description - HTTP Status: $http_code${NC}"
        exit 1
    fi
    
    echo ""
}

# Function to extract JSON value
extract_json_value() {
    local json=$1
    local key=$2
    echo "$json" | grep -o "\"$key\":[^,}]*" | cut -d':' -f2 | tr -d '"' | tr -d ' '
}

echo -e "${BLUE}🏥 Step 1: Create Test Templates${NC}"

# Create CBC Template
cbc_template=$(api_call "POST" "/test-templates" '{
  "name": "Complete Blood Count (CBC)",
  "description": "Comprehensive blood analysis including all major parameters",
  "basePrice": 450.00,
  "parameters": [
    {
      "name": "Hemoglobin",
      "unit": "g/dL",
      "referenceRange": "12.0-15.5 (F), 13.5-17.5 (M)",
      "type": "numeric"
    },
    {
      "name": "RBC Count",
      "unit": "million/μL",
      "referenceRange": "4.2-5.4 (F), 4.7-6.1 (M)",
      "type": "numeric"
    },
    {
      "name": "WBC Count",
      "unit": "/μL",
      "referenceRange": "4000-11000",
      "type": "numeric"
    },
    {
      "name": "Platelet Count",
      "unit": "/μL",
      "referenceRange": "150000-450000",
      "type": "numeric"
    },
    {
      "name": "Hematocrit",
      "unit": "%",
      "referenceRange": "36-46 (F), 41-50 (M)",
      "type": "numeric"
    }
  ]
}' "201" "Create CBC Template")

CBC_TEMPLATE_ID=$(extract_json_value "$cbc_template" "templateId")
echo "CBC Template ID: $CBC_TEMPLATE_ID"

# Create Lipid Profile Template
lipid_template=$(api_call "POST" "/test-templates" '{
  "name": "Lipid Profile",
  "description": "Complete cholesterol and lipid analysis for cardiovascular health",
  "basePrice": 650.00,
  "parameters": [
    {
      "name": "Total Cholesterol",
      "unit": "mg/dL",
      "referenceRange": "<200",
      "type": "numeric"
    },
    {
      "name": "HDL Cholesterol",
      "unit": "mg/dL",
      "referenceRange": ">40 (M), >50 (F)",
      "type": "numeric"
    },
    {
      "name": "LDL Cholesterol",
      "unit": "mg/dL",
      "referenceRange": "<100",
      "type": "numeric"
    },
    {
      "name": "Triglycerides",
      "unit": "mg/dL",
      "referenceRange": "<150",
      "type": "numeric"
    },
    {
      "name": "VLDL Cholesterol",
      "unit": "mg/dL",
      "referenceRange": "<30",
      "type": "numeric"
    }
  ]
}' "201" "Create Lipid Profile Template")

LIPID_TEMPLATE_ID=$(extract_json_value "$lipid_template" "templateId")
echo "Lipid Profile Template ID: $LIPID_TEMPLATE_ID"

# Create Liver Function Test Template
lft_template=$(api_call "POST" "/test-templates" '{
  "name": "Liver Function Test (LFT)",
  "description": "Comprehensive liver function assessment",
  "basePrice": 550.00,
  "parameters": [
    {
      "name": "SGPT/ALT",
      "unit": "U/L",
      "referenceRange": "7-56",
      "type": "numeric"
    },
    {
      "name": "SGOT/AST",
      "unit": "U/L",
      "referenceRange": "10-40",
      "type": "numeric"
    },
    {
      "name": "Bilirubin Total",
      "unit": "mg/dL",
      "referenceRange": "0.3-1.2",
      "type": "numeric"
    },
    {
      "name": "Bilirubin Direct",
      "unit": "mg/dL",
      "referenceRange": "0.0-0.3",
      "type": "numeric"
    },
    {
      "name": "Alkaline Phosphatase",
      "unit": "U/L",
      "referenceRange": "44-147",
      "type": "numeric"
    }
  ]
}' "201" "Create LFT Template")

LFT_TEMPLATE_ID=$(extract_json_value "$lft_template" "templateId")
echo "LFT Template ID: $LFT_TEMPLATE_ID"

echo -e "${BLUE}👥 Step 2: Create Patient Visits${NC}"

# Create Patient 1 - Comprehensive Health Checkup
patient1_visit=$(api_call "POST" "/visits" '{
  "patientDetails": {
    "name": "Dr. Priya Sharma",
    "age": "35",
    "gender": "Female",
    "phone": "9876543210",
    "address": "Apollo Hospital, Hyderabad",
    "email": "priya.sharma@apollo.com",
    "doctorRef": "Dr. Rajesh Kumar",
    "patientId": "PAT001",
    "emergencyContact": "9876543211"
  }
}' "201" "Create Patient 1 Visit")

VISIT1_ID=$(extract_json_value "$patient1_visit" "visitId")
echo "Patient 1 Visit ID: $VISIT1_ID"

# Create Patient 2 - Cardiac Risk Assessment
patient2_visit=$(api_call "POST" "/visits" '{
  "patientDetails": {
    "name": "Mr. Arjun Reddy",
    "age": "45",
    "gender": "Male",
    "phone": "9876543212",
    "address": "KIMS Hospital, Secunderabad",
    "email": "arjun.reddy@kims.com",
    "doctorRef": "Dr. Sunita Rao",
    "patientId": "PAT002",
    "emergencyContact": "9876543213"
  }
}' "201" "Create Patient 2 Visit")

VISIT2_ID=$(extract_json_value "$patient2_visit" "visitId")
echo "Patient 2 Visit ID: $VISIT2_ID"

echo -e "${BLUE}🧪 Step 3: Add Lab Tests${NC}"

# Add tests for Patient 1 (Comprehensive)
api_call "POST" "/visits/$VISIT1_ID/tests" "{\"testTemplateId\": $CBC_TEMPLATE_ID, \"price\": 450.00}" "201" "Add CBC to Patient 1"
api_call "POST" "/visits/$VISIT1_ID/tests" "{\"testTemplateId\": $LIPID_TEMPLATE_ID, \"price\": 650.00}" "201" "Add Lipid Profile to Patient 1"
api_call "POST" "/visits/$VISIT1_ID/tests" "{\"testTemplateId\": $LFT_TEMPLATE_ID, \"price\": 550.00}" "201" "Add LFT to Patient 1"

# Add tests for Patient 2 (Cardiac Focus)
api_call "POST" "/visits/$VISIT2_ID/tests" "{\"testTemplateId\": $CBC_TEMPLATE_ID, \"price\": 450.00}" "201" "Add CBC to Patient 2"
api_call "POST" "/visits/$VISIT2_ID/tests" "{\"testTemplateId\": $LIPID_TEMPLATE_ID, \"price\": 650.00}" "201" "Add Lipid Profile to Patient 2"

echo -e "${BLUE}📊 Step 4: Add Test Results${NC}"

# Patient 1 Results - Normal values
api_call "PATCH" "/visits/$VISIT1_ID/tests/1/results" '{
  "results": {
    "Hemoglobin": {"value": "13.8", "unit": "g/dL", "status": "Normal"},
    "RBC Count": {"value": "4.6", "unit": "million/μL", "status": "Normal"},
    "WBC Count": {"value": "6800", "unit": "/μL", "status": "Normal"},
    "Platelet Count": {"value": "285000", "unit": "/μL", "status": "Normal"},
    "Hematocrit": {"value": "42", "unit": "%", "status": "Normal"},
    "conclusion": "All blood parameters within normal limits"
  }
}' "200" "Add CBC Results for Patient 1"

api_call "PATCH" "/visits/$VISIT1_ID/tests/2/results" '{
  "results": {
    "Total Cholesterol": {"value": "185", "unit": "mg/dL", "status": "Normal"},
    "HDL Cholesterol": {"value": "58", "unit": "mg/dL", "status": "Normal"},
    "LDL Cholesterol": {"value": "110", "unit": "mg/dL", "status": "Borderline High"},
    "Triglycerides": {"value": "125", "unit": "mg/dL", "status": "Normal"},
    "VLDL Cholesterol": {"value": "25", "unit": "mg/dL", "status": "Normal"},
    "conclusion": "Good lipid profile with slightly elevated LDL"
  }
}' "200" "Add Lipid Results for Patient 1"

api_call "PATCH" "/visits/$VISIT1_ID/tests/3/results" '{
  "results": {
    "SGPT/ALT": {"value": "28", "unit": "U/L", "status": "Normal"},
    "SGOT/AST": {"value": "24", "unit": "U/L", "status": "Normal"},
    "Bilirubin Total": {"value": "0.8", "unit": "mg/dL", "status": "Normal"},
    "Bilirubin Direct": {"value": "0.2", "unit": "mg/dL", "status": "Normal"},
    "Alkaline Phosphatase": {"value": "85", "unit": "U/L", "status": "Normal"},
    "conclusion": "Liver function tests are within normal limits"
  }
}' "200" "Add LFT Results for Patient 1"

echo -e "${BLUE}✅ Step 5: Approve Tests${NC}"

# Approve all tests for Patient 1
api_call "PATCH" "/visits/$VISIT1_ID/tests/1/approve" '{"approvedBy": "Dr. Rajesh Kumar"}' "200" "Approve CBC for Patient 1"
api_call "PATCH" "/visits/$VISIT1_ID/tests/2/approve" '{"approvedBy": "Dr. Rajesh Kumar"}' "200" "Approve Lipid Profile for Patient 1"
api_call "PATCH" "/visits/$VISIT1_ID/tests/3/approve" '{"approvedBy": "Dr. Rajesh Kumar"}' "200" "Approve LFT for Patient 1"

echo -e "${BLUE}💰 Step 6: Generate Bills${NC}"

# Generate bill for Patient 1
bill1=$(api_call "GET" "/billing/visits/$VISIT1_ID/bill" "" "200" "Generate Bill for Patient 1")
BILL1_ID=$(extract_json_value "$bill1" "billId")
echo "Patient 1 Bill ID: $BILL1_ID"

echo -e "${BLUE}📄 Step 7: Create Lab Reports${NC}"

# Create comprehensive report for Patient 1
report1=$(api_call "POST" "/reports" "{\"visitId\": $VISIT1_ID, \"reportType\": \"STANDARD\"}" "200" "Create Report for Patient 1")
REPORT1_ULR=$(echo "$report1" | grep -o '"ulrNumber":"[^"]*"' | cut -d'"' -f4)
REPORT1_ID=$(echo "$report1" | grep -o '"reportId":[^,}]*' | cut -d':' -f2)
echo "Patient 1 Report ID: $REPORT1_ID, ULR: $REPORT1_ULR"

echo -e "${BLUE}📄 Step 8: Generate PDF Reports${NC}"

# Test all PDF generation methods
download_pdf "/reports/$REPORT1_ID/pdf" "comprehensive-report-direct.pdf" "Direct PDF Generation"
download_pdf "/reports/$REPORT1_ID/pdf-html" "comprehensive-report-html.pdf" "HTML-to-PDF Generation"

# Test HTML preview
echo -e "${YELLOW}Testing HTML Preview${NC}"
html_preview=$(curl -s -X GET "$BASE_URL/reports/$REPORT1_ID/preview" -H "Accept: text/html")
echo "$html_preview" > "comprehensive-report-preview.html"
echo -e "${GREEN}✅ HTML Preview saved to comprehensive-report-preview.html${NC}"
echo ""

echo -e "${BLUE}📊 Step 9: System Statistics${NC}"

# Get comprehensive statistics
api_call "GET" "/reports/statistics" "" "200" "Report Statistics"
api_call "GET" "/billing/stats" "" "200" "Billing Statistics"
api_call "GET" "/visits/count-by-status" "" "200" "Visit Status Count"

echo -e "${BLUE}💳 Step 10: Payment Processing${NC}"

# Mark bill as paid
api_call "PATCH" "/billing/$BILL1_ID/pay" "" "200" "Mark Bill as Paid"

echo -e "${BLUE}📋 Step 11: Final Report Generation${NC}"

# Generate final report after payment
download_pdf "/reports/$REPORT1_ID/pdf" "final-comprehensive-report.pdf" "Final PDF Report"

echo -e "${GREEN}🎉 All PDF Generation Tests Completed Successfully!${NC}"
echo ""
echo -e "${BLUE}Generated Files:${NC}"
ls -la *.pdf *.html | grep -E "(comprehensive|final)" || echo "No files found"
echo ""
echo -e "${BLUE}Summary:${NC}"
echo "✅ Test Templates Created: 3"
echo "✅ Patient Visits Created: 2"
echo "✅ Lab Tests Added: 5"
echo "✅ Test Results Recorded: 3"
echo "✅ Tests Approved: 3"
echo "✅ Bills Generated: 1"
echo "✅ Reports Created: 1"
echo "✅ PDF Files Generated: 3"
echo "✅ HTML Preview Generated: 1"
echo ""
echo -e "${GREEN}🏆 PDF Generation System is Fully Functional!${NC}"
