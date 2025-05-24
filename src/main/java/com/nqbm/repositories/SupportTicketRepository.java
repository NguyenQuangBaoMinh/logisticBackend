/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.repositories;

/**
 *
 * @author baominh14022004gmail.com
 */
import com.nqbm.pojo.SupportTicket;
import java.util.List;
import java.util.Map;

public interface SupportTicketRepository {
    List<SupportTicket> getSupportTickets(Map<String, String> params);
    SupportTicket getSupportTicketById(Long id);
    SupportTicket getSupportTicketByCode(String ticketCode);
    List<SupportTicket> getSupportTicketsByUser(Long userId);
    boolean addSupportTicket(SupportTicket ticket);
    boolean updateSupportTicket(SupportTicket ticket);
    boolean deleteSupportTicket(Long id);
    Long countSupportTickets();
    Long countTicketsByStatus(SupportTicket.TicketStatus status);
}
