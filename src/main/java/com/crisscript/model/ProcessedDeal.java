package com.crisscript.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;

@Entity
public class ProcessedDeal extends PanacheEntity {
    public String dealId;
    public LocalDateTime processedAt;

    public static boolean isNew(String id) {
        return find("dealId", id).firstResult() == null;
    }
}