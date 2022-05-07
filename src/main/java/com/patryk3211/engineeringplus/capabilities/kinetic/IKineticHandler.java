package com.patryk3211.engineeringplus.capabilities.kinetic;

import com.patryk3211.engineeringplus.kinetic.IKineticNetwork;

public interface IKineticHandler {
    abstract class NetworkReference {
        public float speedMultiplier;
        public float angleOffset;

        public NetworkReference(float speedMultiplier, float angleOffset) {
            this.speedMultiplier = speedMultiplier;
            this.angleOffset = angleOffset;
        }

        public void offset(float speedMultiplier, float angleOffset) {
            this.speedMultiplier *= speedMultiplier;
            this.angleOffset += angleOffset;
        }

        public abstract IKineticNetwork getNetwork();
    }

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

    float getSpeedMultiplier();
    float getAngleOffset();

    /**
     * Get the inertial mass of this handler
     * @return Mass [1 kg]
     */
    float getInertia();

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
    NetworkReference getNetworkReference();
}
