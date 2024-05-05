package Utilities.Patterns.Delegates;

import io.vavr.Function3;

import java.util.ArrayList;
import java.util.List;

public class Delegate3<A, B, C>
{
    List<Function3<A, B, C, Boolean>> Subscribers = new ArrayList<>();

    public void Subscribe(Function3<A, B, C, Boolean> function)
    {
        Subscribers.add(function);
    }

    public void Fire(A var1, B var2, C var3)
    {
        for(var func : Subscribers)
        {
            func.apply(var1, var2, var3);
        }
    }
}

