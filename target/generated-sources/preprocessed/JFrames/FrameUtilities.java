package JFrames;

import javax.swing.*;
import java.awt.*;

public class FrameUtilities
{

    public static JFrame OpenGui(String name, Container Form)
    {
        JFrame test = new JFrame(name);
        test.setContentPane(Form);
        test.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        test.pack();
        test.setVisible(true);
        return test;
    }

    public static void CloseGui(JFrame Gui)
    {
        Gui.setVisible(false);
        Gui.dispose();
    }
}
