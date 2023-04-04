package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ParkingLotRepository;
import com.driver.repository.ReservationRepository;
import com.driver.repository.SpotRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {
    @Autowired
    UserRepository userRepository3;
    @Autowired
    SpotRepository spotRepository3;
    @Autowired
    ReservationRepository reservationRepository3;
    @Autowired
    ParkingLotRepository parkingLotRepository3;
    @Override
    public Reservation reserveSpot(Integer userId, Integer parkingLotId, Integer timeInHours, Integer numberOfWheels) throws Exception {
         //Reserve a spot in the given parkingLot such that the total price is minimum. Note that the price per hour for each spot is different
        //Note that the vehicle can only be parked in a spot having a type equal to or larger than given vehicle
        //If parkingLot is not found, user is not found, or no spot is available, throw "Cannot make reservation" exception.
        User user;
        ParkingLot parkingLot;
        Spot spotChosen = null;
        try{
            user = userRepository3.findById(userId).get();
            parkingLot = parkingLotRepository3.findById(parkingLotId).get();
        }catch (Exception e){
            throw new Exception("Cannot make reservation");
        }
        List<Spot> spotList = parkingLot.getSpotList();
        boolean check = false;
        for(Spot spot1 : spotList){
            if(!spot1.getOccupied()) {
                check = true;
                break;
            }
        }
        if(!check) throw new Exception("Cannot make reservation");
        SpotType requestSpotType;
        if(numberOfWheels > 4) requestSpotType = SpotType.OTHERS;
        else if(numberOfWheels > 2) requestSpotType = SpotType.FOUR_WHEELER;
        else requestSpotType = SpotType.TWO_WHEELER;

        int minPrice = Integer.MAX_VALUE;
        check = false;
        for (Spot spot : spotList){
            if(requestSpotType.equals(SpotType.OTHERS) && spot.getSpotType().equals(SpotType.OTHERS)){
                if (spot.getPricePerHour() * timeInHours < minPrice && !spot.getOccupied()){
                    minPrice = spot.getPricePerHour() * timeInHours;
                    check = true;
                    spotChosen = spot;
                }
            } else if (requestSpotType.equals(SpotType.FOUR_WHEELER) && spot.getSpotType().equals(SpotType.OTHERS) ||
                    spot.getSpotType().equals(SpotType.FOUR_WHEELER)) {
                if(spot.getPricePerHour() * timeInHours < minPrice && !spot.getOccupied()){
                    minPrice = spot.getPricePerHour() * timeInHours;
                    check = true;
                    spotChosen = spot;
                }
            } else if (requestSpotType.equals(SpotType.TWO_WHEELER) && spot.getSpotType().equals(SpotType.OTHERS) ||
                    spot.getSpotType().equals(SpotType.FOUR_WHEELER) || spot.getSpotType().equals(SpotType.TWO_WHEELER)) {
                if(spot.getPricePerHour() * timeInHours < minPrice && !spot.getOccupied()){
                    minPrice = spot.getPricePerHour() * timeInHours;
                    check = true;
                    spotChosen = spot;
                }
            }
        }
        if(!check){
            throw  new Exception("Cannot make reservation");
        }
        assert spotChosen != null;
        spotChosen.setOccupied(true);

        Reservation reservation = new Reservation();
        reservation.setNumberOfHours(timeInHours);
        reservation.setSpot(spotChosen);
        reservation.setUser(user);

        spotChosen.getReservationList().add(reservation);
        user.getReservationList().add(reservation);

        userRepository3.save(user);
        spotRepository3.save(spotChosen);
        return reservation;
    }
}
