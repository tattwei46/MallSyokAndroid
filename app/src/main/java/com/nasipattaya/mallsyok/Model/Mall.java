package com.nasipattaya.mallsyok.Model;

import android.support.annotation.NonNull;

/**
 * Created by davidcheah on 24/1/18.
 */

public class Mall {

    public Mall() {
    }


    public Mall(String mallName, String category) {
        this.mallName = mallName;
    }

    private String mallName;

    private String mallAddress, mallPhone, mallWebsite;

    private String parkingWeekday, parkingWeekend, parkingLost;

    private String directionRail, directionService, directionBus;

    private String openingHours;

    private String key;

    private String listMaps;

    public String getListMaps() {
        return listMaps;
    }

    public String getMallAddress() {
        return mallAddress;
    }

    public String getMallPhone() {
        return mallPhone;
    }

    public String getMallWebsite(){
        return mallWebsite;
    }

    public String getParkingWeekday() { return parkingWeekday;}

    public String getParkingWeekend() { return parkingWeekend;}

    public String getParkingLost() { return parkingLost;}

    public String getDirectionRail() { return directionRail;}

    public String getDirectionService() { return directionService;}

    public String getDirectionBus() { return directionBus;}

    public String getOpeningHours() { return openingHours;}

    public String getMallName() {
        return mallName;
    }

    public void setMallName(String mallName) {
        this.mallName = mallName;
    }
}
