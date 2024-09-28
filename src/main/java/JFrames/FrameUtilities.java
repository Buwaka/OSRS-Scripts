package JFrames;

import javax.swing.*;
import java.awt.*;

public class FrameUtilities
{

    public static void OpenGui(String name, Container Form)
    {
        JFrame test = new JFrame(name);
        test.setContentPane(Form);
        test.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        test.pack();
        test.setVisible(true);
    }
}
