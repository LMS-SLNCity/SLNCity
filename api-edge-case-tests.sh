#!/bin/bash

# Lab Operations API Edge Case Testing Suite
# Comprehensive testing of boundary conditions, validation rules, and business logic

BASE_URL="http://localhost:8080"
REPORT_FILE="edge-case-test-report-$(date +%Y%m%d-%H%M%S).html"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m'

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Test results
RESULTS_FILE="/tmp/edge_case_results.txt"
echo "Test#|Status|Category|Description|Method|Endpoint|StatusCode|Response" > "$RESULTS_FILE"

# Global variables for test data
VALID_TEMPLATE_ID=""
VALID_VISIT_ID=""
VALID_TEST_ID=""
VALID_BILL_ID=""

# Function to extract ID from JSON response
extract_id() {
    local json="$1"
    local field="$2"
    echo "$json" | grep -o "\"$field\":[0-9]*" | cut -d':' -f2 | head -1
}

# Function to test API endpoint
test_edge_case() {
    local method="$1"
    local endpoint="$2"
    local data="$3"
    local expected_status="$4"
    local category="$5"
    local description="$6"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    echo -e "${BLUE}[$category] Testing: $description${NC}"
    
    # Make the API call
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" "$BASE_URL$endpoint")
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST -H "Content-Type: application/json" -d "$data" "$BASE_URL$endpoint")
    elif [ "$method" = "PATCH" ]; then
        response=$(curl -s -w "\n%{http_code}" -X PATCH -H "Content-Type: application/json" -d "$data" "$BASE_URL$endpoint")
    elif [ "$method" = "PUT" ]; then
        response=$(curl -s -w "\n%{http_code}" -X PUT -H "Content-Type: application/json" -d "$data" "$BASE_URL$endpoint")
    elif [ "$method" = "DELETE" ]; then
        response=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL$endpoint")
    fi
    
    # Extract status code and body
    status_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    # Check if test passed
    if [ "$status_code" = "$expected_status" ]; then
        echo -e "${GREEN}✅ PASSED${NC} - Status: $status_code"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        echo "$TOTAL_TESTS|PASS|$category|$description|$method|$endpoint|$status_code|$body" >> "$RESULTS_FILE"
    else
        echo -e "${RED}❌ FAILED${NC} - Expected: $expected_status, Got: $status_code"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        echo "$TOTAL_TESTS|FAIL|$category|$description|$method|$endpoint|$status_code|$body" >> "$RESULTS_FILE"
    fi
    
    echo ""
    return $status_code
}

# Setup test data
setup_test_data() {
    echo -e "${YELLOW}🔧 Setting up test data...${NC}"
    
    # Create a valid template
    template_name="EdgeTest_$(date +%s)"
    template_response=$(curl -s -X POST -H "Content-Type: application/json" \
        -d "{\"name\": \"$template_name\", \"description\": \"Test template\", \"basePrice\": 100.00, \"parameters\": {\"test\": \"value\"}}" \
        "$BASE_URL/test-templates")
    VALID_TEMPLATE_ID=$(extract_id "$template_response" "templateId")
    
    # Create a valid visit
    visit_response=$(curl -s -X POST -H "Content-Type: application/json" \
        -d '{"patientDetails": {"name": "Test Patient", "age": 30, "gender": "M", "phone": "1234567890"}}' \
        "$BASE_URL/visits")
    VALID_VISIT_ID=$(extract_id "$visit_response" "visitId")
    
    # Add a test to the visit
    if [ -n "$VALID_TEMPLATE_ID" ] && [ -n "$VALID_VISIT_ID" ]; then
        test_response=$(curl -s -X POST -H "Content-Type: application/json" \
            -d "{\"testTemplateId\": $VALID_TEMPLATE_ID, \"price\": 100.00}" \
            "$BASE_URL/visits/$VALID_VISIT_ID/tests")
        VALID_TEST_ID=$(extract_id "$test_response" "testId")
    fi
    
    echo "Valid Template ID: $VALID_TEMPLATE_ID"
    echo "Valid Visit ID: $VALID_VISIT_ID"
    echo "Valid Test ID: $VALID_TEST_ID"
    echo ""
}

