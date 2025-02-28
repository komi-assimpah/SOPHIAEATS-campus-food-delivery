package fr.unice.polytech.infrastructure.repository.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseDb {
    private static FirebaseDatabase instance;
    private FirebaseDb() {}

    public static synchronized FirebaseDatabase getInstanceDB() throws IOException {
        if (instance ==null) {
            File file = new File(
                    FirebaseDb.class.getClassLoader().getResource("sophiaeats-firebase.json").getFile()
            );

            FileInputStream fis = new FileInputStream(file);

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(fis))
                    .setDatabaseUrl("https://sophiaeats-d51e2-default-rtdb.firebaseio.com/")
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            instance = FirebaseDatabase.getInstance();
        }
        return instance;
    }
}
