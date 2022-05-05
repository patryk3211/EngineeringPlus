package com.patryk3211.engineeringplus.capabilities.kinetic;

import com.patryk3211.engineeringplus.kinetic.KineticNetwork;

public interface IKineticHandler {
    class NetworkReference {
        public KineticNetwork network;
        public float speedMultiplier;
        public float angleOffset;

        public NetworkReference(KineticNetwork network, float speedMultiplier, float angleOffset) {
            this.network = network;
            this.speedMultiplier = speedMultiplier;
            this.angleOffset = angleOffset;
        }
    }

    float getSpeed();
    float getAngle();

    float getSpeedMultiplier();
    float getAngleOffset();
    float getInertia();

    void applyForce(float force);
    float calculateForce(float targetSpeed);

    void setNetwork(NetworkReference network);
    NetworkReference getNetwork();
}
