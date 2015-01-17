import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by ishankhanna on 18/01/15.
 */
public class GoogleConfigurationView implements ActionListener{

    public static final String KEY_PATH_TO_P12 = "pubplug.p12FilePath";
    public static final String KEY_SERVICE_ACCOUNT_EMAIL = "pubplug.serviceAccountEMail";

    private JPanel mainJPanel;
    private JTextField tf_serviceAccountEmail;
    private JButton bt_testConnection;
    private JButton bt_save;
    private JButton bt_reset;
    private JProgressBar pb_status;
    private JButton bt_chooseKeyFile;
    private JLabel lb_status;
    private JLabel lb_p12FilePath;

    private boolean isFileChoosen = false;

    private PropertiesComponent propertiesComponent;

    public GoogleConfigurationView() {

        propertiesComponent = PropertiesComponent.getInstance();
        tf_serviceAccountEmail.setText(propertiesComponent.getValue(KEY_SERVICE_ACCOUNT_EMAIL, ""));
        lb_p12FilePath.setText(propertiesComponent.getValue(KEY_PATH_TO_P12, "p12 File Not Available"));

        if (lb_p12FilePath.getText().equalsIgnoreCase("p12 File Not Available")) {
            isFileChoosen = false;
        } else {
            isFileChoosen = true;
        }

        bt_chooseKeyFile.addActionListener(this);
        bt_save.addActionListener(this);

    }

    public JPanel getMainJPanel() {
        return mainJPanel;
    }

    public void setMainJPanel(JPanel mainJPanel) {
        this.mainJPanel = mainJPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {


        Object performedBy = e.getSource();

        if (performedBy.equals(bt_chooseKeyFile)) {

            final FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, true, true, true, true, true);

            FileChooser.chooseFile(fileChooserDescriptor, null, null, new Consumer<VirtualFile>() {
                @Override
                public void consume(VirtualFile virtualFile) {

                    if(!virtualFile.getPath().isEmpty()) {
                        lb_p12FilePath.setText(virtualFile.getPath());
                        isFileChoosen = true;
                    }

                }
            });
        }

        if (performedBy.equals(bt_save)) {

            if (!tf_serviceAccountEmail.getText().isEmpty() && isFileChoosen) {
                lb_status.setText("Working...");
                pb_status.setStringPainted(true);
                pb_status.setMaximum(0);
                pb_status.setMaximum(100);
                pb_status.setValue(20);
                propertiesComponent.setValue(KEY_PATH_TO_P12, lb_p12FilePath.getText());
                pb_status.setValue(75);
                propertiesComponent.setValue(KEY_SERVICE_ACCOUNT_EMAIL, tf_serviceAccountEmail.getText());
                pb_status.setValue(90);
                lb_status.setText("Credentials Saved!");
                pb_status.setValue(100);
            }

        }

    }
}
