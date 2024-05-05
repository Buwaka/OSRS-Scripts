package Utilities.Patterns.Delegates;

import io.vavr.Function4;

import java.util.ArrayList;
import java.util.List;

public class Delegate4<A, B, C, D>
{
    List<Function4<A, B, C, D, Void>> Subscribers = new ArrayList<Function4<A, B, C, D, Void>>();

    public Delegate4()
    {
    }

    public boolean Subscribe(Function4<A, B, C, D, Void> function)
    {
        return Subscribers.add(function);
    }

    public void Fire(A var1, B var2, C var3, D var4)
    {
        for(var func : Subscribers)
        {
            func.apply(var1, var2, var3, var4);
        }
    }
}