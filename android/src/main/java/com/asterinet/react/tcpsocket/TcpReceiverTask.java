package com.asterinet.react.tcpsocket;

import android.os.AsyncTask;
import android.util.Pair;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.ByteArrayOutputStream;

/**
 * This is a specialized AsyncTask that receives data from a socket in the background, and
 * notifies it's listener when data is received.  This is not threadsafe, the listener
 * should handle synchronicity.
 */
class TcpReceiverTask extends AsyncTask<Pair<TcpSocketClient, TcpReceiverTask.OnDataReceivedListener>, Void, Void> {
    /**
     * An infinite loop to block and read data from the socket.
     */
    @SafeVarargs
    @Override
    protected final Void doInBackground(Pair<TcpSocketClient, TcpReceiverTask.OnDataReceivedListener>... params) {
        if (params.length > 1) {
            throw new IllegalArgumentException("This task is only for a single socket/listener pair.");
        }

        TcpSocketClient clientSocket = params[0].first;
        OnDataReceivedListener receiverListener = params[0].second;
        int socketId = clientSocket.getId();
        Socket socket = clientSocket.getSocket();
        byte[] buffer = new byte[8192];
        int bufferCount;
        int l_intByte = -1;

        try {
//            BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            while (!isCancelled() && !socket.isClosed()) {
                ByteArrayOutputStream byteArrayBuffer = new ByteArrayOutputStream();
                while ((l_intByte = in.read() )!= -1) {
                    if (l_intByte == 0) {
                        break;
                    }
                    byteArrayBuffer.write(l_intByte);
                }
                byte[] l_arryByte = byteArrayBuffer.toByteArray();

                if (l_arryByte.length > 0) {
                    //receiverListener.onData(socketId, Arrays.copyOfRange(buffer, 0, bufferCount));
                    receiverListener.onData(socketId, l_arryByte);
                } else {
                    clientSocket.close();
                }
//                bufferCount = in.read(buffer);
//                if (bufferCount > 0) {
//                    receiverListener.onData(socketId, Arrays.copyOfRange(buffer, 0, bufferCount));
//                } else if (bufferCount == -1) {
//                    clientSocket.close();
//                }
            }
        } catch (IOException ioe) {
            if (receiverListener != null && !socket.isClosed()) {
                receiverListener.onError(socketId, ioe.getMessage());
            }
            this.cancel(false);
        }
        return null;
    }

    /**
     * Listener interface for receive events.
     */
    @SuppressWarnings("WeakerAccess")
    public interface OnDataReceivedListener {
        void onConnection(Integer serverId, Integer clientId, InetSocketAddress socketAddress);

        void onConnect(Integer id, String host, int port);

        void onData(Integer id, byte[] data);

        void onClose(Integer id, String error);

        void onError(Integer id, String error);
    }
}