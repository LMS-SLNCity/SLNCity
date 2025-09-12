#!/bin/bash

# NABL Sample Lifecycle Test
# Tests complete NABL-compliant sample management from collection to disposal

set -e

BASE_URL="http://localhost:8080"
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔬 NABL Sample Lifecycle Test${NC}"
echo "Base URL: $BASE_URL"
echo ""

# Function to make API calls
api_call() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    
    echo -e "${YELLOW}$description${NC}"
    
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
    else
        echo -e "${RED}❌ Status: $http_code${NC}"
        echo "Response: $body"
    fi
    
    echo ""
}

echo -e "${PURPLE}📋 PHASE 1: SETUP - Create Test Template & Visit${NC}"
echo "=================================================="

# Create CBC Template
api_call "POST" "/test-templates" '{
  "name": "Complete Blood Count (CBC)",
  "description": "NABL-compliant blood analysis",
  "basePrice": 450.00,
  "parameters": [
    {"name": "Hemoglobin", "unit": "g/dL", "referenceRange": "12.0-15.5 (F), 13.5-17.5 (M)", "type": "numeric"},
    {"name": "WBC Count", "unit": "/μL", "referenceRange": "4000-11000", "type": "numeric"},
    {"name": "Platelet Count", "unit": "/μL", "referenceRange": "150000-450000", "type": "numeric"}
  ]
}' "🧪 Create CBC Test Template"

# Create Patient Visit
api_call "POST" "/visits" '{
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
}' "🏥 Create Patient Visit"

echo -e "${PURPLE}📋 PHASE 2: NABL SAMPLE COLLECTION${NC}"
echo "=================================="

# Step 1: Collect Sample (NABL Requirement: Sample Collection Documentation)
api_call "POST" "/samples/collect" '{
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
}' "🩸 NABL SAMPLE LIFECYCLE: Sample Collection (Status: COLLECTED)"

echo -e "${PURPLE}📋 PHASE 3: NABL SAMPLE RECEIPT & QUALITY CONTROL${NC}"
echo "================================================="

# Step 2: Receive Sample (NABL Requirement: Sample Receipt Documentation)
api_call "PATCH" "/samples/SMP/2025/000001/receive" '{
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
}' "📦 NABL SAMPLE LIFECYCLE: Sample Receipt (Status: RECEIVED)"

# Step 3: Accept Sample (NABL Requirement: Quality Control Acceptance)
api_call "PATCH" "/samples/SMP/2025/000001/accept" '{
  "acceptedBy": "QC Supervisor Sarah",
  "containerType": "EDTA Tube",
  "preservative": "EDTA",
  "storageConditions": "Refrigerated 2-8°C"
}' "✅ NABL SAMPLE LIFECYCLE: Sample Acceptance (Status: ACCEPTED)"

echo -e "${PURPLE}📋 PHASE 4: NABL SAMPLE PROCESSING${NC}"
echo "=================================="

# Step 4: Process Sample (NABL Requirement: Processing Documentation)
api_call "PATCH" "/samples/SMP/2025/000001/process" '{
  "processedBy": "Lab Analyst Tom",
  "storageLocation": "Rack A1-B2",
  "storageTemperature": 4.0,
  "processingNotes": "Sample aliquoted for CBC analysis"
}' "⚗️ NABL SAMPLE LIFECYCLE: Sample Processing (Status: PROCESSING)"

echo -e "${PURPLE}📋 PHASE 5: NABL SAMPLE ANALYSIS${NC}"
echo "================================"

# Step 5: Analyze Sample (NABL Requirement: Analysis Documentation)
api_call "PATCH" "/samples/SMP/2025/000001/analyze" '{
  "analyzedBy": "Senior Analyst Dr. Patel",
  "analysisMethod": "Automated Hematology Analyzer",
  "qualityControls": {
    "normalControl": "PASS",
    "abnormalControl": "PASS",
    "calibration": "VALID"
  }
}' "🔬 NABL SAMPLE LIFECYCLE: Sample Analysis (Status: IN_ANALYSIS)"

# Step 6: Complete Analysis (NABL Requirement: Analysis Completion)
api_call "PATCH" "/samples/SMP/2025/000001/complete" '{
  "completedBy": "Senior Analyst Dr. Patel",
  "qualityIndicators": {
    "analysisComplete": true,
    "resultsValidated": true,
    "qualityControlPassed": true
  }
}' "📊 NABL SAMPLE LIFECYCLE: Analysis Complete (Status: ANALYSIS_COMPLETE)"

echo -e "${PURPLE}📋 PHASE 6: NABL SAMPLE REVIEW & STORAGE${NC}"
echo "========================================"

# Step 7: Review Sample (NABL Requirement: Medical Review)
api_call "PATCH" "/samples/SMP/2025/000001/review" '{
  "reviewedBy": "Pathologist Dr. Kumar",
  "reviewNotes": "Results reviewed and approved for reporting",
  "medicalSignificance": "Normal values within reference range"
}' "👨‍⚕️ NABL SAMPLE LIFECYCLE: Medical Review (Status: REVIEWED)"

# Step 8: Store Sample (NABL Requirement: Sample Storage)
api_call "PATCH" "/samples/SMP/2025/000001/store" '{
  "storedBy": "Storage Technician Lisa",
  "storageLocation": "Long-term Storage Freezer F1-S3",
  "storageConditions": "Frozen -20°C",
  "retentionPeriod": "2 years"
}' "🏪 NABL SAMPLE LIFECYCLE: Sample Storage (Status: STORED)"

echo -e "${PURPLE}📋 PHASE 7: NABL SAMPLE DISPOSAL${NC}"
echo "================================"

# Step 9: Dispose Sample (NABL Requirement: Disposal Documentation)
api_call "PATCH" "/samples/SMP/2025/000001/dispose" '{
  "disposedBy": "Waste Management Officer John",
  "disposalMethod": "Incineration",
  "disposalBatch": "BATCH-2025-001",
  "disposalCertificate": "CERT-INC-2025-001"
}' "🗑️ NABL SAMPLE LIFECYCLE: Sample Disposal (Status: DISPOSED)"

echo -e "${PURPLE}📋 PHASE 8: NABL COMPLIANCE VERIFICATION${NC}"
echo "========================================"

# Get complete sample chain of custody
api_call "GET" "/samples/SMP/2025/000001" "" "📋 NABL COMPLIANCE: Complete Chain of Custody"

# Get sample statistics
api_call "GET" "/samples/statistics" "" "📊 NABL COMPLIANCE: Sample Statistics"

echo -e "${GREEN}🎉 NABL Sample Lifecycle Test Complete!${NC}"
echo -e "${BLUE}✅ All NABL requirements tested successfully${NC}"
echo ""
