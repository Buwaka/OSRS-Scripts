package Utilities;

import Utilities.Scripting.Logger;
import org.jetbrains.annotations.Range;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;

public class NameDictionary
{
    private static final Random       random = new Random();
    public static        List<String> Words  = null;

    @Range(from = 3, to = 11)
    public static String GetRandomWord(int length)
    {
        ReadWords();
        int    index = random.nextInt(Words.size());
        String word  = Words.get(index);
        while(word.length() > length)
        {
            index = index / 2;
            word  = Words.get(index);
        }
        return word;
    }

    private static void ReadWords()
    {
        if(Words != null)
        {
            return;
        }
        try
        {
            Words = getResourceFileAsString("NameDictionary/NameDictionary.txt");
            if(Words == null)
            {
                Logger.log("Failed to load NameDictionary: " + Words);
            }
            else
            {
                Logger.log("NameDictionarySize: " + Words.size());
            }

        } catch(Exception e)
        {
            Logger.log("Failed to load NameDictionary: " + e);
        }

    }

    /**
     * Reads given resource file as a string.
     *
     * @param fileName path to the resource file
     *
     * @return the file's contents
     *
     * @throws IOException if read fails for any reason
     */
    static List<String> getResourceFileAsString(String fileName) throws IOException
    {
        ClassLoader classLoader = NameDictionary.class.getClassLoader();
        try(InputStream is = classLoader.getResourceAsStream(fileName))
        {
            if(is == null)
            {
                Logger.log("Could not find resource");
                return null;
            }
            try(InputStreamReader isr = new InputStreamReader(is);
                BufferedReader reader = new BufferedReader(isr))
            {
                return reader.lines().toList();
            }
        }
    }

    public static void main(String[] args)
    {
        ReadWords();
        System.out.print(GetRandomWord());
    }

    public static String GetRandomWord()
    {
        ReadWords();
        int index = random.nextInt(Words.size());
        return Words.get(index);
    }
}
