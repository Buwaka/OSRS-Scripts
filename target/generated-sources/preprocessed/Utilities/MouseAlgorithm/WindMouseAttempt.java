package Utilities.MouseAlgorithm;

import Utilities.Scripting.Logger;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.input.event.impl.mouse.MouseButton;
import org.dreambot.api.input.mouse.algorithm.MouseAlgorithm;
import org.dreambot.api.input.mouse.algorithm.StandardMouseAlgorithm;
import org.dreambot.api.input.mouse.destination.AbstractMouseDestination;
import org.dreambot.api.input.mouse.destination.impl.EntityDestination;
import org.dreambot.api.input.mouse.destination.impl.MiniMapTileDestination;
import org.dreambot.api.input.mouse.destination.impl.PointDestination;
import org.dreambot.api.input.mouse.destination.impl.TileDestination;
import org.dreambot.api.input.mouse.destination.impl.shape.AreaDestination;
import org.dreambot.api.input.mouse.destination.impl.shape.PolygonDestination;
import org.dreambot.api.input.mouse.destination.impl.shape.RectangleDestination;
import org.dreambot.api.input.mouse.destination.impl.shape.ShapeDestination;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.listener.PaintListener;

import java.awt.*;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class WindMouseAttempt implements MouseAlgorithm, PaintListener
{
    final         double sqrt3 = Math.sqrt(3);
    final         double sqrt5 = Math.sqrt(5);
    private final int    CheckDelay;
    StandardMouseAlgorithm                    standardMouseAlgorithm = new StandardMouseAlgorithm();
    double                                    Gravity                = 30;//        G_0 - magnitude of the gravitational force
    double                                    Wind                   = 10;//        W_0 - magnitude of the wind force fluctuations
    double                                    StepSize               = 20;//        M_0 - maximum step size (velocity clip threshold)
    double                                    DampenThreshold        = 20;//        D_0 - distance where wind behavior changes from random to damped
    int                                       MinWait                = 3;
    int                                       MaxWait                = 50;
    Random                                    np                     = new Random();
    //AtomicReference<Point>                    dest                   = new AtomicReference<>(null);
    AtomicReference<AbstractMouseDestination> Destination            = new AtomicReference<>(null);
    AtomicBoolean                             MouseIsMoving          = new AtomicBoolean(false);
    int[]                                     xPoints                = new int[1024];
    int[]                                     yPoints                = new int[1024];
    int                                       nPoints                = 0;

    Thread MouseThread = null;

    public WindMouseAttempt(int CheckDelay)
    {
        this.CheckDelay = CheckDelay;
        MouseThread     = new Thread(this::MouseThread);
        MouseThread.start();
    }

    void MouseThread()
    {
        while(!Thread.currentThread().isInterrupted())
        {
            if(MouseIsMoving.get())
            {
                MoveMouse();
            }
        }
    }

    void MoveMouse()
    {
        int    current_x = Mouse.getX();
        double start_x   = current_x;
        int    current_y = Mouse.getY();
        double start_y   = current_y;


        double v_x  = 0;
        double v_y  = 0;
        double W_x  = 0;
        double W_y  = 0;
        double step = StepSize;
        double dist = Distance();
        while(dist >= 1)
        {
            Point dest   = GetDestination();
            int   dest_x = dest.x;
            int   dest_y = dest.y;

            Logger.log("Mouse: Distance: " + dist);
            double W_mag = Math.min(Wind, dist);
            if(dist >= DampenThreshold)
            {
                W_x = W_x / sqrt3 + (2 * np.nextDouble() - 1) * W_mag / sqrt5;
                W_y = W_y / sqrt3 + (2 * np.nextDouble() - 1) * W_mag / sqrt5;
            }
            else
            {
                W_x /= sqrt3;
                W_y /= sqrt3;
                if(step < 3)
                {
                    step = np.nextDouble() * 3 + 3;
                }

                else
                {
                    step /= sqrt5;
                }
            }
            v_x += W_x + Gravity * (dest_x - start_x) / dist;
            v_y += W_y + Gravity * (dest_y - start_y) / dist;
            double v_mag = Math.hypot(v_x, v_y);
            if(v_mag > step)
            {
                double v_clip = step / 2 + np.nextDouble() * step / 2;
                v_x = (v_x / v_mag) * v_clip;
                v_y = (v_y / v_mag) * v_clip;
            }

            start_x += v_x;
            start_y += v_y;
            int move_x = (int) Math.round(start_x);
            int move_y = (int) Math.round(start_y);
            if(current_x != move_x || current_y != move_y)
            {
                Logger.log("Mouse: hop");
                //This should wait for the mouse polling interval
                current_x        = move_x;
                current_y        = move_y;
                xPoints[nPoints] = move_x;
                yPoints[nPoints] = move_y;
                nPoints++;
                Mouse.hop(new Point(move_x, move_y));
            }
            dist = Distance();
            sleep(np.nextInt(MaxWait - MinWait) + MinWait);
        }
        Logger.log("Mouse: destination reached");
        MouseIsMoving.set(false);
        nPoints = 0;
    }

    double Distance()
    {
        Point dest      = GetDestination();
        int   dest_x    = dest.x;
        int   dest_y    = dest.y;
        int   current_x = Mouse.getX();
        int   current_y = Mouse.getY();
        return Math.hypot(dest_x - current_x, dest_y - current_y);
    }

    Point GetDestination()
    {
        if(Destination == null || Destination.get() == null)
        {
            return null;
        }

        switch(Destination.get().type())
        {
            case 0: //PointDestination
                PointDestination PDest = (PointDestination) Destination.get();
                return PDest.getSuitablePoint();
            case 1: // EntityDestination
                EntityDestination EDest = (EntityDestination) Destination.get();
                if(EDest.isVisible())
                {
                    return EDest.getSuitablePoint();
                }
                if(EDest.getTarget().distance(Players.getLocal()) <
                   (double) Math.max(2.0F, Math.min(12.0F, 3584.0F / (float) Camera.getZoom())) &&
                   EDest.handleCamera())
                {
                    return EDest.getSuitablePoint();
                }
                else if(Walking.walkOnScreen(EDest.getTarget().getTile()))
                {
                    return EDest.getSuitablePoint();
                }
                return null;
            case 2: // AreaDestination
                AreaDestination ADest = (AreaDestination) Destination.get();
                return ADest.getSuitablePoint();
            case 3: //ShapeDestination
                ShapeDestination SDest = (ShapeDestination) Destination.get();
                return SDest.getSuitablePoint();
            case 4: // RectangleDestination
                RectangleDestination RDest = (RectangleDestination) Destination.get();
                return RDest.getSuitablePoint();
            case 5: //PolygonDestination
                PolygonDestination PolyDest = (PolygonDestination) Destination.get();
                return PolyDest.getSuitablePoint();
            case 6://MiniMapTileDestination
                MiniMapTileDestination MMDest = (MiniMapTileDestination) Destination.get();
                return MMDest.getSuitablePoint();
            case 7: //TileDestination
                TileDestination TDest = (TileDestination) Destination.get();
                return TDest.getSuitablePoint();
        }
        return null;
    }

    private void sleep(int millis)
    {
        //millis += MOUSE_DIFF;
        try
        {
            Thread.sleep(millis);
        } catch(Exception ignored)
        {

        }
    }

    @Override
    public void onPaint(Graphics2D graphics)
    {
        graphics.setColor(Color.red);
        if(nPoints > 0)
        {
            graphics.drawPolyline(xPoints, yPoints, nPoints);
        }
    }

    /**
     * @param abstractMouseDestination
     *
     * @return
     */
    @Override
    public boolean handleMovement(AbstractMouseDestination abstractMouseDestination)
    {
        Logger.log("Mouse: new movement: " + abstractMouseDestination.type());
        //dest.set(abstractMouseDestination.getSuitablePoint());

        Destination.set(abstractMouseDestination);
        MouseIsMoving.set(true);

        //        switch(abstractMouseDestination.type())
        //        {
        //            case 0: //PointDestination
        //                break;
        //            case 1: // EntityDestination
        //                break;
        //            case 2: // AreaDestination
        //                break;
        //            case 3: //ShapeDestination
        //                break;
        //            case 4: // RectangleDestination
        //                break;
        //            case 5: //PolygonDestination
        //                break;
        //            case 6://MiniMapTileDestination
        //                break;
        //            case 7: //TileDestination
        //                break;
        //        }
        return false;
        //return Sleep.sleepUntil(() -> !MouseIsMoving.get(), CheckDelay);
    }

    /**
     * @param mouseButton
     *
     * @return
     */
    @Override
    public boolean handleClick(MouseButton mouseButton)
    {
        return standardMouseAlgorithm.handleClick(mouseButton);
    }

}
