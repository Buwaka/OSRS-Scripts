package Utilities.Scripting;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.web.node.AbstractWebNode;

public class WebObstacleNode extends AbstractWebNode
{
    private Tile Location;

    public WebObstacleNode(Tile location)
    {
        super(location.getX(), location.getY(), location.getZ());
        Location = location;
    }

    @Override
    public boolean execute()
    {
        return super.execute();
    }

    @Override
    public boolean hasRequirements()
    {
        //Equipment.all().get(0).
        return Inventory.contains("Knife") || Inventory.contains("Sword") || Inventory.contains("Axe");
    }

    @Override
    public boolean isValid()
    {
        return true;
    }


    @Override
    public boolean forceNext()
    {
        return super.forceNext();
    }
}
