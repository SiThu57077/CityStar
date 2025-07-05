document.querySelectorAll(".nav-link").forEach((item) => {
  item.addEventListener("click", function () {
    const link = this.querySelector(".link");
    if (link) {
      window.location.href = link.href;
    }
  });
});

const profileImage = document.querySelector(".profile-image .img");
if (profileImage) {
  profileImage.addEventListener("click", function () {
    window.location.href = "/admin/profile";
  });
}

const toggler = document.querySelector(".toggler-container");
if (toggler) {
  toggler.addEventListener("click", function () {
    const aside = document.querySelector(".aside");
    if (aside) {
      aside.classList.toggle("collapsed");
    }
  });
}

function confirmLogout(event) {
  event.preventDefault();
  document.getElementById("logout-modal").style.display = "block";
}

function closeModal() {
  document.getElementById("logout-modal").style.display = "none";
}

function proceedLogout() {
  document.getElementById("loading-spinner").style.display = "block";
  document.getElementById("confirm-btn").disabled = true;

  setTimeout(() => {
    document.getElementById("logout-form").submit();
  }, 1000); 
}

const sessionTimeout = 10 * 60 * 1000;
const warningBefore = 1 * 60 * 1000; 
const warningTime = sessionTimeout - warningBefore;

let warningTimer, expireTimer;

function showModal(id) {
  document.getElementById(id).style.display = "block";
}

function logout(){
  window.location.href = "/auth/login?timeout";
}
function continueSession(){
  document.getElementById("session-warning-modal").style.display = "none";
  window.location.reload();
  startSessionCountdown();
}
function startSessionCountdown() {
  clearTimeout(warningTimer);
  clearTimeout(expireTimer);

  warningTimer = setTimeout(() => {
    showModal("session-warning-modal");
  }, warningTime);

  expireTimer = setTimeout(() => {
    document.getElementById("session-warning-modal").style.display = "none";
    showModal("session-expired-modal");
  }, sessionTimeout);
}

["click", "keydown", "scroll"].forEach((event) => {
  document.addEventListener(event, () => {
    startSessionCountdown();
  });
});

window.onload = startSessionCountdown;

const bell = document.getElementById("bellToggle");
const dropdown = document.getElementById("notificationDropdown");

bell.addEventListener("click", () => {
  console.log("Bell icon clicked");
  dropdown.classList.toggle("hidden");
});

document.addEventListener("click", (e) => {
  if (!bell.contains(e.target) && !dropdown.contains(e.target)) {
    dropdown.classList.add("hidden");
  }
});

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

  function loadNotifications() {
    fetch("/admin/notifications")
      .then((res) => res.json())
      .then((notifications) => {
        list.innerHTML = "";
        notifications.forEach((n) => {
          const li = document.createElement("li");
          li.innerHTML = `
            <div class="title">${n.message}</div>
            <div class="time">${timeAgo(n.createdAt)}</div>
          `;
          list.appendChild(li);
          console.log("Notification loaded:", n.message);
        });
      });
  }

  bell.addEventListener("click", () => {
    loadNotifications();
  });
});