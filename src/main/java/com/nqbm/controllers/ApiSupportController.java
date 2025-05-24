/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.controllers;

/**
 *
 * @author baominh14022004gmail.com
 */
import com.nqbm.pojo.SupportTicket;
import com.nqbm.pojo.User;
import com.nqbm.services.SupportTicketService;
import com.nqbm.services.UserService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/support")
@CrossOrigin(origins = "*")
@Transactional
public class ApiSupportController {

    @Autowired
    private SupportTicketService supportTicketService;

    @Autowired
    private UserService userService;

    /**
     * 1. Tạo yêu cầu hỗ trợ - POST /support
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSupportTicket(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validation
            String title = (String) requestData.get("title");
            String message = (String) requestData.get("message");
            Object userIdObj = requestData.get("userId");

            if (title == null || title.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Tiêu đề không được để trống");
                return ResponseEntity.badRequest().body(response);
            }

            if (message == null || message.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Nội dung không được để trống");
                return ResponseEntity.badRequest().body(response);
            }

            if (userIdObj == null) {
                response.put("success", false);
                response.put("message", "Không xác định được người dùng");
                return ResponseEntity.badRequest().body(response);
            }

            Long userId = Long.parseLong(userIdObj.toString());
            User user = userService.getUserById(userId);
            if (user == null) {
                response.put("success", false);
                response.put("message", "Người dùng không tồn tại");
                return ResponseEntity.badRequest().body(response);
            }

            // Tạo support ticket
            SupportTicket ticket = new SupportTicket();
            ticket.setTitle(title.trim());
            ticket.setMessage(message.trim());
            ticket.setUser(user);
            ticket.setStatus(SupportTicket.TicketStatus.OPEN);

            boolean created = supportTicketService.addSupportTicket(ticket);

            if (created) {
                response.put("success", true);
                response.put("message", "Tạo yêu cầu hỗ trợ thành công");
                response.put("ticketCode", ticket.getTicketCode());
                response.put("ticket", createTicketResponse(ticket));
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                response.put("success", false);
                response.put("message", "Có lỗi xảy ra khi tạo yêu cầu hỗ trợ");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (Exception e) {
            System.err.println("Error creating support ticket: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 2. Lấy yêu cầu theo người dùng - GET /support/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getSupportTicketsByUser(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<SupportTicket> tickets = supportTicketService.getSupportTicketsByUser(userId);

            response.put("success", true);
            response.put("tickets", tickets.stream().map(this::createTicketResponse).toList());
            response.put("totalCount", tickets.size());
            response.put("message", "Lấy danh sách yêu cầu hỗ trợ thành công");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi lấy danh sách yêu cầu hỗ trợ: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 3. Admin xem tất cả - GET /support/all
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllSupportTickets(@RequestParam Map<String, String> params) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<SupportTicket> tickets = supportTicketService.getSupportTickets(params);
            Long totalCount = supportTicketService.countSupportTickets();

            response.put("success", true);
            response.put("tickets", tickets.stream().map(this::createTicketResponse).toList());
            response.put("totalCount", totalCount);
            response.put("currentPage", params.get("page") != null ? Integer.parseInt(params.get("page")) : 1);
            response.put("message", "Lấy danh sách yêu cầu hỗ trợ thành công");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi lấy danh sách yêu cầu hỗ trợ: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 4. Admin phản hồi - PUT /support/{id}/reply
     */
    @PutMapping("/{id}/reply")
    public ResponseEntity<Map<String, Object>> replyToSupportTicket(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestData) {

        Map<String, Object> response = new HashMap<>();

        try {
            String adminReply = (String) requestData.get("adminReply");
            Object adminIdObj = requestData.get("adminId");

            if (adminReply == null || adminReply.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Nội dung phản hồi không được để trống");
                return ResponseEntity.badRequest().body(response);
            }

            Long adminId = null;
            if (adminIdObj != null) {
                adminId = Long.parseLong(adminIdObj.toString());
            }

            boolean updated = supportTicketService.replyToTicket(id, adminReply.trim(), adminId);

            if (updated) {
                SupportTicket updatedTicket = supportTicketService.getSupportTicketById(id);

                response.put("success", true);
                response.put("message", "Phản hồi yêu cầu hỗ trợ thành công");
                response.put("ticket", createTicketResponse(updatedTicket));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy yêu cầu hỗ trợ hoặc có lỗi xảy ra");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 5. Lấy thống kê - GET /support/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSupportStatistics() {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> stats = supportTicketService.getTicketStatistics();

            response.put("success", true);
            response.put("statistics", stats);
            response.put("message", "Lấy thống kê thành công");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi lấy thống kê: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 6. Lấy ticket theo ID - GET /support/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getSupportTicketById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            SupportTicket ticket = supportTicketService.getSupportTicketById(id);

            if (ticket != null) {
                response.put("success", true);
                response.put("ticket", createTicketResponse(ticket));
                response.put("message", "Lấy thông tin yêu cầu hỗ trợ thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy yêu cầu hỗ trợ");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi lấy thông tin yêu cầu hỗ trợ: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 7. Đóng ticket - PUT /support/{id}/close
     */
    @PutMapping("/{id}/close")
    public ResponseEntity<Map<String, Object>> closeSupportTicket(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            SupportTicket ticket = supportTicketService.getSupportTicketById(id);

            if (ticket != null) {
                ticket.setStatus(SupportTicket.TicketStatus.CLOSED);
                boolean updated = supportTicketService.updateSupportTicket(ticket);

                if (updated) {
                    response.put("success", true);
                    response.put("message", "Đóng yêu cầu hỗ trợ thành công");
                    response.put("ticket", createTicketResponse(ticket));
                    return ResponseEntity.ok(response);
                } else {
                    response.put("success", false);
                    response.put("message", "Có lỗi xảy ra khi đóng yêu cầu hỗ trợ");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy yêu cầu hỗ trợ");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Helper method to create ticket response
    private Map<String, Object> createTicketResponse(SupportTicket ticket) {
        Map<String, Object> ticketData = new HashMap<>();
        ticketData.put("id", ticket.getId());
        ticketData.put("ticketCode", ticket.getTicketCode());
        ticketData.put("title", ticket.getTitle());
        ticketData.put("message", ticket.getMessage());
        ticketData.put("status", ticket.getStatus().toString());
        ticketData.put("adminReply", ticket.getAdminReply());
        ticketData.put("createdAt", ticket.getCreatedAt());
        ticketData.put("updatedAt", ticket.getUpdatedAt());
        ticketData.put("resolvedAt", ticket.getResolvedAt());

        if (ticket.getUser() != null) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", ticket.getUser().getId());
            userData.put("username", ticket.getUser().getUsername());
            
            userData.put("displayName", ticket.getUser().getDisplayName());
            userData.put("email", ticket.getUser().getEmail());
            ticketData.put("user", userData);
        }

        if (ticket.getAssignedAdmin() != null) {
            Map<String, Object> adminData = new HashMap<>();
            adminData.put("id", ticket.getAssignedAdmin().getId());
            adminData.put("username", ticket.getAssignedAdmin().getUsername());
            
            adminData.put("displayName", ticket.getAssignedAdmin().getDisplayName());
            adminData.put("email", ticket.getAssignedAdmin().getEmail());
            ticketData.put("assignedAdmin", adminData);
        }

        return ticketData;
    }
}
