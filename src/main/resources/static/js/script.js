// 🧠 Common function to toggle password visibility
function setupPasswordToggle() {
  const toggle = document.getElementById('togglePassword');
  const passwordInput = document.getElementById('password');
  if (toggle && passwordInput) {
    toggle.addEventListener('click', function() {
      const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
      passwordInput.setAttribute('type', type);
      this.textContent = type === 'password' ? '👁️' : '🔒';
    });
  }
}
setupPasswordToggle();
// ===============================
// 🔐 PASSWORD STRENGTH METER
// ===============================
const passwordInput = document.getElementById('password');
const strengthBar = document.getElementById('strengthBar');
const strengthText = document.getElementById('strengthText');

if (passwordInput && strengthBar && strengthText) {
    passwordInput.addEventListener('input', function () {
        const password = this.value;

        let strength = 0;
        if (password.length >= 8) strength++;
        if (password.match(/[a-z]/) && password.match(/[A-Z]/)) strength++;
        if (password.match(/\d/)) strength++;
        if (password.match(/[^a-zA-Z\d]/)) strength++;

        // Reset the bar style
        strengthBar.className = 'strength-bar';

        if (password.length === 0) {
            strengthText.textContent = 'Password strength';
        } else if (strength <= 1) {
            strengthBar.classList.add('strength-weak');
            strengthText.textContent = 'Weak password';
        } else if (strength <= 2) {
            strengthBar.classList.add('strength-medium');
            strengthText.textContent = 'Medium strength';
        } else {
            strengthBar.classList.add('strength-strong');
            strengthText.textContent = 'Strong password';
        }
    });
}


// ===============================
// 📝 SIGNUP FORM HANDLER
// ===============================
const signupForm = document.getElementById("signupForm");

if (signupForm) {
    signupForm.addEventListener("submit", async function (e) {
        e.preventDefault();

        const role = document.getElementById("role").value.trim().toLowerCase();
        const name = document.getElementById("name").value.trim();
        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value.trim();
        const confirm = document.getElementById("confirm").value.trim();
        const errorMsg = document.getElementById("errorMsg");
        errorMsg.textContent = "";

        if (!role || !name || !email || !password || !confirm) {
            errorMsg.textContent = "⚠️ Please fill all fields.";
            return;
        }

        if (password !== confirm) {
            errorMsg.textContent = "⚠️ Passwords do not match.";
            return;
        }

        const user = { name, email, password, role };

        try {
            const res = await fetch("http://localhost:8080/api/auth/signup", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(user),
            });

            const data = await res.text();
            console.log("Signup Response:", data);

            if (res.ok && data.toLowerCase().includes("success")) {
                // ✅ Role-based redirection
                if (role === "user") {
                    alert("✅ Signup successful! Redirecting to User Login...");
                    window.location.href = "user_login.html";
                } else if (role === "hospital") {
                    alert("✅ Signup successful! Redirecting to Hospital Login...");
                    window.location.href = "hospital_login.html";
                } else if (role === "bloodbank") {
                    alert("✅ Signup successful! Redirecting to Blood Bank Login...");
                    window.location.href = "bloodbank_login.html";
                } else {
                    alert("✅ Signup successful!");
                }
            } else {
                errorMsg.textContent = data.startsWith("❌") ? data : "❌ " + data;
            }
        } catch (error) {
            console.error("Error:", error);
            errorMsg.textContent =
                "⚠️ Server not responding. Please try again later.";
        }
    });
}

// 🧍‍♂️ 2️⃣ USER LOGIN FORM
const userLoginForm = document.getElementById('userLoginForm');
if (userLoginForm) {
  userLoginForm.addEventListener('submit', async function(e) {
    e.preventDefault();

    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value.trim();
    const errorMsg = document.getElementById('errorMsg');
    errorMsg.textContent = "";

    if (!email || !password) {
      errorMsg.textContent = "Please fill all fields.";
      return;
    }

    try {
      const res = await fetch('http://localhost:8080/api/auth/login/user', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
      });

      const msg = await res.text();
      console.log("Response:", msg); // debug line

      if (msg.toLowerCase().includes("successful")) {
        alert("✅ Login successful!");
        localStorage.setItem("loggedInUserEmail", email);
        window.location.href = "user_dashboard.html"; // ✅ redirect
      } else {
        errorMsg.textContent = "❌ " + msg;
      }
    } catch (err) {
      console.error("Error:", err);
      errorMsg.textContent = "⚠️ Unable to connect to server.";
    }
  });
}

