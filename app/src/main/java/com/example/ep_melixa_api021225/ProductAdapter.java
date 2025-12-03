package com.example.ep_melixa_api021225;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;

public class ProductAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Product> products;
    private ProductActionListener listener;

    public interface ProductActionListener {
        void onEditProduct(Product product);
        void onDeleteProduct(Product product);
    }

    public ProductAdapter(Context context, ArrayList<Product> products, ProductActionListener listener) {
        this.context = context;
        this.products = products;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public Object getItem(int position) {
        return products.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
            holder = new ViewHolder();
            holder.tvName = convertView.findViewById(R.id.tv_product_name);
            holder.tvDescription = convertView.findViewById(R.id.tv_product_description);
            holder.tvPrice = convertView.findViewById(R.id.tv_product_price);
            holder.tvStock = convertView.findViewById(R.id.tv_product_stock);
            holder.btnEdit = convertView.findViewById(R.id.btn_edit);
            holder.btnDelete = convertView.findViewById(R.id.btn_delete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Product product = products.get(position);

        // Mostrar título del producto
        holder.tvName.setText(product.getTitle() != null ? product.getTitle() : "Sin título");

        // Mostrar descripción (truncada si es muy larga)
        String description = product.getDescription();
        if (description != null && description.length() > 80) {
            description = description.substring(0, 77) + "...";
        }
        holder.tvDescription.setText(description != null ? description : "Sin descripción");

        // Mostrar precio
        holder.tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", product.getPrice()));

        // Mostrar disponibilidad
        String stockText = product.getStockDisplay();
        if (product.getRatingCount() > 0) {
            stockText += String.format(Locale.getDefault(), " • ★%.1f (%d)",
                    product.getRating(), product.getRatingCount());
        }
        holder.tvStock.setText(stockText);

        // Cambiar color según disponibilidad
        if (!product.isInStock()) {
            holder.tvStock.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        } else {
            holder.tvStock.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        }

        // Listeners de botones
        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onEditProduct(product);
                }
            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDeleteProduct(product);
                }
            }
        });

        return convertView;
    }

    static class ViewHolder {
        TextView tvName;
        TextView tvDescription;
        TextView tvPrice;
        TextView tvStock;
        ImageButton btnEdit;
        ImageButton btnDelete;
    }
}