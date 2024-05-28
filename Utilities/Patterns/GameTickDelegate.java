package Utilities.Patterns;

import Utilities.Patterns.Delegates.Delegate;
import org.dreambot.api.utilities.Logger;

import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class GameTickDelegate extends Delegate
{
    ConcurrentLinkedQueue<Semaphore> Tickers = new ConcurrentLinkedQueue<>();
    WeakHashMap<Object, AtomicInteger> UpdateTickers = new WeakHashMap<>();

    public void WaitTicks(int ticks)
    {
        Semaphore Lock = new Semaphore(-ticks);
        Tickers.add(Lock);
        try
        {
            Logger.log("Waiting for ticks");
            Lock.acquire();
            Tickers.remove(Lock);
        } catch(Exception e)
        {
            Logger.log("GameTickDelegate: Failed to wait for the lock somehow, possibly thread got interrupted " + e);
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

    public void AddUpdateTicker(Object Caller, AtomicInteger Ticker)
    {
        UpdateTickers.put(Caller, Ticker);
    }
}
