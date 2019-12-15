package org.ros.android.android_tutorial_teleop;


import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.ros.address.InetAddressFactory;
import org.ros.android.RosActivity;
import org.ros.android.view.visualization.VisualizationView;
import org.ros.android.view.visualization.layer.CameraControlLayer;
import org.ros.android.view.visualization.layer.CameraControlListener;
import org.ros.android.view.visualization.layer.LaserScanLayer;
import org.ros.android.view.visualization.layer.Layer;
import org.ros.android.view.visualization.layer.OccupancyGridLayer;
import org.ros.android.view.visualization.layer.RobotLayer;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import org.ros.time.NtpTimeProvider;

import java.util.concurrent.TimeUnit;

public class MapViewer extends RosActivity {

    private static final String MAP_FRAME = "map";
    private static final String ROBOT_FRAME = "base_link";

    private final SystemCommands systemCommands;

    private VisualizationView visualizationView;
    private CameraControlLayer cameraControlLayer;
    private ImageButton backButton;

    public MapViewer() {
        super("Map Viewer", "Map Viewer");
        systemCommands = new SystemCommands();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_map_viewer);
        visualizationView = findViewById(R.id.visualization);
        backButton = findViewById(R.id.back_button);
        cameraControlLayer = new CameraControlLayer();
        visualizationView.onCreate(Lists.<Layer>newArrayList(cameraControlLayer,
                new OccupancyGridLayer("map"), new LaserScanLayer("scan"), new RobotLayer(ROBOT_FRAME)));
        enableFollowMe();
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        visualizationView.init(nodeMainExecutor);
        cameraControlLayer.addListener(new CameraControlListener() {
            @Override
            public void onZoom(float focusX, float focusY, float factor) {
                disableFollowMe();
            }

            @Override
            public void onTranslate(float distanceX, float distanceY) {
                disableFollowMe();
            }

            @Override
            public void onRotate(float focusX, float focusY, double deltaAngle) {
                disableFollowMe();
            }

            @Override
            public void onDoubleTap(float x, float y) {
            }
        });
        NodeConfiguration nodeConfiguration =
                NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress(),
                        getMasterUri());
        NtpTimeProvider ntpTimeProvider =
                new NtpTimeProvider(InetAddressFactory.newFromHostString("192.168.0.1"),
                        nodeMainExecutor.getScheduledExecutorService());
        ntpTimeProvider.startPeriodicUpdates(1, TimeUnit.MINUTES);
        nodeConfiguration.setTimeProvider(ntpTimeProvider);
        nodeMainExecutor.execute(visualizationView, nodeConfiguration);
        nodeMainExecutor.execute(systemCommands, nodeConfiguration);
    }

    public void onClearMapButtonClicked(View view) {
        toast("Clearing map...");
        systemCommands.reset();
        enableFollowMe();
    }

    public void onSaveMapButtonClicked(View view) {
        toast("Saving map...");
        systemCommands.saveGeotiff();
    }

    private void toast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(MapViewer.this, text, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    public void onFollowMeToggleButtonClicked(View view) {
        boolean on = ((ToggleButton) view).isChecked();
        if (on) {
            enableFollowMe();
        } else {
            disableFollowMe();
        }
    }

    private void enableFollowMe() {
        Preconditions.checkNotNull(visualizationView);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                visualizationView.getCamera().jumpToFrame(ROBOT_FRAME);
            }
        });
    }

    private void disableFollowMe() {
        Preconditions.checkNotNull(visualizationView);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                visualizationView.getCamera().setFrame(MAP_FRAME);
            }
        });
    }
}
