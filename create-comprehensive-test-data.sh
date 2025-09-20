#!/bin/bash

echo "🏥 CREATING COMPREHENSIVE TEST DATA - SLNCity Lab System"
echo "======================================================="
echo "Creating realistic data for all workflow profiles"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Function to create patient visit
create_patient() {
    local first_name="$1"
    local last_name="$2"
    local phone="$3"
    local email="$4"
    local dob="$5"
    local gender="$6"
    
    PATIENT_DATA="{
        \"patientDetails\": {
            \"firstName\": \"$first_name\",
            \"lastName\": \"$last_name\",
            \"dateOfBirth\": \"$dob\",
            \"gender\": \"$gender\",
            \"phoneNumber\": \"$phone\",
            \"email\": \"$email\",
            \"address\": \"123 Test Street, SLNCity, Test State 500001\"
        }
    }"
    
    VISIT_RESPONSE=$(curl -s -X POST "http://localhost:8080/visits" \
        -H "Content-Type: application/json" \
        -d "$PATIENT_DATA")
    
    VISIT_ID=$(echo "$VISIT_RESPONSE" | jq -r '.visitId // empty' 2>/dev/null)
    
    if [ -n "$VISIT_ID" ] && [ "$VISIT_ID" != "null" ]; then
        echo -e "${GREEN}✅ Created patient: $first_name $last_name (Visit ID: $VISIT_ID)${NC}"
        echo "$VISIT_ID"
    else
        echo -e "${RED}❌ Failed to create patient: $first_name $last_name${NC}"
        echo ""
    fi
}

# Function to create test template
create_test_template() {
    local name="$1"
    local description="$2"
    local price="$3"
    local sample_type="$4"
    
    TEMPLATE_DATA="{
        \"name\": \"$name\",
        \"description\": \"$description\",
        \"basePrice\": $price,
        \"parameters\": {
            \"sampleType\": \"$sample_type\",
            \"volume\": \"5 mL\",
            \"processingTime\": \"2 hours\",
            \"fasting\": false
        }
    }"
    
    TEMPLATE_RESPONSE=$(curl -s -X POST "http://localhost:8080/test-templates" \
        -H "Content-Type: application/json" \
        -d "$TEMPLATE_DATA")
    
    TEMPLATE_ID=$(echo "$TEMPLATE_RESPONSE" | jq -r '.templateId // empty' 2>/dev/null)
    
    if [ -n "$TEMPLATE_ID" ] && [ "$TEMPLATE_ID" != "null" ]; then
        echo -e "${GREEN}✅ Created test template: $name (ID: $TEMPLATE_ID)${NC}"
        echo "$TEMPLATE_ID"
    else
        echo -e "${RED}❌ Failed to create test template: $name${NC}"
        echo ""
    fi
}

# Function to add test to visit
add_test_to_visit() {
    local visit_id="$1"
    local template_id="$2"
    
    TEST_REQUEST="{\"testTemplateId\": $template_id}"
    
    TEST_RESPONSE=$(curl -s -X POST "http://localhost:8080/visits/$visit_id/tests" \
        -H "Content-Type: application/json" \
        -d "$TEST_REQUEST")
    
    TEST_ID=$(echo "$TEST_RESPONSE" | jq -r '.testId // empty' 2>/dev/null)
    
    if [ -n "$TEST_ID" ] && [ "$TEST_ID" != "null" ]; then
        echo -e "${GREEN}✅ Added test to visit $visit_id (Test ID: $TEST_ID)${NC}"
        echo "$TEST_ID"
    else
        echo -e "${RED}❌ Failed to add test to visit $visit_id${NC}"
        echo ""
    fi
}

# Function to collect sample
collect_sample() {
    local test_id="$1"
    local sample_type="$2"
    local phlebotomist="$3"
    
    SAMPLE_DATA="{
        \"sampleType\": \"$sample_type\",
        \"collectedBy\": \"$phlebotomist\",
        \"collectionSite\": \"Left antecubital vein\",
        \"containerType\": \"EDTA tube\",
        \"volumeReceived\": 5.0,
        \"notes\": \"Sample collected successfully\"
    }"
    
    SAMPLE_RESPONSE=$(curl -s -X POST "http://localhost:8080/sample-collection/collect/$test_id" \
        -H "Content-Type: application/json" \
        -d "$SAMPLE_DATA")
    
    SAMPLE_ID=$(echo "$SAMPLE_RESPONSE" | jq -r '.sampleId // empty' 2>/dev/null)
    
    if [ -n "$SAMPLE_ID" ] && [ "$SAMPLE_ID" != "null" ]; then
        echo -e "${GREEN}✅ Collected sample for test $test_id (Sample ID: $SAMPLE_ID)${NC}"
        echo "$SAMPLE_ID"
    else
        echo -e "${YELLOW}⚠️  Sample collection response for test $test_id:${NC}"
        echo "$SAMPLE_RESPONSE"
        echo ""
    fi
}

