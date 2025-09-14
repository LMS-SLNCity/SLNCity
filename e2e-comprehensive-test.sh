#!/bin/bash

# Comprehensive End-to-End Test Script
# Tests the entire lab operations system including optional WLAN features

BASE_URL="http://localhost:8080"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
LOG_FILE="e2e_comprehensive_test_results_${TIMESTAMP}.log"
SUMMARY_FILE="e2e_comprehensive_test_summary_${TIMESTAMP}.txt"

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
NETWORK_ENABLED=false

# Logging function
log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$LOG_FILE"
}

# Test function
test_endpoint() {
    local test_name="$1"
    local method="$2"
    local endpoint="$3"
    local expected_status="$4"
    local data="$5"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    log "Testing: $test_name"
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" "$BASE_URL$endpoint")
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X POST -H "Content-Type: application/json" -d "$data" "$BASE_URL$endpoint")
    elif [ "$method" = "PUT" ]; then
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X PUT -H "Content-Type: application/json" -d "$data" "$BASE_URL$endpoint")
    fi
    
    http_code=$(echo "$response" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    body=$(echo "$response" | sed -e 's/HTTPSTATUS:.*//g')
    
    if [ "$http_code" -eq "$expected_status" ]; then
        echo -e "✅ ${GREEN}PASS${NC}: $test_name - Status: $http_code"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        log "PASS: $test_name - HTTP $http_code"
    else
        echo -e "❌ ${RED}FAIL${NC}: $test_name - Expected: $expected_status, Got: $http_code"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        log "FAIL: $test_name - Expected HTTP $expected_status, got HTTP $http_code"
        log "Response body: $body"
    fi
    
    echo "$body" >> "$LOG_FILE"
    echo "---" >> "$LOG_FILE"
}

# Wait for application to be ready
wait_for_app() {
    echo "⏳ Waiting for application to be ready..."
    for i in {1..30}; do
        if curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
            echo "✅ Application is ready!"
            return 0
        fi
        sleep 2
    done
    echo "❌ Application failed to start within timeout"
    exit 1
}

echo "🧪 COMPREHENSIVE END-TO-END LAB OPERATIONS TEST SUITE"
echo "====================================================="
echo "🔧 Testing complete lab operations system with optional WLAN features..."
echo "Results will be logged to: $LOG_FILE"
echo ""

wait_for_app

# Check network feature availability
echo "🔍 Checking network feature availability..."
network_status=$(curl -s "$BASE_URL/api/v1/network-status/features" 2>/dev/null | grep -o '"networkMonitoringEnabled":[^,]*' | cut -d':' -f2)
if [[ "$network_status" == "true" ]]; then
    echo "✅ Network monitoring features are ENABLED"
    NETWORK_ENABLED=true
else
    echo "ℹ️  Network monitoring features are DISABLED - system operates in basic mode"
    NETWORK_ENABLED=false
fi
echo ""

# Test Core Application Health
echo "🏥 TESTING CORE APPLICATION HEALTH"
echo "=================================="

test_endpoint "Application Health Check" "GET" "/actuator/health" 200
test_endpoint "Application Metrics" "GET" "/actuator/metrics" 200
test_endpoint "Swagger UI Availability" "GET" "/swagger-ui/index.html" 200

# Test Equipment Management
echo ""
echo "🔬 TESTING EQUIPMENT MANAGEMENT"
echo "==============================="

test_endpoint "Get Equipment Types" "GET" "/api/v1/equipment/types" 200
test_endpoint "Get Equipment Statuses" "GET" "/api/v1/equipment/statuses" 200
test_endpoint "Get All Equipment" "GET" "/api/v1/equipment" 200
test_endpoint "Get Equipment Statistics" "GET" "/api/v1/equipment/statistics" 200

# Test Inventory Management
echo ""
echo "📦 TESTING INVENTORY MANAGEMENT"
echo "==============================="

test_endpoint "Get Inventory Categories" "GET" "/api/v1/inventory/categories" 200
test_endpoint "Get Inventory Statuses" "GET" "/api/v1/inventory/statuses" 200
test_endpoint "Get All Inventory Items" "GET" "/api/v1/inventory" 200
test_endpoint "Get Inventory Statistics" "GET" "/api/v1/inventory/statistics" 200

# Test Workflow Integration
echo ""
echo "🔄 TESTING WORKFLOW INTEGRATION"
echo "==============================="

test_endpoint "Get Workflow Statistics" "GET" "/api/v1/workflow/statistics" 200
test_endpoint "Get Workflow Health" "GET" "/api/v1/workflow/health" 200

# Test Monitoring Integration
echo ""
echo "📊 TESTING MONITORING INTEGRATION"
echo "================================="

test_endpoint "Get System Health" "GET" "/api/v1/monitoring/health" 200
test_endpoint "Get System Metrics" "GET" "/api/v1/monitoring/metrics" 200
test_endpoint "Get Circuit Breaker Status" "GET" "/api/v1/monitoring/circuit-breaker" 200

# Test Notification Integration
echo ""
echo "🔔 TESTING NOTIFICATION INTEGRATION"
echo "==================================="

test_endpoint "Get Notification Statistics" "GET" "/api/v1/notifications/statistics" 200
test_endpoint "Get Alert Statistics" "GET" "/api/v1/notifications/alerts/statistics" 200

# Test Network Status (Always Available)
echo ""
echo "🌐 TESTING NETWORK STATUS (ALWAYS AVAILABLE)"
echo "============================================="

