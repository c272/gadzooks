import java.awt.*;
import java.awt.event.KeyEvent;

public class GadzooksScene extends GadzookRenderer {
    //The arena the scene is using.
    GameArena arena;

    //The position of the player.
    Vector2 playerPos;
    Rectangle player;

    //The size of the map.
    Vector2 mapSize = new Vector2(8, 8);

    //The size of each map square.
    int mapUnitSize = 64;

    //The map array.
    int[][] map = new int[][] {
        new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
        new int[] { 1, 0, 1, 0, 0, 0, 0, 1 },
        new int[] { 1, 0, 1, 0, 0, 0, 0, 1 },
        new int[] { 1, 0, 1, 0, 0, 0, 0, 1 },
        new int[] { 1, 0, 0, 0, 0, 0, 0, 1 },
        new int[] { 1, 0, 0, 0, 0, 1, 0, 1 },
        new int[] { 1, 0, 0, 0, 0, 0, 0, 1 },
        new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
    };

    //Runs the scene until exit.
    public void Run(GameArena a)
    {
        //Set the arena, add self as renderer.
        arena = a;
        arena.addRenderer(this);

        //Initialize the player position.
        playerPos = new Vector2(100, 100);

        //Add the player to the scene as a little rectangle.
        player = new Rectangle(playerPos.X, playerPos.Y, 8, 8, Color.CYAN);

        //Keep window open until escape pressed.
        while (!arena.isKeyPressed(KeyEvent.VK_ESCAPE))
        {
            //todo: game loop

            //Detect player movement.
            DoPlayerMovement();

            //Pause for the refresh rate.
            arena.pause();
        }
    }

    /**
     * Detects player movement, and updates the position accordingly.
     */
    private void DoPlayerMovement() {

        //W, A, S or D keys down?
        if (arena.isKeyPressed(KeyEvent.VK_W)) { playerPos.Y -= 5; }
        if (arena.isKeyPressed(KeyEvent.VK_S)) { playerPos.Y += 5; }
        if (arena.isKeyPressed(KeyEvent.VK_D)) { playerPos.X += 5; }
        if (arena.isKeyPressed(KeyEvent.VK_A)) { playerPos.X -= 5; }

        //Update preview positions.
        player.setXPosition(playerPos.X);
        player.setYPosition(playerPos.Y);
    }

    /**
     * Draws the scene to the screen.
     * @param graphics The graphics instance to draw with.
     */
    @Override
    public void Draw(Graphics graphics) {

        //Draw player.
        graphics.setColor(Color.CYAN);
        graphics.fillRect(playerPos.X, playerPos.Y, 8, 8);
        
    }
}
