import java.awt.*;
import java.awt.event.KeyEvent;

public class GadzooksScene extends GadzookRenderer {
    //The arena the scene is using.
    GameArena arena;

    //The position of the player.
    Vector2f playerPos;

    //The delta look and look angle of the player.
    Vector2f playerDelta = new Vector2f(0, 0);
    float playerAngle = 0;

    //The size of the map.
    Vector2 mapSize = new Vector2(8, 8);

    //The size of each map square.
    int mapUnitSize = 80;

    //The maximum ray depth per trace.
    int maxRayDepth = 32;

    //temp
    Vector2f horRay = new Vector2f(0,0);
    Vector2f vertRay = new Vector2f(0, 0);

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

    /**
     * Draws the scene to the screen.
     * @param graphics The graphics instance to draw with.
     */
    @Override
    public void Draw(Graphics graphics) {

        //Draw player.
        graphics.setColor(Color.CYAN);
        graphics.fillRect((int)playerPos.X, (int)playerPos.Y, 8, 8);
        graphics.drawLine((int)playerPos.X + 4, (int)playerPos.Y + 4, (int)(playerPos.X + playerDelta.X * 10), (int)(playerPos.Y + playerDelta.Y * 10));

        //Draw walls.
        graphics.setColor(Color.WHITE);
        for (int i=0; i<mapSize.X; i++)
        {
            for (int j=0; j<mapSize.Y; j++)
            {
                if (map[j][i] == 0) { continue; }
                graphics.fillRect(i * mapUnitSize + 4, j * mapUnitSize + 4, mapUnitSize - 4, mapUnitSize - 4);
            }
        }

        //Draw temp ray.
        graphics.setColor(Color.RED);
        graphics.drawLine((int)playerPos.X + 4, (int)playerPos.Y + 4, (int)(vertRay.X), (int)(vertRay.Y));
        graphics.setColor(Color.GREEN);
        //graphics.drawLine((int)playerPos.X + 4, (int)playerPos.Y + 4, (int)(horRay.X), (int)(horRay.Y));

        //Draw current angle.
        graphics.setColor(Color.RED);
        graphics.drawString(String.valueOf(playerAngle), 10, 10);
    }

    //Runs the scene until exit.
    public void Run(GameArena a)
    {
        //Set the arena, add self as renderer.
        arena = a;
        arena.addRenderer(this);

        //Initialize the player position.
        playerPos = new Vector2f(100, 100);

        //Set the initial delta value.
        ChangeLookAngle(0);

        //Keep window open until escape pressed.
        while (!arena.isKeyPressed(KeyEvent.VK_ESCAPE))
        {
            //todo: game loop

            //Detect player movement.
            DoPlayerMovement();

            //Draw rays.
            DrawRays();

            //Pause for the refresh rate.
            arena.pause();
        }
    }

    /**
     * Detects player movement, and updates the position accordingly.
     */
    private void DoPlayerMovement()
    {
        //W, A, S or D keys down?
        if (arena.isKeyPressed(KeyEvent.VK_D)) { ChangeLookAngle(0.1f); }
        if (arena.isKeyPressed(KeyEvent.VK_A)) { ChangeLookAngle(-0.1f); }
        if (arena.isKeyPressed(KeyEvent.VK_W)) { playerPos.X += playerDelta.X; playerPos.Y += playerDelta.Y; }
        if (arena.isKeyPressed(KeyEvent.VK_S)) { playerPos.X -= playerDelta.X; playerPos.Y -= playerDelta.Y; }
    }

    /**
     * Alters the player's look angle by the given amount.
     */
    private void ChangeLookAngle(float amt)
    {
        //Add amount, adjust to be within bounds of 2Pi.
        playerAngle += amt;
        if (playerAngle < 0) { playerAngle += 2*Math.PI; }
        if (playerAngle > 2*Math.PI) { playerAngle -= 2*Math.PI; }

        //Make sure player angle is never exactly any of the cardinal directions.
        //...

        //Adjust player look deltas.
        playerDelta.X = (float)Math.cos(playerAngle) * 2;
        playerDelta.Y = (float)Math.sin(playerAngle) * 2;
    }

