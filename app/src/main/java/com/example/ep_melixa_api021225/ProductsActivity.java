package android.example.ep_melixa_api021225;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ProductsActivity extends AppCompatActivity implements ProductAdapter.ProductActionListener {

    // URL Base de la API FakeStores
    private static final String API_BASE_URL = "https://fakestores.vercel.app/api";
    private static final String API_PRODUCTS = API_BASE_URL + "/products";

    // Categorías disponibles
    private static final String[] CATEGORIES = {
            "electronics", "jewelery", "men's clothing", "women's clothing"
    };

    private ListView listView;
    private FloatingActionButton fab;
    private ProductAdapter adapter;
    private ArrayList<Product> productList;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Productos");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        listView = findViewById(R.id.list_products);
        fab = findViewById(R.id.fab_add_product);

        productList = new ArrayList<>();
        adapter = new ProductAdapter(this, productList, this);
        listView.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando...");
        progressDialog.setCancelable(false);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProductDialog(null);
            }
        });

        loadProducts();
    }

    private void loadProducts() {
        new GetProductsTask().execute();
    }

    private void showProductDialog(final Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_product, null);

        final EditText etTitle = dialogView.findViewById(R.id.et_product_name);
        final EditText etDescription = dialogView.findViewById(R.id.et_product_description);
        final EditText etPrice = dialogView.findViewById(R.id.et_product_price);
        final Spinner spinnerCategory = dialogView.findViewById(R.id.spinner_category);

        // Configurar Spinner de categorías
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, CATEGORIES
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        if (product != null) {
            builder.setTitle("Editar Producto");
            etTitle.setText(product.getTitle());
            etDescription.setText(product.getDescription());
            etPrice.setText(String.valueOf(product.getPrice()));

            // Seleccionar categoría actual
            for (int i = 0; i < CATEGORIES.length; i++) {
                if (CATEGORIES[i].equals(product.getCategory())) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        } else {
            builder.setTitle("Nuevo Producto");
        }

        builder.setView(dialogView)
                .setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String title = etTitle.getText().toString().trim();
                        String description = etDescription.getText().toString().trim();
                        String priceStr = etPrice.getText().toString().trim();
                        String category = spinnerCategory.getSelectedItem().toString();

                        // Validar campos requeridos
                        if (title.isEmpty() || priceStr.isEmpty()) {
                            Toast.makeText(ProductsActivity.this,
                                    "El título y precio son obligatorios", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        double price;
                        try {
                            price = Double.parseDouble(priceStr);
                            if (price < 0) {
                                Toast.makeText(ProductsActivity.this,
                                        "El precio debe ser positivo", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(ProductsActivity.this,
                                    "Precio inválido", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (product != null) {
                            // Actualizar producto existente
                            product.setTitle(title);
                            product.setDescription(description);
                            product.setPrice(price);
                            product.setCategory(category);
                            new UpdateProductTask().execute(product);
                        } else {
                            // Crear nuevo producto
                            Product newProduct = new Product(title, description, price, category);
                            new CreateProductTask().execute(newProduct);
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .create()
                .show();
    }

    @Override
    public void onEditProduct(Product product) {
        showProductDialog(product);
    }

    @Override
    public void onDeleteProduct(final Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Está seguro de eliminar \"" + product.getTitle() + "\"?")
                .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new DeleteProductTask().execute(product);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ========== AsyncTask para GET (Obtener productos) ==========
    private class GetProductsTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            HttpURLConnection conn = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(API_PRODUCTS);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                return response.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                if (reader != null) {
                    try { reader.close(); } catch (Exception e) { e.printStackTrace(); }
                }
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();

            if (result != null) {
                try {
                    productList.clear();
                    JSONArray jsonArray = new JSONArray(result);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        Product product = new Product(obj);
                        productList.add(product);
                    }

                    adapter.notifyDataSetChanged();
                    Toast.makeText(ProductsActivity.this,
                            productList.size() + " productos cargados", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ProductsActivity.this,
                            "Error al procesar datos", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ProductsActivity.this,
                        "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ========== AsyncTask para POST (Crear producto) ==========
    private class CreateProductTask extends AsyncTask<Product, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Creando producto...");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Product... products) {
            HttpURLConnection conn = null;

            try {
                Product product = products[0];
                URL url = new URL(API_PRODUCTS);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                // Convertir producto a JSON
                JSONObject jsonProduct = product.toJSON();

                // Enviar JSON
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(jsonProduct.toString());
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                return responseCode == HttpURLConnection.HTTP_OK ||
                        responseCode == HttpURLConnection.HTTP_CREATED;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            progressDialog.dismiss();

            if (success) {
                Toast.makeText(ProductsActivity.this,
                        "Producto creado exitosamente", Toast.LENGTH_SHORT).show();
                loadProducts();
            } else {
                Toast.makeText(ProductsActivity.this,
                        "Error al crear producto", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ========== AsyncTask para PUT (Actualizar producto) ==========
    private class UpdateProductTask extends AsyncTask<Product, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Actualizando producto...");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Product... products) {
            HttpURLConnection conn = null;

            try {
                Product product = products[0];
                URL url = new URL(API_PRODUCTS + "/" + product.getId());
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                // Convertir producto a JSON
                JSONObject jsonProduct = product.toJSON();

                // Enviar JSON
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(jsonProduct.toString());
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                return responseCode == HttpURLConnection.HTTP_OK;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            progressDialog.dismiss();

            if (success) {
                Toast.makeText(ProductsActivity.this,
                        "Producto actualizado exitosamente", Toast.LENGTH_SHORT).show();
                loadProducts();
            } else {
                Toast.makeText(ProductsActivity.this,
                        "Error al actualizar producto", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ========== AsyncTask para DELETE (Eliminar producto) ==========
    private class DeleteProductTask extends AsyncTask<Product, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Eliminando producto...");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Product... products) {
            HttpURLConnection conn = null;

            try {
                Product product = products[0];
                URL url = new URL(API_PRODUCTS + "/" + product.getId());
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                return responseCode == HttpURLConnection.HTTP_OK ||
                        responseCode == HttpURLConnection.HTTP_NO_CONTENT;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            progressDialog.dismiss();

            if (success) {
                Toast.makeText(ProductsActivity.this,
                        "Producto eliminado exitosamente", Toast.LENGTH_SHORT).show();
                loadProducts();
            } else {
                Toast.makeText(ProductsActivity.this,
                        "Error al eliminar producto", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}