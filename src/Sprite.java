import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/** Represents a single sprite made up of a pixel grid on the GameArena. **/
public class Sprite extends GameObject {

    //Whether this sprite should use pixel accurate collision.
    public boolean PixelAccurateCollision = false;

    //The current top left position of this sprite.
    Vector2 position = new Vector2(0, 0);

    //The current position of the sprite relative to the anchor.
    Vector2 anchorRelativePosition = new Vector2(0, 0);

    //The size (square) of each screen-pixel on this sprite.
    float scale;

    //The base image of this sprite.
    BufferedImage baseImage;

    //The current animation frame of this sprite.
    BufferedImage frameImage;

    //The scaled version of this sprite.
    BufferedImage image;

    //The anchor (root position) of this sprite.
    SpriteAnchor anchor = SpriteAnchor.TopLeft;

    //The animations on this sprite.
    HashMap<String, Animation> animations;

    //Whether this sprite is currently active on the game arena.
    boolean addedToGame = false;

    //Animation tracking variables.
    Animation animPlaying = null;
    int animFrame = -1;
    int lastAnimFrame = -1;
    float timeSinceLastFrame = 0;

    //Movement tracking variables.
    Vector2 targetPosition = null;
    float curX; float curY;
    Vector2 startPosition = null;
    float moveTime = -1;
    float moveTimeElapsed = 0;
    MoveStyle moveStyle = MoveStyle.Linear;

    /**
     * Constructor which creates a sprite based on a file path to an image.
     * @param filePath The file to load the sprite image from.
     * @param scale_ The scale of the sprite.
     * @param anchor_ The anchor position of the sprite (where the pivot is).
     */
    public Sprite(String filePath, float scale_, SpriteAnchor anchor_) throws IOException {
        this(ImageIO.read(new File(filePath)), scale_, anchor_);
    }

    /**
     * Constructor, must be passed a buffered image to use as the sprite representation.
     * Pixel size should be passed in >0 values.
     * @param img The image to load as the sprite texture.
     * @param scale_ The scale of the sprite.
     */
    public Sprite(BufferedImage img, float scale_) {
        this(img, scale_, SpriteAnchor.TopLeft);
    }

    /**
     * Constructor, must be passed a buffered image to use as the sprite representation.
     * Pixel size should be passed in >0 values.
     * Also takes an anchor for the sprite.
     * @param img The image to load as the sprite texture.
     * @param scale_ The scale of the sprite.
     * @param anchor_ The anchor position of the sprite (where the pivot is).
     */
    public Sprite(BufferedImage img, float scale_, SpriteAnchor anchor_)
    {
        //Set local parameters.
        image = img;
        baseImage = img;
        frameImage = img;

        //Set the anchor.
        anchor = anchor_;

        //Re-scale. This automatically moves to the new anchor, no need to call again.
        ChangeScale(scale_);
    }

    /**
     * Tick function for the sprite.
     * Updates animation frames on the sprite.
     */
    @Override
    public void Tick(float delta)
    {
        //Update the animations.
        TickAnimation(delta);

        //Update the movement.
        TickMovement(delta);
    }

    /**
     * Updates the movement of this sprite every update tick.
     */
    private void TickMovement(float delta) {
        //If there's a target, continue.
        if (targetPosition == null) { return; }

        //Increment the elapsed time.
        moveTimeElapsed += delta;

        //If the movement has finished, lock to end position and be done.
        if (moveTimeElapsed >= moveTime)
        {
            MoveTo(targetPosition);
            targetPosition = null;
            return;
        }

        //Move based on the move type.
        switch (moveStyle)
        {
            //Just linear basic movement.
            case Linear:
                curX = startPosition.X + ((targetPosition.X - startPosition.X) / moveTime) * moveTimeElapsed;
                curY = startPosition.Y + ((targetPosition.Y - startPosition.Y) / moveTime) * moveTimeElapsed;
                break;

            case Smoothed:
                float percentDone = moveTimeElapsed / moveTime;
                curX = startPosition.X + QuadraticSmoothed(percentDone) * (targetPosition.X - startPosition.X);
                curY = startPosition.Y + QuadraticSmoothed(percentDone) * (targetPosition.Y - startPosition.Y);
                break;
        }

        //Set the X and Y.
        MoveTo(new Vector2((int)curX, (int)curY));
    }

    /**
     * Uses two quadratic curves to create a smoothed percentage from 0.0 to 1.0.
     * @param time The time between 0 and 1 that has passed.
     * @return The percentage value (0 to 1) on the curve.
     */
    static float QuadraticSmoothed(float time)
    {
        //If under halfway, use 2 * time^2.
        if (time <= 0.5f) {
            return 2.0f * time * time;
        }

        //If over halfway, use a separate black magic formula.
        time -= 0.5f;
        return 2.0f * time * (1.0f - time) + 0.5f;
    }

