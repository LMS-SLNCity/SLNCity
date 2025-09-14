#!/bin/bash

# Comprehensive Lab Operations System Test
# Tests all features including new audit trail, notifications, and caching

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test configuration
BASE_URL="http://localhost:8080"
LOG_FILE="comprehensive_test_results_$(date +%Y%m%d_%H%M%S).log"
SUMMARY_FILE="comprehensive_test_summary_$(date +%Y%m%d_%H%M%S).txt"

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Function to log test results
log_test() {
    local test_name="$1"
    local status="$2"
    local details="$3"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    if [ "$status" = "PASS" ]; then
        PASSED_TESTS=$((PASSED_TESTS + 1))
        echo -e "${GREEN}✅ PASS${NC}: $test_name" | tee -a "$LOG_FILE"
    else
        FAILED_TESTS=$((FAILED_TESTS + 1))
        echo -e "${RED}❌ FAIL${NC}: $test_name" | tee -a "$LOG_FILE"
        if [ -n "$details" ]; then
            echo -e "   ${YELLOW}Details${NC}: $details" | tee -a "$LOG_FILE"
        fi
    fi
}

# Function to test HTTP endpoint
test_endpoint() {
    local name="$1"
    local url="$2"
    local expected_status="$3"
    local method="${4:-GET}"
    local data="$5"
    
    echo "Testing: $name" >> "$LOG_FILE"
    echo "URL: $url" >> "$LOG_FILE"
    echo "Method: $method" >> "$LOG_FILE"
    
    if [ "$method" = "POST" ] && [ -n "$data" ]; then
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X POST -H "Content-Type: application/x-www-form-urlencoded" -d "$data" "$url" 2>/dev/null || echo "HTTPSTATUS:000")
    else
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X "$method" "$url" 2>/dev/null || echo "HTTPSTATUS:000")
    fi
    
    http_status=$(echo "$response" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
    body=$(echo "$response" | sed 's/HTTPSTATUS:[0-9]*$//')
    
    echo "Response Status: $http_status" >> "$LOG_FILE"
    echo "Response Body: $body" | head -5 >> "$LOG_FILE"
    echo "---" >> "$LOG_FILE"
    
    if [ "$http_status" = "$expected_status" ]; then
        log_test "$name" "PASS"
    else
        log_test "$name" "FAIL" "Expected HTTP $expected_status, got HTTP $http_status"
    fi
}

# Function to wait for application to be ready
wait_for_app() {
    echo -e "${BLUE}⏳ Waiting for application to be ready...${NC}"
    
    for i in {1..30}; do
        if curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
            echo -e "${GREEN}✅ Application is ready!${NC}"
            return 0
        fi
        echo "Attempt $i/30: Application not ready yet..."
        sleep 2
    done
    
    echo -e "${RED}❌ Application failed to start within timeout${NC}"
    exit 1
}

