/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import facades.PersonFacade;
import javax.persistence.EntityManagerFactory;
import utils.EMF_Creator;

/**
 *
 * @author Bitten
 */
public class DevData {

    public static void main(String[] args) {
        EntityManagerFactory EMF = EMF_Creator.createEntityManagerFactory(
                "pu",
                "jdbc:mysql://localhost:3307/w5_03persons",
                "dev",
                "ax2",
                EMF_Creator.Strategy.DROP_AND_CREATE);
        PersonFacade FACADE = PersonFacade.getPersonFacade(EMF);
        
        FACADE.addPerson("Peter", "Jakobsen", "1234");
        FACADE.addPerson("Annike", "Ehlers", "4321");
        FACADE.addPerson("Hand", "Hansen", "9876");
    }

}
