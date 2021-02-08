import java.awt.*;
import java.util.UUID;

public abstract class GadzookRenderer {

    /**
     * ID of this renderer instance.
     */
    public int ID = UUID.randomUUID().hashCode();

    /**
     * The render priority of this renderer.
     * Higher is later.
     */
    public int RenderPriority;

    /**
     * Called when it is this renderer's turn to draw to the screen.
     * @param graphics The graphics instance to draw with.
     */
    public abstract void Draw(Graphics graphics);

    /**
     * Returns whether this renderer is the same instance as the one provided.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GadzookRenderer)) { return false; }
        return ((GadzookRenderer)obj).ID == ID;
    }
}