# Check if application is running
check_app() {
    echo -e "${YELLOW}Checking if application is running...${NC}"
    if ! curl -s "$BASE_URL/actuator/health" > /dev/null; then
        echo -e "${RED}Application is not running at $BASE_URL${NC}"
        echo "Please start the application first: mvn spring-boot:run"
        exit 1
    fi
    echo -e "${GREEN}Application is running!${NC}"
    echo ""
}

# Test Template Edge Cases
test_template_edge_cases() {
    echo -e "${PURPLE}🧪 Testing Test Template Edge Cases${NC}"
    
    # Validation Edge Cases
    test_edge_case "POST" "/test-templates" '{}' "400" "VALIDATION" "Empty request body"
    test_edge_case "POST" "/test-templates" '{"name": ""}' "400" "VALIDATION" "Empty name"
    test_edge_case "POST" "/test-templates" '{"name": "   "}' "400" "VALIDATION" "Whitespace-only name"
    test_edge_case "POST" "/test-templates" '{"name": "Test", "basePrice": 0}' "400" "VALIDATION" "Zero base price"
    test_edge_case "POST" "/test-templates" '{"name": "Test", "basePrice": -10}' "400" "VALIDATION" "Negative base price"
    test_edge_case "POST" "/test-templates" '{"name": "Test", "basePrice": 100}' "400" "VALIDATION" "Missing parameters"
    test_edge_case "POST" "/test-templates" '{"name": "Test", "basePrice": 100, "parameters": null}' "400" "VALIDATION" "Null parameters"
    
    # Boundary Values
    test_edge_case "POST" "/test-templates" '{"name": "A", "description": "", "basePrice": 0.01, "parameters": {}}' "201" "BOUNDARY" "Minimum valid values"
    
    # Very long name (255+ characters)
    long_name=$(printf 'A%.0s' {1..300})
    test_edge_case "POST" "/test-templates" "{\"name\": \"$long_name\", \"basePrice\": 100, \"parameters\": {}}" "400" "BOUNDARY" "Name too long (300 chars)"
    
    # Large price values
    test_edge_case "POST" "/test-templates" '{"name": "BigPrice", "basePrice": 99999999.99, "parameters": {}}' "201" "BOUNDARY" "Maximum decimal price"
    test_edge_case "POST" "/test-templates" '{"name": "TooBigPrice", "basePrice": 999999999.99, "parameters": {}}' "400" "BOUNDARY" "Price exceeds decimal(10,2)"
    
    # Duplicate name testing
    if [ -n "$VALID_TEMPLATE_ID" ]; then
        # Get the existing template name
        existing_response=$(curl -s "$BASE_URL/test-templates/$VALID_TEMPLATE_ID")
        existing_name=$(echo "$existing_response" | grep -o '"name":"[^"]*"' | cut -d'"' -f4)
        test_edge_case "POST" "/test-templates" "{\"name\": \"$existing_name\", \"basePrice\": 100, \"parameters\": {}}" "409" "BUSINESS" "Duplicate template name"
    fi
    
    # Invalid JSON
    test_edge_case "POST" "/test-templates" '{"name": "Test", "basePrice": 100, "parameters": {invalid json}' "400" "FORMAT" "Invalid JSON syntax"
    
    # Complex parameters
    complex_params='{"hemoglobin": {"type": "number", "unit": "g/dL", "normalRange": "12-16", "criticalLow": 8, "criticalHigh": 20}, "nested": {"deep": {"value": true}}}'
    test_edge_case "POST" "/test-templates" "{\"name\": \"ComplexParams_$(date +%s)\", \"basePrice\": 100, \"parameters\": $complex_params}" "201" "COMPLEX" "Complex nested parameters"
}

