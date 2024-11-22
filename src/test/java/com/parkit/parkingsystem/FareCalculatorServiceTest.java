package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.util.MonetaryUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

//import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Date;

public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
    }

    private Date getDateNow() {
        Date result;
        result = new Date();
        return result ;
    }
    
    private Date getPastDate(int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("invalid argument");
        }

        Date result ;
        result = getDateNow() ;
        result.setTime(System.currentTimeMillis() - (minutes * 60 * 1000));
        return result ;
    }
    
    private Date getFutureDate(int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("invalid argument");
        }

        Date result ;
        result = getDateNow() ;
        result.setTime(System.currentTimeMillis() + (minutes * 60 * 1000));
        return result ;
    }
    
    private void makeTicket(int parkingSpotId, ParkingType parkingType, Date inTime, Date outTime) {
        ParkingSpot parkingSpot = new ParkingSpot(parkingSpotId, parkingType, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
    }
    
    @Test
    public void calculateFareCar(){
        makeTicket (1, ParkingType.CAR, getPastDate(60), getDateNow()) ;
        fareCalculatorService.calculateFare(ticket);
        assertThat(ticket.getPrice()).isEqualTo(Fare.CAR_RATE_PER_HOUR);
    }

    @Test
    public void calculateFareBike(){
        makeTicket (1, ParkingType.BIKE, getPastDate(60), getDateNow()) ;
        fareCalculatorService.calculateFare(ticket);
        assertThat(ticket.getPrice()).isEqualTo(Fare.BIKE_RATE_PER_HOUR);
    }

    // Deleted for the moment: it is no longer possible to pass null for ParkingType. But unit tests should be done for ParkingSpot.
/*    
    @Test
    public void calculateFareUnkownType(){
        makeTicket (1, null, getPastDate(60), getDateNow()) ;
        assertThatThrownBy(() -> fareCalculatorService.calculateFare(ticket)).isInstanceOf(NullPointerException.class);
    }
*/
    
    @Test
    // same for CAR.
    public void calculateFareBikeWithFutureInTime(){
        makeTicket (1, ParkingType.BIKE, getFutureDate(60), getDateNow()) ;
        assertThatThrownBy(() -> fareCalculatorService.calculateFare(ticket)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Out time is before in time");
    }
    
    @Test
    // same for CAR.
    public void calculateFareBikeWithLessThanOneHourParkingTime(){
        // 45 minutes parking time should give 3/4th parking fare
        makeTicket (1, ParkingType.BIKE, getPastDate(45), getDateNow()) ;
        fareCalculatorService.calculateFare(ticket);
        assertThat(ticket.getPrice()).isEqualTo(MonetaryUtil.round(0.75 * Fare.BIKE_RATE_PER_HOUR));    
    }

    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime(){
        // 45 minutes parking time should give 3/4th parking fare
        makeTicket (1, ParkingType.CAR, getPastDate(45), getDateNow()) ;
        fareCalculatorService.calculateFare(ticket);
        assertThat(ticket.getPrice()).isEqualTo(MonetaryUtil.round(0.75 * Fare.CAR_RATE_PER_HOUR));    
    }

    @Test
    public void calculateFareCarWithMoreThanADayParkingTime(){
        // 24 hours parking time should give 24 * parking fare per hour
        makeTicket (1, ParkingType.CAR, getPastDate(24 * 60), getDateNow()) ;
        fareCalculatorService.calculateFare(ticket);
        assertThat(ticket.getPrice()).isEqualTo(MonetaryUtil.round(24 * Fare.CAR_RATE_PER_HOUR));    
     }
    
    // The new tests.
    @Test
    @DisplayName("For a car, parking is free for periods of less than 30 minutes")
    public void calculateFareCarWithLessThan30minutesParkingTime(){
        // 29 minutes parking time should be free
        makeTicket (1, ParkingType.CAR, getPastDate(29), getDateNow()) ;
        fareCalculatorService.calculateFare(ticket);
        assertThat(ticket.getPrice()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("For a bike, parking is free for periods of less than 30 minutes")
    public void calculateFareBikeWithLessThan30minutesParkingTime(){
        // 29 minutes parking time should be free
        makeTicket (1, ParkingType.BIKE, getPastDate(29), getDateNow()) ;
        fareCalculatorService.calculateFare(ticket);
        assertThat(ticket.getPrice()).isEqualTo(0.0);
    }
    
    @Test
    @DisplayName("For a car, discount for returning users")
    public void calculateFareCarWithDiscount() {
        makeTicket (1, ParkingType.CAR, getPastDate(60), getDateNow()) ;
        fareCalculatorService.calculateFare(ticket, true);
        assertThat(ticket.getPrice()).isEqualTo(MonetaryUtil.round(Fare.RECURRING_USER_DISCOUNT * Fare.CAR_RATE_PER_HOUR));        
    }
    
    @Test
    @DisplayName("For a bike, discount for returning users")
    public void calculateFareBikeWithDiscount() {
        makeTicket (1, ParkingType.BIKE, getPastDate(60), getDateNow()) ;
        fareCalculatorService.calculateFare(ticket, true);
        assertThat(ticket.getPrice()).isEqualTo(MonetaryUtil.round(Fare.RECURRING_USER_DISCOUNT * Fare.BIKE_RATE_PER_HOUR));
    }
    
    @Test
    @DisplayName("calculteFare constructor test")
    public void calculateFareWithNullTicket() {
        assertThatThrownBy(() -> fareCalculatorService.calculateFare(null)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Ticket cannot be null");
    }

    @Test
    @DisplayName("Ticket outime is null")
    public void calculateFareWithNullOutTime() {
        makeTicket (1, ParkingType.CAR, getPastDate(60), null) ;
        assertThatThrownBy(() -> fareCalculatorService.calculateFare(ticket)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Out time or in time is incorrect");
    }
    
    @Test
    @DisplayName("Ticket intime is null")
    public void calculateFareWithNullInTime() {
        makeTicket (1, ParkingType.CAR, null, getDateNow()) ;
        ticket.setInTime(null);
        assertThatThrownBy(() -> fareCalculatorService.calculateFare(ticket)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Out time or in time is incorrect");
    }
}
