package jk.mok.grocerypot;

import static android.widget.Toast.LENGTH_SHORT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class product extends AppCompatActivity {

    int cateId;
    List<String> itemIds,itemTitle;
    List<ImageView> images;
    List <LinearLayout> itemLayout;
    LinearLayout lastLayoutSet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        Toolbar toolbar = findViewById(R.id.upperToolBar);
        toolbar.setTitle(getIntent().getStringExtra("categoryTitle"));
        setSupportActionBar(toolbar);

        cateId = getIntent().getIntExtra("cateId",1);
        itemIds = new ArrayList<>();
        itemTitle = new ArrayList<>();
        images = new ArrayList<>();

        itemLayout = new ArrayList<>();
        FirebaseFirestore.getInstance().collection("Products").whereEqualTo("cateId",cateId)
                .get().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        int i=0;
                        for(QueryDocumentSnapshot doc : task.getResult()){
                            itemIds.add(doc.getId());
                            itemTitle.add(doc.get("name").toString());
                            int totalWidth = ((this.getWindowManager().getDefaultDisplay().getWidth()) - 36);
                            itemLayout.add(createItemView(i,totalWidth/2,doc.get("name").toString()));
                            setImages(i);
                            i++;
                        }
                        setItemView(itemLayout);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.searchbar_menu,menu);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.app_bar_search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                try {
                    List<LinearLayout> temp = new ArrayList<>();
                    for (int i = 0; i < itemLayout.size(); i++) {
                        if (itemTitle.get(i).toLowerCase().contains(newText.toLowerCase()))
                            temp.add(itemLayout.get(i));
                    }
                    setItemView(temp);
                }catch (Exception e){
                    Toast.makeText(product.this, e.getMessage(), LENGTH_SHORT).show();
                }
                return false;
            }
        });
        return true;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setItemView(List<LinearLayout> items){
        try {
            ((LinearLayout) findViewById(R.id.itemList)).removeAllViews();
            lastLayoutSet.removeAllViews();
        }catch (Exception ignored){}
        Display display = this.getWindowManager().getDefaultDisplay();
        int totalWidth = ((display.getWidth()) - 36);
        int imgWidth = totalWidth/2;
        for(int i=0; i<items.size(); i+=2) {
            LinearLayout linearLayoutM = new LinearLayout(this);
            linearLayoutM.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(totalWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.leftMargin = 16;
            layoutParams.topMargin = 16;
            layoutParams.rightMargin = 16;
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            lastLayoutSet = linearLayoutM;
            linearLayoutM.setLayoutParams(layoutParams);
            /*..........*/
                linearLayoutM.addView(items.get(i));
            /*..........*/
            LinearLayout l2;
            if(!(items.size()%2==0) && (items.size()-1)==i) {
                l2 = createItemView(i+1,imgWidth,"");
                l2.setVisibility(View.INVISIBLE);
            }
            else
                l2 = items.get(i+1);
            linearLayoutM.addView(l2);
            /*..........*/
            ((LinearLayout)findViewById(R.id.itemList)).addView(linearLayoutM);
        }

    }
    @SuppressLint("ResourceType")
    private LinearLayout createItemView(int id, int imgWidth, String title){
        /*..........*/
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackground(getDrawable(R.drawable.curve_background));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imgWidth, ViewGroup.LayoutParams.WRAP_CONTENT,1);
        layoutParams.rightMargin = 16;
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setId(id);

        ImageView image = new ImageView(this);
        LinearLayout.LayoutParams layoutParamsI = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, imgWidth,1);
        layoutParamsI.topMargin = 24;
        layoutParamsI.leftMargin = 16;
        layoutParamsI.rightMargin = 16;
        image.setLayoutParams(layoutParamsI);
        images.add(image);
        linearLayout.addView(image);

        TextView pnText = new TextView(this);
        LinearLayout.LayoutParams layoutParamsTL = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,1);
        layoutParamsTL.topMargin = 16;
        layoutParamsTL.bottomMargin = 24;
        layoutParamsTL.leftMargin = 16;
        layoutParamsTL.rightMargin = 16;
        pnText.setLayoutParams(layoutParamsTL);
        pnText.setText(title);
        pnText.setTextColor(Color.BLACK);
        pnText.setGravity(Gravity.CENTER_HORIZONTAL);
        linearLayout.addView(pnText);
        //.........
        linearLayout.setOnClickListener(v -> {
            if(linearLayout.getId()<itemIds.size()) {
                Intent intent = new Intent(this,itemDescription.class);
                intent.putExtra("productId",itemIds.get(linearLayout.getId()));
                startActivity(intent);
            }
        });
        return linearLayout;
    }

    private void setImages(int index){
        new Thread(() -> {
            try {
                String name = (itemIds.get(index));
                String imgName = name + ".png";
                final File localFile = File.createTempFile(name, "png");
                StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("/Products/").child(imgName);
                mStorageRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                    images.get(index).setBackground(new BitmapDrawable
                            (getResources(), BitmapFactory.decodeFile(localFile.getAbsolutePath())));

                });
            }catch(Exception ignored){}
        }).start();
    }
}
