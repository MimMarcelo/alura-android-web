package br.com.alura.estoque.ui.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import br.com.alura.estoque.R;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.repository.ProdutoRepository;
import br.com.alura.estoque.ui.dialog.EditaProdutoDialog;
import br.com.alura.estoque.ui.dialog.SalvaProdutoDialog;
import br.com.alura.estoque.ui.recyclerview.adapter.ListaProdutosAdapter;

public class ListaProdutosActivity extends AppCompatActivity {

    private static final String TITULO_APPBAR = "Lista de produtos";
    private ListaProdutosAdapter adapter;
    private ProdutoRepository productRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_produtos);
        setTitle(TITULO_APPBAR);

        configuraListaProdutos();
        configuraFabSalvaProduto();

        productRepository = new ProdutoRepository(this);
        productRepository.buscaProdutos(new ProdutoRepository.ProdutosListener<List<Produto>>() {
            @Override
            public void sucesso(List<Produto> resposta) {
                adapter.atualiza(resposta);
            }

            @Override
            public void falha(String erro) {
                Toast.makeText(ListaProdutosActivity.this, erro, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configuraListaProdutos() {
        RecyclerView listaProdutos = findViewById(R.id.activity_lista_produtos_lista);
        adapter = new ListaProdutosAdapter(this, this::abreFormularioEditaProduto);
        listaProdutos.setAdapter(adapter);
        adapter.setOnItemClickRemoveContextMenuListener(
                ((posicao, produto) -> {
                    productRepository.remove(produto, new ProdutoRepository.ProdutosListener<Void>() {
                        @Override
                        public void sucesso(Void resposta) {
                            adapter.remove(posicao);
                        }

                        @Override
                        public void falha(String erro) {
                            Toast.makeText(ListaProdutosActivity.this, erro, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
        );
    }

    private void configuraFabSalvaProduto() {
        FloatingActionButton fabAdicionaProduto = findViewById(R.id.activity_lista_produtos_fab_adiciona_produto);
        fabAdicionaProduto.setOnClickListener(v -> abreFormularioSalvaProduto());
    }

    private void abreFormularioSalvaProduto() {
        new SalvaProdutoDialog(this, produto -> {
            productRepository.salva(produto, new ProdutoRepository.ProdutosListener<Produto>() {
                @Override
                public void sucesso(Produto resposta) {
                    adapter.adiciona(resposta);
                }

                @Override
                public void falha(String erro) {
                    Toast.makeText(ListaProdutosActivity.this, erro, Toast.LENGTH_SHORT).show();
                }
            });
        })
                .mostra();
    }

    private void abreFormularioEditaProduto(int posicao, Produto produto) {
        new EditaProdutoDialog(this, produto,
                produtoEditado -> productRepository.edita(produtoEditado, new ProdutoRepository.ProdutosListener<Produto>() {
                    @Override
                    public void sucesso(Produto resposta) {
                        adapter.edita(posicao, resposta);
                    }

                    @Override
                    public void falha(String erro) {
                        Toast.makeText(ListaProdutosActivity.this, erro, Toast.LENGTH_SHORT).show();
                    }
                }))
                .mostra();
    }
}
