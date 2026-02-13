document.addEventListener("DOMContentLoaded", async () => {
    // ✅ Fetch logged-in user info from sessionStorage
    const userEmail = sessionStorage.getItem("userEmail");
    const userRole = sessionStorage.getItem("userRole");

    // ✅ Check login status and role
    if (!userEmail || userRole !== "hospital") {
        alert("⚠️ Please log in first!");
        window.location.href = "hospital_login.html";
        return;
    }

    console.log("Logged in hospital:", userEmail);

    // ✅ Load all dashboard data
    await Promise.all([
        loadHospitalProfile(userEmail),
        loadAllDonations(),
        loadPendingApprovals(),
        loadBlockchainStatus(),
        loadBloodInventory(),
        loadMyRequests(),
        loadUsageTrends()
    ]);

    // ✅ Logout functionality
    const logoutBtn = document.querySelector(".logout");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", (e) => {
            e.preventDefault();
            if (confirm("Are you sure you want to logout?")) {
                sessionStorage.clear();
                window.location.href = "index.html";
            }
        });
    }

    // ✅ Mobile menu
    const menuBtn = document.getElementById("mobileMenuBtn");
    if (menuBtn) {
        menuBtn.addEventListener("click", () => {
            document.getElementById("sidebar").classList.toggle("active");
        });
    }
});


// 🏥 Load Hospital Profile
async function loadHospitalProfile(email) {
    try {
        const res = await fetch(`http://localhost:8080/api/hospital/${email}`);
        if (!res.ok) throw new Error("Failed to load hospital profile");

        const hospital = await res.json();
        const nameEl = document.getElementById("hospitalName");
        if (nameEl) {
            nameEl.textContent = hospital.name || "Unknown Hospital";
        }
    } catch (err) {
        console.error("❌ Error loading hospital profile:", err);
    }
}


// 💉 Load All Donations
async function loadAllDonations() {
    try {
        const res = await fetch("http://localhost:8080/api/donations/all");
        if (!res.ok) throw new Error("Failed to fetch all donations");
        const data = await res.json();

        const tbody = document.querySelector("#donations tbody");
        if (!tbody) return; // Exit if table not found on this page
        tbody.innerHTML = "";

        if (data.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;">No donation records found.</td></tr>`;
            return;
        }

        data.forEach((d, i) => {
            const row = `
                <tr>
                    <td>${i + 1}</td>
                    <td>${d.donorName || "Unknown"}</td>
                    <td>${d.bloodGroup || "Unknown"}</td>
                    <td>${d.units || "0"}ml</td>
                    <td>${d.donationDate || "N/A"}</td>
                    <td>${d.location || "N/A"}</td>
                    <td><span class="${d.status === 'Verified' ? 'status-good' : 'status-pending'}">${d.status || "Pending"}</span></td>
                    <td><span class="hash-text" title="${d.blockHash}">${d.blockHash ? d.blockHash.substring(0, 15) + "..." : "Pending"}</span></td>
                </tr>`;
            tbody.insertAdjacentHTML("beforeend", row);
        });

        // Update stats
        document.querySelector("#overview .overview-card:nth-child(1) p").textContent = data.length;
        const verified = data.filter(d => d.status === 'Verified').length;
        document.querySelector("#overview .overview-card:nth-child(3) p").textContent = verified;

    } catch (err) {
        console.error("❌ Error loading donations:", err);
    }
}


// 🩸 Load Pending Approvals
async function loadPendingApprovals() {
    try {
        const res = await fetch("http://localhost:8080/api/donations/pending");
        if (!res.ok) throw new Error("Failed to fetch pending donations");
        const pending = await res.json();

        const tbody = document.querySelector("#approve tbody");
        if (!tbody) return; // Exit if table not found
        tbody.innerHTML = "";

        if (pending.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;">No pending approvals.</td></tr>`;
            return;
        }

        pending.forEach((p) => {
            const row = `
                <tr>
                    <td>${p.donorName || "Unknown Donor"}</td>
                    <td>${p.bloodGroup || "Unknown"}</td>
                    <td>${p.units || "0"}ml</td>
                    <td>${p.donationDate || "N/A"}</td>
                    <td><span class="status-pending">${p.healthStatus || "Review Needed"}</span></td>
                    <td>
                        <button class="btn small" style="background: var(--gradient-success);" onclick="approveDonation(${p.id})">
                            <i class="fas fa-check"></i> Approve
                        </button>
                        <button class="btn small reject" onclick="rejectDonation(${p.id})">
                            <i class="fas fa-times"></i> Reject
                        </button>
                    </td>
                </tr>`;
            tbody.insertAdjacentHTML("beforeend", row);
        });
    } catch (err) {
        console.error("❌ Error loading pending approvals:", err);
        // Don't show alert on load, just log
    }
}

