package Utilities.Scripting;

import Cycles.CombineCycle;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CycleUtilities
{
    static final String CombineDB = "CombineCycles.json";

    public static List<CombineCycle> GetAllCombineCycles()
    {
        var               input  = new BufferedInputStream(Objects.requireNonNull(CycleUtilities.class.getClassLoader().getResourceAsStream(CombineDB)));
        InputStreamReader File   = new InputStreamReader(input);
        JsonReader        Reader = new JsonReader(File);
        Gson gson = new Gson();

        List<CombineCycle> result = new ArrayList<>();

        try
        {
            Reader.beginArray();
            while(Reader.hasNext())
            {
                CombineCycle cycle = gson.fromJson(Reader, CombineCycle.class);
                if(cycle != null)
                {
                    result.add(cycle);
                }
            }

            Reader.endArray();

        } catch(Exception ignored)
        {

        }

        return result;
    }

    public static List<CombineCycle> GetAllValidCombineCycles()
    {
        var all = GetAllCombineCycles();
        var allValid =  all.stream().filter(CombineCycle::isValid).toList();
        for(var valid : allValid)
        {
            valid.SetCycleType(ICycle.CycleType.byCount);
            valid.SetCycleLimit(valid.GetPossibleCycleCount());
        }
        return allValid;
    }
}
