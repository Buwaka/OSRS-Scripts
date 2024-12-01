package IF.JFrames;

import IF.Scripts.CommissionScripts.FullDragon.FDMeleeTrainer;
import IF.Utilities.Encryption.Authenticator;
import IF.Utilities.Patterns.Runnables.Runnable1;
import IF.Utilities.Scripting.IFScript;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;

public class FrameUtilities
{
    public static void main(String[] args)
    {
        ScriptSelector((a) -> {}, () -> {});
    }

    public static void Authenticate(Runnable onSuccess, Runnable onExit)
    {
        SwingUtilities.invokeLater(() -> {
            var KeyPanel = new APIKeyPanel();
            var KeyGUI   = OpenGui("Enter Key", KeyPanel.GetForm());
            KeyPanel.onEnter.Subscribe(KeyGUI, (key) -> {
                Authenticator auth = Authenticator.GetInstance();
                if(auth.isAuthenticated(key))
                {
                    CloseGui(KeyGUI);
                    onSuccess.run();
                }
            });
            KeyGUI.addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosed(WindowEvent e)
                {
                    CloseGui(KeyGUI);
                    onExit.run();
                }
            });
        });


    }

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


    public static void ScriptSelector(Runnable1<Class<? extends IFScript>> onSelect, Runnable onStop)
    {
        //        Logger.log(ClasspathHelper.forPackage(IFScript.class.getPackageName()));
        //        Reflections reflections = new Reflections(new ConfigurationBuilder()
        //                                                          .addClassLoaders(Thread.currentThread().getContextClassLoader())  // Add the thread context classloader
        //                                                          .addUrls(ClasspathHelper.forPackage("IFOBF"))// Your main package(s) - adjust as needed
        //                                                  // ... potentially add more classloaders like the main classloader if required ...
        //        );
        //        var all = reflections.getSubTypesOf(Object.class);
        //        Logger.log(Arrays.toString(all.toArray()));
        Set<Class<? extends IFScript>> classes = new HashSet<>();
        classes.add(FDMeleeTrainer.class);
        //        Logger.log(Arrays.toString(classes.toArray()));reflections.getSubTypesOf(IFScript.class);
        //        for(var klass :classes)
        //        {
        //            Logger.log(klass.getAnnotation(ScriptMetadata.class).Name());
        //        }
        var ScriptSelect = new ScriptSelector();

        ScriptSelect.SetListData(classes);
       var frame =  OpenGui("Script Selector", ScriptSelect);

        ScriptSelect.onProceed.Subscribe(ScriptSelect, onSelect);
        ScriptSelect.onProceed.Subscribe(null, (select) -> CloseGui(frame));

        ScriptSelect.onExit.Subscribe(ScriptSelect, onStop);
        ScriptSelect.onExit.Subscribe(null, () -> CloseGui(frame));
    }
}
