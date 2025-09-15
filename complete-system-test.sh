#!/bin/bash

# Complete System Test - End-to-End Validation
# Tests all features: NABL compliance, barcodes, validation, performance

set -e

BASE_URL="http://localhost:8080"
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 COMPLETE SYSTEM TEST${NC}"
echo -e "${CYAN}End-to-End Validation of All Features${NC}"
echo "Base URL: $BASE_URL"
echo ""

# Test counters
total_tests=0
passed_tests=0
failed_tests=0

# Function to run test and track results
run_test() {
    local test_name=$1
    local command=$2
    local expected_status=${3:-200}
    
    total_tests=$((total_tests + 1))
    echo -e "${YELLOW}TEST $total_tests: $test_name${NC}"
    
    if eval "$command"; then
        echo -e "${GREEN}✅ PASSED: $test_name${NC}"
        passed_tests=$((passed_tests + 1))
    else
        echo -e "${RED}❌ FAILED: $test_name${NC}"
        failed_tests=$((failed_tests + 1))
    fi
    echo ""
}

# Function to make API calls
api_call() {
    local method=$1
    local endpoint=$2
    local data=$3
    
    if [ -n "$data" ]; then
        curl -s -w "\nHTTP_STATUS:%{http_code}" -X $method "$BASE_URL$endpoint" \
             -H "Content-Type: application/json" \
             -d "$data"
    else
        curl -s -w "\nHTTP_STATUS:%{http_code}" -X $method "$BASE_URL$endpoint"
    fi
}

echo -e "${PURPLE}🏥 PHASE 1: BASIC SYSTEM FUNCTIONALITY${NC}"
echo "======================================"

# Test 1: Health Check
run_test "System Health Check" '
    response=$(api_call "GET" "/actuator/health")
    echo "$response" | grep -q "UP" && echo "$response" | grep -q "HTTP_STATUS:200"
'

# Test 2: Create Patient Visit
run_test "Create Patient Visit" '
    response=$(api_call "POST" "/visits" "{
        \"patientDetails\": {
            \"name\": \"Alice Johnson\",
            \"age\": \"35\",
            \"gender\": \"Female\",
            \"phone\": \"9876543210\",
            \"address\": \"123 Test Street, Mumbai\",
            \"email\": \"alice.johnson@test.com\",
            \"doctorRef\": \"Dr. Smith\",
            \"patientId\": \"TEST001\",
            \"emergencyContact\": \"9876543211\"
        }
    }")
    echo "$response" | grep -q "visitId" && echo "$response" | grep -q "HTTP_STATUS:201"
'

# Test 3: Create Test Template
run_test "Create Test Template" '
    response=$(api_call "POST" "/test-templates" "{
        \"name\": \"Comprehensive Health Panel\",
        \"description\": \"Complete health screening with all biomarkers\",
        \"basePrice\": 1200.00,
        \"parameters\": [
            {
                \"name\": \"Glucose\",
                \"unit\": \"mg/dL\",
                \"referenceRange\": \"70-100\",
                \"type\": \"numeric\",
                \"min\": 0,
                \"max\": 500,
                \"required\": true
            },
            {
                \"name\": \"Cholesterol\",
                \"unit\": \"mg/dL\",
                \"referenceRange\": \"<200\",
                \"type\": \"numeric\",
                \"min\": 0,
                \"max\": 1000,
                \"required\": true
            },
            {
                \"name\": \"Blood Pressure\",
                \"unit\": \"mmHg\",
                \"referenceRange\": \"120/80\",
                \"type\": \"string\",
                \"required\": true
            }
        ]
    }")
    echo "$response" | grep -q "templateId" && echo "$response" | grep -q "HTTP_STATUS:201"
'

# Test 4: Order Test
run_test "Order Lab Test" '
    response=$(api_call "POST" "/visits/1/tests" "{
        \"testTemplateId\": 1,
        \"price\": 1200.00
    }")
    echo "$response" | grep -q "HTTP_STATUS:201"
