package IF.Utilities.Patterns;

import IF.Utilities.OSRSUtilities;
import IF.Utilities.Patterns.Delegates.Delegate;
import IF.Utilities.Scripting.Logger;

import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class GameTickDelegate extends Delegate
{
    ConcurrentLinkedQueue<Semaphore>   Tickers       = new ConcurrentLinkedQueue<>();
    WeakHashMap<Object, AtomicInteger> UpdateTickers = new WeakHashMap<>();

    public void AddUpdateTicker(Object Caller, AtomicInteger Ticker)
    {
        UpdateTickers.put(Caller, Ticker);
    }

    public void WaitRandomTicks(int max)
    {
        WaitTicks(OSRSUtilities.rand.nextInt(Math.max(1, max - 1)) + 1);
    }

    public void WaitTicks(int ticks)
    {
        Semaphore Lock = new Semaphore(-ticks);
        Tickers.add(Lock);
        try
        {
            Logger.log("GameTickDelegate: Waiting for ticks");
            Lock.acquire();
            Tickers.remove(Lock);
        } catch(Exception e)
        {
            Logger.log(
                    "GameTickDelegate: Failed to wait for the lock somehow, possibly thread got interrupted " +
                    e);
        }
    }

    /**
     *
     */
    @Override
    public void Fire()
    {
        super.Fire();
        for(var ticker : Tickers)
        {
            Logger.log(ticker.availablePermits() + " available permits left");
            ticker.release();
        }
        for(var ticker : UpdateTickers.values())
        {
            ticker.decrementAndGet();
        }
    }
}