// ✅ Approve Donation
async function approveDonation(id) {
    if (!confirm("Approve this donation?")) return;
    try {
        const res = await fetch(`http://localhost:8080/api/donations/${id}/approve`, {
            method: "PUT"
        });
        const msg = await res.text();
        alert(msg);
        loadPendingApprovals(); // reload pending table
    } catch (err) {
        console.error(err);
        alert("❌ Failed to approve donation.");
    }
}

// ❌ Reject Donation
async function rejectDonation(id) {
    if (!confirm("Reject this donation?")) return;
    try {
        const res = await fetch(`http://localhost:8080/api/donations/${id}/reject`, {
            method: "PUT"
        });
        const msg = await res.text();
        alert(msg);
        loadPendingApprovals();
    } catch (err) {
        console.error(err);
        alert("❌ Failed to reject donation.");
    }
}

// 🔗 Blockchain Verification
async function loadBlockchainStatus() {
    try {
        const res = await fetch("http://localhost:8080/api/blockchain/status");
        if (!res.ok) {
            // If endpoint doesn't exist yet, mock it for UI stability
            console.warn("Blockchain status endpoint not found, using mock.");
            return;
        }
        const status = await res.json();

        // Target the 4th card's paragraph which holds the status
        const statusMsg = document.querySelector(".overview-grid .overview-card:nth-child(4) p");
        if (statusMsg) {
            statusMsg.textContent = status.message || "Valid";
            statusMsg.className = status.valid ? "status-good" : "status-critical"; // Update class for color
        }
    } catch (err) {
        console.error("❌ Error fetching blockchain status:", err);
    }
}


// 🧫 Blood Inventory
// 🩸 Request Blood Form
document.getElementById('requestForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const requestData = {
        hospitalEmail: sessionStorage.getItem("userEmail"),
        componentType: document.getElementById('reqComponent').value,
        bloodGroup: document.getElementById('reqBloodGroup').value,
        quantity: document.getElementById('reqQuantity').value,
        urgency: document.getElementById('reqUrgency').value
    };

    try {
        const res = await fetch("http://localhost:8080/api/hospital/request/create", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(requestData)
        });

        if (res.ok) {
            alert("✅ Blood request sent successfully!");
            e.target.reset();
        } else {
            alert("❌ Failed to send request.");
        }
    } catch (err) {
        console.error("Error sending request:", err);
        alert("Error sending request.");
    }
});

// 💉 Record Usage Form
document.getElementById('usageStatus')?.addEventListener('change', (e) => {
    const details = document.getElementById('reactionDetailsGroup');
    if (e.target.value === 'ADVERSE_REACTION') {
        details.style.display = 'block';
    } else {
        details.style.display = 'none';
    }
});

document.getElementById('usageForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const usageData = {
        hospitalId: 1, // TODO: Get real ID from session
        bloodComponentId: document.getElementById('usageComponentId').value, // This needs to be the ID, usually passed or scanned
        patientName: document.getElementById('usagePatientName').value,
        patientId: document.getElementById('usagePatientId').value,
        status: document.getElementById('usageStatus').value,
        reactionDetails: document.getElementById('usageReaction').value || "None"
    };

    // Note: In real app, we need to map Component ID String (Scanner) to Long ID
    // keeping simplistic for prototype

    try {
        const res = await fetch("http://localhost:8080/api/hospital/usage/record", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(usageData)
        });

        if (res.ok) {
            alert("✅ Transfusion recorded successfully!");
            e.target.reset();
        } else {
            alert("❌ Failed to record transfusion. Check Component ID.");
        }
    } catch (err) {
        console.error("Error recording usage:", err);
        alert("Error recording usage.");
    }
});

// 🧫 Blood Inventory (Updated to show real split data)
// 🧫 Blood Inventory (Updated to show real split data)
async function loadBloodInventory() {
    const userEmail = sessionStorage.getItem("userEmail");
    if (!userEmail) return;

    try {
        const res = await fetch(`http://localhost:8080/api/hospital/inventory/summary?email=${userEmail}`);
        if (!res.ok) throw new Error("Failed to load inventory");

        const inventory = await res.json();

        const grid = document.querySelector("#inventory .blood-group-grid");
        if (!grid) return; // Exit if not on inventory page

        grid.innerHTML = ""; // Clear existing hardcoded cards

        inventory.forEach(item => {
            const card = document.createElement("div");
            card.className = "blood-group-card";
            card.innerHTML = `
                <div class="blood-group">${item.bloodGroup}</div>
                <div class="blood-units">${item.units} Units</div>
                <div class="blood-status ${item.statusClass}">${item.status}</div>
            `;
            grid.appendChild(card);
        });



    } catch (err) {
        console.error("❌ Error loading inventory:", err);
    }
}

