(function () {
  CM.renderNavbar(null);
  if (!CM.requireAuth()) return;

  const form = document.getElementById("profileForm");
  const alertBox = document.getElementById("alert");

  async function load() {
    try {
      const me = await Api.me();
      document.getElementById("email").value = me.email;
      form.fullName.value = me.fullName || "";
      form.phone.value = me.phone || "";
      form.collegeName.value = me.collegeName || "";
      form.hostelOrLocation.value = me.hostelOrLocation || "";
    } catch (err) {
      CM.showAlert(alertBox, "Couldn't load your profile: " + err.message);
    }
  }

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    CM.hideAlert(alertBox);
    const payload = {
      fullName: form.fullName.value.trim(),
      phone: form.phone.value || null,
      collegeName: form.collegeName.value || null,
      hostelOrLocation: form.hostelOrLocation.value || null,
    };
    try {
      const updated = await Api.updateMe(payload);
      const user = Api.getUser();
      user.fullName = updated.fullName;
      localStorage.setItem("cm_user", JSON.stringify(user));
      CM.showAlert(alertBox, "Profile updated.", "success");
      CM.renderNavbar(null);
    } catch (err) {
      CM.showAlert(alertBox, err.message);
    }
  });

  load();
})();