// 🏥 3️⃣ BLOODBANK LOGIN FORM
const bloodbankLoginForm = document.getElementById('bloodbankLoginForm');
if (bloodbankLoginForm) {
  bloodbankLoginForm.addEventListener('submit', async function(e) {
    e.preventDefault();

    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value.trim();

    if (!email || !password) {
      alert("Please fill all fields.");
      return;
    }

    try {
      const res = await fetch('http://localhost:8080/api/auth/login/bloodbank', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
      });

      const msg = await res.text();
      console.log("Response:", msg);

      if (msg.toLowerCase().includes("successful")) {
        alert("✅ Login successful!");
        window.location.href = "bloodbank_dashboard.html"; // ✅ redirect
      } else {
        alert("❌ " + msg);
      }
    } catch (err) {
      console.error("Error:", err);
      alert("⚠️ Unable to connect to server.");
    }
  });
}
// ===============================
// 🏥 Hospital Login
// ===============================
const hospitalLoginForm = document.getElementById('hospitalLoginForm');
if (hospitalLoginForm) {
    const toggleBtn = document.getElementById('togglePassword');
    toggleBtn.addEventListener('click', function () {
        const passInput = document.getElementById('password');
        const type = passInput.getAttribute('type') === 'password' ? 'text' : 'password';
        passInput.setAttribute('type', type);
        this.textContent = type === 'password' ? '👁️' : '🔒';
    });

    hospitalLoginForm.addEventListener('submit', async function (e) {
        e.preventDefault();

        const hospitalId = document.getElementById('hospitalId').value.trim();
        const password = document.getElementById('password').value.trim();
        const errorMsg = document.getElementById('errorMsg');
        errorMsg.textContent = "";

        if (!hospitalId || !password) {
            errorMsg.textContent = "⚠️ Please fill all fields.";
            return;
        }

        const hospital = { email: hospitalId, password: password };

        try {
            const res = await fetch("http://localhost:8080/api/auth/login/hospital", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(hospital)
            });

            const msg = await res.text();
            console.log("Response:", msg);

            if (res.ok && msg.toLowerCase().includes("successful")) {
                alert("✅ Hospital Login successful!");

                // ✅ Store consistent session info
                sessionStorage.setItem("userEmail", hospitalId);
                sessionStorage.setItem("userRole", "hospital");

                window.location.href = "hospital_dashboard.html";
            } else {
                errorMsg.textContent = "❌ " + msg;
            }
        } catch (err) {
            console.error("Error:", err);
            errorMsg.textContent = "⚠️ Unable to connect to server. Please try again later.";
        }
    });
}


// ===============================
// 🛡️ ADMIN LOGIN HANDLER
// ===============================
const adminLoginForm = document.getElementById('adminLoginForm');

if (adminLoginForm) {
    adminLoginForm.addEventListener('submit', async function (e) {
        e.preventDefault();

        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value.trim();
        const errorMsg = document.getElementById('errorMsg');
        errorMsg.textContent = "";

        if (!email || !password) {
            errorMsg.textContent = "⚠️ Please fill all fields.";
            return;
        }

        try {
            const res = await fetch('http://localhost:8080/api/auth/login/admin', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });

            const msg = await res.text();
            console.log("Admin Login Response:", msg);

            if (res.ok && msg.toLowerCase().includes("successful")) {
                alert("✅ Admin Login successful!");
                window.location.href = "admin_dashboard.html";
            } else {
                errorMsg.textContent = "❌ " + msg;
            }
        } catch (err) {
            console.error("Error:", err);
            errorMsg.textContent = "⚠️ Unable to connect to server.";
        }
    });
}
