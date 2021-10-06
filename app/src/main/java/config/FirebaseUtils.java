package config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public final class FirebaseUtils {

    private static FirebaseAuth auth;
    private static FirebaseFirestore database;
    private static FirebaseStorage storage;


    public static FirebaseAuth getAuth() {
        if(auth==null)
            auth = FirebaseAuth.getInstance();
        return auth;
    }

    public static FirebaseFirestore getDatabase() {
        if(database==null)
            database = FirebaseFirestore.getInstance();
        return database;
    }

    public static FirebaseStorage getStorage() {
        if(storage==null)
            storage = FirebaseStorage.getInstance();
        return storage;
    }
}
