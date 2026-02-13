// 🏥 Blood Bank Profile Profile Logic
const API_BASE_URL = 'http://localhost:8080/api/bloodbank';
const BANK_ID = 1; // Simulated Logged-in Bank ID

document.addEventListener('DOMContentLoaded', () => {
    loadProfile();
    setupFormSubmission();
});

// ✅ Load Profile Data
async function loadProfile() {
    try {
        const response = await fetch(`${API_BASE_URL}/${BANK_ID}`);
        if (!response.ok) throw new Error("Failed to load profile");

        const bank = await response.json();

        // Populate Form Fields
        setVal('p_name', bank.name);
        setVal('p_license', bank.licenseNumber);
        setVal('p_category', bank.category || 'Private');
        setVal('p_year', bank.establishedYear);

        setVal('p_city', bank.city);
        setVal('p_area', bank.area);
        setVal('p_state', bank.state);
        setVal('p_pincode', bank.pincode);

        // Populate Op Hours (Format: "HH:mm - HH:mm")
        if (bank.operatingHours && bank.operatingHours.includes(' - ')) {
            const [start, end] = bank.operatingHours.split(' - ');
            setVal('p_open_time', start);
            setVal('p_close_time', end);
        }

        // Populate Working Days (Format: "Mon, Tue, Wed")
        if (bank.workingDays) {
            const days = bank.workingDays.split(', ');
            document.querySelectorAll('input[name="day"]').forEach(checkbox => {
                if (days.includes(checkbox.value)) {
                    checkbox.checked = true;
                }
            });
        }

        setVal('p_phone', bank.phone);

        if (document.getElementById('p_accepting')) {
            document.getElementById('p_accepting').checked = bank.acceptingDonations;
        }

    } catch (error) {
        console.error("Error loading profile:", error);
    }
}

// ✅ Setup Form Submission & Edit Toggle
function setupFormSubmission() {
    const form = document.getElementById('profileForm');
    const btnEdit = document.getElementById('btn_edit');
    const btnSave = document.getElementById('btn_save');
    const btnCancel = document.getElementById('btn_cancel');

    if (!form) return;

    // Initial State: View Mode
    toggleEditMode(false);

    // Edit Button Click
    btnEdit.addEventListener('click', () => {
        toggleEditMode(true);
    });

    // Cancel Button Click
    btnCancel.addEventListener('click', () => {
        toggleEditMode(false);
        loadProfile(); // Revert changes
    });

    // Form Submit (Save)
    form.addEventListener('submit', async function (e) {
        e.preventDefault();

        // Construct Operating Hours String
        const openTime = getVal('p_open_time');
        const closeTime = getVal('p_close_time');
        const operatingHours = (openTime && closeTime) ? `${openTime} - ${closeTime}` : '';

        // Construct Working Days String
        const selectedDays = Array.from(document.querySelectorAll('input[name="day"]:checked'))
            .map(cb => cb.value)
            .join(', ');

        const updatedProfile = {
            name: getVal('p_name'),
            licenseNumber: getVal('p_license'),
            category: getVal('p_category'),
            establishedYear: getVal('p_year'),

            city: getVal('p_city'),
            area: getVal('p_area'),
            state: getVal('p_state'),
            pincode: getVal('p_pincode'),

            operatingHours: operatingHours,
            workingDays: selectedDays,
            phone: getVal('p_phone'),
            acceptingDonations: document.getElementById('p_accepting').checked
        };

        try {
            const res = await fetch(`${API_BASE_URL}/update/${BANK_ID}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(updatedProfile)
            });

            if (res.ok) {
                alert('✅ Profile updated successfully!');
                toggleEditMode(false); // Switch back to view mode
            } else {
                alert('❌ Failed to update profile');
            }
        } catch (error) {
            console.error("Error updating profile:", error);
            alert('Error updating profile');
        }
    });
}

function toggleEditMode(isEdit) {
    const form = document.getElementById('profileForm');
    const inputs = form.querySelectorAll('input, select');

    if (isEdit) {
        form.classList.remove('view-mode');
        inputs.forEach(input => input.disabled = false);
        document.getElementById('btn_edit').style.display = 'none';
        document.getElementById('btn_save').style.display = 'inline-block';
        document.getElementById('btn_cancel').style.display = 'inline-block';
    } else {
        form.classList.add('view-mode');
        inputs.forEach(input => input.disabled = true);
        document.getElementById('btn_edit').style.display = 'inline-block';
        document.getElementById('btn_save').style.display = 'none';
        document.getElementById('btn_cancel').style.display = 'none';
    }
}

// Helper functions
function setVal(id, val) {
    const el = document.getElementById(id);
    if (el) el.value = val || '';
}

function getVal(id) {
    const el = document.getElementById(id);
    return el ? el.value : null;
}
