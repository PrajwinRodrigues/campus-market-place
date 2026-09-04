(function () {
  CM.renderNavbar(null);

  if (Api.isLoggedIn()) {
    window.location.href = "/index.html";
    return;
  }

  const form = document.getElementById("loginForm");
  const alertBox = document.getElementById("alert");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    CM.hideAlert(alertBox);

    const data = new FormData(form);
    const payload = { email: data.get("email").trim(), password: data.get("password") };

    const submitBtn = form.querySelector("button[type=submit]");
    submitBtn.disabled = true;
    try {
      const auth = await Api.login(payload);
      Api.setSession(auth);
      const next = CM.qs("next");
      window.location.href = next ? decodeURIComponent(next) : "/index.html";
    } catch (err) {
      CM.showAlert(alertBox, err.status === 401 ? "Invalid email or password" : err.message);
      submitBtn.disabled = false;
    }
  });
})();