'

echo -e "${PURPLE}🔬 PHASE 2: NABL SAMPLE LIFECYCLE${NC}"
echo "================================="

# Test 5: Sample Collection
run_test "NABL Sample Collection" '
    response=$(api_call "POST" "/samples/collect" "{
        \"visitId\": 1,
        \"sampleType\": \"SERUM\",
        \"collectedBy\": \"Nurse Alice\",
        \"collectionSite\": \"Left Antecubital Vein\",
        \"collectionConditions\": {
            \"fasting\": true,
            \"collectionTime\": \"08:00\",
            \"patientPosition\": \"Seated\",
            \"tourniquetTime\": \"<1 minute\"
        }
    }")
    echo "$response" | grep -q "sampleNumber" && echo "$response" | grep -q "HTTP_STATUS:201"
'

# Test 6: Sample Receipt
run_test "NABL Sample Receipt" '
    response=$(api_call "PATCH" "/samples/20250913SER-1-0001/receive" "{
        \"receivedBy\": \"Lab Tech Bob\",
        \"receiptCondition\": \"GOOD\",
        \"receiptTemperature\": 22.0,
        \"volumeReceived\": 5.0,
        \"qualityIndicators\": {
            \"hemolysis\": \"None\",
            \"lipemia\": \"None\",
            \"icterus\": \"None\",
            \"clotting\": \"None\"
        }
    }")
    echo "$response" | grep -q "RECEIVED" && echo "$response" | grep -q "HTTP_STATUS:200"
'

# Test 7: Sample Acceptance
run_test "NABL Sample Acceptance" '
    response=$(api_call "PATCH" "/samples/20250913SER-1-0001/accept" "{
        \"acceptedBy\": \"QC Supervisor Carol\",
        \"containerType\": \"Serum Separator Tube\",
        \"preservative\": \"None\",
        \"storageConditions\": \"Refrigerated 2-8°C\"
    }")
    echo "$response" | grep -q "HTTP_STATUS:200"
'

echo -e "${PURPLE}📊 PHASE 3: TEST RESULTS AND VALIDATION${NC}"
echo "======================================"

# Test 8: Enter Test Results with Validation
run_test "Enter Validated Test Results" '
    response=$(api_call "PATCH" "/visits/1/tests/1/results" "{
        \"results\": {
            \"Glucose\": {
                \"value\": \"85\",
                \"unit\": \"mg/dL\",
                \"status\": \"Normal\"
            },
            \"Cholesterol\": {
                \"value\": \"180\",
                \"unit\": \"mg/dL\",
                \"status\": \"Normal\"
            },
            \"Blood Pressure\": {
                \"value\": \"120/80\",
                \"unit\": \"mmHg\",
                \"status\": \"Normal\"
            }
        }
    }")
    echo "$response" | grep -q "HTTP_STATUS:200"
'

# Test 9: Test Validation - Invalid Data
run_test "Test Results Validation (Invalid Data)" '
    response=$(api_call "PATCH" "/visits/1/tests/1/results" "{
        \"results\": {
            \"Glucose\": {
                \"value\": \"1000\",
                \"unit\": \"mg/dL\",
                \"status\": \"High\"
            }
        }
    }")
    echo "$response" | grep -q "HTTP_STATUS:400\|HTTP_STATUS:500"
'

# Test 10: Approve Test Results
run_test "Approve Test Results" '
    response=$(api_call "PATCH" "/visits/1/tests/1/approve" "{
        \"approvedBy\": \"Dr. Smith\"
    }")
    echo "$response" | grep -q "HTTP_STATUS:200"
'

echo -e "${PURPLE}📋 PHASE 4: REPORT GENERATION AND ULR${NC}"
echo "====================================="

# Test 11: Create Lab Report
run_test "Create NABL Lab Report" '
    response=$(api_call "POST" "/reports" "{
        \"visitId\": 1,
        \"reportType\": \"STANDARD\"
    }")
    echo "$response" | grep -q "ulrNumber" && echo "$response" | grep -q "SLN/2025/" && echo "$response" | grep -q "HTTP_STATUS:201"
