package com.example.calebmacdonaldblack.myapplication;

import android.util.Log;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import static com.example.calebmacdonaldblack.myapplication.MainActivity.clearScreen;
import static com.example.calebmacdonaldblack.myapplication.MainActivity.loaded;


/**
 * Created by calebmacdonaldblack on 8/09/15.
 */
public class RunClient implements Runnable {

    private final MainActivity context;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private Socket connection;
    private boolean running = true;
    public static boolean connectionStatus = false;

    public RunClient(MainActivity mainActivity) {
        context = mainActivity;
    }


    @Override
    public void run() {

        startRunning();
    }

    private void startRunning() {
        //repeatedly attempt a connection
        while (true) try {
            connectToServer();
            setupStreams();
            whileReceiving();
        } catch (EOFException eofe) {

            onDisconnect();
            Log.i("RunClient", "connection Terminated");

        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
    }

    private void onDisconnect() {
        connectionStatus = false;
        MainActivity.loaded = false;
    }

    private void whileReceiving() throws IOException {
        //retrieve input from server. Error will occur if object received is of unknown type
        //repeat until socket closes
        int[] eventArray = null;
        Object obj = null;
        do {
            try {
                obj = input.readObject();
                eventArray = (int[]) obj;
                MainActivity.clientID = eventArray[5];
                if (!(eventArray[0] == -1))
                    if (!(eventArray[4] == MainActivity.clientID) || loaded == false)
                        MainActivity.drawView.executeTouchEvent(eventArray);
                    else
                        System.out.println("Failed second condition");
                else
                    System.out.println("Failed first condition");
            } catch (ClassNotFoundException classNotFoundException) {
                Log.i("RunClient", "Server did not send an event");
            } catch (ClassCastException classCastException) {
                String command = (String) obj;
                initiateCommand(command);
            } catch (NullPointerException ignored) {

            }

        } while (running);
    }

    private void initiateCommand(String command) {
        //when object from client is a string the client is issuing a command
        if (command.equals("clearScreen")) {
            clearScreen();
        } else if (command.equals("eventsLoaded")) {
            MainActivity.progressDialog.dismiss();
            MainActivity.loaded = true;
        }

    }

    private void setupStreams() throws IOException {
        //initiate connection with server
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input = new ObjectInputStream(connection.getInputStream());
        //get client ID sending. sending this will return clientID from server
        int[] intObj = {-1, 0, 0, 0, 0, 0};
        output.writeObject(intObj);
        Log.i("RunClient", "streams are connected");
        connectionStatus = true;

        //wait for view to finish loading                   try to remove this
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //inform that connection was established, this clears the canvas
        clearScreen();

        MainActivity.progressDialog.dismiss();
        MainActivity.openProgressDialog("Loading", "Drawing onto screen, please wait");
    }

    private void connectToServer() throws IOException {
        //attempt connection this will loop repeatedly
        try {

            connectToAddress();
            Log.i("RunClient", "connected to " + connection.getInetAddress().getHostName());
        } catch (IOException e) {
            onDisconnect();
        }
    }

    private void connectToAddress() throws IOException {
        if (MainActivity.isHostName)
            connection = new Socket(InetAddress.getByName(MainActivity.hostName), 9090);
        else
            connection = new Socket(MainActivity.hostName, 9090);


    }

    public void sendObjectToServer(Object f) {
        //send objects to server
        try {
            output.writeObject(f);
        } catch (NullPointerException e) {

        } catch (IOException e) {
            e.printStackTrace();
            MainActivity.clientID = 0;
        }
    }

    public void stopRunning() {
        running = false;
    }
}
