package com.gotokeep.keep.composer.demo.sample;

import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gotokeep.keep.composer.demo.R;
import com.gotokeep.keep.composer.demo.decode.DecodeThread;
import com.gotokeep.keep.composer.demo.decode.FrameInfo;
import com.gotokeep.keep.composer.demo.decode.RenderThread;
import com.gotokeep.keep.composer.demo.decode.TimeRange;
import com.gotokeep.keep.social.composer.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

public class DecodeSpeedThreadActivity extends AppCompatActivity implements Handler.Callback, TextureView
        .SurfaceTextureListener {

    private static final String TAG = "DecodeSpeedTest";

    private static final int LINE_COLORS[] = {
            Color.MAGENTA, Color.RED
    };
    private Handler handler;
    private DecodeThread decodeThreads[] = new DecodeThread[2];
    private AtomicInteger decodeCount = new AtomicInteger(0);
    private RenderThread renderThread;
    private List<FrameInfo> frameInfos = new ArrayList<>();
    private FrameInfoAdapter adapter;

    private TextureView previewView;
    private RecyclerView recyclerView;
    private SurfaceTexture surfaceTexture;
    private Runnable updateRunnable = new UpdateRunnable();
    private Object contextSyncObj = new Object();

    private LineChartView chart;
    private List<List<PointValue>> decodeTime = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode_speed_thread);
        handler = new Handler(this);

        previewView = findViewById(R.id.preview_view);
        previewView.setSurfaceTextureListener(this);

        adapter = new FrameInfoAdapter();
        adapter.frameInfos = frameInfos;
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        chart = findViewById(R.id.chart);
        chart.setInteractive(true);
        chart.setZoomType(ZoomType.HORIZONTAL);
        chart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        chart.setContainerScrollEnabled(false, ContainerScrollType.VERTICAL);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.decode_speed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_start:
                startDecode();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startDecode() {
        renderThread = new RenderThread(handler, surfaceTexture);
        for (int i = 0; i < decodeThreads.length; i++) {
            DecodeThread thread = decodeThreads[i];
            if (thread == null && surfaceTexture != null) {
                thread = new DecodeThread(i, handler);
                decodeThreads[i] = thread;
                thread.start();
                frameInfos.add(new FrameInfo());
                decodeTime.add(new ArrayList<>());
                decodeCount.getAndIncrement();
            }
        }
        adapter.notifyDataSetChanged();
        updateRunnable.run();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case DecodeThread.MSG_INIT_TIME:
                TimeRange initTime = (TimeRange) msg.obj;
                return true;
            case DecodeThread.MSG_DECODE_FRAME:
                FrameInfo frameInfo = (FrameInfo) msg.obj;
                int index = frameInfo.sourceIndex;
                DecodeThread decodeThread = decodeThreads[index];
                renderThread.renderDecodedTexture(decodeThread, frameInfo);
                return true;
            case RenderThread.MSG_RENDER_COMPLETE:
                frameInfo = (FrameInfo) msg.obj;
                updateInfo(frameInfo);
                return true;
            case DecodeThread.MSG_DECODE_COMPLETE:
                int count = decodeCount.decrementAndGet();
                if (count <= 0) {
                    recyclerView.removeCallbacks(updateRunnable);
                    LineChartData data = new LineChartData();
                    List<Line> lines = new ArrayList<>();
                    for (int i = 0; i < decodeThreads.length; i++) {
                        Line line = new Line(decodeTime.get(i)).setColor(LINE_COLORS[i]).setHasPoints(false);
                        lines.add(line);
                    }
                    data.setLines(lines);
                    data.setAxisXBottom(Axis.generateAxisFromRange(0, 5000, 100));
                    data.setAxisYLeft(Axis.generateAxisFromRange(0, 20000, 2000));
                    chart.setLineChartData(data);
                    try {
                        for (DecodeThread thread : decodeThreads) {
                            thread.join();
                        }
                    } catch (InterruptedException e) {
                        //
                    }
                    Log.d(TAG, "========= COMPLETE ========");
                }
                return true;
        }
        return false;
    }

    private void updateInfo(FrameInfo frameInfo) {
        int index = frameInfo.sourceIndex;
        frameInfos.set(index, frameInfo);
        decodeTime.get(index).add(new PointValue(frameInfo.frame, TimeUtil.nsToUs(frameInfo.decode.duration())));
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        this.surfaceTexture = surface;
        startDecode();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        recyclerView.removeCallbacks(updateRunnable);
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private static class FrameInfoViewHolder extends RecyclerView.ViewHolder {
        TextView sourceIndex;
        TextView presentationTime;
        ImageView imgDecoded;
        TextView inputTime;
        TextView readTime;
        TextView decodeTime;
        TextView renderTime;

        public FrameInfoViewHolder(View itemView) {
            super(itemView);
            sourceIndex = itemView.findViewById(R.id.source_index);
            presentationTime = itemView.findViewById(R.id.pts_text);
            imgDecoded = itemView.findViewById(R.id.img_decoded);
            inputTime = itemView.findViewById(R.id.input_text);
            readTime = itemView.findViewById(R.id.read_text);
            decodeTime = itemView.findViewById(R.id.decode_text);
            renderTime = itemView.findViewById(R.id.render_text);
        }
    }

    private static class FrameInfoAdapter extends RecyclerView.Adapter<FrameInfoViewHolder> {

        List<FrameInfo> frameInfos;

        @NonNull
        @Override
        public FrameInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new FrameInfoViewHolder(inflater.inflate(R.layout.item_frame_info, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull FrameInfoViewHolder holder, int position) {
            FrameInfo info = frameInfos.get(position);
            holder.sourceIndex.setText(String.valueOf(info.sourceIndex));
            holder.presentationTime.setText(TimeUtil.usToString(info.presentationTimeUs));
            holder.imgDecoded.setImageResource(info.decoded ? R.drawable.ic_check : R.drawable.ic_clear);
            holder.inputTime.setText(info.inputBuffer.toString());
            holder.readTime.setText(info.readSample.toString());
            holder.decodeTime.setText(info.decode.toString());
            holder.renderTime.setText(info.updateTexImage.toString());
        }

        @Override
        public int getItemCount() {
            return frameInfos != null ? frameInfos.size() : 0;
        }
    }

    private class UpdateRunnable implements Runnable {

        @Override
        public void run() {
            if (adapter != null) {
                adapter.notifyDataSetChanged();
                recyclerView.postOnAnimationDelayed(this, 1000);
            }
        }
    }
}