echo -e "${BLUE}🌐 Checking SLNCity Lab System...${NC}"
SERVER_STATUS=$(curl -s -w "%{http_code}" "http://localhost:8080/actuator/health" -o /dev/null)
if [ "$SERVER_STATUS" != "200" ]; then
    echo -e "${RED}❌ Server is not accessible${NC}"
    exit 1
fi
echo -e "${GREEN}✅ SLNCity Lab System is online${NC}"
echo ""

echo -e "${PURPLE}🧪 PHASE 1: Creating Test Templates${NC}"
echo "=================================="

# Create comprehensive test templates
CBC_ID=$(create_test_template "Complete Blood Count (CBC)" "Comprehensive blood cell analysis" 450.00 "WHOLE_BLOOD")
LIPID_ID=$(create_test_template "Lipid Profile" "Cholesterol and triglyceride analysis" 380.00 "SERUM")
GLUCOSE_ID=$(create_test_template "Fasting Blood Glucose" "Blood sugar level measurement" 150.00 "SERUM")
LIVER_ID=$(create_test_template "Liver Function Test" "Comprehensive liver enzyme analysis" 520.00 "SERUM")
KIDNEY_ID=$(create_test_template "Kidney Function Test" "Creatinine and BUN analysis" 320.00 "SERUM")

echo ""
echo -e "${PURPLE}👥 PHASE 2: Creating Patient Visits${NC}"
echo "=================================="

# Create diverse patient visits
VISIT1=$(create_patient "Rajesh" "Kumar" "+91-9876543210" "rajesh.kumar@email.com" "1985-06-15" "MALE")
VISIT2=$(create_patient "Priya" "Sharma" "+91-9876543211" "priya.sharma@email.com" "1992-03-22" "FEMALE")
VISIT3=$(create_patient "Amit" "Patel" "+91-9876543212" "amit.patel@email.com" "1978-11-08" "MALE")
VISIT4=$(create_patient "Sunita" "Singh" "+91-9876543213" "sunita.singh@email.com" "1995-07-30" "FEMALE")
VISIT5=$(create_patient "Vikram" "Reddy" "+91-9876543214" "vikram.reddy@email.com" "1988-12-10" "MALE")

echo ""
echo -e "${PURPLE}🔬 PHASE 3: Adding Tests to Visits${NC}"
echo "================================="

# Add tests to visits to create different workflow scenarios
if [ -n "$VISIT1" ] && [ -n "$CBC_ID" ]; then
    TEST1=$(add_test_to_visit "$VISIT1" "$CBC_ID")
fi

if [ -n "$VISIT1" ] && [ -n "$LIPID_ID" ]; then
    TEST2=$(add_test_to_visit "$VISIT1" "$LIPID_ID")
fi

if [ -n "$VISIT2" ] && [ -n "$GLUCOSE_ID" ]; then
    TEST3=$(add_test_to_visit "$VISIT2" "$GLUCOSE_ID")
fi

if [ -n "$VISIT3" ] && [ -n "$LIVER_ID" ]; then
    TEST4=$(add_test_to_visit "$VISIT3" "$LIVER_ID")
fi

if [ -n "$VISIT4" ] && [ -n "$KIDNEY_ID" ]; then
    TEST5=$(add_test_to_visit "$VISIT4" "$KIDNEY_ID")
fi

if [ -n "$VISIT5" ] && [ -n "$CBC_ID" ]; then
    TEST6=$(add_test_to_visit "$VISIT5" "$CBC_ID")
fi

echo ""
echo -e "${PURPLE}🩸 PHASE 4: Sample Collection (Phlebotomy)${NC}"
echo "========================================"

# Collect samples for some tests (leaving others for phlebotomy queue)
if [ -n "$TEST1" ]; then
    SAMPLE1=$(collect_sample "$TEST1" "WHOLE_BLOOD" "Phlebotomist Priya")
fi

if [ -n "$TEST3" ]; then
    SAMPLE3=$(collect_sample "$TEST3" "SERUM" "Phlebotomist Raj")
fi

if [ -n "$TEST4" ]; then
    SAMPLE4=$(collect_sample "$TEST4" "SERUM" "Phlebotomist Priya")
