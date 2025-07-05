package com.cityStar.controller;

import java.time.LocalDate;
import java.util.List;

// import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cityStar.dto.AdminDTO;
import com.cityStar.dto.AppointmentDTO;
import com.cityStar.dto.UserDTO;
import com.cityStar.enums.Status;
import com.cityStar.model.Admin;
import com.cityStar.rowmapper.AdminRowMapper;
import com.cityStar.security.CustomUserDetails;
import com.cityStar.service.UserService;
import com.cityStar.service.AdminService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final AdminService adminService;
    public AdminController(UserService userService,
                           AdminService adminService) {
        this.userService = userService;
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public String Dashboard(@AuthenticationPrincipal CustomUserDetails user,
                            Model model) {
        AdminDTO admin = getAdmin(user);
        model.addAttribute("current_user", admin);
        return "admin/admin-dashboard";
    }

    @GetMapping("/profile")
    public String Profile(@AuthenticationPrincipal CustomUserDetails user,
                            Model model) {
        AdminDTO admin = getAdmin(user);
        AdminDTO adminProfile = getAdminProfile((Admin) userService.findByEmail(user.getUsername()));
        model.addAttribute("admin_profile", adminProfile);
        model.addAttribute("current_user", admin);
        return "admin/admin-profile";
    }

    @GetMapping("/users")
    public String Users(@AuthenticationPrincipal CustomUserDetails user,
                            Model model) {
        AdminDTO admin = getAdmin(user);
        model.addAttribute("current_user", admin);

        List<UserDTO> users = userService.findAllDto();
        model.addAttribute("users", users);
        return "admin/admin-users";
    }

    @GetMapping("/appointments")
    public String Appointments(@AuthenticationPrincipal CustomUserDetails user,
                                Model model) {
        AdminDTO admin = getAdmin(user);
        model.addAttribute("current_user", admin);
        return "admin/admin-appointments";
    }

    @GetMapping("/appointments/by-date")
    @ResponseBody
    public List<AppointmentDTO> getAppointmentsByDate(
            @RequestParam(required = false) Status status,
            @RequestParam LocalDate date
    ) {
        if (status != null) {
            return adminService.getAppointmentsByStatusForDate(status, date);
        }
        return adminService.getAppointmentsByDate(date);
    }

    private AdminDTO getAdmin(CustomUserDetails user){
        Admin admin = (Admin) userService.findByEmail(user.getUsername());
        return new AdminDTO(admin.getFirstName(), admin.getLastName(), admin.getProfilePath());
    }

    private AdminDTO getAdminProfile(Admin admin){
        return AdminRowMapper.toDtoWithoutPassword(admin);
    }
}
