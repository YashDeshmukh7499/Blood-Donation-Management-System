// 🩸 user_dashboard.js
document.addEventListener("DOMContentLoaded", async () => {
    const loggedInEmail = localStorage.getItem("loggedInUserEmail");

    if (!loggedInEmail) {
        alert("⚠️ Please log in first!");
        window.location.href = "user_login.html";
        return;
    }

    // ✅ Declare user variable at higher scope so it's accessible to form handlers
    let user = null;

    try {
        // ✅ Fetch user profile
        const userResponse = await fetch(`http://localhost:8080/api/auth/user/${loggedInEmail}`);
        if (!userResponse.ok) throw new Error("Failed to fetch user details");
        user = await userResponse.json();

        // ✅ Ask only if data missing
        let updated = false;
        let bloodGroup = user.bloodGroup;
        let phone = user.phone;
        let city = user.city;

        if (!bloodGroup || !phone || !city) {
            alert("⚠️ Some details are missing. Please update your information.");

            if (!bloodGroup) bloodGroup = prompt("Enter your Blood Group (e.g., O+, A+, B-, etc.):");
            if (!phone) phone = prompt("Enter your Phone Number:");
            if (!city) city = prompt("Enter your Current City:");

            const updatedUser = { ...user, bloodGroup: bloodGroup, phone: phone, city: city };

            const updateRes = await fetch(`http://localhost:8080/api/auth/user/update/${loggedInEmail}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(updatedUser)
            });

            if (updateRes.ok) {
                alert("✅ Profile updated successfully!");
                updated = true;
            } else {
                alert("⚠️ Failed to update profile.");
            }
        }

        // ✅ Refresh user info after update
        if (updated) {
            const refreshedRes = await fetch(`http://localhost:8080/api/auth/user/${loggedInEmail}`);
            user = await refreshedRes.json();
        }

        // ✅ Fetch donation records
        const donationResponse = await fetch(`http://localhost:8080/api/auth/user/${loggedInEmail}/donations`);
        const donations = donationResponse.ok ? await donationResponse.json() : [];

        // ✅ Display user info on dashboard
        const userNameEl = document.getElementById("userName");
        if (userNameEl) userNameEl.textContent = user.name || "User";

        const profileInfo = document.querySelector(".profile-info");
        if (profileInfo) {
            profileInfo.innerHTML = `
                <p><strong>Name:</strong> ${user.name || "N/A"}</p>
                <p><strong>Email:</strong> ${user.email}</p>
                <p><strong>Blood Group:</strong> ${user.bloodGroup || "N/A"}</p>
                <p><strong>Phone:</strong> ${user.phone || "N/A"}</p>
                <p><strong>Location:</strong> ${user.city || "N/A"}</p>
                <p><strong>Registered On:</strong> ${user.registrationDate || "N/A"}</p>
                <p><strong>Last Donation:</strong> ${user.lastDonationDate || "N/A"}</p>
                <p><strong>Status:</strong>
                    <span style="color: ${user.status === "Eligible" ? "#4caf50" : "#f44336"};">
                        ${user.status || "N/A"}
                    </span>
                </p>
            `;
        }

        // ✅ Update donation records (blockchain table)
        const tbody = document.querySelector(".block-table tbody") || document.getElementById("blockchainRecordsList");
        if (tbody) {
            tbody.innerHTML = "";
            if (donations.length === 0) {
                tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;">No donation records found.</td></tr>`;
            } else {
                donations.forEach((record, index) => {
                    const bloodUnitId = record.bloodUnitId || "Pending Process";
                    const blockHash = record.blockHash ? record.blockHash.substring(0, 15) + "..." : "Generating...";
                    const statusColor = getStatusColor(record.status);

                    const row = `
                        <tr>
                            <td>${index + 1}</td>
                            <td>${record.donationDate || "-"}</td>
                            <td><strong>${bloodUnitId}</strong></td>
                            <td>${record.units || "-"} ml</td>
                            <td>${record.location || "-"}</td>
                            <td><span class="hash-text" title="${record.blockHash}">${blockHash}</span></td>
                            <td><span class="status-badge" style="background-color: ${statusColor};">${record.status || "Pending"}</span></td>
                            ${tbody.id === "blockchainRecordsList" ? "" : `
                            <td>
                                ${['Verified', 'USED', 'COLLECTED', 'STORED', 'APPROVED', 'TESTED', 'DISPATCHED', 'RECEIVED'].includes(record.status) ?
                                `<button class="btn-cert" onclick="downloadCertificate('${record.id}')">
                                        <i class="fas fa-certificate"></i> Cert
                                    </button>` :
                                `<span class="text-muted">Processing</span>`
                            }
                            </td>
                            `}
                        </tr>`;
                    tbody.insertAdjacentHTML("beforeend", row);
                });
            }
        }

        // ✅ Statistics
        const statsSection = document.querySelector("#stats");
        if (statsSection) {
            const totalDonations = donations.length;
            const totalUnits = donations.reduce((sum, d) => sum + (d.units || 0), 0);
            const livesSaved = totalDonations * 3;
            const profileCompletion = calculateProfileCompletion(user);

            statsSection.innerHTML = `
                <div class="stat-card">
                    <div class="number">${totalDonations}</div>
                    <div class="label">Total Donations</div>
                </div>
                <div class="stat-card">
                    <div class="number">${(totalUnits / 1000).toFixed(2)}L</div>
                    <div class="label">Blood Donated</div>
                </div>
                
                <!-- Eligibility Status Card -->
                <div class="stat-card" id="eligibilityCard">
                    <div class="number" id="eligibilityStatus">Checking...</div>
                    <div class="label" id="eligibilityReason">Eligibility Status</div>
                </div>

                <div class="stat-card">
                    <div class="number">${livesSaved}+</div>
                    <div class="label">Lives Saved</div>
                </div>
                <div class="stat-card">
                    <div class="number">${profileCompletion}%</div>
                    <div class="label">Profile Complete</div>
                </div>
            `;

            // ✅ Only check eligibility if stats section (and thus eligibility card) is present
            setTimeout(() => checkEligibility(), 100);
        }

        // ✅ Impact Summary (Statistics Page)
        const impactSummary = document.getElementById("impactSummary");
        if (impactSummary) {
            const totalDonations = donations.length;
            const livesSaved = totalDonations * 3;
            impactSummary.innerHTML = `
                <div style="background: #f8f9fa; padding: 20px; border-radius: 12px; border-left: 5px solid #e53935;">
                    <h3><i class="fas fa-heart" style="color: #e53935; margin-right: 10px;"></i>Your Lifesaving Journey</h3>
                    <p style="font-size: 1.1rem; margin-top: 10px;">Through your <strong>${totalDonations}</strong> donations, you have potentially touched the lives of <strong>${livesSaved}</strong> patients and their families.</p>
                    <p style="margin-top: 10px; color: #666;">Each donation can save up to 3 lives. Thank you for being a hero!</p>
                </div>
            `;
        }

        // ✅ Download Latest Certificate (Certificates Page)
        const downloadLatestBtn = document.querySelector(".certificate-actions .btn:first-child");
        const shareLatestBtn = document.querySelector(".certificate-actions .btn:nth-child(2)");

        if (downloadLatestBtn && downloadLatestBtn.textContent.includes("Download Latest Certificate")) {
            const certStatuses = ['Verified', 'USED', 'COLLECTED', 'STORED', 'APPROVED', 'TESTED', 'DISPATCHED', 'RECEIVED'];
            const verifiedDonations = donations.filter(d => certStatuses.includes(d.status));
            if (verifiedDonations.length > 0) {
                // Sort by date descending
                verifiedDonations.sort((a, b) => new Date(b.donationDate) - new Date(a.donationDate));
                const latest = verifiedDonations[0];

                downloadLatestBtn.onclick = () => downloadCertificate(latest.id);
                if (shareLatestBtn) {
                    shareLatestBtn.onclick = () => shareCertificate(latest.id, latest.blockHash);
                }
            } else {
                const noVerifiedHandler = () => alert("ℹ️ No verified donations found yet. Certificates are available after blood process completion.");
                downloadLatestBtn.onclick = noVerifiedHandler;
                if (shareLatestBtn) shareLatestBtn.onclick = noVerifiedHandler;

                downloadLatestBtn.style.opacity = "0.6";
                if (shareLatestBtn) shareLatestBtn.style.opacity = "0.6";
            }
        }

        // ✅ View All Certificates Button
        const viewAllBtn = document.querySelector(".certificate-actions .btn:last-child");
        const certListContainer = document.getElementById("certificateList");
        if (viewAllBtn && certListContainer) {
            viewAllBtn.onclick = () => certListContainer.scrollIntoView({ behavior: "smooth" });
        }

        // ✅ Population of Certificate History Table (Certificates Page)
        const certHistoryTbody = document.getElementById("certificateHistoryList");
        if (certHistoryTbody) {
            certHistoryTbody.innerHTML = "";
            const certStatuses = ['Verified', 'USED', 'COLLECTED', 'STORED', 'APPROVED', 'TESTED', 'DISPATCHED', 'RECEIVED'];
            const verifiedOnly = donations.filter(d => certStatuses.includes(d.status));

            if (verifiedOnly.length === 0) {
                certHistoryTbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding: 20px;">No verified donations found. Your certificates will appear here once processed.</td></tr>`;
            } else {
                verifiedOnly.forEach((record, index) => {
                    const row = `
                        <tr>
                            <td>${index + 1}</td>
                            <td>${record.donationDate || "-"}</td>
                            <td>${record.location || "-"}</td>
                            <td>${record.units || "-"} ml</td>
                            <td><span class="status-badge" style="background-color: var(--secondary); color: white;">Verified</span></td>
                            <td>
                                <div style="display: flex; gap: 8px;">
                                    <button class="btn small" onclick="downloadCertificate('${record.id}')" title="Download PDF">
                                        <i class="fas fa-download"></i>
                                    </button>
                                    <button class="btn small" style="background: var(--gradient-secondary);" onclick="shareCertificate('${record.id}', '${record.blockHash}')" title="Share Certificate">
                                        <i class="fas fa-share-alt"></i>
                                    </button>
                                </div>
                            </td>
                        </tr>`;
                    certHistoryTbody.insertAdjacentHTML("beforeend", row);
                });
            }
        }

    } catch (error) {
        console.error("❌ Error loading dashboard data:", error);
        alert("Failed to load dashboard data. Please try again later.");
    }

    // ✅ Sidebar toggle
    document.getElementById('mobileMenuBtn').addEventListener('click', () => {
        document.getElementById('sidebar').classList.toggle('active');
    });

    // ✅ Check Eligibility
    async function checkEligibility() {
        try {
            const res = await fetch(`http://localhost:8080/api/auth/user/eligibility/${loggedInEmail}`);
            if (res.ok) {
                const data = await res.json();
                const statusEl = document.getElementById("eligibilityStatus");
                const reasonEl = document.getElementById("eligibilityReason");
                const cardEl = document.getElementById("eligibilityCard");

                // Safety check: ensure elements exist before updating
                if (statusEl && reasonEl && cardEl) {
                    if (data.eligible) {
                        statusEl.textContent = "Eligible to Donate ✅";
                        statusEl.style.color = "#4caf50";
                        reasonEl.textContent = data.reason;
                        cardEl.style.borderLeft = "5px solid #4caf50";
                    } else {
                        statusEl.textContent = "Not Eligible ❌";
                        statusEl.style.color = "#f44336";
                        reasonEl.textContent = `${data.reason} Next eligible: ${data.nextEligibleDate}`;
                        cardEl.style.borderLeft = "5px solid #f44336";
                    }
                } else {
                    console.warn("Eligibility elements not found in DOM yet.");
                }
            }
        } catch (error) {
            console.error("Error checking eligibility:", error);
        }
    }

    // ✅ Load Emergency Requests
    async function loadEmergencyRequests() {
        const listEl = document.getElementById("emergencyList");
        if (!listEl) return;

        try {
            const res = await fetch("http://localhost:8080/api/blood-requests/emergency");
            if (res.ok) {
                const requests = await res.json();
                listEl.innerHTML = ""; // Clear loading text

                if (requests.length === 0) {
                    listEl.innerHTML = "<p>No active emergency requests at the moment.</p>";
                    return;
                }

                requests.forEach(req => {
                    const item = document.createElement("div");
                    item.className = "emergency-item";
                    item.style.padding = "10px";
                    item.style.borderBottom = "1px solid #eee";
                    item.innerHTML = `
                        <div style="display: flex; justify-content: space-between; align-items: center;">
                            <div>
                                <strong style="color: #e53935;">${req.bloodGroup}</strong> 
                                <span style="font-weight: bold;">${req.urgency}</span>
                                <br>
                                <small>${req.location}</small>
                            </div>
                            <button class="btn small" style="background: #e53935;" onclick="alert('Thank you! Please schedule an appointment below.')">
                                I Can Donate
                            </button>
                        </div>
                    `;
                    listEl.appendChild(item);
                });
            }
        } catch (error) {
            console.error("Error loading emergency requests:", error);
        }
    }
    loadEmergencyRequests();

    // ✅ Load All Blood Banks for Sidebar
    async function loadAllBloodBanks() {
        const listEl = document.getElementById("allBloodBanksList");
        if (!listEl) return;

        try {
            const res = await fetch("http://localhost:8080/api/bloodbank/all");
            if (res.ok) {
                const banks = await res.json();
                listEl.innerHTML = ""; // Clear loading text

                if (banks.length === 0) {
                    listEl.innerHTML = "<p class='text-muted'>No blood banks registered yet.</p>";
                    return;
                }

                banks.forEach(bank => {
                    const card = document.createElement("div");
                    card.className = "bank-card";

                    const verifiedBadge = bank.verified
                        ? '<span class="bank-card-badge badge-verified">✓ Verified</span>'
                        : '';

                    const statusBadge = bank.acceptingDonations
                        ? '<span class="bank-card-badge badge-accepting">Accepting</span>'
                        : '<span class="bank-card-badge badge-closed">Closed</span>';

                    card.innerHTML = `
                        <div class="bank-card-header">
                            <h3 class="bank-card-title">
                                <i class="fas fa-hospital-alt"></i>
                                ${bank.name}
                            </h3>
                            <div>
                                ${verifiedBadge}
                                ${statusBadge}
                            </div>
                        </div>
                        <div class="bank-card-details">
                            <p>
                                <i class="fas fa-map-marker-alt"></i>
                                <span>${bank.area || ''}, ${bank.city}, ${bank.state || ''}</span>
                            </p>
                            <p>
                                <i class="fas fa-map-pin"></i>
                                <span>PIN: ${bank.pincode || 'N/A'}</span>
                            </p>
                            <p>
                                <i class="fas fa-phone"></i>
                                <span>${bank.phone || 'N/A'}</span>
                            </p>
                            <p>
                                <i class="fas fa-clock"></i>
                                <span>${bank.operatingHours || 'Not Specified'}</span>
                            </p>
                            <p>
                                <i class="fas fa-calendar"></i>
                                <span>${bank.workingDays || 'Not Specified'}</span>
                            </p>
                            ${bank.website ? `
                            <p>
                                <i class="fas fa-globe"></i>
                                <span><a href="http://${bank.website}" target="_blank" style="color: var(--secondary);">${bank.website}</a></span>
                            </p>
                            ` : ''}
                        </div>
                        <div class="bank-card-footer">
                            <span class="bank-card-category">
                                <i class="fas fa-building"></i> ${bank.category || 'N/A'}
                            </span>
                            <a href="#" class="bank-card-action" onclick="viewBankDetails(${bank.bankId}, '${bank.name}'); return false;">
                                View Details <i class="fas fa-arrow-right"></i>
                            </a>
                        </div>
                    `;
                    listEl.appendChild(card);
                });
            } else {
                listEl.innerHTML = "<p class='text-muted'>Failed to load blood banks.</p>";
            }
        } catch (error) {
            console.error("Error loading all blood banks:", error);
            document.getElementById("allBloodBanksList").innerHTML = "<p class='text-muted'>Error loading blood banks.</p>";
        }
    }
    loadAllBloodBanks();

    // ✅ Search Blood Banks
    window.searchBloodBanks = async function () {
        const citySearchEl = document.getElementById("citySearch");
        const listEl = document.getElementById("bloodBankList");
        if (!citySearchEl || !listEl) return;

        const city = citySearchEl.value;
        if (!city) {
            alert("Please enter a city name.");
            return;
        }

        try {
            listEl.innerHTML = "<p>Searching...</p>";
            const res = await fetch(`http://localhost:8080/api/bloodbank/search?city=${city}`);
            if (res.ok) {
                const banks = await res.json();
                listEl.innerHTML = "";

                if (banks.length === 0) {
                    listEl.innerHTML = "<p>No blood banks found in this city.</p>";
                    return;
                }

                banks.forEach(bank => {
                    const item = document.createElement("div");
                    item.className = "bank-item";
                    item.style.padding = "15px";
                    item.style.border = "1px solid #ddd";
                    item.style.marginBottom = "10px";
                    item.style.borderRadius = "8px";
                    item.style.background = "#fff";

                    const statusBadge = bank.acceptingDonations
                        ? '<span class="status-badge" style="background:#4caf50; font-size:0.7em;">Accepting Donors</span>'
                        : '<span class="status-badge" style="background:#f44336; font-size:0.7em;">Closed for Donation</span>';

                    item.innerHTML = `
                        <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                            <div>
                                <h3 style="margin: 0; color: #d32f2f;">
                                    ${bank.name} 
                                    ${bank.verified ? '<i class="fas fa-check-circle" style="color:#2196f3; font-size:0.8em;" title="Verified"></i>' : ''}
                                </h3>
                                <p style="margin: 5px 0; color: #555;">
                                    📍 ${bank.city}, ${bank.area || ''}
                                </p>
                                <div style="font-size: 0.9em; color: #666; margin-top: 5px;">
                                    <p style="margin: 2px 0;">🏢 Type: <strong>${bank.category || 'N/A'}</strong></p>
                                    <p style="margin: 2px 0;">🕒 Hours: ${bank.operatingHours || 'Not Specified'}</p>
                                    <p style="margin: 2px 0;">📞 ${bank.phone || 'N/A'}</p>
                                </div>
                                <div style="margin-top: 8px;">
                                    ${statusBadge}
                                </div>
                            </div>
                            <button class="btn" onclick="openBooking(${bank.bankId}, '${bank.name}')" ${!bank.acceptingDonations ? 'disabled style="background:#ccc; cursor:not-allowed;"' : ''}>
                                Request Appointment
                            </button>
                        </div>
                    `;
                    listEl.appendChild(item);
                });
            }
        } catch (error) {
            console.error("Error searching blood banks:", error);
            listEl.innerHTML = "<p>Error searching. Try again.</p>";
        }
    };

    // ✅ View Bank Details (from sidebar)
    window.viewBankDetails = function (bankId, bankName) {
        // Scroll to the nearby banks section and show booking form
        const nearbyBanksSection = document.getElementById("nearby-banks");
        if (nearbyBanksSection) {
            nearbyBanksSection.scrollIntoView({ behavior: "smooth" });
            setTimeout(() => openBooking(bankId, bankName), 500);
        } else {
            // If on another page, redirect to donate page with params (simulated)
            window.location.href = `user_donate.html?bankId=${bankId}&bankName=${encodeURIComponent(bankName)}`;
        }
    };

    // ✅ Open Booking Form
    window.openBooking = function (bankId, bankName) {
        const donateSection = document.getElementById("donate");
        if (donateSection) {
            donateSection.style.display = "block";
            const bookingTitle = document.getElementById("bookingTitle");
            const bloodBankIdInput = document.getElementById("bloodBankId");
            const locationInput = document.getElementById("location");

            if (bookingTitle) bookingTitle.innerHTML = `Booking appointment at: <strong>${bankName}</strong>`;
            if (bloodBankIdInput) bloodBankIdInput.value = bankId;
            if (locationInput) locationInput.value = bankName;

            donateSection.scrollIntoView({ behavior: "smooth" });
        } else {
            window.location.href = `user_donate.html?bankId=${bankId}&bankName=${encodeURIComponent(bankName)}`;
        }
    };

    // Check for URL parameters (to handle redirect from sidebar on other pages)
    const urlParams = new URLSearchParams(window.location.search);
    const paramBankId = urlParams.get('bankId');
    const paramBankName = urlParams.get('bankName');
    if (paramBankId && paramBankName) {
        setTimeout(() => openBooking(paramBankId, paramBankName), 800);
    }

    // ✅ Donation form handler (Schedule Appointment)
    const donationForm = document.getElementById('donationForm');
    if (donationForm) {
        donationForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const requestData = {
                donorEmail: loggedInEmail,
                bloodBankId: parseInt(document.getElementById('bloodBankId').value),
                preferredDate: document.getElementById('date').value,
                location: document.getElementById('location').value, // Blood Bank Name
                healthDeclaration: document.getElementById('health').value,
                bloodGroup: user.bloodGroup || "Unknown",
                donationType: document.getElementById('donationType').value
            };

            try {
                const res = await fetch("http://localhost:8080/api/donation-requests/request", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(requestData)
                });

                if (res.ok) {
                    alert("✅ Appointment request submitted! The blood bank will review it.");
                    window.location.href = "user_dashboard.html"; // Redirect to dashboard overview
                } else {
                    const errorMsg = await res.text();
                    alert(`❌ Request Failed: ${errorMsg}`);
                    console.error("Request failed details:", errorMsg);
                }
            } catch (error) {
                console.error("❌ Error submitting request:", error);
                alert("Failed to submit request. Try again later.");
            }
        });
    }

    // ✅ Logout button
    const logoutBtn = document.getElementById("logoutBtn");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", (event) => {
            event.preventDefault();
            localStorage.removeItem("loggedInUserEmail");
            localStorage.clear();
            window.location.href = "index.html";
        });
    }
});

