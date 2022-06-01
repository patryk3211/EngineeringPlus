package com.patryk3211.engineeringplus.element;

public interface IPressureProvider {
    /**
     * Calculate element pressure from provided parameters
     * @param amount Amount [g]
     * @param volume Volume [m^3]
     * @param temperature Temperature [K]
     * @return Pressure [Pa]
     */
    int getPressure(int amount, float volume, float temperature);
}
