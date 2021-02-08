/**
 * Represents a single object that receives tick updates within the game.
 */
public abstract class GameObject {

    //Unique ID tracker.
    static int idIndex = 0;

    //Unique ID of this sprite.
    public int ID;

    /**
     * Constructor for GameObject.
     * Gives all objects a unique ID.
     */
    public GameObject()
    {
        ID = idIndex;
        idIndex++;
    }

    /**
     * Checks whether this GameObject is the same instance as what's being compared.
     * Uses the public "ID" field.
     * @param obj The object to compare to this.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GameObject)) { return false; }
        return ((GameObject)obj).ID == ID;
    }

    /**
     * Runs at the refresh rate of the game.
     */
    public void Tick(float delta) { }
}
