package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.MonetaryUtil;

public class FareCalculatorService {

    private double getHourlyRate (ParkingType type, boolean discount) {
        double result = 0 ;
        
        switch (type){
            case CAR: {
                result = Fare.CAR_RATE_PER_HOUR;
                break;
            }
            case BIKE: {
                result = Fare.BIKE_RATE_PER_HOUR;
            break;
            }
           default: throw new IllegalArgumentException("Unkown Parking Type");
        }
        // 5% discount
        if (discount) {
            result *= Fare.RECURRING_USER_DISCOUNT;
        }
        
        return MonetaryUtil.round(result);
    }
    
    // separated into several instructions for easier reading.
    private double calculateTotalPrice (Ticket ticket, double decimalHour, boolean discount) {
        ParkingType parkingType = ticket.getParkingSpot().getParkingType();
        double hourlyRate = getHourlyRate (parkingType, discount);
        return MonetaryUtil.round(decimalHour * hourlyRate);
    }
    
    public void calculateFare(Ticket ticket, boolean discount){
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket cannot be null");
        }
        if ((ticket.getOutTime() == null) || (ticket.getInTime() == null)) {
            // do not display the time because ticket is null.
            throw new IllegalArgumentException("Out time or in time is incorrect"); // +ticket.getOutTime().toString()
        }
        if (ticket.getOutTime().before(ticket.getInTime())){
            // do not display the time because ticket is null
            throw new IllegalArgumentException("Out time is before in time"); 
        }

        double decimalHour = (double) (ticket.getOutTime().getTime() - ticket.getInTime().getTime()) / (60 * 60 * 1000);
        
        // less than 30 minutes, it's free.
        if (decimalHour < 0.5) {
            ticket.setPrice(0) ;
        } else {
            ticket.setPrice(calculateTotalPrice (ticket, decimalHour, discount));
        }
    }

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }
    
}