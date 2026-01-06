package com.example.myapplication;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProdutoAdapter extends RecyclerView.Adapter<ProdutoAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(dashboard.Produto produto);
    }

    private List<dashboard.Produto> produtos;
    private OnItemClickListener listener;

    // 🔥 Guarda qual item está selecionado
    private int itemSelecionado = -1;

    public ProdutoAdapter(List<dashboard.Produto> produtos, OnItemClickListener listener) {
        this.produtos = produtos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_produto_entrada, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        dashboard.Produto produto = produtos.get(position);
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        holder.txtNome.setText(produto.nome);
        holder.txtCategoria.setText(produto.nomeCategoria);
        holder.txtQuantidade.setText("Estoque: " + produto.quantidade);
        holder.txtUltimoCusto.setText("Último custo: " + nf.format(produto.precoCusto));

        // 🎨 APLICAR COR DE SELEÇÃO OU NORMAL
        if (position == itemSelecionado) {
            // SELECIONADO (verde, igual sua imagem)
            holder.cardProduto.setCardBackgroundColor(Color.parseColor("#E6FFF0"));
            holder.cardProduto.setStrokeColor(Color.parseColor("#18C45A"));
            holder.cardProduto.setStrokeWidth(4);

        } else {
            // NORMAL
            holder.cardProduto.setCardBackgroundColor(Color.parseColor("#99FFFFFF"));
            holder.cardProduto.setStrokeColor(Color.parseColor("#C3C3C3"));
            holder.cardProduto.setStrokeWidth(2);
        }

        // 🎯 Ao clicar: muda item selecionado e notifica adapter
        holder.itemView.setOnClickListener(v -> {

            itemSelecionado = holder.getAdapterPosition();
            notifyDataSetChanged(); // atualiza visualmente

            if (listener != null) listener.onItemClick(produto);
        });
    }

    @Override
    public int getItemCount() {
        return produtos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtNome, txtCategoria, txtQuantidade, txtUltimoCusto;
        MaterialCardView cardProduto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNome = itemView.findViewById(R.id.nomeProduto10);
            txtUltimoCusto = itemView.findViewById(R.id.ultimoCusto);
            txtCategoria = itemView.findViewById(R.id.categoriaProduto2);
            txtQuantidade = itemView.findViewById(R.id.situacaoProduto);
            cardProduto = itemView.findViewById(R.id.cardDoProduto);
        }
    }
}