// ✅ Helper: calculate profile completion %
function calculateProfileCompletion(user) {
    const fields = [user.name, user.email, user.city, user.phone, user.bloodGroup];
    const filled = fields.filter(f => f && f !== "NULL").length;
    return Math.round((filled / fields.length) * 100);
}

// ✅ Helper: Get color based on status (IEEE Standards)
function getStatusColor(status) {
    switch (status) {
        case 'Pending': return '#ff9800'; // Orange
        case 'Verified': return '#2196f3'; // Blue
        case 'COLLECTED': return '#4caf50'; // Green
        case 'TESTED': return '#9c27b0'; // Purple
        case 'STORED': return '#00bcd4'; // Cyan
        case 'APPROVED': return '#78909c'; // Blue Grey
        case 'USED': return '#e91e63'; // Pink (Final State)
        case 'Expired': return '#f44336'; // Red
        default: return '#607d8b'; // Grey
    }
}

// ✅ Feature: Share Certificate (Mock/Clipboard)
window.shareCertificate = function (donationId, blockHash) {
    if (!blockHash || blockHash === "Generating...") {
        alert("⚠️ Certificate is still being verified on the blockchain. Please try again later.");
        return;
    }

    // Copy hash to clipboard as a shareable verification ID
    navigator.clipboard.writeText(blockHash).then(() => {
        alert(`✅ Certificate Verification ID copied to clipboard!\n\nYou can share this ID (${blockHash}) to verify your donation authenticity on the BloodChain network.`);
    }).catch(err => {
        console.error('Failed to copy ID: ', err);
        alert(`📄 Certificate Shared! (Verification ID: ${blockHash})`);
    });
};

// ✅ Feature: Download Certificate (Real PDF Download)
async function downloadCertificate(donationId) {
    try {
        console.log(`📄 Requesting certificate for Donation #${donationId}...`);
        const response = await fetch(`http://localhost:8080/api/donations/${donationId}/certificate`);

        if (!response.ok) throw new Error("Failed to download certificate");

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `BloodChain_Certificate_${donationId}.pdf`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);

        console.log("✅ Certificate downloaded successfully.");
    } catch (error) {
        console.error("❌ Error downloading certificate:", error);
        alert("⚠️ Failed to download certificate. It might still be processing or the server is unavailable.");
    }
}
