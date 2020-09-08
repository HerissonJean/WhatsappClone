package com.studio.whatsapp.Activity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.studio.whatsapp.Config.ConfiguracaoFirebase;
import com.studio.whatsapp.Model.Usuario;
import com.studio.whatsapp.R;
import com.studio.whatsapp.adapter.ContatosAdapter;
import com.studio.whatsapp.adapter.GrupoSelecioandoAdapter;
import com.studio.whatsapp.helper.RecyclerItemClickListener;
import com.studio.whatsapp.helper.UsuarioFirebase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



public class GrupoActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMembrosSelecioandos, recyclerViewMenbros;
    private ContatosAdapter contatosAdapter;
    private GrupoSelecioandoAdapter grupoAdapter;
    private List<Usuario> listaMenbros = new ArrayList<>();
    private List<Usuario> listaMenbrosSelecionados = new ArrayList<>();
    private ValueEventListener valueEventListenerMembros;
    private DatabaseReference usuarioRef;
    private FirebaseUser usuarioAtual;
    private Toolbar toolbar;
    private  FloatingActionButton fabAvancarCadastro;

    public void atualizarMembros(){
        toolbar.setTitle("Novo grupo");
        int totalSelecionados = listaMenbrosSelecionados.size();
        int total = listaMenbros.size()  + totalSelecionados;
        toolbar.setSubtitle( totalSelecionados + " de " + total + " selecionados ");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupo);
        toolbar = findViewById(R.id.toolbar_activityGrupo);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Novo Grupo");


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //configurações iniciais
        recyclerViewMenbros = findViewById(R.id.recyclerMembros);
        recyclerViewMembrosSelecioandos = findViewById(R.id.recyclerMembrosSelecionados);
        usuarioRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();
        fabAvancarCadastro = findViewById(R.id.fab_AvancarCadastro);

        //Configurando adapter
        contatosAdapter = new ContatosAdapter(listaMenbros, getApplicationContext());

        //Configurando recycler menbros
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewMenbros.setLayoutManager(layoutManager);
        recyclerViewMenbros.setHasFixedSize(true);
        recyclerViewMenbros.setAdapter(contatosAdapter);

        recyclerViewMenbros.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getApplicationContext(),
                        recyclerViewMenbros,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Usuario usuariosSelecionado = listaMenbros.get(position);

                                //remover usuario selecionado da lista
                                listaMenbros.remove(usuariosSelecionado);
                                contatosAdapter.notifyDataSetChanged();
                                //adicionar usuario na nova lista de selecioando
                                listaMenbrosSelecionados.add(usuariosSelecionado);
                                grupoAdapter.notifyDataSetChanged();
                                atualizarMembros();

                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            }
                        }
                ));

        grupoAdapter = new GrupoSelecioandoAdapter(listaMenbrosSelecionados, getApplicationContext());

        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(), LinearLayoutManager.HORIZONTAL, false
        );
        recyclerViewMembrosSelecioandos.setLayoutManager(layoutManagerHorizontal);
        recyclerViewMembrosSelecioandos.setHasFixedSize(true);
        recyclerViewMembrosSelecioandos.setAdapter(grupoAdapter);

        recyclerViewMembrosSelecioandos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getApplicationContext(),
                        recyclerViewMenbros,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Usuario usuarioSelecionado = listaMenbrosSelecionados.get(position);
                                listaMenbrosSelecionados.remove(usuarioSelecionado);
                                grupoAdapter.notifyDataSetChanged();
                                listaMenbros.add(usuarioSelecionado);
                                contatosAdapter.notifyDataSetChanged();
                                atualizarMembros();

                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            }
                        }
                )
        );
        //Configurar floating action button
        fabAvancarCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(GrupoActivity.this, CadastroGrupoActivity.class);
                i.putExtra("membros",(Serializable) listaMenbrosSelecionados);
                startActivity(i);
            }
        });
    }

    private void recuperarContatos() {

        valueEventListenerMembros = usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dados : snapshot.getChildren()) {

                    Usuario usuario = dados.getValue(Usuario.class);
                    String email = usuarioAtual.getEmail();
                    if (!email.equals(usuario.getEmail())) {
                        listaMenbros.add(usuario);
                    }

                }
                contatosAdapter.notifyDataSetChanged();
                atualizarMembros();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarContatos();
    }

    @Override
    protected void onStop() {
        super.onStop();
        usuarioRef.removeEventListener(valueEventListenerMembros);
    }
}