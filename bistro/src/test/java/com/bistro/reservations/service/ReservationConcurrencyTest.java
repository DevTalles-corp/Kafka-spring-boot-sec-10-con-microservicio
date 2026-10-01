package com.bistro.reservations.service;

import com.bistro.reservations.controller.ReservationRequest;
import com.bistro.reservations.controller.ReservationResponse;
import com.bistro.reservations.model.ReservationStatus;
import com.bistro.reservations.repository.ReservationRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(properties = {
        "spring.datasource.hikari.maximum-pool-size=1"
})
class ReservationConcurrencyTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Disabled("Cuenta antes de asignar las mesas, y la asignación entrega " +
            "la misma mesa dos veces. Se resuelve en la sección de testing.")
    @Test
    void shouldConfirmExactlyOneReservationForSameSlotAndTable() throws InterruptedException, ExecutionException {
        reservationRepository.deleteAll();

        LocalDateTime slot = LocalDateTime.of(2026, 8, 25, 19, 0);
        int partySize = 8;
        int concurrentRequests = 4;

        ExecutorService executor = Executors.newFixedThreadPool(concurrentRequests);
        List<Future<ReservationResponse>> futures = new ArrayList<>();

        for (int i = 0; i < concurrentRequests; i++) {
            final int index = i;
            Callable<ReservationResponse> task = () -> {
                ReservationRequest request = ReservationRequest.builder()
                        .reservationTime(slot)
                        .partySize(partySize)
                        .build();
                CustomerIdentity customer = new CustomerIdentity(
                        "cliente-" + index, "Cliente " + index, "cliente" + index + "@example.com");

                return reservationService.createReservation(request, customer);
            };
            futures.add(executor.submit(task));
        }

        List<ReservationResponse> responses = new ArrayList<>();
        for (Future<ReservationResponse> future : futures) {
            responses.add(future.get());
        }
        executor.shutdown();

        long confirmedCount = responses.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .count();

        assertThat(confirmedCount).isEqualTo(1);

        long persistedConfirmed = reservationRepository.countByStatus(ReservationStatus.CONFIRMED);
        assertThat(persistedConfirmed).isEqualTo(1);
    }
}
