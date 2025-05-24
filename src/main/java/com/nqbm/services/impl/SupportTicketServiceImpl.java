/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.services.impl;

/**
 *
 * @author baominh14022004gmail.com
 */
import com.nqbm.pojo.SupportTicket;
import com.nqbm.pojo.User;
import com.nqbm.repositories.SupportTicketRepository;
import com.nqbm.services.SupportTicketService;
import com.nqbm.services.UserService;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SupportTicketServiceImpl implements SupportTicketService {
    
    @Autowired
    private SupportTicketRepository supportTicketRepository;
    
    @Autowired
    private UserService userService;
    
    @Override
    public List<SupportTicket> getSupportTickets(Map<String, String> params) {
        return this.supportTicketRepository.getSupportTickets(params);
    }
    
    @Override
    public SupportTicket getSupportTicketById(Long id) {
        return this.supportTicketRepository.getSupportTicketById(id);
    }
    
    @Override
    public SupportTicket getSupportTicketByCode(String ticketCode) {
        return this.supportTicketRepository.getSupportTicketByCode(ticketCode);
    }
    
    @Override
    public List<SupportTicket> getSupportTicketsByUser(Long userId) {
        return this.supportTicketRepository.getSupportTicketsByUser(userId);
    }
    
    @Override
    public boolean addSupportTicket(SupportTicket ticket) {
        return this.supportTicketRepository.addSupportTicket(ticket);
    }
    
    @Override
    public boolean updateSupportTicket(SupportTicket ticket) {
        return this.supportTicketRepository.updateSupportTicket(ticket);
    }
    
    @Override
    public boolean deleteSupportTicket(Long id) {
        return this.supportTicketRepository.deleteSupportTicket(id);
    }
    
    @Override
    public Long countSupportTickets() {
        return this.supportTicketRepository.countSupportTickets();
    }
    
    @Override
    public Long countTicketsByStatus(SupportTicket.TicketStatus status) {
        return this.supportTicketRepository.countTicketsByStatus(status);
    }
    
    @Override
    public boolean replyToTicket(Long ticketId, String adminReply, Long adminId) {
        try {
            SupportTicket ticket = this.supportTicketRepository.getSupportTicketById(ticketId);
            if (ticket != null) {
                ticket.setAdminReply(adminReply);
                ticket.setStatus(SupportTicket.TicketStatus.IN_PROGRESS);
                
                // Set assigned admin
                if (adminId != null) {
                    User admin = userService.getUserById(adminId);
                    ticket.setAssignedAdmin(admin);
                }
                
                return this.supportTicketRepository.updateSupportTicket(ticket);
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error replying to ticket: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getTicketStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            Long totalTickets = countSupportTickets();
            Long openTickets = countTicketsByStatus(SupportTicket.TicketStatus.OPEN);
            Long inProgressTickets = countTicketsByStatus(SupportTicket.TicketStatus.IN_PROGRESS);
            Long resolvedTickets = countTicketsByStatus(SupportTicket.TicketStatus.RESOLVED);
            Long closedTickets = countTicketsByStatus(SupportTicket.TicketStatus.CLOSED);
            
            stats.put("totalTickets", totalTickets);
            stats.put("openTickets", openTickets);
            stats.put("inProgressTickets", inProgressTickets);
            stats.put("resolvedTickets", resolvedTickets);
            stats.put("closedTickets", closedTickets);
            stats.put("pendingTickets", openTickets + inProgressTickets);
            
        } catch (Exception e) {
            System.err.println("Error getting ticket statistics: " + e.getMessage());
        }
        
        return stats;
    }
}
