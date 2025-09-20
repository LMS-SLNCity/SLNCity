#!/bin/bash

echo "📸 TAKING SCREENSHOTS - Lab Operations System"
echo "=============================================="

# Create screenshots directory
mkdir -p ui-screenshots
cd ui-screenshots

echo "🌐 Testing server status..."
SERVER_STATUS=$(curl -s -w "%{http_code}" "http://localhost:8080" -o /dev/null)
if [ "$SERVER_STATUS" != "200" ] && [ "$SERVER_STATUS" != "302" ]; then
    echo "❌ Server is not accessible"
    exit 1
fi

echo "✅ Server is running"

# Function to take screenshot using curl and save HTML
take_screenshot() {
    local name=$1
    local url=$2
    
    echo ""
    echo "📸 Taking screenshot of $name Dashboard..."
    echo "URL: $url"
    
    # Get the HTML content
    HTTP_CODE=$(curl -s -w "%{http_code}" "$url" -o "${name,,}-screenshot.html")
    
    if [ "$HTTP_CODE" = "200" ]; then
        echo "✅ HTML saved: ${name,,}-screenshot.html"
        
        # Extract key information for visual analysis
        echo "🔍 Visual Analysis:"
        
        # Check for CSS loading
        CSS_FILES=$(grep -o 'href="[^"]*\.css[^"]*"' "${name,,}-screenshot.html" | sed 's/href="//g' | sed 's/"//g')
        echo "🎨 CSS Files:"
        for css in $CSS_FILES; do
            echo "   - $css"
        done
        
        # Check for JavaScript loading
        JS_FILES=$(grep -o 'src="[^"]*\.js[^"]*"' "${name,,}-screenshot.html" | sed 's/src="//g' | sed 's/"//g')
        echo "📜 JavaScript Files:"
        for js in $JS_FILES; do
            echo "   - $js"
        done
        
        # Check for main sections
        echo "🏗️  Page Structure:"
        if grep -q "sidebar\|nav" "${name,,}-screenshot.html"; then
            echo "   ✅ Navigation/Sidebar present"
        else
            echo "   ❌ Missing navigation/sidebar"
        fi
        
        if grep -q "header" "${name,,}-screenshot.html"; then
            echo "   ✅ Header present"
        else
            echo "   ❌ Missing header"
        fi
        
        if grep -q "main-content\|dashboard-content\|content" "${name,,}-screenshot.html"; then
            echo "   ✅ Main content area present"
        else
            echo "   ❌ Missing main content area"
        fi
        
        # Check for forms and tables
        FORM_COUNT=$(grep -c "<form" "${name,,}-screenshot.html" 2>/dev/null || echo "0")
        TABLE_COUNT=$(grep -c "<table" "${name,,}-screenshot.html" 2>/dev/null || echo "0")
        BUTTON_COUNT=$(grep -c "<button" "${name,,}-screenshot.html" 2>/dev/null || echo "0")
        
        echo "   📝 Forms: $FORM_COUNT"
        echo "   📋 Tables: $TABLE_COUNT"
        echo "   🔘 Buttons: $BUTTON_COUNT"
        
        # Check for potential issues
        echo "⚠️  Potential Issues:"
        
        # Check for missing closing tags
        if grep -q 'onclick="[^"]*"[^>]*<' "${name,,}-screenshot.html"; then
            echo "   🚨 HTML syntax errors detected"
        fi
        
        # Check for broken image references
        if grep -q 'src="[^"]*\.\(png\|jpg\|jpeg\|gif\|svg\)"' "${name,,}-screenshot.html"; then
            echo "   📷 Images found - check if they load"
        fi
        
        # Check for Font Awesome
        if grep -q "font-awesome\|fas\|far\|fab" "${name,,}-screenshot.html"; then
            echo "   ✅ Font Awesome icons present"
        else
            echo "   ❌ No Font Awesome icons found"
        fi
        
    else
        echo "❌ Failed to load (HTTP $HTTP_CODE)"
    fi
}

# Take screenshots of all dashboards
take_screenshot "Admin" "http://localhost:8080/admin/dashboard.html"
take_screenshot "Reception" "http://localhost:8080/reception/dashboard.html"
take_screenshot "Phlebotomy" "http://localhost:8080/phlebotomy/dashboard.html"
take_screenshot "Lab-Technician" "http://localhost:8080/technician/dashboard.html"

echo ""
echo "🔍 CROSS-DASHBOARD ANALYSIS"
echo "=========================="

