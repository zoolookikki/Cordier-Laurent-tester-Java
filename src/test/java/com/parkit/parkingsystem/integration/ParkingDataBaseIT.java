package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import com.parkit.parkingsystem.util.MonetaryUtil;
import com.parkit.parkingsystem.util.DateUtil;
import com.parkit.parkingsystem.model.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static final Logger logger = LogManager.getLogger("ParkingDataBaseIT");

    private static final String VEHICULE_REG_NUMBER_TEST = "ABCDEF";
    private static final String VEHICULE_REG_NUMBER_NOT_EXIST = "xxxxxxxxxxxxxxx";
    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private static boolean discount = false;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        dataBasePrepareService.clearDataBaseEntries();
        discount = false ;
    }

    @AfterAll
    private static void tearDown(){
    }

    @Test
    @DisplayName("Integration test to verify that following an entry, the database is ok.")
    public void testParkingACar() throws Exception {
        logger.info("-----> START TEST 1");

        // given :
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VEHICULE_REG_NUMBER_TEST);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        
        // when :
        parkingService.processIncomingVehicle();

        // then :
        // ticket ok ?
        Ticket ticket = ticketDAO.getTicket(VEHICULE_REG_NUMBER_TEST);
        assertThat(ticket).as("The ticket must not be null").isNotNull();
        assertThat(ticket.getId()).as("The ticket id must be greater than 0").isGreaterThan(0);
        assertThat(ticket.getParkingSpot()).as("The ticket parkingspot must not be null").isNotNull();
        assertThat(ticket.getParkingSpot().getId()).as("The ticket parkingspot id must be greater than 0").isGreaterThan(0);
        assertThat(ticket.getParkingSpot().getParkingType()).as("The ticket parking type must be a car").isEqualTo(ParkingType.CAR);
        assertThat(ticket.getParkingSpot().isAvailable()).as("The parkingspot space must be not available").isFalse();          
        assertThat(ticket.getVehicleRegNumber()).as("The vehicle registration number must be equal to the test constant").isEqualTo(VEHICULE_REG_NUMBER_TEST);
        assertThat(ticket.getPrice()).as("The price must be equal to 0").isEqualTo(0.0);        
        assertThat(ticket.getInTime()).as("The ticket Intime must not be null").isNotNull();
        assertThat(ticket.getOutTime()).as("The ticket Outtime must be null").isNull();
        // parking ok ?
        ParkingSpot parkingSpot = parkingSpotDAO.getParkingSpot(ticket.getParkingSpot().getId());
        assertThat(parkingSpot).as("The parkingspot must not be null").isNotNull();
        assertThat(parkingSpot.getId()).as("The parkingspot id must be equal to the ticket parkingspot id").isEqualTo(ticket.getParkingSpot().getId());
        assertThat(parkingSpot.getParkingType()).as("The parkingspot type must be a car").isEqualTo(ParkingType.CAR);
        assertThat(parkingSpot.isAvailable()).as("The parkingspot space must be not available").isFalse();          
        
        logger.info("<----- END TEST 1");
    }
    
    @Test
    @DisplayName("Integration test to verify that following an exit, the database is ok.")
    public void testParkingLotExit() throws Exception {
        logger.info("-----> START TEST 2");
        
        // given :
        Ticket ticket = null;
        // the vehicle must be present.
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // when : 
        // modification of the entry time (one hour less) so that there is price > 0.
        ticket = ticketDAO.getTicket(VEHICULE_REG_NUMBER_TEST);
        assertThat(ticket).as("The ticket must not be null").isNotNull();
        assertThat(ticket.getInTime()).as("The ticket Intime must not be null").isNotNull();
        dataBasePrepareService.updateOneTicketInTime(ticket, DateUtil.getPastDate(60));
        // the vehicle leaves.
        parkingService.processExitingVehicle();
            
        // then :
        // ticket ok ?
        ticket = ticketDAO.getTicket(VEHICULE_REG_NUMBER_TEST);
        assertThat(ticket.getId()).as("The ticket id must be greater than 0").isGreaterThan(0);
        assertThat(ticket.getVehicleRegNumber()).as("The vehicle registration number must be equal to the test constant").isEqualTo(VEHICULE_REG_NUMBER_TEST);
        if (discount) {
            assertThat(ticket.getPrice())
                .as("The price for returning users must be equal to 1 hour parking car with 5% discount")
                .isEqualTo(MonetaryUtil.round(Fare.RECURRING_USER_DISCOUNT * Fare.CAR_RATE_PER_HOUR));        
        } else {
            assertThat(ticket.getPrice()).as("The price must be equal to 1 hour parking car").isEqualTo(Fare.CAR_RATE_PER_HOUR);        
        }
        assertThat(ticket.getOutTime()).as("The ticket Outtime must not be null").isNotNull();
        // parking ok ?
        ParkingSpot parkingSpot = parkingSpotDAO.getParkingSpot(ticket.getParkingSpot().getId());
        assertThat(parkingSpot).as("The parkingspot must not be null").isNotNull();
        assertThat(parkingSpot.getId()).as("The parkingspot id must be equal to the ticket parkingspot id").isEqualTo(ticket.getParkingSpot().getId());
        assertThat(parkingSpot.getParkingType()).as("The parkingspot type must be a car").isEqualTo(ParkingType.CAR);
        assertThat(parkingSpot.isAvailable()).as("The parkingspot space must be not available").isTrue();          
        
        logger.info("<----- END TEST 2");
    }
    
    @Test
    @DisplayName("Integration test leaving a vehicle with a non-existent registration number")
    public void testNotExistRegistrationNumber() throws Exception {
        logger.info("-----> START TEST 3");

        // given :
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VEHICULE_REG_NUMBER_NOT_EXIST);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        
        // when :
        parkingService.processExitingVehicle();
        
        // then : 
        Ticket ticket = ticketDAO.getTicket(VEHICULE_REG_NUMBER_NOT_EXIST);
        assertThat(ticket).as("No ticket should be created for a non-existent registration number").isNull();

        logger.info("<----- END TEST 3");
    }
    
    @Test
    @DisplayName("Integration test discount for returning users")
    public void testParkingLotExitRecurringUser() throws Exception {
        logger.info("-----> START TEST 4");
        
        // given : 
        // a previous ticket for a discount.
        dataBasePrepareService.putOldTicket(VEHICULE_REG_NUMBER_TEST);
        // optimization to test the case with discount.
        discount = true;
        
        // when then :
        testParkingLotExit();
            
        logger.info("<----- END TEST 4");
    }
    
}
