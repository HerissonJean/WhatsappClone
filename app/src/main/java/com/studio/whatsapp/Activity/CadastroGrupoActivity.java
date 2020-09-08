package com.studio.whatsapp.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.studio.whatsapp.Config.ConfiguracaoFirebase;
import com.studio.whatsapp.Model.Grupo;
import com.studio.whatsapp.Model.Usuario;
import com.studio.whatsapp.R;
import com.studio.whatsapp.adapter.GrupoSelecioandoAdapter;
import com.studio.whatsapp.helper.UsuarioFirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CadastroGrupoActivity extends AppCompatActivity {

    private List<Usuario> listaMenbrosSelecionados = new ArrayList<>();
    private TextView textTotalPArticipantes;
    private GrupoSelecioandoAdapter grupoSelecioandoAdapter;
    private static final int SELECAO_GALERIA = 200;
    private RecyclerView recyclerViewMembrosSelecionados;
    private CircleImageView imageGrupo;
    private StorageReference storageReference;
    private Grupo grupo;
    private FloatingActionButton fabSalvarGrupo;
    private TextView textNomeGrupo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_grupo);
        Toolbar toolbar = findViewById(R.id.toolbar_activityGrupo);
        toolbar.setTitle("  Novo grupo");
        toolbar.setSubtitle("  Defina o nome ");
        setSupportActionBar(toolbar);

        //configurações iniciais
        textTotalPArticipantes = findViewById(R.id.textTotalParticipantes);
        recyclerViewMembrosSelecionados = findViewById(R.id.recyclerMembrosGrupo);
        imageGrupo = findViewById(R.id.ImageGrupo);
        storageReference = ConfiguracaoFirebase.getFirebaseStorege();
        fabSalvarGrupo = findViewById(R.id.fab_SalvarGrupo);
        textNomeGrupo = findViewById(R.id.editNomeGrupo);
        grupo = new Grupo();

        imageGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (i.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(i, SELECAO_GALERIA);
                }
            }
        });



        //recupera a lista de membros passada
        if(getIntent().getExtras()!=null){
            List<Usuario> membros = (List<Usuario>) getIntent().getExtras().getSerializable("membros");
            listaMenbrosSelecionados.addAll(membros);

            textTotalPArticipantes.setText("Participantes: "+listaMenbrosSelecionados.size());
        }

        //configurar recyclerview
        grupoSelecioandoAdapter = new GrupoSelecioandoAdapter(listaMenbrosSelecionados,getApplicationContext());

        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(), LinearLayoutManager.HORIZONTAL, false
        );

        recyclerViewMembrosSelecionados.setLayoutManager(layoutManagerHorizontal);
        recyclerViewMembrosSelecionados.setHasFixedSize(true);
        recyclerViewMembrosSelecionados.setAdapter(grupoSelecioandoAdapter);

        //configurar flaoting
        fabSalvarGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nomeGrupo = textNomeGrupo.getText().toString();
                listaMenbrosSelecionados .add(UsuarioFirebase.getDadosUsuarioLogado());
                grupo.setMembros(listaMenbrosSelecionados);

                grupo.setNome(nomeGrupo);
                grupo.salvar();

                Intent i = new Intent(CadastroGrupoActivity.this, ChatActivity.class);
                i.putExtra("chatGrupo",grupo);
                startActivity(i);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SELECAO_GALERIA){
            Bitmap imagem = null;

            try {
                Uri local = data.getData();
                imagem = MediaStore.Images.Media.getBitmap(getContentResolver(),local );

                if(imagem != null){

                    imageGrupo.setImageBitmap(imagem);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 99, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //salvar imagens no firebase
                    final StorageReference imagemRef = storageReference
                            .child("imagens")
                            .child("grupos")
                            .child(grupo.getId() + ".jpeg");
                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CadastroGrupoActivity.this, "Upload no sucess", Toast.LENGTH_LONG).show();

                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(CadastroGrupoActivity.this, "Upload sucess", Toast.LENGTH_LONG).show();

                            imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    Uri url = task.getResult();
                                    //atualizaFotosUsuario(url);
                                    imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                           String url = task.getResult().toString();
                                           grupo.setFoto(url);
                                        }
                                    });

                                }
                            });
                        }
                    });

                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}