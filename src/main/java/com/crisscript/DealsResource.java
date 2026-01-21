package com.crisscript;

import com.crisscript.dto.DealDTO;
import com.crisscript.model.ProcessedDeal;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Path("/v1/deals")
public class DealsResource {

    // Lista de palabras clave de alta prioridad
    private static final List<String> SNIPER_KEYWORDS = List.of(
            "error", "gratis", "regalo","santander", "historico", "histórico", "bug","fallo","rapido","pantalla","tv","monitor",
            "gaming","gaming","ultra","led","4k","smart","android","ips","qled","oled","hdr","curvo","144hz","165hz","240hz","1ms","juego",
            "portatil","portátil","laptop","notebook","ssd","m2","ram","memoria","procesador","cpu","ryzen","intel","nvidia","rtx","gtx","grafica","gráfica",
            "tablet","movil","móvil","smartphone","telefono","teléfono","auriculares","auricular","cascos","smartwatch","reloj","wearable","ps5","xbox",
            "switch","drone","camara","cámara","video","television","televisión","ssd","disco duro","pokemon","tcg","cartas"
    );

    @POST
    @Path("/filter")
    @Transactional
    public Response filterDeal(DealDTO incomingDeal) {
        ProcessedDeal existing = ProcessedDeal.findById(incomingDeal.id());

        // 1. Regla de Heurística (Sniper Keywords)
        boolean isSniperMatch = SNIPER_KEYWORDS.stream()
                .anyMatch(word -> incomingDeal.title().toLowerCase().contains(word));

        // --- CASO NUEVA OFERTA ---
        if (existing == null) {
            ProcessedDeal newDeal = new ProcessedDeal(
                    incomingDeal.id(),
                    incomingDeal.title(),
                    incomingDeal.temperature());
            newDeal.persist();

            if (isSniperMatch) {
                // Alertamos de inmediato, pero DB.notified sigue en FALSE
                // para permitir que la regla de temperatura dispare después.
                return Response.ok(newDeal).build();
            }
            return Response.status(202).build(); // Guardada en silencio
        }

        // --- CASO SEGUIMIENTO (Ya existe en DB) ---
        double currentTemp = incomingDeal.temperature() != null ? incomingDeal.temperature() : 0.0;
        long minutesSinceLastUpdate = ChronoUnit.MINUTES.between(existing.updatedAt, LocalDateTime.now());
        if (minutesSinceLastUpdate == 0)
            minutesSinceLastUpdate = 1; // Evitar división por cero

        double tempDiff = currentTemp - existing.currentTemperature;
        double velocity = tempDiff / minutesSinceLastUpdate;

        // Actualizamos temperatura actual para la siguiente comparación
        existing.currentTemperature = currentTemp;
        existing.updatedAt = LocalDateTime.now();

        // 2. Regla de Velocidad/Aumento Inusual
        // Dispara si subió > 20 grados o si la velocidad es > 5 grados por minuto
        boolean isSpiking = (tempDiff >= 20.0 || velocity >= 5.0);

        if (!existing.notified && (isSpiking || isSniperMatch)) {
            // Si es por temperatura, marcamos como notificado para no repetir.
            // Si es solo por keyword, podrías decidir no marcarlo aún.
            if (isSpiking)
                existing.notified = true;

            return Response.ok(existing).build();
        }

        return Response.status(409).build(); // Nada interesante que reportar
    }
}