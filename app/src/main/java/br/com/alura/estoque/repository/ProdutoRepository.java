package br.com.alura.estoque.repository;

import android.content.Context;

import java.util.List;

import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.EstoqueDatabase;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueWeb;
import br.com.alura.estoque.retrofit.callback.BaseCallback;
import br.com.alura.estoque.retrofit.callback.VoidCallback;
import br.com.alura.estoque.retrofit.service.ProdutoService;
import retrofit2.Call;

public class ProdutoRepository {

    private final ProdutoDAO dao;
    private final ProdutoService produtoService;

    public ProdutoRepository(Context context) {
        EstoqueDatabase db = EstoqueDatabase.getInstance(context);
        dao = db.getProdutoDAO();
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


    public void remove(Produto produto, ProdutosListener<Void> listener) {

        Call<Void> call = produtoService.remove(produto.getId());
        call.enqueue(new VoidCallback() {
            @Override
            public void onSuccess(Void responseBody) {
                new BaseAsyncTask<>(() -> {
                    dao.remove(produto);
                    return null;
                }, listener::sucesso
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
