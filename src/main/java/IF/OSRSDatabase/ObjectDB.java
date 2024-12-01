package IF.OSRSDatabase;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ObjectDB extends OSRSDataBase
{

    final private static String                                   ObjectsDB              = "misc/objects-summary.json";
    final private static ReentrantLock                            ObjectsDBLock          = new ReentrantLock();
    final private static ConcurrentHashMap<Integer, EntityObject> ObjectsDBCache         = new ConcurrentHashMap<>();
    final private static ConcurrentHashMap<String, Set<Integer>>  ObjectIDsByNameDBCache = new ConcurrentHashMap<>();

    public static class EntityObject implements Serializable
    {
        public int    id;
        public String name;

        public String toString()
        {
            return _toString(this, this.getClass());
        }
    }

    public static int[] GetObjectIDsByName(String... Names)
    {
        List<String>  names = new ArrayList<>(List.of(Names));
        List<Integer> IDs   = new ArrayList<>();

        for(var name : Names)
        {
            if(ObjectIDsByNameDBCache.containsKey(name))
            {
                IDs.addAll(ObjectIDsByNameDBCache.get(name));
                names.remove(name);
            }
        }

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

                for(var name : names)
                {
                    if(Data.name.equalsIgnoreCase(name))
                    {
                        IDs.add(ID);
                        ObjectIDsByNameDBCache.getOrDefault(name, new HashSet<>()).add(ID);
                        break;
                    }
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
        return intIDs;
    }
}
