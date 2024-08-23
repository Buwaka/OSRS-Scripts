package OSRSDatabase;

import Utilities.GrandExchange.GEInstance;
import Utilities.OSRSUtilities;
import Utilities.Patterns.SYMaths;
import Utilities.Scripting.ExternalLambdaUsage;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.net.URIBuilder;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class OSRSPrices implements Serializable
{

    @Serial
    private static final long serialVersionUID = -5940381882555771355L;

    private static final String                       AllItemsEndPoint   = "https://prices.runescape.wiki/api/v1/osrs/latest";
    private static final HTreeMap<Integer, GELatestData> AllItemsCache      = OSRSUtilities.CacheDB.hashMap("OSRSPrices-Latest").keySerializer(
            Serializer.INTEGER).valueSerializer(Serializer.JAVA).expireAfterGet(6, TimeUnit.HOURS).createOrOpen();
//private static           Cache<Integer, GELatestData> AllItemsCache      = OSRSUtilities.GetCache("GELatest", Integer.class, GELatestData.class);
    //private static final HashMap<Integer, GELatestData> AllItemsCache  = new HashMap<>();
    private static final String                       TimeSeriesEndPoint = "https://prices.runescape.wiki/api/v1/osrs/timeseries";
    private static final HTreeMap<TimeSeriesKey, GETimeSeriesData[]> TimeSeriesCache      = OSRSUtilities.CacheDB.hashMap("OSRSPrices-TimeSeries").keySerializer(
            Serializer.JAVA).valueSerializer(Serializer.JAVA).hashSeed(5).expireAfterGet(5, TimeUnit.MINUTES).createOrOpen();
    //private static final HashMap<TimeSeriesKey, GETimeSeriesData[]> TimeSeriesCache  = new HashMap<>();
//    private static Cache<TimeSeriesKey, GETimeSeriesData[]> TimeSeriesCache = OSRSUtilities.GetCache("TimeSeries", TimeSeriesKey.class, GETimeSeriesData[].class);

    public static final int CoinID = 995;

    public static class TimeSeriesKey implements Serializable
    {
        @Serial
        private static final long serialVersionUID = -97952331964844432L;
        public int ID;
        public TimeSeriesFormat format;

        public TimeSeriesKey(int ID, TimeSeriesFormat format)
        {
            this.ID     = ID;
            this.format = format;
        }

        @Override
        public boolean equals(Object o)
        {
            if(this == o) {return true;}
            if(o == null || getClass() != o.getClass()) {return false;}
            TimeSeriesKey that = (TimeSeriesKey) o;
            return ID == that.ID && format.ordinal() == that.format.ordinal();
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(ID, format.ordinal());
        }
    }

    public enum TimeSeriesFormat implements Serializable
    {
        m5("5m"),
        h1("1h"),
        h6("6h"),
        h24("24h");

        private final String name;

        TimeSeriesFormat(String Name)
        {
            name = Name;
        }

        public boolean equalsName(String otherName)
        {
            // (otherName == null) check is not needed because name.equals(null) returns false
            return name.equals(otherName);
        }

        public String toString()
        {
            return this.name;
        }
    }

    public static class GETimeSeriesData implements Serializable
    {
        @Serial
        private static final long serialVersionUID = -3670655454795683108L;
        public long timestamp;
        public @Nullable Integer avgHighPrice;
        public @Nullable Integer avgLowPrice;
        public           int     highPriceVolume;
        public           int     lowPriceVolume;
    }

    public static class GELatestData implements Serializable
    {
        @Serial
        private static final long serialVersionUID = 3260809249295661528L;
        //        public int     id;
        @Nullable
        public Integer high;
        @Nullable
        public Integer highTime;
        @Nullable
        public Integer low;
        @Nullable
        public Integer lowTime;
    }

    @ExternalLambdaUsage
    public static Integer GetAverageHighPrice(int ID)
    {
        return GetHighAveragePrice(ID, TimeSeriesFormat.h6);
    }

    public static Integer GetHighAveragePrice(int ID, TimeSeriesFormat format)
    {
        var Timeseries = GetTimeSeries(ID, format);
        if(Timeseries == null || Timeseries.length == 0)
        {
            return null;
        }

        var Avg = TrimmedAveragePrice(Timeseries);
        if(Avg == null || Avg._3 == null)
        {
            return null;
        }

        return Avg._3;
    }

    public static GELatestData GetLatest(int ID)
    {
        if(AllItemsCache.containsKey(ID))
        {
            return AllItemsCache.get(ID);
        }

        final int MaxAttempts = 5;
        int       attempt     = 0;
        while(attempt < MaxAttempts)
        {
            try(CloseableHttpClient client = HttpClients.createDefault())
            {
                HttpGet request = new HttpGet(AllItemsEndPoint);
                request.addHeader(new BasicHeader("User-Agent", "Custom runelite plugin deving"));
                URI uri = new URIBuilder(request.getUri()).build();

                request.setUri(uri);
                AtomicInteger Code = new AtomicInteger(400);
                Gson          gson = new Gson().newBuilder().create();
                Map<Integer, GELatestData> AllItems = (Map<Integer, GELatestData>) client.execute(request,
                                                                                    httpResponse -> {
                                                                                        Code.set(
                                                                                                httpResponse.getCode());
                                                                                        if(httpResponse.getCode() >=
                                                                                           300)
                                                                                        {
                                                                                            return null;
                                                                                        }
                                                                                        var content = httpResponse.getEntity()
                                                                                                                  .getContent();
                                                                                        InputStreamReader File = new InputStreamReader(
                                                                                                content);
                                                                                        JsonReader Reader = new JsonReader(
                                                                                                File);
                                                                                        Map<Integer, GELatestData> All = new HashMap<>();

                                                                                        Reader.beginObject();
                                                                                        Reader.nextName(); //data
                                                                                        Reader.beginObject();
                                                                                        while(Reader.hasNext())
                                                                                        {
                                                                                            int id = Integer.parseInt(Reader.nextName());
                                                                                            var data = (GELatestData) gson.fromJson(Reader, GELatestData.class);
                                                                                            All.put(id, data);
                                                                                        }
                                                                                        Reader.endObject();
                                                                                        Reader.endObject();
                                                                                        Reader.close();

                                                                                        return All;
                                                                                    });
                AllItemsCache.putAll(AllItems);

                if(Code.get() >= 300)
                {
                    attempt++;
                    continue;
                }
                if(!AllItemsCache.containsKey(ID))
                {
                    Logger.log("OSRSPrices: GetLatest: Can't find item with ID " + ID);
                    return null;
                }

                return AllItemsCache.get(ID);
            } catch(Exception e)
            {
                Logger.log("OSRSPrices: " + e);
                attempt++;
            }
        }
        return null;
    }

    private static GETimeSeriesData[] GetTimeSeries(int ID, TimeSeriesFormat format)
    {
        TimeSeriesKey Key = new TimeSeriesKey(ID, format);
        if(TimeSeriesCache.containsKey(Key))
        {
            return TimeSeriesCache.get(Key);
        }
        final int MaxAttempts = 5;
        int       attempt     = 0;
        while(attempt < MaxAttempts)
        {
            try(CloseableHttpClient client = HttpClients.createDefault())
            {
                HttpGet request = new HttpGet(TimeSeriesEndPoint);
                request.addHeader(new BasicHeader("User-Agent", "Custom runelite plugin deving"));
                URI uri = new URIBuilder(request.getUri()).addParameter("id", String.valueOf(ID))
                                                          .addParameter("timestep",
                                                                        format.toString())
                                                          .build();

                request.setUri(uri);
                AtomicInteger Code = new AtomicInteger(400);
                Gson          gson = new Gson().newBuilder().create();
                GETimeSeriesData[] TimeSeries = (GETimeSeriesData[]) client.execute(request,
                                                                                    httpResponse -> {
                                                                                        Code.set(
                                                                                                httpResponse.getCode());
                                                                                        if(httpResponse.getCode() >=
                                                                                           300)
                                                                                        {
                                                                                            return null;
                                                                                        }
                                                                                        var content = httpResponse.getEntity()
                                                                                                                  .getContent();
                                                                                        InputStreamReader File = new InputStreamReader(
                                                                                                content);
                                                                                        JsonReader Reader = new JsonReader(
                                                                                                File);

                                                                                        Reader.beginObject();
                                                                                        Reader.nextName();
                                                                                        var series = gson.fromJson(
                                                                                                Reader,
                                                                                                GETimeSeriesData[].class);
                                                                                        Reader.close();

                                                                                        return series;
                                                                                    });

                TimeSeriesCache.put(Key, TimeSeries);

                if(Code.get() >= 300)
                {
                    attempt++;
                    continue;
                }
                return TimeSeries;
            } catch(Exception e)
            {
                Logger.log("OSRSPrices: " + e);
                attempt++;
            }
        }
        return null;
    }

    public static long GetTotalValue(List<Item> allItems)
    {
        long total = 0;

        for(var item : allItems)
        {
            if(item == null)
            {
                continue;
            }

            var latest = GetLatest(item.getID());
            if(latest != null)
            {
                if(latest.high != null)
                {
                    if(latest.low != null)
                    {
                        total += (latest.high + latest.low) / 2;
                    }
                    else
                    {
                        total += latest.high;
                    }
                }
                else if(latest.low != null)
                {
                    total += latest.low;
                }
            }

        }
        return total;
    }

    private static Tuple3<Integer, Integer, Integer> TrimmedAveragePrice(GETimeSeriesData[] data)
    {
        if(data.length <= 1)
        {
            return null;
        }

        double            trim = 0.1;
        ArrayList<Double> high = new ArrayList<>(), low = new ArrayList<>();
        for(var entry : data)
        {
            if(entry.avgLowPrice != null)
            {
                low.add(Double.valueOf(entry.avgLowPrice));
            }
            if(entry.avgHighPrice != null)
            {
                high.add(Double.valueOf(entry.avgHighPrice));
            }
        }

        double Low  = SYMaths.Mean(low, trim);
        double High = SYMaths.Mean(high, trim);

        return new Tuple3<>((int) Low, (int) ((Low + High) / 2), (int) High);
    }

    @ExternalLambdaUsage
    public static Integer GetAverageLowPrice(int ID)
    {
        return GetAverageLowPrice(ID, TimeSeriesFormat.h6);
    }

    public static Integer GetAverageLowPrice(int ID, TimeSeriesFormat format)
    {
        var Timeseries = GetTimeSeries(ID, format);
        if(Timeseries == null || Timeseries.length == 0)
        {
            return null;
        }

        var Avg = TrimmedAveragePrice(Timeseries);
        if(Avg == null || Avg._1 == null)
        {
            return null;
        }

        return Avg._1;
    }

    @ExternalLambdaUsage
    public static Integer GetAveragePrice(int ID)
    {
        return GetAveragePrice(ID, TimeSeriesFormat.h6);
    }

    public static Integer GetAveragePrice(int ID, TimeSeriesFormat format)
    {
        var Timeseries = GetTimeSeries(ID, format);
        if(Timeseries == null || Timeseries.length == 0)
        {
            return null;
        }

        var Avg = TrimmedAveragePrice(Timeseries);
        if(Avg == null || Avg._2 == null)
        {
            return null;
        }

        return Avg._2;
    }

    public static boolean isLowVolume(int ID)
    {
        return GetDailyVolume(ID) < 5000;
    }

    /**
     * @param ID
     * Basically just adds up all the snapshots we get, which are 365 snapshots, so 365 * 5m, 365 * 24h,...
     *
     * @return
     */
    //    public static int GetVolume(int ID, TimeSeriesFormat format)
    //    {
    //        var Timeseries = GetTimeSeries(ID, format);
    //        if(Timeseries == null || Timeseries.length == 0)
    //        {
    //            return 0;
    //        }
    //
    //        var Volume        = SumVolume(Timeseries);
    //        if(Volume == null || Volume._1 == null)
    //        {
    //            return 0;
    //        }
    //
    //        return Volume._1 + Volume._2;
    //    }
    //
    //    public static int GetVolume(int ID)
    //    {
    //        return GetVolume(ID, TimeSeriesFormat.h6);
    //    }
    public static Integer GetDailyVolume(int ID)
    {
        var series = GetTimeSeries(ID, TimeSeriesFormat.h24);
        if(series == null || series.length == 0)
        {
            return null;
        }

        return series[series.length - 1].highPriceVolume + series[series.length - 1].lowPriceVolume;
    }

    public static void main(String[] args)
    {
        OSRSPrices temp = new OSRSPrices();

        //        System.out.println(temp.GetAveragePrice(6211, TimeSeriesFormat.m5));
        //        System.out.println(temp.GetAveragePrice(6211, TimeSeriesFormat.h1));
        //        System.out.println(temp.GetAveragePrice(6211, TimeSeriesFormat.h6));
        //        System.out.println(temp.GetAveragePrice(6211, TimeSeriesFormat.h24));
        System.out.println(temp.GetDailyVolume(6211));
        System.out.println(temp.GetDailyVolume(1141));
        System.out.println(temp.GetDailyVolume(4587));
        System.out.println(temp.GetDailyVolume(554));
    }

    private static Tuple2<Integer, Integer> SumVolume(GETimeSeriesData[] data)
    {
        if(data.length <= 1)
        {
            return null;
        }

        double trim = 0.1;
        int    Low  = Arrays.stream(data).mapToInt(t -> t.lowPriceVolume).sum();
        int    High = 0;
        for(var entry : data)
        {
            Low += entry.lowPriceVolume;
            High += entry.highPriceVolume;
        }

        return new Tuple2<>(Low, High);
    }

    public static long GetNetWorth()
    {
        return GetTotalValue(GEInstance.GetAllItems()) + GEInstance.GetLiquidMoney();
    }


}
