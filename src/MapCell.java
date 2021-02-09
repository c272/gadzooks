/**
 * Represents a single cell inside the raycast map.
 */
public class MapCell {

    /**
     * The cell instance that represents an empty space, with nothing inside it.
     */
    public static MapCell Empty = new MapCell(MapCellType.Empty, null);

    /**
     * The cell instance that represents a blank wall with no texture.
     */
    public static MapCell Wall = new MapCell(MapCellType.Wall, null);

    //The type of cell this is.
    MapCellType type;

    //The texture of this cell.
    Sprite texture;

    /**
     * Constructor for a standard map cell.
     * Takes a type of cell (cannot be empty), and a texture.
     */
    public MapCell(MapCellType type_, Sprite tex)
    {
        type = type_;
        texture = tex;
    }

    /**
     * Returns the sprite texture of this cell.
     */
    public Sprite getTexture() { return texture; }

    /**
     * Returns the type of cell this is.
     */
    public MapCellType getType() { return type; }
}