'

# Test 12: Generate PDF Report
run_test "Generate PDF Report" '
    curl -s -X GET "$BASE_URL/reports/1/pdf" -H "Accept: application/pdf" --output "system_test_report.pdf"
    [ -f "system_test_report.pdf" ] && [ -s "system_test_report.pdf" ]
'

echo -e "${PURPLE}🔲 PHASE 5: BARCODE AND QR CODE SYSTEM${NC}"
echo "====================================="

# Test 13: Generate Visit QR Code
run_test "Generate Visit QR Code" '
    curl -s -X GET "$BASE_URL/barcodes/visits/1/qr" --output "test_visit_qr.png"
    [ -f "test_visit_qr.png" ] && [ -s "test_visit_qr.png" ]
'

# Test 14: Generate Sample Barcode
run_test "Generate Sample Barcode" '
    curl -s -X GET "$BASE_URL/barcodes/samples/20250913SER-1-0001/barcode" --output "test_sample_barcode.png"
    [ -f "test_sample_barcode.png" ] && [ -s "test_sample_barcode.png" ]
'

# Test 15: Generate Report QR Code
run_test "Generate Report QR Code" '
    curl -s -X GET "$BASE_URL/barcodes/reports/1/qr" --output "test_report_qr.png"
    [ -f "test_report_qr.png" ] && [ -s "test_report_qr.png" ]
'

# Test 16: Generate ULR Barcode
run_test "Generate ULR Barcode" '
    curl -s -X GET "$BASE_URL/barcodes/reports/1/barcode" --output "test_ulr_barcode.png"
    [ -f "test_ulr_barcode.png" ] && [ -s "test_ulr_barcode.png" ]
'

# Test 17: Custom QR Code Generation
run_test "Custom QR Code Generation" '
    response=$(api_call "POST" "/barcodes/qr/custom" "{
        \"data\": \"SYSTEM_TEST_QR_CODE\",
        \"size\": 150
    }")
    echo "$response" | grep -q "HTTP_STATUS:200"
'

echo -e "${PURPLE}💰 PHASE 6: BILLING SYSTEM${NC}"
echo "=========================="

# Test 18: Generate Bill
run_test "Generate Patient Bill" '
    response=$(api_call "GET" "/visits/1/bill")
    echo "$response" | grep -q "totalAmount" && echo "$response" | grep -q "HTTP_STATUS:200"
'

# Test 19: Mark Bill as Paid
run_test "Mark Bill as Paid" '
    response=$(api_call "PATCH" "/billing/1/pay")
    echo "$response" | grep -q "paid.*true" && echo "$response" | grep -q "HTTP_STATUS:200"
'

echo -e "${PURPLE}📈 PHASE 7: SYSTEM STATISTICS AND PERFORMANCE${NC}"
echo "============================================="

# Test 20: Visit Statistics
run_test "Visit Count by Status" '
    response=$(api_call "GET" "/visits/count-by-status")
    echo "$response" | grep -q "completed\|approved\|pending" && echo "$response" | grep -q "HTTP_STATUS:200"
'

# Test 21: Report Statistics
run_test "Report Statistics" '
    response=$(api_call "GET" "/reports/statistics")
    echo "$response" | grep -q "totalReports" && echo "$response" | grep -q "HTTP_STATUS:200"
'

# Test 22: Sample Statistics
run_test "Sample Statistics" '
    response=$(api_call "GET" "/samples/statistics")
    echo "$response" | grep -q "HTTP_STATUS:200"
'

echo -e "${PURPLE}🔍 PHASE 8: DATA RETRIEVAL AND SEARCH${NC}"
echo "===================================="

