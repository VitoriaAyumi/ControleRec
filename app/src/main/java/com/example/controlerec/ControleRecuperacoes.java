package com.example.controlerec;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import java.util.Map;

public class ControleRecuperacoes extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean salvarDadosAutomaticamente = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controle_rec);

        db.collection("notas").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    boolean possuiRecuperacao = false;

                    LinearLayout linearLayout = findViewById(R.id.linerec);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String nomeMateria = document.getString("nomeMateria");
                        String nomeProf = document.getString("nomeProf");
                        String po = document.getString("prova");
                        String pe = document.getString("pre");
                        float prova, pre;

                        prova = Float.parseFloat(po);
                        pre = Float.parseFloat(pe);

                        if ((prova - pre) < 0) {
                            possuiRecuperacao = true;
                            float nota = calcularNota(document);
                            View existingContainer = linearLayout.findViewWithTag(nomeMateria);

                            if (existingContainer != null) {
                                atualizarContainer(existingContainer, nomeMateria, nomeProf, nota);
                            } else {
                                View container = getLayoutInflater().inflate(R.layout.activity_add_rec, linearLayout, false);
                                container.setTag(nomeMateria);

                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );
                                int marginInPixels = (int) (10 * getResources().getDisplayMetrics().density);
                                layoutParams.setMargins(0, marginInPixels, 0, marginInPixels);
                                container.setLayoutParams(layoutParams);

                                linearLayout.addView(container);
                                atualizarContainer(container, nomeMateria, nomeProf ,nota);

                                preencherDadosDoFirestore(nomeMateria, container);
                            }
                        } else{
                            View existingContainer = linearLayout.findViewWithTag(nomeMateria);
                            excluirDados(nomeMateria, existingContainer);
                        }
                    }

                    if (!possuiRecuperacao) {
                        Toast.makeText(ControleRecuperacoes.this, "Não possui nenhuma Recuperação", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ControleRecuperacoes.this, "Erro ao recuperar dados", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void preencherDadosDoFirestore(String nomeMateria, View container) {
        db.collection("rec").document(nomeMateria).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        CheckBox c1 = container.findViewById(R.id.checkSujes1);
                        CheckBox c2 = container.findViewById(R.id.checkSujes2);
                        CheckBox c3 = container.findViewById(R.id.checkSujes3);
                        CheckBox c4 = container.findViewById(R.id.checkSujes4);
                        EditText conteudos = container.findViewById(R.id.conteuos_txt);

                        c1.setChecked(document.getBoolean("c1"));
                        c2.setChecked(document.getBoolean("c2"));
                        c3.setChecked(document.getBoolean("c3"));
                        c4.setChecked(document.getBoolean("c4"));
                        conteudos.setText(document.getString("conteudos"));
                    }
                } else {
                    Toast.makeText(ControleRecuperacoes.this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private float calcularNota(DocumentSnapshot document) {
        float cred = Float.parseFloat(document.getString("cred"));
        float trab = Float.parseFloat(document.getString("trab"));
        float list = Float.parseFloat(document.getString("list"));
        float prova = Float.parseFloat(document.getString("prova"));
        return cred + trab + list + prova;
    }

    private void atualizarContainer(View container, String nomeMateria, String nomeProf ,float nota) {

        TextView materiaTextView = container.findViewById(R.id.materiarec);
        materiaTextView.setText(nomeMateria);

        TextView profTextView = container.findViewById(R.id.profrec);
        profTextView.setText(nomeProf);

        TextView notaTextView = container.findViewById(R.id.notarec);
        notaTextView.setText(String.valueOf(nota));

        CheckBox c1 = container.findViewById(R.id.checkSujes1);
        CheckBox c2 = container.findViewById(R.id.checkSujes2);
        CheckBox c3 = container.findViewById(R.id.checkSujes3);
        CheckBox c4 = container.findViewById(R.id.checkSujes4);

        CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (salvarDadosAutomaticamente) {
                    salvarDados(nomeMateria, container);
                }
            }
        };

        c1.setOnCheckedChangeListener(checkedChangeListener);
        c2.setOnCheckedChangeListener(checkedChangeListener);
        c3.setOnCheckedChangeListener(checkedChangeListener);
        c4.setOnCheckedChangeListener(checkedChangeListener);

        EditText conteudosEditText = container.findViewById(R.id.conteuos_txt);
        conteudosEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (salvarDadosAutomaticamente) {
                    salvarDados(nomeMateria, container);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void excluirDados(String nomeMateria, View container) {
        db.collection("rec").document(nomeMateria)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Dados de " + nomeMateria + " excluídos com sucesso!");
                        Toast.makeText(ControleRecuperacoes.this, "Dados de " + nomeMateria + " excluídos com sucesso!", Toast.LENGTH_SHORT).show();

                        LinearLayout linearLayout = findViewById(R.id.linerec);
                        linearLayout.removeView(container);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Erro ao excluir dados: " + e.getMessage());
                        Toast.makeText(ControleRecuperacoes.this, "Erro ao excluir dados de " + nomeMateria, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void salvarDados(String nomeMateria, View container) {
        salvarDadosAutomaticamente = false;

        CheckBox c1 = container.findViewById(R.id.checkSujes1);
        CheckBox c2 = container.findViewById(R.id.checkSujes2);
        CheckBox c3 = container.findViewById(R.id.checkSujes3);
        CheckBox c4 = container.findViewById(R.id.checkSujes4);
        EditText conteudos = container.findViewById(R.id.conteuos_txt);

        Map<String, Object> dados = new HashMap<>();
        dados.put("c1", c1.isChecked());
        dados.put("c2", c2.isChecked());
        dados.put("c3", c3.isChecked());
        dados.put("c4", c4.isChecked());
        dados.put("conteudos", conteudos.getText().toString());

        db.collection("rec").document(nomeMateria).set(dados)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Dados de " + nomeMateria + " gravados com sucesso!");
                        Toast.makeText(ControleRecuperacoes.this, "Dados de " + nomeMateria + " salvos com sucesso!", Toast.LENGTH_SHORT);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Erro ao gravar dados: " + e.getMessage());
                        Toast.makeText(ControleRecuperacoes.this, "Erro ao salvar dados de " + nomeMateria, Toast.LENGTH_SHORT);
                    }
                });

        salvarDadosAutomaticamente = true;
    }

}