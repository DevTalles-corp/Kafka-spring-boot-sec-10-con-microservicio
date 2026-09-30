package com.bistro.reservations.controller;

import com.bistro.reservations.service.CustomerIdentity;
import com.bistro.reservations.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request, @AuthenticationPrincipal Jwt jwt) {
        CustomerIdentity customer = new CustomerIdentity(
                jwt.getSubject(),
                jwt.getClaimAsString("name"),
                jwt.getClaimAsString("email"));


        ReservationResponse response = reservationService.createReservation(request, customer);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{reservationCode}")
    public ResponseEntity<ReservationStatusResponse> getReservationStatus(
            @PathVariable String reservationCode,
            @AuthenticationPrincipal Jwt jwt) {

        ReservationStatusResponse response = reservationService.getReservationStatus(reservationCode, jwt.getSubject());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{reservationCode}/cancel")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable String reservationCode,
            @AuthenticationPrincipal Jwt jwt) {

        reservationService.cancel(reservationCode, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }


}










