const { Builder, By, until } = require('selenium-webdriver');
const chrome = require('selenium-webdriver/chrome');
const fs = require('fs');
const path = require('path');

class SeleniumUITest {
    constructor() {
        this.driver = null;
        this.screenshotDir = 'selenium-screenshots';
        this.results = [];
    }

    async setup() {
        // Create screenshots directory
        if (!fs.existsSync(this.screenshotDir)) {
            fs.mkdirSync(this.screenshotDir, { recursive: true });
        }

        // Setup Chrome options
        const options = new chrome.Options();
        options.addArguments('--no-sandbox');
        options.addArguments('--disable-dev-shm-usage');
        options.addArguments('--window-size=1920,1080');
        // options.addArguments('--headless'); // Uncomment for headless mode

        // Create WebDriver
        this.driver = await new Builder()
            .forBrowser('chrome')
            .setChromeOptions(options)
            .build();

        await this.driver.manage().window().maximize();
    }

    async testDashboard(url, name) {
        console.log(`\n🧪 [Selenium] Testing ${name} Dashboard: ${url}`);
        
        const result = {
            name,
            url,
            status: 'unknown',
            issues: [],
            screenshots: [],
            loadTime: 0,
            elements: {}
        };

        try {
            const startTime = Date.now();
            
            // Navigate to page
            await this.driver.get(url);
            
            // Wait for page to load
            await this.driver.wait(until.titleIs(''), 1000).catch(() => {
                // Title might be empty, that's okay
            });
            
            result.loadTime = Date.now() - startTime;
            
            // Take initial screenshot
            const screenshotPath = path.join(this.screenshotDir, `${name.toLowerCase()}-selenium.png`);
            await this.driver.takeScreenshot().then(data => {
                fs.writeFileSync(screenshotPath, data, 'base64');
            });
            result.screenshots.push(screenshotPath);
            
            console.log(`📸 Screenshot saved: ${screenshotPath}`);
            console.log(`⏱️  Load time: ${result.loadTime}ms`);

            // Get current URL to check for redirects
            const currentUrl = await this.driver.getCurrentUrl();
            if (currentUrl !== url && currentUrl.includes('login')) {
                result.status = 'redirected';
                result.issues.push('Page redirected to login');
                console.log('🔄 Page redirected to login');
            } else {
                result.status = 'loaded';
                console.log('✅ Page loaded successfully');
            }

            // Get page title
            const title = await this.driver.getTitle();
            result.elements.title = title;
            console.log(`📄 Page title: "${title}"`);

            if (!title || title.trim() === '') {
                result.issues.push('Missing or empty page title');
            }

            // Test page structure
            await this.testPageElements(result);

            // Test for JavaScript errors (check console logs)
            const logs = await this.driver.manage().logs().get('browser');
            const errors = logs.filter(log => log.level.name === 'SEVERE');
            if (errors.length > 0) {
                result.issues.push(`JavaScript errors: ${errors.map(e => e.message).join(', ')}`);
            }

            // Test responsive design
            await this.testResponsive(result, name);

        } catch (error) {
            result.status = 'error';
            result.issues.push(`Selenium test error: ${error.message}`);
            console.log(`❌ Test error: ${error.message}`);
            
            // Take error screenshot
            try {
                const errorScreenshotPath = path.join(this.screenshotDir, `${name.toLowerCase()}-error.png`);
                await this.driver.takeScreenshot().then(data => {
                    fs.writeFileSync(errorScreenshotPath, data, 'base64');
                });
                result.screenshots.push(errorScreenshotPath);
            } catch (screenshotError) {
                console.log(`Failed to take error screenshot: ${screenshotError.message}`);
            }
        }

        this.results.push(result);
        return result;
    }

