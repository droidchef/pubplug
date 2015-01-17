/**
 * Created by ishankhanna on 18/01/15.
 */

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import javax.annotation.Nullable;

import com.intellij.ide.util.PropertiesComponent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper class to initialize the publisher APIs client library.
 * <p>
 * Before making any calls to the API through the client library you need to
 * call the {@link AndroidPublisherHelper#init(String)} method. This will run
 * all precondition checks for for client id and secret setup properly in
 * resources/client_secrets.json and authorize this client against the API.
 * </p>
 */
public class AndroidPublisherHelper {

    private static final Log log = LogFactory.getLog(AndroidPublisherHelper.class);

    public static final String MIME_TYPE_APK = "application/vnd.android.package-archive";

    /** Path to the private key file (only used for Service Account auth). */
    private static final String SRC_RESOURCES_KEY_P12 = "key.p12";

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /**
     * Global instance of the {@link DataStoreFactory}. The best practice is to
     * make it a single globally shared instance across your application.
     */
    private static FileDataStoreFactory dataStoreFactory;

    private static Credential authorizeWithServiceAccount(String serviceAccountEmail)
            throws GeneralSecurityException, IOException {
        log.info(String.format("Authorizing using Service Account: %s", serviceAccountEmail));

        // Build service account credential.
        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setServiceAccountId(serviceAccountEmail)
                .setServiceAccountScopes(
                        Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER))
                .setServiceAccountPrivateKeyFromP12File(new File(PropertiesComponent.getInstance().getValue(GoogleConfigurationView.KEY_PATH_TO_P12)))
                .build();
        return credential;
    }

    /**
     * Performs all necessary setup steps for running requests against the API
     * using the Installed Application auth method.
     *
     * @param applicationName the name of the application: com.example.app
     * @return the {@Link AndroidPublisher} service
     */
    protected static AndroidPublisher init(String applicationName) throws Exception {
        return init(applicationName, null);
    }

    /**
     * Performs all necessary setup steps for running requests against the API.
     *
     * @param applicationName the name of the application: com.example.app
     * @param serviceAccountEmail the Service Account Email (empty if using
     *            installed application)
     * @return the {@Link AndroidPublisher} service
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static AndroidPublisher init(String applicationName,
                                        @Nullable String serviceAccountEmail) throws IOException, GeneralSecurityException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(applicationName),
                "applicationName cannot be null or empty!");

        // Authorization.
        newTrustedTransport();
        Credential credential;
        if (serviceAccountEmail == null || serviceAccountEmail.isEmpty()) {
            //TODO : ask for the service account and key.p12 file with a dialog ;)
            credential = authorizeWithServiceAccount(serviceAccountEmail);
        } else {
            credential = authorizeWithServiceAccount(serviceAccountEmail);
        }

        // Set up and return API client.
        return new AndroidPublisher.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(applicationName)
                .build();
    }

    private static void newTrustedTransport() throws GeneralSecurityException,
            IOException {
        if (null == HTTP_TRANSPORT) {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        }
    }

}