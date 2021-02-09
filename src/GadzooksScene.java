import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.util.ArrayList;

public class GadzooksScene extends GadzookRenderer {

    //The arena the scene is using.
    GameArena arena;

    //The default "missing texture" texture.
    GadzookTexture defaultTexture = new GadzookTexture("smallDefaultTexture.png");

    //The position of the player.
    Vector2f playerPos;

    //The delta look and look angle of the player.
    Vector2f playerDelta = new Vector2f(0, 0);
    float playerAngle = 0;

    //The size of the map.
    Vector2 mapSize = new Vector2(8, 8);

    //The size of each map square.
    int mapUnitSize = 64;

    //The maximum ray depth per trace.
    int maxRayDepth = 32;

    //The FOV of the "camera".
    int fieldOfView = 90;

    //The view resolution.
    int viewResolution = 240;

    //How many units a player can be away from a wall before they can't move forward anymore.
    int collisionGap = 20;

    //The list of rays cast this frame.
    ArrayList<Raycast> rays = new ArrayList<>();

    //The map array.
    MapCell[][] map = new MapCell[][] {
        new MapCell[] { MapCell.Wall, MapCell.Wall, MapCell.Wall, MapCell.Wall, MapCell.Wall, MapCell.Wall, MapCell.Wall, MapCell.Wall },
        new MapCell[] { MapCell.Wall, MapCell.Empty, MapCell.Wall, MapCell.Empty, MapCell.Empty, MapCell.Empty, MapCell.Empty, MapCell.Wall },
        new MapCell[] { MapCell.Wall, MapCell.Empty, MapCell.Wall, MapCell.Empty, MapCell.Empty, MapCell.Empty, MapCell.Empty, MapCell.Wall },
        new MapCell[] { MapCell.Wall, MapCell.Empty, MapCell.Wall, MapCell.Empty, MapCell.Empty, MapCell.Empty, MapCell.Empty, MapCell.Wall },
        new MapCell[] { MapCell.Wall, MapCell.Empty, MapCell.Wall, MapCell.Empty, MapCell.Empty, MapCell.Empty, MapCell.Empty, MapCell.Wall },
        new MapCell[] { MapCell.Wall, MapCell.Empty, MapCell.Empty, MapCell.Empty, MapCell.Empty, MapCell.Wall, MapCell.Empty, MapCell.Wall },
        new MapCell[] { MapCell.Wall, MapCell.Empty, MapCell.Empty, MapCell.Empty, MapCell.Empty, MapCell.Empty, MapCell.Empty, MapCell.Wall },
        new MapCell[] { MapCell.Wall, MapCell.Wall, MapCell.Wall, MapCell.Wall, MapCell.Wall, MapCell.Wall, MapCell.Wall, MapCell.Wall },
    };

    /**
     * Default constructor.
     */
    public GadzooksScene() throws IOException { }

    /**
     * Runs the scene until exit.
     */
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

