package com.example.ep_melixa_api021225;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class UsersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Usuarios");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView textView = findViewById(R.id.tv_placeholder);
        textView.setText("Módulo de Usuarios\n\n" +
                "Aquí puedes implementar el CRUD de usuarios\n" +
                "siguiendo el mismo patrón de ProductsActivity");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}