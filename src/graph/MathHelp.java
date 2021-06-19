package graph;

public class MathHelp {
    public static int clamp(int value, int low, int high){
        return Math.max(low, Math.min(value, high));
    }
    public static double clamp(double value, double low, double high){
        return Math.max(low, Math.min(value, high));
    }

    public static boolean isBetween(int value, int low, int high){
        return value >= low && value <= high;
    }
    public static boolean isBetween(double value, double low, double high){
        return value >= low && value <= high;
    }

    public static double xFromPolar(double magnitude, double radians){
        return magnitude*Math.cos(radians);
    }
    public static double yFromPolar(double magnitude, double radians){
        return magnitude*Math.sin(radians);
    }

    public static double angleDiffRadians(double from, double to){
        double diff = to - from;
        diff += Math.PI;
        diff = (diff % (2*Math.PI) + (2*Math.PI)) % (2*Math.PI);
        diff -= Math.PI;
        return diff;
    }
    public static double clampAngleRadians(double angle){
        return angleDiffRadians(0, angle);
    }
}
