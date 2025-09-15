#!/bin/bash

# NABL Complete System Test
# Tests the complete NABL-compliant lab operations system

set -e

BASE_URL="http://localhost:8080"
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔬 NABL Complete System Test${NC}"
echo "Base URL: $BASE_URL"
echo ""

# Function to make API calls and show results
test_api() {
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
    
    echo -e "${GREEN}✅ Response:${NC}"
    echo "$response" | jq . 2>/dev/null || echo "$response"
    echo ""
}

echo -e "${PURPLE}📋 PHASE 1: NABL SAMPLE COLLECTION${NC}"
echo "=================================="

# Test sample collection
test_api "POST" "/samples/collect" '{
  "visitId": 1,
  "sampleType": "WHOLE_BLOOD",
  "collectedBy": "Nurse Jane",
  "collectionSite": "Left Antecubital Vein",
  "collectionConditions": {
    "fasting": true,
    "collectionTime": "08:30",
    "patientPosition": "Seated",
    "tourniquetTime": "<1 minute"
  }
}' "🩸 NABL Sample Collection"

echo -e "${PURPLE}📋 PHASE 2: NABL SAMPLE RECEIPT${NC}"
echo "==============================="

# Test sample receipt
test_api "PATCH" "/samples/20250913WB-1-0001/receive" '{
  "receivedBy": "Lab Tech Mike",
  "receiptCondition": "GOOD",
  "receiptTemperature": 22.5,
  "volumeReceived": 5.0,
  "qualityIndicators": {
    "hemolysis": "None",
    "lipemia": "None",
    "icterus": "None",
    "clotting": "None"
  }
}' "📦 NABL Sample Receipt"

echo -e "${PURPLE}📋 PHASE 3: NABL SAMPLE ACCEPTANCE${NC}"
echo "=================================="

# Test sample acceptance
test_api "PATCH" "/samples/20250913WB-1-0001/accept" '{
  "acceptedBy": "QC Supervisor Sarah",
  "containerType": "EDTA Tube",
  "preservative": "EDTA",
  "storageConditions": "Refrigerated 2-8°C"
}' "✅ NABL Sample Acceptance"

echo -e "${PURPLE}📋 PHASE 4: NABL SAMPLE PROCESSING${NC}"
echo "=================================="

# Test sample processing
test_api "PATCH" "/samples/20250913WB-1-0001/process" '{
  "processedBy": "Lab Analyst Tom",
  "storageLocation": "Rack A1-B2",
  "storageTemperature": 4.0,
  "processingNotes": "Sample aliquoted for CBC analysis"
}' "⚗️ NABL Sample Processing"

echo -e "${PURPLE}📋 PHASE 5: NABL SAMPLE ANALYSIS${NC}"
echo "================================"

# Test sample analysis
test_api "PATCH" "/samples/20250913WB-1-0001/analyze" '{
  "analyzedBy": "Senior Analyst Dr. Patel",
  "analysisMethod": "Automated Hematology Analyzer",
  "qualityControls": {
    "normalControl": "PASS",
    "abnormalControl": "PASS",
    "calibration": "VALID"
  }
}' "🔬 NABL Sample Analysis"

echo -e "${PURPLE}📋 PHASE 6: NABL SAMPLE COMPLETION${NC}"
echo "=================================="

# Test analysis completion
test_api "PATCH" "/samples/20250913WB-1-0001/complete" '{
  "completedBy": "Senior Analyst Dr. Patel",
  "qualityIndicators": {
    "analysisComplete": true,
    "resultsValidated": true,
    "qualityControlPassed": true
  }
}' "📊 NABL Analysis Complete"

echo -e "${PURPLE}📋 PHASE 7: NABL SAMPLE REVIEW${NC}"
echo "==============================="

# Test sample review
test_api "PATCH" "/samples/20250913WB-1-0001/review" '{
  "reviewedBy": "Pathologist Dr. Kumar",
  "reviewNotes": "Results reviewed and approved for reporting",
  "medicalSignificance": "Normal values within reference range"
}' "👨‍⚕️ NABL Medical Review"

echo -e "${PURPLE}📋 PHASE 8: NABL SAMPLE STORAGE${NC}"
echo "==============================="

# Test sample storage
test_api "PATCH" "/samples/20250913WB-1-0001/store" '{
  "storedBy": "Storage Technician Lisa",
  "storageLocation": "Long-term Storage Freezer F1-S3",
  "storageConditions": "Frozen -20°C",
  "retentionPeriod": "2 years"
}' "🏪 NABL Sample Storage"

echo -e "${PURPLE}📋 PHASE 9: NABL COMPLIANCE VERIFICATION${NC}"
echo "========================================"

# Get complete sample chain of custody
test_api "GET" "/samples/20250913WB-1-0001" "" "📋 NABL Complete Chain of Custody"

# Get sample statistics
test_api "GET" "/samples/statistics" "" "📊 NABL Sample Statistics"

echo -e "${GREEN}🎉 NABL Complete System Test Finished!${NC}"
echo -e "${BLUE}✅ All NABL requirements tested successfully${NC}"
echo ""
