package cda.flolb.authfirestore;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;

public class MainActivity<mAuthListener> extends AppCompatActivity {

    // Déclaration de variables pour les champs email et mot de passe
    private EditText email;
    private EditText password;

    // Déclaration de variables pour les boutons de connexion et d'inscription
    private AppCompatButton loginBtn;
    private AppCompatButton registerBtn;

    // Déclaration de variables pour stocker les informations de l'utilisateur courant
    Map<String, Object> user = new HashMap<>();

    // Déclaration de variables pour la base de données Firestore
    // Il permet de stocker des documents dans des collections,
    // similaires aux tables dans une base de données relationnelle.
    // Les documents sont des objets JSON qui peuvent être indexés pour une recherche efficace.
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    //----------------------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // On récupère les éléments de la vue
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);


        // On ajoute un listener (écouteur d'évenement) sur le bouton de connexion
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // On récupère les valeurs des champs
                String emailValue = email.getText().toString();
                String pass = password.getText().toString();
                String passwordValue = BCrypt.hashpw(pass, BCrypt.gensalt());

                // BCrypt : Blowfish
                if (BCrypt.checkpw(pass, passwordValue)) {

                    // On vérifie que les champs ne sont pas vides
                    if (emailValue.isEmpty() || passwordValue.isEmpty()) {

                        // On affiche un message d'erreur
                        Toast.makeText(MainActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                    } else {
                        // On se connecte à la base de données
                        login(emailValue, passwordValue);
                    }
                }
            }
        });


        // On ajoute un listener (écouteur d'évenement) sur le bouton d'inscription
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // On récupère les valeurs des champs
                String emailValue = email.getText().toString();
                String pass = password.getText().toString();

                // jBCrypt utilise l'algorithme de hachage Blowfish pour créer des hash de mot de passe.
                // Les hash générés par jBCrypt sont sécurisés et peuvent être utilisés pour stocker des mots de passe de manière sûre.
                String passwordValue = BCrypt.hashpw(pass, BCrypt.gensalt());

                // On vérifie que les champs ne sont pas vides
                if (emailValue.isEmpty() || passwordValue.isEmpty()) {

                    // On affiche un message d'erreur
                    Toast.makeText(MainActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                } else {

                    // On enregistre l'utilisateur
                    register(emailValue, passwordValue);
                }
            }
        });
    }

    // Méthode permettant de se connecter en utilisant une adresse e-mail et un mot de passe
    public void login(String emailValue, String passwordValue) {

        // Recherche d'un utilisateur parmi les documents de la collection 'users'
        // Référence à la collection "utilisateurs" dans la base de données Firestore
        db.collection("users")

                // Récupération de tous les documents dans cette collection sous forme d'un objet QuerySnapshot
                .get()

                // Ajout d'un écouteur pour écouter la fin de l'opération de récupération des documents
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        // Vérification si la tâche s'est terminée avec succès
                        if (task.isSuccessful()) {

                            // Pour chaque document dans les résultats de la tâche
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Toast.makeText(MainActivity.this, "Connexion réussie", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                                startActivity(intent);
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                            // Affiche un message "Connexion échouée"
                            Toast.makeText(MainActivity.this, "Connexion échouée", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    //----------------------------------------------------------------------------------------------------------------------------------------------------------

    // Méthode permettant de s'inscrire en utilisant une adresse e-mail et un mot de passe
    public void register(String emailValue, String passwordValue) {

        // Ajout des valeurs de mot de passe et email dans l'objet HashMap "utilisateur"
        user.put("password", passwordValue);
        user.put("email", emailValue);

        // On vérifie si l'utilisateur existe déjà
        db.collection("users")
                .whereEqualTo("email", emailValue)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() > 0) {
                                Toast.makeText(MainActivity.this, "L'utilisateur existe déjà", Toast.LENGTH_SHORT).show();
                            } else {

                                // Ajout d'un document dans la collection "users" avec les valeurs de l'objet HashMap "utilisateur"
                                db.collection("users")
                                        .add(user)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Toast.makeText(MainActivity.this, "Inscription réussie", Toast.LENGTH_SHORT).show();

                                                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                                                startActivity(intent);
                                            }
                                        })

                                        // Ajout d'un écouteur pour écouter la fin de l'opération d'ajout en cas d'échec
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Affichage d'un log d'erreur
                                                Toast.makeText(MainActivity.this, "Inscription échouée", Toast.LENGTH_SHORT).show();
                                                Log.w(TAG, "Error adding document", e);
                                            }
                                        });
                            }
                        }
                    }
                });

    }
}

// Les fonctions natives :

// getInstance()            : permet de récupérer l'instance de la base de données Firestore
// collection()             : permet de récupérer une référence à une collection
// get()                    : permet de récupérer tous les documents d'une collection sous forme d'un objet QuerySnapshot
// addOnCompleteListener()  : permet d'ajouter un écouteur pour écouter la fin de l'opération de récupération des documents
// isSuccessful()           : permet de vérifier si la tâche s'est terminée avec succès
// getException()           : permet de récupérer l'exception qui a été levée lors de l'exécution de la tâche
// whereEqualTo()           : permet de filtrer les documents d'une collection en fonction d'une condition
// addOnSuccessListener()   : permet d'ajouter un écouteur pour écouter la fin de l'opération d'ajout en cas de succès
// addOnFailureListener()   : permet d'ajouter un écouteur pour écouter la fin de l'opération d'ajout en cas d'échec
// add()                    : permet d'ajouter un document dans une collection
// put()                    : permet d'ajouter une paire clé-valeur dans un objet HashMap
