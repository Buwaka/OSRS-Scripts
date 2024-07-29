package Utilities.Patterns.Delegates;

import io.vavr.Function2;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class Delegate2<A, B>
{
    List<WeakReference<Function2<A, B, Boolean>>> Subscribers = new ArrayList<>();

    public void Fire(A var1, B var2)
    {
        Subscribers.removeIf(t -> t.get() == null);

        for(var func : Subscribers)
        {
            if(func.get() != null)
            {
                func.get().apply(var1, var2);
            }
        }
    }

    public void Subscribe(Function2<A, B, Boolean> function)
    {
        Subscribers.add(new WeakReference<>(function));
    }
}