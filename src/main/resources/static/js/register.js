(function () {
  CM.renderNavbar(null);

  if (Api.isLoggedIn()) {
    window.location.href = "/index.html";
    return;
  }

  const form = document.getElementById("registerForm");
  const alertBox = document.getElementById("alert");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    CM.hideAlert(alertBox);

    const data = new FormData(form);
    const payload = {
      fullName: data.get("fullName").trim(),
      email: data.get("email").trim(),
      password: data.get("password"),
      phone: data.get("phone") || null,
      collegeName: data.get("collegeName") || null,
      hostelOrLocation: data.get("hostelOrLocation") || null,
    };

    const submitBtn = form.querySelector("button[type=submit]");
    submitBtn.disabled = true;
    try {
      const auth = await Api.register(payload);
      Api.setSession(auth);
      window.location.href = "/index.html";
    } catch (err) {
      CM.showAlert(alertBox, err.message);
      submitBtn.disabled = false;
    }
  });
})();
