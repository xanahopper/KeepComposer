package com.gotokeep.keep.composer.demo;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class LaunchActivity extends AppCompatActivity {
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new Adapter(getActivitiesList()));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 0);
        }
    }

    private void onItemClicked(ActivityInfo info) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(this, info.name));
        startActivity(intent);
    }

    private List<ActivityInfo> getActivitiesList() {
        List<ActivityInfo> list = new ArrayList<>();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_SAMPLE_CODE);

        String packageName = getApplicationInfo().packageName;
        List<ResolveInfo> resolveList = getPackageManager().queryIntentActivities(mainIntent, 0);
        if (resolveList == null) {
            return list;
        }

        for (ResolveInfo info : resolveList) {
            if (packageName.equals(info.activityInfo.packageName)) {
                list.add(info.activityInfo);
            }
        }

        return list;
    }

    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView name;
        ActivityInfo info;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemClicked(info);
        }
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private final List<ActivityInfo> list;

        private Adapter(List<ActivityInfo> list) {
            this.list = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_demo, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.info = list.get(position);

            holder.name.setText(holder.info.loadLabel(getPackageManager()));
            // Setting tinted example icon
            Context context = LaunchActivity.this;
            Drawable icon = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                icon = DrawableCompat.wrap(holder.info.loadUnbadgedIcon(getPackageManager()));
            } else {
                icon = DrawableCompat.wrap(holder.info.loadIcon(getPackageManager()));
            }
            int size = getResources().getDimensionPixelSize(R.dimen.icon_size);
            icon.setBounds(0, 0, size, size);
            DrawableCompat.setTint(icon, ContextCompat.getColor(context, R.color.colorPrimary));
            int padding = getResources().getDimensionPixelSize(R.dimen.example_icon_padding);
            holder.name.setCompoundDrawablePadding(padding);
            holder.name.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }
    }
}