document.addEventListener("DOMContentLoaded", () => {
  const tableBody = document.querySelector(".appointment-table-body");

  function fetchAppointments(status = "") {
    const url = "/admin/all/appointments";
    fetch(url)
      .then((response) => response.json())
      .then((data) => renderTable(data, status));
  }


  function renderTable(appointments, status) {
    tableBody.innerHTML = ""; // clear old rows
    appointments.forEach((a) => {
      const tr = document.createElement("div");
      tr.className = "appointment-row";
      const id = `#${a.appointmentId.toString().padStart(3, "0")}`;
      const time = new Date(a.appointmentTime).toLocaleString();
      const isEditable = a.status.toLowerCase() === "pending";
      tr.innerHTML = `
                <div>${id}</div>
                <div><img class="appointment-img" src="${
                  a.patient.profilePath
                }" alt="image"></div>
                <div>${a.patient.firstName} ${a.patient.lastName}</div>
                <div>${a.patient.age}</div>
                <div>${time}</div>
                <div>${a.appointmentInfo}</div>
                <div><span class="status-badge ${a.status.toLowerCase()}">${
        a.status
      }</span></div>
                <div>
                    ${
                      isEditable
                        ? `
                        <button class="action-button accept" data-id="${a.appointmentId}">Accept</button>
                        <button class="action-button decline" data-id="${a.appointmentId}">Decline</button>
                    `
                        : `
                        <button class="action-button accept" disabled>Accept</button>
                        <button class="action-button decline" disabled>Decline</button>
                    `
                    }
                </div>
            `;
      tableBody.appendChild(tr);
      tr.querySelectorAll(".action-button").forEach((btn) => {
        btn.addEventListener("click", () => {
          const appointmentId = btn.dataset.id;
          const newStatus = btn.classList.contains("accept")
            ? "Confirmed"
            : "Cancelled";

          fetch(`/admin/update-status/${appointmentId}?status=${newStatus}`, {
            method: "PATCH",
            headers: {
              [csrfHeader]: csrfToken,
            },
          })
            .then((res) => {
              if (res.ok) {
                fetchAppointments(status); // reload tabl
                showToast("success", "Success!", "Appointment status updated.");
              }
            })
            .catch((err) =>
              showToast(
                "error",
                "Error",
                err.message || "Something went wrong!"
              )
            );
        });
      });
    });
  }

  fetchAppointments();
});