            //Pause for the refresh rate.
            arena.pause();
        }
    }

    /**
     * Detects player movement, and updates the position accordingly.
     */
    private void DoPlayerMovement()
    {
        //Change the viewing angle based on the A and D keys.
        if (arena.isKeyPressed(KeyEvent.VK_D)) { ChangeLookAngle(0.05f); }
        if (arena.isKeyPressed(KeyEvent.VK_A)) { ChangeLookAngle(-0.05f); }

        //Calculate the current collision gap.
        Vector2 collisionOffset = new Vector2(collisionGap, collisionGap);
        if (playerDelta.X < 0) { collisionOffset.X = -collisionGap; }
        if (playerDelta.Y < 0) { collisionOffset.Y = -collisionGap; }

        //Calculate the point in front and behind the player for collision detection.
        Vector2 playerGridPos = new Vector2((int)(playerPos.X / mapUnitSize), (int)(playerPos.Y / mapUnitSize));
        Vector2 forwardCheckPos = new Vector2((int)((playerPos.X + collisionOffset.X) / mapUnitSize), (int)((playerPos.Y + collisionOffset.Y) / mapUnitSize));
        Vector2 backwardCheckPos = new Vector2((int)((playerPos.X - collisionOffset.X) / mapUnitSize), (int)((playerPos.Y - collisionOffset.Y) / mapUnitSize));

        //Try and move the player forward/backward.
        if (arena.isKeyPressed(KeyEvent.VK_W))
        {
            //Can the player move in the X and Y directions forward?
            if (map[playerGridPos.Y][forwardCheckPos.X].getType() == MapCellType.Empty) { playerPos.X += playerDelta.X; }
            if (map[forwardCheckPos.Y][playerGridPos.X].getType() == MapCellType.Empty) { playerPos.Y += playerDelta.Y; }
        }
        if (arena.isKeyPressed(KeyEvent.VK_S))
        {
            //Can the player move in the X and Y directions backward?
            if (map[playerGridPos.Y][backwardCheckPos.X].getType() == MapCellType.Empty) { playerPos.X -= playerDelta.X; }
            if (map[backwardCheckPos.Y][playerGridPos.X].getType() == MapCellType.Empty) { playerPos.Y -= playerDelta.Y; }
        }
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

        //Adjust player look deltas.
        playerDelta.X = (float)Math.cos(playerAngle) * 2;
        playerDelta.Y = (float)Math.sin(playerAngle) * 2;
    }

    /**
     * Draws the scene to the screen.
     * @param graphics The graphics instance to draw with.
     */
    @Override
    public void Draw(Graphics graphics, BufferedImage image)
    {
        //Cast rays.
        CastRays();

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
                if (map[j][i].getType() == MapCellType.Empty) { continue; }
                graphics.fillRect(i * mapUnitSize + 4, j * mapUnitSize + 4, mapUnitSize - 4, mapUnitSize - 4);
            }
        }

        //Draw all rays.
        graphics.setColor(Color.RED);
        for (int i=0; i<rays.size(); i++) {
            graphics.drawLine((int) playerPos.X + 4, (int) playerPos.Y + 4, (int) (rays.get(i).Destination.X), (int) (rays.get(i).Destination.Y));
        }
        graphics.setColor(Color.GREEN);
        //graphics.drawLine((int)playerPos.X + 4, (int)playerPos.Y + 4, (int)(horRay.X), (int)(horRay.Y));

        //Draw current angle.
        graphics.setColor(Color.RED);
        graphics.drawString(String.valueOf(playerAngle), 10, 10);

        //Draw the casted scene.
        DrawScene(rays, graphics, image, new Vector2(640, 0));
    }

    /**
     * Draws the scene to the screen, given a list of rays, a graphics manager, and a starting point.
     */
    private void DrawScene(ArrayList<Raycast> rays, Graphics graphics, BufferedImage image, Vector2 start)
    {
        //Draw all columns.
        int col = 0;
        int pixelsPerRay = arena.getArenaWidth() / viewResolution;
        Vector2 lastGridCell = null;
        for (Raycast ray : rays)
        {
            //Set up the texture for this ray first.
            //Get the grid cell at the hit point, does it exist?
            GadzookTexture texture = null;
            Vector2 gridCell = new Vector2((int)(ray.Destination.X / mapUnitSize), (int)(ray.Destination.Y / mapUnitSize));
            if (gridCell.X >= 0 && gridCell.Y >= 0 && gridCell.Y < mapSize.X && gridCell.Y < mapSize.Y && !gridCell.equals(lastGridCell))
            {
                //Get the cell, set the texture from that cell as the texture.
                var spr = map[gridCell.Y][gridCell.X].getTexture();
                if (spr != null)
                {
                    texture = spr;
                }
                lastGridCell = gridCell;
            }

            //If no texture was found, or invalid grid square, apply the "missing texture" texture.
            if (texture == null)
            {
                texture = defaultTexture;
            }

            //Calculate the difference between the ray angle and the player's view angle.
            //This corrects the fisheye effect from a non-uniform diagonal ray.
            float angleDiff = playerAngle - ray.Angle;
            if (angleDiff < 0) { angleDiff += 2*Math.PI; }
            if (angleDiff > 2*Math.PI) { angleDiff -= 2*Math.PI; }

            //Calculate the corrected ray distance.
            float fixedRayDistance = ray.Distance * (float)Math.cos(angleDiff);

            //Calculate the height of the line on the projection, calculate texture mapping in Y.
            float lineHeight = mapUnitSize * arena.getArenaHeight() / fixedRayDistance;
            float originalLineHeight = lineHeight;
            if (lineHeight > arena.getArenaHeight())
            {
                //Cap height at screen height.
                lineHeight = arena.getArenaHeight();
            }
            float cutHeightTop = (originalLineHeight - lineHeight) / 2f;
            if (cutHeightTop < 0) { cutHeightTop = 0; }

            //Don't draw a ray that has a line height of less than a pixel.
            if (lineHeight < 1) { continue; }

            //Calculate the offset above the line to center it.
            float lineOffset = arena.getArenaHeight() / 2f - (lineHeight / 2f);

            //Begin drawing the line.
            float pixelY = 0;
            for (int row=0; row<lineHeight; row++)
            {
                //Get the position of the pixel on the texture to use.
                float texSideCoordinate = ray.Destination.X;
                if (ray.IsVerticalHit) { texSideCoordinate = ray.Destination.Y; }
                Vector2 texPixel = new Vector2((int)(texSideCoordinate * (texture.getWidth() / (float)mapUnitSize) % texture.getWidth()), (int)(((cutHeightTop + row) / originalLineHeight) * texture.getHeight()));

                //Flip the texture as necessary to render on this wall.
                //Flip for horizontal hits.
                if (ray.Angle < Math.PI && !ray.IsVerticalHit)
                {
                    texPixel.X = texture.getWidth() - 1 - texPixel.X;
                }

                //Flip for vertical hits.
                if (ray.Angle < 3*Math.PI/2 && ray.Angle > Math.PI/2 && ray.IsVerticalHit)
                {
                    texPixel.X = texture.getWidth() - 1 - texPixel.X;
                }

                //Get the colour of that pixel.
                Color pixelColour = texture.getColour(texPixel.X, texPixel.Y);

                //Alter the colour based on whether it was a vertical or horizontal hit.
                if (!ray.IsVerticalHit) {
                    pixelColour = pixelColour.darker();
                }

                //Draw onto the screen.
                graphics.setColor(pixelColour);
                graphics.drawRect(start.X + col * pixelsPerRay, (int)(start.Y + lineOffset + row), pixelsPerRay, 1);
            }

            //Increase the column index.
            col++;
        }
    }

    /**
     * Draws the rays out from the player for calculating the screen draw.
     */
    private void CastRays()
    {
        //Reset the list of rays.
        rays = new ArrayList<>();

        //Set the initial ray angle.
        float rayAngle = playerAngle;

        //Start drawing rays at half the FOV back.
        rayAngle -= Math.toRadians(fieldOfView) / 2f;
        if (rayAngle < 0) { rayAngle += 2*Math.PI; }
        if (rayAngle > 2*Math.PI) { rayAngle -= 2*Math.PI; }

        //Begin drawing rays.
        for (int i=0; i<viewResolution; i++)
        {
            //Cast ray, add to list.
            rays.add(CastRay(playerPos, rayAngle));

            //Increment the ray angle.
            rayAngle += Math.toRadians(fieldOfView) / (float)viewResolution;
            if (rayAngle > 2*Math.PI) { rayAngle -= 2*Math.PI; }
            if (rayAngle < 0) { rayAngle += 2*Math.PI; }
        }
    }

    /**
     * Casts a ray on the map, given a start position and an angle, and returns a raycast hit.
     * @param start The origin of the ray.
     * @param rayAngle The world angle that the ray is being fired at.
     * @return Raycast data for the generated ray.
     */
    public Raycast CastRay(Vector2f start, float rayAngle)
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
            horIntersect.Y = (int)(start.Y / mapUnitSize) * mapUnitSize - 0.001f;
            horIntersect.X = (start.Y - horIntersect.Y) * aTan + start.X;

            //Set the offset steps based on the fact we're facing up.
            intersectStep.Y = -mapUnitSize;
            intersectStep.X = -intersectStep.Y * aTan;
        }
        else if (rayAngle < Math.PI && rayAngle != 0)
        {
            //Ray facing down. Calculate intersect for downward ray.
            horIntersect.Y = (int)(start.Y / mapUnitSize) * mapUnitSize + mapUnitSize;
            horIntersect.X = (start.Y - horIntersect.Y) * aTan + start.X;

            //Set the offset steps based on the fact we're facing down.
            intersectStep.Y = mapUnitSize;
            intersectStep.X = -intersectStep.Y * aTan;
        }
        else
        {
            //Ray must be facing directly right or left, so don't need to check for horizontal intercept.
            horIntersect.X = start.X;
            horIntersect.Y = start.Y;
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
                if (map[mapCoord.Y][mapCoord.X].getType() == MapCellType.Wall)
                {
                    //Yes. Set distance, then break.
                    horDistance = RayDistance(start, horIntersect, rayAngle);
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
            vertIntersect.X = (int)(start.X / mapUnitSize) * mapUnitSize - 0.001f;
            vertIntersect.Y = (start.X - vertIntersect.X) * nTan + start.Y;

            //Set the offset steps based on the fact we're facing left.
            intersectStep.X = -mapUnitSize;
            intersectStep.Y = -intersectStep.X * nTan;
        }
        else if (rayAngle < Math.PI/2 || rayAngle > 3*Math.PI/2)
        {
            //Ray facing right. Calculate intersect for right facing ray.
            vertIntersect.X = (int)(start.X / mapUnitSize) * mapUnitSize + mapUnitSize;
            vertIntersect.Y = (start.X - vertIntersect.X) * nTan + start.Y;

            //Set the offset steps based on the fact we're facing right.
            intersectStep.X = mapUnitSize;
            intersectStep.Y = -intersectStep.X * nTan;
        }
        else
        {
            //Ray must be facing directly up or down, so don't need to check for horizontal intercept.
            vertIntersect.X = start.X;
            vertIntersect.Y = start.Y;
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
                if (map[mapCoord.Y][mapCoord.X].getType() == MapCellType.Wall)
                {
                    //Yes. Set distance, then break.
                    vertDistance = RayDistance(start, vertIntersect, rayAngle);
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
        float optimalDist = horDistance;
        if (vertDistance < horDistance)
        {
            optimalRay = vertIntersect;
            optimalDist = vertDistance;
        }

        //Add the generated ray to the list of rays this frame.
        return new Raycast(start, optimalRay, optimalDist, rayAngle, optimalRay.equals(vertIntersect));
    }

    /**
     * Uses the pythagorean theorem to calculate distance between a destination and source of a ray.
     */
    private float RayDistance(Vector2f start, Vector2f end, float angle)
    {
        return (float)Math.sqrt(Math.pow(end.X - start.X, 2) + Math.pow(end.Y - start.Y, 2));
    }
}
