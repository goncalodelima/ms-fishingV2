package com.minecraftsolutions.fishing.controller.user.exception;

public class LocationException extends Exception {

    public LocationException(){
        super("There is no spawn or exit");
    }

}