// 📋 Load My Requests (Hospital)
async function loadMyRequests() {
    const userEmail = sessionStorage.getItem("userEmail");
    if (!userEmail) return;

    try {
        const res = await fetch(`http://localhost:8080/api/hospital/requests/${userEmail}`);
        if (!res.ok) throw new Error("Failed to load requests");

        const requests = await res.json();

        const tbody = document.querySelector("#requestsTable tbody");
        if (!tbody) return; // Exit if not on requests page

        tbody.innerHTML = "";

        if (requests.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;">No requests found.</td></tr>`;
            return;
        }

        requests.forEach(r => {
            let actionBtn = "";
            let statusBadge = `<span class="status-pending">${r.status}</span>`;
            let reasonHtml = "";

            if (r.status === "APPROVED") {
                statusBadge = `<span class="status-good">Approved</span>`;
            } else if (r.status === "DISPATCHED") {
                statusBadge = `<span class="status-good" style="background:#2196F3;">Dispatched</span>`;
                actionBtn = `<button class="btn small" onclick="confirmReceipt(${r.id})"><i class="fas fa-box-open"></i> Confirm Receipt</button>`;
            } else if (r.status === "COMPLETED") {
                statusBadge = `<span class="status-good">Received</span>`;
            } else if (r.status === "REJECTED") {
                statusBadge = `<span class="status-critical">Rejected</span>`;
                if (r.rejectionReason) {
                    reasonHtml = `<div style="font-size: 0.75rem; color: #e53935; margin-top: 4px; max-width: 150px;"><strong>Reason:</strong> ${r.rejectionReason}</div>`;
                }
            }

            const row = `
                <tr>
                    <td>${r.requestNumber}</td>
                    <td>${r.componentType} (${r.bloodGroup})</td>
                    <td>${r.quantity}</td>
                    <td>${r.urgency}</td>
                    <td>${new Date(r.requestDate).toLocaleDateString()}</td>
                    <td>
                        ${statusBadge}
                        ${reasonHtml}
                    </td>
                    <td>${actionBtn}</td>
                </tr>
            `;
            tbody.insertAdjacentHTML("beforeend", row);
        });

    } catch (err) {
        console.error("❌ Error loading requests:", err);
    }
}

// 🚚 Confirm Receipt
window.confirmReceipt = async function (id) {
    if (!confirm("Confirm receipt of these blood units? This will add them to your inventory.")) return;

    try {
        const res = await fetch(`http://localhost:8080/api/blood-requests/${id}/confirm-receipt`, {
            method: 'PUT'
        });

        if (res.ok) {
            alert("✅ Receipt Confirmed! Inventory Updated.");
            loadMyRequests();
            loadBloodInventory(); // Update inventory cards if on that page
        } else {
            alert("❌ Failed to confirm receipt.");
        }
    } catch (err) {
        console.error("Error confirming receipt:", err);
        alert("Error handling request.");
    }
};

// 📈 Load Usage Trends (Chart.js)
async function loadUsageTrends() {
    const userEmail = sessionStorage.getItem("userEmail");
    const canvas = document.getElementById("usageChart");
    if (!canvas || !userEmail) return;

    try {
        const res = await fetch(`http://localhost:8080/api/hospital/usage/analytics?email=${userEmail}&days=7`);
        if (!res.ok) throw new Error("Failed to load analytics");

        const data = await res.json();

        // Sort dates and prepare labels/data
        const sortedDates = Object.keys(data).sort();
        const counts = sortedDates.map(date => data[date]);
        const labels = sortedDates.map(date => {
            const d = new Date(date);
            return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
        });

        const ctx = canvas.getContext('2d');

        // Destroy existing chart if it exists
        if (window.myUsageChart) {
            window.myUsageChart.destroy();
        }

        window.myUsageChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Units Transfused',
                    data: counts,
                    borderColor: '#e53935',
                    backgroundColor: 'rgba(229, 57, 53, 0.1)',
                    borderWidth: 3,
                    tension: 0.4,
                    fill: true,
                    pointBackgroundColor: '#e53935',
                    pointRadius: 4,
                    pointHoverRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        backgroundColor: '#2c3e50',
                        titleFont: { size: 14 },
                        bodyFont: { size: 14 },
                        padding: 10,
                        displayColors: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1,
                            precision: 0
                        },
                        grid: {
                            drawBorder: false,
                            color: 'rgba(0,0,0,0.05)'
                        }
                    },
                    x: {
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });

    } catch (err) {
        console.error("❌ Error loading usage trends:", err);
    }
}
