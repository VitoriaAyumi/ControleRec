package com.example.controlerec;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class ControleRec extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controle_rec);

        LinearLayout linearLayout = findViewById(R.id.linerec);
        if()

        for (int i = 0; i < 3; i++) {
            View container = getLayoutInflater().inflate(R.layout.activity_add_rec, null);
            linearLayout.addView(container);

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

    private void salvarDados(View container) {
        // Encontre os elementos dentro do container
        CheckBox c1 = container.findViewById(R.id.checkSujes1);
        CheckBox c2 = container.findViewById(R.id.checkSujes2);
        CheckBox c3 = container.findViewById(R.id.checkSujes3);
        CheckBox c4 = container.findViewById(R.id.checkSujes4);
        EditText conteudos = container.findViewById(R.id.conteuos_txt);

        // Crie um ID único para cada container
        String containerId = UUID.randomUUID().toString();

        // Salve os dados no banco de dados Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("rec").document(containerId).set(new DadosRec(
                containerId,
                c1.isChecked(),
                c2.isChecked(),
                c3.isChecked(),
                c4.isChecked(),
                conteudos.getText().toString()
        )).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Verifique se o ID já existe no banco de dados
                    firestore.collection("rec").document(containerId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    // Se o ID já existe, não permita salvar novamente
                                    Toast.makeText(ControleRec.this, "ID já existe, não é possível salvar novamente", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Se o ID não existe, permita salvar
                                    Toast.makeText(ControleRec.this, "Dados salvos com sucesso!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Se houver um erro, mostre uma mensagem de erro
                                Toast.makeText(ControleRec.this, "Erro ao salvar dados", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    // Se houver um erro, mostre uma mensagem de erro
                    Toast.makeText(ControleRec.this, "Erro ao salvar dados", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}