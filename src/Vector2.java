/**
 * Represents a single vector with an X and Y direction in Rectinvaders.
 */
public class Vector2
{
    //X and Y of the vector.
    public int X;
    public int Y;

    /**
     * Constructor for the vector with basic integer parameters.
     * @param x The X coordinate of the vector.
     * @param y The Y coordinate of the vector.
     */
    public Vector2(int x, int y) {
        X = x;
        Y = y;
    }

    /**
     * Constructor for copying another Vector2.
     * @param toCopy The vector to copy.
     */
    public Vector2(Vector2 toCopy)
    {
        X = toCopy.X;
        Y = toCopy.Y;
    }

    /**
     * Equals override for vectors.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) { return false; }
        var other = (Vector2)obj;
        return other.X == X && other.Y == Y;
    }
}