# Main test execution
main() {
    echo -e "${BLUE}🧪 COMPREHENSIVE LAB OPERATIONS SYSTEM TEST${NC}"
    echo "=========================================="
    echo -e "${BLUE}🔧 Starting Comprehensive System Tests...${NC}"
    echo "Results will be logged to: $LOG_FILE"
    echo ""
    
    # Wait for application
    wait_for_app
    
    echo -e "${BLUE}🔬 TESTING CORE LAB OPERATIONS${NC}"
    echo "=============================="
    
    # Core system health
    test_endpoint "Application Health Check" "$BASE_URL/actuator/health" "200"
    test_endpoint "Application Metrics" "$BASE_URL/actuator/metrics" "200"
    test_endpoint "Swagger UI Availability" "$BASE_URL/swagger-ui/index.html" "200"
    
    echo -e "${BLUE}🔬 TESTING EQUIPMENT MANAGEMENT${NC}"
    echo "==============================="
    
    # Equipment management tests
    test_endpoint "Get Equipment Types" "$BASE_URL/api/v1/equipment/types" "200"
    test_endpoint "Get Equipment Statuses" "$BASE_URL/api/v1/equipment/statuses" "200"
    test_endpoint "Get All Equipment" "$BASE_URL/api/v1/equipment" "200"
    test_endpoint "Get Equipment Statistics" "$BASE_URL/api/v1/equipment/statistics" "200"
    
    echo -e "${BLUE}📦 TESTING INVENTORY MANAGEMENT${NC}"
    echo "==============================="
    
    # Inventory management tests
    test_endpoint "Get Inventory Categories" "$BASE_URL/api/v1/inventory/categories" "200"
    test_endpoint "Get Inventory Statuses" "$BASE_URL/api/v1/inventory/statuses" "200"
    test_endpoint "Get All Inventory Items" "$BASE_URL/api/v1/inventory" "200"
    test_endpoint "Get Inventory Statistics" "$BASE_URL/api/v1/inventory/statistics" "200"
    
    echo -e "${BLUE}📊 TESTING ANALYTICS AND MONITORING${NC}"
    echo "===================================="
    
    # Analytics and monitoring tests
    test_endpoint "Get Visit Statistics" "$BASE_URL/api/v1/visits/statistics" "200"
    test_endpoint "Get Billing Statistics" "$BASE_URL/api/v1/billing/statistics" "200"
    test_endpoint "Circuit Breaker Status" "$BASE_URL/api/v1/monitoring/circuit-breaker" "200"
    test_endpoint "Rate Limiter Status" "$BASE_URL/api/v1/monitoring/rate-limiter" "200"
    test_endpoint "System Health Status" "$BASE_URL/api/v1/monitoring/health" "200"
    
    echo -e "${BLUE}🔄 TESTING WORKFLOW INTEGRATION${NC}"
    echo "==============================="
    
    # Workflow integration tests
    test_endpoint "Workflow Statistics" "$BASE_URL/api/v1/workflow/statistics" "200"
    test_endpoint "Workflow Health Check" "$BASE_URL/api/v1/workflow/health" "200"
    test_endpoint "Equipment Utilization" "$BASE_URL/api/v1/workflow/equipment/utilization" "200"
    test_endpoint "Inventory Consumption" "$BASE_URL/api/v1/workflow/inventory/consumption" "200"
    test_endpoint "Active Operations" "$BASE_URL/api/v1/workflow/operations/active" "200"
    
    echo -e "${BLUE}🔔 TESTING NOTIFICATION SYSTEM${NC}"
    echo "==============================="
    
    # Notification system tests
    test_endpoint "Get Notification Statistics" "$BASE_URL/api/v1/notifications/statistics" "200"
    test_endpoint "Get Alert Statistics" "$BASE_URL/api/v1/notifications/alerts/statistics" "200"
    test_endpoint "Get Active Alerts" "$BASE_URL/api/v1/notifications/alerts" "200"
    test_endpoint "Get Critical Alerts" "$BASE_URL/api/v1/notifications/alerts/critical" "200"
    test_endpoint "Get Unacknowledged Alerts" "$BASE_URL/api/v1/notifications/alerts/unacknowledged" "200"
    
    # Test notification creation
    test_endpoint "Create System Notification" "$BASE_URL/api/v1/notifications/system-wide" "201" "POST" "type=SYSTEM&title=Test%20Notification&message=This%20is%20a%20test%20notification&severity=INFO"
    test_endpoint "Create System Alert" "$BASE_URL/api/v1/notifications/alerts" "201" "POST" "alertType=SYSTEM&alertCode=TEST001&title=Test%20Alert&description=This%20is%20a%20test%20alert&severity=LOW"
    
    # Test user-specific notifications
    test_endpoint "Get User Notifications" "$BASE_URL/api/v1/notifications/user/testuser" "200"
    test_endpoint "Get Unread User Notifications" "$BASE_URL/api/v1/notifications/user/testuser/unread" "200"
    test_endpoint "Count Unread User Notifications" "$BASE_URL/api/v1/notifications/user/testuser/count" "200"
    
    echo -e "${BLUE}🔍 TESTING FAULT TOLERANCE${NC}"
    echo "=========================="
    
    # Fault tolerance tests
    test_endpoint "Resilient Barcode Service Health" "$BASE_URL/api/v1/resilient/barcode/health" "200"
    test_endpoint "Resilient Database Service Health" "$BASE_URL/api/v1/resilient/database/health" "200"
    test_endpoint "Resilient PDF Service Health" "$BASE_URL/api/v1/resilient/pdf/health" "200"
    
    echo -e "${BLUE}🏥 TESTING CORE LAB WORKFLOWS${NC}"
    echo "============================="
    
    # Core lab workflow tests
    test_endpoint "Get Test Templates" "$BASE_URL/api/v1/test-templates" "200"
    test_endpoint "Get Sample Types" "$BASE_URL/api/v1/samples/types" "200"
    test_endpoint "Get Visit Statuses" "$BASE_URL/api/v1/visits/statuses" "200"
    
    echo -e "${BLUE}📋 TEST SUMMARY${NC}"
    echo "==============="
    
    # Calculate success rate
    if [ $TOTAL_TESTS -gt 0 ]; then
        SUCCESS_RATE=$(( (PASSED_TESTS * 100) / TOTAL_TESTS ))
    else
        SUCCESS_RATE=0
    fi
    
    # Create summary
    {
        echo "COMPREHENSIVE LAB OPERATIONS SYSTEM TEST SUMMARY"
        echo "================================================"
        echo "Test Date: $(date)"
        echo "Total Tests: $TOTAL_TESTS"
        echo "Passed: $PASSED_TESTS"
        echo "Failed: $FAILED_TESTS"
        echo "Success Rate: $SUCCESS_RATE%"
        echo ""
        
        if [ $SUCCESS_RATE -eq 100 ]; then
            echo "🎉 ALL TESTS PASSED!"
            echo "The system is fully operational with all features working correctly."
        elif [ $SUCCESS_RATE -ge 90 ]; then
            echo "✅ EXCELLENT! Most tests passed."
            echo "The system is highly functional with minor issues."
        elif [ $SUCCESS_RATE -ge 75 ]; then
            echo "⚠️  GOOD. Most core functionality is working."
            echo "Some features may need attention."
        else
            echo "❌ NEEDS ATTENTION. Multiple test failures detected."
            echo "System requires investigation and fixes."
        fi
        
        echo ""
        echo "Detailed results: $LOG_FILE"
        echo "Summary report: $SUMMARY_FILE"
    } | tee "$SUMMARY_FILE"
    
    # Display final results
    echo ""
    echo -e "${BLUE}📊 FINAL RESULTS${NC}"
    echo "================"
    echo -e "Total Tests: ${BLUE}$TOTAL_TESTS${NC}"
    echo -e "Passed: ${GREEN}$PASSED_TESTS${NC}"
    echo -e "Failed: ${RED}$FAILED_TESTS${NC}"
    echo -e "Success Rate: ${YELLOW}$SUCCESS_RATE%${NC}"
    
    if [ $SUCCESS_RATE -eq 100 ]; then
        echo -e "${GREEN}🎉 ALL TESTS PASSED!${NC}"
        echo -e "${GREEN}The comprehensive lab operations system is fully operational!${NC}"
    else
        echo -e "${YELLOW}⚠️  Some tests failed. Check the detailed log for more information.${NC}"
    fi
    
    echo ""
    echo -e "${BLUE}📄 Detailed results saved to: $LOG_FILE${NC}"
    echo -e "${BLUE}📊 Summary report saved to: $SUMMARY_FILE${NC}"
    
    # Exit with appropriate code
    if [ $SUCCESS_RATE -eq 100 ]; then
        exit 0
    else
        exit 1
    fi
}

# Run main function
main "$@"
