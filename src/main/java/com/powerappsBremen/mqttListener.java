/**
 * A simple mqtt listener that reads mqtt messages from a broker
 * according to a configuration file.
 *  The messages accepted in json format.
*   It can receive messages from a list of topics / thus the configuration file contains a list of "message" objects
 *   Each message object specifies the Topic in the Broker and the parameters that the message contains.
 *  Author Anastasios Fakas 03.03.2022
 *  Version 1.0
 */
package com.powerappsBremen;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class mqttListener {


    /**
     * The main entry point of the sample.
     *
     * This method handles parsing of the arguments specified on the
     * command-line before performing the specified action.
     */
    public static void main(String[] args) throws IOException {

        Logger LOGGER = Logger.getLogger( "mqttListener" );
        // Create an instance of FileHandler that write log to a file called
        // app.log. Each new message will be appended at the at of the log file.
        FileHandler fileHandler = new FileHandler("mqttListener.log", true);
        LOGGER.addHandler(fileHandler);

        Reader readerBroker = null;
        try {
            readerBroker = Files.newBufferedReader(Paths.get(".\\ConfigurationBroker.json"));
        } catch (IOException e) {
            LOGGER.log( Level.SEVERE, "Can not read the capplication configuration file ConfigurationBroker.json",e);
        }

        JsonObject jobj = new Gson().fromJson(readerBroker, JsonObject.class);

        String protocol = jobj.get("protocol").toString();
        String broker = jobj.get("broker").toString();
        String port = jobj.get("port").toString();
        String password = jobj.get("password").toString();
        String userName = jobj.get("userName").toString();

          String   configString = "Read broker configuration " + "protocol: " + protocol + " broker :" + broker
                  + " port: " + port;

        LOGGER.info(configString);


        // Default settings:
        boolean quietMode 	= false;
        String action 		= "subscribe";
        String topic 		= "";
        String message 		= "Message from blocking Paho MQTTv3 Java client sample";
        int qos 			= 0;
        //String broker     = "localhost";
        //String broker 		= "lns.sw-hb.de";
        //int port 			= 3883;
        String clientId 	= null;
        String subTopic		= "smartboard/+";
        String pubTopic 	= "Sample/Java/v3";
        boolean cleanSession = true;			// Non durable subscriptions
        boolean ssl = false;


        String url = protocol + "://" + broker + ":" + port;
        url = url.replace("\"", "");
        LOGGER.info("URL to connect: " +  url);



        if (clientId == null || clientId.equals("")) {
            clientId = "";
        }

        // create Gson instance
        Gson gson = new Gson();

        // create a reader

        Reader reader = null;
        try {                                          
            reader = Files.newBufferedReader(Paths.get(".\\Configuration.json"));
        } catch (IOException e) {
            LOGGER.log( Level.SEVERE, "Can not read the capplication configuration file Configuration.json",e);
        }


        // convert JSON array to list of users
        List<Connection> connections = new Gson().fromJson(reader, new TypeToken<List<Connection>>() {}.getType());

        // print connections
        for(Connection connection : connections) {
            String text = connection.toString();
            LOGGER.info(text);
        }
        try {
        // for every connection create an instance of the sample client
        for(Connection connection : connections) {

                // Create an instance of this class
                Sample sampleClient = new Sample(url, clientId, cleanSession, quietMode,userName,password,connection);
                // Subscribe
            LOGGER.info("One more instance of listening client is about to be launched");
                    sampleClient.subscribe(connection.getTopic(),qos);
            LOGGER.info("One more instance of listening client has been launched");

        }  // end create of a Sample client instance.
        } catch(MqttException me) {
            // Display full details of any exception that occurs
            LOGGER.log( Level.SEVERE,"reason " + me.getReasonCode() + "msg "+ me.getMessage()
                     + "loc " + me.getLocalizedMessage() + "cause " +me.getCause() + "excep " , me );
        }

        // close reader
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
