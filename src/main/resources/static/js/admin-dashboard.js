async function loadDashboardStats() {
  const response = await fetch("/admin/appointments-stat"); // Change to your endpoint
  const appointments = await response.json();

  const totalAppointments = appointments.length;
  const confirmedAppointments = appointments.filter(
    (a) => a.status === "CONFIRMED"
  ).length;
  const canceledAppointments = appointments.filter(
    (a) => a.status === "CANCELED"
  ).length;

  const uniquePatients = new Set(appointments.map((a) => a.patient.id)).size;

  updateDashboardCounts({
    totalAppointments,
    confirmedAppointments,
    uniquePatients,
    canceledAppointments,
  });

  document.querySelector(".appointment-count-container .count-value").textContent = totalAppointments;
  console.log(document.querySelector(".appointment-count-container .count-value"));
}
function updateDashboardCounts({
  totalAppointments,
  confirmedAppointments,
  uniquePatients,
  canceledAppointments,
}) {
  // Assuming order: Appointments, Confirmed, New Patients, Canceled
  const counts = document.querySelectorAll(".card .statics-count");

  if (counts.length >= 4) {
    counts[0].textContent = totalAppointments;
    counts[1].textContent = confirmedAppointments;
    counts[2].textContent = uniquePatients;
    counts[3].textContent = canceledAppointments;
  }
}
loadDashboardStats();

async function loadTopAppointments() {
  try {
    const response = await fetch("/admin/appointments/top5");
    const appointments = await response.json();

    const tbody = document.querySelector(".appointment-table tbody");
    tbody.innerHTML = "";

    if (appointments.length === 0) {
      tbody.innerHTML = `<tr><td colspan="5" class="text-center text-gray-400">No appointments found</td></tr>`;
      return;
    }

    appointments.forEach((app) => {
      const patientName = `${app.patient.firstName} ${app.patient.lastName}`;
      const doctorName = `${app.availability.doctor.firstName} ${app.availability.doctor.lastName}`;
      const date = app.appointmentTime.split("T")[0]; // yyyy-mm-dd
      const time = formatTime(app.appointmentTime.split("T")[1]); // HH:mm:ss → AM/PM
      const disease = app.appointmentInfo;

      const row = document.createElement("tr");
      row.innerHTML = `
          <td>${patientName}</td>
          <td>${doctorName}</td>
          <td>${date}</td>
          <td>${time}</td>
          <td>${disease}</td>
        `;
      tbody.appendChild(row);
    });
  } catch (err) {
    console.error("Failed to load top appointments:", err);
  }
}

function formatTime(timeStr) {
  const [hour, minute] = timeStr.split(":");
  const h = parseInt(hour);
  const suffix = h >= 12 ? "PM" : "AM";
  const formattedHour = h % 12 === 0 ? 12 : h % 12;
  return `${formattedHour}:${minute} ${suffix}`;
}

loadTopAppointments();

async function loadTopDoctors() {
  try {
    const response = await fetch("/admin/doctors/top5");
    const doctors = await response.json();

    const tbody = document.querySelector(".doctor-table tbody");
    tbody.innerHTML = "";

    if (doctors.length === 0) {
      tbody.innerHTML = `<tr><td colspan="2" class="text-center text-gray-400">No doctors found</td></tr>`;
      return;
    }

    doctors.forEach((doc) => {
      const fullName = `Dr. ${doc.firstName} ${doc.lastName}`;
      const specialty = doc.speciality || "General";

      const row = document.createElement("tr");
      row.innerHTML = `
          <td>${fullName}</td>
          <td>${specialty}</td>
        `;
      tbody.appendChild(row);
    });
  } catch (err) {
    console.error("Failed to load doctors:", err);
  }
}

loadTopDoctors();
  