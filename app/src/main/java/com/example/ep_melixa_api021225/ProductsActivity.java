package com.example.ep_melixa_api021225;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
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

    private static final String API_URL = "https://fakestores.vercel.app/docs";
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

        final EditText etName = dialogView.findViewById(R.id.et_product_name);
        final EditText etDescription = dialogView.findViewById(R.id.et_product_description);
        final EditText etPrice = dialogView.findViewById(R.id.et_product_price);
        final EditText etStock = dialogView.findViewById(R.id.et_product_stock);

        if (product != null) {
            builder.setTitle("Editar Producto");
            etName.setText(product.getName());
            etDescription.setText(product.getDescription());
            etPrice.setText(String.valueOf(product.getPrice()));
            etStock.setText(String.valueOf(product.getStock()));
        } else {
            builder.setTitle("Nuevo Producto");
        }

        builder.setView(dialogView)
                .setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = etName.getText().toString().trim();
                        String description = etDescription.getText().toString().trim();
                        String priceStr = etPrice.getText().toString().trim();
                        String stockStr = etStock.getText().toString().trim();

                        if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
                            Toast.makeText(ProductsActivity.this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        double price = Double.parseDouble(priceStr);
                        int stock = Integer.parseInt(stockStr);

                        if (product != null) {
                            product.setName(name);
                            product.setDescription(description);
                            product.setPrice(price);
                            product.setStock(stock);
                            new UpdateProductTask().execute(product);
                        } else {
                            Product newProduct = new Product(name, description, price, stock);
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
                .setMessage("¿Está seguro de eliminar " + product.getName() + "?")
                .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new DeleteProductTask().execute(product);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // AsyncTask para GET (Obtener productos)
    private class GetProductsTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                conn.disconnect();

                return response.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
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
                        Product product = new Product(
                                obj.getInt("id"),
                                obj.getString("name"),
                                obj.getString("description"),
                                obj.getDouble("price"),
                                obj.getInt("stock")
                        );
                        productList.add(product);
                    }

                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Toast.makeText(ProductsActivity.this, "Error al procesar datos", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ProductsActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // AsyncTask para POST (Crear producto)
    private class CreateProductTask extends AsyncTask<Product, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Product... products) {
            try {
                Product product = products[0];
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonProduct = new JSONObject();
                jsonProduct.put("name", product.getName());
                jsonProduct.put("description", product.getDescription());
                jsonProduct.put("price", product.getPrice());
                jsonProduct.put("stock", product.getStock());

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(jsonProduct.toString());
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                conn.disconnect();

                return responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            progressDialog.dismiss();

            if (success) {
                Toast.makeText(ProductsActivity.this, "Producto creado exitosamente", Toast.LENGTH_SHORT).show();
                loadProducts();
            } else {
                Toast.makeText(ProductsActivity.this, "Error al crear producto", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // AsyncTask para PUT (Actualizar producto)
    private class UpdateProductTask extends AsyncTask<Product, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Product... products) {
            try {
                Product product = products[0];
                URL url = new URL(API_URL + "/" + product.getId());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonProduct = new JSONObject();
                jsonProduct.put("name", product.getName());
                jsonProduct.put("description", product.getDescription());
                jsonProduct.put("price", product.getPrice());
                jsonProduct.put("stock", product.getStock());

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(jsonProduct.toString());
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                conn.disconnect();

                return responseCode == HttpURLConnection.HTTP_OK;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            progressDialog.dismiss();

            if (success) {
                Toast.makeText(ProductsActivity.this, "Producto actualizado exitosamente", Toast.LENGTH_SHORT).show();
                loadProducts();
            } else {
                Toast.makeText(ProductsActivity.this, "Error al actualizar producto", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // AsyncTask para DELETE (Eliminar producto)
    private class DeleteProductTask extends AsyncTask<Product, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Product... products) {
            try {
                Product product = products[0];
                URL url = new URL(API_URL + "/" + product.getId());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");

                int responseCode = conn.getResponseCode();
                conn.disconnect();

                return responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            progressDialog.dismiss();

            if (success) {
                Toast.makeText(ProductsActivity.this, "Producto eliminado exitosamente", Toast.LENGTH_SHORT).show();
                loadProducts();
            } else {
                Toast.makeText(ProductsActivity.this, "Error al eliminar producto", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}