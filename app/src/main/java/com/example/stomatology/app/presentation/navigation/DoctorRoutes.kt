package com.example.stomatology.app.presentation.navigation

object DoctorRoutes {
    const val Dashboard = "doctor_dashboard"
    const val Appointments = "doctor_appointments"
    const val AppointmentDetail = "doctor_appointment_detail/{appointmentId}"
    const val Profile = "doctor_profile"

    fun appointmentDetail(appointmentId: String): String {
        return "doctor_appointment_detail/$appointmentId"
    }
}