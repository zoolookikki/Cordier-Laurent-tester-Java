package com.parkit.parkingsystem.model;

import com.parkit.parkingsystem.constants.ParkingType;

public class ParkingSpot {
    private int id;
    private ParkingType parkingType;
    private boolean isAvailable;

    public ParkingSpot(int id, ParkingType parkingType, boolean isAvailable) {
        if (parkingType == null) {
            throw new IllegalArgumentException("ParkingType cannot be null");
        }        
        this.id = id;
        this.parkingType = parkingType;
        this.isAvailable = isAvailable;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ParkingType getParkingType() {
        return parkingType;
    }

    public void setParkingType(ParkingType parkingType) {
        this.parkingType = parkingType;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingSpot that = (ParkingSpot) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
