document.querySelectorAll('.header-item').forEach(item => {
    item.addEventListener('click', event => {
        const target = event.target;
        if (target.classList.contains('active')) {
            return;
        }
        document.querySelectorAll('.header-item').forEach(i => i.classList.remove('active'));
        target.classList.add('active');
    });
})

document.addEventListener("DOMContentLoaded", () => {
  const tableBody = document.querySelector(".patient-table tbody");
  const dateInput = document.getElementById("calendar");
  const headerItems = document.querySelectorAll(".header-item");

  const today = new Date().toISOString().split("T")[0]; // yyyy-MM-dd
  dateInput.value = today;

  function fetchAppointments(date, status = null) {
    const params = new URLSearchParams();
    params.append("date", date); // Must be yyyy-MM-dd
    if (status && status !== "All") {
      params.append("status", status);
    }

    fetch(`/admin/appointments/by-date?${params.toString()}`)
      .then((res) => res.json())
      .then((appointments) => renderTable(appointments))
      .catch((err) => console.error("Failed to fetch appointments:", err));
  }

  function renderTable(appointments) {
    tableBody.innerHTML = "";
    appointments.forEach((a, index) => {
      const time = new Date(a.appointmentTime).toLocaleTimeString([], {
        hour: "2-digit",
        minute: "2-digit",
      });

      const row = document.createElement("tr");
      row.innerHTML = `
        <td>${index + 1}</td>
        <td class="patient-info">
          <img class="patient-img" src="${a.patient.profilePath}" />
          <div>
            ${a.patient.firstName} ${a.patient.lastName}<br>
            <small>${a.patient.age} yr</small>
          </div>
        </td>
        <td>${a.patient.phoneNumber ?? "-"}</td>
        <td>${time}</td>
        <td>${a.availability.doctor.firstName} ${
        a.availability.doctor.lastName
      }</td>
        <td><span class="status ${a.status}">${a.status}</span></td>
      `;
      tableBody.appendChild(row);
    });
  }

  headerItems.forEach((item) => {
    item.addEventListener("click", () => {
      document.querySelector(".header-item.active").classList.remove("active");
      item.classList.add("active");

      const input = document.getElementById("calendar");
      const selectedDate = getSafeLocalDate(input); 
      const status = item.textContent.trim();

      console.log("Selected Date:", input.value);
      console.log("Normalized Selected Date:", selectedDate);
      fetchAppointments(selectedDate, status);
    });
  });

  document.getElementById("calendar").addEventListener("change", () => {
    const input = document.getElementById("calendar");
    const formattedDate = getSafeLocalDate(input); 
    const selectedStatus = document
      .querySelector(".header-item.active")
      .textContent.trim();

    console.log("Selected Date:", input.value);
    console.log("Normalized Selected Date:", formattedDate);
    fetchAppointments(formattedDate, selectedStatus);
  });

  function getSafeLocalDate(input) {
    if (!input.value) return null; 
    const [year, month, day] = input.value.split("-");
    return `${year}-${month}-${day}`;
  }

  fetchAppointments(today, "All");
});
