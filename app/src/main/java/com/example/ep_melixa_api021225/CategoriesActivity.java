package com.example.ep_melixa_api021225;


import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class CategoriesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Categorías");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView textView = findViewById(R.id.tv_placeholder);
        textView.setText("Módulo de Categorías\n\n" +
                "Aquí puedes implementar el CRUD de categorías\n" +
                "siguiendo el mismo patrón de ProductsActivity");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}