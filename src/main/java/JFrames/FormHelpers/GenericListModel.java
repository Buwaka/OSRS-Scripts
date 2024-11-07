package JFrames.FormHelpers;

import JFrames.EquipmentConfiguration;

import javax.swing.*;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.function.Predicate;

public class GenericListModel<T> extends AbstractListModel<T>
{
    @Serial
    private static final long serialVersionUID = -601156788382158028L;

    List<T> items = new ArrayList<>();

    public List<T> getList()
    {
        return items;
    }

    public void set(List<T> overwrite)
    {
        items = new ArrayList<>(overwrite);
    }

    @Override
    public int getSize()
    {
        return items.size();
    }

    @Override
    public T getElementAt(int index)
    {
        if(items.size() < index)
            return null;

        return items.get(index);
    }

    public void add(T entry)
    {
        items.add(entry);
    }

    public void removeIf(Predicate<T> condition)
    {
        items.removeIf(condition);
    }
    public void remove(T entry)
    {
        items.remove(entry);
    }

    public void remove(T... entry)
    {
        for(var en : entry)
        {
            items.remove(en);
        }
    }

    public void remove(int entry)
    {
        if(items.size() < entry)
            return;

        items.remove(entry);
    }

    public void remove(int... entry)
    {
        for(var en : entry)
        {
            if(items.size() < en)
                continue;

            items.remove(en);
        }
    }

    public void remove(List<T> selected) {
        for(var entry : selected)
        {
            remove(entry);
        }
    }

    public void clear()
    {
        items.clear();
    }

    public void swap(int i1, int i2)
    {
        if(items.size() < Math.max(i1,i2))
            return;
        Collections.swap(items, i1, i2);
    }
}
