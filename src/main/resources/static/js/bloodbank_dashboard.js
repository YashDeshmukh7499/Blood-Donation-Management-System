// Blood Bank Dashboard JavaScript - Fetch Real Data from Database

const API_BASE_URL = 'http://localhost:8080/api/bloodbank/dashboard';

// Initialize dashboard on page load
document.addEventListener('DOMContentLoaded', function () {
    loadDashboardData();
    setupEventListeners();

    // Auto-refresh every 30 seconds
    setInterval(loadDashboardData, 30000);
});

// Setup event listeners
function setupEventListeners() {
    // Refresh button
    const refreshBtn = document.querySelector('.quick-action-btn, .btn.small');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', function () {
            loadDashboardData();
        });
    }
}

// Main function to load all dashboard data
async function loadDashboardData() {
    try {
        await Promise.all([
            loadOverviewStats(),
            loadBloodInventory(),
            loadHospitalRequests(), // New
            loadPendingDonations(),
            loadApprovedDonations(), // Added
            loadCompletedDonations(), // Added
            loadBlockchainStats()
        ]);
        console.log('✅ Dashboard data loaded successfully');
    } catch (error) {
        console.error('❌ Error loading dashboard data:', error);
    }
}

// [1] Load overview statistics (Updated)
async function loadOverviewStats() {
    try {
        const response = await fetch(`${API_BASE_URL}/overview`);
        const data = await response.json();

        // Update summary cards
        updateElementText('totalDonors', data.totalDonors || 0);
        updateElementText('totalUnits', data.availableStock || 0); // Using availableStock for Total Units display
        updateElementText('expiringSoon', data.expiringSoon || 0);
        updateElementText('emergencyRequests', data.activeRequests || 0); // mapped activeRequests to emergencyRequests for now

    } catch (error) {
        console.error('Error loading overview stats:', error);
    }
}

function updateElementText(id, value) {
    const element = document.getElementById(id);
    if (element) {
        element.textContent = value;
    }
}

// [2] Load blood inventory (Updated for Table)
async function loadBloodInventory() {
    try {
        const response = await fetch(`${API_BASE_URL}/inventory`);
        const inventory = await response.json();

        const tbody = document.getElementById('inventoryTableBody');
        if (!tbody) return;

        tbody.innerHTML = ''; // Clear existing

        if (inventory.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" style="text-align: center;">No inventory data</td></tr>';
            return;
        }

        inventory.forEach(item => {
            const row = document.createElement('tr');
            row.style.borderBottom = '1px solid #eee';

            // Status badge class mapping handled by backend or mapped here
            // Backend sends statusClass: 'status-safe', 'status-low', 'status-critical'

            row.innerHTML = `
                <td style="padding: 10px; font-weight: bold;">${item.bloodGroup}</td>
                <td style="padding: 10px;">${item.units} Units</td>
                <td style="padding: 10px;">
                    <span class="status-badge ${item.statusClass || getStatusClass(item.status)}">${item.status}</span>
                </td>
                <td style="padding: 10px;">
                    <button class="btn small" style="padding: 5px 10px; font-size: 0.8rem;">Details</button>
                </td>
            `;
            tbody.appendChild(row);
        });

    } catch (error) {
        console.error('Error loading blood inventory:', error);
    }
}

function getStatusClass(status) {
    if (status === 'Safe' || status === 'Adequate') return 'status-good'; // Assuming you have these CSS classes
    if (status === 'Low') return 'status-warning'; // or similar
    if (status === 'Critical') return 'status-critical'; // or similar
    return '';
}

// [3] Load Hospital Requests (New)
async function loadHospitalRequests() {
    try {
        const response = await fetch(`${API_BASE_URL}/hospital-requests`);
        const requests = await response.json();

        const container = document.getElementById('hospitalRequestsList');
        if (!container) return; // Exit if not on page

        container.innerHTML = '';

        if (requests.length === 0) {
            container.innerHTML = '<div style="text-align: center; color: #777;">No active requests</div>';
            return;
        }

        requests.forEach(req => {
            const item = document.createElement('div');
            item.className = 'request-item';
            item.style.padding = '10px';
            item.style.border = '1px solid #eee';
            item.style.borderRadius = '5px';
            item.style.background = '#fafafa';

            item.innerHTML = `
                <div style="display: flex; justify-content: space-between; margin-bottom: 5px;">
                    <strong>${req.hospitalName}</strong>
                    <span style="font-size: 0.8rem; color: #666;">${req.requestDate.split('T')[0]}</span>
                </div>
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <div>
                        <span style="font-weight: bold; color: var(--danger);">${req.componentType || 'N/A'}</span>
                        <span style="color: #333;"> - ${req.bloodGroup}</span>
                        <span style="color: #666; font-weight: normal;"> (${req.quantity} units)</span>
                    </div>
                    <span class="status-badge ${req.urgency === 'EMERGENCY' ? 'status-critical' : 'status-low'}">${req.urgency}</span>
                </div>
                <div style="margin-top: 10px; display: flex; gap: 10px;">
                    <button class="btn small" style="background: var(--gradient-success); flex: 1;" onclick="approveHospitalRequest(${req.requestId})">Approve</button>
                    <button class="btn small reject" style="flex: 1;" onclick="rejectHospitalRequest(${req.requestId})">Reject</button>
                </div>
            `;
            container.appendChild(item);
        });

    } catch (error) {
        console.error('Error loading hospital requests:', error);
    }
}

