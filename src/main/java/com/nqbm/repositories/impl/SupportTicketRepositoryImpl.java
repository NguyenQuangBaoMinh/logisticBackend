/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.repositories.impl;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.SupportTicket;
import com.nqbm.repositories.SupportTicketRepository;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class SupportTicketRepositoryImpl implements SupportTicketRepository {
    
    @Autowired
    private SessionFactory sessionFactory;
    
    @Override
    public List<SupportTicket> getSupportTickets(Map<String, String> params) {
        Session session = this.sessionFactory.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<SupportTicket> query = builder.createQuery(SupportTicket.class);
        Root<SupportTicket> root = query.from(SupportTicket.class);
        query.select(root);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // Filter by status
        if (params != null && params.containsKey("status")) {
            String status = params.get("status");
            if (status != null && !status.isEmpty()) {
                predicates.add(builder.equal(root.get("status"), SupportTicket.TicketStatus.valueOf(status)));
            }
        }
        
        // Filter by user
        if (params != null && params.containsKey("userId")) {
            String userId = params.get("userId");
            if (userId != null && !userId.isEmpty()) {
                predicates.add(builder.equal(root.get("user").get("id"), Long.parseLong(userId)));
            }
        }
        
        // Search by title or ticket code
        if (params != null && params.containsKey("search")) {
            String search = params.get("search");
            if (search != null && !search.isEmpty()) {
                Predicate titlePredicate = builder.like(root.get("title"), "%" + search + "%");
                Predicate codePredicate = builder.like(root.get("ticketCode"), "%" + search + "%");
                predicates.add(builder.or(titlePredicate, codePredicate));
            }
        }
        
        // Sort by created date (newest first)
        query.orderBy(builder.desc(root.get("createdAt")));
        
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(Predicate[]::new));
        }
        
        // Pagination
        Query<SupportTicket> q = session.createQuery(query);
        
        if (params != null) {
            String page = params.get("page");
            if (page != null && !page.isEmpty()) {
                int pageNumber = Integer.parseInt(page);
                int pageSize = 10;
                
                q.setFirstResult((pageNumber - 1) * pageSize);
                q.setMaxResults(pageSize);
            }
        }
        
        return q.getResultList();
    }
    
    @Override
    public SupportTicket getSupportTicketById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            return session.get(SupportTicket.class, id);
        } catch (NoResultException ex) {
            return null;
        }
    }
    
    @Override
    public SupportTicket getSupportTicketByCode(String ticketCode) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            String hql = "FROM SupportTicket st WHERE st.ticketCode = :ticketCode";
            Query<SupportTicket> query = session.createQuery(hql, SupportTicket.class);
            query.setParameter("ticketCode", ticketCode);
            return query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }
    
    @Override
    public List<SupportTicket> getSupportTicketsByUser(Long userId) {
        Session session = this.sessionFactory.getCurrentSession();
        String hql = "FROM SupportTicket st WHERE st.user.id = :userId ORDER BY st.createdAt DESC";
        Query<SupportTicket> query = session.createQuery(hql, SupportTicket.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }
    
    @Override
    public boolean addSupportTicket(SupportTicket ticket) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            session.persist(ticket);
            return true;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean updateSupportTicket(SupportTicket ticket) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            session.merge(ticket);
            return true;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean deleteSupportTicket(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            SupportTicket ticket = session.get(SupportTicket.class, id);
            if (ticket != null) {
                session.remove(ticket);
                return true;
            }
            return false;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
    
    @Override
    public Long countSupportTickets() {
        Session session = this.sessionFactory.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<SupportTicket> root = query.from(SupportTicket.class);
        
        query.select(builder.count(root));
        
        return session.createQuery(query).getSingleResult();
    }
    
    @Override
    public Long countTicketsByStatus(SupportTicket.TicketStatus status) {
        Session session = this.sessionFactory.getCurrentSession();
        String hql = "SELECT COUNT(st) FROM SupportTicket st WHERE st.status = :status";
        Query<Long> query = session.createQuery(hql, Long.class);
        query.setParameter("status", status);
        return query.getSingleResult();
    }
}