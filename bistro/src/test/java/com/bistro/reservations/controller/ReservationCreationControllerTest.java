package com.bistro.reservations.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class ReservationCreationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor anaToken(){
        return jwt().jwt(token -> token
                .subject("ana-id")
                .claim("name", "Ana García")
                .claim("email", "ana@example.com"));
    }

    @Disabled("La mesa se asigna por eventos, después de la respuesta. Se resuelve en la sección de testing.")
    @Test
    void shouldConfirmReservation() throws Exception {
        mockMvc.perform(post("/api/v1/reservations")
                        .with(anaToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reservationTime": "2026-08-20T19:30:00",
                                  "partySize": 4
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationCode").isNotEmpty())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.assignedTableId").exists());
    }

    @Disabled("El rechazo llega por eventos, después de la respuesta. Se resuelve en la sección de testing.")
    @Test
    void shouldRejectReservationWhenNoCapacity() throws Exception {
        mockMvc.perform(post("/api/v1/reservations")
                        .with(anaToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reservationTime": "2026-08-20T20:00:00",
                                  "partySize": 10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationCode").isNotEmpty())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.assignedTableId").doesNotExist());
    }

    @Test
    void shouldReturn400WhenPartySizeExceeds12() throws Exception {
        mockMvc.perform(post("/api/v1/reservations")
                        .with(anaToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reservationTime": "2026-08-20T20:00:00",
                                  "partySize": 13
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Solicitud inválida"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void shouldReturn400ForInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/reservations")
                        .with(anaToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reservationTime": null,
                                  "partySize": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Solicitud inválida"))
                .andExpect(jsonPath("$.errors").isArray());
    }
}
