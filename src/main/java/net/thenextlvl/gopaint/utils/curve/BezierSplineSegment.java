/*
 * goPaint is designed to simplify painting inside of Minecraft.
 * Copyright (C) Arcaniax-Development
 * Copyright (C) Arcaniax team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.thenextlvl.gopaint.utils.curve;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Getter
@Setter
public class BezierSplineSegment {

    private final double[] lengths = new double[20];
    private Location p0, p1, p2, p3;
    private float a, b, c;
    private @Nullable Double xFlat, yFlat, zFlat;
    private Location r;
    private double curveLength;

    public BezierSplineSegment(Location p0, Location p3) {
        this.p0 = p0;
        this.p3 = p3;
        p1 = new Location(p0.getWorld(), 0, 0, 0);
        p2 = new Location(p0.getWorld(), 0, 0, 0);
        r = new Location(p0.getWorld(), 0, 0, 0);
    }

    public void setX(double xflat2) {
        p0.setX(xflat2);
        p1.setX(xflat2);
        p2.setX(xflat2);
        p3.setX(xflat2);
        xFlat = xflat2;
    }

    public void setY(double yflat2) {
        p0.setY(yflat2);
        p1.setY(yflat2);
        p2.setY(yflat2);
        p3.setY(yflat2);
        yFlat = yflat2;
    }

    public void setZ(double zflat2) {
        p0.setZ(zflat2);
        p1.setZ(zflat2);
        p2.setZ(zflat2);
        p3.setZ(zflat2);
        zFlat = zflat2;
    }

    public void calculateCurveLength() {
        Location current = p0.clone();
        double step = 0.05;
        lengths[0] = 0;
        Location temp;
        for (int i = 1; i < 20; i++) {
            temp = getPoint(i * step);
            lengths[i] = lengths[i - 1] + temp.distance(current);
            current = temp;
        }
        curveLength = lengths[19];
    }

    public double getdXdT(double t) {
        assert (t >= 0);
        assert (t <= 1);
        return 3 * (1 - t) * (1 - t) * (p1.getX() - p0.getX()) + 6 * (1 - t) * t
                                                                 * (p2.getX() - p1.getX()) + 3 * t * t * (p3.getX() - p2.getX());
    }

    public double getdYdT(double t) {
        assert (t <= 1);
        return 3 * (1 - t) * (1 - t) * (p1.getY() - p0.getY()) + 6 * (1 - t) * t
                                                                 * (p2.getY() - p1.getY()) + 3 * t * t * (p3.getY() - p2.getY());
    }

    public double getdZdT(double t) {
        assert (t >= 0);
        assert (t <= 1);
        return 3 * (1 - t) * (1 - t) * (p1.getZ() - p0.getZ()) + 6 * (1 - t) * t
                                                                 * (p2.getZ() - p1.getZ()) + 3 * t * t * (p3.getZ() - p2.getZ());
    }

    public double getdTdS(double t) {
        double dZdT = getdZdT(t);
        double dXdT = getdXdT(t);
        double dYdT = getdYdT(t);
        return 1 / Math.sqrt(dZdT * dZdT + dXdT * dXdT + dYdT * dYdT);
    }

    public double getHAngle(double t) {
        // Positive x is 0, positive z is pi/2, negative x is pi, negative z is
        // 3*pi/2
        double dZdT = getdZdT(t);
        double dXdT = getdXdT(t);
        if (dXdT == 0) {
            if (dZdT < 0) {
                return Math.PI / 2;
            } else {
                return -Math.PI / 2;
            }
        }

        if (dXdT < 0) {
            return Math.PI + Math.atan(dZdT / dXdT);
        }
        return Math.atan(dZdT / dXdT);
    }

    public double getT(double d) {
        assert (d >= 0);
        assert (d <= curveLength);
        if (d == 0) {
            return 0;
        }
        if (d == curveLength) {
            return 1;
        }
        int i;
        for (i = 0; i < 20; i++) {
            if (d == lengths[i]) {
                return i / 19d;
            }
            if (d < lengths[i]) {
                break;
            }
        }
        return (i + (d - lengths[i - 1]) / (lengths[i] - lengths[i - 1])) / 20;
    }

    public Location getPoint(double f) {
        Location result = new Location(p0.getWorld(), 0, 0, 0);
        result.setX(Objects.requireNonNullElseGet(xFlat, () -> (Math.pow(1 - f, 3) * p0.getX())
                                                               + (3 * Math.pow(1 - f, 2) * f * p1.getX())
                                                               + (3 * (1 - f) * f * f * p2.getX()) + (Math.pow(f, 3) * p3.getX())));
        result.setY(Objects.requireNonNullElseGet(yFlat, () -> (Math.pow(1 - f, 3) * p0.getY())
                                                               + (3 * Math.pow(1 - f, 2) * f * p1.getY())
                                                               + (3 * (1 - f) * f * f * p2.getY()) + (Math.pow(f, 3) * p3.getY())));
        result.setZ(Objects.requireNonNullElseGet(zFlat, () -> (Math.pow(1 - f, 3) * p0.getZ())
                                                               + (3 * Math.pow(1 - f, 2) * f * p1.getZ())
                                                               + (3 * (1 - f) * f * f * p2.getZ()) + (Math.pow(f, 3) * p3.getZ())));
        return result;
    }

    public double getLinearLength() {
        return p0.distance(p3);
    }

}
