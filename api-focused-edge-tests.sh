#!/bin/bash

# Lab Operations API Focused Edge Case Testing
# Testing critical validation and business logic edge cases

BASE_URL="http://localhost:8080"
REPORT_FILE="focused-edge-test-report-$(date +%Y%m%d-%H%M%S).html"

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
RESULTS_FILE="/tmp/focused_edge_results.txt"
echo "Test#|Status|Category|Description|Method|Endpoint|Expected|Actual|Details" > "$RESULTS_FILE"

# Global test data
TEMPLATE_ID=""
VISIT_ID=""
TEST_ID=""

# Function to extract ID from JSON response
extract_id() {
    local json="$1"
    local field="$2"
    echo "$json" | grep -o "\"$field\":[0-9]*" | cut -d':' -f2 | head -1
}

# Function to test edge case
test_edge() {
    local method="$1"
    local endpoint="$2"
    local data="$3"
    local expected_status="$4"
    local category="$5"
    local description="$6"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    echo -e "${BLUE}[$category] $description${NC}"
    
    # Make the API call
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" "$BASE_URL$endpoint")
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST -H "Content-Type: application/json" -d "$data" "$BASE_URL$endpoint")
    elif [ "$method" = "PATCH" ]; then
        response=$(curl -s -w "\n%{http_code}" -X PATCH -H "Content-Type: application/json" -d "$data" "$BASE_URL$endpoint")
    elif [ "$method" = "PUT" ]; then
        response=$(curl -s -w "\n%{http_code}" -X PUT -H "Content-Type: application/json" -d "$data" "$BASE_URL$endpoint")
    fi
    
    # Extract status code and body
    status_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    # Clean response for logging
    clean_response=$(echo "$body" | tr -d '\n\r' | sed 's/"/\\"/g' | cut -c1-100)
    
    # Check if test passed
    if [ "$status_code" = "$expected_status" ]; then
        echo -e "${GREEN}✅ PASSED${NC} - Expected: $expected_status, Got: $status_code"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        echo "$TOTAL_TESTS|PASS|$category|$description|$method|$endpoint|$expected_status|$status_code|$clean_response" >> "$RESULTS_FILE"
    else
        echo -e "${RED}❌ FAILED${NC} - Expected: $expected_status, Got: $status_code"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        echo "$TOTAL_TESTS|FAIL|$category|$description|$method|$endpoint|$expected_status|$status_code|$clean_response" >> "$RESULTS_FILE"
    fi
    
    echo ""
    return $status_code
}

# Setup test data
setup_test_data() {
    echo -e "${YELLOW}🔧 Setting up test data...${NC}"
    
    # Create template
    template_name="EdgeTest_$(date +%s)"
    template_response=$(curl -s -X POST -H "Content-Type: application/json" \
        -d "{\"name\": \"$template_name\", \"description\": \"Test template\", \"basePrice\": 100.00, \"parameters\": {\"test\": \"value\"}}" \
        "$BASE_URL/test-templates")
    TEMPLATE_ID=$(extract_id "$template_response" "templateId")
    
    # Create visit
    visit_response=$(curl -s -X POST -H "Content-Type: application/json" \
        -d '{"patientDetails": {"name": "Test Patient", "age": 30, "gender": "M", "phone": "1234567890"}}' \
        "$BASE_URL/visits")
    VISIT_ID=$(extract_id "$visit_response" "visitId")
    
    echo "Template ID: $TEMPLATE_ID, Visit ID: $VISIT_ID"
    echo ""
}

# Test Template Edge Cases
test_template_edge_cases() {
    echo -e "${PURPLE}🧪 Template Validation Edge Cases${NC}"
    
    # Critical validation tests
    test_edge "POST" "/test-templates" '{}' "400" "VALIDATION" "Empty request body"
    test_edge "POST" "/test-templates" '{"name": ""}' "400" "VALIDATION" "Empty name field"
    test_edge "POST" "/test-templates" '{"name": "   "}' "400" "VALIDATION" "Whitespace-only name"
    test_edge "POST" "/test-templates" '{"name": "Test", "basePrice": 0}' "400" "VALIDATION" "Zero base price"
    test_edge "POST" "/test-templates" '{"name": "Test", "basePrice": -10}' "400" "VALIDATION" "Negative base price"
    test_edge "POST" "/test-templates" '{"name": "Test", "basePrice": 100}' "400" "VALIDATION" "Missing parameters field"
    test_edge "POST" "/test-templates" '{"name": "Test", "basePrice": 100, "parameters": null}' "500" "VALIDATION" "Null parameters"
    
    # Boundary tests
    TIMESTAMP=$(date +%s)
    test_edge "POST" "/test-templates" "{\"name\": \"Min-$TIMESTAMP\", \"basePrice\": 0.01, \"parameters\": {}}" "201" "BOUNDARY" "Minimum valid values"
    test_edge "POST" "/test-templates" "{\"name\": \"Max-$TIMESTAMP\", \"basePrice\": 99999999.99, \"parameters\": {}}" "201" "BOUNDARY" "Maximum decimal price"
    
    # Duplicate name test
    if [ -n "$TEMPLATE_ID" ]; then
        existing_response=$(curl -s "$BASE_URL/test-templates/$TEMPLATE_ID")
        existing_name=$(echo "$existing_response" | grep -o '"name":"[^"]*"' | cut -d'"' -f4)
        test_edge "POST" "/test-templates" "{\"name\": \"$existing_name\", \"basePrice\": 100, \"parameters\": {}}" "409" "BUSINESS" "Duplicate template name"
    fi
}