// [4] Load Pending Donations (Updated for List)
async function loadPendingDonations() {
    try {
        const response = await fetch(`${API_BASE_URL}/pending-donations`);
        const donations = await response.json();

        const list = document.getElementById('pendingDonationsList');
        if (!list) return;

        list.innerHTML = '';

        if (donations.length === 0) {
            list.innerHTML = '<li style="text-align: center; padding: 10px; color: #777;">No pending donations</li>';
            return;
        }

        donations.forEach(donation => {
            const li = document.createElement('li');
            li.style.borderBottom = '1px solid #eee';
            li.style.padding = '10px 0';

            li.innerHTML = `
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <div>
                        <div style="font-weight: bold;">${donation.donorEmail}</div>
                        <div style="font-size: 0.85rem; color: #666;">Group: ${donation.bloodGroup} • Date: ${donation.requestDate.split('T')[0]}</div>
                    </div>
                    <button class="btn small" style="padding: 5px 10px;" onclick="approveDonationRequest(${donation.requestId})">Verify</button>
                </div>
            `;
            list.appendChild(li);
        });

    } catch (error) {
        console.error('Error loading pending donations:', error);
    }
}

// [4b] Load Approved Donations (Ready for Completion)
async function loadApprovedDonations() {
    try {
        const response = await fetch(`${API_BASE_URL}/approved-donations`);
        const donations = await response.json();

        const list = document.getElementById('approvedDonationsList');
        if (!list) return;

        list.innerHTML = '';

        if (donations.length === 0) {
            list.innerHTML = '<li style="text-align: center; padding: 10px; color: #777;">No approved donations to process</li>';
            return;
        }

        donations.forEach(donation => {
            const li = document.createElement('li');
            li.style.borderBottom = '1px solid #eee';
            li.style.padding = '10px 0';

            // Extract Appointment Date/Time if available
            const appointmentInfo = donation.appointmentDate
                ? `<br><span style="color: #2e7d32; font-weight: bold;">Scheduled: ${donation.appointmentDate} ${donation.appointmentTime || ''}</span>`
                : '';

            li.innerHTML = `
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <div>
                        <div style="font-weight: bold;">${donation.donorEmail}</div>
                        <div style="font-size: 0.85rem; color: #666;">
                            Group: ${donation.bloodGroup}
                            ${appointmentInfo}
                        </div>
                    </div>
                    <button class="btn small" style="padding: 5px 10px; background: var(--gradient-success);"
                        onclick="completeDonation(${donation.requestId}, '${donation.bloodGroup}', ${donation.bloodBankId})">
                        Complete
                    </button>
                </div>
            `;
            list.appendChild(li);
        });

    } catch (error) {
        console.error('Error loading approved donations:', error);
    }
}

// [4c] Load Completed Donations (History)
async function loadCompletedDonations() {
    try {
        const response = await fetch(`${API_BASE_URL}/completed-donations`);
        const donations = await response.json();

        const list = document.getElementById('completedDonationsList');
        if (!list) return;

        list.innerHTML = '';

        if (donations.length === 0) {
            list.innerHTML = '<li style="text-align: center; padding: 10px; color: #777;">No completed history yet</li>';
            return;
        }

        donations.forEach(donation => {
            const li = document.createElement('li');
            li.style.borderBottom = '1px solid #eee';
            li.style.padding = '10px 0';

            li.innerHTML = `
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <div>
                        <div style="font-weight: bold; color: #555;">${donation.donorEmail}</div>
                            Group: ${donation.bloodGroup} • Completed
                            ${donation.volumeMl ? `<br><span style="color: #2e7d32; font-weight: bold;">Volume: ${donation.volumeMl} ml</span>` : ''}
                        </div>
                    </div>
                    <span class="status-badge status-good" style="font-size: 0.7rem;">Completed</span>
                </div>
            `;
            list.appendChild(li);
        });

    } catch (error) {
        console.error('Error loading completed donations:', error);
    }
}

