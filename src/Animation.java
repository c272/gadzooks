/**
 * Represents a simple animation within RectInvaders.
 * Animations should not fall onto a succeeding line.
 */
public class Animation {
    public Vector2 StartPosition;
    public Vector2 FrameSize;
    public int NumFrames;
    public float FPS;
    public boolean Loops;

    public Animation(Vector2 startPos, Vector2 frameSize, int numFrame, float fps, boolean looping)
    {
        StartPosition = startPos;
        FrameSize = frameSize;
        NumFrames = numFrame;
        FPS = fps;
        Loops = looping;
    }
}
