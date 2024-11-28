package com.parkit.parkingsystem.integration.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.DateUtil;
import com.parkit.parkingsystem.util.MonetaryUtil;

import java.util.Date;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

public class DataBasePrepareService {

    DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

    public void clearDataBaseEntries(){
        Connection connection = null;
        try{
            connection = dataBaseTestConfig.getConnection();

            //set parking entries to available
            connection.prepareStatement("UPDATE parking SET available = TRUE").execute();

            //clear ticket entries;
            connection.prepareStatement("TRUNCATE TABLE ticket").execute();

        }catch(Exception e){
            e.printStackTrace();
        }finally {
            dataBaseTestConfig.closeConnection(connection);
        }
    }

    public boolean updateOneTicketInTime(Ticket ticket, Date inTime) {
        if (ticket == null || ticket.getId() <= 0 || inTime == null) {
            throw new IllegalArgumentException("Invalid argument : Ticket or inTime cannot be null or invalid");
        }

        Connection connection = null;
        PreparedStatement ps = null;
        try{
            connection = dataBaseTestConfig.getConnection();

            // changes the entry time of a ticket.
            ps = connection.prepareStatement("UPDATE ticket SET in_time = ? WHERE id = ?");
            ps.setTimestamp(1, new Timestamp(inTime.getTime()));
            ps.setInt(2,ticket.getId());                    
            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated == 1;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }finally {
            dataBaseTestConfig.closeConnection(connection);
        }
    }
    
    public void setParkingFull() {
        Connection connection = null;
        try{
            connection = dataBaseTestConfig.getConnection();

            //set parking entries to not available.
            connection.prepareStatement("UPDATE parking SET available = FALSE").execute();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            dataBaseTestConfig.closeConnection(connection);
        }
    }
    
    public boolean putOldTicket(String vehicleRegNumber) {
        if (vehicleRegNumber == null || vehicleRegNumber.isEmpty()) {
            throw new IllegalArgumentException("Invalid argument : vehiculeRegNumber cannot be null or empty");
        }
        Connection connection = null;
        PreparedStatement ps = null;
        try{
            connection = dataBaseTestConfig.getConnection();

            //insert a old ticket for a registration number.
            ps = connection.prepareStatement("INSERT INTO ticket(PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME) values(?,?,?,?,?)");
            ps.setInt(1,1);
            ps.setString(2, vehicleRegNumber);
            ps.setDouble(3, MonetaryUtil.round(3 * Fare.CAR_RATE_PER_HOUR));
            ps.setTimestamp(4, new Timestamp(DateUtil.getPastDate(240).getTime()));
            ps.setTimestamp(5, new Timestamp(DateUtil.getPastDate(60).getTime()));
            return ps.execute();
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }finally {
            dataBaseTestConfig.closeConnection(connection);
        }
    }
}
