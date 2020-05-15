package br.com.alura.estoque.retrofit.callback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseCallback<T> implements Callback<T> {

    public abstract void onSuccess(T responseBody);

    public abstract void onFail(String error);

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if(response.isSuccessful()) {
            T responseBody = response.body();
            if (responseBody != null) {
                onSuccess(responseBody);
            } else {
                onFail("Response body is null");
            }
        } else {
            onFail("Response failure");
        }
    }

    @Override
    public final void onFailure(Call<T> call, Throwable t) {
        onFail("Communication fail: " + t.getMessage());
    }
}
