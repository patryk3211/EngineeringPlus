package com.patryk3211.engineeringplus.capabilities.kinetic;

import com.patryk3211.engineeringplus.kinetic.IKineticNetwork;

public interface IKineticHandler {
    /**
     * Get the rotational velocity of this handler
     * @return Speed [1 rpm]
     */
    float getSpeed();

    /**
     * Get the current angle of this handler
     * @return Angle [1 deg]
     */
    float getAngle();

    /**
     * Get speed multiplier of this handler (relative to it's network)
     * @return Speed multiplier
     */
    float getSpeedMultiplier();

    /**
     * Get angle offset of this handler (relative to it's network)
     * @return Angle offset
     */
    float getAngleOffset();

    /**
     * Get the inertial mass of this handler
     * @return Mass [1 kg]
     */
    float getInertia();

    /**
     * Get friction of this handler (This will be applied as a counter force to the network)
     * @return Friction [1 rpm/s * kg]
     */
    float getFriction();

    /**
     * Apply a given amount of force for 1 tick
     * @param force Amount of force [1 rpm/s * kg]
     */
    void applyForce(float force);

    /**
     * Calculate the force needed to accelerate/decelerate this network to the given targetSpeed
     * @param targetSpeed Speed [1 rpm]
     * @return Force [1 rpm/s * kg]
     */
    float calculateForce(float targetSpeed);

    void setNetwork(IKineticNetwork network);
    IKineticNetwork getNetwork();

    void setParameters(float speedMultiplier, float angleOffset);
    void offsetParameters(float speedMultiplier, float angleOffset);

    boolean areConnected(IKineticHandler other);
}
