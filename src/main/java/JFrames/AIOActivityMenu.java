package JFrames;

import Cycles.CycleGenerators.CycleGenerator;
import OSRSDatabase.ItemDB;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class AIOActivityMenu
{
    private       JPanel                                             Main;
    private       JList                                              list1;
    private       JPanel                                             SkillPanel;
    private       JLabel                                             AttackLabel;
    private       JLabel                                             StrengthLabel;
    private       JLabel                                             DefenseLabel;
    private       JLabel                                             RangedLabel;
    private       JLabel                                             PrayerLabel;
    private       JLabel                                             MagicLabel;
    private       JLabel                                             RunecraftingLabel;
    private       JLabel                                             ConstructionLabel;
    private       JLabel                                             HPLabel;
    private       JLabel                                             AgilityLabel;
    private       JLabel                                             HerbloreLabel;
    private       JLabel                                             ThievingLabel;
    private       JLabel                                             CraftingLabel;
    private       JLabel                                             FletchingLabel;
    private       JLabel                                             SlayerLabel;
    private       JLabel                                             HunterLabel;
    private       JLabel                                             MiningLabel;
    private       JLabel                                             SmithingLabel;
    private       JLabel                                             FishingLabel;
    private       JLabel                                             CookingLabel;
    private       JLabel                                             FiremakingLabel;
    private       JLabel                                             WoodcuttingLabel;
    private       JLabel                                             FarmingLabel;
    public        Map<ItemDB.Skill, JLabel>                          SkillLabelMap = new HashMap<>();
    // populate Jlist based on Skill tag filter
    private final EnumMap<ItemDB.Skill, CycleGeneratorConfiguration> GeneratorMap  = new EnumMap<>(
            ItemDB.Skill.class);

    class CycleGeneratorConfiguration
    {
        public boolean         ManualInterference; // basically once the user makes a change to the config, do not auto-update it anymore
        public Future<Boolean> Enabled;
        public CycleGenerator  Generator;
        public CycleGenerator.CycleData   Parameters;

        //TODO make a popup window warning that not all requirements have been updated yet, some cycles might not be valid
        // TODO Auto-update Enabled with a requirement check, each time the user opens a skill tab, maybe something with future, idk
        public CycleGeneratorConfiguration(CycleGenerator generator)
        {
            ManualInterference = false;
            Enabled            = CompletableFuture.supplyAsync(() -> WaitForRequirements(generator.GetRequirements()))
                                                  .whenComplete(AIOActivityMenu.this::Refresh); //TODO should run immediately, but better test out
            Generator          = generator;
            Parameters         = null;
        }

        private boolean WaitForRequirements(Set<CycleGenerator.CycleRequirement> Requirements)
        {
            Set<CycleGenerator.CycleRequirement> temp = new HashSet<>(Requirements);

            boolean AllResolved;
            do
            {
                AllResolved = true;
                for(var Requirement : temp)
                {
                    var result = Requirement.IsRequirementMet();
                    if(!result.isPresent())
                    {
                        AllResolved = false;
                    }
                }
                try
                {
                    Thread.sleep(100);
                } catch(InterruptedException ignored) {}
            }
            while(!AllResolved);

            boolean result = true;
            for(var Requirement : temp)
            {
                result &= Requirement.IsRequirementMet().get();
            }
            return result;
        }

    }

    private void Refresh(Boolean aBoolean, Throwable throwable) {}


    public static void main(String[] args)
    {
        JFrame test = new JFrame("TestFrame");
        test.setContentPane(new AIOActivityMenu().Main);
        test.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        test.pack();
        test.setVisible(true);
    }

    public AIOActivityMenu()
    {
        SkillLabelMap.put(ItemDB.Skill.ATTACK, AttackLabel);
        AttackLabel.addMouseListener(new SkillLabelMouseAdapter());
    }

    class SkillLabelMouseAdapter extends MouseAdapter
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            System.out.print("Clicked");

            //                var rect = SkillLabelMap.get(ItemDB.Skill.ATTACK).getVisibleRect();
            //                System.out.print(rect);
            //                if(rect.contains(e.getPoint()))
            //                {
            //                    System.out.print("Clicked in rectangle");
            //                    SkillLabelMap.get(ItemDB.Skill.ATTACK).setText("Clicked!");
            //                }
            super.mouseClicked(e);
        }
    }

    {
        // GUI initializer generated by IntelliJ IDEA GUI Designer
        // >>> IMPORTANT!! <<<
        // DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        Main = new JPanel();
        Main.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setContinuousLayout(false);
        Main.add(splitPane1,
                 new GridConstraints(0,
                                     0,
                                     1,
                                     1,
                                     GridConstraints.ANCHOR_CENTER,
                                     GridConstraints.FILL_BOTH,
                                     GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                     GridConstraints.SIZEPOLICY_CAN_GROW,
                                     GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                     GridConstraints.SIZEPOLICY_CAN_GROW,
                                     null,
                                     new Dimension(1000, 600),
                                     null,
                                     0,
                                     false));
        list1 = new JList();
        splitPane1.setRightComponent(list1);
        SkillPanel = new JPanel();
        SkillPanel.setLayout(new GridLayoutManager(8, 3, new Insets(0, 0, 0, 0), 0, 0, true, true));
        SkillPanel.setAlignmentX(1.0f);
        SkillPanel.setAlignmentY(1.0f);
        SkillPanel.setAutoscrolls(false);
        SkillPanel.setDoubleBuffered(true);
        SkillPanel.setEnabled(true);
        SkillPanel.setFocusable(true);
        SkillPanel.setMinimumSize(new Dimension(300, 300));
        SkillPanel.setOpaque(false);
        SkillPanel.setRequestFocusEnabled(true);
        splitPane1.setLeftComponent(SkillPanel);
        AttackLabel = new JLabel();
        AttackLabel.setIcon(new ImageIcon(getClass().getResource("/icons/Attack_icon_(detail).png")));
        AttackLabel.setText("Label");
        SkillPanel.add(AttackLabel,
                       new GridConstraints(0,
                                           0,
                                           1,
                                           1,
                                           GridConstraints.ANCHOR_CENTER,
                                           GridConstraints.FILL_BOTH,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           null,
                                           null,
                                           null,
                                           0,
                                           false));
        StrengthLabel = new JLabel();
        StrengthLabel.setText("Label");
        SkillPanel.add(StrengthLabel,
                       new GridConstraints(1,
                                           0,
                                           1,
                                           1,
                                           GridConstraints.ANCHOR_CENTER,
                                           GridConstraints.FILL_BOTH,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           null,
                                           null,
                                           null,
                                           0,
                                           false));
        DefenseLabel = new JLabel();
        DefenseLabel.setText("Label");
        SkillPanel.add(DefenseLabel,
                       new GridConstraints(2,
                                           0,
                                           1,
                                           1,
                                           GridConstraints.ANCHOR_CENTER,
                                           GridConstraints.FILL_BOTH,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           null,
                                           null,
                                           null,
                                           0,
                                           false));
        RangedLabel = new JLabel();
        RangedLabel.setText("Label");
        SkillPanel.add(RangedLabel,
                       new GridConstraints(3,
                                           0,
                                           1,
                                           1,
                                           GridConstraints.ANCHOR_CENTER,
                                           GridConstraints.FILL_BOTH,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           null,
                                           null,
                                           null,
                                           0,
                                           false));
        PrayerLabel = new JLabel();
        PrayerLabel.setText("Label");
        SkillPanel.add(PrayerLabel,
                       new GridConstraints(4,
                                           0,
                                           1,
                                           1,
                                           GridConstraints.ANCHOR_CENTER,
                                           GridConstraints.FILL_BOTH,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           null,
                                           null,
                                           null,
                                           0,
                                           false));
        MagicLabel = new JLabel();
        MagicLabel.setText("Label");
        SkillPanel.add(MagicLabel,
                       new GridConstraints(5,
                                           0,
                                           1,
                                           1,
                                           GridConstraints.ANCHOR_CENTER,
                                           GridConstraints.FILL_BOTH,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           null,
                                           null,
                                           null,
                                           0,
                                           false));
        HPLabel = new JLabel();
        HPLabel.setText("Label");
        SkillPanel.add(HPLabel,
                       new GridConstraints(0,
                                           1,
                                           1,
                                           1,
                                           GridConstraints.ANCHOR_CENTER,
                                           GridConstraints.FILL_BOTH,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           null,
                                           null,
                                           null,
                                           0,
                                           false));
        AgilityLabel = new JLabel();
        AgilityLabel.setText("Label");
        SkillPanel.add(AgilityLabel,
                       new GridConstraints(1,
                                           1,
                                           1,
                                           1,
                                           GridConstraints.ANCHOR_CENTER,
                                           GridConstraints.FILL_BOTH,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           null,
                                           null,
                                           null,
                                           0,
                                           false));
        HerbloreLabel = new JLabel();
        HerbloreLabel.setText("Label");
        SkillPanel.add(HerbloreLabel,
                       new GridConstraints(2,
                                           1,
                                           1,
                                           1,
                                           GridConstraints.ANCHOR_CENTER,
                                           GridConstraints.FILL_BOTH,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           null,
                                           null,
                                           null,
                                           0,
                                           false));
        ThievingLabel = new JLabel();
        ThievingLabel.setText("Label");
        SkillPanel.add(ThievingLabel,
                       new GridConstraints(3,
                                           1,
                                           1,
                                           1,
                                           GridConstraints.ANCHOR_CENTER,
                                           GridConstraints.FILL_BOTH,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           null,
                                           null,
                                           null,
                                           0,
                                           false));
        CraftingLabel = new JLabel();
        CraftingLabel.setText("Label");
        SkillPanel.add(CraftingLabel,
                       new GridConstraints(4,
                                           1,
                                           1,
                                           1,
                                           GridConstraints.ANCHOR_CENTER,
                                           GridConstraints.FILL_BOTH,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           null,
                                           null,
                                           null,
                                           0,
                                           false));
        FletchingLabel = new JLabel();
        FletchingLabel.setText("Label");
        SkillPanel.add(FletchingLabel,
                       new GridConstraints(5,
                                           1,
                                           1,
                                           1,
                                           GridConstraints.ANCHOR_CENTER,
                                           GridConstraints.FILL_BOTH,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           null,
                                           null,
                                           null,
                                           0,
                                           false));
        RunecraftingLabel = new JLabel();
        RunecraftingLabel.setText("Label");
        SkillPanel.add(RunecraftingLabel,
                       new GridConstraints(6,
                                           0,
                                           1,
                                           1,
                                           GridConstraints.ANCHOR_CENTER,
                                           GridConstraints.FILL_BOTH,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           null,
                                           null,
                                           null,
                                           0,
                                           false));
        ConstructionLabel = new JLabel();
        ConstructionLabel.setText("Label");
        SkillPanel.add(ConstructionLabel,
                       new GridConstraints(7,
                                           0,
                                           1,
                                           1,
                                           GridConstraints.ANCHOR_CENTER,
                                           GridConstraints.FILL_BOTH,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           null,
                                           null,
                                           null,
                                           0,
                                           false));
        SlayerLabel = new JLabel();
        SlayerLabel.setText("Label");
        SkillPanel.add(SlayerLabel,
                       new GridConstraints(6,
                                           1,
                                           1,
                                           1,
                                           GridConstraints.ANCHOR_CENTER,
                                           GridConstraints.FILL_BOTH,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           null,
                                           null,
                                           null,
                                           0,
                                           false));
        HunterLabel = new JLabel();
        HunterLabel.setText("Label");
        SkillPanel.add(HunterLabel,
                       new GridConstraints(7,
                                           1,
                                           1,
                                           1,
                                           GridConstraints.ANCHOR_CENTER,
                                           GridConstraints.FILL_BOTH,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           null,
                                           null,
                                           null,
                                           0,
                                           false));
        MiningLabel = new JLabel();
        MiningLabel.setText("Label");
        SkillPanel.add(MiningLabel,
                       new GridConstraints(0,
                                           2,
                                           1,
                                           1,
                                           GridConstraints.ANCHOR_CENTER,
                                           GridConstraints.FILL_BOTH,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           null,
                                           null,
                                           null,
                                           0,
                                           false));
        SmithingLabel = new JLabel();
        SmithingLabel.setText("Label");
        SkillPanel.add(SmithingLabel,
                       new GridConstraints(1,
                                           2,
                                           1,
                                           1,
                                           GridConstraints.ANCHOR_CENTER,
                                           GridConstraints.FILL_BOTH,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           null,
                                           null,
                                           null,
                                           0,
                                           false));
        FishingLabel = new JLabel();
        FishingLabel.setText("Label");
        SkillPanel.add(FishingLabel,
                       new GridConstraints(2,
                                           2,
                                           1,
                                           1,
                                           GridConstraints.ANCHOR_CENTER,
                                           GridConstraints.FILL_BOTH,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           null,
                                           null,
                                           null,
                                           0,
                                           false));
        CookingLabel = new JLabel();
        CookingLabel.setText("Label");
        SkillPanel.add(CookingLabel,
                       new GridConstraints(3,
                                           2,
                                           1,
                                           1,
                                           GridConstraints.ANCHOR_CENTER,
                                           GridConstraints.FILL_BOTH,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           null,
                                           null,
                                           null,
                                           0,
                                           false));
        FiremakingLabel = new JLabel();
        FiremakingLabel.setText("Label");
        SkillPanel.add(FiremakingLabel,
                       new GridConstraints(4,
                                           2,
                                           1,
                                           1,
                                           GridConstraints.ANCHOR_CENTER,
                                           GridConstraints.FILL_BOTH,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           null,
                                           null,
                                           null,
                                           0,
                                           false));
        WoodcuttingLabel = new JLabel();
        WoodcuttingLabel.setText("Label");
        SkillPanel.add(WoodcuttingLabel,
                       new GridConstraints(5,
                                           2,
                                           1,
                                           1,
                                           GridConstraints.ANCHOR_CENTER,
                                           GridConstraints.FILL_BOTH,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           null,
                                           null,
                                           null,
                                           0,
                                           false));
        FarmingLabel = new JLabel();
        FarmingLabel.setText("Label");
        SkillPanel.add(FarmingLabel,
                       new GridConstraints(6,
                                           2,
                                           1,
                                           1,
                                           GridConstraints.ANCHOR_CENTER,
                                           GridConstraints.FILL_BOTH,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           GridConstraints.SIZEPOLICY_FIXED,
                                           null,
                                           null,
                                           null,
                                           0,
                                           false));
    }

    /** @noinspection ALL */
    public JComponent $$$getRootComponent$$$() {return Main;}

}
