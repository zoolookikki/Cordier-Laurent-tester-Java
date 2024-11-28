package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static final String VEHICULE_REG_NUMBER_TEST = "ABCDEF";
    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;
    
    
    @BeforeEach
    private void setUpPerTest() {
        try {
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize parkingService");
        }
    }

    @Nested
    class ProcessExitingVehicleTests {
        
        private void commonExitingVehicle (int nbTicket, boolean isUpdateTicket) {
            try {
                // parking space 1 is occupied.
                ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
    
                // ticket for one hour of parking on this parking space.
                Ticket ticket = new Ticket();
                ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
                ticket.setParkingSpot(parkingSpot);
    
                // mock configuration
                when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VEHICULE_REG_NUMBER_TEST);
                when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
                when(ticketDAO.getNbTicket(VEHICULE_REG_NUMBER_TEST)).thenReturn(nbTicket);
                when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(isUpdateTicket);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed in commonExitingVehicle");
            }
        }
        
        @Test
        @DisplayName("A customer vehicle leaves the parking")
        public void processExitingVehicle() {
            // given : the vehicle is present, there is a ticket and its parking space is occupied.
            commonExitingVehicle(1, true) ;
            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
    
            // when : the vehicle leaves.
            parkingService.processExitingVehicle();
    
            // then : updateTicket and updateParking must have been called once.
            verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
            verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        }
    
        @Test
        @DisplayName("A customer vehicle leaves the parking but updating the ticket will not work")
        public void processExitingVehicleUnableUpdate() {
            // given : the vehicle is present, there is a ticket and its parking space is occupied. But updating the ticket will not work.
           commonExitingVehicle(0, false) ;
    
           // when : the vehicle leaves.
           parkingService.processExitingVehicle();
    
           // then : 
           // updateTicket must have been called once.
           verify(ticketDAO,  Mockito.times(1)).updateTicket(any(Ticket.class));
           // updateParking should never be called.
           verify(parkingSpotDAO, Mockito.never()).updateParking(any(ParkingSpot.class));
        }
    }

    @Nested
    class ProcessIncomingVehicleTests {
        
        private void commonIncomingVehicle(int nbTicket) {
            try {
                when(inputReaderUtil.readSelection()).thenReturn(1);
                when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VEHICULE_REG_NUMBER_TEST);
                when(ticketDAO.getNbTicket(VEHICULE_REG_NUMBER_TEST)).thenReturn(nbTicket);
                when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
                when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
                when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed in CommonIncomingVehicle");
            }
        }

        @Test
        @DisplayName("A customer vehicle enters in the parking")
        public void processIncomingVehicle() {
            // given : configuration of mocks for vehicle entry test. No previous ticket, only one current ticket, no discount.
            commonIncomingVehicle(1);
              
            // when : the vehicle enters.
            parkingService.processIncomingVehicle();
              
            // then : saveTicket and updateParking must have been called once.
            verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class)); 
            verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class)); 
        }
  
        @Test
        @DisplayName("An already customer vehicle enters the parking again")
        public void processIncomingVehicleWithDiscount(){
            // given : configuration of mocks for vehicle entry test. One previous ticket + one current ticket => we make discount.
            commonIncomingVehicle(2);
            
            // when : the vehicle enters.
            parkingService.processIncomingVehicle();
                   
            // then : saveTicket and updateParking must have been called once.
            verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class)); 
            verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class)); 
        }
    }
    
    
    @Nested
    class GetNextParkingNumberTests {

        private void commonGetNextParkingNumber (int vehiculeTypeChoice) {
            try {
                when(inputReaderUtil.readSelection()).thenReturn(vehiculeTypeChoice);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed in commonGetNextParkingNumber");
            }
        }
   
        @Test
        @DisplayName("Testing the next free parking space : ok")
        public void GetNextParkingNumberIfAvailable() {
           // given : 
           // configuration of mocks for testing the next free parking space. Input choice = a car. 
           commonGetNextParkingNumber (1);
           // with parking number 1.
           when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
       
          // when : to get the next free parking space.
          ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

          // then :
          assertThat(parkingSpot).as("The parkingSpot must not be null").isNotNull();
          assertThat(parkingSpot.getId()).as("The parking space id must be equal to 1").isEqualTo(1);
          assertThat(parkingSpot.getParkingType()).as("The parking type must be a car").isEqualTo(ParkingType.CAR);
          assertThat(parkingSpot.isAvailable()).as("The parking space must be available").isTrue();          
       }
   
       @Test
       @DisplayName("Testing the next free parking space : not ok")
       public void GetNextParkingNumberIfAvailableParkingNumberNotFound() {
           // given : 
           // configuration of mocks for testing the next free parking space. Input choice = a bike. 
           commonGetNextParkingNumber (2);    
           // with no parking number found.
           when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1);
    
           // when : to get the next free parking space.
           ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
    
           // then :
           assertThat(parkingSpot).as("The parkingSpot must be null").isNull();
       }
   
       @Test
       @DisplayName("Testing the next free parking space : wrong user choice")
       public void GetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
           // given : 
           // configuration of mocks for testing the next free parking space. Input choice = wrong choice.For a wrong.
           commonGetNextParkingNumber (3);    

           // when : to get the next free parking space.
           ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
    
           // then :
           assertThat(parkingSpot).as("The parkingSpot must be null").isNull();
       }
    }
    
    @Test
    @DisplayName("Testing class ParkingService with null arguments")
    public void constructorWithNullArguments() {
        assertThatThrownBy(() -> new ParkingService(null, parkingSpotDAO, ticketDAO)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid argument in ParkingService");
        assertThatThrownBy(() -> new ParkingService(inputReaderUtil, null, ticketDAO)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid argument in ParkingService");
        assertThatThrownBy(() -> new ParkingService(inputReaderUtil, parkingSpotDAO, null)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Invalid argument in ParkingService");
    }
}
