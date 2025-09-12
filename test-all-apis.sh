#!/bin/bash

# Lab Operations API Test Suite
# This script tests all API endpoints and generates a comprehensive report

# Configuration
BASE_URL="http://localhost:8080"
REPORT_FILE="api-test-report-$(date +%Y%m%d-%H%M%S).html"
TEMP_DIR="/tmp/lab-api-test"
LOG_FILE="$TEMP_DIR/api-test.log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Create temp directory
mkdir -p "$TEMP_DIR"

# Function to log messages
log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" >> "$LOG_FILE"
    echo -e "$1"
}

# Function to test API endpoint
test_api() {
    local method="$1"
    local endpoint="$2"
    local data="$3"
    local expected_status="$4"
    local description="$5"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    log "${BLUE}Testing: $description${NC}"
    log "  Method: $method"
    log "  Endpoint: $endpoint"
    
    # Make the API call
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" "$BASE_URL$endpoint")
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST -H "Content-Type: application/json" -d "$data" "$BASE_URL$endpoint")
    elif [ "$method" = "PATCH" ]; then
        response=$(curl -s -w "\n%{http_code}" -X PATCH -H "Content-Type: application/json" -d "$data" "$BASE_URL$endpoint")
    elif [ "$method" = "DELETE" ]; then
        response=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL$endpoint")
    fi
    
    # Extract status code and body
    status_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    # Check if test passed
    if [ "$status_code" = "$expected_status" ]; then
        log "${GREEN}✅ PASSED${NC} - Status: $status_code"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        echo "$TOTAL_TESTS|PASS|$description|$method|$endpoint|$status_code|$body" >> "$TEMP_DIR/results.csv"
    else
        log "${RED}❌ FAILED${NC} - Expected: $expected_status, Got: $status_code"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        echo "$TOTAL_TESTS|FAIL|$description|$method|$endpoint|$status_code|$body" >> "$TEMP_DIR/results.csv"
    fi
    
    log "  Response: $body"
    log ""
    
    # Store response for later use
    echo "$body" > "$TEMP_DIR/response_$TOTAL_TESTS.json"
    
    # Return the response body for chaining
    echo "$body"
}

# Function to extract ID from JSON response
extract_id() {
    local json="$1"
    local field="$2"
    echo "$json" | grep -o "\"$field\":[0-9]*" | cut -d':' -f2 | head -1
}

# Start the application if not running
check_app() {
    log "${YELLOW}Checking if application is running...${NC}"
    if ! curl -s "$BASE_URL/actuator/health" > /dev/null; then
        log "${RED}Application is not running at $BASE_URL${NC}"
        log "Please start the application first: mvn spring-boot:run"
        exit 1
    fi
    log "${GREEN}Application is running!${NC}"
    log ""
}

# Initialize CSV results file
init_results() {
    echo "Test#|Status|Description|Method|Endpoint|StatusCode|Response" > "$TEMP_DIR/results.csv"
}

