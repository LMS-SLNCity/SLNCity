// Reception Dashboard JavaScript
class ReceptionApp {
    constructor() {
        this.apiEndpoints = {
            visits: '/visits',
            billing: '/billing',
            patients: '/api/v1/patients',
            testTemplates: '/test-templates'
        };
        this.currentVisits = [];
        this.selectedTests = [];
        this.currentVisitId = null;
        this.testTemplates = [];
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.loadDashboardData();
        this.startAutoRefresh();
    }

    setupEventListeners() {
        // Quick action buttons
        document.getElementById('new-visit-btn').addEventListener('click', () => {
            this.openModal('new-visit-modal');
        });

        document.getElementById('search-patient-btn').addEventListener('click', () => {
            this.openModal('search-patient-modal');
        });

        document.getElementById('view-queue-btn').addEventListener('click', () => {
            this.refreshQueue();
        });

        document.getElementById('billing-btn').addEventListener('click', () => {
            window.location.href = '/billing';
        });

        // Refresh buttons
        document.getElementById('refresh-queue').addEventListener('click', () => {
            this.refreshQueue();
        });

        document.getElementById('view-all-visits').addEventListener('click', () => {
            this.viewAllVisits();
        });

        // Form submissions
        document.getElementById('new-visit-form').addEventListener('submit', (e) => {
            this.handleNewVisitSubmit(e);
        });

        document.getElementById('search-btn').addEventListener('click', () => {
            this.searchPatient();
        });

        // Modal close on outside click
        document.querySelectorAll('.modal').forEach(modal => {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    this.closeModal(modal.id);
                }
            });
        });
    }

    async loadDashboardData() {
        try {
            await Promise.all([
                this.loadTodayStats(),
                this.loadPatientQueue(),
                this.loadRecentVisits()
            ]);
        } catch (error) {
            console.error('Error loading dashboard data:', error);
            this.showNotification('Error loading dashboard data', 'error');
        }
    }

    async loadTodayStats() {
        try {
            // Load visits data
            const visitsResponse = await fetch(this.apiEndpoints.visits);
            if (visitsResponse.ok) {
                const visits = await visitsResponse.json();
                const today = new Date().toDateString();
                
                const todayVisits = visits.filter(visit => 
                    new Date(visit.visitDate).toDateString() === today
                );
                
                const pendingVisits = todayVisits.filter(visit => 
                    visit.status === 'PENDING' || visit.status === 'SCHEDULED'
                );
                
                const completedVisits = todayVisits.filter(visit => 
                    visit.status === 'COMPLETED'
                );

                document.getElementById('today-visits').textContent = todayVisits.length;
                document.getElementById('pending-visits').textContent = pendingVisits.length;
                document.getElementById('completed-visits').textContent = completedVisits.length;
            }

            // Load billing data for revenue
            const billingResponse = await fetch(this.apiEndpoints.billing);
            if (billingResponse.ok) {
                const bills = await billingResponse.json();
                const today = new Date().toDateString();
                
                const todayBills = bills.filter(bill => 
                    new Date(bill.billDate).toDateString() === today
                );
                
                const todayRevenue = todayBills.reduce((sum, bill) => sum + (bill.totalAmount || 0), 0);
                document.getElementById('today-revenue').textContent = `₹${todayRevenue.toLocaleString()}`;
            }

        } catch (error) {
            console.error('Error loading today stats:', error);
        }
    }

    async loadPatientQueue() {
        try {
            const response = await fetch(this.apiEndpoints.visits);
            if (response.ok) {
                const visits = await response.json();
                const today = new Date().toDateString();
                
                const queueVisits = visits.filter(visit => 
                    new Date(visit.visitDate).toDateString() === today &&
                    (visit.status === 'PENDING' || visit.status === 'SCHEDULED')
                ).sort((a, b) => new Date(a.visitDate) - new Date(b.visitDate));

                this.renderPatientQueue(queueVisits);
            }
        } catch (error) {
            console.error('Error loading patient queue:', error);
        }
    }

    renderPatientQueue(visits) {
        const queueContainer = document.getElementById('patient-queue');
        
        if (visits.length === 0) {
            queueContainer.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-users" style="font-size: 3rem; color: var(--secondary-color); margin-bottom: 1rem;"></i>
                    <p>No patients in queue</p>
                </div>
            `;
            return;
        }

        queueContainer.innerHTML = visits.map((visit, index) => `
            <div class="queue-item">
                <div class="queue-patient-info">
                    <h4>${visit.patientDetails?.name || 'N/A'}</h4>
                    <p>Phone: ${visit.patientDetails?.phone || 'N/A'} | Age: ${visit.patientDetails?.age || 'N/A'} | Gender: ${visit.patientDetails?.gender || 'N/A'}</p>
                    <small>Created: ${new Date(visit.createdAt).toLocaleTimeString()}</small>
                </div>
                <div class="queue-actions">
                    <button class="btn btn-sm btn-primary" onclick="receptionApp.viewVisit(${visit.visitId})">
                        <i class="fas fa-eye"></i> View Details
                    </button>
                    <button class="btn btn-sm btn-success" onclick="receptionApp.orderTests(${visit.visitId})">
                        <i class="fas fa-vial"></i> Order Tests
                    </button>
                </div>
            </div>
        `).join('');
    }

    async loadRecentVisits() {
        try {
            const response = await fetch(this.apiEndpoints.visits);
            if (response.ok) {
                const visits = await response.json();
                const recentVisits = visits
                    .sort((a, b) => new Date(b.visitDate) - new Date(a.visitDate))
                    .slice(0, 10);

                this.renderRecentVisits(recentVisits);
            }
        } catch (error) {
            console.error('Error loading recent visits:', error);
        }
    }

    renderRecentVisits(visits) {
        const tbody = document.getElementById('recent-visits-body');

        tbody.innerHTML = visits.map(visit => `
            <tr>
                <td>#${visit.visitId}</td>
                <td>${visit.patientDetails?.name || 'N/A'}</td>
                <td>${visit.patientDetails?.phone || 'N/A'}</td>
                <td>${new Date(visit.createdAt).toLocaleString()}</td>
                <td>
                    <span class="status-badge status-${visit.status?.toLowerCase() || 'pending'}">
                        ${visit.status || 'Pending'}
                    </span>
                </td>
                <td>
                    <button class="btn btn-sm btn-primary" onclick="receptionApp.viewVisit(${visit.visitId})">
                        <i class="fas fa-eye"></i> View Details
                    </button>
                </td>
            </tr>
        `).join('');
    }

    async handleNewVisitSubmit(e) {
        e.preventDefault();
        
        const formData = new FormData(e.target);
        // Create the correct API format with patientDetails object
        const visitData = {
            patientDetails: {
                name: formData.get('name'),
                age: formData.get('age'),
                gender: formData.get('gender'),
                phone: formData.get('phone'),
                email: formData.get('email') || '',
                address: formData.get('address') || '',
                patientId: `PAT${Date.now()}`, // Generate unique patient ID
                doctorRef: formData.get('doctorRef') || 'Walk-in',
                emergencyContact: formData.get('emergencyContact') || ''
            }
        };

        try {
            const response = await fetch(this.apiEndpoints.visits, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(visitData)
            });

            if (response.ok) {
                const result = await response.json();
                this.showNotification(`Visit registered successfully! Visit ID: ${result.visitId}`, 'success');
                this.closeModal('new-visit-modal');
                e.target.reset();
                await this.loadDashboardData();
            } else {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `HTTP ${response.status}: Failed to register visit`);
            }
        } catch (error) {
            console.error('Error registering visit:', error);
            this.showNotification(`Error registering visit: ${error.message}`, 'error');
        }
    }

    async searchPatient() {
        const phone = document.getElementById('search-phone').value.trim();
        if (!phone) {
            this.showNotification('Please enter a phone number', 'warning');
            return;
        }

        try {
            const response = await fetch(this.apiEndpoints.visits);
            if (response.ok) {
                const visits = await response.json();
                const patientVisits = visits.filter(visit => 
                    visit.patientPhone.includes(phone)
                );

                this.renderSearchResults(patientVisits);
            }
        } catch (error) {
            console.error('Error searching patient:', error);
            this.showNotification('Error searching patient', 'error');
        }
    }

    renderSearchResults(visits) {
        const resultsContainer = document.getElementById('search-results');
        
        if (visits.length === 0) {
            resultsContainer.innerHTML = `
                <div class="empty-state">
                    <p>No patient found with this phone number</p>
                </div>
            `;
            return;
        }

        // Group visits by patient
        const patients = {};
        visits.forEach(visit => {
            const key = `${visit.patientName}_${visit.patientPhone}`;
            if (!patients[key]) {
                patients[key] = {
                    name: visit.patientName,
                    phone: visit.patientPhone,
                    email: visit.patientEmail,
                    visits: []
                };
            }
            patients[key].visits.push(visit);
        });

        resultsContainer.innerHTML = Object.values(patients).map(patient => `
            <div class="search-result-item" onclick="receptionApp.selectPatient('${patient.phone}')">
                <h4>${patient.name}</h4>
                <p>Phone: ${patient.phone}</p>
                <p>Email: ${patient.email || 'Not provided'}</p>
                <small>${patient.visits.length} previous visit(s)</small>
            </div>
        `).join('');
    }

    selectPatient(phone) {
        // Auto-fill new visit form with patient data
        document.getElementById('patient-phone').value = phone;
        this.closeModal('search-patient-modal');
        this.openModal('new-visit-modal');
    }

    async startVisit(visitId) {
        try {
            const response = await fetch(`${this.apiEndpoints.visits}/${visitId}`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ status: 'IN_PROGRESS' })
            });

            if (response.ok) {
                this.showNotification('Visit started successfully!', 'success');
                await this.loadDashboardData();
            }
        } catch (error) {
            console.error('Error starting visit:', error);
            this.showNotification('Error starting visit', 'error');
        }
    }

    editVisit(visitId) {
        // Navigate to edit visit page or open edit modal
        console.log('Edit visit:', visitId);
        this.showNotification('Edit functionality coming soon', 'info');
    }

    viewVisit(visitId) {
        // Navigate to visit details page
        console.log('View visit:', visitId);
        this.showNotification('View details functionality coming soon', 'info');
    }

    viewAllVisits() {
        // Navigate to all visits page
        console.log('View all visits');
        this.showNotification('View all visits functionality coming soon', 'info');
    }

    refreshQueue() {
        const refreshBtn = document.getElementById('refresh-queue');
        const icon = refreshBtn.querySelector('i');
        
        icon.style.animation = 'spin 1s linear infinite';
        
        this.loadPatientQueue().then(() => {
            setTimeout(() => {
                icon.style.animation = '';
            }, 1000);
        });
    }

    openModal(modalId) {
        document.getElementById(modalId).classList.add('show');
        document.body.style.overflow = 'hidden';
    }

    closeModal(modalId) {
        document.getElementById(modalId).classList.remove('show');
        document.body.style.overflow = '';
    }

    startAutoRefresh() {
        // Refresh queue every 30 seconds
        setInterval(() => {
            this.loadPatientQueue();
        }, 30000);

        // Refresh stats every 60 seconds
        setInterval(() => {
            this.loadTodayStats();
        }, 60000);
    }

    showNotification(message, type = 'info') {
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.textContent = message;
        notification.style.cssText = `
            position: fixed;
            top: 1rem;
            right: 1rem;
            padding: 1rem;
            border-radius: 6px;
            color: white;
            z-index: 1001;
            animation: slideIn 0.3s ease;
        `;
        
        const colors = {
            success: 'var(--success-color)',
            error: 'var(--danger-color)',
            warning: 'var(--warning-color)',
            info: 'var(--info-color)'
        };
        notification.style.background = colors[type] || colors.info;
        
        document.body.appendChild(notification);
        
        setTimeout(() => {
            notification.remove();
        }, 3000);
    }
    // Visit Details and Test Ordering Functions
    async viewVisit(visitId) {
        try {
            const response = await fetch(`${this.apiEndpoints.visits}/${visitId}`);
            if (response.ok) {
                const visit = await response.json();
                this.displayVisitDetails(visit);
                this.openModal('visit-details-modal');
            } else {
                throw new Error('Failed to load visit details');
            }
        } catch (error) {
            console.error('Error loading visit details:', error);
            this.showNotification('Error loading visit details', 'error');
        }
    }

    displayVisitDetails(visit) {
        const content = document.getElementById('visit-details-content');
        const patientDetails = visit.patientDetails;

        content.innerHTML = `
            <div class="visit-details">
                <div class="visit-info">
                    <h4><i class="fas fa-user"></i> Patient Information</h4>
                    <div class="patient-info">
                        <div class="info-item">
                            <span class="info-label">Name</span>
                            <span class="info-value">${patientDetails.name || 'N/A'}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Age</span>
                            <span class="info-value">${patientDetails.age || 'N/A'}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Gender</span>
                            <span class="info-value">${patientDetails.gender || 'N/A'}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Phone</span>
                            <span class="info-value">${patientDetails.phone || 'N/A'}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Email</span>
                            <span class="info-value">${patientDetails.email || 'N/A'}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Visit Status</span>
                            <span class="info-value">${visit.status}</span>
                        </div>
                    </div>
                    <div class="visit-actions">
                        <button class="btn btn-primary" onclick="receptionApp.orderTests(${visit.visitId})">
                            <i class="fas fa-vial"></i> Order Tests
                        </button>
                    </div>
                </div>
                <div class="visit-tests">
                    <h4><i class="fas fa-flask"></i> Ordered Tests</h4>
                    <div id="visit-tests-list">
                        ${this.renderVisitTests(visit.labTests || [])}
                    </div>
                </div>
            </div>
        `;
    }

    renderVisitTests(tests) {
        if (!tests || tests.length === 0) {
            return '<p class="text-muted">No tests ordered yet.</p>';
        }

        return tests.map(test => `
            <div class="test-list-item">
                <div>
                    <strong>${test.testTemplate?.name || 'Unknown Test'}</strong>
                    <br>
                    <small class="text-muted">₹${test.price}</small>
                </div>
                <span class="test-status ${test.status.toLowerCase()}">${test.status}</span>
            </div>
        `).join('');
    }

    async orderTests(visitId) {
        this.currentVisitId = visitId;
        this.selectedTests = [];
        await this.loadTestTemplates();
        this.displayTestSelection();
        this.closeModal('visit-details-modal');
        this.openModal('order-tests-modal');
    }

    async loadTestTemplates() {
        try {
            const response = await fetch(this.apiEndpoints.testTemplates);
            if (response.ok) {
                this.testTemplates = await response.json();
            } else {
                throw new Error('Failed to load test templates');
            }
        } catch (error) {
            console.error('Error loading test templates:', error);
            this.showNotification('Error loading test templates', 'error');
        }
    }

    displayTestSelection() {
        const container = document.getElementById('available-tests');
        container.innerHTML = this.testTemplates.map(template => `
            <div class="test-item" data-template-id="${template.templateId}" onclick="receptionApp.toggleTestSelection(${template.templateId})">
                <h5>${template.name}</h5>
                <p>${template.description}</p>
                <div class="test-price">₹${template.basePrice}</div>
            </div>
        `).join('');

        this.updateSelectedTests();
    }

    toggleTestSelection(templateId) {
        const template = this.testTemplates.find(t => t.templateId === templateId);
        const existingIndex = this.selectedTests.findIndex(t => t.templateId === templateId);

        if (existingIndex >= 0) {
            // Remove from selection
            this.selectedTests.splice(existingIndex, 1);
        } else {
            // Add to selection
            this.selectedTests.push(template);
        }

        this.updateTestSelectionUI();
        this.updateSelectedTests();
    }

    updateTestSelectionUI() {
        // Update visual selection in available tests
        document.querySelectorAll('.test-item').forEach(item => {
            const templateId = parseInt(item.dataset.templateId);
            const isSelected = this.selectedTests.some(t => t.templateId === templateId);
            item.classList.toggle('selected', isSelected);
        });
    }

    updateSelectedTests() {
        const container = document.getElementById('selected-tests');
        const totalElement = document.getElementById('tests-total');

        if (this.selectedTests.length === 0) {
            container.innerHTML = '<p class="text-muted">No tests selected</p>';
            totalElement.textContent = '0.00';
            return;
        }

        container.innerHTML = this.selectedTests.map(test => `
            <div class="selected-test-item">
                <div>
                    <strong>${test.name}</strong>
                    <br>
                    <small class="text-muted">${test.description}</small>
                </div>
                <div>
                    <span class="test-price">₹${test.basePrice}</span>
                    <button class="btn btn-sm btn-danger ml-2" onclick="receptionApp.toggleTestSelection(${test.templateId})">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
            </div>
        `).join('');

        const total = this.selectedTests.reduce((sum, test) => sum + parseFloat(test.basePrice), 0);
        totalElement.textContent = total.toFixed(2);
    }

    async confirmTestOrder() {
        if (this.selectedTests.length === 0) {
            this.showNotification('Please select at least one test', 'error');
            return;
        }

        try {
            const promises = this.selectedTests.map(test =>
                fetch(`${this.apiEndpoints.visits}/${this.currentVisitId}/tests`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        testTemplateId: test.templateId,
                        price: test.basePrice
                    })
                })
            );

            const responses = await Promise.all(promises);
            const allSuccessful = responses.every(response => response.ok);

            if (allSuccessful) {
                this.showNotification(`${this.selectedTests.length} test(s) ordered successfully!`, 'success');
                this.closeModal('order-tests-modal');
                this.selectedTests = [];
                this.currentVisitId = null;
                await this.loadDashboardData();
            } else {
                throw new Error('Some tests failed to order');
            }
        } catch (error) {
            console.error('Error ordering tests:', error);
            this.showNotification('Error ordering tests', 'error');
        }
    }
}

// Add CSS for animations
const style = document.createElement('style');
style.textContent = `
    @keyframes spin {
        from { transform: rotate(0deg); }
        to { transform: rotate(360deg); }
    }
    
    @keyframes slideIn {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    
    .empty-state {
        text-align: center;
        padding: 2rem;
        color: var(--secondary-color);
    }
`;
document.head.appendChild(style);

// Initialize reception app when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.receptionApp = new ReceptionApp();
});