# Test Visit Edge Cases
test_visit_edge_cases() {
    echo -e "${PURPLE}🏥 Visit Validation Edge Cases${NC}"
    
    # Validation tests
    test_edge "POST" "/visits" '{}' "400" "VALIDATION" "Empty visit request"
    test_edge "POST" "/visits" '{"patientDetails": null}' "500" "VALIDATION" "Null patient details"
    test_edge "POST" "/visits" '{"patientDetails": {}}' "201" "BOUNDARY" "Empty patient details object"
    
    # Status transition tests
    if [ -n "$VISIT_ID" ]; then
        test_edge "PATCH" "/visits/$VISIT_ID/status?status=invalid-status" "" "400" "VALIDATION" "Invalid status value"
        test_edge "PATCH" "/visits/$VISIT_ID/status?status=" "" "400" "VALIDATION" "Empty status parameter"
        test_edge "PATCH" "/visits/$VISIT_ID/status" "" "400" "VALIDATION" "Missing status parameter"

        # Valid transition
        test_edge "PATCH" "/visits/$VISIT_ID/status?status=in-progress" "" "200" "BUSINESS" "Valid status transition"

        # Invalid transitions
        test_edge "PATCH" "/visits/$VISIT_ID/status?status=completed" "" "500" "BUSINESS" "Invalid status jump"
        test_edge "PATCH" "/visits/$VISIT_ID/status?status=billed" "" "500" "BUSINESS" "Invalid status jump to billed"
    fi
    
    # Non-existent visit tests
    test_edge "GET" "/visits/0" "" "404" "BOUNDARY" "Visit ID zero"
    test_edge "GET" "/visits/-1" "" "404" "BOUNDARY" "Negative visit ID"
    test_edge "GET" "/visits/999999" "" "404" "BOUNDARY" "Very large visit ID"
}

# Test Lab Test Edge Cases
test_lab_test_edge_cases() {
    echo -e "${PURPLE}🔬 Lab Test Edge Cases${NC}"
    
    if [ -n "$VISIT_ID" ] && [ -n "$TEMPLATE_ID" ]; then
        # Validation tests
        test_edge "POST" "/visits/$VISIT_ID/tests" '{}' "400" "VALIDATION" "Empty test request"
        test_edge "POST" "/visits/$VISIT_ID/tests" '{"testTemplateId": null}' "400" "VALIDATION" "Null template ID"
        test_edge "POST" "/visits/$VISIT_ID/tests" '{"testTemplateId": 999999}' "404" "VALIDATION" "Non-existent template ID"
        
        # Add a valid test
        test_response=$(curl -s -X POST -H "Content-Type: application/json" \
            -d "{\"testTemplateId\": $TEMPLATE_ID, \"price\": 100.00}" \
            "$BASE_URL/visits/$VISIT_ID/tests")
        TEST_ID=$(extract_id "$test_response" "testId")
        
        if [ -n "$TEST_ID" ]; then
            # Results validation
            test_edge "PATCH" "/visits/$VISIT_ID/tests/$TEST_ID/results" '{}' "400" "VALIDATION" "Empty results request"
            test_edge "PATCH" "/visits/$VISIT_ID/tests/$TEST_ID/results" '{"results": null}' "500" "VALIDATION" "Null results"
            
            # Valid results update
            test_edge "PATCH" "/visits/$VISIT_ID/tests/$TEST_ID/results" '{"results": {"value": "normal"}}' "200" "VALID" "Update test results"
            
            # Approval validation
            test_edge "PATCH" "/visits/$VISIT_ID/tests/$TEST_ID/approve" '{}' "400" "VALIDATION" "Empty approval request"
            test_edge "PATCH" "/visits/$VISIT_ID/tests/$TEST_ID/approve" '{"approvedBy": ""}' "400" "VALIDATION" "Empty approver name"
            test_edge "PATCH" "/visits/$VISIT_ID/tests/$TEST_ID/approve" '{"approvedBy": "   "}' "400" "VALIDATION" "Whitespace approver"
            
            # Valid approval
            test_edge "PATCH" "/visits/$VISIT_ID/tests/$TEST_ID/approve" '{"approvedBy": "Dr. Smith"}' "200" "BUSINESS" "Approve test results"
            
            # Double approval
            test_edge "PATCH" "/visits/$VISIT_ID/tests/$TEST_ID/approve" '{"approvedBy": "Dr. Jones"}' "500" "BUSINESS" "Double approval attempt"
        fi
    fi
    
    # Non-existent test operations
    test_edge "PATCH" "/visits/999999/tests/999999/results" '{"results": {"value": "test"}}' "404" "VALIDATION" "Update non-existent test"
    test_edge "PATCH" "/visits/999999/tests/999999/approve" '{"approvedBy": "Dr. Smith"}' "404" "VALIDATION" "Approve non-existent test"
}

