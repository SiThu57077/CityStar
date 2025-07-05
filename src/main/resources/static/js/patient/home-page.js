document.addEventListener("DOMContentLoaded", () => {
  const bell = document.querySelector(".bell-icon-container");
  const list = document.getElementById("notificationList");

  function timeAgo(dateString) {
    const now = new Date();
    const then = new Date(dateString);
    const diff = (now - then) / 1000; // seconds
    if (diff < 60) return "Just now";
    if (diff < 3600) return `${Math.floor(diff / 60)} min ago`;
    if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`;
    return then.toLocaleDateString("en-US", {
      day: "2-digit",
      month: "short",
      year: "numeric",
    });
  }

  function markAsRead(notificationId, liElement) {
    fetch(`/patient/notifications/${notificationId}`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        [csrfHeader]: csrfToken,
      },
    })
      .then((res) => {
        if (!res.ok) throw new Error("Failed to mark as read");
        liElement.classList.remove("unread");
        console.log(`Notification ${notificationId} marked as read.`);
      })
      .catch((err) => console.error(err.message));
  }

  function loadNotifications() {
    fetch("/patient/notifications")
      .then((res) => res.json())
      .then((notifications) => {
        list.innerHTML = "";
        notifications.forEach((n) => {
          const li = document.createElement("li");
          li.setAttribute("data-id", n.notificationId);
          li.className = n.read ? "" : "unread";
          li.innerHTML = `
              <div class="title">${n.message}</div>
              <div class="timet">${timeAgo(n.createdAt)}</div>
            `;

          li.addEventListener("click", () => {
            if (!n.isRead) {
              markAsRead(n.notificationId, li);
              li.className = "read"; 
            }
          });

          list.appendChild(li);
        });
      });
  }

  bell.addEventListener("click", () => {
    loadNotifications();
  });
});
