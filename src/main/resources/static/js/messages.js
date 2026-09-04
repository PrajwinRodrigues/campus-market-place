(function () {
  CM.renderNavbar("messages");
  if (!CM.requireAuth()) return;

  const conversationList = document.getElementById("conversationList");
  const threadHeader = document.getElementById("threadHeader");
  const threadMessages = document.getElementById("threadMessages");
  const composerForm = document.getElementById("composerForm");
  const composerInput = document.getElementById("composerInput");
  const alertBox = document.getElementById("alert");

  const me = Api.getUser();
  let conversations = [];
  let active = null; // { listingId, otherUserId, listingTitle, otherUserName }

  function convKey(c) {
    return c.listingId + ":" + c.otherUserId;
  }

  function conversationRowHtml(c) {
    const isActive = active && convKey(c) === convKey(active);
    return `
      <a class="conversation-item ${isActive ? "active" : ""}" data-listing-id="${c.listingId}" data-other-id="${c.otherUserId}">
        <div class="conv-title">${CM.escapeHtml(c.otherUserName)} ${c.unreadCount > 0 ? '<span class="unread-dot"></span>' : ""}</div>
        <div class="listing-meta">${CM.escapeHtml(c.listingTitle)}</div>
        <div class="conv-preview">${CM.escapeHtml(c.lastMessage)}</div>
      </a>`;
  }

  async function loadConversations() {
    try {
      conversations = await Api.myConversations();
      renderConversationList();

      const qListingId = CM.qs("listingId");
      const qOtherId = CM.qs("otherUserId");
      if (qListingId && qOtherId) {
        openConversation(Number(qListingId), Number(qOtherId));
      } else if (conversations.length > 0 && !active) {
        openConversation(conversations[0].listingId, conversations[0].otherUserId);
      }
    } catch (err) {
      CM.showAlert(alertBox, "Couldn't load conversations: " + err.message);
    }
  }

  function renderConversationList() {
    if (conversations.length === 0) {
      conversationList.innerHTML = `<div class="empty-state">No conversations yet. Message a seller from a listing page to start one.</div>`;
      return;
    }
    conversationList.innerHTML = conversations.map(conversationRowHtml).join("");
    conversationList.querySelectorAll(".conversation-item").forEach((el) => {
      el.addEventListener("click", () => {
        openConversation(Number(el.dataset.listingId), Number(el.dataset.otherId));
      });
    });
  }

  async function openConversation(listingId, otherUserId) {
    const conv = conversations.find((c) => c.listingId === listingId && c.otherUserId === otherUserId);
    active = conv || { listingId, otherUserId, listingTitle: "", otherUserName: "" };
    renderConversationList();

    threadHeader.textContent = active.otherUserName
      ? `${active.otherUserName} · ${active.listingTitle}`
      : "Conversation";
    threadMessages.innerHTML = `<div class="spinner-row">Loading…</div>`;
    composerForm.hidden = false;

    try {
      const msgs = await Api.getConversation(listingId, otherUserId);
      renderMessages(msgs);
      await Api.markConversationRead(listingId, otherUserId);
      if (conv) conv.unreadCount = 0;
      renderConversationList();
    } catch (err) {
      threadMessages.innerHTML = `<div class="empty-state">${CM.escapeHtml(err.message)}</div>`;
    }
  }

  function renderMessages(msgs) {
    if (msgs.length === 0) {
      threadMessages.innerHTML = `<div class="empty-state">No messages yet — say hi!</div>`;
      return;
    }
    threadMessages.innerHTML = msgs
      .map((m) => {
        const mine = me && m.senderId === me.id;
        return `
          <div class="msg-bubble ${mine ? "msg-mine" : "msg-theirs"}">
            <div>${CM.escapeHtml(m.content)}</div>
            <div class="msg-time">${CM.formatDate(m.sentAt)}</div>
          </div>`;
      })
      .join("");
    threadMessages.scrollTop = threadMessages.scrollHeight;
  }

  composerForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const content = composerInput.value.trim();
    if (!content || !active) return;
    try {
      await Api.sendMessage({ listingId: active.listingId, receiverId: active.otherUserId, content });
      composerInput.value = "";
      const msgs = await Api.getConversation(active.listingId, active.otherUserId);
      renderMessages(msgs);
      await loadConversations();
    } catch (err) {
      CM.showAlert(alertBox, err.message);
    }
  });

  loadConversations();
})();
