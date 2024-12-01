/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package IF.JFrames;

import IF.JFrames.FormHelpers.GenericListModel;
import IF.OSRSDatabase.ItemDB;
import IF.Utilities.OSRSUtilities;
import IF.Utilities.Patterns.Delegates.Delegate1;
import IF.Utilities.Scripting.Logger;
import com.google.gson.Gson;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author SammyLaptop
 */
public class EquipmentConfiguration extends javax.swing.JPanel implements OSRSForm
{

    private final Field[] EquipmentStats = ItemDB.EquipmentData.class.getFields();
    Map<JLabel, ItemDB.EquipmentData.EquipmentSlot>                SlotMapping  = new HashMap<>();
    Map<ItemDB.EquipmentData.EquipmentSlot, List<ItemDB.ItemData>> AllEquipment = new HashMap<>();
    private EquipmentFilter    CurrentFilter    = EquipmentFilter.No_Filter;
    private javax.swing.JLabel CurrentLabel     = null;
    private File               CurrentSafeFile  = null;
    private Border             SelectBorder     = javax.swing.BorderFactory.createLineBorder(new java.awt.Color(
            255,
            153,
            153), 5);
    private Border             DeselectBorder   = javax.swing.BorderFactory.createLineBorder(new java.awt.Color(
            255,
            153,
            153), 0);
    private Object             CurrentSortValue = ExtraEquipmentSort.Total;
    private boolean            Untradables      = false;
    private String             StringFilter     = null;
    public Delegate1<List<LoadoutEntryWrapper>> onSave = new Delegate1<>();

    public enum LoadoutOrder
    {
        InOrder,
        Randomize
    }

    public enum EquipmentFilter
    {
        No_Filter,
        F2P,
        NoCosmetic,
        Cosmetic,
        P2P
    }

    public enum ExtraEquipmentSort
    {
        Total,
        ItemID,
        Price,
        Custom
    }

    public void Init()
    {
        // All
        SlotMapping.put(AllGearSlot, null);
        // Head
        SlotMapping.put(HeadGearLabel, ItemDB.EquipmentData.EquipmentSlot.head);
        // Cape
        SlotMapping.put(CapeGearSlot, ItemDB.EquipmentData.EquipmentSlot.cape);
        // Neck
        SlotMapping.put(NeckGearSlot, ItemDB.EquipmentData.EquipmentSlot.neck);
        // Ammo
        SlotMapping.put(AmmoGearSlot, ItemDB.EquipmentData.EquipmentSlot.ammo);
        // Weapon
        SlotMapping.put(WeaponGearSlot, ItemDB.EquipmentData.EquipmentSlot.weapon);
        // Body
        SlotMapping.put(BodyGearSlot, ItemDB.EquipmentData.EquipmentSlot.body);
        // Shield
        SlotMapping.put(ShieldGearSlot, ItemDB.EquipmentData.EquipmentSlot.shield);
        // Legs
        SlotMapping.put(PantsGearSlot, ItemDB.EquipmentData.EquipmentSlot.legs);
        // Hands
        SlotMapping.put(HandGearSlot, ItemDB.EquipmentData.EquipmentSlot.hands);
        // Feet
        SlotMapping.put(FeetGearSlot, ItemDB.EquipmentData.EquipmentSlot.feet);
        // Ring
        SlotMapping.put(RingGearSlot, ItemDB.EquipmentData.EquipmentSlot.ring);

        InitEquipmentMap();
    }

    /**
     * Creates new form EquipmentConfiguration
     */
    public EquipmentConfiguration()
    {
        initComponents();
    }

    public class EquipmentEntryWrapper
    {
        public ItemDB.ItemData itemData;
        public boolean         AllowPurchase = false;

        public EquipmentEntryWrapper(ItemDB.ItemData data)
        {
            itemData = data;
        }

        public EquipmentEntryWrapper(LoadoutEntryWrapper.LoadoutEquipment equip)
        {
            itemData      = equip.item;
            AllowPurchase = equip.allow_purchase;
        }

        @Override
        public String toString()
        {
            return itemData == null
                    ? "null"
                    : itemData.wiki_name + " (" + (GetSortableEquipmentValue(this) + ")");
        }
    }

    public class LoadoutEntryWrapper implements Serializable
    {
        @Serial
        private static final long serialVersionUID = -3768305870313639394L;

        public static class LoadoutEquipment implements Serializable
        {
            @Serial
            private static final long serialVersionUID = -5670899445037684010L;

            public ItemDB.ItemData item;
            public boolean         allow_purchase = false;

            public LoadoutEquipment(ItemDB.ItemData item, boolean AllowPurchase)
            {
                this.item      = item;
                allow_purchase = AllowPurchase;
            }
        }

        @Override
        public String toString()
        {
            return Arrays.toString(Equipment.stream().map((t) -> t.item.wiki_name).toArray());
        }

        public List<LoadoutEquipment> Equipment = new ArrayList<>();
        public LoadoutOrder           Order     = LoadoutOrder.InOrder;

        public LoadoutEntryWrapper(List<EquipmentEntryWrapper> data)
        {
            ReadEntries(data);
        }

        public LoadoutEntryWrapper(List<EquipmentEntryWrapper> data, LoadoutOrder order)
        {
            ReadEntries(data);
            Order = order;
        }