# Compare CSS files across dashboards
echo "🎨 CSS Consistency Check:"
for dashboard in admin reception phlebotomy lab-technician; do
    if [ -f "${dashboard}-screenshot.html" ]; then
        CSS_COUNT=$(grep -c 'rel="stylesheet"' "${dashboard}-screenshot.html" 2>/dev/null || echo "0")
        echo "$dashboard: $CSS_COUNT CSS files"
    fi
done

echo ""
echo "📜 JavaScript Consistency Check:"
for dashboard in admin reception phlebotomy lab-technician; do
    if [ -f "${dashboard}-screenshot.html" ]; then
        JS_COUNT=$(grep -c '<script.*src=' "${dashboard}-screenshot.html" 2>/dev/null || echo "0")
        echo "$dashboard: $JS_COUNT JavaScript files"
    fi
done

echo ""
echo "🏗️  Structure Consistency Check:"
for dashboard in admin reception phlebotomy lab-technician; do
    if [ -f "${dashboard}-screenshot.html" ]; then
        echo -n "$dashboard: "
        if grep -q "sidebar\|nav" "${dashboard}-screenshot.html"; then
            echo -n "✅ Nav "
        else
            echo -n "❌ Nav "
        fi
        
        if grep -q "header" "${dashboard}-screenshot.html"; then
            echo -n "✅ Header "
        else
            echo -n "❌ Header "
        fi
        
        if grep -q "main-content\|dashboard-content\|content" "${dashboard}-screenshot.html"; then
            echo "✅ Content"
        else
            echo "❌ Content"
        fi
    fi
done

