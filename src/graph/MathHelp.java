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
}