    /**
     * Updates the animation frame every update tick.
     */
    private void TickAnimation(float delta) {
        //Is an animation playing?
        if (animPlaying != null) {
            //Add another refresh rate portion of a second to the time since last frame.
            timeSinceLastFrame += delta;

            //Has it been enough time to change frame?
            if (timeSinceLastFrame > 1 / animPlaying.FPS)
            {
                animFrame++;
                if (animFrame >= animPlaying.NumFrames)
                {
                    //Reset frame depending on the loop setting.
                    if (animPlaying.Loops) { animFrame = 0; }
                    else { animFrame--; }
                }

                //Set the time since frame to zero.
                timeSinceLastFrame = 0;
            }

            //Has the frame changed since last tick?
            if (animFrame != lastAnimFrame)
            {
                lastAnimFrame = animFrame;

                //Update the frame image.
                frameImage = baseImage.getSubimage(animPlaying.StartPosition.X + animPlaying.FrameSize.X * animFrame, animPlaying.StartPosition.Y, animPlaying.FrameSize.X, animPlaying.FrameSize.Y);

                //Recalculate scaled image.
                ChangeScale(scale);
            }
        }
        else {
            //No frame, set to base image.
            frameImage = baseImage;
        }
    }

    /**
     * Adds a list of animations to this sprite, and plays the provided one (if present).
     * @param anims The animations to add.
     * @param toPlay The animation to play after addition.
     */
    public void AddAnimations(HashMap<String, Animation> anims, String toPlay)
    {
        animations = anims;
        if (toPlay != null)
        {
            PlayAnimation(toPlay);
        }
    }

    /**
     * Plays the given animation on this sprite.
     * @param toPlay The animation to play.
     */
    public void PlayAnimation(String toPlay) {
        //Set tracking variables.
        animPlaying = animations.get(toPlay);
        animFrame = -1;
        lastAnimFrame = -1;
        timeSinceLastFrame = 99999999;
    }

    /**
     * Alters the scale of the sprite to the given parameter.
     * @param scale_ The scale to change to.
     */
    private void ChangeScale(float scale_) {

        //Get a scaled base image class.
        var scaled = frameImage.getScaledInstance((int)(frameImage.getWidth() * scale_), (int)(frameImage.getHeight() * scale_), Image.SCALE_SMOOTH);

        //Recreate the buffered image w/ transparency.
        image = new BufferedImage(scaled.getWidth(null), scaled.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        //Draw image. This is expensive, but we won't be resizing often.
        Graphics2D bGr = image.createGraphics();
        bGr.drawImage(scaled, 0, 0, null);
        bGr.dispose();

        //Set the new scale.
        scale = scale_;

        //Reset the position in case the anchor pos is now invalid.
        MoveTo(anchorRelativePosition);
    }

    /**
     * Gets the position of the sprite based on the current anchor.
     */
    public Vector2 GetPosition()
    {
        return anchorRelativePosition;
    }

    /**
     * Gets the current top left position of the sprite.
     */
    public Vector2 GetTopLeftPosition() {
        return position;
    }

    /**
     * Returns the current image assigned to this sprite.
     */
    public BufferedImage GetImage() {
        return image;
    }

    /**
     * Returns whether this sprite is currently moving or not.
     */
    public boolean IsMoving() { return targetPosition != null; }

    /**
     * Moves the given sprite to the pixel position on screen, over a period of seconds.
     * @param position_ The final position to end at.
     * @param seconds The time (in seconds) to move over.
     */
    public void MoveTo(Vector2 position_, float seconds, MoveStyle moveStyle_)
    {
        targetPosition = position_;
        startPosition = anchorRelativePosition;
        moveTime = seconds;
        moveTimeElapsed = 0;
        moveStyle = moveStyle_;
    }

    /**
     * Moves this sprite to the given pixel position on screen.
     * @param position_ The position to move to.
     */
    public void MoveTo(Vector2 position_) {
        //Set anchor relative position.
        anchorRelativePosition = new Vector2(position_);

        //Correct literal position for centered anchor.
        if (anchor == SpriteAnchor.Center){
            position_.X -= image.getWidth() / 2;
            position_.Y -= image.getHeight() / 2;
        }
        position = position_;
    }

    /**
     * Translates this sprite by a given amount.
     * @param direction The direction to move in, in pixels.
     */
    public void Translate(Vector2 direction) {
        MoveTo(new Vector2(position.X + direction.X, position.Y + direction.Y));
    }

    /**
     * Rotate this sprite by a given amount.
     * Positive is clockwise, negative is anticlockwise.
     * @param amount The amount of degrees to move.
     */
    public void Rotate(float amount) {
        //todo
    }

    /**
     * Changes the anchor on this sprite for position calculation purposes.
     * @param anchor_ The new anchor of the sprite.
     */
    public void ChangeAnchor(SpriteAnchor anchor_)
    {
        //Don't bother if the anchor is the same.
        if (anchor == anchor_) { return; }

        //Set the anchor.
        anchor = anchor_;
        int goneToCenter = 1;
        if (anchor == SpriteAnchor.TopLeft) { goneToCenter = -1; }

        //Recalculate position.
        position.X += image.getWidth() / 2 * goneToCenter;
        position.Y += image.getHeight() / 2 * goneToCenter;
    }
}
