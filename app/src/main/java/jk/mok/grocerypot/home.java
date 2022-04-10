package jk.mok.grocerypot;


import static android.widget.Toast.LENGTH_SHORT;

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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;

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

public class home extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private List<String> highlightedItemIds,highlightedItemTitle;
    private List<ImageView> highlightedItemImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.upperToolBar);
        setSupportActionBar(toolbar);

        viewFlipper = findViewById(R.id.imageSlider);
        highlightedItemImages = new ArrayList<>();
        highlightedItemTitle = new ArrayList<>();
        highlightedItemIds = new ArrayList<>();

        userTypeToViewItemHead();
        setImageSlider();
        setAdv();
        setHighlightedItem();
        redirectProductPage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu,menu);
        /*final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<String> list = new ArrayList<>();
                list.add("hi");
                list.add("Hago");
                list.add("Helo");
                ArrayAdapter<String>adapter = new ArrayAdapter<>(home.this, android.R.layout.simple_list_item_1,list);
                ((ListView)findViewById(R.id.searchListView)).setAdapter(adapter);
                adapter.getFilter().filter(newText.toLowerCase());
                return false;
            }
        });*/
        return true;
    }
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item) {
        //respond to menu item selection
        switch (item.getItemId()) {
            case R.id.profile:
                startActivity(new Intent(this,profile.class));
                return true;
            case R.id.cart:
                startActivity(new Intent(this,cart.class));
                return true;
            default:
                Toast.makeText(this, "Error", LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void userTypeToViewItemHead(){
        new Thread(() -> FirebaseFirestore.getInstance().collection("users").document("uniqueId")
                .get().addOnCompleteListener(task -> {
            if(task.isSuccessful() && Integer.parseInt(task.getResult().get("typeId").toString())>0)
                findViewById(R.id.productTile1).setVisibility(View.VISIBLE);
        })).start();
    }
    private void setImageSlider(){
        new Thread(new Runnable() {
            int index=0;
            @Override
            public void run() {
                try {
                    String name = ("sliderImg"+(index+1)).trim();
                    String imgName = name + ".png";
                    final File localFile = File.createTempFile(name, "png");
                    StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("/productExtra/").child(imgName);
                    mStorageRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                        // ...
                        ImageView imageView = new ImageView(home.this);
                        imageView.setBackground(new BitmapDrawable
                                (getResources(), BitmapFactory.decodeFile(localFile.getAbsolutePath())));
                        viewFlipper.addView(imageView);
                    });
                }catch(Exception ignored){}
                index++;
                caller();
            }
            private void caller(){
                if (index<3)
                    run();
                else
                    flipperImages();

            }
        }).start();
    }
    public void flipperImages(){
        viewFlipper.startFlipping();
        viewFlipper.setFlipInterval(2000);
        viewFlipper.setAutoStart(true);
        viewFlipper.setInAnimation(this,R.anim.slide_in_right);
        viewFlipper.setOutAnimation(this,R.anim.slide_out_left);
    }
    private void setAdv(){
        new Thread(() -> {
            try {
                String name = ("adBanner").trim();
                String imgName = name + ".png";
                final File localFile = File.createTempFile(name, "png");

                StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("/productExtra/").child(imgName);
                mStorageRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                    // ...
                    findViewById(R.id.adBanner).setBackground(new BitmapDrawable
                            (getResources(), BitmapFactory.decodeFile(localFile.getAbsolutePath())));
                }).addOnFailureListener(exception -> findViewById(R.id.adBanner).setVisibility(View.GONE));
            }catch(Exception ignored){}
        }).start();
    }
    private void setHighlightedItem(){
        FirebaseFirestore.getInstance().collection("Products").whereEqualTo("importance",1)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for(QueryDocumentSnapshot doc : task.getResult()){
                    highlightedItemIds.add(doc.getId());
                    highlightedItemTitle.add(doc.get("name").toString());
                }
                setHighlightedItemView();
            }
        });
    }
    private void setImages(int index){
        new Thread(() -> {
            try {
                String name = (highlightedItemIds.get(index));
                String imgName = name + ".png";
                final File localFile = File.createTempFile(name, "png");
                StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("/Products/").child(imgName);
                mStorageRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                    highlightedItemImages.get(index).setBackground(new BitmapDrawable
                            (getResources(), BitmapFactory.decodeFile(localFile.getAbsolutePath())));

                });
            }catch(Exception ignored){}
        }).start();
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    private void setHighlightedItemView(){
        Display display = this.getWindowManager().getDefaultDisplay();
        int totalWidth = ((display.getWidth()) - 48);
        int imgWidth = totalWidth/2;
        for(int i=0; i<highlightedItemIds.size(); i+=2) {
            LinearLayout linearLayoutM = new LinearLayout(this);
            linearLayoutM.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(totalWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.leftMargin = 16;
            layoutParams.topMargin = 16;
            layoutParams.rightMargin = 16;
            linearLayoutM.setLayoutParams(layoutParams);
            /*..........*/
            linearLayoutM.addView(createItemView(i,imgWidth,highlightedItemTitle.get(i)));
            setImages(i);
            /*..........*/
            LinearLayout l2;
            if(!(highlightedItemIds.size()%2==0) && (highlightedItemIds.size()-1)==i) {
                l2 = createItemView(i+1,imgWidth,"");
                l2.setVisibility(View.INVISIBLE);
            }
            else {
                l2 = createItemView(i + 1, imgWidth, highlightedItemTitle.get(i + 1));
                setImages(i+1);
            }
            linearLayoutM.addView(l2);/*..........*/
            ((LinearLayout)findViewById(R.id.highlightItem)).addView(linearLayoutM);
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
        highlightedItemImages.add(image);
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
            if(linearLayout.getId()<highlightedItemIds.size()) {
                Intent intent = new Intent(this,itemDescription.class);
                intent.putExtra("productId",highlightedItemIds.get(linearLayout.getId()));
                startActivity(intent);
            }
        });
        return linearLayout;
    }
    private void redirectProductPage(){
        findViewById(R.id.title1Layout1).setOnClickListener(view->{
            Intent intent = new Intent(this,product.class);
            intent.putExtra("cateId",extra.CROPS);
            intent.putExtra("categoryTitle","CROPS");
            startActivity(intent);
        });
        findViewById(R.id.title1Layout2).setOnClickListener(view->{
            Intent intent = new Intent(this,product.class);
            intent.putExtra("cateId",extra.FERTILIZERS);
            intent.putExtra("categoryTitle","FERTILIZERS");
            startActivity(intent);
        });
        findViewById(R.id.title1Layout3).setOnClickListener(view->{
            Intent intent = new Intent(this,product.class);
            intent.putExtra("cateId",extra.PESTICIDES);
            intent.putExtra("categoryTitle","PESTICIDES");
            startActivity(intent);
        });
        findViewById(R.id.title1Layout4).setOnClickListener(view->{
            Intent intent = new Intent(this,product.class);
            intent.putExtra("cateId",extra.TOOLS);
            intent.putExtra("categoryTitle","TOOLS");
            startActivity(intent);
        });
        findViewById(R.id.title2Layout1).setOnClickListener(view->{
            Intent intent = new Intent(this,product.class);
            intent.putExtra("cateId",extra.FRUITS);
            intent.putExtra("categoryTitle","FRUITS");
            startActivity(intent);
        });
        findViewById(R.id.title2Layout2).setOnClickListener(view->{
            Intent intent = new Intent(this,product.class);
            intent.putExtra("cateId",extra.VEGETABLES);
            intent.putExtra("categoryTitle","VEGETABLES");
            startActivity(intent);
        });
        findViewById(R.id.title2Layout3).setOnClickListener(view->{
            Intent intent = new Intent(this,product.class);
            intent.putExtra("cateId",extra.GRAINS);
            intent.putExtra("categoryTitle","GRAINS");
            startActivity(intent);
        });

    }
}