package br.com.alura.estoque.repository;

import java.io.IOException;
import java.util.List;

import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueWeb;
import br.com.alura.estoque.retrofit.service.ProdutoService;
import retrofit2.Call;
import retrofit2.Response;

public class ProdutoRepository {

    private ProdutoDAO dao;

    public ProdutoRepository(ProdutoDAO dao) {
        this.dao = dao;
    }

    public void buscaProdutos(ProdutosListener listener) {
        new BaseAsyncTask<>(dao::buscaTodos,
                resultado -> {
                    listener.carregado(resultado);
                    buscaProdutosNaApi(listener);
                })
                .execute();
    }

    private void buscaProdutosNaApi(ProdutosListener listener) {
        ProdutoService produtoService = new EstoqueWeb().getProdutoService();
        Call<List<Produto>> call = produtoService.all();

        new BaseAsyncTask<>(() -> {
            try {
                Response<List<Produto>> response = call.execute();
                List<Produto> produtos = response.body();
                dao.salva(produtos);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return dao.buscaTodos();
        },
                produtos -> {
                    listener.carregado(produtos);
                }).execute();
    }

    public interface ProdutosListener {
        void carregado(List<Produto> produtos);
    }
}
