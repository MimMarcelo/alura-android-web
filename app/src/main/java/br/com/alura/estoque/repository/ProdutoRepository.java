package br.com.alura.estoque.repository;

import java.io.IOException;
import java.util.List;

import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueWeb;
import br.com.alura.estoque.retrofit.callback.BaseCallback;
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
        call.enqueue(new BaseCallback<List<Produto>>() {
            @Override
            public void onSuccess(List<Produto> responseBody) {
                new BaseAsyncTask<>(() -> {
                    dao.salva(responseBody);
                    return dao.buscaTodos();
                },
                        produtos -> {
                            listener.sucesso(produtos);
                        }
                ).execute();

            }

            @Override
            public void onFail(String error) {
                listener.falha(error);
            }
        });
    }

    public void salva(Produto produto, ProdutosListener<Produto> listener) {

        Call<Produto> call = produtoService.save(produto);
        call.enqueue(new BaseCallback<Produto>() {
            @Override
            public void onSuccess(Produto responseBody) {
                salvaInternamente(responseBody, listener);
            }

            @Override
            public void onFail(String error) {
                listener.falha(error);
            }
        });
    }

    public void edita(Produto produto, ProdutosListener<Produto> listener) {
        Call<Produto> call = produtoService.edita(produto.getId(), produto);
        call.enqueue(new BaseCallback<Produto>() {
            @Override
            public void onSuccess(Produto responseBody) {
                new BaseAsyncTask<>(() -> {
                    dao.atualiza(produto);
                    return produto;
                },
                        listener::sucesso
                ).execute();
            }

            @Override
            public void onFail(String error) {
                listener.falha(error);
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