fi

echo ""
echo -e "${PURPLE}🏥 PHASE 5: Creating Lab Equipment${NC}"
echo "================================"

# Create lab equipment for admin dashboard
EQUIPMENT_DATA='{
    "name": "Automated Hematology Analyzer",
    "manufacturer": "Sysmex Corporation",
    "model": "XN-1000",
    "serialNumber": "SLN-2024-001",
    "equipmentType": "ANALYZER",
    "status": "ACTIVE",
    "location": "Hematology Lab - Section A",
    "notes": "Primary CBC analyzer for high-volume testing"
}'

EQUIPMENT_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/v1/equipment" \
    -H "Content-Type: application/json" \
    -d "$EQUIPMENT_DATA")

EQUIPMENT_ID=$(echo "$EQUIPMENT_RESPONSE" | jq -r '.id // empty' 2>/dev/null)

if [ -n "$EQUIPMENT_ID" ] && [ "$EQUIPMENT_ID" != "null" ]; then
    echo -e "${GREEN}✅ Created lab equipment: Hematology Analyzer (ID: $EQUIPMENT_ID)${NC}"
else
    echo -e "${YELLOW}⚠️  Equipment creation response:${NC}"
    echo "$EQUIPMENT_RESPONSE"
fi

echo ""
echo -e "${BLUE}📊 COMPREHENSIVE TEST DATA SUMMARY${NC}"
echo "=================================="

# Get final system status
FINAL_VISITS=$(curl -s "http://localhost:8080/visits" | jq 'length' 2>/dev/null || echo "0")
FINAL_TESTS=$(curl -s "http://localhost:8080/lab-tests" | jq 'length' 2>/dev/null || echo "0")
FINAL_SAMPLES=$(curl -s "http://localhost:8080/samples" | jq 'length' 2>/dev/null || echo "0")
FINAL_TEMPLATES=$(curl -s "http://localhost:8080/test-templates" | jq 'length' 2>/dev/null || echo "0")
FINAL_EQUIPMENT=$(curl -s "http://localhost:8080/api/v1/equipment" | jq 'length' 2>/dev/null || echo "0")

# Check pending collections
PENDING_COLLECTIONS=$(curl -s "http://localhost:8080/sample-collection/pending" | jq 'length' 2>/dev/null || echo "0")

echo "📈 System Data Overview:"
echo "   • Patient Visits: $FINAL_VISITS"
echo "   • Lab Tests: $FINAL_TESTS"
echo "   • Samples Collected: $FINAL_SAMPLES"
echo "   • Test Templates: $FINAL_TEMPLATES"
echo "   • Lab Equipment: $FINAL_EQUIPMENT"
echo "   • Pending Sample Collections: $PENDING_COLLECTIONS"

echo ""
echo -e "${GREEN}🎉 COMPREHENSIVE TEST DATA CREATED SUCCESSFULLY!${NC}"
echo ""
echo -e "${YELLOW}🌐 All Dashboards Now Fully Functional:${NC}"
echo ""
echo -e "${BLUE}🏥 Reception Dashboard:${NC}"
echo "   • URL: http://localhost:8080/reception/dashboard.html"
echo "   • Shows: $FINAL_VISITS patient visits, test ordering capability"
echo ""
echo -e "${BLUE}🩸 Phlebotomy Dashboard:${NC}"
echo "   • URL: http://localhost:8080/phlebotomy/dashboard.html"
echo "   • Shows: $PENDING_COLLECTIONS tests needing sample collection"
echo "   • Features: Patient queue, sample collection workflow"
echo ""
echo -e "${BLUE}🔬 Lab Technician Dashboard:${NC}"
echo "   • URL: http://localhost:8080/technician/dashboard.html"
echo "   • Shows: Tests ready for processing and analysis"
echo "   • Features: Results entry, test approval workflow"
echo ""
echo -e "${BLUE}👨‍💼 Admin Dashboard:${NC}"
echo "   • URL: http://localhost:8080/admin/dashboard.html"
echo "   • Shows: System overview, equipment management"
echo "   • Features: Complete system monitoring"

echo ""
echo -e "${PURPLE}🔄 COMPLETE WORKFLOW NOW AVAILABLE:${NC}"
echo "   1. Reception → Register patients & order tests"
echo "   2. Phlebotomy → Collect samples from pending tests"
echo "   3. Lab Technician → Process samples & enter results"
echo "   4. Admin → Monitor system & manage equipment"

echo ""
echo -e "${GREEN}🚀 SLNCity Lab System is Ready for Full Testing!${NC}"

exit 0