# Test Visit Edge Cases
test_visit_edge_cases() {
    echo -e "${PURPLE}🏥 Testing Visit Edge Cases${NC}"
    
    # Validation Edge Cases
    test_edge_case "POST" "/visits" '{}' "400" "VALIDATION" "Empty request body"
    test_edge_case "POST" "/visits" '{"patientDetails": null}' "400" "VALIDATION" "Null patient details"
    test_edge_case "POST" "/visits" '{"patientDetails": {}}' "201" "BOUNDARY" "Empty patient details object"
    
    # Invalid patient data
    test_edge_case "POST" "/visits" '{"patientDetails": {"age": -5}}' "201" "BOUNDARY" "Negative age (no validation)"
    test_edge_case "POST" "/visits" '{"patientDetails": {"age": 200}}' "201" "BOUNDARY" "Very high age (no validation)"
    
    # Very long patient details
    long_name=$(printf 'A%.0s' {1..1000})
    test_edge_case "POST" "/visits" "{\"patientDetails\": {\"name\": \"$long_name\", \"phone\": \"1234567890\"}}" "201" "BOUNDARY" "Very long patient name"
    
    # Special characters in patient details
    test_edge_case "POST" "/visits" '{"patientDetails": {"name": "José María", "address": "123 Main St. #4B", "phone": "+1-555-123-4567"}}' "201" "SPECIAL" "Special characters in patient data"
    
    # Status transition edge cases
    if [ -n "$VALID_VISIT_ID" ]; then
        # Invalid status values
        test_edge_case "PATCH" "/visits/$VALID_VISIT_ID/status?status=invalid" "400" "VALIDATION" "Invalid status value"
        test_edge_case "PATCH" "/visits/$VALID_VISIT_ID/status?status=" "400" "VALIDATION" "Empty status parameter"
        test_edge_case "PATCH" "/visits/$VALID_VISIT_ID/status" "400" "VALIDATION" "Missing status parameter"

        # Valid status transition
        test_edge_case "PATCH" "/visits/$VALID_VISIT_ID/status?status=in-progress" "200" "BUSINESS" "Valid status transition"

        # Invalid status transitions
        test_edge_case "PATCH" "/visits/$VALID_VISIT_ID/status?status=completed" "500" "BUSINESS" "Invalid status jump (in-progress to completed)"
        test_edge_case "PATCH" "/visits/$VALID_VISIT_ID/status?status=billed" "500" "BUSINESS" "Invalid status jump (in-progress to billed)"
    fi
    
    # Non-existent visit operations
    test_edge_case "GET" "/visits/0" "404" "BOUNDARY" "Visit ID zero"
    test_edge_case "GET" "/visits/-1" "404" "BOUNDARY" "Negative visit ID"
    test_edge_case "GET" "/visits/999999999" "404" "BOUNDARY" "Very large visit ID"
    test_edge_case "PATCH" "/visits/999999/status?status=in-progress" "404" "BOUNDARY" "Update non-existent visit status"
}

