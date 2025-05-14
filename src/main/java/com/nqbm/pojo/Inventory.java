/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nqbm.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 *
 * @author ADMIN
 */

@Entity
@Table(name="inventory")
public class Inventory implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
   
    @Column(name = "total_product")
    private Integer totalProduct;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update")
    private Date lastUpdate;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "inventoryId")
    @JsonIgnore
    private Set<Product> productSet;
    
    //CONSTRUCTOR
    public Inventory(){}
    
    public Inventory(Integer id){
        this.id = id;
    }
    
    public Inventory(Integer id, Integer totalProduct){
        this.id = id;
        this.totalProduct = totalProduct;
    }
    
    //GETTER - SETTER

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the totalProduct
     */
    public Integer getTotalProduct() {
        return totalProduct;
    }

    /**
     * @param totalProduct the totalProduct to set
     */
    public void setTotalProduct(Integer totalProduct) {
        this.totalProduct = totalProduct;
    }

    /**
     * @return the lastUpdate
     */
    public Date getLastUpdate() {
        return lastUpdate;
    }

    /**
     * @param lastUpdate the lastUpdate to set
     */
    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    /**
     * @return the productSet
     */
    public Set<Product> getProductSet() {
        return productSet;
    }

    /**
     * @param productSet the productSet to set
     */
    public void setProductSet(Set<Product> productSet) {
        this.productSet = productSet;
    }
    
    
    
    
}