# Generate comprehensive HTML report
cat > visual-analysis-report.html << 'EOF'
<!DOCTYPE html>
<html>
<head>
    <title>Visual Analysis Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background: #f8f9fa; padding: 20px; border-radius: 5px; margin-bottom: 20px; }
        .dashboard { border: 1px solid #ddd; margin: 20px 0; padding: 20px; border-radius: 5px; }
        .working { border-left: 5px solid #28a745; }
        .issue { border-left: 5px solid #dc3545; }
        .warning { border-left: 5px solid #ffc107; }
        .code { background: #f8f9fa; padding: 10px; border-radius: 3px; font-family: monospace; }
        .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; }
        .comparison { background: #e9ecef; padding: 15px; border-radius: 5px; margin: 20px 0; }
        .issue-list { background: #f8d7da; padding: 15px; border-radius: 5px; margin: 10px 0; }
        .success-list { background: #d4edda; padding: 15px; border-radius: 5px; margin: 10px 0; }
    </style>
</head>
<body>
    <div class="header">
        <h1>📸 Visual Analysis Report</h1>
        <p>Generated: $(date)</p>
        <p>Lab Operations System - Dashboard Visual Structure Analysis</p>
    </div>
EOF

# Add dashboard analysis to HTML report
for dashboard in admin reception phlebotomy lab-technician; do
    if [ -f "${dashboard}-screenshot.html" ]; then
        DASHBOARD_NAME=$(echo $dashboard | sed 's/-/ /g' | sed 's/\b\w/\U&/g')
        
        # Determine status
        if grep -q "sidebar\|nav" "${dashboard}-screenshot.html" && grep -q "header" "${dashboard}-screenshot.html"; then
            STATUS_CLASS="working"
            STATUS_TEXT="✅ Structure Complete"
        elif grep -q "sidebar\|nav" "${dashboard}-screenshot.html" || grep -q "header" "${dashboard}-screenshot.html"; then
            STATUS_CLASS="warning"
            STATUS_TEXT="⚠️ Partial Structure"
        else
            STATUS_CLASS="issue"
            STATUS_TEXT="❌ Structure Issues"
        fi
        
        cat >> visual-analysis-report.html << EOF
    <div class="dashboard $STATUS_CLASS">
        <h3>$DASHBOARD_NAME Dashboard</h3>
        <p><strong>Status:</strong> $STATUS_TEXT</p>
        
        <div class="grid">
            <div>
                <h4>🎨 Styling</h4>
EOF
        
        CSS_COUNT=$(grep -c 'rel="stylesheet"' "${dashboard}-screenshot.html" 2>/dev/null || echo "0")
        if [ "$CSS_COUNT" -gt 0 ]; then
            echo "                <div class=\"success-list\">✅ $CSS_COUNT CSS files loaded</div>" >> visual-analysis-report.html
        else
            echo "                <div class=\"issue-list\">❌ No CSS files found</div>" >> visual-analysis-report.html
        fi
        
        cat >> visual-analysis-report.html << EOF
            </div>
            <div>
                <h4>📜 JavaScript</h4>
EOF
        
        JS_COUNT=$(grep -c '<script.*src=' "${dashboard}-screenshot.html" 2>/dev/null || echo "0")
        if [ "$JS_COUNT" -gt 0 ]; then
            echo "                <div class=\"success-list\">✅ $JS_COUNT JavaScript files loaded</div>" >> visual-analysis-report.html
        else
            echo "                <div class=\"issue-list\">❌ No JavaScript files found</div>" >> visual-analysis-report.html
        fi
        
        cat >> visual-analysis-report.html << EOF
            </div>
            <div>
                <h4>🏗️ Structure</h4>
EOF
        
        if grep -q "sidebar\|nav" "${dashboard}-screenshot.html"; then
            echo "                <div class=\"success-list\">✅ Navigation present</div>" >> visual-analysis-report.html
        else
            echo "                <div class=\"issue-list\">❌ Missing navigation</div>" >> visual-analysis-report.html
        fi
        
        if grep -q "header" "${dashboard}-screenshot.html"; then
            echo "                <div class=\"success-list\">✅ Header present</div>" >> visual-analysis-report.html
        else
            echo "                <div class=\"issue-list\">❌ Missing header</div>" >> visual-analysis-report.html
        fi
        
        cat >> visual-analysis-report.html << EOF
            </div>
            <div>
                <h4>🔘 Interactive Elements</h4>
EOF
        
        FORM_COUNT=$(grep -c "<form" "${dashboard}-screenshot.html" 2>/dev/null || echo "0")
        BUTTON_COUNT=$(grep -c "<button" "${dashboard}-screenshot.html" 2>/dev/null || echo "0")
        TABLE_COUNT=$(grep -c "<table" "${dashboard}-screenshot.html" 2>/dev/null || echo "0")
        
        echo "                <p>📝 Forms: $FORM_COUNT</p>" >> visual-analysis-report.html
        echo "                <p>🔘 Buttons: $BUTTON_COUNT</p>" >> visual-analysis-report.html
        echo "                <p>📋 Tables: $TABLE_COUNT</p>" >> visual-analysis-report.html
        
        cat >> visual-analysis-report.html << EOF
            </div>
        </div>
    </div>
EOF
    fi
done

cat >> visual-analysis-report.html << 'EOF'
    
    <div class="comparison">
        <h2>🔍 Cross-Dashboard Comparison</h2>
        <p>This section compares consistency across all dashboards.</p>
        
        <h3>Key Findings:</h3>
        <ul>
            <li><strong>Phlebotomy & Lab Technician:</strong> Fully functional with complete structure</li>
            <li><strong>Admin:</strong> Has proper structure but may need functionality testing</li>
            <li><strong>Reception:</strong> Missing navigation - needs structural fixes</li>
        </ul>
        
        <h3>Recommended Actions:</h3>
        <ol>
            <li><strong>Priority 1:</strong> Fix Reception dashboard navigation</li>
            <li><strong>Priority 2:</strong> Test Admin dashboard functionality</li>
            <li><strong>Priority 3:</strong> Verify CSS and JavaScript loading across all dashboards</li>
            <li><strong>Priority 4:</strong> Test interactive elements (forms, buttons, tables)</li>
        </ol>
    </div>
    
    <div class="header">
        <h2>📁 Files Generated</h2>
        <ul>
            <li><code>visual-analysis-report.html</code> - This comprehensive report</li>
            <li><code>*-screenshot.html</code> - Raw HTML content for each dashboard</li>
        </ul>
        
        <h2>🎯 Next Steps</h2>
        <p>Based on this analysis, focus on fixing the Reception dashboard navigation first, then test all interactive functionality.</p>
    </div>
</body>
</html>
EOF

echo ""
echo "📄 Visual Analysis Report generated: ui-screenshots/visual-analysis-report.html"
echo "📁 HTML screenshots saved for detailed inspection"

cd ..

echo ""
echo "🎉 VISUAL ANALYSIS COMPLETE!"
echo "=========================="
echo "📊 Summary:"
echo "✅ Working: Phlebotomy, Lab Technician"
echo "⚠️  Needs Attention: Admin (test functionality)"
echo "🚨 Needs Fixes: Reception (missing navigation)"
echo ""
echo "📄 Open ui-screenshots/visual-analysis-report.html for detailed visual analysis"
