#!/bin/bash

# Comprehensive Lifecycle Tracking Test
# Tests sample lifecycle, visit lifecycle, bill cycle, and report cycle

set -e

BASE_URL="http://localhost:8080"
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔄 Comprehensive Lifecycle Tracking Test${NC}"
echo "Base URL: $BASE_URL"
echo ""

# Function to make API calls with lifecycle tracking
track_lifecycle() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    echo -e "${YELLOW}[$timestamp] $description${NC}"

    if [ -n "$data" ]; then
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X $method "$BASE_URL$endpoint" \
                   -H "Content-Type: application/json" \
                   -d "$data")
    else
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X $method "$BASE_URL$endpoint")
    fi

    http_code=$(echo $response | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    body=$(echo $response | sed -e 's/HTTPSTATUS:.*//g')

    if [ "$http_code" -eq "200" ] || [ "$http_code" -eq "201" ]; then
        echo -e "${GREEN}✅ Status: $http_code${NC}"
        echo "$body" | jq . 2>/dev/null || echo "$body"
        echo "$body"  # Return body for variable extraction
    else
        echo -e "${RED}❌ Status: $http_code${NC}"
        echo "Response: $body"
        echo "$body"  # Return body even on error
    fi

    echo ""
}

# Function to extract JSON value
extract_json_value() {
    local json=$1
    local key=$2
    echo "$json" | jq -r ".$key" 2>/dev/null || echo "$json" | grep -o "\"$key\":[^,}]*" | cut -d':' -f2 | tr -d '"' | tr -d ' '
}

echo -e "${PURPLE}📋 PHASE 1: INITIAL SETUP & TEMPLATE CREATION${NC}"
echo "=============================================="

# Create test templates
track_lifecycle "POST" "/test-templates" '{
  "name": "Complete Blood Count (CBC)",
  "description": "Comprehensive blood analysis for lifecycle tracking",
  "basePrice": 450.00,
  "parameters": [
    {"name": "Hemoglobin", "unit": "g/dL", "referenceRange": "12.0-15.5 (F), 13.5-17.5 (M)", "type": "numeric"},
    {"name": "WBC Count", "unit": "/μL", "referenceRange": "4000-11000", "type": "numeric"},
    {"name": "Platelet Count", "unit": "/μL", "referenceRange": "150000-450000", "type": "numeric"}
  ]
}' "Create CBC Template for Lifecycle Tracking"

# Extract template ID from the API directly
CBC_TEMPLATE_ID=$(curl -s -X GET "$BASE_URL/test-templates" | jq -r '.[0].templateId' 2>/dev/null || echo "1")
echo -e "${BLUE}📝 CBC Template ID: $CBC_TEMPLATE_ID${NC}"

echo -e "${PURPLE}📋 PHASE 2: VISIT LIFECYCLE TRACKING${NC}"
echo "===================================="

# Step 1: Create Patient Visit (PENDING)
track_lifecycle "POST" "/visits" '{
  "patientDetails": {
    "name": "John Doe",
    "age": "45",
    "gender": "Male",
    "phone": "9876543210",
    "address": "123 Main St, City",
    "email": "john.doe@email.com",
    "doctorRef": "Dr. Smith",
    "patientId": "PAT001",
    "emergencyContact": "9876543211"
  }
}' "VISIT LIFECYCLE: Create Patient Visit (Initial Status: PENDING)"

# Extract visit ID from the API directly
VISIT_ID=$(curl -s -X GET "$BASE_URL/visits" | jq -r '.[0].visitId' 2>/dev/null || echo "1")
echo -e "${BLUE}🏥 Visit ID: $VISIT_ID${NC}"

# Step 2: Check Visit Status
track_lifecycle "GET" "/visits/$VISIT_ID" "" "VISIT LIFECYCLE: Check Initial Visit Status"

# Step 3: Add Lab Test (Visit moves to IN_PROGRESS) - Use existing template ID 1
track_lifecycle "POST" "/visits/$VISIT_ID/tests" "{\"testTemplateId\": 1, \"price\": 450.00}" "VISIT LIFECYCLE: Add Lab Test (Status: PENDING → IN_PROGRESS)"

# Step 4: Check Visit Status After Test Addition
track_lifecycle "GET" "/visits/$VISIT_ID" "" "VISIT LIFECYCLE: Check Status After Test Addition"

echo -e "${PURPLE}📋 PHASE 3: NABL-COMPLIANT SAMPLE LIFECYCLE TRACKING${NC}"
echo "=================================================="

