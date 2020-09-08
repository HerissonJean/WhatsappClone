package com.studio.whatsapp.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.studio.whatsapp.Config.ConfiguracaoFirebase;
import com.studio.whatsapp.Model.Conversa;
import com.studio.whatsapp.Model.Grupo;
import com.studio.whatsapp.Model.Mensagem;
import com.studio.whatsapp.Model.Usuario;
import com.studio.whatsapp.R;
import com.studio.whatsapp.adapter.MensagensAdapter;
import com.studio.whatsapp.helper.Base64Custom;
import com.studio.whatsapp.helper.UsuarioFirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private TextView nome;
    private CircleImageView foto;
    private EditText editMensagem;
    private Usuario usuarioDestinatario;
    private Usuario usuarioRemetente;
    private MensagensAdapter adapter;
    private List<Mensagem> mensagens = new ArrayList<>();
    private String idUsuarioRemetente;
    private static final int SELECAO_CAMERA = 100;
    private String idUsuarioDestinatario;
    private StorageReference storage;
    private RecyclerView recyclerMensagens;
    private DatabaseReference database;
    private ImageView imageCamera;
    private DatabaseReference mensagensRef;
    private ChildEventListener childEventListenerMensagens;
    private Grupo grupo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbar_chatActivity);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        nome = findViewById(R.id.text_chatActivity_nome);
        foto = findViewById(R.id.circle_chatActivity_foto);
        editMensagem = findViewById(R.id.editMensagem);
        recyclerMensagens = findViewById(R.id.recyclerMensagens);
        imageCamera = findViewById(R.id.iamgeCamera);


        //recupera dados remetente
        idUsuarioRemetente = UsuarioFirebase.getIdentificador();
        usuarioRemetente = UsuarioFirebase.getDadosUsuarioLogado();

                //recuperar dados do usu destinatatio
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            if (bundle.containsKey("chatGrupo")) {

                grupo = (Grupo) bundle.getSerializable("chatGrupo");
                idUsuarioDestinatario = grupo.getId();
                nome.setText(grupo.getNome());


                String fotoResgatada = grupo.getFoto();
                if (fotoResgatada != null) {
                    Uri url = Uri.parse(grupo.getFoto());
                    Glide.with(ChatActivity.this)
                            .load(url)
                            .into(foto);
                } else {
                    foto.setImageResource(R.drawable.foto_padrao);
                }
            } else {
                /**/
                usuarioDestinatario = (Usuario) bundle.getSerializable("chatContato");
                nome.setText(usuarioDestinatario.getNome());
                String fotoResgatada = usuarioDestinatario.getFoto();
                if (foto != null) {
                    Uri url = Uri.parse(usuarioDestinatario.getFoto());
                    Glide.with(ChatActivity.this).load(url).into(foto);
                } else {
                    foto.setImageResource(R.drawable.foto_padrao);
                }
                idUsuarioDestinatario = Base64Custom.codificar(usuarioDestinatario.getEmail());
                /**/
            }
        }

        //Configurar adapter
        adapter = new MensagensAdapter(mensagens, getApplicationContext());

        //Configurar recycler
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMensagens.setLayoutManager(layoutManager);
        recyclerMensagens.setAdapter(adapter);

        database = ConfiguracaoFirebase.getFirebaseDatabase();
        storage = ConfiguracaoFirebase.getFirebaseStorege();
        mensagensRef = database.child("mensagens")
                .child(idUsuarioRemetente)
                .child(idUsuarioDestinatario);

        //configurando envio de image no chat
        imageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (i.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(i, SELECAO_CAMERA);
                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == requestCode){
            Bitmap imagem = null;

            try{
                imagem = (Bitmap)data.getExtras().get("data");
                if(imagem!=null){

                    //recuperar dados  da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG,70,baos);
                    final byte[] dadosImagem = baos.toByteArray();

                    //cria nome imagem
                    String nomeImagem = UUID.randomUUID().toString();

                    //configurar referencia para o firebase
                    final StorageReference imageRef = storage.child("imagens")
                            .child("fotos")
                            .child(idUsuarioRemetente)
                            .child(nomeImagem);

                    UploadTask uploadTask = imageRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    String url = task.getResult().toString();


                                    if(usuarioDestinatario != null){
                                        Mensagem mensagem = new Mensagem();
                                        mensagem.setIdUsuario(idUsuarioRemetente);
                                        mensagem.setMensagem("imagem.jpeg");
                                        mensagem.setImagem(url);

                                        //salvar rementente * destinatario
                                        salvarMensagem(idUsuarioRemetente,idUsuarioDestinatario,mensagem);
                                        salvarMensagem(idUsuarioDestinatario,idUsuarioRemetente,mensagem);

                                    }else{

                                        for(Usuario membro: grupo.getMembros()){

                                            String idRemetenteGrupo = Base64Custom.codificar(membro.getEmail());
                                            String idUsusarioLogadoGrupo = UsuarioFirebase.getIdentificador();

                                            Mensagem mensagem = new Mensagem();
                                            mensagem.setIdUsuario(idUsusarioLogadoGrupo );
                                            mensagem.setMensagem("imagem.jpeg");
                                            mensagem.setNome(usuarioRemetente.getNome());
                                            mensagem.setImagem(url);

                                            salvarMensagem(idRemetenteGrupo,idUsuarioDestinatario,mensagem);
                                            salvarConversa(idRemetenteGrupo,idUsuarioDestinatario,usuarioDestinatario,mensagem,true);
                                        }

                                    }
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


    public void enviarMensagem(View view) {

        String textoMensagem = editMensagem.getText().toString();

        if (!textoMensagem.isEmpty()) {

            if (usuarioDestinatario != null) {
                Mensagem mensagem = new Mensagem();
                mensagem.setMensagem(textoMensagem);
                mensagem.setIdUsuario(idUsuarioRemetente);

                //rementente
                salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);
                salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

                //salvar conversa remetente
                salvarConversa(idUsuarioRemetente,idUsuarioDestinatario,usuarioDestinatario,mensagem,false);

                //salvar conversa destinatario

                salvarConversa(idUsuarioDestinatario, idUsuarioRemetente ,usuarioRemetente,mensagem,false);


            } else {

                for(Usuario membro: grupo.getMembros()){

                    String idRemetenteGrupo = Base64Custom.codificar(membro.getEmail());
                    String idUsusarioLogadoGrupo = UsuarioFirebase.getIdentificador();

                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario(idUsusarioLogadoGrupo );
                    mensagem.setMensagem(textoMensagem);
                    mensagem.setNome(usuarioRemetente.getNome());

                    salvarMensagem(idRemetenteGrupo,idUsuarioDestinatario,mensagem);
                    salvarConversa(idRemetenteGrupo,idUsuarioDestinatario,usuarioDestinatario,mensagem,true);
                }
            }
            //akiii

        } else {
            Toast.makeText(ChatActivity.this, "Preencha mensagem", Toast.LENGTH_LONG).show();
        }
    }

    private void salvarConversa(String idRemetente, String idDestinatario,Usuario usuarioExibicao,Mensagem msg, boolean isGroup) {
        Conversa conversaRemetente = new Conversa();
        conversaRemetente.setIdRemetente(idRemetente);
        conversaRemetente.setIdDestinatario(idDestinatario);
        conversaRemetente.setUltimaMensagem(msg.getMensagem());
        if(isGroup){
            //cvs convencional
            conversaRemetente.setIsGroup("true");
            conversaRemetente.setGrupo(grupo);
        }else{
            //cvs convencional
            conversaRemetente.setUsusarioExibicao(usuarioExibicao);
            conversaRemetente.setIsGroup("false");
        }
        conversaRemetente.salvar();
    }

    private void salvarMensagem(String idRemetente, String idDestinatario, Mensagem msg) {

        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        mensagensRef = database.child("mensagens");

        mensagensRef.child(idRemetente)
                .child(idDestinatario)
                .push()
                .setValue(msg);

        editMensagem.setText("");
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarMensagens();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mensagensRef.removeEventListener(childEventListenerMensagens);
    }

    private void recuperarMensagens() {

        mensagens.clear();

        childEventListenerMensagens = mensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("msg", "" + snapshot.getValue());
                Mensagem mensagem = snapshot.getValue(Mensagem.class);
                mensagens.add(mensagem);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}