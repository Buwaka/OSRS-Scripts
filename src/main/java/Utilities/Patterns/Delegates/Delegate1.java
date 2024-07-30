package Utilities.Patterns.Delegates;

import io.vavr.Function1;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class Delegate1<A>
{
    List<WeakReference<Function1<A, Boolean>>> Subscribers = new ArrayList<>();

    public void Fire(A var1)
    {
        Subscribers.removeIf(t -> t.get() == null);

        for(var func : Subscribers)
        {
            if(func.get() != null)
            {
                func.get().apply(var1);
            }
        }
    }

    public void Subscribe(Function1<A, Boolean> function)
    {
        Subscribers.add(new WeakReference<>(function));
    }
}