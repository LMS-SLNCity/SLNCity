#!/bin/bash

# Advanced Lab Features Test Script
# Tests equipment management, inventory management, and enhanced analytics

echo "🧪 ADVANCED LAB FEATURES COMPREHENSIVE TEST"
echo "=========================================="

BASE_URL="http://localhost:8080"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
TEST_RESULTS_FILE="advanced_lab_test_results_${TIMESTAMP}.log"

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

# Function to log test results
log_test() {
    local test_name="$1"
    local status="$2"
    local response="$3"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    if [ "$status" = "PASS" ]; then
        PASSED_TESTS=$((PASSED_TESTS + 1))
        echo -e "${GREEN}✅ PASS${NC}: $test_name"
    else
        FAILED_TESTS=$((FAILED_TESTS + 1))
        echo -e "${RED}❌ FAIL${NC}: $test_name"
        echo "   Response: $response"
    fi
    
    echo "[$status] $test_name - $response" >> "$TEST_RESULTS_FILE"
}

# Function to make HTTP request and check response
test_api() {
    local method="$1"
    local endpoint="$2"
    local data="$3"
    local expected_status="$4"
    local test_name="$5"
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X "$method" \
                       -H "Content-Type: application/json" \
                       -d "$data" \
                       "$BASE_URL$endpoint")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$BASE_URL$endpoint")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" = "$expected_status" ]; then
        log_test "$test_name" "PASS" "HTTP $http_code"
        return 0
    else
        log_test "$test_name" "FAIL" "Expected HTTP $expected_status, got HTTP $http_code"
        return 1
    fi
}

echo "🔧 Starting Advanced Lab Features Tests..."
echo "Results will be logged to: $TEST_RESULTS_FILE"
echo ""

# Wait for application to be ready
echo "⏳ Waiting for application to be ready..."
for i in {1..30}; do
    if curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Application is ready!${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${RED}❌ Application failed to start within 30 seconds${NC}"
        exit 1
    fi
    sleep 1
done

echo ""
echo "🔬 TESTING LAB EQUIPMENT MANAGEMENT"
echo "=================================="

# Test 1: Get all equipment types
test_api "GET" "/api/v1/equipment/types" "" "200" "Get Equipment Types"

# Test 2: Get all equipment statuses
test_api "GET" "/api/v1/equipment/statuses" "" "200" "Get Equipment Statuses"

# Test 3: Create new equipment
equipment_data='{
    "name": "Test Analyzer",
    "model": "TA-2024",
    "manufacturer": "Test Corp",
    "serialNumber": "TEST-001-2024",
    "equipmentType": "ANALYZER",
    "location": "Test Lab",
    "status": "ACTIVE"
}'
test_api "POST" "/api/v1/equipment" "$equipment_data" "201" "Create New Equipment"

# Test 4: Get all equipment
test_api "GET" "/api/v1/equipment" "" "200" "Get All Equipment"

# Test 5: Get equipment by status
test_api "GET" "/api/v1/equipment/status/ACTIVE" "" "200" "Get Equipment by Status"

# Test 6: Get equipment by type
test_api "GET" "/api/v1/equipment/type/ANALYZER" "" "200" "Get Equipment by Type"

# Test 7: Search equipment
test_api "GET" "/api/v1/equipment/search?name=Test&page=0&size=10" "" "200" "Search Equipment"

# Test 8: Get equipment requiring maintenance
test_api "GET" "/api/v1/equipment/maintenance-due" "" "200" "Get Equipment Maintenance Due"

# Test 9: Get equipment requiring calibration
test_api "GET" "/api/v1/equipment/calibration-due" "" "200" "Get Equipment Calibration Due"

# Test 10: Get equipment statistics
test_api "GET" "/api/v1/equipment/statistics" "" "200" "Get Equipment Statistics"

echo ""
echo "📦 TESTING INVENTORY MANAGEMENT"
echo "==============================="

# Test 11: Get all inventory categories
test_api "GET" "/api/v1/inventory/categories" "" "200" "Get Inventory Categories"

