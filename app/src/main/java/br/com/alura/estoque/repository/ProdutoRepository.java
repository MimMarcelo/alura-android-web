package br.com.alura.estoque.repository;

import java.io.IOException;
import java.util.List;

import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueWeb;
import br.com.alura.estoque.retrofit.service.ProdutoService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProdutoRepository {

    private final ProdutoDAO dao;
    private final ProdutoService produtoService;

    public ProdutoRepository(ProdutoDAO dao) {
        this.dao = dao;
        produtoService = new EstoqueWeb().getProdutoService();
    }

    public void buscaProdutos(ProdutosListener<List<Produto>> listener) {
        new BaseAsyncTask<>(dao::buscaTodos,
                resultado -> {
                    listener.sucesso(resultado);
                    buscaProdutosNaApi(listener);
                })
                .execute();
    }

    private void buscaProdutosNaApi(ProdutosListener<List<Produto>> listener) {
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
                    listener.sucesso(produtos);
                }).execute();
    }

    public void salva(Produto produto, ProdutosListener<Produto> listener) {

        Call<Produto> call = produtoService.save(produto);
        call.enqueue(new Callback<Produto>() {
            @Override
            public void onResponse(Call<Produto> call, Response<Produto> response) {
                if(response.isSuccessful()) {
                    Produto produtoSalvo = response.body();
                    if(produtoSalvo != null) {
                        salvaInternamente(produtoSalvo, listener);
                    }
                }
            }

            @Override
            public void onFailure(Call<Produto> call, Throwable t) {
                listener.falha("Não foi possível estabelecer conexão: " + t.getMessage());
            }
        });
    }

    private void salvaInternamente(Produto produto, ProdutosListener<Produto> listener) {
        new BaseAsyncTask<>(() -> {
            long id = dao.salva(produto);
            return dao.buscaProduto(id);
        },
                listener::sucesso
        )
                .execute();
    }

    public interface ProdutosListener<T> {
        void sucesso(T resposta);
        void falha(String erro);
    }
}
