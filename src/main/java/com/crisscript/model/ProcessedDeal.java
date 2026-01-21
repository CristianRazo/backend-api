package com.crisscript.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class ProcessedDeal extends PanacheEntityBase {
    @Id
    public String id;
    public String title;
    public Double initialTemperature;
    public Double currentTemperature;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public boolean notified; // Para no avisar 20 veces de la misma oferta caliente

    public ProcessedDeal() {}

    public ProcessedDeal(String id, String title, Double temp) {
        this.id = id;
        this.title = title;
        this.initialTemperature = temp;
        this.currentTemperature = temp;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.notified = false;
    }
}