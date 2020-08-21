package com.studio.whatsapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.studio.whatsapp.Config.ConfiguracaoFirebase;
import com.studio.whatsapp.Model.Usuario;
import com.studio.whatsapp.R;

public class CadastroActivity extends AppCompatActivity {

    private TextInputEditText campoEmail, campoNome, campoSenha;
    private Button bt_cadastrar;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        campoNome = findViewById(R.id.InputT_cadastro_nome);
        campoEmail = findViewById(R.id.InputT_cadastro_email);
        campoSenha = findViewById(R.id.InputT_cadastro_senha);
        bt_cadastrar = findViewById(R.id.bt_cadastro_cadastrar);

    }

    public void cadastrarUsuario(Usuario usuario) {

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(usuario.getEmail(), usuario.getSenha()).
                addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(CadastroActivity.this, "Sucess", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String excecao = "";
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                excecao = "Digite uma senha mais forte";
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                excecao = "Email inválido";
                            } catch (FirebaseAuthUserCollisionException e) {
                                excecao = "Email já cadastrada";
                            } catch (Exception e) {
                                excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                                e.printStackTrace();
                            }
                            Toast.makeText(CadastroActivity.this, excecao, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    public void validarCadastroUsuario(View view) {

        String nome = campoNome.getText().toString();
        String email = campoEmail.getText().toString();
        String senha = campoSenha.getText().toString();

        if (!nome.isEmpty()) {
            if (!email.isEmpty()) {
                if (!senha.isEmpty()) {

                    Usuario usuario = new Usuario();
                    usuario.setEmail(email);
                    usuario.setNome(nome);
                    usuario.setSenha(senha);

                    cadastrarUsuario(usuario);
                } else {
                    Toast.makeText(this, "Preencha senha", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Preencha o email", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Preencha o Nome", Toast.LENGTH_SHORT).show();
        }

    }
}