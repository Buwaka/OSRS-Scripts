package OSRSDatabase;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.dreambot.api.utilities.Logger;

import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class NPCDB extends OSRSDataBase
{
    final private static String                                  NPCDBPath      = "misc/npcs-summary.json";
    final private static ReentrantLock                           NPCDBLock      = new ReentrantLock();
    final private static ConcurrentHashMap<String, Set<Integer>> NPCByNameCache = new ConcurrentHashMap<>();

    public static class NPCData
    {
        public int    id;
        public String name;

        public String toString()
        {
            return _toString(this, this.getClass());
        }
    }

    public static NPCData GetClosestMatch(String keyword)
    {
        return GetClosestMatch(keyword, 10);
    }

    public static NPCData GetClosestMatch(String keyword, int threshold)
    {
        SortedMap<Integer, NPCData> out = new TreeMap<>();
        try
        {
            NPCDBLock.lock();
            InputStreamReader File   = new InputStreamReader(GetInputStream(NPCDBPath));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = new Gson();
            Reader.setLenient(true);

            Reader.beginObject();

            LevenshteinDistance calc = new LevenshteinDistance(threshold);

            while(Reader.hasNext())
            {
                int             ID       = Integer.parseInt(Reader.nextName());
                NPCData Obj      = gson.fromJson(Reader, NPCData.class);
                int             distance = calc.apply(Obj.name.toLowerCase(), keyword.toLowerCase());

                if(distance != -1)
                {
                    out.put(distance, Obj);
                }
            }

            Reader.endObject();
            Reader.close();

        } catch(Exception e)
        {
            Logger.log("NPCDB: GetClosestMatch: Tried to find keyword: " + keyword);
            if(NPCDBLock.isLocked() && NPCDBLock.isHeldByCurrentThread())
            {
                NPCDBLock.unlock();
            }
            throw new RuntimeException(e);
        }

        NPCDBLock.unlock();
        return out.firstEntry().getValue();
    }

    public static int[] GetObjectIDsByName(String... Names)
    {
        List<String>  names = new ArrayList<>(List.of(Names));
        List<Integer> IDs   = new ArrayList<>();

        for(var name : Names)
        {
            if(NPCByNameCache.containsKey(name))
            {
                IDs.addAll(NPCByNameCache.get(name));
                names.remove(name);
            }
        }

        try
        {
            NPCDBLock.lock();
            InputStreamReader File   = new InputStreamReader(GetInputStream(NPCDBPath));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = new Gson();

            Reader.setLenient(true);

            Reader.beginObject();

            while(Reader.hasNext())
            {
                int     ID   = Integer.parseInt(Reader.nextName());
                NPCData Data = gson.fromJson(Reader, NPCData.class);

                for(var name : names)
                {
                    if(Data.name.equalsIgnoreCase(name))
                    {
                        IDs.add(ID);
                        NPCByNameCache.getOrDefault(name, new HashSet<>()).add(ID);
                        break;
                    }
                }
            }

            Reader.endObject();
            Reader.close();

        } catch(Exception e)
        {
            Logger.log("NPCDB: GetClosestMatch: Tried to find names: " + Arrays.toString(Names));
            if(NPCDBLock.isLocked() && NPCDBLock.isHeldByCurrentThread())
            {
                NPCDBLock.unlock();
            }
            throw new RuntimeException(e);
        }
        NPCDBLock.unlock();

        int[] intIDs = IDs.stream().mapToInt(Integer::intValue).toArray();
        return intIDs;
    }
}