// Global Action Functions
window.approveDonationRequest = async function (id) {
    if (!confirm("Verify and Approve this donation request?")) return;
    try {
        const res = await fetch(`http://localhost:8080/api/donation-requests/requests/${id}/approve`, {
            method: 'PUT'
        });
        if (res.ok) {
            alert("✅ Donation Approved! Moving to Approved list.");
            loadDashboardData();
        } else {
            const err = await res.text();
            alert(`❌ Failed to approve: ${err}`);
        }
    } catch (e) {
        console.error("Error approving donation:", e);
        alert("Error processing request.");
    }
};

window.completeDonation = async function (id, bloodGroup, bankId) {
    const units = prompt("Enter volume collected (mL):", "450");
    if (!units) return;

    const notes = prompt("Enter any test notes (optional):", "Routine collection");

    // Construct completion data
    // Gather Bank Name from UI if possible, else default
    const bankNameText = document.getElementById('bankName') ? document.getElementById('bankName').innerText : "Blood Bank";

    const completionData = {
        bloodBankId: bankId || 1, // Fallback to 1 if missing
        bloodBankName: bankNameText.replace("Welcome, ", "").replace(" 🩸", ""),
        unitsCollected: parseInt(units),
        testNotes: notes,
        storageLocation: "Standard Storage"
    };

    try {
        const res = await fetch(`http://localhost:8080/api/donation-requests/requests/${id}/complete`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(completionData)
        });

        if (res.ok) {
            alert("✅ Donation Completed! Blood Unit added to Inventory.");
            loadDashboardData();
        } else {
            const err = await res.text();
            alert(`❌ Failed to complete: ${err}`);
        }
    } catch (e) {
        console.error("Error completing donation:", e);
        alert("Error processing completion.");
    }
};

window.approveHospitalRequest = async function (id) {
    if (!confirm("Approve this hospital request? This will reserve blood units.")) return;
    try {
        const res = await fetch(`http://localhost:8080/api/blood-requests/${id}/approve`, {
            method: 'PUT'
        });
        if (res.ok) {
            alert("✅ Request Approved & Units Reserved!");
            loadDashboardData();
        } else {
            const err = await res.text();
            alert(`❌ Failed to approve: ${err}`);
        }
    } catch (e) {
        console.error("Error approving request:", e);
        alert("Error processing request.");
    }
};

let currentRejectRequestId = null;

window.rejectHospitalRequest = function (id) {
    currentRejectRequestId = id;
    const modal = document.getElementById('rejectionModal');
    if (modal) {
        modal.style.display = 'block';
        // Reset modal state
        document.querySelectorAll('input[name="rejectionReason"]').forEach(r => r.checked = r.value === 'Insufficient stock of this blood group');
        const customArea = document.getElementById('customReasonArea');
        if (customArea) customArea.style.display = 'none';
        const customText = document.getElementById('customRejectionReason');
        if (customText) customText.value = '';
    } else {
        // Fallback if modal not in current page
        if (confirm("Reject this hospital request?")) {
            submitRejection(id, "Insufficient Stock / Policy");
        }
    }
};

async function submitRejection(id, reason) {
    try {
        const res = await fetch(`http://localhost:8080/api/blood-requests/${id}/reject`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ reason: reason })
        });
        if (res.ok) {
            alert("✅ Request Rejected.");
            const modal = document.getElementById('rejectionModal');
            if (modal) modal.style.display = 'none';
            loadDashboardData();
            if (typeof loadHospitalRequests === 'function') loadHospitalRequests();
        } else {
            const err = await res.text();
            alert(`❌ Failed to reject: ${err}`);
        }
    } catch (e) {
        console.error("Error rejecting request:", e);
        alert("Error processing request.");
    }
}

