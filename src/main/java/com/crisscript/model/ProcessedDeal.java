package com.crisscript.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

@Entity
public class ProcessedDeal extends PanacheEntityBase {
    @Id
    public String id;
    
    @Column(length = 500)
    public String title;
    
    public Double initialTemperature;
    public Double currentTemperature;
    
    @Column(length = 500)
    public String link;
    
    public Double price;
    
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public boolean notified;

    public ProcessedDeal() {}

    public ProcessedDeal(String id, String title, Double temp, String link, Double price) {
        this.id = id;
        this.title = title;
        this.initialTemperature = temp;
        this.currentTemperature = temp;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.notified = false;
        this.link = link;
        this.price = price;
    }
}