# Test Search Edge Cases
test_search_edge_cases() {
    echo -e "${PURPLE}🔍 Search Edge Cases${NC}"
    
    # Template search
    test_edge "GET" "/test-templates/search" "" "400" "VALIDATION" "Search without name parameter"
    test_edge "GET" "/test-templates/search?name=" "" "400" "VALIDATION" "Search with empty name"
    test_edge "GET" "/test-templates/search?name=%20%20%20" "" "400" "VALIDATION" "Search with whitespace name"
    test_edge "GET" "/test-templates/search?name=NonExistent" "" "200" "VALID" "Search for non-existent template"
    
    # Visit filtering
    test_edge "GET" "/visits?status=invalid-status" "" "400" "VALIDATION" "Filter by invalid status"
    test_edge "GET" "/visits?status=" "" "400" "VALIDATION" "Filter with empty status"
    test_edge "GET" "/visits?status=pending" "" "200" "VALID" "Filter by valid status"
}

# Test Billing Edge Cases
test_billing_edge_cases() {
    echo -e "${PURPLE}💰 Billing Edge Cases${NC}"
    
    # Revenue calculation
    test_edge "GET" "/billing/revenue" "" "400" "VALIDATION" "Revenue without date parameters"
    test_edge "GET" "/billing/revenue?startDate=invalid" "" "500" "VALIDATION" "Invalid start date format"
    test_edge "GET" "/billing/revenue?startDate=2025-01-01T00:00:00&endDate=2024-12-31T23:59:59" "" "400" "BUSINESS" "End date before start date"
    
    # Valid revenue calculation
    start_date=$(date -v-1m '+%Y-%m-%dT00:00:00')
    end_date=$(date '+%Y-%m-%dT23:59:59')
    test_edge "GET" "/billing/revenue?startDate=$start_date&endDate=$end_date" "" "200" "VALID" "Valid revenue calculation"
    
    # Non-existent bill operations
    test_edge "GET" "/billing/999999" "" "404" "VALIDATION" "Get non-existent bill"
    test_edge "PATCH" "/billing/999999/pay" "" "404" "VALIDATION" "Pay non-existent bill"
}

