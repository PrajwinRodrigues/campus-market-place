(function () {
  CM.renderNavbar("new-listing");
  if (!CM.requireAuth()) return;

  const form = document.getElementById("listingForm");
  const alertBox = document.getElementById("alert");
  const submitBtn = document.getElementById("submitBtn");
  const imageSection = document.getElementById("imageSection");
  const imageList = document.getElementById("imageList");
  const imageInput = document.getElementById("imageInput");
  const formHeading = document.getElementById("formHeading");
  const doneLink = document.getElementById("doneLink");

  const editId = CM.qs("id");
  let listingId = editId ? Number(editId) : null;
  let currentImages = [];

  function readForm() {
    const data = new FormData(form);
    return {
      title: data.get("title").trim(),
      description: data.get("description").trim(),
      price: Number(data.get("price")),
      category: data.get("category"),
      condition: data.get("condition"),
      location: data.get("location") || null,
      imageUrls: currentImages,
    };
  }

  function renderImages() {
    imageList.innerHTML = currentImages
      .map(
        (url, i) => `
        <div style="position:relative">
          <img src="${url}" alt="">
          <button type="button" class="btn btn-danger btn-sm" data-idx="${i}"
            style="position:absolute;top:-8px;right:-8px;padding:2px 6px;border-radius:50%;">✕</button>
        </div>`
      )
      .join("");

    imageList.querySelectorAll("button[data-idx]").forEach((btn) => {
      btn.addEventListener("click", async () => {
        const idx = Number(btn.dataset.idx);
        const removed = currentImages.slice();
        removed.splice(idx, 1);
        try {
          const updated = await Api.updateListing(listingId, { ...readForm(), imageUrls: removed });
          currentImages = updated.imageUrls;
          renderImages();
        } catch (err) {
          CM.showAlert(alertBox, err.message);
        }
      });
    });
  }

  function enterEditModeUi(listing) {
    listingId = listing.id;
    formHeading.textContent = "Edit your listing";
    document.title = "Edit listing — Campus Marketplace";
    submitBtn.textContent = "Save changes";
    imageSection.hidden = false;
    doneLink.href = "/listing.html?id=" + listingId;
    currentImages = listing.imageUrls || [];
    renderImages();
  }

  async function loadForEdit() {
    try {
      const listing = await Api.getListing(listingId);
      const me = Api.getUser();
      if (!me || listing.sellerId !== me.id) {
        CM.showAlert(alertBox, "You can only edit your own listings.");
        submitBtn.disabled = true;
        return;
      }
      form.title.value = listing.title;
      form.description.value = listing.description;
      form.price.value = listing.price;
      form.category.value = listing.category;
      form.condition.value = listing.condition;
      form.location.value = listing.location || "";
      enterEditModeUi(listing);
    } catch (err) {
      CM.showAlert(alertBox, "Could not load listing: " + err.message);
    }
  }

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    CM.hideAlert(alertBox);
    submitBtn.disabled = true;

    try {
      const payload = readForm();
      if (listingId) {
        await Api.updateListing(listingId, payload);
        CM.showAlert(alertBox, "Listing updated.", "success");
      } else {
        const created = await Api.createListing(payload);
        enterEditModeUi(created);
        CM.showAlert(alertBox, "Listing published! Add some photos below.", "success");
      }
    } catch (err) {
      CM.showAlert(alertBox, err.message);
    } finally {
      submitBtn.disabled = false;
    }
  });

  imageInput.addEventListener("change", async () => {
    const file = imageInput.files[0];
    if (!file || !listingId) return;
    try {
      const result = await Api.uploadListingImage(listingId, file);
      currentImages.push(result.imageUrl);
      renderImages();
    } catch (err) {
      CM.showAlert(alertBox, err.message);
    } finally {
      imageInput.value = "";
    }
  });

  if (listingId) {
    loadForEdit();
  }
})();
