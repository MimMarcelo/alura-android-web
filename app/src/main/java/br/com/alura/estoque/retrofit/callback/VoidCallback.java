package br.com.alura.estoque.retrofit.callback;

import retrofit2.Call;
import retrofit2.Response;

public abstract class VoidCallback extends BaseCallback<Void> {
    @Override
    public void onResponse(Call<Void> call, Response<Void> response) {
        if(response.isSuccessful()) {
            onSuccess(null);

        } else {
            onFail("Response failure");
        }
    }
}