# Main test execution
run_tests() {
    log "${BLUE}🚀 Starting Lab Operations API Test Suite${NC}"
    log "Base URL: $BASE_URL"
    log "Report will be saved to: $REPORT_FILE"
    log ""
    
    # Health Check
    test_api "GET" "/actuator/health" "" "200" "Health Check"
    
    # Test Template APIs
    log "${YELLOW}📋 Testing Test Template APIs${NC}"
    template_response=$(test_api "POST" "/test-templates" '{"name": "Complete Blood Count", "description": "Full blood analysis including RBC, WBC, platelets", "basePrice": 500.00, "parameters": {"hemoglobin": {"type": "number", "unit": "g/dL", "normalRange": "12-16"}, "wbc_count": {"type": "number", "unit": "cells/μL", "normalRange": "4000-11000"}}}' "201" "Create Test Template")
    
    TEMPLATE_ID=$(extract_id "$template_response" "templateId")
    log "Extracted Template ID: $TEMPLATE_ID"
    
    test_api "GET" "/test-templates/$TEMPLATE_ID" "" "200" "Get Test Template by ID"
    test_api "GET" "/test-templates" "" "200" "Get All Test Templates"
    test_api "GET" "/test-templates/search?name=Complete" "" "200" "Search Test Templates"
    
    test_api "PATCH" "/test-templates/$TEMPLATE_ID" '{"name": "Complete Blood Count - Updated", "description": "Updated description", "basePrice": 550.00, "parameters": {"hemoglobin": {"type": "number", "unit": "g/dL", "normalRange": "12-16"}}}' "200" "Update Test Template"
    
    # Visit APIs
    log "${YELLOW}🏥 Testing Visit APIs${NC}"
    visit_response=$(test_api "POST" "/visits" '{"patientDetails": {"name": "John Doe", "age": 35, "gender": "M", "phone": "9876543210", "address": "123 Main St, Hyderabad", "email": "john.doe@example.com"}}' "201" "Create Visit")
    
    VISIT_ID=$(extract_id "$visit_response" "visitId")
    log "Extracted Visit ID: $VISIT_ID"
    
    test_api "GET" "/visits/$VISIT_ID" "" "200" "Get Visit by ID"
    test_api "GET" "/visits" "" "200" "Get All Visits"
    test_api "GET" "/visits?status=pending" "" "200" "Filter Visits by Status"
    test_api "GET" "/visits/search?phone=9876543210" "" "200" "Search Visits by Phone"
    test_api "PATCH" "/visits/$VISIT_ID/status?status=in_progress" "" "200" "Update Visit Status"
}


    # Lab Test APIs
    log "${YELLOW}🧪 Testing Lab Test APIs${NC}"
    test_response=$(test_api "POST" "/visits/$VISIT_ID/tests" "{\"testTemplateId\": $TEMPLATE_ID, \"price\": 500.00}" "201" "Add Test to Visit")

    TEST_ID=$(extract_id "$test_response" "testId")

    test_api "GET" "/visits/$VISIT_ID/tests" "" "200" "Get Tests for Visit"

    test_api "PATCH" "/visits/$VISIT_ID/tests/$TEST_ID/results" '{"results": {"hemoglobin": "14.2 g/dL", "wbc_count": "7500 cells/μL", "conclusion": "All values within normal range"}}' "200" "Update Test Results"

    test_api "PATCH" "/visits/$VISIT_ID/tests/$TEST_ID/approve" '{"approvedBy": "Dr. Smith"}' "200" "Approve Test Results"

    # Billing APIs
    log "${YELLOW}💰 Testing Billing APIs${NC}"
    bill_response=$(test_api "GET" "/billing/visits/$VISIT_ID/bill" "" "200" "Generate Bill")

    BILL_ID=$(extract_id "$bill_response" "billId")

    test_api "GET" "/billing/$BILL_ID" "" "200" "Get Bill by ID"
    test_api "GET" "/billing" "" "200" "Get All Bills"
    test_api "GET" "/billing/unpaid" "" "200" "Get Unpaid Bills"

    test_api "PATCH" "/billing/$BILL_ID/pay" "" "200" "Mark Bill as Paid"

    # Revenue API (macOS compatible date)
    start_date=$(date -v-1m '+%Y-%m-%dT00:00:00')
    end_date=$(date '+%Y-%m-%dT23:59:59')
    test_api "GET" "/billing/revenue?startDate=$start_date&endDate=$end_date" "" "200" "Get Revenue for Period"

    # Error Handling Tests
    log "${YELLOW}⚠️ Testing Error Handling${NC}"
    test_api "GET" "/visits/99999" "" "404" "Get Non-existent Visit"
    test_api "GET" "/test-templates/99999" "" "404" "Get Non-existent Test Template"
    test_api "GET" "/billing/99999" "" "404" "Get Non-existent Bill"

    test_api "POST" "/visits" '{"patientDetails": {}}' "400" "Create Visit with Invalid Data"

    test_api "POST" "/test-templates" '{"name": "", "basePrice": -100, "parameters": {}}' "400" "Create Test Template with Invalid Data"

    test_api "PATCH" "/visits/$VISIT_ID/status?status=invalid_status" "" "400" "Update Visit with Invalid Status"
}