        private void ReadEntries(List<EquipmentEntryWrapper> data)
        {
            for(var entry : data)
            {
                Equipment.add(new LoadoutEquipment(entry.itemData, entry.AllowPurchase));
            }
        }
    }



    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        var form = new EquipmentConfiguration();
        var gui = FrameUtilities.OpenGui("Equipment Configurator", form.GetForm());
        form.setEnableSave(true);
        form.onSave.Subscribe(gui, (equips) ->
        {
            System.out.print("Saved! \n");
            System.out.print(Arrays.toString(equips.toArray()));
            FrameUtilities.CloseGui(gui);
        });
        System.out.print("End of Main! \n");
    }

    public void setEnableSave(boolean b)
    {
        SaveFileButton.setEnabled(b);
    }

    @Override
    public Container GetForm()
    {
        return this;
    }

    private List<EquipmentEntryWrapper> FilterSortItemList(List<ItemDB.ItemData> items, EquipmentFilter filter)
    {
        return FilterSortItemList(items.stream().map(EquipmentEntryWrapper::new).toList());
    }

    private List<EquipmentEntryWrapper> FilterSortItemList(List<EquipmentEntryWrapper> items)
    {
        List<EquipmentEntryWrapper>   out    = new Vector<>();
        Stream<EquipmentEntryWrapper> Filter = items.stream();
        if(StringFilter != null)
        {
            Filter = Filter.filter((t) -> t.itemData.name.toLowerCase()
                                                         .contains(StringFilter.toLowerCase()));
        }
        if(!Untradables)
        {
            Filter = Filter.filter((t) -> t.itemData.tradeable_on_ge);
        }

        switch(CurrentFilter)
        {
            case No_Filter ->
            {
            }
            case F2P ->
            {
                Filter = Filter.filter((t) -> !t.itemData.members);
            }
            case NoCosmetic ->
            {
                Filter = Filter.filter((t) -> !t.itemData.equipment.isCosmetic());
            }
            case Cosmetic ->
            {
                Filter = Filter.filter((t) -> t.itemData.equipment.isCosmetic());
            }
            case P2P ->
            {
                Filter = Filter.filter((t) -> t.itemData.members);
            }
        }

        Filter.forEach((t) -> out.add(t));
        if(CurrentSortValue != ExtraEquipmentSort.Custom)
        {
            out.sort(Comparator.comparing(this::GetSortableEquipmentValue).reversed());
        }
        return out;
    }

    private String[] GetSortOptionList()
    {
        List<String> out = new ArrayList<>();
        // extra enums sorts
        out.addAll(Arrays.stream(ExtraEquipmentSort.values()).map((t) -> t.name()).toList());

        // equipment stat sorts
        for(var field : EquipmentStats)
        {
            if(field.getType() != int.class) {continue;}

            out.add(field.getName());
        }
        return out.toArray(new String[0]);
    }

    private int GetSortableEquipmentValue(EquipmentEntryWrapper item)
    {
        if(CurrentSortValue instanceof Field &&
           ((Field) CurrentSortValue).getDeclaringClass() == ItemDB.EquipmentData.class)
        {
            try
            {
                return (int) ((Field) CurrentSortValue).get(item.itemData.equipment);
            } catch(Exception e)
            {
            }
        }
        else if(CurrentSortValue instanceof ExtraEquipmentSort)
        {
            if(CurrentSortValue == ExtraEquipmentSort.Total)
            {
                return item.itemData.equipment != null ? item.itemData.equipment.GetTotal() : 0;
            }
            else if(CurrentSortValue == ExtraEquipmentSort.Price)
            {
                var cost = item.itemData.cost;
                if(item.itemData.highalch != null)
                {
                    cost = Math.max(cost, item.itemData.highalch);
                }
                return cost;
            }
            else if(CurrentSortValue == ExtraEquipmentSort.ItemID)
            {
                return item.itemData.id;
            }
        }

        return 0;
    }

    private void InitEquipmentMap()
    {
        var all = ItemDB.GetAllEquipment(true);

        for(var slot : ItemDB.EquipmentData.EquipmentSlot.values())
        {
            AllEquipment.put(slot, new ArrayList<>());
        }
        for(var item : all)
        {
            if(item.equipment == null) {continue;}
            var slot = item.equipment.slot;
            if(slot == ItemDB.EquipmentData.EquipmentSlot.two_handed)
            {
                slot = ItemDB.EquipmentData.EquipmentSlot.weapon;
            }
            AllEquipment.get(slot).add(item);
        }
    }

    void AddItemsForSlot()
    {
        List<ItemDB.ItemData> Items = new ArrayList<>();
        if(CurrentLabel != null && SlotMapping.containsKey(CurrentLabel))
        {
            var slot = SlotMapping.get(CurrentLabel);
            if(slot != null && AllEquipment != null)
            {
                Items = AllEquipment.get(slot);
            }
            else if(slot == null)
            {
                for(var entry : AllEquipment.values())
                {
                    Items.addAll(entry);
                }
            }
        }
        SetEquipmentList(Items);
    }

    private void SetEquipmentList(List<ItemDB.ItemData> Items)
    {
        GenericListModel<EquipmentEntryWrapper> model = (GenericListModel<EquipmentEntryWrapper>) EquipmentList.getModel();
        if(model != null)
        {
            model.set(Items.stream().map((t) -> new EquipmentEntryWrapper(t)).toList());
        }
    }

    private void RefreshEquipmentList()
    {
        GenericListModel<EquipmentEntryWrapper> model = (GenericListModel<EquipmentEntryWrapper>) EquipmentList.getModel();

        if(model != null)
        {
            var Items = model.getList();
            if(Items != null)
            {
                model.set(FilterSortItemList(Items));
                EquipmentList.clearSelection();
                EquipmentList.updateUI();
            }
        }

        AlowPurchaseCheckBox.setSelected(false);
        if(((String) SortByComboBox.getSelectedItem()).equalsIgnoreCase(ExtraEquipmentSort.Custom.name()))
        {
            SwapLoadoutButton.setEnabled(true);
        }
    }

    public static File ensureExtension(File file, String extension)
    {
        // Check if the extension is already present (case-insensitive)
        if(file.getName().toLowerCase().endsWith("." + extension.toLowerCase()))
        {
            return file; // No change needed
        }
        else
        {
            // Add the extension
            return new File(file.getAbsolutePath() + "." + extension);
        }
    }
    //GEN-BEGIN

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jSplitPane1              = new javax.swing.JSplitPane();
        panel1                   = new java.awt.Panel();
        jLabel7                  = new javax.swing.JLabel();
        HeadGearLabel            = new javax.swing.JLabel();
        AllGearSlot              = new javax.swing.JLabel();
        CapeGearSlot             = new javax.swing.JLabel();
        NeckGearSlot             = new javax.swing.JLabel();
        AmmoGearSlot             = new javax.swing.JLabel();
        WeaponGearSlot           = new javax.swing.JLabel();
        BodyGearSlot             = new javax.swing.JLabel();
        ShieldGearSlot           = new javax.swing.JLabel();
        jLabel24                 = new javax.swing.JLabel();
        PantsGearSlot            = new javax.swing.JLabel();
        jLabel26                 = new javax.swing.JLabel();
        HandGearSlot             = new javax.swing.JLabel();
        FeetGearSlot             = new javax.swing.JLabel();
        RingGearSlot             = new javax.swing.JLabel();
        panel2                   = new java.awt.Panel();
        EquipmentPanel           = new javax.swing.JPanel();
        jLabel3                  = new javax.swing.JLabel();
        SortByComboBox           = new JComboBox(GetSortOptionList());
        jLabel2                  = new javax.swing.JLabel();
        jScrollPane1             = new javax.swing.JScrollPane();
        EquipmentList            = new javax.swing.JList<>();
        SearchBar                = new javax.swing.JTextField();
        EquipmentFilterComboBox  = new JComboBox(EquipmentFilter.values());
        jLabel4                  = new javax.swing.JLabel();
        jDesktopPane1            = new javax.swing.JDesktopPane();
        AddLoadoutButton         = new javax.swing.JButton();
        AlowPurchaseCheckBox     = new javax.swing.JCheckBox();
        LoadoutOrderComboBox     = new JComboBox(LoadoutOrder.values());
        UntradablesCheckbox      = new javax.swing.JCheckBox();
        EquipmentEntrySwapButton = new javax.swing.JButton();
        EquipmentRemoveButton    = new javax.swing.JButton();
        LoadoutPanel             = new javax.swing.JPanel();
        DeleteLoadoutButton      = new javax.swing.JButton();
        ClearLoadoutButton       = new javax.swing.JButton();
        SwapLoadoutButton        = new javax.swing.JButton();
        jScrollPane2             = new javax.swing.JScrollPane();
        LoadoutList              = new javax.swing.JList<>();
        jLabel1                  = new javax.swing.JLabel();
        ExportButton             = new javax.swing.JButton();
        ImportButton             = new javax.swing.JButton();
        SaveFileButton           = new javax.swing.JButton();

        setForeground(new java.awt.Color(0, 0, 0));

        panel1.setBackground(new java.awt.Color(0, 0, 0));
        panel1.setMinimumSize(new java.awt.Dimension(300, 100));
        panel1.setPreferredSize(new java.awt.Dimension(500, 485));
        panel1.setLayout(new java.awt.GridLayout(5, 3));

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setFocusable(false);
        jLabel7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel7.setIconTextGap(10);
        jLabel7.setName(""); // NOI18N
        panel1.add(jLabel7);

        HeadGearLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HeadGearLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource(
                "/icons/EquipmentSlots/Head_slot.jpg"))); // NOI18N
        HeadGearLabel.setBorder(DeselectBorder);
        HeadGearLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        HeadGearLabel.setIconTextGap(10);
        HeadGearLabel.setName(""); // NOI18N
        HeadGearLabel.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                GearButtonClick(evt);
            }
        });
        panel1.add(HeadGearLabel);

        AllGearSlot.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        AllGearSlot.setIcon(new javax.swing.ImageIcon(getClass().getResource(
                "/icons/EquipmentSlots/Worn_Equipment.jpg"))); // NOI18N
        AllGearSlot.setFocusable(false);
        AllGearSlot.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        AllGearSlot.setIconTextGap(10);
        AllGearSlot.setName(""); // NOI18N
        AllGearSlot.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                GearButtonClick(evt);
            }
        });
        panel1.add(AllGearSlot);

        CapeGearSlot.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        CapeGearSlot.setIcon(new javax.swing.ImageIcon(getClass().getResource(
                "/icons/EquipmentSlots/Cape_slot.jpg"))); // NOI18N
        CapeGearSlot.setBorder(DeselectBorder);
        CapeGearSlot.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        CapeGearSlot.setIconTextGap(10);
        CapeGearSlot.setName(""); // NOI18N
        CapeGearSlot.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                GearButtonClick(evt);
            }
        });
        panel1.add(CapeGearSlot);

        NeckGearSlot.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        NeckGearSlot.setIcon(new javax.swing.ImageIcon(getClass().getResource(
                "/icons/EquipmentSlots/Neck_slot.jpg"))); // NOI18N
        NeckGearSlot.setBorder(DeselectBorder);
        NeckGearSlot.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        NeckGearSlot.setIconTextGap(10);
        NeckGearSlot.setName(""); // NOI18N
        NeckGearSlot.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                GearButtonClick(evt);
            }
        });
        panel1.add(NeckGearSlot);

        AmmoGearSlot.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        AmmoGearSlot.setIcon(new javax.swing.ImageIcon(getClass().getResource(
                "/icons/EquipmentSlots/Ammo_slot.jpg"))); // NOI18N
        AmmoGearSlot.setBorder(DeselectBorder);
        AmmoGearSlot.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        AmmoGearSlot.setIconTextGap(10);
        AmmoGearSlot.setName(""); // NOI18N
        AmmoGearSlot.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                GearButtonClick(evt);
            }
        });
        panel1.add(AmmoGearSlot);

        WeaponGearSlot.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        WeaponGearSlot.setIcon(new javax.swing.ImageIcon(getClass().getResource(
                "/icons/EquipmentSlots/Weapon_slot.jpg"))); // NOI18N
        WeaponGearSlot.setBorder(DeselectBorder);
        WeaponGearSlot.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        WeaponGearSlot.setIconTextGap(10);
        WeaponGearSlot.setName(""); // NOI18N
        WeaponGearSlot.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                GearButtonClick(evt);
            }
        });
        panel1.add(WeaponGearSlot);

        BodyGearSlot.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        BodyGearSlot.setIcon(new javax.swing.ImageIcon(getClass().getResource(
                "/icons/EquipmentSlots/Body_slot.jpg"))); // NOI18N
        BodyGearSlot.setBorder(DeselectBorder);
        BodyGearSlot.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BodyGearSlot.setIconTextGap(10);
        BodyGearSlot.setName(""); // NOI18N
        BodyGearSlot.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                GearButtonClick(evt);
            }
        });
        panel1.add(BodyGearSlot);

        ShieldGearSlot.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ShieldGearSlot.setIcon(new javax.swing.ImageIcon(getClass().getResource(
                "/icons/EquipmentSlots/Shield_slot.jpg"))); // NOI18N
        ShieldGearSlot.setBorder(DeselectBorder);
        ShieldGearSlot.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ShieldGearSlot.setIconTextGap(10);
        ShieldGearSlot.setName(""); // NOI18N
        ShieldGearSlot.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                GearButtonClick(evt);
            }
        });
        panel1.add(ShieldGearSlot);

        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel24.setFocusable(false);
        jLabel24.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel24.setIconTextGap(10);
        jLabel24.setName(""); // NOI18N
        panel1.add(jLabel24);

        PantsGearSlot.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        PantsGearSlot.setIcon(new javax.swing.ImageIcon(getClass().getResource(
                "/icons/EquipmentSlots/Legs_slot.jpg"))); // NOI18N
        PantsGearSlot.setBorder(DeselectBorder);
        PantsGearSlot.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        PantsGearSlot.setIconTextGap(10);
        PantsGearSlot.setName(""); // NOI18N
        PantsGearSlot.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                GearButtonClick(evt);
            }
        });
        panel1.add(PantsGearSlot);

        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel26.setFocusable(false);
        jLabel26.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel26.setIconTextGap(10);
        jLabel26.setName(""); // NOI18N
        panel1.add(jLabel26);

        HandGearSlot.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        HandGearSlot.setIcon(new javax.swing.ImageIcon(getClass().getResource(
                "/icons/EquipmentSlots/Hands_slot.jpg"))); // NOI18N
        HandGearSlot.setBorder(DeselectBorder);
        HandGearSlot.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        HandGearSlot.setIconTextGap(10);
        HandGearSlot.setName(""); // NOI18N
        HandGearSlot.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                GearButtonClick(evt);
            }
        });
        panel1.add(HandGearSlot);

        FeetGearSlot.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        FeetGearSlot.setIcon(new javax.swing.ImageIcon(getClass().getResource(
                "/icons/EquipmentSlots/Feet_slot.jpg"))); // NOI18N
        FeetGearSlot.setToolTipText("");
        FeetGearSlot.setBorder(DeselectBorder);
        FeetGearSlot.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        FeetGearSlot.setIconTextGap(10);
        FeetGearSlot.setName(""); // NOI18N
        FeetGearSlot.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                GearButtonClick(evt);
            }
        });
        panel1.add(FeetGearSlot);

        RingGearSlot.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        RingGearSlot.setIcon(new javax.swing.ImageIcon(getClass().getResource(
                "/icons/EquipmentSlots/Ring_slot.jpg"))); // NOI18N
        RingGearSlot.setBorder(DeselectBorder);
        RingGearSlot.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        RingGearSlot.setIconTextGap(10);
        RingGearSlot.setName(""); // NOI18N
        RingGearSlot.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                GearButtonClick(evt);
            }
        });
        panel1.add(RingGearSlot);

        jSplitPane1.setLeftComponent(panel1);

        panel2.setBackground(new java.awt.Color(0, 0, 0));

        jLabel3.setText("sort by...");

        SortByComboBox.setSelectedItem(GetSortOptionList()[0]);
        SortByComboBox.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(java.awt.event.ItemEvent evt)
            {
                onSortByChanged(evt);
            }
        });

        jLabel2.setText("Equipment List");

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        EquipmentList.setModel(new GenericListModel<EquipmentEntryWrapper>());
        EquipmentList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                EquipmentListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(EquipmentList);

        SearchBar.setToolTipText("");
        SearchBar.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                SearchBarActionPerformed(evt);
            }
        });

        EquipmentFilterComboBox.setToolTipText(
                "<html>No_Filter: All items.<br>\nF2P: Non-member items only.<br>\nNoCosmetic: only items with stats.<br>\nP2P: Member items only.<br>\nUntradables: Untradable items only.</html>");
        EquipmentFilterComboBox.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(java.awt.event.ItemEvent evt)
            {
                onFilterByChanged(evt);
            }
        });

        jLabel4.setText("Filter by...");

        AddLoadoutButton.setText("=>");
        AddLoadoutButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                AddLoadoutButtonActionPerformed(evt);
            }
        });

        AlowPurchaseCheckBox.setText("Allow purchase");
        AlowPurchaseCheckBox.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                AllowPurchaseCheckBoxActionPerformed(evt);
            }
        });

        jDesktopPane1.setLayer(AddLoadoutButton, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jDesktopPane1.setLayer(AlowPurchaseCheckBox, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jDesktopPane1.setLayer(LoadoutOrderComboBox, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jDesktopPane1Layout = new javax.swing.GroupLayout(jDesktopPane1);
        jDesktopPane1.setLayout(jDesktopPane1Layout);
        jDesktopPane1Layout.setHorizontalGroup(jDesktopPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                  .addGroup(jDesktopPane1Layout.createSequentialGroup()
                                                                                               .addContainerGap()
                                                                                               .addGroup(
                                                                                                       jDesktopPane1Layout.createParallelGroup(
                                                                                                                                  javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                          .addComponent(
                                                                                                                                  AlowPurchaseCheckBox,
                                                                                                                                  javax.swing.GroupLayout.Alignment.TRAILING)
                                                                                                                          .addGroup(
                                                                                                                                  jDesktopPane1Layout.createParallelGroup(
                                                                                                                                                             javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                                                     .addComponent(
                                                                                                                                                             AddLoadoutButton,
                                                                                                                                                             javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                                                                             javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                             55,
                                                                                                                                                             javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                     .addComponent(
                                                                                                                                                             LoadoutOrderComboBox,
                                                                                                                                                             javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                                                                             javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                             104,
                                                                                                                                                             javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                                                               .addContainerGap(
                                                                                                       javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                       Short.MAX_VALUE)));
        jDesktopPane1Layout.setVerticalGroup(jDesktopPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                .addGroup(jDesktopPane1Layout.createSequentialGroup()
                                                                                             .addContainerGap()
                                                                                             .addComponent(
                                                                                                     AlowPurchaseCheckBox)
                                                                                             .addPreferredGap(
                                                                                                     javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                             .addComponent(
                                                                                                     LoadoutOrderComboBox,
                                                                                                     javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                     javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                     javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                             .addPreferredGap(
                                                                                                     javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                             .addComponent(
                                                                                                     AddLoadoutButton)
                                                                                             .addContainerGap()));

        UntradablesCheckbox.setText("Untradables");
        UntradablesCheckbox.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                UntradablesCheckboxActionPerformed(evt);
            }
        });

        EquipmentEntrySwapButton.setText("Swap");
        EquipmentEntrySwapButton.setEnabled(false);
        EquipmentEntrySwapButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                EquipmentEntrySwapButtonActionPerformed(evt);
            }
        });

        EquipmentRemoveButton.setText("Remove");
        EquipmentRemoveButton.setEnabled(false);
        EquipmentRemoveButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                EquipmentRemoveButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout EquipmentPanelLayout = new javax.swing.GroupLayout(EquipmentPanel);
        EquipmentPanel.setLayout(EquipmentPanelLayout);
        EquipmentPanelLayout.setHorizontalGroup(EquipmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                    .addGroup(EquipmentPanelLayout.createSequentialGroup()
                                                                                                  .addContainerGap()
                                                                                                  .addGroup(
                                                                                                          EquipmentPanelLayout.createParallelGroup(
                                                                                                                                      javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                              .addGroup(
                                                                                                                                      EquipmentPanelLayout.createSequentialGroup()
                                                                                                                                                          .addGroup(
                                                                                                                                                                  EquipmentPanelLayout.createParallelGroup(
                                                                                                                                                                                              javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                                                                                      .addGroup(
                                                                                                                                                                                              EquipmentPanelLayout.createSequentialGroup()
                                                                                                                                                                                                                  .addGap(0,
                                                                                                                                                                                                                          0,
                                                                                                                                                                                                                          Short.MAX_VALUE)
                                                                                                                                                                                                                  .addGroup(
                                                                                                                                                                                                                          EquipmentPanelLayout.createParallelGroup(
                                                                                                                                                                                                                                                      javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                                                                                                                                                                      false)
                                                                                                                                                                                                                                              .addComponent(
                                                                                                                                                                                                                                                      jScrollPane1,
                                                                                                                                                                                                                                                      javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                                                                                                                                                                      javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                                                                                                      353,
                                                                                                                                                                                                                                                      javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                                                                                                              .addGroup(
                                                                                                                                                                                                                                                      javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                                                                                                                                                                      EquipmentPanelLayout.createSequentialGroup()
                                                                                                                                                                                                                                                                          .addComponent(
                                                                                                                                                                                                                                                                                  jLabel2)
                                                                                                                                                                                                                                                                          .addPreferredGap(
                                                                                                                                                                                                                                                                                  javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                                                                                                                                                                                                                                  javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                                                                                                                                  Short.MAX_VALUE)
                                                                                                                                                                                                                                                                          .addGroup(
                                                                                                                                                                                                                                                                                  EquipmentPanelLayout.createParallelGroup(
                                                                                                                                                                                                                                                                                                              javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                                                                                                                                                                                                      .addComponent(
                                                                                                                                                                                                                                                                                                              jLabel3)
                                                                                                                                                                                                                                                                                                      .addComponent(
                                                                                                                                                                                                                                                                                                              SortByComboBox,
                                                                                                                                                                                                                                                                                                              javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                                                                                                                                                              120,
                                                                                                                                                                                                                                                                                                              javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                                                                                                                                                                      .addGroup(
                                                                                                                                                                                              EquipmentPanelLayout.createSequentialGroup()
                                                                                                                                                                                                                  .addGroup(
                                                                                                                                                                                                                          EquipmentPanelLayout.createParallelGroup(
                                                                                                                                                                                                                                                      javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                                                                                                                                                                      false)
                                                                                                                                                                                                                                              .addGroup(
                                                                                                                                                                                                                                                      EquipmentPanelLayout.createSequentialGroup()
                                                                                                                                                                                                                                                                          .addComponent(
                                                                                                                                                                                                                                                                                  EquipmentFilterComboBox,
                                                                                                                                                                                                                                                                                  javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                                                                                                                                  104,
                                                                                                                                                                                                                                                                                  javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                                                                                                                                          .addPreferredGap(
                                                                                                                                                                                                                                                                                  javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                                                                                                                                                                                          .addComponent(
                                                                                                                                                                                                                                                                                  UntradablesCheckbox))
                                                                                                                                                                                                                                              .addComponent(
                                                                                                                                                                                                                                                      jLabel4)
                                                                                                                                                                                                                                              .addComponent(
                                                                                                                                                                                                                                                      SearchBar))
                                                                                                                                                                                                                  .addGap(31,
                                                                                                                                                                                                                          31,
                                                                                                                                                                                                                          31)
                                                                                                                                                                                                                  .addComponent(
                                                                                                                                                                                                                          jDesktopPane1)))
                                                                                                                                                          .addGap(42,
                                                                                                                                                                  42,
                                                                                                                                                                  42))
                                                                                                                              .addGroup(
                                                                                                                                      EquipmentPanelLayout.createSequentialGroup()
                                                                                                                                                          .addComponent(
                                                                                                                                                                  EquipmentEntrySwapButton)
                                                                                                                                                          .addPreferredGap(
                                                                                                                                                                  javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                                                                          .addComponent(
                                                                                                                                                                  EquipmentRemoveButton)
                                                                                                                                                          .addContainerGap(
                                                                                                                                                                  javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                  Short.MAX_VALUE)))));
        EquipmentPanelLayout.setVerticalGroup(EquipmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                  .addGroup(EquipmentPanelLayout.createSequentialGroup()
                                                                                                .addComponent(
                                                                                                        jLabel3)
                                                                                                .addGap(4,
                                                                                                        4,
                                                                                                        4)
                                                                                                .addGroup(
                                                                                                        EquipmentPanelLayout.createParallelGroup(
                                                                                                                                    javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                                            .addComponent(
                                                                                                                                    SortByComboBox,
                                                                                                                                    javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                    javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                    javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                            .addComponent(
                                                                                                                                    jLabel2))
                                                                                                .addPreferredGap(
                                                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                .addComponent(
                                                                                                        jScrollPane1,
                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                        437,
                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addPreferredGap(
                                                                                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                .addGroup(
                                                                                                        EquipmentPanelLayout.createParallelGroup(
                                                                                                                                    javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                            .addGroup(
                                                                                                                                    EquipmentPanelLayout.createSequentialGroup()
                                                                                                                                                        .addComponent(
                                                                                                                                                                SearchBar,
                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                        .addPreferredGap(
                                                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                                                                        .addComponent(
                                                                                                                                                                jLabel4)
                                                                                                                                                        .addPreferredGap(
                                                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                                                        .addGroup(
                                                                                                                                                                EquipmentPanelLayout.createParallelGroup(
                                                                                                                                                                                            javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                                                                                                    .addComponent(
                                                                                                                                                                                            EquipmentFilterComboBox,
                                                                                                                                                                                            javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                                            javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                                            javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                                                    .addComponent(
                                                                                                                                                                                            UntradablesCheckbox)))
                                                                                                                            .addComponent(
                                                                                                                                    jDesktopPane1,
                                                                                                                                    javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                    javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                    javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                                .addGap(10,
                                                                                                        10,
                                                                                                        10)
                                                                                                .addGroup(
                                                                                                        EquipmentPanelLayout.createParallelGroup(
                                                                                                                                    javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                                            .addComponent(
                                                                                                                                    EquipmentEntrySwapButton)
                                                                                                                            .addComponent(
                                                                                                                                    EquipmentRemoveButton))
                                                                                                .addContainerGap(
                                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                        Short.MAX_VALUE)));

        DeleteLoadoutButton.setText("Delete");
        DeleteLoadoutButton.setToolTipText("");
        DeleteLoadoutButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                DeleteLoadoutButtonActionPerformed(evt);
            }
        });

        ClearLoadoutButton.setText("Clear");
        ClearLoadoutButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                ClearLoadoutButtonActionPerformed(evt);
            }
        });

        SwapLoadoutButton.setText("Swap");
        SwapLoadoutButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                SwapLoadoutButtonActionPerformed(evt);
            }
        });

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        LoadoutList.setModel(new GenericListModel<LoadoutEntryWrapper>());
        LoadoutList.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                LoadoutListMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(LoadoutList);

        jLabel1.setText("Loadout Priority");

        ExportButton.setText("Export Loadout");
        ExportButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                ExportButtonActionPerformed(evt);
            }
        });

        ImportButton.setText("Import Loadout");
        ImportButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                ImportButtonActionPerformed(evt);
            }
        });

        SaveFileButton.setText("Save");
        SaveFileButton.setEnabled(false);
        SaveFileButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                SaveFileButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout LoadoutPanelLayout = new javax.swing.GroupLayout(LoadoutPanel);
        LoadoutPanel.setLayout(LoadoutPanelLayout);
        LoadoutPanelLayout.setHorizontalGroup(LoadoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                .addGroup(LoadoutPanelLayout.createSequentialGroup()
                                                                                            .addGroup(
                                                                                                    LoadoutPanelLayout.createParallelGroup(
                                                                                                                              javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                      .addGroup(
                                                                                                                              LoadoutPanelLayout.createSequentialGroup()
                                                                                                                                                .addContainerGap()
                                                                                                                                                .addGroup(
                                                                                                                                                        LoadoutPanelLayout.createParallelGroup(
                                                                                                                                                                                  javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                                                                          .addComponent(
                                                                                                                                                                                  jLabel1)
                                                                                                                                                                          .addGroup(
                                                                                                                                                                                  LoadoutPanelLayout.createParallelGroup(
                                                                                                                                                                                                            javax.swing.GroupLayout.Alignment.TRAILING)
                                                                                                                                                                                                    .addGroup(
                                                                                                                                                                                                            LoadoutPanelLayout.createSequentialGroup()
                                                                                                                                                                                                                              .addComponent(
                                                                                                                                                                                                                                      DeleteLoadoutButton)
                                                                                                                                                                                                                              .addGap(54,
                                                                                                                                                                                                                                      54,
                                                                                                                                                                                                                                      54)
                                                                                                                                                                                                                              .addComponent(
                                                                                                                                                                                                                                      SwapLoadoutButton)
                                                                                                                                                                                                                              .addGap(59,
                                                                                                                                                                                                                                      59,
                                                                                                                                                                                                                                      59)
                                                                                                                                                                                                                              .addComponent(
                                                                                                                                                                                                                                      ClearLoadoutButton))
                                                                                                                                                                                                    .addComponent(
                                                                                                                                                                                                            jScrollPane2,
                                                                                                                                                                                                            javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                                                            329,
                                                                                                                                                                                                            javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                                                                    .addGroup(
                                                                                                                                                                                                            javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                                                                                                                            LoadoutPanelLayout.createSequentialGroup()
                                                                                                                                                                                                                              .addGap(46,
                                                                                                                                                                                                                                      46,
                                                                                                                                                                                                                                      46)
                                                                                                                                                                                                                              .addComponent(
                                                                                                                                                                                                                                      ExportButton)
                                                                                                                                                                                                                              .addPreferredGap(
                                                                                                                                                                                                                                      javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                                                                                                                                              .addComponent(
                                                                                                                                                                                                                                      ImportButton)))))
                                                                                                                      .addGroup(
                                                                                                                              LoadoutPanelLayout.createSequentialGroup()
                                                                                                                                                .addGap(135,
                                                                                                                                                        135,
                                                                                                                                                        135)
                                                                                                                                                .addComponent(
                                                                                                                                                        SaveFileButton)))
                                                                                            .addContainerGap(
                                                                                                    20,
                                                                                                    Short.MAX_VALUE)));
        LoadoutPanelLayout.setVerticalGroup(LoadoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                              .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                                        LoadoutPanelLayout.createSequentialGroup()
                                                                                          .addGap(20,
                                                                                                  20,
                                                                                                  20)
                                                                                          .addComponent(
                                                                                                  jLabel1)
                                                                                          .addGap(12,
                                                                                                  12,
                                                                                                  12)
                                                                                          .addComponent(
                                                                                                  jScrollPane2,
                                                                                                  javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                  437,
                                                                                                  javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                          .addGap(18,
                                                                                                  18,
                                                                                                  18)
                                                                                          .addGroup(
                                                                                                  LoadoutPanelLayout.createParallelGroup(
                                                                                                                            javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                                    .addComponent(
                                                                                                                            DeleteLoadoutButton)
                                                                                                                    .addComponent(
                                                                                                                            SwapLoadoutButton)
                                                                                                                    .addComponent(
                                                                                                                            ClearLoadoutButton))
                                                                                          .addPreferredGap(
                                                                                                  javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                                                  javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                  Short.MAX_VALUE)
                                                                                          .addGroup(
                                                                                                  LoadoutPanelLayout.createParallelGroup(
                                                                                                                            javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                                    .addComponent(
                                                                                                                            ExportButton)
                                                                                                                    .addComponent(
                                                                                                                            ImportButton))
                                                                                          .addPreferredGap(
                                                                                                  javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                          .addComponent(
                                                                                                  SaveFileButton)
                                                                                          .addGap(59,
                                                                                                  59,
                                                                                                  59)));

        javax.swing.GroupLayout panel2Layout = new javax.swing.GroupLayout(panel2);
        panel2.setLayout(panel2Layout);
        panel2Layout.setHorizontalGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(panel2Layout.createSequentialGroup()
                                                                          .addGap(25, 25, 25)
                                                                          .addComponent(
                                                                                  EquipmentPanel,
                                                                                  javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                  371,
                                                                                  javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                          .addComponent(LoadoutPanel,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                          .addContainerGap(19,
                                                                                           Short.MAX_VALUE)));
        panel2Layout.setVerticalGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                  .addGroup(panel2Layout.createSequentialGroup()
                                                                        .addGap(23, 23, 23)
                                                                        .addGroup(panel2Layout.createParallelGroup(
                                                                                                      javax.swing.GroupLayout.Alignment.LEADING)
                                                                                              .addComponent(
                                                                                                      EquipmentPanel,
                                                                                                      javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                      javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                      Short.MAX_VALUE)
                                                                                              .addComponent(
                                                                                                      LoadoutPanel,
                                                                                                      javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                      javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                      Short.MAX_VALUE))
                                                                        .addContainerGap()));

        jSplitPane1.setRightComponent(panel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jSplitPane1,
                                                      javax.swing.GroupLayout.DEFAULT_SIZE,
                                                      1087,
                                                      Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                      .addComponent(jSplitPane1));
    }// </editor-fold>//GEN-END:initComponents

    private void GearButtonClick(java.awt.event.MouseEvent evt)
    {//GEN-FIRST:event_GearButtonClick
        javax.swing.JLabel Label = (javax.swing.JLabel) evt.getSource();

        if(Label != null)
        {
            if(Label != CurrentLabel)
            {
                DeselectGearLabel();
            }
            SelectGearLabel(Label);
            AddItemsForSlot();


            AlowPurchaseCheckBox.setSelected(false);
            EquipmentRemoveButton.setEnabled(false);
            RefreshLoadoutList();
            RefreshEquipmentList();
        }


    }//GEN-LAST:event_GearButtonClick

    private void SelectGearLabel(javax.swing.JLabel Label)
    {
        DeselectGearLabel();
        Label.setBorder(SelectBorder);
        CurrentLabel = Label;
    }

    private void DeselectGearLabel()
    {
        if(CurrentLabel != null)
        {
            CurrentLabel.setBorder(DeselectBorder);
            CurrentLabel = null;
        }
    }

    private void DeleteLoadoutButtonActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_DeleteLoadoutButtonActionPerformed
        GenericListModel<LoadoutEntryWrapper> listModel = (GenericListModel<LoadoutEntryWrapper>) LoadoutList.getModel();
        int[]                                   selected  = LoadoutList.getSelectedIndices();
        if(listModel != null && selected != null && selected.length > 0)
        {
            listModel.remove(selected);
            RefreshLoadoutList();
        }
    }//GEN-LAST:event_DeleteLoadoutButtonActionPerformed

    private void SwapLoadoutButtonActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_SwapLoadoutButtonActionPerformed
        GenericListModel<LoadoutEntryWrapper> listModel = (GenericListModel<LoadoutEntryWrapper>) LoadoutList.getModel();
        var                                   selected  = LoadoutList.getSelectedIndices();
        if(selected == null || selected.length < 2)
        {
            return;
        }
        if(listModel != null)
        {
            listModel.swap(selected[0], selected[1]);
            RefreshLoadoutList();
        }
    }//GEN-LAST:event_SwapLoadoutButtonActionPerformed

    private void ClearLoadoutButtonActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_ClearLoadoutButtonActionPerformed
        GenericListModel<LoadoutEntryWrapper> listModel = (GenericListModel<LoadoutEntryWrapper>) LoadoutList.getModel();
        if(listModel != null)
        {
            listModel.clear();
            RefreshLoadoutList();
        }
    }//GEN-LAST:event_ClearLoadoutButtonActionPerformed

    private void AddLoadoutButtonActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_AddLoadoutButtonActionPerformed

        if(evt.getID() == ActionEvent.ACTION_PERFORMED)
        {
            var          SelectedEquipment = EquipmentList.getSelectedValuesList();
            LoadoutOrder order             = (LoadoutOrder) LoadoutOrderComboBox.getSelectedItem();
            if(SelectedEquipment != null && !SelectedEquipment.isEmpty())
            {
                LoadoutEntryWrapper LoadOutEntry;
                if(order == null)
                {
                    LoadOutEntry = new LoadoutEntryWrapper(SelectedEquipment);
                }
                else
                {
                    LoadOutEntry = new LoadoutEntryWrapper(SelectedEquipment, order);
                }

                GenericListModel<LoadoutEntryWrapper> listModel = (GenericListModel<LoadoutEntryWrapper>) LoadoutList.getModel();

                listModel.add(LoadOutEntry);
                RefreshLoadoutList();
            }
        }
    }//GEN-LAST:event_AddLoadoutButtonActionPerformed

    private void RefreshLoadoutList()
    {
        LoadoutList.clearSelection();
        LoadoutList.updateUI();
    }

    private void onFilterByChanged(java.awt.event.ItemEvent evt)
    {//GEN-FIRST:event_onFilterByChanged

        if(evt.getStateChange() == ItemEvent.SELECTED)
        {
            CurrentFilter = (EquipmentFilter) evt.getItem();
            EquipmentFilterComboBox.hidePopup();
            RefreshEquipmentList();
        }
    }//GEN-LAST:event_onFilterByChanged

    private void onSortByChanged(java.awt.event.ItemEvent evt)
    {//GEN-FIRST:event_onSortByChanged

        if(evt.getStateChange() == ItemEvent.SELECTED)
        {
            var str = (String) evt.getItem();

            var EnumMatch = Arrays.stream(ExtraEquipmentSort.values())
                                  .filter((t) -> t.name().equalsIgnoreCase(str))
                                  .findFirst();
            var FieldMatch = Arrays.stream(EquipmentStats)
                                   .filter((t) -> t.getName().equalsIgnoreCase(str))
                                   .findFirst();
            if(FieldMatch.isPresent())
            {
                CurrentSortValue = FieldMatch.get();
            }
            else if(EnumMatch.isPresent())
            {
                CurrentSortValue = EnumMatch.get();
            }
            else
            {
                CurrentSortValue = null;
            }
            SortByComboBox.hidePopup();
            //SortByComboBox.updateUI();
            RefreshEquipmentList();
        }
    }//GEN-LAST:event_onSortByChanged

    private void AllowPurchaseCheckBoxActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_AllowPurchaseCheckBoxActionPerformed

        GenericListModel<EquipmentEntryWrapper> model         = (GenericListModel<EquipmentEntryWrapper>) EquipmentList.getModel();
        var                                     selectIndices = EquipmentList.getSelectedIndices();
        for(var index : selectIndices)
        {
            var entry = model.getElementAt(index);
            if(entry != null)
            {
                entry.AllowPurchase = AlowPurchaseCheckBox.isSelected();
            }
        }
    }//GEN-LAST:event_AllowPurchaseCheckBoxActionPerformed

    private void EquipmentListValueChanged(javax.swing.event.ListSelectionEvent evt)
    {//GEN-FIRST:event_EquipmentListValueChanged
        boolean isSelected = false;
        for(var index : EquipmentList.getSelectedValuesList())
        {
            isSelected |= index.AllowPurchase;
        }
        AlowPurchaseCheckBox.setSelected(isSelected);

        if(CurrentSortValue == ExtraEquipmentSort.Custom &&
           !EquipmentList.getSelectedValuesList().isEmpty())
        {
            EquipmentEntrySwapButton.setEnabled(true);
        }
        else
        {
            EquipmentEntrySwapButton.setEnabled(false);
        }

    }//GEN-LAST:event_EquipmentListValueChanged

    private void UntradablesCheckboxActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_UntradablesCheckboxActionPerformed
        Untradables = UntradablesCheckbox.isSelected();
        RefreshEquipmentList();
    }//GEN-LAST:event_UntradablesCheckboxActionPerformed

    private void SearchBarActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_SearchBarActionPerformed
        if(SearchBar.getText() != null)
        {
            if(SearchBar.getText().isEmpty())
            {
                StringFilter = null;
            }
            else
            {
                StringFilter = SearchBar.getText();
            }
            RefreshEquipmentList();
        }
    }//GEN-LAST:event_SearchBarActionPerformed

    private void ImportButtonActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_ImportButtonActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open File");
        //fileChooser.setFileFilter(n);

        int userSelection = fileChooser.showOpenDialog(this);

        if(userSelection == JFileChooser.APPROVE_OPTION)
        {
            File fileToLoad = fileChooser.getSelectedFile();
            try(BufferedReader reader = new BufferedReader(new FileReader(fileToLoad)))
            {
                Gson gson    = OSRSUtilities.OSRSGsonBuilder.create();
                var  loadout = gson.fromJson(reader, LoadoutEntryWrapper[].class);
                var  model   = new GenericListModel<LoadoutEntryWrapper>();
                model.set(List.of(loadout));
                LoadoutList.setModel(model);
                reader.close();
                CurrentSafeFile = fileToLoad;
                SaveFileButton.setEnabled(true);

            } catch(IOException ex)
            {
                JOptionPane.showMessageDialog(this,
                                              "Error loading file: " + ex.getMessage(),
                                              "Error",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_ImportButtonActionPerformed

    private void ExportButtonActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_ExportButtonActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Json file (*.json)", "json"));

        int userSelection = fileChooser.showSaveDialog(this);

        if(userSelection == JFileChooser.APPROVE_OPTION)
        {
            File fileToSave = fileChooser.getSelectedFile();
            fileToSave = ensureExtension(fileToSave, "json");

            try
            {
                SaveFile(fileToSave);
            } catch(Exception ex)
            {
                JOptionPane.showMessageDialog(this,
                                              "Error saving file: " + ex.getMessage(),
                                              "Error",
                                              JOptionPane.ERROR_MESSAGE);
            }


        }
    }//GEN-LAST:event_ExportButtonActionPerformed

    private void SaveFile(File file) throws IOException
    {
        GenericListModel<LoadoutEntryWrapper> listModel = (GenericListModel<LoadoutEntryWrapper>) LoadoutList.getModel();
        if(listModel != null)
        {
            if(file != null)
            {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                Gson           gson   = OSRSUtilities.OSRSGsonBuilder.create();
                var            json   = gson.toJson(listModel.getList().toArray());
                writer.write(json);
                writer.close();
                CurrentSafeFile = file;
                SaveFileButton.setEnabled(true);
            }
            onSave.Fire(listModel.getList());
        }
    }



    private void LoadoutListMouseClicked(java.awt.event.MouseEvent evt)
    {//GEN-FIRST:event_LoadoutListMouseClicked
        if(LoadoutList.locationToIndex(evt.getPoint()) == -1)
        {
            LoadoutList.clearSelection();
            return;
        }
        var selected = LoadoutList.getSelectedValuesList();

        if(selected != null)
        {
            var items = selected.stream()
                                .flatMap((t) -> t.Equipment.stream())
                                .map((t) -> t.item)
                                .toList();

            DeselectGearLabel();
            EquipmentFilterComboBox.setSelectedItem(EquipmentFilter.No_Filter);
            EquipmentFilterComboBox.hidePopup();
            EquipmentFilterComboBox.updateUI();
            SortByComboBox.setSelectedItem(ExtraEquipmentSort.Custom.name());
            SortByComboBox.hidePopup();
            SortByComboBox.updateUI();
            SetEquipmentList(items);
            RefreshEquipmentList();
            EquipmentRemoveButton.setEnabled(true);
        }
    }//GEN-LAST:event_LoadoutListMouseClicked

    private void EquipmentRemoveButtonActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_EquipmentRemoveButtonActionPerformed
        GenericListModel<EquipmentEntryWrapper> EquipModel      = (GenericListModel<EquipmentEntryWrapper>) EquipmentList.getModel();
        var                                     SelectedLoadout = LoadoutList.getSelectedValue();
        var                                     selected        = EquipmentList.getSelectedValuesList();
        if(CurrentLabel == null && EquipModel != null && SelectedLoadout != null &&
           selected != null && selected.size() > 0)
        {
            EquipModel.remove(selected);
            SelectedLoadout.Equipment.removeIf((t) -> selected.stream()
                                                              .anyMatch((x) -> x.itemData.id ==
                                                                               t.item.id));
            RefreshEquipmentList();
            RefreshLoadoutList();
        }


    }//GEN-LAST:event_EquipmentRemoveButtonActionPerformed

    private void EquipmentEntrySwapButtonActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_EquipmentEntrySwapButtonActionPerformed
        GenericListModel<EquipmentEntryWrapper> model    = (GenericListModel<EquipmentEntryWrapper>) EquipmentList.getModel();
        var                                     selected = EquipmentList.getSelectedIndices();
        if(selected == null || selected.length < 2)
        {
            return;
        }
        if(model != null)
        {
            model.swap(selected[0], selected[1]);
            RefreshEquipmentList();
        }
    }//GEN-LAST:event_EquipmentEntrySwapButtonActionPerformed

    private void SaveFileButtonActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_SaveFileButtonActionPerformed
        try
        {
            SaveFile(CurrentSafeFile);
        } catch(Exception ex)
        {
            Logger.error("EquipmentConfigurator: SaveFileButtonActionPerformed: Failed to save file " + ex);
        }
    }//GEN-LAST:event_SaveFileButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton                      AddLoadoutButton;
    public  javax.swing.JLabel                       AllGearSlot;
    private javax.swing.JCheckBox                    AlowPurchaseCheckBox;
    public  javax.swing.JLabel                       AmmoGearSlot;
    public  javax.swing.JLabel                       BodyGearSlot;
    public  javax.swing.JLabel                       CapeGearSlot;
    private javax.swing.JButton                      ClearLoadoutButton;
    private javax.swing.JButton                      DeleteLoadoutButton;
    private javax.swing.JButton                      EquipmentEntrySwapButton;
    private javax.swing.JComboBox<EquipmentFilter>   EquipmentFilterComboBox;
    private javax.swing.JList<EquipmentEntryWrapper> EquipmentList;
    private javax.swing.JPanel                       EquipmentPanel;
    private javax.swing.JButton                      EquipmentRemoveButton;
    private javax.swing.JButton                      ExportButton;
    public  javax.swing.JLabel                       FeetGearSlot;
    public  javax.swing.JLabel                       HandGearSlot;
    public  javax.swing.JLabel                       HeadGearLabel;
    private javax.swing.JButton                      ImportButton;
    private javax.swing.JList<LoadoutEntryWrapper>   LoadoutList;
    private javax.swing.JComboBox<LoadoutOrder>      LoadoutOrderComboBox;
    private javax.swing.JPanel                       LoadoutPanel;
    public  javax.swing.JLabel                       NeckGearSlot;
    public  javax.swing.JLabel                       PantsGearSlot;
    public  javax.swing.JLabel                       RingGearSlot;
    private javax.swing.JButton                      SaveFileButton;
    private javax.swing.JTextField                   SearchBar;
    public  javax.swing.JLabel                       ShieldGearSlot;
    private javax.swing.JComboBox<String>            SortByComboBox;
    private javax.swing.JButton                      SwapLoadoutButton;
    private javax.swing.JCheckBox                    UntradablesCheckbox;
    public  javax.swing.JLabel                       WeaponGearSlot;
    private javax.swing.JDesktopPane                 jDesktopPane1;
    private javax.swing.JLabel                       jLabel1;
    private javax.swing.JLabel                       jLabel2;
    private javax.swing.JLabel                       jLabel24;
    private javax.swing.JLabel                       jLabel26;
    private javax.swing.JLabel                       jLabel3;
    private javax.swing.JLabel                       jLabel4;
    private javax.swing.JLabel                       jLabel7;
    private javax.swing.JScrollPane                  jScrollPane1;
    private javax.swing.JScrollPane                  jScrollPane2;
    private javax.swing.JSplitPane                   jSplitPane1;
    private java.awt.Panel                           panel1;
    private java.awt.Panel                           panel2;
    // End of variables declaration//GEN-END:variables
    //GEN-END

}
