// Shared navbar + small utilities used across pages.
(function () {
  function renderNavbar(activePage) {
    const mount = document.getElementById("navbar");
    if (!mount) return;

    const user = window.Api.getUser();
    const loggedIn = window.Api.isLoggedIn();

    const link = (href, label, page) =>
      `<a href="${href}" class="${activePage === page ? "active" : ""}">${label}</a>`;

    const links = [link("/index.html", "Browse", "browse")];
    if (loggedIn) {
      links.push(link("/new-listing.html", "Post an item", "new-listing"));
      links.push(link("/my-listings.html", "My listings", "my-listings"));
      links.push(link("/favorites.html", "Favorites", "favorites"));
      links.push(link("/messages.html", "Messages", "messages"));
    }

    const right = loggedIn
      ? `<span class="nav-user">${escapeHtml(user ? user.fullName : "")}</span>
         <a href="/profile.html" class="btn btn-secondary btn-sm">Profile</a>
         <button id="navLogoutBtn" class="btn btn-secondary btn-sm">Log out</button>`
      : `<a href="/login.html" class="btn btn-secondary btn-sm">Log in</a>
         <a href="/register.html" class="btn btn-sm">Sign up</a>`;

    mount.innerHTML = `
      <div class="navbar">
        <div class="navbar-inner">
          <a href="/index.html" class="brand">Campus<span>Market</span></a>
          <div class="nav-links">${links.join("")}</div>
          <div class="nav-right">${right}</div>
        </div>
      </div>`;

    const logoutBtn = document.getElementById("navLogoutBtn");
    if (logoutBtn) {
      logoutBtn.addEventListener("click", () => {
        window.Api.clearSession();
        window.location.href = "/index.html";
      });
    }
  }

  // Redirects to the login page if not authenticated. Call at the top of
  // any page that requires a logged-in user.
  function requireAuth() {
    if (!window.Api.isLoggedIn()) {
      const next = encodeURIComponent(window.location.pathname + window.location.search);
      window.location.href = "/login.html?next=" + next;
      return false;
    }
    return true;
  }

  function escapeHtml(str) {
    if (str === null || str === undefined) return "";
    return String(str)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#39;");
  }

  function formatPrice(value) {
    const n = Number(value);
    return "₹" + n.toLocaleString("en-IN", { maximumFractionDigits: 2 });
  }

  function formatDate(iso) {
    if (!iso) return "";
    const d = new Date(iso);
    return d.toLocaleDateString(undefined, { day: "numeric", month: "short" }) +
      " " + d.toLocaleTimeString(undefined, { hour: "2-digit", minute: "2-digit" });
  }

  function timeAgo(iso) {
    if (!iso) return "";
    const diffMs = Date.now() - new Date(iso).getTime();
    const mins = Math.round(diffMs / 60000);
    if (mins < 1) return "just now";
    if (mins < 60) return mins + "m ago";
    const hours = Math.round(mins / 60);
    if (hours < 24) return hours + "h ago";
    const days = Math.round(hours / 24);
    if (days < 7) return days + "d ago";
    return formatDate(iso);
  }

  function labelize(enumValue) {
    if (!enumValue) return "";
    return enumValue
      .toLowerCase()
      .split("_")
      .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
      .join(" ");
  }

  function statusBadgeClass(status) {
    return {
      AVAILABLE: "badge-available",
      RESERVED: "badge-reserved",
      SOLD: "badge-sold",
    }[status] || "badge-available";
  }

  function showAlert(container, message, type = "error") {
    if (!container) return;
    container.className = "alert alert-" + type;
    container.textContent = message;
    container.hidden = false;
  }

  function hideAlert(container) {
    if (!container) return;
    container.hidden = true;
  }

  function qs(name) {
    return new URLSearchParams(window.location.search).get(name);
  }

  window.CM = {
    renderNavbar,
    requireAuth,
    escapeHtml,
    formatPrice,
    formatDate,
    timeAgo,
    labelize,
    statusBadgeClass,
    showAlert,
    hideAlert,
    qs,
  };
})();
