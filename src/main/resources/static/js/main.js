// Lab Operations Management System - Main JavaScript

class LabOperationsApp {
    constructor() {
        // API Configuration - Different endpoints have different base paths
        this.apiEndpoints = {
            visits: '/visits',
            billing: '/billing',
            equipment: '/api/v1/equipment',
            inventory: '/api/v1/inventory',
            samples: '/api/v1/samples',
            tests: '/api/v1/tests',
            reports: '/api/v1/reports',
            networkConnections: '/api/v1/network-connections',
            machineIdIssues: '/api/v1/machine-id-issues',
            health: '/actuator/health'
        };
        this.currentPage = 'dashboard';
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.loadInitialData();
        this.setupNavigation();
    }

    setupEventListeners() {
        // Global error handling
        window.addEventListener('error', (e) => {
            console.error('Global error:', e.error);
            this.showNotification('An unexpected error occurred', 'error');
        });

        // Handle form submissions
        document.addEventListener('submit', (e) => {
            if (e.target.id === 'new-visit-form') {
                e.preventDefault();
                this.handleNewVisitSubmission(e.target);
            } else if (e.target.classList.contains('ajax-form')) {
                e.preventDefault();
                this.handleFormSubmission(e.target);
            }
        });

        // Handle navigation clicks
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('nav-item')) {
                e.preventDefault();
                const pageId = e.target.getAttribute('data-page');
                if (pageId) {
                    this.showPage(pageId);
                }
            }

