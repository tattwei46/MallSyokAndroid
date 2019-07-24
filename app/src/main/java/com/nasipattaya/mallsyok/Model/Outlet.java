package com.nasipattaya.mallsyok.Model;

/**
 * Created by davidcheah on 29/1/18.
 */

public class Outlet {
    private String outletName, category, floorNumber, unitNumber, contactNumber, roundLeft, roundTop;

    public String getRoundLeft() {
        return roundLeft;
    }

    public void setRoundLeft(String roundLeft) {
        this.roundLeft = roundLeft;
    }

    public String getRoundTop() {
        return roundTop;
    }

    public void setRoundTop(String roundTop) {
        this.roundTop = roundTop;
    }

    public Outlet(){

    }

    public String getOutletName() {
        return outletName;
    }

    public String getCategory() {
        return category;
    }

    public String getFloorNumber() {
        return floorNumber;
    }

    public String getUnitNumber() {
        return unitNumber;
    }

    public String getContactNumber() {
        return contactNumber;
    }
}
