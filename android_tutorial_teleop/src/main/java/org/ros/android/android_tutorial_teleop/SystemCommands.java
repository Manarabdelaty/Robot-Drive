
package org.ros.android.android_tutorial_teleop;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.topic.Publisher;


public class SystemCommands extends AbstractNodeMain {

    private Publisher<std_msgs.String> publisher;

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("system_commands");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        publisher = connectedNode.newPublisher("syscommand", std_msgs.String._TYPE);
    }

    public void reset() {
        publish("reset");
    }

    public void saveGeotiff() {
        publish("savegeotiff");
    }

    private void publish(String command) {
        if (publisher != null) {
            std_msgs.String message = publisher.newMessage();
            message.setData(command);
            publisher.publish(message);
        }
    }

    @Override
    public void onShutdown(Node arg0) {
        publisher = null;
    }
}