# Test Lab Test Edge Cases
test_lab_test_edge_cases() {
    echo -e "${PURPLE}🔬 Testing Lab Test Edge Cases${NC}"
    
    if [ -n "$VALID_VISIT_ID" ] && [ -n "$VALID_TEMPLATE_ID" ]; then
        # Invalid test template ID
        test_edge_case "POST" "/visits/$VALID_VISIT_ID/tests" '{"testTemplateId": 999999}' "404" "VALIDATION" "Non-existent template ID"
        test_edge_case "POST" "/visits/$VALID_VISIT_ID/tests" '{"testTemplateId": 0}' "404" "BOUNDARY" "Zero template ID"
        test_edge_case "POST" "/visits/$VALID_VISIT_ID/tests" '{"testTemplateId": -1}' "404" "BOUNDARY" "Negative template ID"
        test_edge_case "POST" "/visits/$VALID_VISIT_ID/tests" '{}' "400" "VALIDATION" "Missing template ID"
        test_edge_case "POST" "/visits/$VALID_VISIT_ID/tests" '{"testTemplateId": null}' "400" "VALIDATION" "Null template ID"
        
        # Price edge cases
        test_edge_case "POST" "/visits/$VALID_VISIT_ID/tests" "{\"testTemplateId\": $VALID_TEMPLATE_ID, \"price\": 0}" "201" "BOUNDARY" "Zero price (allowed)"
        test_edge_case "POST" "/visits/$VALID_VISIT_ID/tests" "{\"testTemplateId\": $VALID_TEMPLATE_ID, \"price\": -10}" "201" "BOUNDARY" "Negative price (no validation)"
        test_edge_case "POST" "/visits/$VALID_VISIT_ID/tests" "{\"testTemplateId\": $VALID_TEMPLATE_ID, \"price\": 99999999.99}" "201" "BOUNDARY" "Maximum price"
        
        # Test operations on non-existent visit
        test_edge_case "POST" "/visits/999999/tests" "{\"testTemplateId\": $VALID_TEMPLATE_ID}" "404" "VALIDATION" "Add test to non-existent visit"
    fi
    
    if [ -n "$VALID_VISIT_ID" ] && [ -n "$VALID_TEST_ID" ]; then
        # Update results edge cases
        test_edge_case "PATCH" "/visits/$VALID_VISIT_ID/tests/$VALID_TEST_ID/results" '{}' "400" "VALIDATION" "Empty results"
        test_edge_case "PATCH" "/visits/$VALID_VISIT_ID/tests/$VALID_TEST_ID/results" '{"results": null}' "400" "VALIDATION" "Null results"
        
        # Valid results update
        test_edge_case "PATCH" "/visits/$VALID_VISIT_ID/tests/$VALID_TEST_ID/results" '{"results": {"value": "normal"}}' "200" "VALID" "Update test results"
        
        # Approval edge cases
        test_edge_case "PATCH" "/visits/$VALID_VISIT_ID/tests/$VALID_TEST_ID/approve" '{}' "400" "VALIDATION" "Empty approval request"
        test_edge_case "PATCH" "/visits/$VALID_VISIT_ID/tests/$VALID_TEST_ID/approve" '{"approvedBy": ""}' "400" "VALIDATION" "Empty approver name"
        test_edge_case "PATCH" "/visits/$VALID_VISIT_ID/tests/$VALID_TEST_ID/approve" '{"approvedBy": "   "}' "400" "VALIDATION" "Whitespace-only approver"
        
        # Valid approval
        test_edge_case "PATCH" "/visits/$VALID_VISIT_ID/tests/$VALID_TEST_ID/approve" '{"approvedBy": "Dr. Smith"}' "200" "VALID" "Approve test results"
        
        # Double approval attempt
        test_edge_case "PATCH" "/visits/$VALID_VISIT_ID/tests/$VALID_TEST_ID/approve" '{"approvedBy": "Dr. Jones"}' "500" "BUSINESS" "Double approval attempt"
        
        # Operations on non-existent test
        test_edge_case "PATCH" "/visits/$VALID_VISIT_ID/tests/999999/results" '{"results": {"value": "test"}}' "404" "VALIDATION" "Update non-existent test results"
        test_edge_case "PATCH" "/visits/$VALID_VISIT_ID/tests/999999/approve" '{"approvedBy": "Dr. Smith"}' "404" "VALIDATION" "Approve non-existent test"
    fi
}

# Test Billing Edge Cases
test_billing_edge_cases() {
    echo -e "${PURPLE}💰 Testing Billing Edge Cases${NC}"
    
    if [ -n "$VALID_VISIT_ID" ]; then
        # The visit should be approved after test approval, so try to generate bill
        bill_response=$(curl -s "$BASE_URL/billing/visits/$VALID_VISIT_ID/bill")
        bill_status=$(echo "$bill_response" | tail -n1)

        if [ "$bill_status" = "200" ]; then
            VALID_BILL_ID=$(extract_id "$bill_response" "billId")

            if [ -n "$VALID_BILL_ID" ]; then
                # Double billing attempt
                test_edge_case "GET" "/billing/visits/$VALID_VISIT_ID/bill" "500" "BUSINESS" "Double billing attempt"

                # Payment operations
                test_edge_case "PATCH" "/billing/$VALID_BILL_ID/pay" "" "200" "VALID" "Mark bill as paid"
                test_edge_case "PATCH" "/billing/$VALID_BILL_ID/pay" "" "200" "IDEMPOTENT" "Mark already paid bill as paid"
            fi
        else
            test_edge_case "GET" "/billing/visits/$VALID_VISIT_ID/bill" "500" "BUSINESS" "Generate bill for non-approved visit"
        fi

        # Operations on non-existent bill
        test_edge_case "GET" "/billing/999999" "404" "VALIDATION" "Get non-existent bill"
        test_edge_case "PATCH" "/billing/999999/pay" "" "404" "VALIDATION" "Pay non-existent bill"
    fi
    
    # Revenue calculation edge cases
    test_edge_case "GET" "/billing/revenue" "400" "VALIDATION" "Revenue without date parameters"
    test_edge_case "GET" "/billing/revenue?startDate=invalid" "500" "VALIDATION" "Invalid start date format"
    test_edge_case "GET" "/billing/revenue?startDate=2025-01-01T00:00:00&endDate=2024-12-31T23:59:59" "400" "BUSINESS" "End date before start date"

    # Valid revenue calculation
    start_date=$(date -v-1m '+%Y-%m-%dT00:00:00')
    end_date=$(date '+%Y-%m-%dT23:59:59')
    test_edge_case "GET" "/billing/revenue?startDate=$start_date&endDate=$end_date" "200" "VALID" "Valid revenue calculation"
}

