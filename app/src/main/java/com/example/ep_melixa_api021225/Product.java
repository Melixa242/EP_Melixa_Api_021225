package com.example.ep_melixa_api021225;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;

/**
 * Clase modelo para Producto adaptada a FakeStores API
 * Estructura JSON de la API:
 * {
 *   "id": "5LyQpt",
 *   "title": "Product Title",
 *   "price": 99.99,
 *   "description": "Product description",
 *   "category": "electronics",
 *   "image": "https://image.url",
 *   "rating": { "rate": 4.5, "count": 120 }
 * }
 */
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    // Atributos según API FakeStores
    private String id;              // ID es String en esta API
    private String title;           // Nombre del producto
    private double price;           // Precio
    private String description;     // Descripción
    private String category;        // Categoría
    private String image;           // URL de imagen
    private double rating;          // Calificación
    private int ratingCount;        // Cantidad de valoraciones
    private String availability;    // "InStock" o "OutOfStock"

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor vacío
     */
    public Product() {
        this.availability = "InStock";
    }

    /**
     * Constructor con campos básicos (sin ID)
     * Para crear nuevos productos
     */
    public Product(String title, String description, double price, String category) {
        this();
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    /**
     * Constructor completo con ID
     * Para productos recibidos desde la API
     */
    public Product(String id, String title, String description, double price,
                   String category, String image) {
        this(title, description, price, category);
        this.id = id;
        this.image = image;
    }

    /**
     * Constructor desde JSONObject
     * Facilita el parseo desde la respuesta de la API
     */
    public Product(JSONObject json) throws JSONException {
        this.id = json.optString("id", "");
        this.title = json.optString("title", "");
        this.price = json.optDouble("price", 0.0);
        this.description = json.optString("description", "");
        this.category = json.optString("category", "");
        this.image = json.optString("image", "");
        this.availability = json.optString("availability", "InStock");

        // Parsear rating si existe
        if (json.has("rating") && !json.isNull("rating")) {
            JSONObject ratingObj = json.optJSONObject("rating");
            if (ratingObj != null) {
                this.rating = ratingObj.optDouble("rate", 0.0);
                this.ratingCount = ratingObj.optInt("count", 0);
            }
        }
    }

    // ========== MÉTODOS DE CONVERSIÓN ==========

    /**
     * Convierte el producto a JSONObject para enviar a la API
     * Campos requeridos: title, price, category
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();

        // Solo incluir ID si existe (para UPDATE)
        if (id != null && !id.isEmpty()) {
            json.put("id", id);
        }

        // Campos requeridos
        json.put("title", title != null ? title : "");
        json.put("price", price);
        json.put("category", category != null ? category : "");

        // Campos opcionales
        if (description != null && !description.isEmpty()) {
            json.put("description", description);
        }

        if (image != null && !image.isEmpty()) {
            json.put("image", image);
        }

        if (availability != null && !availability.isEmpty()) {
            json.put("availability", availability);
        }

        return json;
    }

    // ========== GETTERS ==========

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getImage() {
        return image;
    }

    public double getRating() {
        return rating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public String getAvailability() {
        return availability;
    }

    /**
     * Verifica si el producto está en stock
     */
    public boolean isInStock() {
        return "InStock".equalsIgnoreCase(availability);
    }

    // ========== SETTERS ==========

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPrice(double price) {
        if (price >= 0) {
            this.price = price;
        } else {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * Valida que los campos requeridos estén completos
     * Requeridos: title, price, category
     */
    public boolean isValid() {
        return title != null && !title.trim().isEmpty() &&
                price >= 0 &&
                category != null && !category.trim().isEmpty();
    }

    /**
     * Obtiene el stock como String para mostrar
     */
    public String getStockDisplay() {
        return isInStock() ? "En Stock" : "Agotado";
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", availability='" + availability + '\'' +
                '}';
    }
}