import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by ishankhanna on 17/01/15.
 */
public class UploadAPKAction extends AnAction {

    public void actionPerformed(AnActionEvent e) {

        JFrame frame = new JFrame("UploadApkView");
        frame.setContentPane(new UploadApkView().getMainJPanel());
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
            }
        });
        frame.pack();
        frame.setVisible(true);


    }

}