# Generate HTML report
generate_html_report() {
    echo -e "${BLUE}📊 Generating HTML Report...${NC}"
    
    cat > "$REPORT_FILE" << EOF
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lab Operations API Focused Edge Case Report</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; }
        .container { max-width: 1400px; margin: 0 auto; background: white; border-radius: 15px; box-shadow: 0 20px 40px rgba(0,0,0,0.1); overflow: hidden; }
        .header { background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%); color: white; padding: 40px; text-align: center; }
        .header h1 { margin: 0; font-size: 2.5em; font-weight: 300; }
        .header p { margin: 10px 0 0 0; opacity: 0.9; font-size: 1.2em; }
        .summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; padding: 30px; background: #f8f9fa; }
        .summary-card { background: white; padding: 30px; border-radius: 10px; text-align: center; box-shadow: 0 5px 15px rgba(0,0,0,0.08); }
        .summary-card h3 { margin: 0 0 10px 0; font-size: 2.5em; font-weight: bold; }
        .summary-card p { margin: 0; color: #666; font-size: 1.1em; }
        .total { background: linear-gradient(135deg, #3498db 0%, #2980b9 100%); color: white; }
        .pass { background: linear-gradient(135deg, #2ecc71 0%, #27ae60 100%); color: white; }
        .fail { background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%); color: white; }
        .rate { background: linear-gradient(135deg, #9b59b6 0%, #8e44ad 100%); color: white; }
        .content { padding: 30px; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 5px 15px rgba(0,0,0,0.08); }
        th { background: linear-gradient(135deg, #34495e 0%, #2c3e50 100%); color: white; padding: 15px; text-align: left; font-weight: 600; }
        td { padding: 12px 15px; border-bottom: 1px solid #ecf0f1; }
        tr:hover { background: #f8f9fa; }
        .status-pass { color: #27ae60; font-weight: bold; }
        .status-fail { color: #e74c3c; font-weight: bold; }
        .category { padding: 6px 12px; border-radius: 20px; font-size: 0.85em; font-weight: bold; color: white; display: inline-block; }
        .category-validation { background: #e74c3c; }
        .category-business { background: #9b59b6; }
        .category-boundary { background: #f39c12; }
        .category-valid { background: #27ae60; }
        .method { padding: 4px 8px; border-radius: 4px; font-size: 0.8em; font-weight: bold; }
        .method-get { background: #e3f2fd; color: #1976d2; }
        .method-post { background: #e8f5e8; color: #388e3c; }
        .method-patch { background: #fff3e0; color: #f57c00; }
        .method-put { background: #f3e5f5; color: #7b1fa2; }
        .response { max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-family: 'Courier New', monospace; font-size: 0.85em; background: #f8f9fa; padding: 5px; border-radius: 3px; }
        .footer { text-align: center; padding: 30px; background: #34495e; color: white; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🔬 Focused Edge Case Analysis</h1>
            <p>Lab Operations API Critical Validation Testing</p>
        </div>
        
        <div class="summary">
            <div class="summary-card total">
                <h3>$TOTAL_TESTS</h3>
                <p>Total Tests</p>
            </div>
            <div class="summary-card pass">
                <h3>$PASSED_TESTS</h3>
                <p>Passed</p>
            </div>
            <div class="summary-card fail">
                <h3>$FAILED_TESTS</h3>
                <p>Failed</p>
            </div>
            <div class="summary-card rate">
                <h3>$(( PASSED_TESTS * 100 / TOTAL_TESTS ))%</h3>
                <p>Success Rate</p>
            </div>
        </div>
        
        <div class="content">
            <table>
                <thead>
                    <tr>
                        <th>#</th>
                        <th>Status</th>
                        <th>Category</th>
                        <th>Description</th>
                        <th>Method</th>
                        <th>Endpoint</th>
                        <th>Expected</th>
                        <th>Actual</th>
                        <th>Details</th>
                    </tr>
                </thead>
                <tbody>
EOF

    # Add test results
    while IFS='|' read -r test_num status category description method endpoint expected actual details; do
        if [ "$test_num" != "Test#" ]; then
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
                        <td>$expected</td>
                        <td>$actual</td>
                        <td class="response">$details</td>
                    </tr>
EOF
        fi
    done < "$RESULTS_FILE"
    
    cat >> "$REPORT_FILE" << EOF
                </tbody>
            </table>
        </div>
        
        <div class="footer">
            <p>Focused Edge Case Report generated on $(date)</p>
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
    echo -e "${BLUE}📊 FOCUSED EDGE CASE SUMMARY${NC}"
    echo "================================"
    echo "Total Tests: $TOTAL_TESTS"
    echo -e "${GREEN}Passed: $PASSED_TESTS${NC}"
    echo -e "${RED}Failed: $FAILED_TESTS${NC}"
    echo "Success Rate: $(( PASSED_TESTS * 100 / TOTAL_TESTS ))%"
    echo ""
    echo -e "${GREEN}Report: $REPORT_FILE${NC}"
    echo ""
    
    if [ $FAILED_TESTS -gt 0 ]; then
        echo -e "${RED}⚠️  Some edge cases failed. Review for validation gaps.${NC}"
    else
        echo -e "${GREEN}🎉 All edge cases passed! Excellent validation coverage.${NC}"
    fi
}

# Main execution
main() {
    echo -e "${BLUE}🔬 Lab Operations API Focused Edge Case Testing${NC}"
    echo "Base URL: $BASE_URL"
    echo ""
    
    # Check if app is running
    if ! curl -s "$BASE_URL/actuator/health" > /dev/null; then
        echo -e "${RED}Application not running at $BASE_URL${NC}"
        exit 1
    fi
    
    setup_test_data
    test_template_edge_cases
    test_visit_edge_cases
    test_lab_test_edge_cases
    test_search_edge_cases
    test_billing_edge_cases
    
    generate_html_report
    print_summary
}

# Run main function
main "$@"
