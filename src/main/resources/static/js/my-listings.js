(function () {
  CM.renderNavbar("my-listings");
  if (!CM.requireAuth()) return;

  const list = document.getElementById("list");
  const emptyState = document.getElementById("emptyState");
  const alertBox = document.getElementById("alert");

  function rowHtml(listing) {
    const thumb = listing.imageUrls && listing.imageUrls[0] ? listing.imageUrls[0] : "";
    return `
      <div class="row-card" data-id="${listing.id}">
        ${thumb ? `<img class="row-thumb" src="${thumb}">` : `<div class="row-thumb"></div>`}
        <div class="row-body">
          <div class="row-title">
            <a href="/listing.html?id=${listing.id}">${CM.escapeHtml(listing.title)}</a>
            <span class="badge ${CM.statusBadgeClass(listing.status)}">${CM.labelize(listing.status)}</span>
          </div>
          <div class="listing-meta">${CM.formatPrice(listing.price)} · ${CM.labelize(listing.category)}</div>
        </div>
        <div class="row-actions">
          <select class="status-select" data-id="${listing.id}">
            <option value="AVAILABLE" ${listing.status === "AVAILABLE" ? "selected" : ""}>Available</option>
            <option value="RESERVED" ${listing.status === "RESERVED" ? "selected" : ""}>Reserved</option>
            <option value="SOLD" ${listing.status === "SOLD" ? "selected" : ""}>Sold</option>
          </select>
          <a href="/new-listing.html?id=${listing.id}" class="btn btn-secondary btn-sm">Edit</a>
          <button class="btn btn-danger btn-sm delete-btn" data-id="${listing.id}">Delete</button>
        </div>
      </div>`;
  }

  async function load() {
    try {
      const result = await Api.myListings({ size: 100, sort: "createdAt,desc" });
      if (result.content.length === 0) {
        emptyState.hidden = false;
        return;
      }
      list.innerHTML = result.content.map(rowHtml).join("");
      attachHandlers();
    } catch (err) {
      CM.showAlert(alertBox, "Couldn't load your listings: " + err.message);
    }
  }

  function attachHandlers() {
    list.querySelectorAll(".status-select").forEach((sel) => {
      sel.addEventListener("change", async () => {
        try {
          await Api.updateListingStatus(sel.dataset.id, sel.value);
          const badge = sel.closest(".row-card").querySelector(".badge");
          badge.textContent = CM.labelize(sel.value);
          badge.className = "badge " + CM.statusBadgeClass(sel.value);
        } catch (err) {
          CM.showAlert(alertBox, err.message);
        }
      });
    });

    list.querySelectorAll(".delete-btn").forEach((btn) => {
      btn.addEventListener("click", async () => {
        if (!confirm("Delete this listing? This can't be undone.")) return;
        try {
          await Api.deleteListing(btn.dataset.id);
          btn.closest(".row-card").remove();
        } catch (err) {
          CM.showAlert(alertBox, err.message);
        }
      });
    });
  }

  load();
})();
