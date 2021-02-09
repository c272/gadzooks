/**
 * Represents a single ray cast onto an object within Gadzooks.
 */
public class Raycast {
    public Vector2f Ray;
    public float Distance;
    public float Angle;
    public boolean IsVerticalHit;

    /**
     * Creates a single ray, given the ray vector and distance from start point.
     * @param ray The ray vector that has been cast.
     * @param dist The distance from the source of the ray to the endpoint.
     * @param angle The world space angle of the ray.
     * @param verticalHit Whether this ray hit the vertical side of a grid square or not. Used for lighting calculation.
     */
    public Raycast(Vector2f ray, float dist, float angle, boolean verticalHit)
    {
        Ray = ray;
        Distance = dist;
        Angle = angle;
        IsVerticalHit = verticalHit;
    }
}
