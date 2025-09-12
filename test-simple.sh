#!/bin/bash

# Simple API test script
BASE_URL="http://localhost:8080"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

# Function to extract ID from JSON response
extract_id() {
    local json="$1"
    local field="$2"
    echo "$json" | grep -o "\"$field\":[0-9]*" | cut -d':' -f2 | head -1
}

# Test function
test_api() {
    local method="$1"
    local endpoint="$2"
    local data="$3"
    local description="$4"
    
    echo -e "${BLUE}Testing: $description${NC}"
    echo "  Method: $method"
    echo "  Endpoint: $endpoint"
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" "$BASE_URL$endpoint")
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST -H "Content-Type: application/json" -d "$data" "$BASE_URL$endpoint")
    elif [ "$method" = "PATCH" ]; then
        response=$(curl -s -w "\n%{http_code}" -X PATCH -H "Content-Type: application/json" -d "$data" "$BASE_URL$endpoint")
    fi
    
    # Extract status code and body
    status_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    echo "  Status: $status_code"
    echo "  Response: $body"
    echo ""
    
    # Return the response body
    echo "$body"
}

echo -e "${BLUE}🚀 Simple API Test${NC}"
echo ""

# Test 1: Create Test Template
template_response=$(test_api "POST" "/test-templates" '{
    "name": "Complete Blood Count",
    "description": "Full blood analysis",
    "basePrice": 500.00,
    "parameters": {
        "hemoglobin": {"type": "number", "unit": "g/dL", "normalRange": "12-16"}
    }
}' "Create Test Template")

TEMPLATE_ID=$(extract_id "$template_response" "templateId")
echo "Extracted Template ID: $TEMPLATE_ID"
echo ""

# Test 2: Create Visit
visit_response=$(test_api "POST" "/visits" '{
    "patientDetails": {
        "name": "John Doe",
        "age": 35,
        "gender": "M",
        "phone": "9876543210",
        "address": "123 Main St, Hyderabad"
    }
}' "Create Visit")

VISIT_ID=$(extract_id "$visit_response" "visitId")
echo "Extracted Visit ID: $VISIT_ID"
echo ""

# Test 3: Add Test to Visit
if [ -n "$TEMPLATE_ID" ] && [ -n "$VISIT_ID" ]; then
    test_response=$(test_api "POST" "/visits/$VISIT_ID/tests" "{
        \"testTemplateId\": $TEMPLATE_ID,
        \"price\": 500.00
    }" "Add Test to Visit")
    
    TEST_ID=$(extract_id "$test_response" "testId")
    echo "Extracted Test ID: $TEST_ID"
    echo ""
    
    # Test 4: Get Tests for Visit
    test_api "GET" "/visits/$VISIT_ID/tests" "" "Get Tests for Visit"
    
    # Test 5: Update Test Results
    if [ -n "$TEST_ID" ]; then
        test_api "PATCH" "/visits/$VISIT_ID/tests/$TEST_ID/results" '{
            "results": {
                "hemoglobin": "14.2 g/dL",
                "conclusion": "Normal"
            }
        }' "Update Test Results"
        
        # Test 6: Approve Test
        test_api "PATCH" "/visits/$VISIT_ID/tests/$TEST_ID/approve" '{
            "approvedBy": "Dr. Smith"
        }' "Approve Test Results"
        
        # Test 7: Generate Bill
        bill_response=$(test_api "GET" "/billing/visits/$VISIT_ID/bill" "" "Generate Bill")
        
        BILL_ID=$(extract_id "$bill_response" "billId")
        echo "Extracted Bill ID: $BILL_ID"
        echo ""
        
        # Test 8: Mark Bill as Paid
        if [ -n "$BILL_ID" ]; then
            test_api "PATCH" "/billing/$BILL_ID/pay" "" "Mark Bill as Paid"
        fi
    fi
else
    echo -e "${RED}❌ Could not extract IDs, skipping dependent tests${NC}"
fi

echo -e "${GREEN}✅ Simple test completed!${NC}"
