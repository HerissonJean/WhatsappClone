package com.studio.whatsapp.Model;

import com.google.firebase.database.DatabaseReference;
import com.studio.whatsapp.Config.ConfiguracaoFirebase;

public class Conversa {

    private String idRemetente;
    private String idDestinatario;
    private String ultimaMensagem;
    private Usuario ususarioExibicao;
    private String isGroup;
    private Grupo grupo;

    public Conversa() {
        this.setIsGroup("false");
    }

    public void salvar() {
        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference conversasRef = database.child("conversas");

        conversasRef.child(this.getIdRemetente()).child(this.getIdDestinatario()).setValue(this);
    }

    public String getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(String isGroup) {
        this.isGroup = isGroup;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public String getIdRemetente() {
        return idRemetente;
    }

    public void setIdRemetente(String idRemetente) {
        this.idRemetente = idRemetente;
    }

    public String getIdDestinatario() {
        return idDestinatario;
    }

    public void setIdDestinatario(String idDestinatario) {
        this.idDestinatario = idDestinatario;
    }

    public String getUltimaMensagem() {
        return ultimaMensagem;
    }

    public void setUltimaMensagem(String ultimaMensagem) {
        this.ultimaMensagem = ultimaMensagem;
    }

    public Usuario getUsusarioExibicao() {
        return ususarioExibicao;
    }

    public void setUsusarioExibicao(Usuario ususarioExibicao) {
        this.ususarioExibicao = ususarioExibicao;
    }
}