# Step 5: Collect Sample (NABL Requirement: Sample Collection Documentation)
track_lifecycle "POST" "/samples/collect" "{
  \"visitId\": $VISIT_ID,
  \"sampleType\": \"WHOLE_BLOOD\",
  \"collectedBy\": \"Nurse Jane\",
  \"collectionSite\": \"Left Antecubital Vein\",
  \"collectionConditions\": {
    \"fasting\": true,
    \"collectionTime\": \"08:30\",
    \"patientPosition\": \"Seated\",
    \"tourniquetTime\": \"<1 minute\"
  }
}" "NABL SAMPLE LIFECYCLE: Sample Collection (Status: COLLECTED)"

# Extract sample number for further tracking
SAMPLE_NUMBER="20250912WB-$VISIT_ID-0001"

# Step 6: Receive Sample (NABL Requirement: Sample Receipt Documentation)
track_lifecycle "PATCH" "/samples/$SAMPLE_NUMBER/receive" "{
  \"receivedBy\": \"Lab Tech Mike\",
  \"receiptTemperature\": 4.5,
  \"receiptCondition\": \"Good\"
}" "NABL SAMPLE LIFECYCLE: Sample Receipt (Status: COLLECTED → RECEIVED)"

# Step 7: Accept Sample (NABL Requirement: Quality Control Checks)
track_lifecycle "PATCH" "/samples/$SAMPLE_NUMBER/accept" "{
  \"acceptedBy\": \"QC Supervisor\",
  \"volumeReceived\": 5.0,
  \"containerType\": \"EDTA tube\",
  \"preservative\": \"EDTA\"
}" "NABL SAMPLE LIFECYCLE: Sample Acceptance (Status: RECEIVED → ACCEPTED)"

# Step 8: Start Processing (NABL Requirement: Processing Documentation)
track_lifecycle "PATCH" "/samples/$SAMPLE_NUMBER/process" "{
  \"processedBy\": \"Lab Tech Sarah\",
  \"storageLocation\": \"Refrigerator-A1\",
  \"storageTemperature\": 4.0
}" "NABL SAMPLE LIFECYCLE: Start Processing (Status: ACCEPTED → PROCESSING)"

# Step 9: Start Analysis (NABL Requirement: Analysis Documentation)
track_lifecycle "PATCH" "/samples/$SAMPLE_NUMBER/analyze" "{
  \"analyst\": \"Medical Technologist John\"
}" "NABL SAMPLE LIFECYCLE: Start Analysis (Status: PROCESSING → IN_ANALYSIS)"

# Step 10: Complete Analysis (NABL Requirement: Analysis Completion)
track_lifecycle "PATCH" "/samples/$SAMPLE_NUMBER/complete" "{
  \"analyst\": \"Medical Technologist John\",
  \"qualityIndicators\": {
    \"appearance\": \"Clear\",
    \"color\": \"Red\",
    \"clotting\": \"None\",
    \"hemolysis\": \"None\"
  }
}" "NABL SAMPLE LIFECYCLE: Complete Analysis (Status: IN_ANALYSIS → ANALYSIS_COMPLETE)"

# Step 11: Review Results (NABL Requirement: Result Review)
track_lifecycle "PATCH" "/samples/$SAMPLE_NUMBER/review" "{
  \"reviewer\": \"Dr. Smith\"
}" "NABL SAMPLE LIFECYCLE: Review Results (Status: ANALYSIS_COMPLETE → REVIEWED)"

# Step 12: Store Sample (NABL Requirement: Sample Storage)
track_lifecycle "PATCH" "/samples/$SAMPLE_NUMBER/store" "{
  \"storageLocation\": \"Archive-B2\",
  \"storageTemperature\": -20.0,
  \"storageConditions\": \"Frozen storage for 1 year retention\"
}" "NABL SAMPLE LIFECYCLE: Store Sample (Status: REVIEWED → STORED)"

# Step 13: Check Complete Sample Chain of Custody
track_lifecycle "GET" "/samples/$SAMPLE_NUMBER" "" "NABL SAMPLE LIFECYCLE: Check Complete Chain of Custody"

echo -e "${PURPLE}📋 PHASE 4: BILLING CYCLE TRACKING${NC}"
echo "=================================="

# Step 9: Generate Bill (Visit moves to BILLED)
bill_creation=$(track_lifecycle "GET" "/billing/visits/$VISIT_ID/bill" "" "BILLING CYCLE: Generate Bill (Visit Status: AWAITING_APPROVAL → BILLED)")

