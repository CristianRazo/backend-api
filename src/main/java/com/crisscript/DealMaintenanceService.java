package com.crisscript;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.jboss.logging.Logger;

import com.crisscript.model.ProcessedDeal;

@ApplicationScoped
public class DealMaintenanceService {

    private static final Logger LOG = Logger.getLogger(DealMaintenanceService.class);

    // Se ejecuta cada hora para limpiar lo que tenga más de 24 horas
    @Transactional
    @Scheduled(every = "1h") 
    void cleanOldDeals() {
        LOG.info("Iniciando limpieza de base de datos...");
        
        // Borramos ofertas creadas hace más de 24 horas
        long deletedCount = ProcessedDeal.delete("createdAt < ?1", LocalDateTime.now().minusDays(1));
        
        if (deletedCount > 0) {
            LOG.infof("Limpieza completada. Se eliminaron %d ofertas antiguas.", deletedCount);
        }
    }
}