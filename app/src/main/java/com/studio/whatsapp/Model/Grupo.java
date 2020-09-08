package com.studio.whatsapp.Model;

import com.google.firebase.database.DatabaseReference;
import com.studio.whatsapp.Config.ConfiguracaoFirebase;
import com.studio.whatsapp.helper.Base64Custom;

import java.io.Serializable;
import java.util.List;

public class Grupo implements Serializable {

    private String id;
    private String nome;
    private String foto;
    private List<Usuario> membros;

    public Grupo() {
        DatabaseReference databaseReference = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference gruppRef = databaseReference.child("grupos");

        String idFirebase = gruppRef.push().getKey();
        setId(idFirebase);
    }

    public void salvar() {
        DatabaseReference databaseReference = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference gruppRef = databaseReference.child("grupos");

        gruppRef.child(getId()).setValue(this);


        for (Usuario membro : getMembros()) {
            String idRemetente = Base64Custom.codificar(membro.getEmail());
            String idDestinatatio = getId();

            Conversa conversa = new Conversa();
            conversa.setIdRemetente(idRemetente);
            conversa.setIdDestinatario(idDestinatatio);
            conversa.setUltimaMensagem("");
            conversa.setIsGroup("true");
            conversa.setGrupo(this);

            conversa.salvar();
        }


    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public List<Usuario> getMembros() {
        return membros;
    }

    public void setMembros(List<Usuario> membros) {
        this.membros = membros;
    }
}