// [5] Load Blockchain Stats (Updated for Table)
async function loadBlockchainStats() {
    try {
        const response = await fetch(`${API_BASE_URL}/blockchain-stats`);
        const stats = await response.json();

        // Update Badge
        const statusBadge = document.getElementById('blockchainStatus');
        if (statusBadge) {
            if (stats.isValid) {
                statusBadge.textContent = 'Blockchain Active';
                statusBadge.className = 'status-badge status-good';
            } else {
                statusBadge.textContent = 'No Data';
                statusBadge.className = 'status-badge status-low';
            }
        }

        // Update Table
        const tbody = document.getElementById('blockchainLogBody');
        if (!tbody) return;

        tbody.innerHTML = '';

        const logs = stats.recentTransactions || [];

        if (logs.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" style="text-align: center;">No registered blocks</td></tr>';
            return;
        }

        logs.forEach(log => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td style="font-family: monospace;">${log.unitId}</td>
                <td>${log.action}</td>
                <td>${log.timestamp.split('T')[0]}</td>
                <td style="font-family: monospace; color: #666;">${log.hash}</td>
            `;
            tbody.appendChild(row);
        });

    } catch (error) {
        console.error('Error loading blockchain stats:', error);
    }
}

// Sidebar Navigation Handling (Simple Tab Switch)
document.querySelectorAll('.nav-links a').forEach(link => {
    link.addEventListener('click', function (e) {
        if (this.classList.contains('logout')) return; // Allow logout to proceed

        // If link is external (not starting with #), allow default navigation
        const href = this.getAttribute('href');
        if (!href.startsWith('#')) return;

        e.preventDefault();

        // Hide all sections
        document.querySelectorAll('main > div > section, main > section').forEach(sec => sec.style.display = 'none');

        // Remove active class
        document.querySelectorAll('.nav-links a').forEach(l => l.classList.remove('active'));
        this.classList.add('active');

        // Show target section
        const targetId = href.substring(1); // remove #
        const targetSec = document.getElementById(targetId);

        if (targetSec) {
            targetSec.style.display = 'block';
        }

        // Restore flex layout for dashboard grid if 'overview' or other main tabs are selected
        const dashboardGrid = document.querySelector('.dashboard-grid');
        if (targetId === 'overview' || targetId === 'inventory' || targetId === 'blockchain') {
            // Re-show dashboard grid parts
            document.getElementById('inventory').style.display = 'block';
            document.getElementById('blockchain').style.display = 'block';
            document.getElementById('hospital-requests').style.display = 'block';
            document.getElementById('pending-donations').style.display = 'block';
            // Show new sections
            const approvedSec = document.getElementById('approved-donations');
            if (approvedSec) approvedSec.style.display = 'block';

            const completedSec = document.getElementById('completed-donations');
            if (completedSec) completedSec.style.display = 'block';

            document.getElementById('overview').style.display = 'block';
        }
    });
});

// 🩸 Load Detailed Blood Inventory (All Units with Full Details)
async function loadDetailedInventory() {
    try {
        const response = await fetch('http://localhost:8080/api/bloodbank/dashboard/inventory/all');
        if (!response.ok) throw new Error('Failed to fetch detailed inventory');

        const units = await response.json();
        const tbody = document.getElementById('detailedInventoryBody');

        if (!tbody) return; // Exit if not on inventory page

        tbody.innerHTML = '';

        if (units.length === 0) {
            tbody.innerHTML = '<tr><td colspan="13" style="text-align:center;">No blood units found.</td></tr>';
            return;
        }

        units.forEach(unit => {
            // Determine status badge class
            let statusClass = 'status-pending';
            if (unit.status === 'STORED') statusClass = 'status-good';
            else if (unit.status === 'DISPATCHED') statusClass = 'status-warning';
            else if (unit.status === 'EXPIRED') statusClass = 'status-critical';

            // Test result badges
            const getTestBadge = (result) => {
                if (result === 'NEGATIVE') return '<span class="status-good">NEG</span>';
                if (result === 'POSITIVE') return '<span class="status-critical">POS</span>';
                return '<span class="status-pending">PENDING</span>';
            };

            const row = `
                <tr>
                    <td><code>${unit.bloodUnitId || 'N/A'}</code></td>
                    <td><strong>${unit.bloodGroup || 'N/A'}</strong></td>
                    <td>${unit.volumeMl || 0}</td>
                    <td>${unit.collectionDate || 'N/A'}</td>
                    <td>${unit.expiryDate || 'N/A'}</td>
                    <td><span class="${statusClass}">${unit.status || 'UNKNOWN'}</span></td>
                    <td><span class="${unit.testStatus === 'PASSED' ? 'status-good' : 'status-pending'}">${unit.testStatus || 'PENDING'}</span></td>
                    <td>${getTestBadge(unit.hivTest)}</td>
                    <td>${getTestBadge(unit.hbvTest)}</td>
                    <td>${getTestBadge(unit.hcvTest)}</td>
                    <td>${unit.storageLocation || 'N/A'}</td>
                    <td>${unit.storageTemperature || 'N/A'}</td>
                    <td><span class="hash-text" title="${unit.blockHash || 'N/A'}">${unit.blockHash ? unit.blockHash.substring(0, 12) + '...' : 'Pending'}</span></td>
                </tr>
            `;
            tbody.insertAdjacentHTML('beforeend', row);
        });

        console.log(`✅ Loaded ${units.length} blood units`);
    } catch (error) {
        console.error('❌ Error loading detailed inventory:', error);
        const tbody = document.getElementById('detailedInventoryBody');
        if (tbody) {
            tbody.innerHTML = '<tr><td colspan="13" style="text-align:center; color:red;">Error loading inventory data</td></tr>';
        }
    }
}

// 📦 Load Available Components (For Dispatch Page)
async function loadAvailableComponents() {
    try {
        const response = await fetch('http://localhost:8080/api/bloodbank/dashboard/components/inventory');
        if (!response.ok) throw new Error('Failed to fetch components');

        const componentCounts = await response.json(); // Array of {type, bloodGroup, count}
        const tbody = document.getElementById('componentsTableBody');

        if (!tbody) return; // Exit if not on dispatch page

        tbody.innerHTML = '';

        if (componentCounts.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" style="text-align:center;">No available components in inventory</td></tr>';
            return;
        }

        // Sort by type then blood group
        componentCounts.sort((a, b) => {
            if (a.type !== b.type) return a.type.localeCompare(b.type);
            return a.bloodGroup.localeCompare(b.bloodGroup);
        });

        // Display
        componentCounts.forEach(comp => {
            let statusClass = 'status-critical';
            let status = 'Critical';
            if (comp.count >= 10) {
                statusClass = 'status-good';
                status = 'Safe';
            } else if (comp.count >= 3) {
                statusClass = 'status-warning';
                status = 'Low';
            }

            const row = `
                <tr>
                    <td><strong>${comp.type}</strong></td>
                    <td>${comp.bloodGroup}</td>
                    <td>${comp.count} units</td>
                    <td><span class="${statusClass}">${status}</span></td>
                </tr>
            `;
            tbody.insertAdjacentHTML('beforeend', row);
        });

        console.log(`✅ Loaded ${componentCounts.length} component types`);
    } catch (error) {
        console.error('❌ Error loading components:', error);
        const tbody = document.getElementById('componentsTableBody');
        if (tbody) {
            tbody.innerHTML = '<tr><td colspan="4" style="text-align:center; color:red;">Error loading components</td></tr>';
        }
    }
}

// Auto-load detailed inventory if on inventory page
document.addEventListener('DOMContentLoaded', function () {
    if (document.getElementById('detailedInventoryBody')) {
        loadDetailedInventory();
    }
    if (document.getElementById('componentsTableBody')) {
        loadAvailableComponents();
    }
    if (document.getElementById('hospitalRequestsList')) {
        loadHospitalRequests();
    }

    // --- Rejection Modal Event Listeners ---
    const closeBtn = document.getElementById('closeRejectionModal');
    const cancelBtn = document.getElementById('cancelRejection');
    const confirmBtn = document.getElementById('confirmRejectionBtn');
    const modal = document.getElementById('rejectionModal');

    if (closeBtn) closeBtn.onclick = () => modal.style.display = 'none';
    if (cancelBtn) cancelBtn.onclick = () => modal.style.display = 'none';

    // Close on click outside
    window.onclick = (event) => {
        if (event.target == modal) modal.style.display = 'none';
    };

    // Toggle custom reason area
    document.querySelectorAll('input[name="rejectionReason"]').forEach(radio => {
        radio.addEventListener('change', (e) => {
            const customArea = document.getElementById('customReasonArea');
            if (customArea) {
                customArea.style.display = e.target.value === 'Other / Custom' ? 'block' : 'none';
            }
        });
    });

    if (confirmBtn) {
        confirmBtn.onclick = () => {
            const selectedRadio = document.querySelector('input[name="rejectionReason"]:checked');
            let reason = selectedRadio ? selectedRadio.value : "Insufficient Stock";

            if (reason === 'Other / Custom') {
                const customText = document.getElementById('customRejectionReason').value.trim();
                if (!customText) {
                    alert("Please enter a custom reason.");
                    return;
                }
                reason = customText;
            }

            if (currentRejectRequestId) {
                submitRejection(currentRejectRequestId, reason);
            }
        };
    }
});
