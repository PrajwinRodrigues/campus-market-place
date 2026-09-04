(function () {
  CM.renderNavbar("favorites");
  if (!CM.requireAuth()) return;

  const list = document.getElementById("list");
  const emptyState = document.getElementById("emptyState");
  const alertBox = document.getElementById("alert");

  function rowHtml(fav) {
    const listing = fav.listing;
    const thumb = listing.imageUrls && listing.imageUrls[0] ? listing.imageUrls[0] : "";
    return `
      <div class="row-card" data-listing-id="${listing.id}">
        ${thumb ? `<img class="row-thumb" src="${thumb}">` : `<div class="row-thumb"></div>`}
        <div class="row-body">
          <div class="row-title">
            <a href="/listing.html?id=${listing.id}">${CM.escapeHtml(listing.title)}</a>
            <span class="badge ${CM.statusBadgeClass(listing.status)}">${CM.labelize(listing.status)}</span>
          </div>
          <div class="listing-meta">${CM.formatPrice(listing.price)} · ${CM.escapeHtml(listing.sellerName)}</div>
        </div>
        <div class="row-actions">
          <button class="btn btn-secondary btn-sm remove-btn" data-listing-id="${listing.id}">Remove</button>
        </div>
      </div>`;
  }

  async function load() {
    try {
      const favorites = await Api.myFavorites();
      if (favorites.length === 0) {
        emptyState.hidden = false;
        return;
      }
      list.innerHTML = favorites.map(rowHtml).join("");
      list.querySelectorAll(".remove-btn").forEach((btn) => {
        btn.addEventListener("click", async () => {
          try {
            await Api.removeFavorite(btn.dataset.listingId);
            btn.closest(".row-card").remove();
            if (!list.querySelector(".row-card")) emptyState.hidden = false;
          } catch (err) {
            CM.showAlert(alertBox, err.message);
          }
        });
      });
    } catch (err) {
      CM.showAlert(alertBox, "Couldn't load favorites: " + err.message);
    }
  }

  load();
})();
