package org.ros.android.android_tutorial_teleop;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.github.rosjava.android_remocons.common_tools.apps.RosAppActivity;
import com.google.common.collect.Lists;

import org.ros.address.InetAddressFactory;

import org.ros.android.view.visualization.VisualizationView;
import org.ros.android.view.visualization.layer.CameraControlLayer;
import org.ros.android.view.visualization.layer.LaserScanLayer;
import org.ros.android.view.visualization.layer.Layer;
import org.ros.android.view.visualization.layer.OccupancyGridLayer;
import org.ros.android.view.visualization.layer.PathLayer;
import org.ros.namespace.NameResolver;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import org.ros.time.NtpTimeProvider;
import org.ros.time.TimeProvider;
import org.ros.time.WallTimeProvider;


import java.util.concurrent.TimeUnit;


public class map_nav extends RosAppActivity {

    private static final String MAP_FRAME = "map";
    private static final String ROBOT_FRAME = "base_link";
    private static final String cameraTopic = "camera/rgb/image_color/compressed_throttle";
    private static final String virtualJoystickTopic = "android/virtual_joystick/cmd_vel";
    private static final String mapTopic = "map";
    private static final String scanTopic = "scan";
    private static final String pathLayerTopic = "move_base/TrajectoryPlannerROS/global_plan";
    private static final String initialPoseTopic = "initialpose";

    private VisualizationView mapView;

    private ImageButton backButton;
    private MapPosePublisherLayer mapPosePublisherLayer;

    private NodeConfiguration nodeConfiguration;


    public map_nav() {
        super("Map nav", "Map nav");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("map_nav", "Called Oncreat");
        String defaultRobotName = getString(R.string.default_robot);
        String defaultAppName = getString(R.string.default_app);
        setDefaultMasterName(defaultRobotName);
        setDefaultAppName(defaultAppName);
        setDashboardResource(R.id.top_bar);
        setMainWindowResource(R.layout.map_nav);
        super.onCreate(savedInstanceState);
        Log.d("map_nav", "Called Oncreat");

        backButton = (ImageButton) findViewById(R.id.back_button);
        mapView =  (VisualizationView) findViewById(R.id.map_view);
        mapView.onCreate(Lists.<Layer>newArrayList());

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mapView.getCamera().jumpToFrame((String) params.get("map_frame", getString(R.string.map_frame)));

        Log.d("map_nav", "Called Oncreat");

    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {

        super.init(nodeMainExecutor);
        Log.d("map_nav", "Called Init");

        //this.nodeMainExecutor = nodeMainExecutor;
        nodeConfiguration = NodeConfiguration.newPublic(InetAddressFactory
                .newNonLoopback().getHostAddress(), getMasterUri());

        NameResolver appNameSpace = getMasterNameSpace();

        String mapTopic      = remaps.get(getString(R.string.map_topic));
        String scanTopic     = remaps.get(getString(R.string.scan_topic));
        String planTopic     = remaps.get(getString(R.string.global_plan_topic));
        String initTopic     = remaps.get(getString(R.string.initial_pose_topic));
        String robotFrame    = (String) params.get("robot_frame", getString(R.string.robot_frame));

        OccupancyGridLayer mapLayer = new OccupancyGridLayer(appNameSpace.resolve(mapTopic).toString());
        LaserScanLayer laserScanLayer = new LaserScanLayer(appNameSpace.resolve(scanTopic).toString());
        PathLayer pathLayer = new PathLayer(appNameSpace.resolve(planTopic).toString());
        mapPosePublisherLayer = new MapPosePublisherLayer(this, appNameSpace, params, remaps);
        InitialPoseSubscriberLayer initialPoseSubscriberLayer =
                new InitialPoseSubscriberLayer(appNameSpace.resolve(initTopic).toString(), robotFrame);


        mapView.addLayer(new CameraControlLayer());
        mapView.addLayer(mapLayer);
        mapView.addLayer(laserScanLayer);
        mapView.addLayer(pathLayer);
        mapView.addLayer(mapPosePublisherLayer);
        mapView.addLayer(initialPoseSubscriberLayer);
        Log.d("map_nav", "Called Init");

        mapView.init(nodeMainExecutor);
        Log.d("map_nav", "Called Init");

        TimeProvider timeProvider = null;
        try {
            NtpTimeProvider ntpTimeProvider = new NtpTimeProvider(
                    InetAddressFactory.newFromHostString("pool.ntp.org"),
                    nodeMainExecutor.getScheduledExecutorService());
            ntpTimeProvider.startPeriodicUpdates(1, TimeUnit.MINUTES);
            timeProvider = ntpTimeProvider;
        } catch (Throwable t) {

            timeProvider = new WallTimeProvider();
        }
        Log.d("map_nav", "Called Time Provided");
        nodeConfiguration.setTimeProvider(timeProvider);
        Log.d("map_nav", "Passed Time Provided");

        nodeMainExecutor.execute(mapView, nodeConfiguration.setNodeName("android/map_view"));
        Log.d("map_nav", "Called Init");

    }

    public void setPoseClicked(View view) {
        setPose();
    }

    public void setGoalClicked(View view) {
        setGoal();
    }

    private void setPose() {
        mapPosePublisherLayer.setPoseMode();
    }

    private void setGoal() {
        mapPosePublisherLayer.setGoalMode();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, R.string.stop_app);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case 0:
               onDestroy();
                break;
        }
        return true;
    }
}

