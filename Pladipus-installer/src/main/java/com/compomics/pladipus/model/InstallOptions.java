package com.compomics.pladipus.model;

import java.awt.*;

/**
 * Created by Davy Maddelein on 9/27/2016.
 */
public enum InstallOptions {

    MANAGER(new Component[]{new com.compomics.pladipus.view.MySQLPanel(),new com.compomics.pladipus.view.ActiveMQPanel(),new com.compomics.pladipus.view.PladipusPanel()}),
    WORKER(new Component[]{new com.compomics.pladipus.view.PladipusPanel()}),
    STANDALONE(new Component[]{new com.compomics.pladipus.view.MySQLPanel(),new com.compomics.pladipus.view.ActiveMQPanel(),new com.compomics.pladipus.view.PladipusPanel()});


    Component[] cardsToShow;

    InstallOptions(Component[] cards){
        cardsToShow = cards;
    }


    public Component[] getCardsForOption(){
        return cardsToShow;
    }
}
