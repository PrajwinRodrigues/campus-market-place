(function () {
  CM.renderNavbar("browse");

  const grid = document.getElementById("listingGrid");
  const emptyState = document.getElementById("emptyState");
  const resultsInfo = document.getElementById("resultsInfo");
  const pagination = document.getElementById("pagination");
  const form = document.getElementById("searchForm");

  let page = 0;
  const size = 12;

  function currentFilters() {
    const data = new FormData(form);
    return {
      keyword: data.get("keyword") || "",
      category: data.get("category") || "",
      condition: data.get("condition") || "",
      minPrice: data.get("minPrice") || "",
      maxPrice: data.get("maxPrice") || "",
      status: "AVAILABLE",
      page,
      size,
      sort: "createdAt,desc",
    };
  }

  function cardHtml(listing) {
    const thumb = listing.imageUrls && listing.imageUrls[0]
      ? `<img src="${listing.imageUrls[0]}" alt="">`
      : "";
    return `
      <a class="listing-card" href="/listing.html?id=${listing.id}">
        <div class="listing-thumb">${thumb}</div>
        <div class="listing-card-body">
          <div class="listing-title">${CM.escapeHtml(listing.title)}</div>
          <div class="listing-price">${CM.formatPrice(listing.price)}</div>
          <div class="listing-meta">
            ${CM.labelize(listing.category)} · ${CM.labelize(listing.condition)}
          </div>
          <div class="listing-meta">${CM.escapeHtml(listing.location || "")}</div>
        </div>
      </a>`;
  }

  async function load() {
    resultsInfo.textContent = "Loading…";
    grid.innerHTML = "";
    emptyState.hidden = true;
    pagination.innerHTML = "";

    try {
      const result = await Api.searchListings(currentFilters());
      resultsInfo.textContent = `${result.totalElements} item${result.totalElements === 1 ? "" : "s"} found`;

      if (result.content.length === 0) {
        emptyState.hidden = false;
      } else {
        grid.innerHTML = result.content.map(cardHtml).join("");
      }

      renderPagination(result);
    } catch (err) {
      resultsInfo.textContent = "Marketplace unavailable";
      grid.innerHTML = `
        <div class="load-error">
          <span class="load-error-icon" aria-hidden="true">↻</span>
          <div><strong>We couldn't reach the marketplace</strong>
          <p>Check your connection and try again in a moment.</p></div>
          <button type="button" class="btn btn-secondary btn-sm" id="retryListings">Try again</button>
        </div>`;
      document.getElementById("retryListings").addEventListener("click", load);
    }
  }

  function renderPagination(result) {
    if (result.totalPages <= 1) return;
    const parts = [];
    for (let i = 0; i < result.totalPages; i++) {
      parts.push(
        `<button class="btn ${i === result.number ? "" : "btn-secondary"} btn-sm" data-page="${i}">${i + 1}</button>`
      );
    }
    pagination.innerHTML = parts.join("");
    pagination.querySelectorAll("button").forEach((btn) => {
      btn.addEventListener("click", () => {
        page = Number(btn.dataset.page);
        load();
        window.scrollTo({ top: 0, behavior: "smooth" });
      });
    });
  }

  form.addEventListener("submit", (e) => {
    e.preventDefault();
    page = 0;
    load();
  });

  load();
})();
