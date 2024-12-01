package IF.OSRSDatabase;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.dreambot.api.methods.map.Tile;

import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

public class PrayerDB extends OSRSDataBase
{
    final private static String                       AltarDB      = "misc/PrayerAltar.json";
    final private static ReentrantLock                AltarDBLock  = new ReentrantLock();
    final private static ConcurrentLinkedQueue<Altar> AltarDBCache = new ConcurrentLinkedQueue<>();

    public static class Altar implements Serializable
    {
        public String  Name       = "";
        public Tile    Location;
        public boolean Member     = false;
        public boolean Wilderness = false;

        Altar(Tile loc)
        {
            Location = loc;
        }

        public String toString()
        {
            return _toString(this, this.getClass());
        }
    }

    public static Altar[] GetAltars()
    {
        return GetAltars(false, false);
    }

    public static Altar[] GetAltars(boolean Member, boolean Wilderness)
    {
        if(!AltarDBCache.isEmpty())
        {
            return AltarDBCache.toArray(new Altar[0]);
        }

        List<Altar> out = new ArrayList<>();
        try
        {
            AltarDBLock.lock();
            InputStreamReader File   = new InputStreamReader(GetInputStream(AltarDB));
            JsonReader        Reader = new JsonReader(File);
            Gson              gson   = new Gson();
            Reader.setLenient(true);

            Reader.beginArray();

            while(Reader.hasNext())
            {
                Altar Obj = gson.fromJson(Reader, Altar.class);
                if((Member || !Obj.Member) && (Wilderness || !Obj.Wilderness))
                {
                    out.add(Obj);
                }
                AltarDBCache.add(Obj);
            }

            Reader.endArray();
            Reader.close();

        } catch(Exception e)
        {
            if(AltarDBLock.isLocked() && AltarDBLock.isHeldByCurrentThread())
            {
                AltarDBLock.unlock();
            }
            throw new RuntimeException(e);
        }

        AltarDBLock.unlock();
        return out.toArray(out.toArray(new Altar[0]));
    }

    public static Altar[] GetAltars(boolean Member)
    {
        return GetAltars(Member, false);
    }
}