# Test Search and Filter Edge Cases
test_search_edge_cases() {
    echo -e "${PURPLE}🔍 Testing Search and Filter Edge Cases${NC}"
    
    # Template search edge cases
    test_edge_case "GET" "/test-templates/search" "400" "VALIDATION" "Search without name parameter"
    test_edge_case "GET" "/test-templates/search?name=" "400" "VALIDATION" "Search with empty name"
    test_edge_case "GET" "/test-templates/search?name=   " "400" "VALIDATION" "Search with whitespace-only name"
    test_edge_case "GET" "/test-templates/search?name=NonExistentTemplate" "200" "VALID" "Search for non-existent template"
    
    # Visit filter edge cases
    test_edge_case "GET" "/visits?status=invalid-status" "400" "VALIDATION" "Filter by invalid status"
    test_edge_case "GET" "/visits?status=" "400" "VALIDATION" "Filter with empty status"
    test_edge_case "GET" "/visits?status=pending" "200" "VALID" "Filter by valid status"
    
    # Special characters in search
    test_edge_case "GET" "/test-templates/search?name=Test%20With%20Spaces" "200" "SPECIAL" "Search with URL-encoded spaces"
    test_edge_case "GET" "/test-templates/search?name=Test%26Special" "200" "SPECIAL" "Search with special characters"
}

# Test Concurrent Operations (Simulation)
test_concurrent_edge_cases() {
    echo -e "${PURPLE}⚡ Testing Concurrent Operation Edge Cases${NC}"
    
    if [ -n "$VALID_TEMPLATE_ID" ]; then
        # Simulate concurrent template updates
        template_name="ConcurrentTest_$(date +%s)"
        test_edge_case "PUT" "/test-templates/$VALID_TEMPLATE_ID" "{\"name\": \"${template_name}_1\", \"basePrice\": 100, \"parameters\": {}}" "200" "CONCURRENT" "First concurrent update"
        test_edge_case "PUT" "/test-templates/$VALID_TEMPLATE_ID" "{\"name\": \"${template_name}_2\", \"basePrice\": 200, \"parameters\": {}}" "200" "CONCURRENT" "Second concurrent update"
    fi
}

