package Utilities.Patterns;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

public class SYMaths implements Serializable
{

    @Serial
    private static final long serialVersionUID = -6699390512791046631L;


    public static double AddPercentage(double number, double percentage)
    {
        return number + Math.round(number * percentage);
    }

    public static int AddPercentage(int number, double percentage)
    {
        return number + (int) Math.round(number * percentage);
    }

    public static int DigitCount(float number)
    {
        int num = Math.round(number);
        return DigitCount(num);
    }

    public static int DigitCount(long number)
    {
        String str = String.valueOf(number);
        return str.length();
    }

    public static int DigitCount(double number)
    {
        long num = Math.round(number);
        return DigitCount(num);
    }

    public static double Mean(ArrayList<Double> list, double trim)
    {
        list.sort(Comparator.naturalOrder());
        list.subList(0, (int) Math.round(list.size() * trim)).clear();
        return list.stream().mapToDouble(Double::doubleValue).sum() / list.size();
    }
}