            // Handle modal clicks (close on backdrop click)
            if (e.target.classList.contains('modal')) {
                this.closeModal(e.target.id);
            }
        });
    }

    setupNavigation() {
        const currentPath = window.location.pathname;
        const navLinks = document.querySelectorAll('.nav-link');
        
        navLinks.forEach(link => {
            if (link.getAttribute('href') === currentPath) {
                link.classList.add('active');
            }
        });
    }

    async loadInitialData() {
        try {
            // Load system health
            await this.loadSystemHealth();
            
            // Load basic statistics
            await this.loadDashboardStats();
            
        } catch (error) {
            console.error('Failed to load initial data:', error);
            this.showNotification('Failed to load system data', 'error');
        }
    }

    async loadSystemHealth() {
        try {
            const response = await fetch(this.apiEndpoints.health);
            const health = await response.json();

            const healthIndicator = document.getElementById('system-health');
            if (healthIndicator) {
                healthIndicator.className = `badge ${health.status === 'UP' ? 'badge-success' : 'badge-danger'}`;
                healthIndicator.textContent = health.status;
            }
        } catch (error) {
            console.error('Failed to load system health:', error);
        }
    }

    async loadDashboardStats() {
        try {
            // Load visits stats
            const visitsResponse = await fetch(this.apiEndpoints.visits);
            const visits = await visitsResponse.json();
            this.updateStatCard('total-visits', visits.length);

            // Load pending tests (from visits with pending status)
            const pendingVisits = visits.filter(visit => visit.status === 'PENDING' || visit.status === 'IN_PROGRESS');
            this.updateStatCard('pending-tests', pendingVisits.length);

            // Load equipment stats
            const equipmentResponse = await fetch(this.apiEndpoints.equipment);
            const equipment = await equipmentResponse.json();
            const activeEquipment = equipment.filter(eq => eq.status === 'ACTIVE');
            this.updateStatCard('active-equipment', activeEquipment.length);

            // Load network status
            try {
                const networkResponse = await fetch(this.apiEndpoints.networkConnections);
                const connections = await networkResponse.json();
                const connectedDevices = connections.filter(conn => conn.connectionStatus === 'CONNECTED');
                this.updateStatCard('network-status', `${connectedDevices.length} Connected`);
            } catch (networkError) {
                this.updateStatCard('network-status', 'N/A');
            }

        } catch (error) {
            console.error('Failed to load dashboard stats:', error);
            // Set default values on error
            this.updateStatCard('total-visits', '0');
            this.updateStatCard('pending-tests', '0');
            this.updateStatCard('active-equipment', '0');
            this.updateStatCard('network-status', 'Offline');
        }
    }

    updateStatCard(elementId, value) {
        const element = document.getElementById(elementId);
        if (element) {
            element.textContent = value;
        }
    }

    updateStatsCard(cardId, stats) {
        const card = document.getElementById(cardId);
        if (!card) return;

        // Update total count
        const totalElement = card.querySelector('.stats-value');
        if (totalElement && stats.total !== undefined) {
            totalElement.textContent = stats.total;
        }

        // Update status breakdown
        const statusContainer = card.querySelector('.status-breakdown');
        if (statusContainer && stats.statusBreakdown) {
            statusContainer.innerHTML = '';
            Object.entries(stats.statusBreakdown).forEach(([status, count]) => {
                const statusElement = document.createElement('div');
                statusElement.className = 'status-item';
                statusElement.innerHTML = `
                    <span class="badge badge-info">${status}</span>
                    <span>${count}</span>
                `;
                statusContainer.appendChild(statusElement);
            });
        }
    }

    async handleFormSubmission(form) {
        const formData = new FormData(form);
        const url = form.action || form.getAttribute('data-url');
        const method = form.method || 'POST';

        try {
            this.showLoading(form);
            
            const response = await fetch(url, {
                method: method,
                body: formData,
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });

            if (response.ok) {
                const result = await response.json();
                this.showNotification('Operation completed successfully', 'success');
                
                // Refresh data if needed
                if (form.hasAttribute('data-refresh')) {
                    await this.loadInitialData();
                }
                
                // Reset form
                form.reset();
            } else {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
        } catch (error) {
            console.error('Form submission error:', error);
            this.showNotification('Operation failed: ' + error.message, 'error');
        } finally {
            this.hideLoading(form);
        }
    }

    navigateTo(path) {
        window.location.href = path;
    }

    showPage(pageId) {
        // Hide all pages
        const pages = document.querySelectorAll('.page');
        pages.forEach(page => page.classList.remove('active'));

        // Show selected page
        const targetPage = document.getElementById(`${pageId}-page`);
        if (targetPage) {
            targetPage.classList.add('active');
        }

        // Update navigation
        const navItems = document.querySelectorAll('.nav-item');
        navItems.forEach(item => item.classList.remove('active'));

        const activeNavItem = document.querySelector(`[data-page="${pageId}"]`);
        if (activeNavItem) {
            activeNavItem.classList.add('active');
        }

        // Update page title
        const pageTitle = document.getElementById('page-title');
        if (pageTitle) {
            pageTitle.textContent = this.getPageTitle(pageId);
        }

        // Load page-specific data
        this.loadPageData(pageId);
        this.currentPage = pageId;
    }

    getPageTitle(pageId) {
        const titles = {
            dashboard: 'Dashboard',
            visits: 'Patient Visits',
            equipment: 'Lab Equipment',
            inventory: 'Inventory Management',
            samples: 'Sample Management',
            tests: 'Lab Tests',
            reports: 'Reports',
            billing: 'Billing',
            network: 'Network Connections',
            settings: 'Settings'
        };
        return titles[pageId] || 'Lab Operations';
    }

    async loadPageData(pageId) {
        try {
            switch (pageId) {
                case 'visits':
                    await this.loadVisitsData();
                    break;
                case 'equipment':
                    await this.loadEquipmentData();
                    break;
                case 'network':
                    await this.loadNetworkData();
                    break;
                case 'billing':
                    await this.loadBillingData();
                    break;
                default:
                    // Dashboard or other pages
                    break;
            }
        } catch (error) {
            console.error(`Failed to load data for ${pageId}:`, error);
            this.showNotification(`Failed to load ${pageId} data`, 'error');
        }
    }

    showLoading(element) {
        const submitBtn = element.querySelector('button[type="submit"]');
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<span class="loading"></span> Processing...';
        }
    }

    hideLoading(element) {
        const submitBtn = element.querySelector('button[type="submit"]');
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.innerHTML = submitBtn.getAttribute('data-original-text') || 'Submit';
        }
    }

    showNotification(message, type = 'info') {
        // Create notification element
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.innerHTML = `
            <div class="notification-content">
                <span>${message}</span>
                <button class="notification-close">&times;</button>
            </div>
        `;

        // Add to page
        document.body.appendChild(notification);

        // Auto remove after 5 seconds
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 5000);

        // Handle close button
        notification.querySelector('.notification-close').addEventListener('click', () => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        });
    }

    // Page-specific data loading methods
    async loadVisitsData() {
        try {
            const response = await fetch(this.apiEndpoints.visits);
            const visits = await response.json();
            this.renderVisitsTable(visits);
        } catch (error) {
            console.error('Failed to load visits:', error);
            this.showNotification('Failed to load visits data', 'error');
        }
    }

    async loadEquipmentData() {
        try {
            const response = await fetch(this.apiEndpoints.equipment);
            const equipment = await response.json();
            this.renderEquipmentGrid(equipment);
        } catch (error) {
            console.error('Failed to load equipment:', error);
            this.showNotification('Failed to load equipment data', 'error');
        }
    }

    async loadNetworkData() {
        try {
            const response = await fetch(this.apiEndpoints.networkConnections);
            const connections = await response.json();
            this.renderNetworkGrid(connections);
        } catch (error) {
            console.error('Failed to load network data:', error);
            this.showNotification('Failed to load network data', 'error');
        }
    }

    async loadBillingData() {
        try {
            const response = await fetch(this.apiEndpoints.billing);
            const bills = await response.json();
            this.renderBillingTable(bills);
        } catch (error) {
            console.error('Failed to load billing data:', error);
            this.showNotification('Failed to load billing data', 'error');
        }
    }

    // Rendering methods
    renderVisitsTable(visits) {
        const tableBody = document.getElementById('visits-table-body');
        if (!tableBody) return;

        if (visits.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="5" class="empty-state">
                        <i class="fas fa-user-injured"></i>
                        <p>No visits found</p>
                    </td>
                </tr>
            `;
            return;
        }

        tableBody.innerHTML = visits.map(visit => `
            <tr>
                <td>${visit.visitId}</td>
                <td>${visit.patientDetails?.name || 'N/A'}</td>
                <td>${this.formatDate(visit.createdAt)}</td>
                <td><span class="badge badge-${this.getStatusClass(visit.status)}">${visit.status}</span></td>
                <td>
                    <button class="btn btn-sm" onclick="app.viewVisit(${visit.visitId})">View</button>
                    <button class="btn btn-sm btn-secondary" onclick="app.editVisit(${visit.visitId})">Edit</button>
                </td>
            </tr>
        `).join('');
    }

    renderEquipmentGrid(equipment) {
        const grid = document.getElementById('equipment-grid');
        if (!grid) return;

        if (equipment.length === 0) {
            grid.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-microscope"></i>
                    <p>No equipment found</p>
                </div>
            `;
            return;
        }

        grid.innerHTML = equipment.map(eq => `
            <div class="equipment-card">
                <div class="equipment-status ${eq.status.toLowerCase()}"></div>
                <h4>${eq.name}</h4>
                <p>Model: ${eq.model}</p>
                <p>Status: ${eq.status}</p>
                <div class="equipment-actions">
                    <button class="btn btn-sm" onclick="app.viewEquipment(${eq.id})">View</button>
                    <button class="btn btn-sm btn-secondary" onclick="app.maintainEquipment(${eq.id})">Maintain</button>
                </div>
            </div>
        `).join('');
    }

    renderNetworkGrid(connections) {
        const grid = document.getElementById('network-status-grid');
        if (!grid) return;

        if (connections.length === 0) {
            grid.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-wifi"></i>
                    <p>No network connections found</p>
                </div>
            `;
            return;
        }

        grid.innerHTML = connections.map(conn => `
            <div class="network-card">
                <div class="network-status ${conn.connectionStatus.toLowerCase()}"></div>
                <h4>${conn.machineId}</h4>
                <p>IP: ${conn.ipAddress || 'N/A'}</p>
                <p>Status: ${conn.connectionStatus}</p>
                <p>Signal: ${this.getSignalStrength(conn.signalStrength)}</p>
            </div>
        `).join('');
    }

    // Utility methods
    formatDate(dateString) {
        if (!dateString) return 'N/A';
        return new Date(dateString).toLocaleDateString();
    }

    getStatusClass(status) {
        const statusClasses = {
            'PENDING': 'warning',
            'IN_PROGRESS': 'info',
            'COMPLETED': 'success',
            'APPROVED': 'success',
            'BILLED': 'primary',
            'ACTIVE': 'success',
            'INACTIVE': 'secondary',
            'CONNECTED': 'success',
            'DISCONNECTED': 'danger',
            'MAINTENANCE': 'warning'
        };
        return statusClasses[status] || 'secondary';
    }

    getSignalStrength(strength) {
        if (!strength) return 'Unknown';
        if (strength >= 80) return 'Excellent';
        if (strength >= 60) return 'Good';
        if (strength >= 40) return 'Fair';
        return 'Poor';
    }

    formatDateTime(dateString) {
        return new Date(dateString).toLocaleString();
    }

    formatCurrency(amount) {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR'
        }).format(amount);
    }

    // Action methods for UI interactions
    viewVisit(visitId) {
        console.log('Viewing visit:', visitId);
        // TODO: Implement visit details modal
        this.showNotification(`Viewing visit ${visitId}`, 'info');
    }

    editVisit(visitId) {
        console.log('Editing visit:', visitId);
        // TODO: Implement visit edit modal
        this.showNotification(`Editing visit ${visitId}`, 'info');
    }

    viewEquipment(equipmentId) {
        console.log('Viewing equipment:', equipmentId);
        // TODO: Implement equipment details modal
        this.showNotification(`Viewing equipment ${equipmentId}`, 'info');
    }

    maintainEquipment(equipmentId) {
        console.log('Maintaining equipment:', equipmentId);
        // TODO: Implement maintenance modal
        this.showNotification(`Scheduling maintenance for equipment ${equipmentId}`, 'info');
    }

    viewBill(billId) {
        console.log('Viewing bill:', billId);
        // TODO: Implement bill details modal
        this.showNotification(`Viewing bill ${billId}`, 'info');
    }

    markPaid(billId) {
        console.log('Marking bill as paid:', billId);
        // TODO: Implement payment processing
        this.showNotification(`Marking bill ${billId} as paid`, 'info');
    }

    // Modal management
    showNewVisitModal() {
        const modal = document.getElementById('new-visit-modal');
        if (modal) {
            modal.classList.add('show');
            // Reset form
            const form = document.getElementById('new-visit-form');
            if (form) form.reset();
        }
    }

    closeModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.remove('show');
        }
    }

    // Visit management
    async refreshVisits() {
        try {
            await this.loadVisitsData();
            this.showNotification('Visits refreshed successfully', 'success');
        } catch (error) {
            console.error('Failed to refresh visits:', error);
            this.showNotification('Failed to refresh visits', 'error');
        }
    }

    filterVisits() {
        const searchTerm = document.getElementById('visits-search')?.value.toLowerCase() || '';
        const statusFilter = document.getElementById('visits-status-filter')?.value || '';

        const tableBody = document.getElementById('visits-table-body');
        if (!tableBody) return;

        const rows = tableBody.querySelectorAll('tr');

        rows.forEach(row => {
            if (row.querySelector('.empty-state')) return; // Skip empty state row

            const patientName = row.cells[1]?.textContent.toLowerCase() || '';
            const visitId = row.cells[0]?.textContent.toLowerCase() || '';
            const status = row.querySelector('.badge')?.textContent || '';

            const matchesSearch = patientName.includes(searchTerm) || visitId.includes(searchTerm);
            const matchesStatus = !statusFilter || status === statusFilter;

            row.style.display = (matchesSearch && matchesStatus) ? '' : 'none';
        });
    }

    async createNewVisit(formData) {
        try {
            const visitData = {
                patientDetails: {
                    name: formData.get('name'),
                    age: parseInt(formData.get('age')),
                    gender: formData.get('gender'),
                    phone: formData.get('phone'),
                    email: formData.get('email') || null,
                    address: formData.get('address') || null
                }
            };

            const response = await fetch(this.apiEndpoints.visits, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(visitData)
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const newVisit = await response.json();
            this.showNotification(`Visit created successfully for ${visitData.patientDetails.name}`, 'success');
            this.closeModal('new-visit-modal');
            await this.loadVisitsData(); // Refresh the visits list

            return newVisit;
        } catch (error) {
            console.error('Failed to create visit:', error);
            this.showNotification('Failed to create visit. Please try again.', 'error');
            throw error;
        }
    }

    async handleNewVisitSubmission(form) {
        try {
            const formData = new FormData(form);

            // Validate required fields
            const requiredFields = ['name', 'age', 'gender', 'phone'];
            for (const field of requiredFields) {
                if (!formData.get(field)) {
                    this.showNotification(`Please fill in the ${field} field`, 'error');
                    return;
                }
            }

            // Show loading state
            const submitBtn = form.querySelector('button[type="submit"]');
            const originalText = submitBtn.textContent;
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<span class="loading"></span> Creating...';

            await this.createNewVisit(formData);

            // Reset loading state
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;

        } catch (error) {
            // Reset loading state on error
            const submitBtn = form.querySelector('button[type="submit"]');
            submitBtn.disabled = false;
            submitBtn.textContent = 'Create Visit';
        }
    }

    // API helper methods
    async apiGet(endpoint) {
        const response = await fetch(`${this.baseUrl}${endpoint}`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        return response.json();
    }

    async apiPost(endpoint, data) {
        const response = await fetch(`${this.baseUrl}${endpoint}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify(data)
        });
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        return response.json();
    }

    async apiPut(endpoint, data) {
        const response = await fetch(`${this.baseUrl}${endpoint}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify(data)
        });
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        return response.json();
    }

    async apiDelete(endpoint) {
        const response = await fetch(`${this.baseUrl}${endpoint}`, {
            method: 'DELETE',
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        });
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        return response.ok;
    }
}

// Initialize the application when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.labApp = new LabOperationsApp();
    console.log('Lab Operations App initialized');
});

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = LabOperationsApp;
}
