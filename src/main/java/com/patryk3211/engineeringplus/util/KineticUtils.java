package com.patryk3211.engineeringplus.util;

import com.patryk3211.engineeringplus.capabilities.kinetic.IKineticHandler;

public class KineticUtils {
    /**
     * Normalizes the given angle to a value between 0 and 360
     * @param angle Angle [deg]
     * @return Angle [deg]
     */
    public static float normalizeAngle(float angle) {
        return angle >= 0 ? angle % 360f : 360f + (angle % 360f);
    }

    /**
     * Calculates the signed distance between two angles
     * @param angle1 Angle [deg]
     * @param angle2 Angle [deg]
     * @return Distance [deg]
     */
    public static float signedAngleDistance(float angle1, float angle2) {
        float distance = normalizeAngle(angle2 - angle1);
        return distance > 180f ? distance - 360f : distance;
    }

    /**
     * Calculates the force required to accelerate the handler to a speed in which it would achieve
     * the given angle in the time of 1 second
     * @param angle Angle [deg]
     * @param handler Kinetic handler
     * @return Force [rpm/s * kg]
     */
    public static float calculateForceForAngle(float angle, IKineticHandler handler) {
        float targetSpeed = (angle - handler.getAngle()) / 360f * 60f;
        return handler.calculateForce(targetSpeed);
    }
}
