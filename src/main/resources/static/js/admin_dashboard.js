const API_BASE = "http://localhost:8080/api/admin";

// ====== Tab Navigation ======
document.querySelectorAll(".nav-links a[data-tab]").forEach(link => {
  link.addEventListener("click", e => {
    e.preventDefault();
    const targetTab = link.dataset.tab;

    // Update active link
    document.querySelectorAll(".nav-links a").forEach(l => l.classList.remove("active"));
    link.classList.add("active");

    // Update active content
    document.querySelectorAll(".tab-content").forEach(tab => tab.classList.remove("active"));
    const targetSection = document.getElementById(targetTab);
    if (targetSection) {
      targetSection.classList.add("active");
    }

    // Auto-fetch data if needed
    if (targetTab !== 'overview') {
      fetchData(targetTab, `${targetTab.slice(0, -1)}Table`);
    }
  });
});

// ====== Logout ======
const logoutBtn = document.getElementById("logoutBtn");
if (logoutBtn) {
  logoutBtn.addEventListener("click", (e) => {
    e.preventDefault();
    if (confirm("Are you sure you want to logout from the Admin Panel?")) {
      localStorage.removeItem("loggedInAdminEmail");
      window.location.href = "admin_login.html";
    }
  });
}

// ====== Fetch Summary Stats ======
async function loadSummary() {
  try {
    const [donors, hospitals, bloodbanks] = await Promise.all([
      fetch(`${API_BASE}/donors`).then(res => res.json()),
      fetch(`${API_BASE}/hospitals`).then(res => res.json()),
      fetch(`${API_BASE}/bloodbanks`).then(res => res.json())
    ]);

    document.getElementById("donorCount").textContent = donors.length;
    document.getElementById("hospitalCount").textContent = hospitals.length;
    document.getElementById("bloodbankCount").textContent = bloodbanks.length;

    // Remove pulse after load
    document.querySelectorAll(".number").forEach(n => n.classList.remove("pulse"));
  } catch (error) {
    console.error("Error loading summary:", error);
  }
}

// ====== Fetch and Populate Tables ======
async function fetchData(endpoint, tableId) {
  try {
    const res = await fetch(`${API_BASE}/${endpoint}`);
    const data = await res.json();
    const tbody = document.querySelector(`#${tableId} tbody`);
    if (!tbody) return;

    tbody.innerHTML = "";

    data.forEach(u => {
      const actionBtn = `<button class="contact-btn" onclick="contactUser('${u.email}')"><i class="fas fa-envelope"></i> Contact</button>`;
      let row = "";

      if (endpoint === "donors") {
        row = `<td>${u.name}</td><td>${u.email}</td><td><span class="badge-verified">${u.bloodGroup}</span></td><td>${u.city}</td><td>${u.lastDonation || 'Never'}</td><td>${actionBtn}</td>`;
      } else if (endpoint === "hospitals") {
        row = `<td>${u.name}</td><td>${u.email}</td><td>${u.contactNumber || 'N/A'}</td><td>${u.city}</td><td><span class="badge-accepting">${u.totalPatients || 0}</span></td><td>${actionBtn}</td>`;
      } else if (endpoint === "bloodbanks") {
        row = `<td>${u.name}</td><td>${u.email}</td><td>${u.city}</td><td>${u.capacity} ml</td><td><span class="badge-verified">${u.availableUnits} units</span></td><td>${actionBtn}</td>`;
      }

      tbody.insertAdjacentHTML('beforeend', `<tr>${row}</tr>`);
    });
  } catch (error) {
    console.error(`Error fetching ${endpoint}:`, error);
  }
}

// ====== Download Report ======
function downloadReport(type) {
  window.open(`${API_BASE}/reports/${type}`, "_blank");
}

// ====== Send Email ======
async function contactUser(email) {
  const subject = prompt(`Enter subject for ${email}:`);
  const body = prompt("Enter message content:");
  if (!subject || !body) return;

  try {
    await fetch(`${API_BASE}/contact`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ to: email, subject, body })
    });
    alert(`✅ Message sent to ${email} successfully.`);
  } catch (error) {
    alert("❌ Failed to send email. Please check server logs.");
  }
}

// ====== Initialization ======
window.onload = () => {
  loadSummary();
  // Initial data load for tables is now handled on tab switch to save bandwidth,
  // but we can pre-load the default view if needed.
};