    async testPageElements(result) {
        console.log('🔍 Testing page elements...');
        
        const elementsToTest = [
            { selector: 'body', name: 'Body' },
            { selector: '.dashboard-container', name: 'Dashboard Container' },
            { selector: '.sidebar', name: 'Sidebar' },
            { selector: '.main-content', name: 'Main Content' },
            { selector: 'nav', name: 'Navigation' },
            { selector: '.nav-link', name: 'Navigation Links' },
            { selector: 'form', name: 'Forms' },
            { selector: 'button', name: 'Buttons' },
            { selector: 'input', name: 'Input Fields' },
            { selector: '.stats-grid', name: 'Statistics Grid' },
            { selector: 'table', name: 'Tables' }
        ];

        for (const element of elementsToTest) {
            try {
                const elements = await this.driver.findElements(By.css(element.selector));
                result.elements[element.name] = elements.length;
                
                if (elements.length > 0) {
                    console.log(`✅ Found ${elements.length} ${element.name} element(s)`);
                    
                    // Check if element is visible
                    const isVisible = await elements[0].isDisplayed();
                    if (!isVisible) {
                        result.issues.push(`${element.name} element exists but is not visible`);
                    }
                } else {
                    console.log(`❌ Missing ${element.name}`);
                    result.issues.push(`Missing ${element.name} element`);
                }
            } catch (error) {
                result.issues.push(`Error testing ${element.name}: ${error.message}`);
            }
        }
    }

    async testResponsive(result, name) {
        console.log('📱 Testing responsive design...');
        
        const viewports = [
            { width: 1920, height: 1080, name: 'desktop' },
            { width: 768, height: 1024, name: 'tablet' },
            { width: 375, height: 667, name: 'mobile' }
        ];

        for (const viewport of viewports) {
            try {
                await this.driver.manage().window().setRect({
                    width: viewport.width,
                    height: viewport.height
                });
                
                await this.driver.sleep(1000); // Wait for layout to adjust
                
                const screenshotPath = path.join(this.screenshotDir, `${name.toLowerCase()}-${viewport.name}.png`);
                await this.driver.takeScreenshot().then(data => {
                    fs.writeFileSync(screenshotPath, data, 'base64');
                });
                result.screenshots.push(screenshotPath);
                
                console.log(`📸 ${viewport.name} screenshot: ${screenshotPath}`);
                
            } catch (error) {
                result.issues.push(`Responsive test failed for ${viewport.name}: ${error.message}`);
            }
        }
        
        // Reset to desktop size
        await this.driver.manage().window().maximize();
    }

    async runAllTests() {
        console.log('🚀 Starting Selenium UI Testing...');
        
        await this.setup();

        const dashboards = [
            { name: 'Admin', url: 'http://localhost:8080/admin/dashboard.html' },
            { name: 'Reception', url: 'http://localhost:8080/reception/dashboard.html' },
            { name: 'Phlebotomy', url: 'http://localhost:8080/phlebotomy/dashboard.html' },
            { name: 'Lab-Technician', url: 'http://localhost:8080/technician/dashboard.html' }
        ];

        for (const dashboard of dashboards) {
            await this.testDashboard(dashboard.url, dashboard.name);
        }

        await this.generateReport();
        await this.cleanup();
    }

    async generateReport() {
        console.log('\n📊 Generating Selenium Test Report...');
        
        const report = {
            timestamp: new Date().toISOString(),
            testTool: 'Selenium WebDriver',
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
        fs.writeFileSync('selenium-test-report.json', JSON.stringify(report, null, 2));

        console.log('📄 Selenium report generated: selenium-test-report.json');
        console.log(`📸 Screenshots saved in: ${this.screenshotDir}/`);

        // Print summary
        console.log('\n📈 SELENIUM TEST SUMMARY:');
        console.log(`✅ Passed: ${report.summary.passed}`);
        console.log(`❌ Failed: ${report.summary.failed}`);
        console.log(`🔄 Redirected: ${report.summary.redirected}`);
        console.log(`⚠️  With Issues: ${report.summary.withIssues}`);

        // Print detailed issues
        this.results.forEach(result => {
            if (result.issues.length > 0) {
                console.log(`\n🔍 ${result.name} Dashboard Issues:`);
                result.issues.forEach(issue => console.log(`  - ${issue}`));
            }
        });
    }

    async cleanup() {
        if (this.driver) {
            await this.driver.quit();
        }
    }
}

// Run the tests
async function main() {
    const suite = new SeleniumUITest();
    try {
        await suite.runAllTests();
    } catch (error) {
        console.error('Selenium test suite failed:', error);
        process.exit(1);
    }
}

if (require.main === module) {
    main();
}

module.exports = SeleniumUITest;
