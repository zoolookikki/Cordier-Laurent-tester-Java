package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.config.DataBaseConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ParkingSpotDAOTest {

    private static final int PARKING_NUMBER_TEST = 1;
    private static final String NAME_CAR_TYPE_TEST = "CAR";
    private ParkingSpotDAO parkingSpotDAO;

    @Mock
    private static DataBaseConfig dataBaseConfig;
    @Mock
    private static Connection con;
    @Mock
    private static PreparedStatement ps;
    @Mock
    private static ResultSet rs;

    @BeforeEach
    public void setUpPerTest() throws Exception {
        try {
            parkingSpotDAO = new ParkingSpotDAO();
            parkingSpotDAO.dataBaseConfig = dataBaseConfig;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize ticketDAO");
        }
        when(dataBaseConfig.getConnection()).thenReturn(con);
        when(con.prepareStatement(anyString())).thenReturn(ps);
    }

    @Test
    public void getParkingSpotWithSuccess() throws Exception {
        // given :
        // execution must succeed.
        when(ps.executeQuery()).thenReturn(rs);
        // there is a parking spot .
        when(rs.next()).thenReturn(true);
        // make a parking spot.
        when(rs.getInt(1)).thenReturn(PARKING_NUMBER_TEST);
        when(rs.getBoolean(2)).thenReturn(false);
        when(rs.getString(3)).thenReturn(NAME_CAR_TYPE_TEST);

        // when :
        ParkingSpot parkingSpot = parkingSpotDAO.getParkingSpot(PARKING_NUMBER_TEST);

        // then :
        assertThat(parkingSpot).isNotNull();
        verify(ps, Mockito.times(1)).executeQuery();
    }

    @Test
    public void getNextAvailableSlotWithSuccess() throws Exception {
        // given ;
        // execution must succeed.
        when(ps.executeQuery()).thenReturn(rs);
        // there is a free slot.
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(PARKING_NUMBER_TEST);

        // when :
        int parkingNumber = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

        // then :
        assertThat(parkingNumber).isEqualTo(PARKING_NUMBER_TEST);
        verify(ps, Mockito.times(1)).executeQuery();
    }
    
    @Test
    public void updateParkingWithSuccess() throws Exception {
        // given :
        ParkingSpot parkingSpot = new ParkingSpot(PARKING_NUMBER_TEST, ParkingType.CAR, true);
        // one row modified, it's ok.
        when(ps.executeUpdate()).thenReturn(1);

        // when :
        boolean result = parkingSpotDAO.updateParking(parkingSpot);

        // then :
        assertThat(result).isTrue();
        verify(ps, Mockito.times(1)).executeUpdate();
    }
}
