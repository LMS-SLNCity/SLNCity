#!/bin/bash

echo "🔍 UI DIAGNOSIS - Lab Operations System"
echo "======================================="

# Create results directory
mkdir -p ui-diagnosis
cd ui-diagnosis

echo "🌐 Testing server status..."
SERVER_STATUS=$(curl -s -w "%{http_code}" "http://localhost:8080" -o /dev/null)
echo "Server HTTP Status: $SERVER_STATUS"

if [ "$SERVER_STATUS" != "200" ] && [ "$SERVER_STATUS" != "302" ]; then
    echo "❌ Server is not accessible"
    exit 1
fi

echo ""
echo "🧪 Testing Admin Dashboard..."
ADMIN_STATUS=$(curl -s -w "%{http_code}" "http://localhost:8080/admin/dashboard.html" -o admin-response.html)
echo "Admin Dashboard HTTP Status: $ADMIN_STATUS"

echo ""
echo "🧪 Testing Reception Dashboard..."
RECEPTION_STATUS=$(curl -s -w "%{http_code}" "http://localhost:8080/reception/dashboard.html" -o reception-response.html)
echo "Reception Dashboard HTTP Status: $RECEPTION_STATUS"

echo ""
echo "🧪 Testing Phlebotomy Dashboard..."
PHLEBOTOMY_STATUS=$(curl -s -w "%{http_code}" "http://localhost:8080/phlebotomy/dashboard.html" -o phlebotomy-response.html)
echo "Phlebotomy Dashboard HTTP Status: $PHLEBOTOMY_STATUS"

echo ""
echo "🧪 Testing Lab Technician Dashboard..."
TECHNICIAN_STATUS=$(curl -s -w "%{http_code}" "http://localhost:8080/technician/dashboard.html" -o technician-response.html)
echo "Lab Technician Dashboard HTTP Status: $TECHNICIAN_STATUS"

echo ""
echo "📊 ANALYSIS RESULTS"
echo "=================="

# Function to analyze HTML file
analyze_html() {
    local file=$1
    local name=$2
    local status=$3
    
    echo ""
    echo "🔍 $name Dashboard Analysis:"
    echo "HTTP Status: $status"
    
    if [ "$status" = "200" ]; then
        echo "✅ Page loads successfully"
        
        # Check title
        if grep -q "<title>" "$file"; then
            TITLE=$(grep -o "<title>[^<]*</title>" "$file" | sed 's/<[^>]*>//g')
            echo "📄 Title: '$TITLE'"
        else
            echo "❌ Missing page title"
        fi
        
        # Check CSS
        CSS_COUNT=$(grep -c 'rel="stylesheet"' "$file" 2>/dev/null || echo "0")
        echo "🎨 CSS files: $CSS_COUNT"
        
        # Check JavaScript
        JS_COUNT=$(grep -c '<script.*src=' "$file" 2>/dev/null || echo "0")
        echo "📜 JavaScript files: $JS_COUNT"
        
        # Check structure
        if grep -q "dashboard-container\|main-content" "$file"; then
            echo "✅ Dashboard structure found"
        else
            echo "❌ Missing dashboard structure"
        fi
        
        if grep -q "sidebar\|nav" "$file"; then
            echo "✅ Navigation found"
        else
            echo "❌ Missing navigation"
        fi
        
        # Check for forms
        FORM_COUNT=$(grep -c "<form" "$file" 2>/dev/null || echo "0")
        echo "📝 Forms: $FORM_COUNT"
        
        # Check for tables
        TABLE_COUNT=$(grep -c "<table" "$file" 2>/dev/null || echo "0")
        echo "📋 Tables: $TABLE_COUNT"
        
    elif [ "$status" = "302" ]; then
        echo "🔄 Page redirects (likely to login)"
    elif [ "$status" = "404" ]; then
        echo "❌ Page not found"
    else
        echo "❌ HTTP Error: $status"
    fi
}

# Analyze each dashboard
analyze_html "admin-response.html" "Admin" "$ADMIN_STATUS"
analyze_html "reception-response.html" "Reception" "$RECEPTION_STATUS"
analyze_html "phlebotomy-response.html" "Phlebotomy" "$PHLEBOTOMY_STATUS"
analyze_html "technician-response.html" "Lab Technician" "$TECHNICIAN_STATUS"

echo ""
echo "🎯 SUMMARY & RECOMMENDATIONS"
echo "============================"

# Count working dashboards
WORKING=0
REDIRECTED=0
BROKEN=0

for status in "$ADMIN_STATUS" "$RECEPTION_STATUS" "$PHLEBOTOMY_STATUS" "$TECHNICIAN_STATUS"; do
    if [ "$status" = "200" ]; then
        WORKING=$((WORKING + 1))
    elif [ "$status" = "302" ]; then
        REDIRECTED=$((REDIRECTED + 1))
    else
        BROKEN=$((BROKEN + 1))
    fi
done

echo "📊 Dashboard Status:"
echo "✅ Working: $WORKING"
echo "🔄 Redirected: $REDIRECTED"
echo "❌ Broken: $BROKEN"

echo ""
echo "🔧 Immediate Actions Needed:"

if [ "$ADMIN_STATUS" != "200" ]; then
    echo "1. 🚨 Fix Admin Dashboard (HTTP $ADMIN_STATUS)"
