function closeAppointmentModal() {
  document.getElementById("appointment-modal").style.display = "none";
}


const doctorCards = document.querySelectorAll(".doctor-card");

doctorCards.forEach((card) => {
  card.addEventListener("click", () => {
    const availabilityId = card.getAttribute("data-availability-id");
    console.log("Opening appointment modal for availability ID:", availabilityId);
    fetch(`/patient/availability/${availabilityId}`) // <-- adjust this endpoint
      .then((res) => res.json())
      .then((data) => {
        openAppointmentModal(data);
      })
      .catch((err) => {
        console.error("Failed to fetch availability:", err);
      });
  });
});


function openAppointmentModal(availability) {
  document.getElementById('custom-doctor-name').innerText =
    'Dr. ' + availability.doctor.firstName + ' ' + availability.doctor.lastName;

  document.getElementById('custom-doctor-speciality').innerText = availability.doctor.speciality;
  document.getElementById('custom-available-date').innerText = availability.availableDate;

  document.getElementById('custom-appointment-time').value = availability.startTime || "09:00";
  document.getElementById('custom-appointment-info').value = "";

  document.getElementById('custom-appointment-form')
    .setAttribute('data-availability-id', availability.availabilityId);

  document.getElementById('custom-appointment-modal').style.display = 'block';
}

function closeAppointmentModal() {
  document.getElementById('custom-appointment-modal').style.display = 'none';
}

document
  .getElementById("custom-appointment-form")
  .addEventListener("submit", function (e) {
    e.preventDefault();

    const availabilityId = this.getAttribute("data-availability-id");
    const appointmentInfo = document.getElementById(
      "custom-appointment-info"
    ).value;
    const timeValue = document.getElementById("custom-appointment-time").value;
    const dateValue = document.getElementById(
      "custom-available-date"
    ).innerText;

    
    const appointmentDateTime = `${dateValue}T${timeValue}`;

    const payload = {
      appointmentInfo: appointmentInfo,
      appointmentTime: appointmentDateTime,
      availability: { availabilityId: availabilityId },
    };

    console.log("Booking appointment with payload:", payload);

    fetch("/patient/appointment", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        [csrfHeader]: csrfToken,
      },
      body: JSON.stringify(payload),
    })
      .then((response) => {
        if (response.ok) {
          showToast("success", "Success!", "Appointment booked successfully");
          closeAppointmentModal();
        } else {
          return response.json().then((err) => {
            throw new Error(err.message || "Failed to book appointment");
          });
        }
      })
      .catch((error) => {
        showToast("error", "Error", err.message);
        alert(error.message);
      });
  });

  document.querySelector(".search-btn").addEventListener("click", () => {
    const name = document.querySelector(".input").value.trim();
    const startTime = document.getElementById("start-time").value;
    const endTime = document.getElementById("end-time").value;

    const queryParams = new URLSearchParams();
    if (name) queryParams.append("doctorName", name);
    if (startTime) queryParams.append("startTime", startTime);
    if (endTime) queryParams.append("endTime", endTime);

    console.log("Searching availabilities with params:", queryParams.toString());
    fetch(`/patient/availabilities/search?${queryParams.toString()}`)
      .then((res) => res.json())
      .then((data) => {
        renderDoctorCards(data);
      })
      .catch((err) => {
        console.error("Search failed:", err);
      });
  });

  function renderDoctorCards(data) {
    const container = document.querySelector(".doctor-card-container");
    container.innerHTML = "";

    data.forEach((av) => {
      const div = document.createElement("div");
      div.className = "doctor-card";
      div.setAttribute("data-availability-id", av.availabilityId);
      div.innerHTML = `
        <img class="doctor-image" src="${av.doctor.profilePath}" alt="Doctor Photo" />
        <div class="doctor-info">
          <div class="doctor-name">Dr. ${av.doctor.firstName} ${av.doctor.lastName}</div>
          <div class="doctor-speciality">${av.doctor.speciality}</div>
          <div class="availability-time">Available: ${av.startTime} - ${av.endTime}</div>
        </div>
      `;
      container.appendChild(div);
    });

    // ✅ Attach event listeners to the new cards
    const updatedCards = document.querySelectorAll(".doctor-card");
    updatedCards.forEach((card) => {
      card.addEventListener("click", () => {
        const availabilityId = card.getAttribute("data-availability-id");
        fetch(`/patient/availability/${availabilityId}`)
          .then((res) => res.json())
          .then((data) => {
            openAppointmentModal(data);
          })
          .catch((err) => {
            console.error("Failed to fetch availability:", err);
          });
      });
    });
  }

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