const { chromium } = require('playwright');
const fs = require('fs');
const path = require('path');

class UITestingSuite {
    constructor() {
        this.browser = null;
        this.context = null;
        this.screenshotDir = 'ui-test-screenshots';
        this.results = [];
    }

    async setup() {
        // Create screenshots directory
        if (!fs.existsSync(this.screenshotDir)) {
            fs.mkdirSync(this.screenshotDir, { recursive: true });
        }

        // Launch browser
        this.browser = await chromium.launch({ 
            headless: false, // Set to true for CI/CD
            slowMo: 1000 // Slow down for better visibility
        });
        
        this.context = await this.browser.newContext({
            viewport: { width: 1920, height: 1080 }
        });
    }

    async testDashboard(url, name, expectedElements = []) {
        console.log(`\n🧪 Testing ${name} Dashboard: ${url}`);
        
        const page = await this.context.newPage();
        const result = {
            name,
            url,
            status: 'unknown',
            issues: [],
            screenshot: null,
            loadTime: 0
        };

        try {
            const startTime = Date.now();
            
            // Navigate to page
            const response = await page.goto(url, { 
                waitUntil: 'networkidle',
                timeout: 30000 
            });
            
            result.loadTime = Date.now() - startTime;
            result.httpStatus = response.status();

            // Take initial screenshot
            const screenshotPath = path.join(this.screenshotDir, `${name.toLowerCase()}-initial.png`);
            await page.screenshot({ path: screenshotPath, fullPage: true });
            result.screenshot = screenshotPath;

            console.log(`📸 Screenshot saved: ${screenshotPath}`);
            console.log(`⏱️  Load time: ${result.loadTime}ms`);
            console.log(`🌐 HTTP Status: ${result.httpStatus}`);

            // Check if page redirected (302/login)
            if (response.status() === 302 || page.url().includes('login')) {
                result.status = 'redirected';
                result.issues.push('Page redirected to login - authentication required');
                console.log('🔄 Page redirected to login');
            } else if (response.status() !== 200) {
                result.status = 'error';
                result.issues.push(`HTTP ${response.status()} error`);
                console.log(`❌ HTTP Error: ${response.status()}`);
            } else {
                result.status = 'loaded';
                console.log('✅ Page loaded successfully');
            }

            // Wait for potential JavaScript to load
            await page.waitForTimeout(3000);

            // Check for JavaScript errors
            const jsErrors = [];
            page.on('pageerror', error => {
                jsErrors.push(error.message);
            });

            // Check for console errors
            const consoleErrors = [];
            page.on('console', msg => {
                if (msg.type() === 'error') {
                    consoleErrors.push(msg.text());
                }
            });

            // Test basic page structure
            await this.testPageStructure(page, result);

            // Test expected elements
            if (expectedElements.length > 0) {
                await this.testExpectedElements(page, result, expectedElements);
            }

            // Test navigation if present
            await this.testNavigation(page, result, name);

            // Test forms if present
            await this.testForms(page, result, name);

            // Take final screenshot after interactions
            const finalScreenshotPath = path.join(this.screenshotDir, `${name.toLowerCase()}-final.png`);
            await page.screenshot({ path: finalScreenshotPath, fullPage: true });

            // Add JS and console errors to results
            if (jsErrors.length > 0) {
                result.issues.push(`JavaScript errors: ${jsErrors.join(', ')}`);
            }
            if (consoleErrors.length > 0) {
                result.issues.push(`Console errors: ${consoleErrors.join(', ')}`);
            }

        } catch (error) {
            result.status = 'error';
            result.issues.push(`Test error: ${error.message}`);
            console.log(`❌ Test error: ${error.message}`);
            
            // Take error screenshot
            try {
                const errorScreenshotPath = path.join(this.screenshotDir, `${name.toLowerCase()}-error.png`);
                await page.screenshot({ path: errorScreenshotPath, fullPage: true });
                result.screenshot = errorScreenshotPath;
            } catch (screenshotError) {
                console.log(`Failed to take error screenshot: ${screenshotError.message}`);
            }
        } finally {
            await page.close();
        }

        this.results.push(result);
        return result;
    }

