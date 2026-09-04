(function () {
  CM.renderNavbar(null);

  const id = CM.qs("id");
  const alertBox = document.getElementById("alert");
  const loading = document.getElementById("loading");
  const detail = document.getElementById("detail");

  if (!id) {
    loading.hidden = true;
    CM.showAlert(alertBox, "No listing specified.");
    return;
  }

  let listing = null;
  let isFavorited = false;

  async function load() {
    try {
      listing = await Api.getListing(id);
    } catch (err) {
      loading.hidden = true;
      CM.showAlert(alertBox, "Couldn't load this listing: " + err.message);
      return;
    }

    document.title = listing.title + " — Campus Marketplace";
    document.getElementById("title").textContent = listing.title;
    document.getElementById("meta").textContent =
      `${CM.labelize(listing.category)} · ${CM.labelize(listing.condition)} · Posted ${CM.timeAgo(listing.createdAt)}`;
    document.getElementById("price").textContent = CM.formatPrice(listing.price);
    document.getElementById("description").textContent = listing.description;
    document.getElementById("location").textContent = listing.location ? "📍 " + listing.location : "";
    document.getElementById("sellerName").textContent = listing.sellerName;

    const badge = document.getElementById("statusBadge");
    badge.textContent = CM.labelize(listing.status);
    badge.className = "badge " + CM.statusBadgeClass(listing.status);

    renderGallery(listing.imageUrls || []);

    const me = Api.getUser();
    const isOwner = me && me.id === listing.sellerId;

    if (isOwner) {
      document.getElementById("ownerActions").hidden = false;
      document.getElementById("statusSelect").value = listing.status;
      document.getElementById("editLink").href = "/new-listing.html?id=" + listing.id;
      document.getElementById("sellerPhone").textContent = listing.sellerPhone ? "📞 " + listing.sellerPhone : "";
    } else if (Api.isLoggedIn()) {
      document.getElementById("buyerActions").hidden = false;
      document.getElementById("messageForm").hidden = false;
      document.getElementById("sellerPhone").textContent = "Message the seller to get contact details.";
      await refreshFavoriteState();
    } else {
      document.getElementById("loginToMessage").hidden = false;
      document.getElementById("sellerPhone").textContent = "";
    }

    loading.hidden = true;
    detail.hidden = false;
  }

  function renderGallery(images) {
    const mainImage = document.getElementById("mainImage");
    const thumbs = document.getElementById("thumbs");
    if (images.length === 0) {
      mainImage.hidden = true;
      thumbs.innerHTML = "";
      return;
    }
    mainImage.src = images[0];
    mainImage.hidden = false;
    thumbs.innerHTML = images
      .map((url, i) => `<img src="${url}" class="${i === 0 ? "active" : ""}" data-src="${url}">`)
      .join("");
    thumbs.querySelectorAll("img").forEach((img) => {
      img.addEventListener("click", () => {
        mainImage.src = img.dataset.src;
        thumbs.querySelectorAll("img").forEach((t) => t.classList.remove("active"));
        img.classList.add("active");
      });
    });
  }

  async function refreshFavoriteState() {
    try {
      const favorites = await Api.myFavorites();
      isFavorited = favorites.some((f) => f.listing.id === listing.id);
      updateFavoriteBtn();
    } catch (err) {
      // non-fatal — favorite button just won't reflect saved state
    }
  }

  function updateFavoriteBtn() {
    const btn = document.getElementById("favoriteBtn");
    btn.textContent = isFavorited ? "★ Saved" : "☆ Save";
  }

  document.getElementById("favoriteBtn").addEventListener("click", async () => {
    const btn = document.getElementById("favoriteBtn");
    btn.disabled = true;
    try {
      if (isFavorited) {
        await Api.removeFavorite(listing.id);
      } else {
        await Api.addFavorite(listing.id);
      }
      isFavorited = !isFavorited;
      updateFavoriteBtn();
    } catch (err) {
      CM.showAlert(alertBox, err.message);
    } finally {
      btn.disabled = false;
    }
  });

  document.getElementById("messageForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const textarea = document.getElementById("messageText");
    const content = textarea.value.trim();
    if (!content) return;
    try {
      await Api.sendMessage({ listingId: listing.id, receiverId: listing.sellerId, content });
      textarea.value = "";
      CM.showAlert(alertBox, "Message sent! Check Messages for the reply.", "success");
    } catch (err) {
      CM.showAlert(alertBox, err.message);
    }
  });

  document.getElementById("statusSelect").addEventListener("change", async (e) => {
    try {
      listing = await Api.updateListingStatus(listing.id, e.target.value);
      const badge = document.getElementById("statusBadge");
      badge.textContent = CM.labelize(listing.status);
      badge.className = "badge " + CM.statusBadgeClass(listing.status);
    } catch (err) {
      CM.showAlert(alertBox, err.message);
    }
  });

  document.getElementById("deleteBtn").addEventListener("click", async () => {
    if (!confirm("Delete this listing? This can't be undone.")) return;
    try {
      await Api.deleteListing(listing.id);
      window.location.href = "/my-listings.html";
    } catch (err) {
      CM.showAlert(alertBox, err.message);
    }
  });

  load();
})();
