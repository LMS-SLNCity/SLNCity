/**
 * Technician Dashboard JavaScript Application
 */
class TechnicianApp {
    constructor() {
        this.currentSection = 'dashboard';
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.loadDashboardData();
        this.loadTestQueue();
        this.loadEquipment();
    }

    setupEventListeners() {
        // Navigation
        document.querySelectorAll('.nav-link').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const section = e.target.closest('.nav-link').dataset.section;
                this.showSection(section);
            });
        });

        // Test processing form
        const processingForm = document.getElementById('test-processing-form');
        if (processingForm) {
            processingForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.processTest();
            });
        }

        // Auto-refresh data every 30 seconds
        setInterval(() => {
            if (this.currentSection === 'dashboard') {
                this.loadDashboardData();
                this.loadTestQueue();
            }
        }, 30000);
    }

    showSection(sectionName) {
        // Hide all sections
        document.querySelectorAll('.content-section').forEach(section => {
            section.style.display = 'none';
        });

        // Remove active class from all nav links
        document.querySelectorAll('.nav-link').forEach(link => {
            link.classList.remove('active');
        });

        // Show selected section
        const targetSection = document.getElementById(`${sectionName}-section`);
        if (targetSection) {
            targetSection.style.display = 'block';
        }

        // Add active class to selected nav link
        const activeLink = document.querySelector(`[data-section="${sectionName}"]`);
        if (activeLink) {
            activeLink.classList.add('active');
        }

        this.currentSection = sectionName;

        // Load section-specific data
        switch (sectionName) {
            case 'equipment':
                this.loadEquipment();
                break;
            case 'results':
                this.loadResults();
                break;
        }
    }

    async loadDashboardData() {
        try {
            // Load statistics
            const [visits, equipment] = await Promise.all([
                this.fetchData('/visits'),
                this.fetchData('/api/v1/equipment')
            ]);

            // Extract all tests from visits
            const allTests = [];
            visits.forEach(visit => {
                if (visit.labTests && visit.labTests.length > 0) {
                    allTests.push(...visit.labTests);
                }
            });

            // Calculate statistics
            const pendingTests = allTests.filter(t => t.status === 'PENDING').length;
            const inProgressTests = allTests.filter(t => t.status === 'IN_PROGRESS').length;
            const completedToday = this.getTodayCount(allTests.filter(t => t.status === 'COMPLETED'));
            const activeEquipment = equipment.filter(e => e.status === 'ACTIVE').length;

            // Update dashboard cards
            document.getElementById('pending-tests').textContent = pendingTests;
            document.getElementById('in-progress-tests').textContent = inProgressTests;
            document.getElementById('completed-today').textContent = completedToday;
            document.getElementById('equipment-active').textContent = activeEquipment;

        } catch (error) {
            console.error('Error loading dashboard data:', error);
            this.showNotification('Error loading dashboard data', 'error');
        }
    }

    async loadTestQueue() {
        try {
            // Load visits with tests instead of separate lab-tests endpoint
            const visits = await this.fetchData('/visits');
            const allTests = [];

            // Extract all tests from all visits
            visits.forEach(visit => {
                if (visit.labTests && visit.labTests.length > 0) {
                    visit.labTests.forEach(test => {
                        allTests.push({
                            ...test,
                            patientName: visit.patientDetails?.name || 'Unknown Patient',
                            patientPhone: visit.patientDetails?.phone || 'N/A'
                        });
                    });
                }
            });

            const pendingTests = allTests.filter(t => t.status === 'PENDING' || t.status === 'IN_PROGRESS');

            const tableBody = document.querySelector('#test-queue-table tbody');
            if (!tableBody) return;

            if (pendingTests.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="7" class="text-center">No pending tests</td></tr>';
                return;
            }

            tableBody.innerHTML = pendingTests.map(test => {
                const canStart = test.status === 'PENDING' && this.isSampleReady(test);
                const sampleStatus = this.getSampleStatus(test);

                return `
                    <tr>
                        <td>${test.testId}</td>
                        <td>${test.patientName}</td>
                        <td>${test.testTemplate?.name || 'Unknown Test'}</td>
                        <td>${this.getSampleTypeForTest(test.testTemplate?.name)}</td>
                        <td><span class="sample-status ${sampleStatus.class}">${sampleStatus.text}</span></td>
                        <td><span class="status-badge status-${test.status.toLowerCase().replace('_', '-')}">${test.status}</span></td>
                        <td>
                            ${canStart ?
                                `<button class="btn btn-sm btn-primary" onclick="technicianApp.startTest(${test.testId})">
                                    <i class="fas fa-play"></i> Start
                                </button>` :
                                `<button class="btn btn-sm btn-secondary" disabled title="Sample not ready">
                                    <i class="fas fa-clock"></i> Waiting
                                </button>`
                            }
                            <button class="btn btn-sm btn-success" onclick="technicianApp.viewTest(${test.testId})">
                                <i class="fas fa-eye"></i> View
                            </button>
                        </td>
                    </tr>
                `;
            }).join('');

        } catch (error) {
            console.error('Error loading test queue:', error);
        }
    }

    async loadEquipment() {
        try {
            const equipment = await this.fetchData('/api/v1/equipment');
            
            const tableBody = document.querySelector('#equipment-table tbody');
            if (!tableBody) return;

            if (equipment.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="6" class="text-center">No equipment found</td></tr>';
                return;
            }

            // Load equipment dropdown for test processing
            const equipmentSelect = document.getElementById('equipment-used');
            if (equipmentSelect) {
                equipmentSelect.innerHTML = '<option value="">Select Equipment</option>' +
                    equipment.filter(e => e.status === 'ACTIVE').map(e => 
                        `<option value="${e.id}">${e.name} (${e.model})</option>`
                    ).join('');
            }

            tableBody.innerHTML = equipment.map(eq => `
                <tr>
                    <td>
                        <div class="equipment-status">
                            <span class="status-indicator status-${eq.status.toLowerCase()}"></span>
                            ${eq.name}
                        </div>
                    </td>
                    <td>${eq.model}</td>
                    <td><span class="status-badge status-${eq.status.toLowerCase()}">${eq.status}</span></td>
                    <td>${eq.lastMaintenance ? this.formatDate(eq.lastMaintenance) : 'N/A'}</td>
                    <td>${eq.calibrationDue ? this.formatDate(eq.calibrationDue) : 'N/A'}</td>
                    <td>
                        <button class="btn btn-sm btn-primary" onclick="technicianApp.useEquipment(${eq.id})">
                            <i class="fas fa-cog"></i> Use
                        </button>
                        <button class="btn btn-sm btn-warning" onclick="technicianApp.maintainEquipment(${eq.id})">
                            <i class="fas fa-wrench"></i> Maintain
                        </button>
                    </td>
                </tr>
            `).join('');

        } catch (error) {
            console.error('Error loading equipment:', error);
        }
    }

    async loadResults() {
        try {
            const tests = await this.fetchData('/lab-tests');
            const completedTests = tests.filter(t => t.status === 'COMPLETED' || t.status === 'APPROVED');
            
            const tableBody = document.querySelector('#results-table tbody');
            if (!tableBody) return;

            if (completedTests.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="7" class="text-center">No results found</td></tr>';
                return;
            }

            tableBody.innerHTML = completedTests.slice(0, 20).map(test => `
                <tr>
                    <td>${test.testId}</td>
                    <td>Patient ${test.visitId}</td>
                    <td>Lab Test</td>
                    <td>${test.results ? 'Available' : 'Pending'}</td>
                    <td><span class="status-badge status-${test.status.toLowerCase()}">${test.status}</span></td>
                    <td>${test.resultsEnteredAt ? this.formatDate(test.resultsEnteredAt) : 'N/A'}</td>
                    <td>
                        <button class="btn btn-sm btn-primary" onclick="technicianApp.viewResults(${test.testId})">
                            <i class="fas fa-eye"></i> View
                        </button>
                        ${test.status === 'COMPLETED' ? 
                            `<button class="btn btn-sm btn-success" onclick="technicianApp.approveResults(${test.testId})">
                                <i class="fas fa-check"></i> Approve
                            </button>` : ''
                        }
                    </td>
                </tr>
            `).join('');

        } catch (error) {
            console.error('Error loading results:', error);
        }
    }

    async processTest() {
        try {
            const form = document.getElementById('test-processing-form');
            const formData = new FormData(form);
            
            const testId = formData.get('testId');
            const results = formData.get('results');
            const notes = formData.get('notes');

            // Parse results as JSON
            let parsedResults;
            try {
                parsedResults = JSON.parse(results);
            } catch (e) {
                // If not valid JSON, create a simple object
                parsedResults = { result: results, notes: notes };
            }

            const response = await fetch(`/visits/${testId}/tests/${testId}/results`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ results: parsedResults })
            });

            if (response.ok) {
                this.showNotification('Test results saved successfully', 'success');
                form.reset();
                this.loadDashboardData();
                this.loadTestQueue();
            } else {
                throw new Error('Failed to save test results');
            }

        } catch (error) {
            console.error('Error processing test:', error);
            this.showNotification('Error saving test results', 'error');
        }
    }

    async startTest(testId) {
        try {
            // Check if sample is ready before starting test
            const response = await fetch(`/visits`);
            const visits = await response.json();

            let testFound = false;
            let canStart = false;

            for (const visit of visits) {
                if (visit.labTests) {
                    const test = visit.labTests.find(t => t.testId === testId);
                    if (test) {
                        testFound = true;
                        canStart = this.isSampleReady(test);
                        break;
                    }
                }
            }

            if (!testFound) {
                this.showNotification('Test not found', 'error');
                return;
            }

            if (!canStart) {
                this.showNotification('Cannot start test: Sample not collected or not ready', 'warning');
                return;
            }

            // Update test status to IN_PROGRESS
            const updateResponse = await fetch(`/visits/${testId}/status`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ status: 'IN_PROGRESS' })
            });

            if (updateResponse.ok) {
                this.showSection('test-processing');
                document.getElementById('test-id').value = testId;
                this.showNotification(`Started processing test ${testId}`, 'success');
                this.loadTestQueue(); // Refresh the queue
            } else {
                this.showNotification('Failed to start test', 'error');
            }

        } catch (error) {
            console.error('Error starting test:', error);
            this.showNotification('Error starting test', 'error');
        }
    }

    viewTest(testId) {
        this.showNotification(`Viewing details for test ${testId}`, 'info');
        // In a real application, this would open a detailed view
    }

    useEquipment(equipmentId) {
        this.showNotification(`Equipment ${equipmentId} is now in use`, 'info');
        // In a real application, this would mark equipment as in use
    }

    maintainEquipment(equipmentId) {
        this.showNotification(`Maintenance scheduled for equipment ${equipmentId}`, 'info');
        // In a real application, this would open maintenance form
    }

    viewResults(testId) {
        this.showNotification(`Viewing results for test ${testId}`, 'info');
        // In a real application, this would show detailed results
    }

    async approveResults(testId) {
        try {
            const response = await fetch(`/visits/${testId}/tests/${testId}/approve`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ approvedBy: 'Lab Technician' })
            });

            if (response.ok) {
                this.showNotification(`Test ${testId} results approved`, 'success');
                this.loadResults();
                this.loadDashboardData();
            } else {
                throw new Error('Failed to approve results');
            }

        } catch (error) {
            console.error('Error approving results:', error);
            this.showNotification('Error approving results', 'error');
        }
    }

    filterResults() {
        const date = document.getElementById('result-date').value;
        const status = document.getElementById('result-status').value;
        
        this.showNotification(`Filtering results by date: ${date}, status: ${status}`, 'info');
        // In a real application, this would filter the results table
        this.loadResults();
    }

    // Utility methods
    async fetchData(endpoint) {
        try {
            const response = await fetch(endpoint);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            console.error(`Error fetching ${endpoint}:`, error);
            return [];
        }
    }

    getTodayCount(items) {
        const today = new Date().toDateString();
        return items.filter(item => 
            item.resultsEnteredAt && new Date(item.resultsEnteredAt).toDateString() === today
        ).length;
    }

    formatDate(dateString) {
        return new Date(dateString).toLocaleDateString('en-US');
    }

    formatDateTime(dateString) {
        return new Date(dateString).toLocaleString('en-US');
    }

    showNotification(message, type = 'info') {
        // Create notification element
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.innerHTML = `
            <i class="fas fa-${this.getNotificationIcon(type)}"></i>
            <span>${message}</span>
            <button class="notification-close" onclick="this.parentElement.remove()">×</button>
        `;

        // Add to page
        document.body.appendChild(notification);

        // Auto-remove after 5 seconds
        setTimeout(() => {
            if (notification.parentElement) {
                notification.remove();
            }
        }, 5000);
    }

    getNotificationIcon(type) {
        const icons = {
            success: 'check-circle',
            error: 'exclamation-circle',
            warning: 'exclamation-triangle',
            info: 'info-circle'
        };
        return icons[type] || 'info-circle';
    }

    // Helper method for sample types
    getSampleTypeForTest(testName) {
        if (!testName) return 'WHOLE_BLOOD';

        const sampleTypeMap = {
            'Complete Blood Count (CBC)': 'WHOLE_BLOOD',
            'Blood Sugar (Fasting)': 'SERUM',
            'Lipid Profile': 'SERUM',
            'Liver Function Test (LFT)': 'SERUM',
            'Kidney Function Test': 'SERUM',
            'Thyroid Function Test': 'SERUM',
            'Urine Analysis': 'RANDOM_URINE',
            'Stool Analysis': 'STOOL'
        };

        return sampleTypeMap[testName] || 'WHOLE_BLOOD';
    }

    // Sample status checking methods
    isSampleReady(test) {
        // For now, assume sample is ready if test status is PENDING
        // In a real implementation, this would check the sample collection status
        return test.status === 'PENDING' || test.status === 'IN_PROGRESS';
    }

    getSampleStatus(test) {
        if (test.status === 'SAMPLE_PENDING') {
            return { text: 'Sample Collected', class: 'sample-collected' };
        } else if (test.status === 'PENDING') {
            return { text: 'Sample Ready', class: 'sample-ready' };
        } else if (test.status === 'IN_PROGRESS') {
            return { text: 'Processing', class: 'sample-processing' };
        } else {
            return { text: 'Not Collected', class: 'sample-not-collected' };
        }
    }
}

// Logout function
function logout() {
    if (confirm('Are you sure you want to logout?')) {
        window.location.href = '/logout';
    }
}

// Initialize the application
const technicianApp = new TechnicianApp();
