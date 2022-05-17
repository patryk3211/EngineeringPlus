package com.patryk3211.engineeringplus.capabilities.kinetic;

import com.patryk3211.engineeringplus.kinetic.IKineticNetwork;

public class BasicKineticHandler implements IKineticHandler {
    private IKineticNetwork network;
    private final float inertialMass;

    private final float friction;
    private float speedMultiplier = 1;
    private float angleOffset = 0;

    public BasicKineticHandler(float inertialMass, float friction) {
        this.inertialMass = inertialMass;
        this.friction = friction;
    }

    @Override
    public float getSpeed() {
        if(network == null) return 0;
        return network.getSpeed() * speedMultiplier;
    }

    @Override
    public float getAngle() {
        if(network == null) return 0;
        return ((network.getAngle() * speedMultiplier) + angleOffset) % 360;
    }

    @Override
    public float getSpeedMultiplier() {
        return speedMultiplier;
    }

    @Override
    public float getAngleOffset() {
        return angleOffset;
    }

    @Override
    public float getInertia() {
        return inertialMass;
    }

    @Override
    public float getFriction() {
        return friction;
    }

    @Override
    public void applyForce(float force) {
        if(network == null) return;
        float localInertia = network.getInertia() / speedMultiplier;
        network.changeSpeed(force / localInertia * 0.05f); // Force / mass * 1/20 second (1 tick)
    }

    @Override
    public float calculateForce(float targetSpeed) {
        if(network == null) return 0;
        float localInertia = network.getInertia() / speedMultiplier;
        float speedDelta = targetSpeed - network.getSpeed();
        return speedDelta * localInertia * 20f;
    }

    @Override
    public void setNetwork(IKineticNetwork network) {
        this.network = network;
    }

    @Override
    public IKineticNetwork getNetwork() {
        return network;
    }

    @Override
    public void setParameters(float speedMultiplier, float angleOffset) {
        this.speedMultiplier = speedMultiplier;
        this.angleOffset = angleOffset;
    }

    @Override
    public void offsetParameters(float speedMultiplier, float angleOffset) {
        this.speedMultiplier *= speedMultiplier;
        this.angleOffset += angleOffset;
    }

    @Override
    public boolean areConnected(IKineticHandler other) {
        return other.equals(this);
    }
}
