import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Represents a single array of pixels within Gadzook.
 */
public class GadzookTexture extends GameObject {

    //The texture's pixels.
    private Color[] texture;

    //The size of a texture.
    private Vector2 size;

    /**
     * Instantiates a new instance of the texture, based on a file path.
     * Also instantiates a number of animations.
     */
    public GadzookTexture(String filePath) throws IOException
    {
        //Read in the image.
        BufferedImage image = ImageIO.read(new File(filePath));

        //Create the texture array.
        texture = new Color[image.getWidth() * image.getHeight()];

        //Set size.
        size = new Vector2(image.getWidth(), image.getHeight());

        //Read image pixel by pixel into the texture array.
        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                texture[i * image.getWidth() + j] = new Color(image.getRGB(j, i));
            }
        }
    }

    /**
     * Returns the colour of a specific pixel on the texture, provided a coordinate.
     */
    public Color getColour(int x, int y)
    {
        return texture[y * size.X + x];
    }

    /**
     * Returns the width of the texture.
     */
    public int getWidth() { return size.X; }

    /**
     * Returns the height of the texture.
     */
    public int getHeight() { return size.Y; }

}