# Generate HTML Report
generate_html_report() {
    echo -e "${BLUE}📊 Generating Edge Case HTML Report...${NC}"
    
    cat > "$REPORT_FILE" << 'EOF'
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lab Operations API Edge Case Test Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
        .container { max-width: 1400px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .header { text-align: center; color: #333; border-bottom: 2px solid #e74c3c; padding-bottom: 20px; margin-bottom: 30px; }
        .summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 30px; }
        .summary-card { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 8px; text-align: center; }
        .summary-card h3 { margin: 0 0 10px 0; font-size: 2em; }
        .summary-card p { margin: 0; opacity: 0.9; }
        .pass { background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%); }
        .fail { background: linear-gradient(135deg, #f44336 0%, #da190b 100%); }
        .total { background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%); }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #f8f9fa; font-weight: bold; }
        .status-pass { color: #4CAF50; font-weight: bold; }
        .status-fail { color: #f44336; font-weight: bold; }
        .category { padding: 4px 8px; border-radius: 4px; font-size: 0.8em; font-weight: bold; color: white; }
        .category-validation { background: #e74c3c; }
        .category-boundary { background: #f39c12; }
        .category-business { background: #9b59b6; }
        .category-format { background: #34495e; }
        .category-complex { background: #16a085; }
        .category-special { background: #e67e22; }
        .category-concurrent { background: #8e44ad; }
        .category-valid { background: #27ae60; }
        .category-idempotent { background: #3498db; }
        .method { padding: 4px 8px; border-radius: 4px; font-size: 0.8em; font-weight: bold; }
        .method-get { background: #e3f2fd; color: #1976d2; }
        .method-post { background: #e8f5e8; color: #388e3c; }
        .method-patch { background: #fff3e0; color: #f57c00; }
        .method-put { background: #f3e5f5; color: #7b1fa2; }
        .method-delete { background: #ffebee; color: #d32f2f; }
        .response { max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-family: monospace; font-size: 0.9em; }
        .timestamp { text-align: center; color: #666; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🔬 Lab Operations API Edge Case Test Report</h1>
            <p>Comprehensive Boundary and Validation Testing</p>
        </div>
        
        <div class="summary">
EOF

    # Add summary cards with actual values
    cat >> "$REPORT_FILE" << EOF
            <div class="summary-card total">
                <h3>$TOTAL_TESTS</h3>
                <p>Total Edge Cases</p>
            </div>
            <div class="summary-card pass">
                <h3>$PASSED_TESTS</h3>
                <p>Passed</p>
            </div>
            <div class="summary-card fail">
                <h3>$FAILED_TESTS</h3>
                <p>Failed</p>
            </div>
            <div class="summary-card">
                <h3>$(( PASSED_TESTS * 100 / TOTAL_TESTS ))%</h3>
                <p>Success Rate</p>
            </div>
        </div>
        
        <table>
            <thead>
                <tr>
                    <th>#</th>
                    <th>Status</th>
                    <th>Category</th>
                    <th>Description</th>
                    <th>Method</th>
                    <th>Endpoint</th>
                    <th>Status Code</th>
                    <th>Response</th>
                </tr>
            </thead>
            <tbody>
EOF

    # Add test results to HTML
    while IFS='|' read -r test_num status category description method endpoint status_code response; do
        if [ "$test_num" != "Test#" ]; then  # Skip header
            status_class="status-pass"
            if [ "$status" = "FAIL" ]; then
                status_class="status-fail"
            fi
            
            method_class="method-$(echo $method | tr '[:upper:]' '[:lower:]')"
            category_class="category-$(echo $category | tr '[:upper:]' '[:lower:]')"
            
            cat >> "$REPORT_FILE" << EOF
                <tr>
                    <td>$test_num</td>
                    <td class="$status_class">$status</td>
                    <td><span class="category $category_class">$category</span></td>
                    <td>$description</td>
                    <td><span class="method $method_class">$method</span></td>
                    <td><code>$endpoint</code></td>
                    <td>$status_code</td>
                    <td class="response">$response</td>
                </tr>
EOF
        fi
    done < "$RESULTS_FILE"
    
    cat >> "$REPORT_FILE" << EOF
            </tbody>
        </table>
        
        <div class="timestamp">
            <p>Edge Case Report generated on $(date)</p>
            <p>Base URL: $BASE_URL</p>
        </div>
    </div>
</body>
</html>
EOF
}

# Print summary
print_summary() {
    echo ""
    echo -e "${BLUE}📊 EDGE CASE TEST SUMMARY${NC}"
    echo "=========================="
    echo "Total Edge Cases: $TOTAL_TESTS"
    echo -e "${GREEN}Passed: $PASSED_TESTS${NC}"
    echo -e "${RED}Failed: $FAILED_TESTS${NC}"
    echo "Success Rate: $(( PASSED_TESTS * 100 / TOTAL_TESTS ))%"
    echo ""
    echo -e "${GREEN}HTML Report: $REPORT_FILE${NC}"
    echo ""
    
    if [ $FAILED_TESTS -gt 0 ]; then
        echo -e "${RED}⚠️  Some edge case tests failed. This may indicate validation gaps or business logic issues.${NC}"
    else
        echo -e "${GREEN}🎉 All edge case tests passed! The API handles boundary conditions well.${NC}"
    fi
}

# Main execution
main() {
    echo -e "${BLUE}🔬 Starting Lab Operations API Edge Case Testing${NC}"
    echo "Base URL: $BASE_URL"
    echo ""
    
    check_app
    setup_test_data
    
    test_template_edge_cases
    test_visit_edge_cases
    test_lab_test_edge_cases
    test_billing_edge_cases
    test_search_edge_cases
    test_concurrent_edge_cases
    
    generate_html_report
    print_summary
}

# Check if curl is available
if ! command -v curl &> /dev/null; then
    echo "Error: curl is required but not installed."
    exit 1
fi

# Run main function
main "$@"
