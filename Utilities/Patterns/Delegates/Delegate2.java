package Utilities.Patterns.Delegates;

import io.vavr.Function2;

import java.util.ArrayList;
import java.util.List;

public class Delegate2<A, B>
{
    List<Function2<A, B, Void>> Subscribers = new ArrayList<Function2<A, B, Void>>();

    public boolean Subscribe(Function2<A, B, Void> function)
    {
        return Subscribers.add(function);
    }

    public void Fire(A var1, B var2)
    {
        for(var func : Subscribers)
        {
            func.apply(var1, var2);
        }
    }
}