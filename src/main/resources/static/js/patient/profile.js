const cameraIcon = document.querySelector(".camera-icon");
const fileInput = document.getElementById("profile-image-input");
const profileImage = document.querySelector(".main-profile-image");
const updateBtn = document.querySelector(".main-profile-update-button");

const inputs = document.querySelectorAll(
  ".main-image-profile-info input"
);

let originalImageSrc = profileImage.src;
const originalValues = {};
inputs.forEach((input) => {
  originalValues[input.id] = input.getAttribute("data-original");
});

cameraIcon.addEventListener("click", () => {
  fileInput.click();
});

inputs.forEach((input) => {
  input.addEventListener("input", checkForChanges);
});

fileInput.addEventListener("change", (event) => {
  const file = event.target.files[0];
  if (file && file.type.startsWith("image/")) {
    const reader = new FileReader();
    reader.onload = function (e) {
      profileImage.src = e.target.result;
      checkForChanges();
    };
    reader.readAsDataURL(file);
  }
});

function checkForChanges() {
  let hasChanged = false;

  inputs.forEach((input) => {
    if (input.value !== originalValues[input.id]) {
      hasChanged = true;
    }
  });

  if (profileImage.src !== originalImageSrc) {
    hasChanged = true;
  }

  updateBtn.style.display = hasChanged ? "block" : "none";
}

updateBtn.addEventListener("click", () => {
  const formData = new FormData();
  const changedData = {};

  inputs.forEach((input) => {
    if (input.value !== originalValues[input.id]) {
      changedData[input.name] = input.value;
    }
  });

  if (Object.keys(changedData).length > 0) {
    formData.append(
      "patient",
      new Blob([JSON.stringify(changedData)], { type: "application/json" })
    );
  }

  let imageChanged = false;
  if (fileInput.files.length > 0 && profileImage.src !== originalImageSrc) {
    formData.append("file", fileInput.files[0]);
    imageChanged = true;
  }

  fetch("/patient/profile", {
    method: "PATCH",
    headers: {
      [csrfHeader]: csrfToken,
    },
    body: formData,
  })
    .then((response) => {
      if (!response.ok) throw new Error("Failed to update profile");
      return response.text();
    })
    .then((message) => {
      showToast("success", "Success!", message);
      updateBtn.style.display = "none";

      // Update stored original values
      inputs.forEach((input) => {
        originalValues[input.id] = input.value;
        input.setAttribute("data-original", input.value);
      });

      originalImageSrc = profileImage.src;
      fileInput.value = "";

      // Inform the user
      setTimeout(() => {
        showToast("info", "Note", "Updating info, please wait...");
      }, 1000);

      // Update visible name/email
      setTimeout(() => {
        const fullName = `${
          changedData.firstName || originalValues["first-name"]
        } ${changedData.lastName || originalValues["last-name"] || ""}`.trim();
        document.querySelector(".main-profile-name-text").textContent =
          fullName;

        if (changedData.email) {
          document.querySelector(".main-profile-email").textContent =
            changedData.email;
        }

        if (imageChanged) {
          const imageElement = document.querySelector(".main-profile-image");
          if (imageElement) {
            imageElement.src = originalImageSrc;
          }
        }

        const navName = document.querySelector(".nav-profile-name");
        if (navName) {
          navName.textContent = fullName;
        }

        const navImage = document.querySelector(
          ".nav-profile-image-container img"
        );
        if (navImage && imageChanged) {
          navImage.src = originalImageSrc;
        }

        showToast("success", "Success!", "Profile Updated");
        imageChanged = false;
      }, 3000);
    })
    .catch((err) => {
      console.error("Update error:", err);
      showToast("error", "Error", err);
    });
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
            <div class="time">${timeAgo(n.createdAt)}</div>
          `;

          li.addEventListener("click", () => {
            if (!n.isRead) {
              markAsRead(n.notificationId, li);
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