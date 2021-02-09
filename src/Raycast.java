/**
 * Represents a single ray cast onto an object within Gadzooks.
 */
public class Raycast {

    //
    public float Distance;
    public float Angle;
    public boolean IsVerticalHit;

    //The destination and source of the ray.
    public Vector2f Destination;
    public Vector2f Source;

    /**
     * Creates a single ray, given the ray vector and distance from start point.
     * @param source The source of the ray.
     * @param dest The destination of the ray.
     * @param dist The distance from the source of the ray to the endpoint.
     * @param angle The world space angle of the ray.
     * @param verticalHit Whether this ray hit the vertical side of a grid square or not. Used for lighting calculation.
     */
    public Raycast(Vector2f source, Vector2f dest, float dist, float angle, boolean verticalHit)
    {
        Source = source;
        Destination = dest;
        Distance = dist;
        Angle = angle;
        IsVerticalHit = verticalHit;
    }
}
