package com.eyetyping.eyetyping2.eyetracker;

import com.eyetyping.eyetyping2.services.MouseService;
import com.eyetyping.eyetyping2.utils.Position2D;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

@Data
public class Connections {

    private boolean isRunning = false;
    private MouseService mouseService = MouseService.getSingleton();

    public void connect(String host, int port){
        try {
            Socket socket = new Socket(host, port);
            String REQ_CONNECT = "{\"values\":{\"push\":true,\"version\":1},\"category\":\"tracker\",\"request\":\"set\"}";
            socket.getOutputStream().write(REQ_CONNECT.getBytes());
            socket.getOutputStream().flush();

            new Thread(() -> listenerLoop(socket)).start();

            String REQ_HEATBEAT = "{\"category\":\"heartbeat\",\"request\":null}";
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        if(isRunning){
                            socket.getOutputStream().write(REQ_HEATBEAT.getBytes());
                            socket.getOutputStream().flush();
                        }
                        else
                            timer.cancel();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 250);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenerLoop(Socket socket){
        try(socket) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            isRunning = true;
            while (isRunning){
                String response = reader.readLine();
                if (response.length() > 200){
                    ObjectMapper mapper = new ObjectMapper();
                    Packet packet = mapper.readValue(response, Packet.class);
                    mouseService.updateList(new Position2D(packet));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        Connections connections = new Connections();
        connections.connect("localhost", 3000);


    }
}