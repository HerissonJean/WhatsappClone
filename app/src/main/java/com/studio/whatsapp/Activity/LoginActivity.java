package com.studio.whatsapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
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
import com.google.firebase.auth.FirebaseUser;
import com.studio.whatsapp.Config.ConfiguracaoFirebase;
import com.studio.whatsapp.Model.Usuario;
import com.studio.whatsapp.R;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText campoEmail, campoSenha;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        campoEmail = findViewById(R.id.InputT_login_email);
        campoSenha = findViewById(R.id.InputT_login_senha);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    }

    public void logarUsuario(Usuario usuario) {
        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()).
                addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    //verifica se o processo de login ocorre
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, " Login ", Toast.LENGTH_SHORT).show();
                            abrirTelaPrincipal();
                        } else {
                            Toast.makeText(LoginActivity.this, "Error Login", Toast.LENGTH_SHORT).show();
                            String excecao = "";
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                excecao = "Usuario nao cadastrado";
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                excecao = "Email ou senha inválido";
                            } catch (Exception e) {
                                excecao = "Erro ao logar usuário: " + e.getMessage();
                                e.printStackTrace();
                            }
                            Toast.makeText(LoginActivity.this, excecao, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void ValidarAutenticacaoUsuario(View view) {

        String email = campoEmail.getText().toString();
        String senha = campoSenha.getText().toString();

        if (!email.isEmpty()) {
            if (!senha.isEmpty()) {

                Usuario usuario = new Usuario();
                usuario.setSenha(senha);
                usuario.setEmail(email);

                logarUsuario(usuario);

            } else {
                Toast.makeText(LoginActivity.this, "Preencha a senha", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(LoginActivity.this, "Preencha o email", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser usuarioAtual = autenticacao.getCurrentUser();
        if(usuarioAtual != null){
            abrirTelaPrincipal();
        }
    }

    public void abrirTelaCadastro(View view) {
        startActivity(new Intent(this, CadastroActivity.class));
    }

    public void abrirTelaPrincipal() {
        startActivity(new Intent(this, PrincipalActivity.class));
    }
}