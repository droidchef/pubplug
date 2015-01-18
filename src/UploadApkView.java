import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.model.Apk;
import com.google.api.services.androidpublisher.model.AppEdit;
import com.google.api.services.androidpublisher.model.Track;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ishankhanna on 18/01/15.
 */
public class UploadApkView implements ActionListener{



    private JPanel mainJPanel;
    private JTextField textField1;
    private JTextField textField2;
    private JButton button1;
    private JComboBox comboBox1;
    private JButton uploadButton;
    private JLabel lb_apkPath;
    private JProgressBar progressBar1;
    private JTextArea textArea1;
    private JLabel lb_versionCode;
    private JLabel lb_versionName;
    private JLabel lb_APKSize;

    private PropertiesComponent propertiesComponent;

    public UploadApkView() {

        propertiesComponent = PropertiesComponent.getInstance();

        uploadButton.addActionListener(this);
        button1.addActionListener(this);
        progressBar1.setStringPainted(true);
        progressBar1.setMinimum(0);
        progressBar1.setMaximum(100);

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

        if (performedBy.equals(button1)) {

            final FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, true, true, true, true, false);

            FileChooser.chooseFile(fileChooserDescriptor, null, null, new Consumer<VirtualFile>() {
                @Override
                public void consume(VirtualFile virtualFile) {

                    if (!virtualFile.getPath().isEmpty()) {
                        lb_apkPath.setText(virtualFile.getPath());
                        long sizeInKiloBytes = virtualFile.getLength()/1024;

                        lb_APKSize.setText(sizeInKiloBytes + " Kb");
                    }

                }
            });

        }

        if(performedBy.equals(uploadButton)) {

            final String applicationName = textField1.getText();
            final String packageName = textField2.getText();

            if(!applicationName.isEmpty() && !packageName.isEmpty() && !lb_apkPath.getText().equalsIgnoreCase("No File Chosen")) {

                UploadAPKTask uploadAPKTask = new UploadAPKTask(packageName, applicationName, lb_apkPath.getText(), propertiesComponent.getValue(GoogleConfigurationView.KEY_SERVICE_ACCOUNT_EMAIL, ""));

                uploadAPKTask.start();


            }

        }

    }



    class UploadAPKTask extends Thread{

        private final Log log = LogFactory.getLog(UploadApkView.class);

        /**
         * Track for uploading the apk, can be 'alpha', beta', 'production' or
         * 'rollout'.
         */
        private final String TRACK_ALPHA = "alpha";

        private String packageName;
        private String applicationName;
        private String apkPath;
        private String serviceAccountEmail;

        public List<String> messageList;

        public UploadAPKTask(String packageName, String applicationName, String apkPath, String serviceAccountEmail) {
            this.packageName = packageName;
            this.applicationName = applicationName;
            this.apkPath = apkPath;
            this.serviceAccountEmail = serviceAccountEmail;

            messageList = new ArrayList<String>();

        }

        @Override
        public synchronized void start() {
            super.start();

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    progressBar1.setStringPainted(true);
                    progressBar1.setIndeterminate(true);
                }
            });

        }

        @Override
        public void run() {
            super.run();



            try {
                Preconditions.checkArgument(!Strings.isNullOrEmpty(packageName),
                        "PACKAGE_NAME cannot be null or empty!");

                // Create the API service.
                AndroidPublisher service = AndroidPublisherHelper.init(
                        applicationName, serviceAccountEmail );
                final AndroidPublisher.Edits edits = service.edits();

                // Create a new edit to make changes to your listing.
                AndroidPublisher.Edits.Insert editRequest = edits
                        .insert(packageName,
                                null /** no content */);
                AppEdit edit = editRequest.execute();
                final String editId = edit.getId();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        progressBar1.setIndeterminate(false);
                        progressBar1.setValue(25);
                        textArea1.append(String.format("Created edit with id: %s", editId) + "\n");
                    }
                });
                log.info(String.format("Created edit with id: %s", editId));

                // Upload new apk to developer console
                final AbstractInputStreamContent apkFile =
                        new FileContent(AndroidPublisherHelper.MIME_TYPE_APK, new File(apkPath));
                AndroidPublisher.Edits.Apks.Upload uploadRequest = edits
                        .apks()
                        .upload(packageName,
                                editId,
                                apkFile);
                final Apk apk = uploadRequest.execute();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        textArea1.append(String.format("Version code %d has been uploaded",
                                apk.getVersionCode()) + "\n");
                        lb_versionCode.setText(""+apk.getVersionCode());
                        progressBar1.setValue(50);

                    }
                });
                log.info(String.format("Version code %d has been uploaded",
                        apk.getVersionCode()));

                // Assign apk to alpha track.
                List<Integer> apkVersionCodes = new ArrayList<Integer>();
                apkVersionCodes.add(apk.getVersionCode());
                AndroidPublisher.Edits.Tracks.Update updateTrackRequest = edits
                        .tracks()
                        .update(packageName,
                                editId,
                                TRACK_ALPHA,
                                new Track().setVersionCodes(apkVersionCodes));
                final Track updatedTrack = updateTrackRequest.execute();

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        textArea1.append(String.format("Track %s has been updated.", updatedTrack.getTrack()) + "\n");
                        progressBar1.setValue(75);

                    }
                });

                log.info(String.format("Track %s has been updated.", updatedTrack.getTrack()));

                // Commit changes for edit.
                AndroidPublisher.Edits.Commit commitRequest = edits.commit(packageName, editId);
                final AppEdit appEdit = commitRequest.execute();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        textArea1.append(String.format("App edit with id %s has been comitted", appEdit.getId()) + "\n");
                        progressBar1.setValue(100);

                    }
                });
                log.info(String.format("App edit with id %s has been comitted", appEdit.getId()));

            } catch (IOException ex) {
                log.error("Excpetion was thrown while uploading apk to alpha track", ex);
            } catch (GeneralSecurityException ex) {
                log.error("Excpetion was thrown while uploading apk to alpha track", ex);
            }

        }


    }

}
