package com.patryk3211.engineeringplus.kinetic;

import java.util.UUID;

public interface IKineticNetwork {
    float getSpeed();
    float getAngle();
    float getInertia();

    void changeSpeed(float amount);

    UUID getId();
}
