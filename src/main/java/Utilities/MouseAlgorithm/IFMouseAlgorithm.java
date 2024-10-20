package Utilities.MouseAlgorithm;

import org.dreambot.api.Client;
import org.dreambot.api.input.event.impl.mouse.MouseButton;
import org.dreambot.api.input.mouse.algorithm.MouseAlgorithm;
import org.dreambot.api.input.mouse.destination.AbstractMouseDestination;
import org.dreambot.api.utilities.Logger;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IFMouseAlgorithm implements MouseAlgorithm
{
   static final int BlockWidth  = 70;
    static final  int BlockHeight = 70;
    static  int WindowWidth       = 765;
    static   int WindowHeight      = 503;

    //Map<Integer, VectorReader.NormalizedOptimizedVector> Vectors = new HashMap<>();


    Point Destination = null;


    public IFMouseAlgorithm()
    {
        WindowWidth =  Client.getViewportWidth();
        WindowHeight = Client.getViewportHeight();
        new Thread(() -> ReadVectors(
                "C:/Users/SammyLaptop/Documents/OSRSVectorData/OptimizedVectors.dat"));
    }

    void ReadVectors(String path)
    {
        try
        {
            BufferedInputStream fis          = new BufferedInputStream(new FileInputStream(path));
            ObjectInputStream   ObjectReader = new ObjectInputStream(fis);

//            var Obj  = ObjectReader.readObject();
//            var list = (List<VectorReader.NormalizedOptimizedVector>) Obj;
//            for(var vector : list)
//            {
//                Vectors.put(vector.GetWindowIndex(BlockWidth, BlockHeight, WindowWidth, WindowHeight), vector);
//            }

        } catch(Exception e)
        {
            Logger.log("IFMouseAlgorithm: ReadVectors: " + e);
        }
    }

//    Point NormalizePoint(Point current, Point destination)
//    {
//
//    }
//
//    Point CalculateOffset(Point point, double progress) // progress is the index of the point / amount of points
//    {
//
//    }


    @Override
    public boolean handleMovement(AbstractMouseDestination abstractMouseDestination)
    {
        return false;
    }

    @Override
    public boolean handleClick(MouseButton mouseButton)
    {
        return false;
    }
}
