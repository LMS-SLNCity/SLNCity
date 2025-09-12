#!/bin/bash

# Lab Operations API Comprehensive Edge Case Testing
# Focus on critical validation and business logic edge cases

BASE_URL="http://localhost:8080"
REPORT_FILE="comprehensive-edge-test-report-$(date +%Y%m%d-%H%M%S).html"

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
RESULTS_FILE="/tmp/comprehensive_edge_results.txt"
echo "Test#|Status|Category|Description|Method|Endpoint|Expected|Actual|Response" > "$RESULTS_FILE"

# Global test data
TEMPLATE_ID=""
VISIT_ID=""
TEST_ID=""
BILL_ID=""

# Function to extract ID from JSON response
extract_id() {
    local json="$1"
    local field="$2"
    echo "$json" | grep -o "\"$field\":[0-9]*" | cut -d':' -f2 | head -1
}

# Function to test API endpoint with detailed logging
test_comprehensive() {
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
    elif [ "$method" = "DELETE" ]; then
        response=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL$endpoint")
    fi
    
    # Extract status code and body
    status_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    # Clean response for logging
    clean_response=$(echo "$body" | tr -d '\n\r' | sed 's/"/\\"/g')
    
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

# Critical Validation Tests
test_critical_validations() {
    echo -e "${PURPLE}🔍 Critical Validation Tests${NC}"
    
    # Test Template Validations
    test_comprehensive "POST" "/test-templates" '{}' "400" "VALIDATION" "Empty template request"
    test_comprehensive "POST" "/test-templates" '{"name": "", "basePrice": 100, "parameters": {}}' "400" "VALIDATION" "Empty template name"
    test_comprehensive "POST" "/test-templates" '{"name": "Test", "basePrice": 0, "parameters": {}}' "400" "VALIDATION" "Zero base price"
    test_comprehensive "POST" "/test-templates" '{"name": "Test", "basePrice": -10, "parameters": {}}' "400" "VALIDATION" "Negative base price"
    test_comprehensive "POST" "/test-templates" '{"name": "Test", "basePrice": 100}' "400" "VALIDATION" "Missing parameters"
    
    # Visit Validations
    test_comprehensive "POST" "/visits" '{}' "400" "VALIDATION" "Empty visit request"
    test_comprehensive "POST" "/visits" '{"patientDetails": null}' "400" "VALIDATION" "Null patient details"
    
    # Lab Test Validations
    if [ -n "$VISIT_ID" ]; then
        test_comprehensive "POST" "/visits/$VISIT_ID/tests" '{}' "400" "VALIDATION" "Empty test request"
        test_comprehensive "POST" "/visits/$VISIT_ID/tests" '{"testTemplateId": null}' "400" "VALIDATION" "Null template ID"
        test_comprehensive "POST" "/visits/$VISIT_ID/tests" '{"testTemplateId": 999999}' "404" "VALIDATION" "Non-existent template ID"
    fi
}

# Business Logic Tests
test_business_logic() {
    echo -e "${PURPLE}🏢 Business Logic Tests${NC}"
    
    if [ -n "$TEMPLATE_ID" ] && [ -n "$VISIT_ID" ]; then
        # Add test to visit
        test_response=$(curl -s -X POST -H "Content-Type: application/json" \
            -d "{\"testTemplateId\": $TEMPLATE_ID, \"price\": 100.00}" \
            "$BASE_URL/visits/$VISIT_ID/tests")
        TEST_ID=$(extract_id "$test_response" "testId")
        
        if [ -n "$TEST_ID" ]; then
            # Test approval without results
            test_comprehensive "PATCH" "/visits/$VISIT_ID/tests/$TEST_ID/approve" '{"approvedBy": "Dr. Smith"}' "500" "BUSINESS" "Approve test without results"
            
            # Update results first
            curl -s -X PATCH -H "Content-Type: application/json" \
                -d '{"results": {"value": "normal"}}' \
                "$BASE_URL/visits/$VISIT_ID/tests/$TEST_ID/results" > /dev/null
            
            # Now approve should work
            test_comprehensive "PATCH" "/visits/$VISIT_ID/tests/$TEST_ID/approve" '{"approvedBy": "Dr. Smith"}' "200" "BUSINESS" "Approve test with results"
            
            # Double approval
            test_comprehensive "PATCH" "/visits/$VISIT_ID/tests/$TEST_ID/approve" '{"approvedBy": "Dr. Jones"}' "500" "BUSINESS" "Double approval attempt"
        fi
    fi
    
    # Status transition tests
    if [ -n "$VISIT_ID" ]; then
        # Invalid status transitions
        test_comprehensive "PATCH" "/visits/$VISIT_ID/status?status=invalid-status" "400" "BUSINESS" "Invalid status value"
        test_comprehensive "PATCH" "/visits/$VISIT_ID/status?status=completed" "500" "BUSINESS" "Invalid status jump to completed"
        
        # Valid transition
        test_comprehensive "PATCH" "/visits/$VISIT_ID/status?status=in-progress" "200" "BUSINESS" "Valid status transition"
    fi
}

# Boundary Value Tests
test_boundary_values() {
    echo -e "${PURPLE}📏 Boundary Value Tests${NC}"
    
    # Large values
    large_price="99999999.99"
    test_comprehensive "POST" "/test-templates" "{\"name\": \"LargePrice_$(date +%s)\", \"basePrice\": $large_price, \"parameters\": {}}" "201" "BOUNDARY" "Maximum valid price"
    
    # Very long strings
    long_name=$(printf 'A%.0s' {1..255})
    test_comprehensive "POST" "/test-templates" "{\"name\": \"$long_name\", \"basePrice\": 100, \"parameters\": {}}" "201" "BOUNDARY" "Maximum name length"
    
    # Too long strings
    too_long_name=$(printf 'A%.0s' {1..300})
    test_comprehensive "POST" "/test-templates" "{\"name\": \"$too_long_name\", \"basePrice\": 100, \"parameters\": {}}" "500" "BOUNDARY" "Name too long"
    
    # Minimum valid values
    test_comprehensive "POST" "/test-templates" "{\"name\": \"M_$(date +%s)\", \"basePrice\": 0.01, \"parameters\": {}}" "201" "BOUNDARY" "Minimum valid price"
    
    # Edge case IDs
    test_comprehensive "GET" "/visits/0" "" "404" "BOUNDARY" "Zero ID"
    test_comprehensive "GET" "/visits/-1" "" "404" "BOUNDARY" "Negative ID"
    test_comprehensive "GET" "/visits/999999999" "" "404" "BOUNDARY" "Very large ID"
}

# Data Format Tests
test_data_formats() {
    echo -e "${PURPLE}📄 Data Format Tests${NC}"
    
    # Invalid JSON
    test_comprehensive "POST" "/test-templates" '{"name": "Test", "basePrice": 100, "parameters": {invalid}' "400" "FORMAT" "Invalid JSON syntax"
    
    # Special characters
    test_comprehensive "POST" "/visits" '{"patientDetails": {"name": "José María Ñoño", "address": "123 Main St. #4B"}}' "201" "FORMAT" "Special characters"
    
    # Unicode characters
    test_comprehensive "POST" "/visits" '{"patientDetails": {"name": "测试用户", "address": "北京市朝阳区"}}' "201" "FORMAT" "Unicode characters"
    
    # Very large JSON
    large_params='{"param1": "' $(printf 'A%.0s' {1..1000}) '", "param2": "value"}'
    test_comprehensive "POST" "/test-templates" "{\"name\": \"LargeJSON_$(date +%s)\", \"basePrice\": 100, \"parameters\": $large_params}" "201" "FORMAT" "Large JSON parameters"
}

# Concurrent Operation Simulation
test_concurrent_operations() {
    echo -e "${PURPLE}⚡ Concurrent Operation Tests${NC}"
    
    if [ -n "$TEMPLATE_ID" ]; then
        # Simulate rapid updates
        timestamp=$(date +%s)
        test_comprehensive "PUT" "/test-templates/$TEMPLATE_ID" "{\"name\": \"Concurrent1_$timestamp\", \"basePrice\": 100, \"parameters\": {}}" "200" "CONCURRENT" "First rapid update"
        test_comprehensive "PUT" "/test-templates/$TEMPLATE_ID" "{\"name\": \"Concurrent2_$timestamp\", \"basePrice\": 200, \"parameters\": {}}" "200" "CONCURRENT" "Second rapid update"
    fi
}

# Security Tests
test_security_edge_cases() {
    echo -e "${PURPLE}🔒 Security Edge Cases${NC}"
    
    # SQL Injection attempts
    test_comprehensive "GET" "/test-templates/search?name='; DROP TABLE visits; --" "" "200" "SECURITY" "SQL injection in search"
    
    # XSS attempts
    test_comprehensive "POST" "/visits" '{"patientDetails": {"name": "<script>alert(\"xss\")</script>"}}' "201" "SECURITY" "XSS in patient name"
    
    # Very large payloads
    huge_string=$(printf 'A%.0s' {1..10000})
    test_comprehensive "POST" "/visits" "{\"patientDetails\": {\"name\": \"$huge_string\"}}" "201" "SECURITY" "Very large payload"
}

# Generate comprehensive HTML report
generate_comprehensive_report() {
    echo -e "${BLUE}📊 Generating Comprehensive Report...${NC}"
    
    cat > "$REPORT_FILE" << EOF
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lab Operations API Comprehensive Edge Case Report</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; }
        .container { max-width: 1600px; margin: 0 auto; background: white; border-radius: 15px; box-shadow: 0 20px 40px rgba(0,0,0,0.1); overflow: hidden; }
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
        .category-format { background: #34495e; }
        .category-concurrent { background: #8e44ad; }
        .category-security { background: #c0392b; }
        .method { padding: 4px 8px; border-radius: 4px; font-size: 0.8em; font-weight: bold; }
        .method-get { background: #e3f2fd; color: #1976d2; }
        .method-post { background: #e8f5e8; color: #388e3c; }
        .method-patch { background: #fff3e0; color: #f57c00; }
        .method-put { background: #f3e5f5; color: #7b1fa2; }
        .method-delete { background: #ffebee; color: #d32f2f; }
        .response { max-width: 400px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-family: 'Courier New', monospace; font-size: 0.85em; background: #f8f9fa; padding: 5px; border-radius: 3px; }
        .footer { text-align: center; padding: 30px; background: #34495e; color: white; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🔬 Comprehensive Edge Case Analysis</h1>
            <p>Lab Operations API Security & Validation Testing</p>
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
                        <th>Response</th>
                    </tr>
                </thead>
                <tbody>
EOF

    # Add test results
    while IFS='|' read -r test_num status category description method endpoint expected actual response; do
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
                        <td class="response">$response</td>
                    </tr>
EOF
        fi
    done < "$RESULTS_FILE"
    
    cat >> "$REPORT_FILE" << EOF
                </tbody>
            </table>
        </div>
        
        <div class="footer">
            <p>Comprehensive Edge Case Report generated on $(date)</p>
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
    echo -e "${BLUE}📊 COMPREHENSIVE EDGE CASE SUMMARY${NC}"
    echo "===================================="
    echo "Total Tests: $TOTAL_TESTS"
    echo -e "${GREEN}Passed: $PASSED_TESTS${NC}"
    echo -e "${RED}Failed: $FAILED_TESTS${NC}"
    echo "Success Rate: $(( PASSED_TESTS * 100 / TOTAL_TESTS ))%"
    echo ""
    echo -e "${GREEN}Report: $REPORT_FILE${NC}"
    echo ""
    
    if [ $FAILED_TESTS -gt 0 ]; then
        echo -e "${RED}⚠️  Some edge cases failed. Review the report for validation gaps.${NC}"
    else
        echo -e "${GREEN}🎉 All edge cases passed! Excellent API robustness.${NC}"
    fi
}

# Main execution
main() {
    echo -e "${BLUE}🔬 Lab Operations API Comprehensive Edge Case Testing${NC}"
    echo "Base URL: $BASE_URL"
    echo ""
    
    # Check if app is running
    if ! curl -s "$BASE_URL/actuator/health" > /dev/null; then
        echo -e "${RED}Application not running at $BASE_URL${NC}"
        exit 1
    fi
    
    setup_test_data
    test_critical_validations
    test_business_logic
    test_boundary_values
    test_data_formats
    test_concurrent_operations
    test_security_edge_cases
    
    generate_comprehensive_report
    print_summary
}

# Run main function
main "$@"