    /**
     * Draws the rays out from the player for calculating the screen draw.
     */
    private void DrawRays()
    {
        //Set the initial ray angle.
        float rayAngle = playerAngle;

        for (int i=0; i<1; i++)
        {
            ///////////////////////////////////
            /// HORIZONTAL LINE CALCULATION ///
            ///////////////////////////////////

            //Create a new vector for the ray intersect position.
            Vector2f horIntersect = new Vector2f(0,0);
            float horDistance = Float.MAX_VALUE;

            //Create the depth tracker.
            int depth = 0;

            //Calculate the Y of the horizontal grid intersect.
            float aTan = -1 / (float)Math.tan(rayAngle);
            Vector2f intersectStep = new Vector2f(0,0);
            if (rayAngle > Math.PI)
            {
                //Ray facing up. Calculate intersect for upward ray.
                horIntersect.Y = (int)(playerPos.Y / mapUnitSize) * mapUnitSize - 0.001f;
                horIntersect.X = (playerPos.Y - horIntersect.Y) * aTan + playerPos.X;

                //Set the offset steps based on the fact we're facing up.
                intersectStep.Y = -mapUnitSize;
                intersectStep.X = -intersectStep.Y * aTan;
            }
            else if (rayAngle < Math.PI && rayAngle != 0)
            {
                //Ray facing down. Calculate intersect for downward ray.
                horIntersect.Y = (int)(playerPos.Y / mapUnitSize) * mapUnitSize + mapUnitSize;
                horIntersect.X = (playerPos.Y - horIntersect.Y) * aTan + playerPos.X;

                //Set the offset steps based on the fact we're facing down.
                intersectStep.Y = mapUnitSize;
                intersectStep.X = -intersectStep.Y * aTan;
            }
            else
            {
                //Ray must be facing directly right or left, so don't need to check for horizontal intercept.
                horIntersect.X = playerPos.X;
                horIntersect.Y = playerPos.Y;
                depth = maxRayDepth;
            }

            //Trace until a wall hit (or run out of depth).
            while (depth < maxRayDepth)
            {
                //Get the map coordinate.
                Vector2 mapCoord = new Vector2((int)(horIntersect.X / mapUnitSize), (int)(horIntersect.Y / mapUnitSize));

                //Is the current grid space actually in the world?
                if (mapCoord.X < mapSize.X && mapCoord.Y < mapSize.Y && mapCoord.X >= 0 && mapCoord.Y >= 0)
                {
                    //Yes, is it a wall?
                    if (map[mapCoord.Y][mapCoord.X] == 1)
                    {
                        //Yes. Set distance, then break.
                        horDistance = RayDistance(playerPos, horIntersect, rayAngle);
                        break;
                    }
                }

                //Go to the next intersect.
                horIntersect.X += intersectStep.X;
                horIntersect.Y += intersectStep.Y;

                //Keep going.
                depth++;
            }

            /////////////////////////////////
            /// VERTICAL LINE CALCULATION ///
            /////////////////////////////////

            //Create a new vector for the ray intersect position.
            Vector2f vertIntersect = new Vector2f(0,0);
            float vertDistance = Float.MAX_VALUE;

            //Reset the depth tracker.
            depth = 0;

            //Calculate the Y of the horizontal grid intersect.
            float nTan = (float)-Math.tan(rayAngle);
            intersectStep = new Vector2f(0,0);
            if (rayAngle > Math.PI/2 && rayAngle < 3*Math.PI/2)
            {
                //Ray facing left. Calculate intersect for left facing ray.
                vertIntersect.X = (int)(playerPos.X / mapUnitSize) * mapUnitSize - 0.001f;
                vertIntersect.Y = (playerPos.X - vertIntersect.X) * nTan + playerPos.Y;

                //Set the offset steps based on the fact we're facing left.
                intersectStep.X = -mapUnitSize;
                intersectStep.Y = -intersectStep.X * nTan;
            }
            else if (rayAngle < Math.PI/2 || rayAngle > 3*Math.PI/2)
            {
                //Ray facing right. Calculate intersect for right facing ray.
                vertIntersect.X = (int)(playerPos.X / mapUnitSize) * mapUnitSize + mapUnitSize;
                vertIntersect.Y = (playerPos.X - vertIntersect.X) * nTan + playerPos.Y;

                //Set the offset steps based on the fact we're facing right.
                intersectStep.X = mapUnitSize;
                intersectStep.Y = -intersectStep.X * nTan;
            }
            else
            {
                //Ray must be facing directly up or down, so don't need to check for horizontal intercept.
                vertIntersect.X = playerPos.X;
                vertIntersect.Y = playerPos.Y;
                depth = maxRayDepth;
            }

            //Trace until a wall hit (or run out of depth).
            while (depth < maxRayDepth)
            {
                //Get the map coordinate.
                Vector2 mapCoord = new Vector2((int)(vertIntersect.X / mapUnitSize), (int)(vertIntersect.Y / mapUnitSize));

                //Is the current grid space actually in the world?
                if (mapCoord.X < mapSize.X && mapCoord.Y < mapSize.Y && mapCoord.X >= 0 && mapCoord.Y >= 0)
                {
                    //Yes, is it a wall?
                    if (map[mapCoord.Y][mapCoord.X] == 1)
                    {
                        //Yes. Set distance, then break.
                        vertDistance = RayDistance(playerPos, vertIntersect, rayAngle);
                        break;
                    }
                }

                //Go to the next intersect.
                vertIntersect.X += intersectStep.X;
                vertIntersect.Y += intersectStep.Y;

                //Keep going.
                depth++;
            }

            //Figure out the optimal ray to use (one with shortest distance).
            Vector2f optimalRay = horIntersect;
            if (vertDistance < horDistance) { optimalRay = vertIntersect; }

            //Set debug.
            vertRay = optimalRay;
        }
    }

    /**
     * Uses the pythagorean theorem to calculate distance between a destination and source of a ray.
     */
    private float RayDistance(Vector2f start, Vector2f end, float angle)
    {
        return (float)Math.sqrt(Math.pow(end.X - start.X, 2) + Math.pow(end.Y - start.Y, 2));
    }
}