    async testPageStructure(page, result) {
        console.log('🔍 Testing page structure...');
        
        // Check for basic HTML structure
        const hasTitle = await page.title();
        if (!hasTitle || hasTitle.trim() === '') {
            result.issues.push('Missing or empty page title');
        }

        // Check for CSS loading
        const stylesheets = await page.$$('link[rel="stylesheet"]');
        console.log(`📄 Found ${stylesheets.length} stylesheets`);
        
        if (stylesheets.length === 0) {
            result.issues.push('No CSS stylesheets found');
        }

        // Check for JavaScript files
        const scripts = await page.$$('script[src]');
        console.log(`📜 Found ${scripts.length} external scripts`);

        // Check for common layout elements
        const commonElements = [
            { selector: 'body', name: 'Body' },
            { selector: 'header, .header', name: 'Header' },
            { selector: 'nav, .nav, .sidebar', name: 'Navigation' },
            { selector: 'main, .main-content', name: 'Main content' }
        ];

        for (const element of commonElements) {
            const exists = await page.$(element.selector);
            if (!exists) {
                result.issues.push(`Missing ${element.name} element`);
            } else {
                console.log(`✅ Found ${element.name}`);
            }
        }
    }

    async testExpectedElements(page, result, expectedElements) {
        console.log('🎯 Testing expected elements...');
        
        for (const selector of expectedElements) {
            const element = await page.$(selector);
            if (!element) {
                result.issues.push(`Missing expected element: ${selector}`);
                console.log(`❌ Missing: ${selector}`);
            } else {
                console.log(`✅ Found: ${selector}`);
            }
        }
    }

    async testNavigation(page, result, dashboardName) {
        console.log('🧭 Testing navigation...');
        
        // Look for navigation links
        const navLinks = await page.$$('nav a, .nav-link, .sidebar a');
        console.log(`🔗 Found ${navLinks.length} navigation links`);
        
        if (navLinks.length === 0) {
            result.issues.push('No navigation links found');
            return;
        }

        // Test first few navigation links
        for (let i = 0; i < Math.min(3, navLinks.length); i++) {
            try {
                const link = navLinks[i];
                const linkText = await link.textContent();
                console.log(`🔗 Testing navigation link: ${linkText}`);
                
                await link.click();
                await page.waitForTimeout(1000);
                
                // Take screenshot of navigation result
                const navScreenshotPath = path.join(this.screenshotDir, `${dashboardName.toLowerCase()}-nav-${i}.png`);
                await page.screenshot({ path: navScreenshotPath, fullPage: true });
                
            } catch (error) {
                result.issues.push(`Navigation link ${i} failed: ${error.message}`);
            }
        }
    }

    async testForms(page, result, dashboardName) {
        console.log('📝 Testing forms...');
        
        const forms = await page.$$('form');
        console.log(`📋 Found ${forms.length} forms`);
        
        if (forms.length === 0) {
            return;
        }

        // Test first form
        try {
            const form = forms[0];
            const inputs = await form.$$('input, select, textarea');
            console.log(`📝 Form has ${inputs.length} input fields`);
            
            // Take screenshot of form
            const formScreenshotPath = path.join(this.screenshotDir, `${dashboardName.toLowerCase()}-form.png`);
            await page.screenshot({ path: formScreenshotPath, fullPage: true });
            
        } catch (error) {
            result.issues.push(`Form testing failed: ${error.message}`);
        }
    }

    async runAllTests() {
        console.log('🚀 Starting UI Testing Suite...');
        
        await this.setup();

        // Test all dashboards
        const dashboards = [
            {
                name: 'Admin',
                url: 'http://localhost:8080/admin/dashboard.html',
                expectedElements: ['.dashboard-container', '.sidebar', '.main-content']
            },
            {
                name: 'Reception',
                url: 'http://localhost:8080/reception/dashboard.html',
                expectedElements: ['.dashboard-container', '.sidebar', '.main-content']
            },
            {
                name: 'Phlebotomy',
                url: 'http://localhost:8080/phlebotomy/dashboard.html',
                expectedElements: ['.dashboard-container', '.sidebar', '.main-content']
            },
            {
                name: 'Lab-Technician',
                url: 'http://localhost:8080/technician/dashboard.html',
                expectedElements: ['.dashboard-container', '.sidebar', '.main-content']
            }
        ];

        for (const dashboard of dashboards) {
            await this.testDashboard(dashboard.url, dashboard.name, dashboard.expectedElements);
        }

        await this.generateReport();
        await this.cleanup();
    }

