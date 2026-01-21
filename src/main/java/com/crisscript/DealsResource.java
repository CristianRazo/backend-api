package com.crisscript;

import com.crisscript.dto.DealDTO;
import com.crisscript.model.ProcessedDeal;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;

@Path("/v1/deals")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DealsResource {

    @POST
    @Path("/filter")
    @Transactional
    public Response filter(DealDTO deal) {
        // Regla 1: ¿Ya lo procesamos antes?
        if (!ProcessedDeal.isNew(deal.id())) {
            return Response.status(Response.Status.CONFLICT).build(); 
        }

        // Regla 2: ¿Es una oferta "Caliente" o error de precio?
        boolean isHot = deal.temperature() != null && deal.temperature() >= 500;
        boolean isError = deal.title().toLowerCase().contains("error de precio");

        if (isHot || isError) {
            ProcessedDeal processed = new ProcessedDeal();
            processed.dealId = deal.id();
            processed.processedAt = LocalDateTime.now();
            processed.persist();
            
            return Response.ok(deal).build();
        }

        return Response.noContent().build();
    }
}