package com.bistro.reservations.controller;

import com.bistro.reservations.service.CustomerIdentity;
import com.bistro.reservations.service.Requester;
import com.bistro.reservations.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {

        ReservationStatusResponse response = reservationService.getReservationStatus(reservationCode,
                requester(jwt, authentication));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{reservationCode}/cancel")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable String reservationCode,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
            ) {

        reservationService.cancel(reservationCode, requester(jwt, authentication));
        return ResponseEntity.noContent().build();
    }

    private static Requester requester(Jwt jwt, Authentication authentication){
        boolean staff = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_STAFF"));

        return new Requester(jwt.getSubject(), staff);
    }


}










