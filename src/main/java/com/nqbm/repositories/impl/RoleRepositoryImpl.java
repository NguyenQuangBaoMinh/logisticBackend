/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.repositories.impl;

/**
 *
 * @author baominh14022004gmail.com
 */

import com.nqbm.pojo.Role;
import com.nqbm.repositories.RoleRepository;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class RoleRepositoryImpl implements RoleRepository {
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public Role getRoleById(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        return session.get(Role.class, id);
    }

    @Override
    public Role getRoleByName(String name) {
        Session session = this.sessionFactory.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Role> query = builder.createQuery(Role.class);
        Root<Role> root = query.from(Role.class);
        query.select(root);
        
        Predicate p = builder.equal(root.get("name").as(String.class), name);
        query.where(p);
        
        try {
            return session.createQuery(query).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public List<Role> getRoles() {
        Session session = this.sessionFactory.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Role> query = builder.createQuery(Role.class);
        Root<Role> root = query.from(Role.class);
        query.select(root);
        
        return session.createQuery(query).getResultList();
    }

    @Override
    public boolean addRole(Role role) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            session.persist(role);
            return true;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateRole(Role role) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            session.merge(role);
            return true;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteRole(Long id) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            Role role = session.get(Role.class, id);
            session.remove(role);
            return true;
        } catch (HibernateException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }
}