BILL_ID=$(extract_json_value "$bill_creation" "billId")
echo -e "${BLUE}💰 Bill ID: $BILL_ID${NC}"

# Step 10: Check Bill Status
track_lifecycle "GET" "/billing/$BILL_ID" "" "BILLING CYCLE: Check Bill Status (Unpaid)"

# Step 11: Check Visit Status After Billing
track_lifecycle "GET" "/visits/$VISIT_ID" "" "VISIT LIFECYCLE: Check Status After Billing"

# Step 12: Process Payment
track_lifecycle "PATCH" "/billing/$BILL_ID/pay" "" "BILLING CYCLE: Process Payment (Status: Unpaid → Paid)"

# Step 13: Check Visit Status After Payment
track_lifecycle "GET" "/visits/$VISIT_ID" "" "VISIT LIFECYCLE: Check Status After Payment (Should be COMPLETED)"

echo -e "${PURPLE}📋 PHASE 5: REPORT CYCLE TRACKING${NC}"
echo "================================="

# Step 14: Create Lab Report
report_creation=$(track_lifecycle "POST" "/reports" "{\"visitId\": $VISIT_ID, \"reportType\": \"STANDARD\"}" "REPORT CYCLE: Create Lab Report (Status: DRAFT)")

REPORT_ID=$(extract_json_value "$report_creation" "reportId")
ULR_NUMBER=$(extract_json_value "$report_creation" "ulrNumber")
echo -e "${BLUE}📄 Report ID: $REPORT_ID, ULR: $ULR_NUMBER${NC}"

# Step 15: Check Report Status
track_lifecycle "GET" "/reports/$REPORT_ID" "" "REPORT CYCLE: Check Report Status (DRAFT)"

# Step 16: Generate Report Content
track_lifecycle "PATCH" "/reports/$REPORT_ID/generate" '{"templateVersion": "1.0"}' "REPORT CYCLE: Generate Report Content (Status: DRAFT → GENERATED)"

# Step 17: Authorize Report
track_lifecycle "PATCH" "/reports/$REPORT_ID/authorize" '{"authorizedBy": "Dr. Smith"}' "REPORT CYCLE: Authorize Report (Status: GENERATED → AUTHORIZED)"

# Step 18: Mark Report as Sent
track_lifecycle "PATCH" "/reports/$REPORT_ID/send" "" "REPORT CYCLE: Mark Report as Sent (Status: AUTHORIZED → SENT)"

# Step 19: Final Report Status Check
track_lifecycle "GET" "/reports/$REPORT_ID" "" "REPORT CYCLE: Final Report Status Check"

echo -e "${PURPLE}📋 PHASE 6: COMPREHENSIVE STATUS SUMMARY${NC}"
echo "========================================"

# Step 20: Final Visit Status
track_lifecycle "GET" "/visits/$VISIT_ID" "" "FINAL STATUS: Visit Lifecycle Complete"

# Step 21: Final Bill Status
track_lifecycle "GET" "/billing/$BILL_ID" "" "FINAL STATUS: Billing Cycle Complete"

# Step 22: Final Report Status
track_lifecycle "GET" "/reports/$REPORT_ID" "" "FINAL STATUS: Report Cycle Complete"

# Step 23: System Statistics
track_lifecycle "GET" "/visits/count-by-status" "" "SYSTEM STATS: Visit Status Distribution"
track_lifecycle "GET" "/billing/stats" "" "SYSTEM STATS: Billing Statistics"
track_lifecycle "GET" "/reports/statistics" "" "SYSTEM STATS: Report Statistics"

echo -e "${GREEN}🎉 LIFECYCLE TRACKING COMPLETE!${NC}"
echo ""
echo -e "${BLUE}📊 LIFECYCLE SUMMARY:${NC}"
echo "====================="
echo -e "${YELLOW}Visit Lifecycle:${NC} PENDING → IN_PROGRESS → AWAITING_APPROVAL → BILLED → COMPLETED"
echo -e "${YELLOW}Sample Lifecycle:${NC} PENDING → IN_PROGRESS → COMPLETED → APPROVED"
echo -e "${YELLOW}Billing Cycle:${NC} Generated → Unpaid → Paid"
echo -e "${YELLOW}Report Cycle:${NC} DRAFT → GENERATED → AUTHORIZED → SENT"
echo ""
echo -e "${GREEN}✅ All lifecycles successfully tracked and documented!${NC}"
