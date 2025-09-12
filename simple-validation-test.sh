#!/bin/bash

# Simple Validation Test
# Tests the corrected validation logic

set -e

BASE_URL="http://localhost:8080"
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔬 Simple Validation Test${NC}"
echo "Base URL: $BASE_URL"
echo ""

# Function to make API calls and show results
test_api() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    local expect_error=$5
    
    echo -e "${YELLOW}$description${NC}"
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X $method "$BASE_URL$endpoint" \
                   -H "Content-Type: application/json" \
                   -d "$data")
    else
        response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X $method "$BASE_URL$endpoint")
    fi
    
    # Extract HTTP status
    http_status=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    response_body=$(echo "$response" | sed '/HTTP_STATUS:/d')
    
    if [ "$expect_error" = "true" ]; then
        if [ "$http_status" -ge 400 ]; then
            echo -e "${GREEN}✅ Expected error received (HTTP $http_status)${NC}"
        else
            echo -e "${RED}❌ Expected error but got success (HTTP $http_status)${NC}"
        fi
    else
        if [ "$http_status" -lt 400 ]; then
            echo -e "${GREEN}✅ Success (HTTP $http_status)${NC}"
        else
            echo -e "${RED}❌ Error (HTTP $http_status)${NC}"
        fi
    fi
    
    echo "$response_body" | jq . 2>/dev/null || echo "$response_body"
    echo ""
}

echo -e "${BLUE}Testing Corrected Validation Logic${NC}"

# Test valid NABL-compliant results
test_api "PATCH" "/visits/1/tests/1/results" '{
  "results": {
    "Glucose": {
      "value": "85",
      "unit": "mg/dL",
      "status": "Normal"
    },
    "Method": {
      "value": "Enzymatic",
      "unit": "",
      "status": "Normal"
    },
    "conclusion": "Glucose level within normal range"
  }
}' "✅ Test Valid NABL-Compliant Results" false

# Test simple numeric value format
test_api "PATCH" "/visits/2/tests" '{
  "testTemplateId": 1,
  "price": 150.00
}' "🧪 Add Test to Visit 2" false

test_api "PATCH" "/visits/2/tests/1/results" '{
  "results": {
    "Glucose": "92",
    "Method": "Hexokinase",
    "conclusion": "Normal glucose level"
  }
}' "✅ Test Simple Value Format" false

# Test invalid numeric value
test_api "PATCH" "/visits/3/tests" '{
  "testTemplateId": 1,
  "price": 150.00
}' "🧪 Add Test to Visit 3" false

test_api "PATCH" "/visits/3/tests/1/results" '{
  "results": {
    "Glucose": "not_a_number",
    "Method": "Enzymatic"
  }
}' "❌ Test Invalid Numeric Value" true

# Test missing required field
test_api "PATCH" "/visits/4/tests" '{
  "testTemplateId": 1,
  "price": 150.00
}' "🧪 Add Test to Visit 4" false

test_api "PATCH" "/visits/4/tests/1/results" '{
  "results": {
    "Method": "Enzymatic"
  }
}' "❌ Test Missing Required Field" true

echo -e "${GREEN}🎉 Simple Validation Test Complete!${NC}"
