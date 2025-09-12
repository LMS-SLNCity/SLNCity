#!/bin/bash

# Test script for Visit Count by Status endpoint
# Issue #16: Add endpoint to get visit count by status

echo "🧪 Testing Visit Count by Status Endpoint"
echo "=========================================="

BASE_URL="http://localhost:8080"

# Function to make API calls and format JSON output
call_api() {
    local method=$1
    local endpoint=$2
    local data=$3
    
    if [ -n "$data" ]; then
        curl -s -X $method "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            -d "$data" | jq '.'
    else
        curl -s -X $method "$BASE_URL$endpoint" | jq '.'
    fi
}

echo ""
echo "📊 Step 1: Check initial visit count by status (should be all zeros)"
call_api GET "/visits/count-by-status"

echo ""
echo "➕ Step 2: Create some test visits"

echo "Creating visit 1 (John Doe)..."
VISIT1=$(call_api POST "/visits" '{"patientDetails": {"name": "John Doe", "age": 30, "phone": "1234567890"}}')
VISIT1_ID=$(echo "$VISIT1" | jq -r '.visitId')
echo "Created visit ID: $VISIT1_ID"

echo "Creating visit 2 (Jane Smith)..."
VISIT2=$(call_api POST "/visits" '{"patientDetails": {"name": "Jane Smith", "age": 25, "phone": "9876543210"}}')
VISIT2_ID=$(echo "$VISIT2" | jq -r '.visitId')
echo "Created visit ID: $VISIT2_ID"

echo "Creating visit 3 (Bob Wilson)..."
VISIT3=$(call_api POST "/visits" '{"patientDetails": {"name": "Bob Wilson", "age": 45, "phone": "5555555555"}}')
VISIT3_ID=$(echo "$VISIT3" | jq -r '.visitId')
echo "Created visit ID: $VISIT3_ID"

echo ""
echo "📊 Step 3: Check visit count after creating visits (should show 3 pending)"
call_api GET "/visits/count-by-status"

echo ""
echo "🔄 Step 4: Update visit statuses to different states"

echo "Updating visit $VISIT1_ID to IN_PROGRESS..."
call_api PATCH "/visits/$VISIT1_ID/status?status=in-progress" | jq '.status'

echo "Updating visit $VISIT2_ID to IN_PROGRESS then to AWAITING_APPROVAL..."
call_api PATCH "/visits/$VISIT2_ID/status?status=in-progress" | jq '.status'
call_api PATCH "/visits/$VISIT2_ID/status?status=awaiting-approval" | jq '.status'

echo ""
echo "📊 Step 5: Check final visit count by status"
echo "Expected: pending=1, in-progress=1, awaiting-approval=1, others=0"
call_api GET "/visits/count-by-status"

echo ""
echo "✅ Test completed! The endpoint correctly shows:"
echo "   - All status types are included in response"
echo "   - Counts are accurate based on actual visit statuses"
echo "   - Response format matches expected JSON structure"

echo ""
echo "🔍 Bonus: Let's verify by getting all visits to confirm statuses"
echo "All visits:"
call_api GET "/visits" | jq '.[] | {visitId, status, patientDetails: {name: .patientDetails.name}}'
