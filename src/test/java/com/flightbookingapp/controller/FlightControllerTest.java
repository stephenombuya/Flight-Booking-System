package com.flightbookingapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightbookingapp.TestDataFactory;
import com.flightbookingapp.dto.request.FlightRequest;
import com.flightbookingapp.dto.request.FlightSearchRequest;
import com.flightbookingapp.dto.response.FlightResponse;
import com.flightbookingapp.dto.response.PagedResponse;
import com.flightbookingapp.model.Flight;
import com.flightbookingapp.model.FlightStatus;
import com.flightbookingapp.service.FlightService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FlightController.class)
@DisplayName("FlightController")
class FlightControllerTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean FlightService flightService;
    @MockBean com.flightbookingapp.security.JwtService jwtService;
    @MockBean org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private FlightResponse sampleFlight;

    @BeforeEach
    void setUp() {
        Flight f = TestDataFactory.buildFlight();
        sampleFlight = FlightResponse.builder()
                .id(1L).flightNumber("FBS-001").airline("Demo Air")
                .origin("Nairobi").destination("London")
                .departureTime(LocalDateTime.now().plusDays(10))
                .arrivalTime(LocalDateTime.now().plusDays(10).plusHours(8))
                .price(new BigDecimal("450.00"))
                .totalSeats(200).availableSeats(180)
                .status(FlightStatus.SCHEDULED).durationMinutes(480)
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/flights returns 200 for anonymous user")
    void searchFlights_publicAccess() throws Exception {
        PagedResponse<FlightResponse> page = PagedResponse.<FlightResponse>builder()
                .content(List.of(sampleFlight)).page(0).size(20).totalElements(1)
                .totalPages(1).last(true).build();
        when(flightService.searchFlights(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/flights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].flightNumber").value("FBS-001"));
    }

    @Test
    @DisplayName("GET /api/v1/flights/{id} returns 200 for known flight")
    void getFlightById_found() throws Exception {
        when(flightService.getFlightById(1L)).thenReturn(sampleFlight);

        mockMvc.perform(get("/api/v1/flights/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.origin").value("Nairobi"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/flights returns 201 for admin")
    void createFlight_adminAllowed() throws Exception {
        when(flightService.createFlight(any(FlightRequest.class))).thenReturn(sampleFlight);

        mockMvc.perform(post("/api/v1/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.buildFlightRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.flightNumber").value("FBS-001"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("POST /api/v1/flights returns 403 for customer")
    void createFlight_customerForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.buildFlightRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/v1/flights/{id} returns 204 for admin")
    void deleteFlight_adminAllowed() throws Exception {
        mockMvc.perform(delete("/api/v1/flights/1"))
                .andExpect(status().isNoContent());
    }
}
