package com.studio.whatsapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.studio.whatsapp.Model.Usuario;
import com.studio.whatsapp.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GrupoSelecioandoAdapter extends RecyclerView.Adapter<GrupoSelecioandoAdapter.MyViewHolder> {


    private List<Usuario> contatosSelecioandos;
    private Context context;

    public GrupoSelecioandoAdapter(List<Usuario> listaContatos, Context c) {
        this.contatosSelecioandos = listaContatos;
        this.context = c;
    }

    @NonNull
    @Override
    public GrupoSelecioandoAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_grupo_selecionado, parent, false);
        return new GrupoSelecioandoAdapter.MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull GrupoSelecioandoAdapter.MyViewHolder holder, int position) {

        Usuario usuario = contatosSelecioandos.get(position);

        /* caso o email seja 0 usu é definido como cabeçalho */

        holder.nome.setText(usuario.getNome());

        if (usuario.getFoto() != null) {
            Uri uri = Uri.parse(usuario.getFoto());
            Glide.with(context).load(uri).into(holder.foto);
        } else {
            holder.foto.setImageResource(R.drawable.foto_padrao);

        }
    }

    @Override
    public int getItemCount() {
        return contatosSelecioandos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView foto;
        TextView nome;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            foto = itemView.findViewById(R.id.image_fotoSelecionada);
            nome = itemView.findViewById(R.id.text_nomeSelecionado);

        }
    }
}