fi

if [ "$RECEPTION_STATUS" != "200" ]; then
    echo "2. 🚨 Fix Reception Dashboard (HTTP $RECEPTION_STATUS)"
fi

if [ "$PHLEBOTOMY_STATUS" = "200" ]; then
    echo "3. ✅ Phlebotomy Dashboard is working"
else
    echo "3. 🚨 Fix Phlebotomy Dashboard (HTTP $PHLEBOTOMY_STATUS)"
fi

if [ "$TECHNICIAN_STATUS" = "200" ]; then
    echo "4. ✅ Lab Technician Dashboard is working"
else
    echo "4. 🚨 Fix Lab Technician Dashboard (HTTP $TECHNICIAN_STATUS)"
fi

# Generate HTML report
cat > ui-diagnosis-report.html << EOF
<!DOCTYPE html>
<html>
<head>
    <title>UI Diagnosis Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background: #f8f9fa; padding: 20px; border-radius: 5px; margin-bottom: 20px; }
        .dashboard { border: 1px solid #ddd; margin: 15px 0; padding: 15px; border-radius: 5px; }
        .working { border-left: 5px solid #28a745; }
        .broken { border-left: 5px solid #dc3545; }
        .redirect { border-left: 5px solid #ffc107; }
        .summary { background: #e9ecef; padding: 15px; border-radius: 5px; margin: 20px 0; }
        .action { background: #fff3cd; padding: 10px; margin: 10px 0; border-radius: 3px; }
    </style>
</head>
<body>
    <div class="header">
        <h1>🔍 UI Diagnosis Report</h1>
        <p>Generated: $(date)</p>
        <p>Lab Operations System - Dashboard Status Analysis</p>
    </div>
    
    <div class="summary">
        <h2>📊 Summary</h2>
        <p>✅ Working: $WORKING | 🔄 Redirected: $REDIRECTED | ❌ Broken: $BROKEN</p>
    </div>
EOF

# Add dashboard details
add_dashboard_section() {
    local name=$1
    local status=$2
    local file=$3
    
    if [ "$status" = "200" ]; then
        CLASS="working"
        STATUS_TEXT="✅ Working"
    elif [ "$status" = "302" ]; then
        CLASS="redirect"
        STATUS_TEXT="🔄 Redirects to Login"
    else
        CLASS="broken"
        STATUS_TEXT="❌ Broken (HTTP $status)"
    fi
    
    cat >> ui-diagnosis-report.html << EOF
    <div class="dashboard $CLASS">
        <h3>$name Dashboard</h3>
        <p><strong>Status:</strong> $STATUS_TEXT</p>
        <p><strong>HTTP Code:</strong> $status</p>
EOF
    
    if [ "$status" = "200" ] && [ -f "$file" ]; then
        # Add analysis details
        if grep -q "dashboard-container\|main-content" "$file"; then
            echo "        <p>✅ Dashboard structure present</p>" >> ui-diagnosis-report.html
        else
            echo "        <p>❌ Missing dashboard structure</p>" >> ui-diagnosis-report.html
        fi
        
        CSS_COUNT=$(grep -c 'rel="stylesheet"' "$file" 2>/dev/null || echo "0")
        echo "        <p>🎨 CSS files: $CSS_COUNT</p>" >> ui-diagnosis-report.html
        
        JS_COUNT=$(grep -c '<script.*src=' "$file" 2>/dev/null || echo "0")
        echo "        <p>📜 JavaScript files: $JS_COUNT</p>" >> ui-diagnosis-report.html
    fi
    
    echo "    </div>" >> ui-diagnosis-report.html
}

add_dashboard_section "Admin" "$ADMIN_STATUS" "admin-response.html"
add_dashboard_section "Reception" "$RECEPTION_STATUS" "reception-response.html"
add_dashboard_section "Phlebotomy" "$PHLEBOTOMY_STATUS" "phlebotomy-response.html"
add_dashboard_section "Lab Technician" "$TECHNICIAN_STATUS" "technician-response.html"

cat >> ui-diagnosis-report.html << 'EOF'
    
    <div class="header">
        <h2>🔧 Recommended Actions</h2>
        <div class="action">
            <h3>Priority 1: Fix Broken Dashboards</h3>
            <p>Start with dashboards returning HTTP errors (404, 500, etc.)</p>
        </div>
        <div class="action">
            <h3>Priority 2: Handle Authentication</h3>
            <p>Configure security settings for dashboards redirecting to login</p>
        </div>
        <div class="action">
            <h3>Priority 3: Verify Structure</h3>
            <p>Ensure all working dashboards have proper HTML structure</p>
        </div>
        <div class="action">
            <h3>Priority 4: Test Functionality</h3>
            <p>Verify forms, navigation, and interactive elements work correctly</p>
        </div>
    </div>
</body>
</html>
EOF

echo ""
echo "📄 HTML Report generated: ui-diagnosis/ui-diagnosis-report.html"
echo "📁 Raw HTML responses saved for detailed inspection"

cd ..

echo ""
echo "🎉 DIAGNOSIS COMPLETE!"
echo "Open ui-diagnosis/ui-diagnosis-report.html in your browser for detailed results"
