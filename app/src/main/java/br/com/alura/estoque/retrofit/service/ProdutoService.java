package br.com.alura.estoque.retrofit.service;

import java.util.List;

import br.com.alura.estoque.model.Produto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ProdutoService {

    @GET("produto")
    Call<List<Produto>> all();

    @POST("produto")
    Call<Produto> save(@Body Produto produto);

    @PUT("produto/{id}")
    Call<Produto> edita(@Path("id") long produtoId, @Body Produto produto);

    @DELETE("produto/{id}")
    Call<Void> remove(@Path("id") long produtoId);
}
