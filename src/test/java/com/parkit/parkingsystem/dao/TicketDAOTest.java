package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.util.DateUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.ResultSet;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TicketDAOTest {

    private static final String VEHICULE_REG_NUMBER_TEST = "ABCDEF";
    private static final String NAME_CAR_TYPE_TEST = "CAR";
    private static final int NB_TICKET_TEST = 99;
    private static final double PRICE_TEST = 1.23;
    private static TicketDAO ticketDAO;

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
            ticketDAO = new TicketDAO();
            ticketDAO.dataBaseConfig = dataBaseConfig;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize ticketDAO");
        }
        when(dataBaseConfig.getConnection()).thenReturn(con);
        when(con.prepareStatement(anyString())).thenReturn(ps);
    }

    @Test
    public void saveTicketWithSuccess() throws Exception {
        // given : 
        // ticket creation.
        Ticket ticket = new Ticket();
        ticket.setId(1);
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ticket.setVehicleRegNumber(VEHICULE_REG_NUMBER_TEST);
        ticket.setPrice(0.0);
        ticket.setInTime(DateUtil.getDateNow());
        ticket.setOutTime(null);
        // execution must succeed.
        when(ps.execute()).thenReturn(true);

        // when : 
        boolean result = ticketDAO.saveTicket(ticket);

        // then :
        assertThat(result).isTrue();
        verify(ps, Mockito.times(1)).execute();
    }

    @Test
    public void getTicketWithSuccess() throws Exception {
        // given : 
        // execution must succeed.
        when(ps.executeQuery()).thenReturn(rs);
        // there is a ticket.
        when(rs.next()).thenReturn(true);
        // make the ticket.
            // Mockito.lenient() because bug mockito ?
        Mockito.lenient().when(rs.getInt(1)).thenReturn(1);
        Mockito.lenient().when(rs.getInt(2)).thenReturn(2);
        when(rs.getDouble(3)).thenReturn(PRICE_TEST);
        Mockito.lenient().when(rs.getTimestamp(4)).thenReturn(new Timestamp(DateUtil.getDateNow().getTime()));
        Mockito.lenient().when(rs.getTimestamp(5)).thenReturn(null);
        when(rs.getString(6)).thenReturn(NAME_CAR_TYPE_TEST);

        // when :
        Ticket ticket = ticketDAO.getTicket(VEHICULE_REG_NUMBER_TEST);

        // then : 
        assertThat(ticket).isNotNull();
        verify(ps, Mockito.times(1)).executeQuery();
    }

    @Test
    public void updateTicketWithSuccess() throws Exception {
        // given :
        // ticket to update.
        Ticket ticket = new Ticket();
        ticket.setPrice(PRICE_TEST);
        ticket.setOutTime(DateUtil.getDateNow());
        ticket.setId(1);
        // execution must succeed.
        when(ps.execute()).thenReturn(true);

        // when : 
        boolean result = ticketDAO.updateTicket(ticket);

        // then : 
        assertThat(result).isTrue();
        verify(ps, Mockito.times(1)).execute();
    }

    @Test
    public void getNbTicketWithSuccess() throws Exception {
        // given : 
        // there are one or more tickets.
        when(rs.next()).thenReturn(true);
        when(rs.getInt("total")).thenReturn(NB_TICKET_TEST);
        // execution must succeed.
        when(ps.executeQuery()).thenReturn(rs);

        // when : 
        int count = ticketDAO.getNbTicket(VEHICULE_REG_NUMBER_TEST);

        // then : 
        assertThat(count).isEqualTo(NB_TICKET_TEST);
        verify(ps, Mockito.times(1)).executeQuery();
    }
}
