package com.patryk3211.engineeringplus.capabilities.kinetic;

import com.patryk3211.engineeringplus.kinetic.IKineticNetwork;

public class EntangledKineticHandler implements IKineticHandler {
    public static class KineticHandlerDataStore {
        private IKineticNetwork network;
        private float speedMultiplier;
        private float angleOffset;
    }

    private final KineticHandlerDataStore store;
    private final float speedMultiplier;
    private final float angleOffset;
    private final float inertialMass;

    public EntangledKineticHandler(KineticHandlerDataStore store, float speedMultiplier, float angleOffset, float inertialMass) {
        this.store = store;
        this.speedMultiplier = speedMultiplier;
        this.angleOffset = angleOffset;
        this.inertialMass = inertialMass;
    }

    @Override
    public float getSpeed() {
        return store.network.getSpeed() * speedMultiplier * store.speedMultiplier;
    }

    @Override
    public float getAngle() {
        return ((store.network.getAngle() * speedMultiplier) + angleOffset) % 360;
    }

    @Override
    public float getSpeedMultiplier() {
        return speedMultiplier * store.speedMultiplier;
    }

    @Override
    public float getAngleOffset() {
        return angleOffset * store.angleOffset;
    }

    @Override
    public float getInertia() {
        return inertialMass;
    }

    @Override
    public void applyForce(float force) {
        float localInertia = store.network.getInertia() / speedMultiplier;
        store.network.changeSpeed(force / localInertia * 0.05f);
    }

    @Override
    public float calculateForce(float targetSpeed) {
        float localInertia = store.network.getInertia() / speedMultiplier;
        float speedDelta = targetSpeed - store.network.getSpeed();
        return speedDelta * localInertia * 20f;
    }

    @Override
    public void setNetwork(IKineticNetwork network) {
        store.network = network;
    }

    @Override
    public IKineticNetwork getNetwork() {
        return store.network;
    }

    @Override
    public void setParameters(float speedMultiplier, float angleOffset) {
        store.speedMultiplier = speedMultiplier / this.speedMultiplier;
        store.angleOffset = angleOffset - this.angleOffset;
    }

    @Override
    public void offsetParameters(float speedMultiplier, float angleOffset) {
        store.speedMultiplier *= speedMultiplier;
        store.angleOffset += angleOffset;
    }

    @Override
    public boolean areConnected(IKineticHandler other) {
        if(other instanceof EntangledKineticHandler) return ((EntangledKineticHandler) other).store.equals(this.store);
        return other.equals(this);
    }
}
