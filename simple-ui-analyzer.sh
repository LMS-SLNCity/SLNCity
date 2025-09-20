#!/bin/bash

echo "🔍 COMPREHENSIVE UI ANALYSIS - Lab Operations System"
echo "===================================================="

# Create results directory
mkdir -p ui-analysis-results
cd ui-analysis-results

# Function to test dashboard
test_dashboard() {
    local name=$1
    local url=$2
    
    echo ""
    echo "🧪 Testing $name Dashboard"
    echo "URL: $url"
    echo "----------------------------------------"
    
    # Test HTTP response
    echo "📡 Testing HTTP Response..."
    HTTP_CODE=$(curl -s -w "%{http_code}" "$url" -o "${name,,}-response.html")
    echo "HTTP Status: $HTTP_CODE"
    
    if [ "$HTTP_CODE" = "200" ]; then
        echo "✅ Page loads successfully"
        
        # Analyze HTML content
        echo "🔍 Analyzing HTML structure..."
        
        # Check for basic HTML elements
        if grep -q "<title>" "${name,,}-response.html"; then
            TITLE=$(grep -o "<title>[^<]*</title>" "${name,,}-response.html" | sed 's/<[^>]*>//g')
            echo "📄 Title: '$TITLE'"
        else
            echo "❌ Missing page title"
        fi
        
        # Check for CSS links
        CSS_COUNT=$(grep -c 'rel="stylesheet"' "${name,,}-response.html" || echo "0")
        echo "🎨 CSS files: $CSS_COUNT"
        
        # Check for JavaScript files
        JS_COUNT=$(grep -c '<script.*src=' "${name,,}-response.html" || echo "0")
        echo "📜 JavaScript files: $JS_COUNT"
        
        # Check for common dashboard elements
        echo "🏗️  Checking dashboard structure..."
        
        if grep -q "dashboard-container\|main-content" "${name,,}-response.html"; then
            echo "✅ Dashboard container found"
        else
            echo "❌ Missing dashboard container"
        fi
        
        if grep -q "sidebar\|nav" "${name,,}-response.html"; then
            echo "✅ Navigation found"
        else
            echo "❌ Missing navigation"
        fi
        
        if grep -q "form" "${name,,}-response.html"; then
            FORM_COUNT=$(grep -c "<form" "${name,,}-response.html")
            echo "✅ Forms found: $FORM_COUNT"
        else
            echo "⚠️  No forms found"
        fi
        
        if grep -q "table" "${name,,}-response.html"; then
            TABLE_COUNT=$(grep -c "<table" "${name,,}-response.html")
            echo "✅ Tables found: $TABLE_COUNT"
        else
            echo "⚠️  No tables found"
        fi
        
        # Check for JavaScript errors in HTML
        if grep -q "onclick.*window\." "${name,,}-response.html"; then
            echo "✅ JavaScript event handlers found"
        else
            echo "⚠️  No JavaScript event handlers found"
        fi
        
        # Check for missing closing tags
        if grep -q 'onclick="[^"]*"[^>]*<' "${name,,}-response.html"; then
            echo "❌ Potential HTML syntax errors found"
        else
            echo "✅ No obvious HTML syntax errors"
        fi
        
    elif [ "$HTTP_CODE" = "302" ]; then
        echo "🔄 Page redirects (likely to login)"
        echo "⚠️  Authentication required"
    elif [ "$HTTP_CODE" = "404" ]; then
        echo "❌ Page not found"
    elif [ "$HTTP_CODE" = "000" ]; then
        echo "❌ Server not responding"
    else
        echo "❌ HTTP Error: $HTTP_CODE"
    fi
    
    # Test CSS loading
    echo "🎨 Testing CSS files..."
    if [ "$HTTP_CODE" = "200" ]; then
        CSS_URLS=$(grep -o 'href="[^"]*\.css[^"]*"' "${name,,}-response.html" | sed 's/href="//g' | sed 's/"//g')
        for css_url in $CSS_URLS; do
            if [[ $css_url == /* ]]; then
                full_css_url="http://localhost:8080$css_url"
            else
                full_css_url="$css_url"
            fi
            
            CSS_STATUS=$(curl -s -w "%{http_code}" "$full_css_url" -o /dev/null)
            if [ "$CSS_STATUS" = "200" ]; then
                echo "✅ CSS loads: $css_url"
            else
                echo "❌ CSS failed ($CSS_STATUS): $css_url"
            fi
        done
    fi
    
    # Test JavaScript loading
    echo "📜 Testing JavaScript files..."
    if [ "$HTTP_CODE" = "200" ]; then
        JS_URLS=$(grep -o 'src="[^"]*\.js[^"]*"' "${name,,}-response.html" | sed 's/src="//g' | sed 's/"//g')
        for js_url in $JS_URLS; do
            if [[ $js_url == /* ]]; then
                full_js_url="http://localhost:8080$js_url"
            else
                full_js_url="$js_url"
            fi
            
            JS_STATUS=$(curl -s -w "%{http_code}" "$full_js_url" -o /dev/null)
            if [ "$JS_STATUS" = "200" ]; then
                echo "✅ JavaScript loads: $js_url"
            else
                echo "❌ JavaScript failed ($JS_STATUS): $js_url"
            fi
        done
    fi
    
    echo "----------------------------------------"
}

# Test server status
echo "🌐 Checking server status..."
SERVER_STATUS=$(curl -s -w "%{http_code}" "http://localhost:8080" -o /dev/null)
if [ "$SERVER_STATUS" = "200" ] || [ "$SERVER_STATUS" = "302" ]; then
    echo "✅ Server is running (HTTP $SERVER_STATUS)"
else
    echo "❌ Server is not running or not accessible"
    echo "💡 Please start the server with: mvn spring-boot:run"
    exit 1
fi

# Test all dashboards
test_dashboard "Admin" "http://localhost:8080/admin/dashboard.html"
test_dashboard "Reception" "http://localhost:8080/reception/dashboard.html"
test_dashboard "Phlebotomy" "http://localhost:8080/phlebotomy/dashboard.html"
test_dashboard "Lab-Technician" "http://localhost:8080/technician/dashboard.html"

# Generate summary report
echo ""
echo "📊 GENERATING SUMMARY REPORT"
echo "============================"

cat > ui-analysis-summary.html << 'EOF'
<!DOCTYPE html>
<html>
<head>
    <title>UI Analysis Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background: #f8f9fa; padding: 20px; border-radius: 5px; margin-bottom: 20px; }
        .dashboard { border: 1px solid #ddd; margin: 20px 0; padding: 20px; border-radius: 5px; }
        .working { border-left: 5px solid #28a745; }
        .broken { border-left: 5px solid #dc3545; }
        .redirect { border-left: 5px solid #ffc107; }
        .code { background: #f8f9fa; padding: 10px; border-radius: 3px; font-family: monospace; }
        .issue { background: #f8d7da; padding: 10px; margin: 10px 0; border-radius: 3px; }
        .success { background: #d4edda; padding: 10px; margin: 10px 0; border-radius: 3px; }
    </style>
</head>
<body>
    <div class="header">
        <h1>🔍 UI Analysis Report</h1>
        <p>Generated: $(date)</p>
        <p>This report analyzes the current state of all dashboard UIs in the Lab Operations System.</p>
    </div>
EOF

# Analyze each dashboard result
for dashboard in admin reception phlebotomy lab-technician; do
    if [ -f "${dashboard}-response.html" ]; then
        HTTP_CODE=$(curl -s -w "%{http_code}" "http://localhost:8080/${dashboard}/dashboard.html" -o /dev/null 2>/dev/null || echo "000")
        
        if [ "$HTTP_CODE" = "200" ]; then
            STATUS_CLASS="working"
            STATUS_TEXT="✅ Working"
        elif [ "$HTTP_CODE" = "302" ]; then
            STATUS_CLASS="redirect"
            STATUS_TEXT="🔄 Redirects to Login"
        else
            STATUS_CLASS="broken"
            STATUS_TEXT="❌ Broken (HTTP $HTTP_CODE)"
        fi
        
        cat >> ui-analysis-summary.html << EOF
    <div class="dashboard $STATUS_CLASS">
        <h2>$(echo $dashboard | sed 's/-/ /g' | sed 's/\b\w/\U&/g') Dashboard</h2>
        <p><strong>Status:</strong> $STATUS_TEXT</p>
        <p><strong>URL:</strong> http://localhost:8080/${dashboard}/dashboard.html</p>
        
        <h3>Analysis Results:</h3>
EOF
        
        # Add specific analysis for each dashboard
        if [ "$HTTP_CODE" = "200" ]; then
            if grep -q "dashboard-container\|main-content" "${dashboard}-response.html"; then
                echo '        <div class="success">✅ Dashboard structure present</div>' >> ui-analysis-summary.html
            else
                echo '        <div class="issue">❌ Missing dashboard structure</div>' >> ui-analysis-summary.html
            fi
            
            if grep -q "sidebar\|nav" "${dashboard}-response.html"; then
                echo '        <div class="success">✅ Navigation present</div>' >> ui-analysis-summary.html
            else
                echo '        <div class="issue">❌ Missing navigation</div>' >> ui-analysis-summary.html
            fi
            
            CSS_COUNT=$(grep -c 'rel="stylesheet"' "${dashboard}-response.html" || echo "0")
            if [ "$CSS_COUNT" -gt 0 ]; then
                echo "        <div class=\"success\">✅ CSS files: $CSS_COUNT</div>" >> ui-analysis-summary.html
            else
                echo '        <div class="issue">❌ No CSS files found</div>' >> ui-analysis-summary.html
            fi
            
            JS_COUNT=$(grep -c '<script.*src=' "${dashboard}-response.html" || echo "0")
            if [ "$JS_COUNT" -gt 0 ]; then
                echo "        <div class=\"success\">✅ JavaScript files: $JS_COUNT</div>" >> ui-analysis-summary.html
            else
                echo '        <div class="issue">❌ No JavaScript files found</div>' >> ui-analysis-summary.html
            fi
        fi
        
        echo '    </div>' >> ui-analysis-summary.html
    fi
done

cat >> ui-analysis-summary.html << 'EOF'
    
    <div class="header">
        <h2>🔧 Recommended Actions</h2>
        <ol>
            <li><strong>Fix Broken Dashboards:</strong> Start with dashboards showing HTTP errors</li>
            <li><strong>Authentication Issues:</strong> Configure security for dashboard access</li>
            <li><strong>Missing Structure:</strong> Add proper HTML structure to broken dashboards</li>
            <li><strong>CSS/JS Loading:</strong> Ensure all stylesheets and scripts load correctly</li>
            <li><strong>Test Functionality:</strong> Verify forms, navigation, and interactive elements</li>
        </ol>
        
        <h2>📁 Files Generated</h2>
        <ul>
            <li><code>ui-analysis-summary.html</code> - This report</li>
            <li><code>*-response.html</code> - Raw HTML responses for each dashboard</li>
        </ul>
    </div>
</body>
</html>
EOF

echo "📄 Summary report generated: ui-analysis-results/ui-analysis-summary.html"
echo "📁 Raw HTML files saved for detailed analysis"

# Go back to original directory
cd ..

echo ""
echo "🎯 ANALYSIS COMPLETE!"
echo "====================="
echo "📄 Open ui-analysis-results/ui-analysis-summary.html in your browser"
echo "🔍 Check individual *-response.html files for detailed HTML analysis"
echo ""
echo "💡 Next Steps:"
echo "1. Review the summary report"
echo "2. Fix broken dashboards starting with HTTP errors"
echo "3. Ensure proper HTML structure and CSS/JS loading"
echo "4. Test functionality after fixes"