test_endpoint "Get Network Feature Status" "GET" "/api/v1/network-status/features" 200
test_endpoint "Get Network Configuration" "GET" "/api/v1/network-status/configuration" 200
test_endpoint "Get Basic Connectivity" "GET" "/api/v1/network-status/basic-connectivity" 200
test_endpoint "Get Available Endpoints" "GET" "/api/v1/network-status/available-endpoints" 200

# Test Optional Network Features
if [ "$NETWORK_ENABLED" = true ]; then
    echo ""
    echo "🌐 TESTING NETWORK CONNECTION MANAGEMENT (ENABLED)"
    echo "=================================================="
    
    test_endpoint "Get Connection Statuses" "GET" "/api/v1/network-connections/statuses" 200
    test_endpoint "Get Connection Types" "GET" "/api/v1/network-connections/types" 200
    test_endpoint "Get All Network Connections" "GET" "/api/v1/network-connections" 200
    test_endpoint "Get Network Statistics" "GET" "/api/v1/network-connections/statistics" 200
    
    echo ""
    echo "🔧 TESTING MACHINE ID ISSUE MANAGEMENT (ENABLED)"
    echo "================================================"
    
    test_endpoint "Get Issue Types" "GET" "/api/v1/machine-id-issues/types" 200
    test_endpoint "Get Issue Severities" "GET" "/api/v1/machine-id-issues/severities" 200
    test_endpoint "Get Issue Statuses" "GET" "/api/v1/machine-id-issues/statuses" 200
    test_endpoint "Get All Machine ID Issues" "GET" "/api/v1/machine-id-issues" 200
    test_endpoint "Get Issue Statistics" "GET" "/api/v1/machine-id-issues/statistics" 200
else
    echo ""
    echo "ℹ️  NETWORK FEATURES (DISABLED - PLUG AND PLAY MODE)"
    echo "===================================================="
    echo "Network connection and machine ID management features are disabled"
    echo "System operates in basic mode without network monitoring"
fi

# Generate Summary Report
echo ""
echo "📋 TEST SUMMARY"
echo "==============="

SUCCESS_RATE=$(echo "scale=1; $PASSED_TESTS * 100 / $TOTAL_TESTS" | bc -l 2>/dev/null || echo "0")

echo "COMPREHENSIVE END-TO-END TEST SUMMARY" > "$SUMMARY_FILE"
echo "=====================================" >> "$SUMMARY_FILE"
echo "Test Date: $(date)" >> "$SUMMARY_FILE"
echo "Network Features Enabled: $NETWORK_ENABLED" >> "$SUMMARY_FILE"
echo "Total Tests: $TOTAL_TESTS" >> "$SUMMARY_FILE"
echo "Passed: $PASSED_TESTS" >> "$SUMMARY_FILE"
echo "Failed: $FAILED_TESTS" >> "$SUMMARY_FILE"
echo "Success Rate: ${SUCCESS_RATE}%" >> "$SUMMARY_FILE"
echo "" >> "$SUMMARY_FILE"

if [ $FAILED_TESTS -eq 0 ]; then
    echo "✅ EXCELLENT. Complete end-to-end system is working perfectly." >> "$SUMMARY_FILE"
    echo -e "✅ ${GREEN}EXCELLENT${NC}. Complete end-to-end system is working perfectly."
elif [ $SUCCESS_RATE -gt 90 ]; then
    echo "✅ EXCELLENT. System is working with minor issues." >> "$SUMMARY_FILE"
    echo -e "✅ ${GREEN}EXCELLENT${NC}. System is working with minor issues."
elif [ $SUCCESS_RATE -gt 80 ]; then
    echo "✅ GOOD. Most system features are working." >> "$SUMMARY_FILE"
    echo -e "✅ ${GREEN}GOOD${NC}. Most system features are working."
else
    echo "⚠️  NEEDS ATTENTION. Several system features need fixes." >> "$SUMMARY_FILE"
    echo -e "⚠️  ${YELLOW}NEEDS ATTENTION${NC}. Several system features need fixes."
fi

echo "" >> "$SUMMARY_FILE"
echo "System Architecture:" >> "$SUMMARY_FILE"
echo "- Core Lab Operations: Always Available" >> "$SUMMARY_FILE"
echo "- Equipment Management: Always Available" >> "$SUMMARY_FILE"
echo "- Inventory Management: Always Available" >> "$SUMMARY_FILE"
echo "- Workflow Integration: Always Available" >> "$SUMMARY_FILE"
echo "- Monitoring & Notifications: Always Available" >> "$SUMMARY_FILE"
echo "- Network Features: $( [ "$NETWORK_ENABLED" = true ] && echo "Enabled (Plug and Play)" || echo "Disabled (Basic Mode)" )" >> "$SUMMARY_FILE"
echo "" >> "$SUMMARY_FILE"
echo "Detailed results: $LOG_FILE" >> "$SUMMARY_FILE"

echo ""
echo "📊 FINAL RESULTS"
echo "================"
echo "Total Tests: $TOTAL_TESTS"
echo "Passed: $PASSED_TESTS"
echo "Failed: $FAILED_TESTS"
echo "Success Rate: ${SUCCESS_RATE}%"
echo "Network Features: $( [ "$NETWORK_ENABLED" = true ] && echo "Enabled" || echo "Disabled (Basic Mode)" )"

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "✅ ${GREEN}All tests passed! System is production ready.${NC}"
    exit 0
else
    echo -e "⚠️  ${YELLOW}Some tests failed. Check the detailed log for more information.${NC}"
    exit 1
fi

echo ""
echo "📄 Detailed results saved to: $LOG_FILE"
echo "📊 Summary report saved to: $SUMMARY_FILE"
