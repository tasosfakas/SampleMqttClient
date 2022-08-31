/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *
 * The Eclipse Public License is available at 
 *    https://www.eclipse.org/legal/epl-2.0
 * and the Eclipse Distribution License is available at 
 *   https://www.eclipse.org/org/documents/edl-v10.php
 *
 * Contributors:
 *    Dave Locke - initial API and implementation and/or initial documentation
 *      *  Adapted for a simple mqttmiddleware: Anastasios Fakas 03.03.2022
 *      *   Description. An api for reading messages from an mqtt broker based on the Eclipse PACO library.
 *      *  Version 1.0
 */

package com.powerappsBremen;

import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


/**
 * A sample application that demonstrates how to use the Paho MQTT v3.1 Client blocking API.
 *
 * It can be run from the command line in one of two modes:
 *  - as a publisher, sending a single message to a topic on the server
 *  - as a subscriber, listening for messages from the server
 *
 *  There are three versions of the sample that implement the same features
 *  but do so using using different programming styles:
 *  <ol>
 *  <li>Sample (this one) which uses the API which blocks until the operation completes</li>
 *  <li>SampleAsyncWait shows how to use the asynchronous API with waiters that block until
 *  an action completes</li>
 *  <li>SampleAsyncCallBack shows how to use the asynchronous API where events are
 *  used to notify the application when an action completes<li>
 *  </ol>
 *
 *  If the application is run with the -h parameter then info is displayed that
 *  describes all of the options / parameters.
 */
public class Sample implements MqttCallback {


	// Private instance variables
	private Logger LOGGER = null;
	private MqttClient 			client;
	private String 				brokerUrl;
	private boolean 			quietMode;
	private MqttConnectOptions 	conOpt;
	private boolean 			clean;
	private String password;
	private String userName;
	private Connection configConnection;
	private FileHandler fileHandler = null;

	public Connection getConfigConnection() {
		return configConnection;
	}

	public void setConfigConnection(Connection configConnection) {
		this.configConnection = configConnection;
	}

	/**
	 * Constructs an instance of the sample client wrapper
	 * @param brokerUrl the url of the server to connect to
	 * @param clientId the client id to connect with
	 * @param cleanSession clear state at end of connection or not (durable or non-durable subscriptions)
	 * @param quietMode whether debug should be printed to standard out
   * @param userName the username to connect with
   * @param password the password for the user
	 * @throws MqttException
	 */
    public Sample(String brokerUrl, String clientId, boolean cleanSession, boolean quietMode, String userName, String password, Connection connection) throws MqttException {

		// Setup the log file  "listname".log, listname is the sharepoint list name this class connects to.....
		LOGGER = Logger.getLogger(connection.getList() );
		try {
			this.fileHandler = new FileHandler(connection.getList() + ".log");
		} catch (java.lang.Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		LOGGER.addHandler(this.fileHandler);
		// end setting up the logger

		this.brokerUrl = brokerUrl;
    	this.quietMode = quietMode;
    	this.clean 	   = cleanSession;
    	this.password = password;
    	this.userName = userName;
		this.configConnection = connection;
    	//This sample stores in a temporary directory... where messages temporarily
    	// stored until the message has been delivered to the server.
    	//..a real application ought to store them somewhere
    	// where they are not likely to get deleted or tampered with
    	String tmpDir = System.getProperty("java.io.tmpdir");
    	MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);

    	try {
    		// Construct the connection options object that contains connection parameters
    		// such as cleanSession and LWT
	    	conOpt = new MqttConnectOptions();
	    	conOpt.setCleanSession(clean);
	    	if(password != null ) {
	    	  conOpt.setPassword(this.password.toCharArray());
	    	}
	    	if(userName != null) {
	    	  conOpt.setUserName(this.userName);
	    	}

    		// Construct an MQTT blocking mode client

			client = new MqttClient(this.brokerUrl,MqttClient.generateClientId(), dataStore);


	    	client.setCallback(this);
			client.generateClientId();

		} catch (Exception e) {

			LOGGER.log(Level.SEVERE, "Unable to set up an MQTT client: for list : " + connection.getList() + "  " + e.toString() );

			System.exit(1);
		}
    }

    /**
     * Publish / send a message to an MQTT server
     * @param topicName the name of the topic to publish to
     * @param qos the quality of service to delivery the message at (0,1,2)
     * @param payload the set of bytes to send to the MQTT server
     * @throws MqttException
     */
    public void publish(String topicName, int qos, byte[] payload) throws MqttException , java.io.IOException {

    	// Connect to the MQTT server
		LOGGER.log(Level.INFO, "Connecting to "+brokerUrl + " with client ID "+client.getClientId());
    	client.connect(conOpt);
		LOGGER.log(Level.INFO, "Connected");

    	String time = new Timestamp(System.currentTimeMillis()).toString();
		LOGGER.log(Level.INFO, "Publishing at: "+time+ " to topic \""+topicName+"\" qos "+qos);

    	// Create and configure a message
   		MqttMessage message = new MqttMessage(payload);
    	message.setQos(qos);

    	// Send the message to the server, control is not returned until
    	// it has been delivered to the server meeting the specified
    	// quality of service.
    	client.publish(topicName, message);

    	// Disconnect the client
    	client.disconnect();
		LOGGER.log(Level.INFO, "Disconnected");
    }

