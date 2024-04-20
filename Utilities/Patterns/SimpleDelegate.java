package Utilities.Patterns;

import java.util.ArrayList;
import java.util.List;

public class SimpleDelegate
{
    List<Runnable> Subscribers = new ArrayList<>();

    public boolean Subscribe(Runnable function)
    {
        return Subscribers.add(function);
    }

    public void Fire()
    {
        for(var func : Subscribers)
        {
            func.run();
        }
    }
}