# Generate HTML Report
generate_report() {
    log "${BLUE}📊 Generating HTML Report...${NC}"

    cat > "$REPORT_FILE" << EOF
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lab Operations API Test Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .header { text-align: center; color: #333; border-bottom: 2px solid #007bff; padding-bottom: 20px; margin-bottom: 30px; }
        .summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 30px; }
        .summary-card { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 8px; text-align: center; }
        .summary-card h3 { margin: 0 0 10px 0; font-size: 2em; }
        .summary-card p { margin: 0; opacity: 0.9; }
        .pass { background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%); }
        .fail { background: linear-gradient(135deg, #f44336 0%, #da190b 100%); }
        .total { background: linear-gradient(135deg, #2196F3 0%, #1976D2 100%); }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #f8f9fa; font-weight: bold; }
        .status-pass { color: #4CAF50; font-weight: bold; }
        .status-fail { color: #f44336; font-weight: bold; }
        .method { padding: 4px 8px; border-radius: 4px; font-size: 0.8em; font-weight: bold; }
        .method-get { background: #e3f2fd; color: #1976d2; }
        .method-post { background: #e8f5e8; color: #388e3c; }
        .method-patch { background: #fff3e0; color: #f57c00; }
        .method-delete { background: #ffebee; color: #d32f2f; }
        .response { max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-family: monospace; font-size: 0.9em; }
        .timestamp { text-align: center; color: #666; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; }
        .section { margin: 30px 0; }
        .section h2 { color: #333; border-left: 4px solid #007bff; padding-left: 15px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🧪 Lab Operations API Test Report</h1>
            <p>Comprehensive API Testing Results</p>
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
            <div class="summary-card">
                <h3>$(( PASSED_TESTS * 100 / TOTAL_TESTS ))%</h3>
                <p>Success Rate</p>
            </div>
        </div>

        <div class="section">
            <h2>📋 Test Results</h2>
            <table>
                <thead>
                    <tr>
                        <th>#</th>
                        <th>Status</th>
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
    while IFS='|' read -r test_num status description method endpoint status_code response; do
        if [ "$test_num" != "Test#" ]; then  # Skip header
            status_class="status-pass"
            if [ "$status" = "FAIL" ]; then
                status_class="status-fail"
            fi

            method_class="method-$(echo $method | tr '[:upper:]' '[:lower:]')"

            cat >> "$REPORT_FILE" << EOF
                    <tr>
                        <td>$test_num</td>
                        <td class="$status_class">$status</td>
                        <td>$description</td>
                        <td><span class="method $method_class">$method</span></td>
                        <td><code>$endpoint</code></td>
                        <td>$status_code</td>
                        <td class="response">$response</td>
                    </tr>
EOF
        fi
    done < "$TEMP_DIR/results.csv"

    cat >> "$REPORT_FILE" << EOF
                </tbody>
            </table>
        </div>

        <div class="timestamp">
            <p>Report generated on $(date)</p>
            <p>Base URL: $BASE_URL</p>
        </div>
    </div>
</body>
</html>
EOF
}

# Generate JSON Report
generate_json_report() {
    log "${BLUE}📄 Generating JSON Report...${NC}"

    JSON_REPORT="api-test-report-$(date +%Y%m%d-%H%M%S).json"

    cat > "$JSON_REPORT" << EOF
{
    "testSuite": "Lab Operations API Test Suite",
    "timestamp": "$(date -Iseconds)",
    "baseUrl": "$BASE_URL",
    "summary": {
        "totalTests": $TOTAL_TESTS,
        "passedTests": $PASSED_TESTS,
        "failedTests": $FAILED_TESTS,
        "successRate": $(( PASSED_TESTS * 100 / TOTAL_TESTS ))
    },
    "results": [
EOF

    first=true
    while IFS='|' read -r test_num status description method endpoint status_code response; do
        if [ "$test_num" != "Test#" ]; then  # Skip header
            if [ "$first" = true ]; then
                first=false
            else
                echo "," >> "$JSON_REPORT"
            fi

            # Escape JSON strings
            description=$(echo "$description" | sed 's/"/\\"/g')
            response=$(echo "$response" | sed 's/"/\\"/g' | tr -d '\n\r')

            cat >> "$JSON_REPORT" << EOF
        {
            "testNumber": $test_num,
            "status": "$status",
            "description": "$description",
            "method": "$method",
            "endpoint": "$endpoint",
            "statusCode": "$status_code",
            "response": "$response"
        }EOF
        fi
    done < "$TEMP_DIR/results.csv"

    cat >> "$JSON_REPORT" << EOF

    ]
}
EOF

    log "${GREEN}JSON Report saved to: $JSON_REPORT${NC}"
}

# Print summary
print_summary() {
    log ""
    log "${BLUE}📊 TEST SUMMARY${NC}"
    log "================="
    log "Total Tests: $TOTAL_TESTS"
    log "${GREEN}Passed: $PASSED_TESTS${NC}"
    log "${RED}Failed: $FAILED_TESTS${NC}"
    log "Success Rate: $(( PASSED_TESTS * 100 / TOTAL_TESTS ))%"
    log ""
    log "${GREEN}HTML Report: $REPORT_FILE${NC}"
    log "${GREEN}JSON Report: api-test-report-$(date +%Y%m%d-%H%M%S).json${NC}"
    log "${GREEN}Log File: $LOG_FILE${NC}"
    log ""

    if [ $FAILED_TESTS -gt 0 ]; then
        log "${RED}⚠️  Some tests failed. Please check the report for details.${NC}"
        exit 1
    else
        log "${GREEN}🎉 All tests passed successfully!${NC}"
    fi
}

# Cleanup function
cleanup() {
    log "${YELLOW}🧹 Cleaning up temporary files...${NC}"
    # Keep the temp directory for debugging, but clean up old ones
    find /tmp -name "lab-api-test-*" -type d -mtime +7 -exec rm -rf {} \; 2>/dev/null || true
}

# Main execution
main() {
    log "${BLUE}🚀 Lab Operations API Test Suite${NC}"
    log "=================================="
    log ""

    # Check if application is running
    check_app

    # Initialize
    init_results

    # Run all tests
    run_tests

    # Generate reports
    generate_report
    generate_json_report

    # Print summary
    print_summary

    # Cleanup
    cleanup
}

# Handle script interruption
trap cleanup EXIT

# Check if curl is available
if ! command -v curl &> /dev/null; then
    echo "Error: curl is required but not installed."
    exit 1
fi

# Run main function
main "$@"