    async generateReport() {
        console.log('\n📊 Generating Test Report...');
        
        const report = {
            timestamp: new Date().toISOString(),
            summary: {
                total: this.results.length,
                passed: this.results.filter(r => r.status === 'loaded' && r.issues.length === 0).length,
                failed: this.results.filter(r => r.status === 'error').length,
                redirected: this.results.filter(r => r.status === 'redirected').length,
                withIssues: this.results.filter(r => r.issues.length > 0).length
            },
            results: this.results
        };

        // Save JSON report
        fs.writeFileSync('ui-test-report.json', JSON.stringify(report, null, 2));

        // Generate HTML report
        const htmlReport = this.generateHTMLReport(report);
        fs.writeFileSync('ui-test-report.html', htmlReport);

        console.log('📄 Reports generated:');
        console.log('  - ui-test-report.json');
        console.log('  - ui-test-report.html');
        console.log(`📸 Screenshots saved in: ${this.screenshotDir}/`);

        // Print summary
        console.log('\n📈 TEST SUMMARY:');
        console.log(`✅ Passed: ${report.summary.passed}`);
        console.log(`❌ Failed: ${report.summary.failed}`);
        console.log(`🔄 Redirected: ${report.summary.redirected}`);
        console.log(`⚠️  With Issues: ${report.summary.withIssues}`);
    }

    generateHTMLReport(report) {
        return `
<!DOCTYPE html>
<html>
<head>
    <title>UI Test Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .summary { background: #f5f5f5; padding: 15px; border-radius: 5px; margin-bottom: 20px; }
        .result { border: 1px solid #ddd; margin: 10px 0; padding: 15px; border-radius: 5px; }
        .passed { border-left: 5px solid #28a745; }
        .failed { border-left: 5px solid #dc3545; }
        .redirected { border-left: 5px solid #ffc107; }
        .issues { background: #fff3cd; padding: 10px; margin: 10px 0; border-radius: 3px; }
        .screenshot { max-width: 300px; margin: 10px 0; }
        img { max-width: 100%; height: auto; }
    </style>
</head>
<body>
    <h1>🧪 UI Testing Report</h1>
    <div class="summary">
        <h2>Summary</h2>
        <p>Generated: ${report.timestamp}</p>
        <p>✅ Passed: ${report.summary.passed} | ❌ Failed: ${report.summary.failed} | 🔄 Redirected: ${report.summary.redirected} | ⚠️ With Issues: ${report.summary.withIssues}</p>
    </div>
    
    ${report.results.map(result => `
        <div class="result ${result.status}">
            <h3>${result.name} Dashboard</h3>
            <p><strong>URL:</strong> ${result.url}</p>
            <p><strong>Status:</strong> ${result.status} (HTTP ${result.httpStatus || 'N/A'})</p>
            <p><strong>Load Time:</strong> ${result.loadTime}ms</p>
            
            ${result.issues.length > 0 ? `
                <div class="issues">
                    <h4>Issues Found:</h4>
                    <ul>
                        ${result.issues.map(issue => `<li>${issue}</li>`).join('')}
                    </ul>
                </div>
            ` : '<p>✅ No issues found</p>'}
            
            ${result.screenshot ? `
                <div class="screenshot">
                    <h4>Screenshot:</h4>
                    <img src="${result.screenshot}" alt="${result.name} screenshot">
                </div>
            ` : ''}
        </div>
    `).join('')}
</body>
</html>`;
    }

    async cleanup() {
        if (this.browser) {
            await this.browser.close();
        }
    }
}

// Run the tests
async function main() {
    const suite = new UITestingSuite();
    try {
        await suite.runAllTests();
    } catch (error) {
        console.error('Test suite failed:', error);
        process.exit(1);
    }
}

if (require.main === module) {
    main();
}

module.exports = UITestingSuite;
