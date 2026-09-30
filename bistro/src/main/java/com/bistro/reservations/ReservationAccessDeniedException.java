package com.bistro.reservations;

public class ReservationAccessDeniedException extends RuntimeException {
    public ReservationAccessDeniedException(String reservationCode) {
        super("La reserva " + reservationCode + " no te pertenece");
    }
}