    /**
     * Subscribe to a topic on an MQTT server
     * Once subscribed this method waits for the messages to arrive from the server
     * that match the subscription. It continues listening for messages until the enter key is
     * pressed.
     * @param topicName to subscribe to (can be wild carded)
     * @param qos the maximum quality of service to receive messages at for this subscription
     * @throws MqttException
     */
    public void subscribe(String topicName, int qos) throws MqttException, java.io.IOException {

    	// Connect to the MQTT server
    	client.connect(conOpt);
		LOGGER.log(Level.INFO, "Connected to "+brokerUrl+" with client ID "+client.getClientId());

    	// Subscribe to the requested topic
    	// The QoS specified is the maximum level that messages will be sent to the client at.
    	// For instance if QoS 1 is specified, any messages originally published at QoS 2 will
    	// be downgraded to 1 when delivering to the client but messages published at 1 and 0
    	// will be received at the same level they were published at.
		LOGGER.log(Level.INFO, "Subscribing to topic:  " + topicName + " qos " + qos);
    	client.subscribe(topicName, qos);

    }

    /**
     * Utility method to handle logging. If 'quietMode' is set, this method does nothing
     * @param message the message to log
     */
   /* private void log(String message) {
    	if (!quietMode) {
			writer.append(message);
    	}
    } */

	/****************************************************************/
	/* Methods to implement the MqttCallback interface              */
	/****************************************************************/

    /**
     * @see MqttCallback#connectionLost(Throwable)
     */
	public void connectionLost(Throwable cause) {
		// Called when the connection to the server has been lost.
		// An application may choose to implement reconnection
		// logic at this point. This sample simply exits.
		LOGGER.log(Level.SEVERE, "Connection to " + brokerUrl + " lost!" + cause);
		System.exit(1);
	}

    /**
     * @see MqttCallback#deliveryComplete(IMqttDeliveryToken)
     */
	public void deliveryComplete(IMqttDeliveryToken token) {
		// Called when a message has been delivered to the
		// server. The token passed in here is the same one
		// that was passed to or returned from the original call to publish.
		// This allows applications to perform asynchronous
		// delivery without blocking until delivery completes.
		//
		// This sample demonstrates asynchronous deliver and
		// uses the token.waitForCompletion() call in the main thread which
		// blocks until the delivery has completed.
		// Additionally the deliveryComplete method will be called if
		// the callback is set on the client
		//
		// If the connection to the server breaks before delivery has completed
		// delivery of a message will complete after the client has re-connected.
		// The getPendingTokens method will provide tokens for any messages
		// that are still to be delivered.
	}

    /**
     * @see MqttCallback#messageArrived(String, MqttMessage)
     */
	public void messageArrived(String topic, MqttMessage message) throws MqttException, IOException {

		// Called when a message arrives from the server that matches any
		// subscription made by the client


		JsonParser parser = new JsonParser();
		JsonElement jsonTree = parser.parse(new String(message.getPayload()));

		String time = new Timestamp(System.currentTimeMillis()).toString();

		JsonObject jsonObject = jsonTree.getAsJsonObject();

		// The parameters we expect are
		List<String> parameters = configConnection.getParameters();


		LOGGER.log(Level.INFO, " MEssage arrived : Time:\t" + time +
				"  Topic:\t" + configConnection.getTopic() +
				"  Message:\t" +  jsonObject.toString() +
				"  QoS:\t" + message.getQos());

		String values = "";
		String current_parameter[] = {};
		JsonObject internalObject = null;

		try{

			// Create the values parameter (parameters/values set)
			for(String parameter : parameters) {

				if(parameter.contains("-")) {
					// in case the parameter we are searching for, is inside a json object in the json payload
					current_parameter = parameter.split("-");
				//this.writer.append( "About to read parameter: " +  Arrays.toString(current_parameter) );
				//	internalObject = jsonObject.getAsJsonObject(current_parameter[0]);

				//this.writer.append(  "About to read parameter: " +  current_parameter[1]  +
				//			"  from json object " + current_parameter[0] + "  from json object PRINTED :" + internalObject.toString() +
				//			"   and the PARAMETER IS: " + internalObject.get(current_parameter[1]).toString() );
					values = values + "#" + current_parameter[1] + "=" + internalObject.get(current_parameter[1]).toString();
				} else {
				//this.writer.append( "About to read parameter: " + parameter);
					values = values + "#" + parameter + "=" + jsonObject.get(parameter).toString();
				}
			}

		}catch (NullPointerException e){

			LOGGER.log(Level.SEVERE,  "Message with wrong parameters is insterted to the Topic: " +  configConnection.getTopic()  +
					"  Wrong parameter: " + current_parameter + e.toString() );

			return;

		}

		values = values.substring(1);
		//this.writer.append("Values to be inserted are: " + values);

		String command = "C:\\Windows\\syswow64\\WindowsPowerShell\\v1.0\\powershell.exe & \".\\SmartBoard\" -List " + configConnection.getList() + " -Values " + values
				+ " -Url " + configConnection.getURL();


		//this.writer.append("The ps command is : " +  command);

		Process powerShellProcess = Runtime.getRuntime().exec(command);

		String line;
		powerShellProcess.getOutputStream().close();

		BufferedReader stdout = new BufferedReader(new InputStreamReader(powerShellProcess.getInputStream()));
		while ((line = stdout.readLine()) != null) {
			LOGGER.log(Level.INFO, "Output: " + line);

		}

		stdout.close();

		BufferedReader stderr = new BufferedReader(new InputStreamReader(powerShellProcess.getErrorStream()));
		while ((line = stderr.readLine()) != null) {
			LOGGER.log(Level.INFO, "Output: " + line);
		}
		stderr.close();


	}


	/****************************************************************/
	/* End of MqttCallback methods                                  */
	/****************************************************************/




}
