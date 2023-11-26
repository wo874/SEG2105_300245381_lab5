package mgarzon.createbest.productcatalog;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText editTextName;
    EditText editTextPrice;
    Button buttonAddProduct;
    ListView listViewProducts;

    List<Product> products;

    DatabaseReference databaseProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextName = findViewById(R.id.editTextName);
        editTextPrice = findViewById(R.id.editTextPrice);
        listViewProducts = findViewById(R.id.listViewProducts);
        buttonAddProduct = findViewById(R.id.addButton);

        products = new ArrayList<>();

        databaseProducts = FirebaseDatabase.getInstance().getReference("products");

        // adding an onclicklistener to button
        buttonAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProduct();
            }
        });

        listViewProducts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Product product=products.get(i);
                showUpdateDeleteDialog(product.getId(), product.getProductName());
                return true;
            }
        });

        //Attaching value event listener
        databaseProducts.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // clearing past product list
                products.clear();
                // iterating through all nodes
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    // getting product
                    Product product=postSnapshot.getValue(Product.class);
                    // adding product to list
                    products.add(product);
                }

                //creating adapter
                ProductList productsAdapter = new ProductList(MainActivity.this, products);
                //attaching adapter to listview
                listViewProducts.setAdapter(productsAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "failed to read value", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUpdateDeleteDialog(final String productId, String productName) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.update_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName = dialogView.findViewById(R.id.editTextName);
        final EditText editTextPrice = dialogView.findViewById(R.id.editTextPrice);
        final Button buttonUpdate = dialogView.findViewById(R.id.buttonUpdateProduct);
        final Button buttonDelete = dialogView.findViewById(R.id.buttonDeleteProduct);

        dialogBuilder.setTitle(productName);
        final AlertDialog b=dialogBuilder.create();
        b.show();

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editTextName.getText().toString().trim();
                double price = Double.parseDouble(String.valueOf(editTextPrice.getText().toString()));
                if (!TextUtils.isEmpty(name)) {
                    updateProduct(productId, name, price);
                    b.dismiss();
                }
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteProduct(productId);
                b.dismiss();
            }
        });
    }

    private void updateProduct(String id, String name, double price) {
        Product product = new Product(id, name, price);
        databaseProducts.child(id).setValue(product);
        Toast.makeText(getApplicationContext(), "Product Updated!", Toast.LENGTH_LONG).show();
    }

    private void deleteProduct(String id) {
        databaseProducts.child(id).removeValue();
        Toast.makeText(getApplicationContext(), "Product Deleted!", Toast.LENGTH_LONG).show();
    }

    private void addProduct() {
        String name= editTextName.getText().toString().trim();
        double price= Double.parseDouble(String.valueOf(editTextPrice.getText().toString()));

        if (!TextUtils.isEmpty(name)) {
            // getting unique id using push().getKey() method
            String id = databaseProducts.push().getKey();

            // creating Product Object
            Product product = new Product(id, name, price);

            // saving the Product
            databaseProducts.child(id).setValue(product);

            // setting edittext to blank
            editTextName.setText("");
            editTextPrice.setText("");

            // displaying success toast
            Toast.makeText(this, "Product added", Toast.LENGTH_LONG).show();
        } else {
            // if value isn't given displaying toast
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_LONG).show();
        }
    }
}
