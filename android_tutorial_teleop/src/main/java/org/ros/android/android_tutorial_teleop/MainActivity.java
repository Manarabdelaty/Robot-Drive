
package org.ros.android.android_tutorial_teleop;

import com.google.common.collect.Lists;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import org.ros.address.InetAddressFactory;
import org.ros.android.AppCompatRosActivity;
import org.ros.android.RosActivity;


import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;
import org.ros.node.NodeMainExecutor;

public class MainActivity extends AppCompatActivity {

  BluetoothSPP bluetooth;

  Button connect, brake, right, left, forward, backward, navigate, viewMap, setBtn;
  String input;
  SeekBar simpleSeekBar;
  int speed=10;


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.settings_menu, menu);
    return true;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    bluetooth = new BluetoothSPP(this);

    connect = (Button) findViewById(R.id.button_connect);
    brake = (Button) findViewById(R.id.button_brake);
    right = (Button) findViewById(R.id.button_right);
    left = (Button) findViewById(R.id.button_left);
    forward = (Button) findViewById(R.id.button_forward);
    backward = (Button) findViewById(R.id.button_backward);
    navigate = findViewById(R.id.button_navigate);
    simpleSeekBar=(SeekBar)findViewById(R.id.simpleSeekBar);
    viewMap = findViewById(R.id.button_map);
    setBtn = findViewById(R.id.button_settings);

    if (!bluetooth.isBluetoothAvailable()) {
      Toast.makeText(getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_SHORT).show();
      finish();
    }

    bluetooth.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
      public void onDeviceConnected(String name, String address) {
        Toast.makeText(getApplicationContext(), "Connected to " + name, Toast.LENGTH_SHORT).show();
        connect.setBackgroundResource(R.drawable.blueetooth_btn_cnt);
      }

      public void onDeviceDisconnected() {
        //connect.setText("Connection lost");
        Toast.makeText(getApplicationContext(), "Connection lost", Toast.LENGTH_SHORT).show();
        connect.setBackgroundResource(R.drawable.blueetooth_btn);
      }

      public void onDeviceConnectionFailed() {
        //connect.setText("Unable to connect");
        Toast.makeText(getApplicationContext(), "Unable to connect", Toast.LENGTH_SHORT).show();
        connect.setBackgroundResource(R.drawable.blueetooth_btn);

      }
    });

    connect.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Log.d("BB", "Bluetooth clicked");
        if (bluetooth.getServiceState() == BluetoothState.STATE_CONNECTED) {
          bluetooth.disconnect();
        } else {
          Intent intent = new Intent(getApplicationContext(), DeviceList.class);
          startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        }
      }
    });

    simpleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      int progressChangedValue = 0;

      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        progressChangedValue = progress;
      }

      public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
      }

      public void onStopTrackingTouch(SeekBar seekBar) {
        if (progressChangedValue < 66) {
          speed = 10;
        }
        else
        if(progressChangedValue > 66 && progressChangedValue < 130)
        {
          speed = 20;
        }
        else
        {
          speed =30;
        }

        Toast.makeText(MainActivity.this, "Seek bar progress is :" + progressChangedValue,
                Toast.LENGTH_SHORT).show();
      }
    });

    brake.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        input = "X:"+String.valueOf(0) +"Y:"+ String.valueOf(0)+";";
        bluetooth.send(input, true);
        Log.v("Gazar", input);
        brake.setBackgroundResource(R.drawable.brake);
        if (!backward.isEnabled())
        {
          backward.setEnabled(true);
          backward.setBackgroundResource(R.drawable.arrow_pointing_down);
        }
        if (!forward.isEnabled())
        {
          forward.setEnabled(true);
          forward.setBackgroundResource(R.drawable.arrow_pointing_up);
        }
      }
    });

    right.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        input = "X:"+String.valueOf(speed) +"Y:-"+ String.valueOf(speed)+";";
        bluetooth.send(input, true);
        Log.v("Gazar", input);
        brake.setBackgroundResource(R.drawable.notbrake);
      }
    });

    left.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        input = "X:-"+String.valueOf(speed) +"Y:"+ String.valueOf(speed)+";";
        bluetooth.send(input, true);
        Log.v("Gazar", input);
        brake.setBackgroundResource(R.drawable.notbrake);

      }
    });

    forward.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (forward.isEnabled())
        {
          input = "X:"+String.valueOf(speed) +"Y:"+ String.valueOf(speed)+";";
          bluetooth.send(input, true);
          Log.v("Gazar", input);
          backward.setBackgroundResource(R.drawable.arrow_pointing_down_disable);
          backward.setEnabled(false);
          brake.setBackgroundResource(R.drawable.notbrake);

        }
        else{

          Toast.makeText(getApplicationContext(), "Must brake first", Toast.LENGTH_SHORT).show();
        }


      }
    });

    backward.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (backward.isEnabled())
        {
          input = "X:"+String.valueOf(speed) +"Y:"+ String.valueOf(speed)+";";
          bluetooth.send(input, true);
          Log.v("Gazar", input);
          forward.setBackgroundResource(R.drawable.arrow_pointing_up_disable);
          forward.setEnabled(false);
          brake.setBackgroundResource(R.drawable.notbrake);

        }else {
          Toast.makeText(getApplicationContext(), "Must brake first", Toast.LENGTH_SHORT).show();
        }
      }
    });

    navigate.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent i = new Intent(getApplicationContext(), map_nav.class);
        Log.d("I", "Called Intent");
        startActivity(i);
        Log.d("I", "Started Activity");
      }
    });

    viewMap.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent i = new Intent(getApplicationContext(), MapViewer.class);
        startActivity(i);
      }
    });
    setBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent i = new Intent(getApplicationContext(), Joystick.class);
        startActivity(i);
      }
    });
  }


//  @Override
//  protected void init(NodeMainExecutor nodeMainExecutor) {
////    visualizationView.init(nodeMainExecutor);
////    virtualJoystickView.setTopicName("cmd_vel");
////    //ConnectedNode connectedNode;
////    // publisher = connectedNode.newPublisher("goal", "geometry_msgs/PoseStamped");
////    NodeConfiguration nodeConfiguration =
////            NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress(),
////                    getMasterUri());
////    nodeMainExecutor
////            .execute(virtualJoystickView, nodeConfiguration.setNodeName("virtual_joystick"));
////    nodeMainExecutor.execute(visualizationView, nodeConfiguration.setNodeName("android/map_view"));
//
//  }

  public void onStart() {
    super.onStart();
    if (!bluetooth.isBluetoothEnabled()) {
      bluetooth.enable();
    } else {
      if (!bluetooth.isServiceAvailable()) {
        bluetooth.setupService();
        bluetooth.startService(BluetoothState.DEVICE_OTHER);
      }
    }
  }


  public void onDestroy() {
    super.onDestroy();
    bluetooth.stopService();
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
      if (resultCode == Activity.RESULT_OK)
        bluetooth.connect(data);
    } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
      if (resultCode == Activity.RESULT_OK) {
        bluetooth.setupService();
      } else {
        Toast.makeText(getApplicationContext()
                , "Bluetooth was not enabled."
                , Toast.LENGTH_SHORT).show();
        finish();
      }
    }
  }

}
