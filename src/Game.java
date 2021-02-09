import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The entrypoint for the game. Spawns enemies on screen, etc.
 */
public class Game {

    public static void main(String[] args) throws IOException {

        //Create a game instance.
        GameArena arena = new GameArena(1440, 600);

        //Set the refresh rate to 60hz.
        GameArena.setRefreshRateHertz(60);

        //Create and call the raycaster scene.
        var scene = new GadzooksScene();
        scene.Run(arena);
    }
}
