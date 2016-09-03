package com.karl.draggridview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<String> list;
    private DragGridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
    }
    public void initData(){
        list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add("Drag #"+i);
        }
    }
    private void initView(){
        gridView = (DragGridView) findViewById(R.id.drag_grid_view);
        GridViewAdapter adapter = new GridViewAdapter(this,list);
        gridView.setAdapter(adapter);
        gridView.setExpanded(true);
        gridView.setFocusable(false);
    }
}