# Test 12: Get all inventory statuses
test_api "GET" "/api/v1/inventory/statuses" "" "200" "Get Inventory Statuses"

# Test 13: Get transaction types
test_api "GET" "/api/v1/inventory/transaction-types" "" "200" "Get Transaction Types"

# Test 14: Create new inventory item
inventory_data='{
    "name": "Test Reagent",
    "description": "Test reagent for validation",
    "sku": "TEST-REG-001",
    "category": "REAGENTS",
    "unitOfMeasurement": "mL",
    "currentStock": 100,
    "minimumStockLevel": 20,
    "maximumStockLevel": 500,
    "unitCost": 1.50,
    "supplier": "Test Supplier"
}'
test_api "POST" "/api/v1/inventory" "$inventory_data" "201" "Create New Inventory Item"

# Test 15: Get all inventory items
test_api "GET" "/api/v1/inventory" "" "200" "Get All Inventory Items"

# Test 16: Get items by category
test_api "GET" "/api/v1/inventory/category/REAGENTS" "" "200" "Get Items by Category"

# Test 17: Search inventory items
test_api "GET" "/api/v1/inventory/search?name=Test&page=0&size=10" "" "200" "Search Inventory Items"

# Test 18: Get low stock items
test_api "GET" "/api/v1/inventory/low-stock" "" "200" "Get Low Stock Items"

# Test 19: Get items expiring soon
test_api "GET" "/api/v1/inventory/expiring-soon?daysThreshold=30" "" "200" "Get Items Expiring Soon"

# Test 20: Get expired items
test_api "GET" "/api/v1/inventory/expired" "" "200" "Get Expired Items"

# Test 21: Get items requiring reorder
test_api "GET" "/api/v1/inventory/reorder-required" "" "200" "Get Items Requiring Reorder"

# Test 22: Get inventory statistics
test_api "GET" "/api/v1/inventory/statistics" "" "200" "Get Inventory Statistics"

echo ""
echo "🔄 TESTING STOCK OPERATIONS"
echo "==========================="

# First, get an inventory item ID for stock operations
echo "Getting inventory item for stock operations..."
inventory_response=$(curl -s "$BASE_URL/api/v1/inventory")
item_id=$(echo "$inventory_response" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -n "$item_id" ]; then
    echo "Using inventory item ID: $item_id"
    
    # Test 23: Add stock
    test_api "POST" "/api/v1/inventory/$item_id/add-stock?quantity=50&unitCost=1.25&supplier=Test%20Supplier&performedBy=Test%20User" "" "200" "Add Stock to Item"
    
    # Test 24: Remove stock
    test_api "POST" "/api/v1/inventory/$item_id/remove-stock?quantity=10&reason=Testing&performedBy=Test%20User" "" "200" "Remove Stock from Item"
    
    # Test 25: Update stock level
    test_api "PATCH" "/api/v1/inventory/$item_id/stock?newStock=120&transactionType=ADJUSTMENT&reason=Stock%20adjustment&performedBy=Test%20User" "" "200" "Update Stock Level"
else
    echo -e "${YELLOW}⚠️  No inventory items found for stock operations tests${NC}"
    TOTAL_TESTS=$((TOTAL_TESTS + 3))
    FAILED_TESTS=$((FAILED_TESTS + 3))
fi

echo ""
echo "📊 TESTING ENHANCED ANALYTICS"
echo "============================="

# Test 26: Get visit statistics (existing endpoint)
test_api "GET" "/visits/statistics" "" "200" "Get Visit Statistics"

# Test 27: Get billing statistics (existing endpoint)
test_api "GET" "/billing/statistics" "" "200" "Get Billing Statistics"

# Test 28: Test health endpoints
test_api "GET" "/actuator/health" "" "200" "Application Health Check"

# Test 29: Test metrics endpoints
test_api "GET" "/actuator/metrics" "" "200" "Application Metrics"

# Test 30: Test Swagger UI availability
test_api "GET" "/swagger-ui/index.html" "" "200" "Swagger UI Availability"

