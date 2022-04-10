package jk.mok.grocerypot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

public class itemDescription extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_description);
        Toast.makeText(this, getIntent().getStringExtra("productId"), Toast.LENGTH_SHORT).show();
    }
}