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
            "error", "gratis", "regalo", "santander", "historico", "histórico", "bug", "fallo", "rapido", "pantalla",
            "tv", "monitor",
            "gaming", "gaming", "ultra", "led", "4k", "smart", "android", "ips", "qled", "oled", "hdr", "curvo",
            "144hz", "165hz", "240hz", "1ms", "juego",
            "portatil", "portátil", "laptop", "notebook", "ssd", "m2", "ram", "memoria", "procesador", "cpu", "ryzen",
            "intel", "nvidia", "rtx", "gtx", "grafica", "gráfica",
            "tablet", "movil", "móvil", "smartphone", "telefono", "teléfono", "auriculares", "auricular", "cascos",
            "smartwatch", "reloj", "wearable", "ps5", "xbox",
            "switch", "drone", "camara", "cámara", "video", "television", "televisión", "ssd", "disco duro", "pokemon",
            "tcg", "cartas");

    @POST
    @Path("/filter")
    @Transactional
    public Response filterDeal(DealDTO incomingDeal) {
        // Debug mejorado
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("📦 DEAL RECIBIDO:");
        System.out.println("   ID: " + incomingDeal.id());
        System.out.println("   Título: " + incomingDeal.title());
        System.out.println("   Precio: " + incomingDeal.price());
        System.out.println("   Temperatura: " + incomingDeal.temperature());
        System.out.println("   Link: " + incomingDeal.link());
        System.out.println("   Merchant: " + incomingDeal.merchant());
        System.out.println("   isExpired: " + incomingDeal.isExpired());
        System.out.println("═══════════════════════════════════════════════════");
        
        ProcessedDeal existing = ProcessedDeal.findById(incomingDeal.id());
        // 1. Regla de Heurística (Sniper Keywords)
        boolean isSniperMatch = SNIPER_KEYWORDS.stream()
                .anyMatch(word -> incomingDeal.title().toLowerCase().contains(word));

        // --- CASO NUEVA OFERTA ---
        if (existing == null) {
            ProcessedDeal newDeal = new ProcessedDeal(
                    incomingDeal.id(),
                    incomingDeal.title(),
                    incomingDeal.temperature(),
                    incomingDeal.link(),   
                    incomingDeal.price());
            newDeal.persist();

            if (isSniperMatch) {
                // Alertamos pero notified sigue en false para que la temperatura pueda disparar
                // después
                return Response.ok(newDeal).build();
            }
            return Response.status(202).build();
        }

        // --- CASO SEGUIMIENTO (Aquí aplicamos los fallbacks de seguridad) ---

        // FIX: Manejo de nulos para evitar el Error 500
        LocalDateTime lastUpdate = (existing.updatedAt != null) ? existing.updatedAt : existing.createdAt;
        // Si por algún motivo ambos son nulos (fallo de migración), asumimos que pasó 1
        // min
        if (lastUpdate == null)
            lastUpdate = LocalDateTime.now().minusMinutes(1);

        long minutesSinceLastUpdate = ChronoUnit.MINUTES.between(lastUpdate, LocalDateTime.now());
        if (minutesSinceLastUpdate <= 0)
            minutesSinceLastUpdate = 1;

        double oldTemp = (existing.currentTemperature != null) ? existing.currentTemperature : 0.0;
        double currentTemp = (incomingDeal.temperature() != null) ? incomingDeal.temperature() : 0.0;

        double tempDiff = currentTemp - oldTemp;
        double velocity = tempDiff / minutesSinceLastUpdate;

        // Actualizamos temperatura y fecha para la siguiente vuelta
        existing.currentTemperature = currentTemp;
        existing.updatedAt = LocalDateTime.now();

        // 2. Regla de Velocidad/Aumento Inusual
        boolean isSpiking = (tempDiff >= 20.0 || velocity >= 5.0);

        // Solo disparamos si no hemos notificado por comportamiento (!notified)
        // O si es un sniper match (esto permite que el sniper se repita si la oferta
        // sube de temp)
        if (!existing.notified && (isSpiking || isSniperMatch)) {

            // Solo bloqueamos futuras notificaciones si fue por un spike de temperatura
            // real
            if (isSpiking) {
                existing.notified = true;
            }

            return Response.ok(existing).build();
        }

        return Response.status(409).build();
    }
}