// Thin fetch wrapper around the Campus Marketplace REST API.
// Loaded as a plain script (no bundler), so everything hangs off `window.Api`.
(function () {
  const TOKEN_KEY = "cm_token";
  const USER_KEY = "cm_user";

  function getToken() {
    return localStorage.getItem(TOKEN_KEY);
  }

  function getUser() {
    try {
      const raw = localStorage.getItem(USER_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch (e) {
      return null;
    }
  }

  function setSession(authResponse) {
    localStorage.setItem(TOKEN_KEY, authResponse.token);
    localStorage.setItem(
      USER_KEY,
      JSON.stringify({
        id: authResponse.userId,
        fullName: authResponse.fullName,
        email: authResponse.email,
        role: authResponse.role,
      })
    );
  }

  function clearSession() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }

  function isLoggedIn() {
    return !!getToken();
  }

  /**
   * @param {string} path e.g. "/api/listings"
   * @param {object} opts { method, body, isForm, auth }
   *   - body: plain object (JSON-encoded) or a FormData instance when isForm=true
   *   - auth: send the bearer token if logged in (default true)
   */
  async function request(path, opts = {}) {
    const { method = "GET", body, isForm = false, auth = true } = opts;
    const headers = {};
    if (auth && getToken()) {
      headers["Authorization"] = "Bearer " + getToken();
    }

    let fetchBody;
    if (body !== undefined) {
      if (isForm) {
        fetchBody = body; // FormData sets its own Content-Type w/ boundary
      } else {
        headers["Content-Type"] = "application/json";
        fetchBody = JSON.stringify(body);
      }
    }

    const res = await fetch(path, { method, headers, body: fetchBody });

    if (res.status === 204) {
      return null;
    }

    let data = null;
    const text = await res.text();
    if (text) {
      try {
        data = JSON.parse(text);
      } catch (e) {
        // non-JSON response; leave data null
      }
    }

    if (!res.ok) {
      if (res.status === 401 && auth) {
        clearSession();
      }
      const message = (data && data.message) || res.statusText || "Request failed";
      const err = new Error(message);
      err.status = res.status;
      err.fieldErrors = data && data.fieldErrors;
      throw err;
    }

    return data;
  }

  window.Api = {
    // auth
    getToken,
    getUser,
    setSession,
    clearSession,
    isLoggedIn,
    request,

    // auth endpoints
    register: (payload) => request("/api/auth/register", { method: "POST", body: payload, auth: false }),
    login: (payload) => request("/api/auth/login", { method: "POST", body: payload, auth: false }),

    // user
    me: () => request("/api/users/me"),
    updateMe: (payload) => request("/api/users/me", { method: "PUT", body: payload }),

    // listings
    searchListings: (params) => {
      const qs = new URLSearchParams();
      Object.entries(params || {}).forEach(([k, v]) => {
        if (v !== undefined && v !== null && v !== "") qs.set(k, v);
      });
      const suffix = qs.toString() ? "?" + qs.toString() : "";
      return request("/api/listings" + suffix, { auth: false });
    },
    getListing: (id) => request("/api/listings/" + id, { auth: false }),
    myListings: (params) => {
      const qs = new URLSearchParams(params || {});
      const suffix = qs.toString() ? "?" + qs.toString() : "";
      return request("/api/listings/mine" + suffix);
    },
    createListing: (payload) => request("/api/listings", { method: "POST", body: payload }),
    updateListing: (id, payload) => request("/api/listings/" + id, { method: "PUT", body: payload }),
    updateListingStatus: (id, status) =>
      request("/api/listings/" + id + "/status", { method: "PATCH", body: { status } }),
    deleteListing: (id) => request("/api/listings/" + id, { method: "DELETE" }),
    uploadListingImage: (id, file) => {
      const form = new FormData();
      form.append("file", file);
      return request("/api/listings/" + id + "/images", { method: "POST", body: form, isForm: true });
    },

    // favorites
    addFavorite: (listingId) => request("/api/favorites/" + listingId, { method: "POST" }),
    removeFavorite: (listingId) => request("/api/favorites/" + listingId, { method: "DELETE" }),
    myFavorites: () => request("/api/favorites"),

    // messages
    sendMessage: (payload) => request("/api/messages", { method: "POST", body: payload }),
    myConversations: () => request("/api/messages/conversations"),
    getConversation: (listingId, otherUserId) =>
      request(`/api/messages/listing/${listingId}/user/${otherUserId}`),
    markConversationRead: (listingId, otherUserId) =>
      request(`/api/messages/listing/${listingId}/user/${otherUserId}/read`, { method: "PATCH" }),
  };
})();
