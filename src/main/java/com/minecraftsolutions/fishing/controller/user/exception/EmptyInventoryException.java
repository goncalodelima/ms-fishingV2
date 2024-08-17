package com.minecraftsolutions.fishing.controller.user.exception;

public class EmptyInventoryException extends Exception{

    public EmptyInventoryException(){
        super("Inventory is not empty");
    }

}
