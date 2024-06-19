package OSRSDatabase;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ObjectDB extends OSRSDataBase
{

    final private static String                                   ObjectsDB              = "objects-summary.json";
    final private static ReentrantLock                            ObjectsDBLock          = new ReentrantLock();
    final private static ConcurrentHashMap<Integer, EntityObject> ObjectsDBCache         = new ConcurrentHashMap<>();
    final private static ConcurrentHashMap<String, int[]>         ObjectIDsByNameDBCache = new ConcurrentHashMap<>();

    public static class EntityObject
    {
        public int    id;
        public String name;
    }

    public static int[] GetObjectIDsByName(String Name)
    {
        if(ObjectIDsByNameDBCache.containsKey(Name))
        {
            return ObjectIDsByNameDBCache.get(Name);
        }

        List<Integer> IDs = new ArrayList<>();

        try
        {
            ObjectsDBLock.lock();
            InputStreamReader File   = new InputStreamReader(GetInputStream(ObjectsDB));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = new Gson();

            Reader.setLenient(true);

            Reader.beginObject();

            while(Reader.hasNext())
            {
                int          ID   = Integer.parseInt(Reader.nextName());
                EntityObject Data = gson.fromJson(Reader, EntityObject.class);

                if(Data.name.equalsIgnoreCase(Name))
                {
                    IDs.add(ID);
                }
            }

            Reader.endObject();
            Reader.close();

        } catch(Exception e)
        {
            if(ObjectsDBLock.isLocked() && ObjectsDBLock.isHeldByCurrentThread())
            {
                ObjectsDBLock.unlock();
            }
            throw new RuntimeException(e);
        }
        ObjectsDBLock.unlock();

        int[] intIDs = IDs.stream().mapToInt(Integer::intValue).toArray();
        ObjectIDsByNameDBCache.put(Name, intIDs);
        return intIDs;
    }
}
