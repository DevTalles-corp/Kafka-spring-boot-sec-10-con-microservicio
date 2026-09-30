package com.bistro.reservations.controller;

import com.bistro.reservations.model.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reservationCode", ignore = true)
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "customerName", ignore = true)
    @Mapping(target = "customerEmail", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "assignedTableId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Reservation toEntity(ReservationRequest request);

    ReservationResponse toResponse(Reservation reservation);

    ReservationStatusResponse toStatusResponse(Reservation reservation);
}
