package org.ros.android.android_tutorial_teleop;

import com.google.common.collect.Lists;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import org.ros.address.InetAddressFactory;
import org.ros.android.RosActivity;
import org.ros.android.view.VirtualJoystickView;
import org.ros.android.view.visualization.VisualizationView;
import org.ros.android.view.visualization.layer.CameraControlLayer;
import org.ros.android.view.visualization.layer.LaserScanLayer;
import org.ros.android.view.visualization.layer.Layer;
import org.ros.android.view.visualization.layer.OccupancyGridLayer;
import org.ros.android.view.visualization.layer.PathLayer;
import org.ros.android.view.visualization.layer.PosePublisherLayer;
import org.ros.android.view.visualization.layer.PoseSubscriberLayer;
import org.ros.android.view.visualization.layer.RobotLayer;
import org.ros.message.Time;
import org.ros.node.NodeConfiguration;
import geometry_msgs.PoseStamped;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Publisher;
import org.ros.node.ConnectedNode;

public class Joystick extends RosActivity {

    private VirtualJoystickView virtualJoystickView;
    private VisualizationView visualizationView;
    private ImageButton backButton;

    public Joystick() {
        super("Teleop", "Teleop");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.virtual_joystick_snap:
                if (!item.isChecked()) {
                    item.setChecked(true);
                    virtualJoystickView.EnableSnapping();
                } else {
                    item.setChecked(false);
                    virtualJoystickView.DisableSnapping();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joystick);
        virtualJoystickView = (VirtualJoystickView) findViewById(R.id.virtual_joystick);
        visualizationView = (VisualizationView) findViewById(R.id.visualization);
        backButton = findViewById(R.id.back_button);
        visualizationView.getCamera().jumpToFrame("map");
        visualizationView.onCreate(Lists.<Layer>newArrayList(new CameraControlLayer(),
                new OccupancyGridLayer("map"), new PathLayer("move_base/NavfnROS/plan"), new PathLayer(
                        "move_base_dynamic/NavfnROS/plan"), new LaserScanLayer("base_scan"),
                new PoseSubscriberLayer("simple_waypoints_server/goal_pose"), new PosePublisherLayer(
                        "simple_waypoints_server/goal_pose"), new RobotLayer("base_footprint")));

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
        virtualJoystickView.setTopicName("cmd_vel");
        //ConnectedNode connectedNode;
        // publisher = connectedNode.newPublisher("goal", "geometry_msgs/PoseStamped");
        NodeConfiguration nodeConfiguration =
                NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress(),
                        getMasterUri());
        nodeMainExecutor
                .execute(virtualJoystickView, nodeConfiguration.setNodeName("virtual_joystick"));
        nodeMainExecutor.execute(visualizationView, nodeConfiguration.setNodeName("android/map_view"));

    }

}