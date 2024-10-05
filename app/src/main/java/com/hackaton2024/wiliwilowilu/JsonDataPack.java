package com.hackaton2024.wiliwilowilu;

import java.util.Random;

public class JsonDataPack {
    private double temperature, airHumidity, soilHumidity, luminosity, pressure;


    public double getTemperature() {
        return temperature;
    }

    public double getAirHumidity() {
        return airHumidity;
    }

    public double getSoilHumidity() {
        return soilHumidity;
    }

    public double getLuminosity() {
        return luminosity;
    }

    public double getPressure() {
        return pressure;
    }

    public double getpH() {
        Random rnd = new Random();

        return rnd.nextDouble() * 15;
    }
}