echo ""
echo "🔍 TESTING FAULT TOLERANCE"
echo "=========================="

# Test 31: Test circuit breaker endpoints
test_api "GET" "/api/v1/monitoring/circuit-breaker" "" "200" "Circuit Breaker Status"

# Test 32: Test rate limiter endpoints
test_api "GET" "/api/v1/monitoring/rate-limiter" "" "200" "Rate Limiter Status"

# Test 33: Test resilient barcode service health
test_api "GET" "/api/v1/resilient/barcodes/health" "" "200" "Resilient Barcode Service Health"

echo ""
echo "🔄 TESTING WORKFLOW INTEGRATION"
echo "==============================="

# Test 34: Test workflow statistics
test_api "GET" "/api/v1/workflow/statistics" "" "200" "Workflow Statistics"

# Test 35: Test workflow health
test_api "GET" "/api/v1/workflow/health" "" "200" "Workflow Health Check"

# Test 36: Test equipment utilization
test_api "GET" "/api/v1/workflow/equipment/utilization" "" "200" "Equipment Utilization"

# Test 37: Test inventory consumption
test_api "GET" "/api/v1/workflow/inventory/consumption" "" "200" "Inventory Consumption"

# Test 38: Test active operations
test_api "GET" "/api/v1/workflow/operations/active" "" "200" "Active Operations"

echo ""
echo "📋 TEST SUMMARY"
echo "==============="
echo -e "Total Tests: ${BLUE}$TOTAL_TESTS${NC}"
echo -e "Passed: ${GREEN}$PASSED_TESTS${NC}"
echo -e "Failed: ${RED}$FAILED_TESTS${NC}"

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}🎉 ALL TESTS PASSED!${NC}"
    success_rate=100
else
    success_rate=$((PASSED_TESTS * 100 / TOTAL_TESTS))
    echo -e "${YELLOW}⚠️  Some tests failed${NC}"
fi

echo -e "Success Rate: ${BLUE}${success_rate}%${NC}"
echo ""
echo "📄 Detailed results saved to: $TEST_RESULTS_FILE"

# Generate summary report
echo "ADVANCED LAB FEATURES TEST SUMMARY" > "advanced_lab_test_summary_${TIMESTAMP}.txt"
echo "=================================" >> "advanced_lab_test_summary_${TIMESTAMP}.txt"
echo "Test Date: $(date)" >> "advanced_lab_test_summary_${TIMESTAMP}.txt"
echo "Total Tests: $TOTAL_TESTS" >> "advanced_lab_test_summary_${TIMESTAMP}.txt"
echo "Passed: $PASSED_TESTS" >> "advanced_lab_test_summary_${TIMESTAMP}.txt"
echo "Failed: $FAILED_TESTS" >> "advanced_lab_test_summary_${TIMESTAMP}.txt"
echo "Success Rate: ${success_rate}%" >> "advanced_lab_test_summary_${TIMESTAMP}.txt"
echo "" >> "advanced_lab_test_summary_${TIMESTAMP}.txt"
echo "Features Tested:" >> "advanced_lab_test_summary_${TIMESTAMP}.txt"
echo "- Lab Equipment Management" >> "advanced_lab_test_summary_${TIMESTAMP}.txt"
echo "- Inventory Management" >> "advanced_lab_test_summary_${TIMESTAMP}.txt"
echo "- Stock Operations" >> "advanced_lab_test_summary_${TIMESTAMP}.txt"
echo "- Enhanced Analytics" >> "advanced_lab_test_summary_${TIMESTAMP}.txt"
echo "- Fault Tolerance" >> "advanced_lab_test_summary_${TIMESTAMP}.txt"
echo "- Workflow Integration" >> "advanced_lab_test_summary_${TIMESTAMP}.txt"

echo "📊 Summary report saved to: advanced_lab_test_summary_${TIMESTAMP}.txt"

if [ $FAILED_TESTS -eq 0 ]; then
    exit 0
else
    exit 1
fi
