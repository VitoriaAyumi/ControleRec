package com.example.controlerec;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class ControleRec extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controle_rec);

        LinearLayout linearLayout = findViewById(R.id.linerec);

        // Verifique se o campo "prova" é menor que o campo "pre" no banco de dados
        db.collection("notas").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    boolean possuiRecuperacao = false;

                    // Configura o LinearLayout para mostrar múltiplos containers
                    LinearLayout linearLayout = findViewById(R.id.linerec);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        // Recupere os valores dos campos "prova" e "pre"
                        String prova = document.getString("prova");
                        String pre = document.getString("pre");

                        // Verifique se o campo "prova" é menor que o campo "pre"
                        if (prova.compareTo(pre) < 0) {
                            possuiRecuperacao = true;

                            // Recupere os valores dos campos "nomeMateria" e "nota"
                            String nomeMateria = document.getString("nomeMateria");
                            String cred = document.getString("cred");
                            String trab = document.getString("trab");
                            String list = document.getString("list");
                            String provaNota = document.getString("prova");

                            // Calcule a nota
                            int nota = Integer.parseInt(cred) + Integer.parseInt(trab) + Integer.parseInt(list) + Integer.parseInt(provaNota);

                            // Crie um container
                            View container = getLayoutInflater().inflate(R.layout.activity_add_rec, null);
                            linearLayout.addView(container);

                            // Encontre os TextViews dentro do container e substitua os valores
                            TextView materiaTextView = container.findViewById(R.id.materiarec);
                            materiaTextView.setText(nomeMateria);

                            TextView notaTextView = container.findViewById(R.id.notarec);
                            notaTextView.setText(String.valueOf(nota));

                            // Encontre o botão dentro do container e adicione um listener a ele
                            Button button = container.findViewById(R.id.salvar);
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    salvarDados(container);
                                }
                            });
                        }
                    }

                    if (!possuiRecuperacao) {
                        // Se o campo "prova" não é menor que o campo "pre", não permita salvar
                        Toast.makeText(ControleRec.this, "Não possui nenhuma Recuperação", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Se houver um erro, mostre uma mensagem de erro
                    Toast.makeText(ControleRec.this, "Erro ao recuperar dados", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void salvarDados(View container) {
        // Encontre os elementos dentro do container
        CheckBox c1 = container.findViewById(R.id.checkSujes1);
        CheckBox c2 = container.findViewById(R.id.checkSujes2);
        CheckBox c3 = container.findViewById(R.id.checkSujes3);
        CheckBox c4 = container.findViewById(R.id.checkSujes4);
        EditText conteudos = container.findViewById(R.id.conteuos_txt);

        // Crie um campo para armazenar o último ID gerado
        db.collection("config").document("ultimo_id").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Recupere o último ID gerado
                        long ultimoId = document.getLong("ultimo_id");

                        // Gere um novo ID em ordem numérica
                        long novoId = ultimoId + 1;

                        // Salve o novo ID no banco de dados
                        db.collection("config").document("ultimo_id").set(new HashMap<String, Object>() {{
                            put("ultimo_id", novoId);
                        }});

                        // Use o novo ID para salvar os dados
                        db.collection("rec").document(String.valueOf(novoId)).set(new HashMap<String, Object>() {{
                                    put("containerId", String.valueOf(novoId));
                                    put("c1", c1.isChecked());
                                    put("c2", c2.isChecked());
                                    put("c3", c3.isChecked());
                                    put("c4", c4.isChecked());
                                    put("conteudos", conteudos.getText().toString());
                                }})
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("Firestore", "Dados gravados com sucesso!");
                                        Toast.makeText(ControleRec.this, "Dados salvos com sucesso!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("Firestore", "Erro ao gravar dados: " + e.getMessage());
                                        Toast.makeText(ControleRec.this, "Erro ao salvar dados", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // Se o documento não existe, crie um novo ID
                        long novoId = 1;

                        // Salve o novo ID no banco de dados
                        db.collection("config").document("ultimo_id").set(new HashMap<String, Object>() {{
                            put("ultimo_id", novoId);
                        }});

                        // Use o novo ID para salvar os dados
                        db.collection("rec").document(String.valueOf(novoId)).set(new HashMap<String, Object>() {{
                                    put("containerId", String.valueOf(novoId));
                                    put("c1", c1.isChecked());
                                    put("c2", c2.isChecked());
                                    put("c3", c3.isChecked());
                                    put("c4", c4.isChecked());
                                    put("conteudos", conteudos.getText().toString());
                                }})
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("Firestore", "Dados gravados com sucesso!");
                                        Toast.makeText(ControleRec.this, "Dados salvos com sucesso!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("Firestore", "Erro ao gravar dados: " + e.getMessage());
                                        Toast.makeText(ControleRec.this, "Erro ao salvar dados", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } else {
                    // Se houver um erro, mostre uma mensagem de erro
                    Toast.makeText(ControleRec.this, "Erro ao recuperar dados", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}