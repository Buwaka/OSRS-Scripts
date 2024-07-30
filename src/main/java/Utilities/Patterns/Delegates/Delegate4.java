package Utilities.Patterns.Delegates;

import io.vavr.Function4;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class Delegate4<A, B, C, D>
{
    List<WeakReference<Function4<A, B, C, D, Boolean>>> Subscribers = new ArrayList<>();

    public void Fire(A var1, B var2, C var3, D var4)
    {
        Subscribers.removeIf(t -> t.get() == null);

        for(var func : Subscribers)
        {
            if(func.get() != null)
            {
                func.get().apply(var1, var2, var3, var4);
            }
        }
    }

    public void Subscribe(Function4<A, B, C, D, Boolean> function)
    {
        Subscribers.add(new WeakReference<>(function));
    }
}