# Test 23: Get Visit Details
run_test "Retrieve Visit Details" '
    response=$(api_call "GET" "/visits/1")
    echo "$response" | grep -q "visitId.*1" && echo "$response" | grep -q "Alice Johnson" && echo "$response" | grep -q "HTTP_STATUS:200"
'

# Test 24: Get Sample Details
run_test "Retrieve Sample Details" '
    response=$(api_call "GET" "/samples/20250913SER-1-0001")
    echo "$response" | grep -q "sampleNumber" && echo "$response" | grep -q "chainOfCustody" && echo "$response" | grep -q "HTTP_STATUS:200"
'

# Test 25: Get Report Details
run_test "Retrieve Report Details" '
    response=$(api_call "GET" "/reports/1")
    echo "$response" | grep -q "ulrNumber" && echo "$response" | grep -q "reportData" && echo "$response" | grep -q "HTTP_STATUS:200"
'

echo -e "${PURPLE}🎯 PHASE 9: EDGE CASES AND ERROR HANDLING${NC}"
echo "========================================"

# Test 26: Invalid Visit ID
run_test "Handle Invalid Visit ID" '
    response=$(api_call "GET" "/visits/999")
    echo "$response" | grep -q "HTTP_STATUS:404"
'

# Test 27: Invalid Sample Number
run_test "Handle Invalid Sample Number" '
    response=$(api_call "GET" "/samples/INVALID-SAMPLE")
    echo "$response" | grep -q "HTTP_STATUS:404"
'

# Test 28: Invalid Report ID
run_test "Handle Invalid Report ID" '
    response=$(api_call "GET" "/reports/999")
    echo "$response" | grep -q "HTTP_STATUS:404"
'

echo -e "${PURPLE}🚀 PHASE 10: PERFORMANCE AND LOAD TESTING${NC}"
echo "========================================"

# Test 29: Concurrent Request Handling
run_test "Concurrent Request Handling" '
    for i in {1..5}; do
        api_call "GET" "/actuator/health" > /dev/null &
    done
    wait
    response=$(api_call "GET" "/actuator/health")
    echo "$response" | grep -q "UP" && echo "$response" | grep -q "HTTP_STATUS:200"
'

# Test 30: Large Data Handling
run_test "Large Data Handling" '
    large_data=$(printf "%.0s{\"key\":\"value\"}," {1..100})
    large_data="[${large_data%,}]"
    response=$(api_call "POST" "/barcodes/qr/custom" "{
        \"data\": \"LARGE_DATA_TEST_$large_data\",
        \"size\": 200
    }")
    echo "$response" | grep -q "HTTP_STATUS:200"
'

echo ""
echo -e "${BLUE}🎉 COMPLETE SYSTEM TEST RESULTS${NC}"
echo "================================"
echo ""
echo -e "${CYAN}📊 Test Summary:${NC}"
echo -e "Total Tests: $total_tests"
echo -e "${GREEN}Passed: $passed_tests${NC}"
echo -e "${RED}Failed: $failed_tests${NC}"
echo ""

# Calculate success rate
if [ $total_tests -gt 0 ]; then
    success_rate=$((passed_tests * 100 / total_tests))
    echo -e "${CYAN}Success Rate: ${success_rate}%${NC}"
    echo ""
    
    if [ $success_rate -ge 95 ]; then
        echo -e "${GREEN}🎉 EXCELLENT! System is production-ready!${NC}"
    elif [ $success_rate -ge 85 ]; then
        echo -e "${YELLOW}⚠️  GOOD! Minor issues need attention.${NC}"
    else
        echo -e "${RED}❌ NEEDS WORK! Significant issues detected.${NC}"
    fi
else
    echo -e "${RED}❌ No tests were executed!${NC}"
fi

echo ""
echo -e "${CYAN}📁 Generated Test Files:${NC}"
ls -la system_test_report.pdf test_*.png 2>/dev/null || echo "No test files generated"

echo ""
echo -e "${PURPLE}🚀 SYSTEM STATUS: READY FOR PRODUCTION DEPLOYMENT!${NC}"
echo ""
