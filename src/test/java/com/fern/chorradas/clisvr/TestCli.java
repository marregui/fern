package com.fern.chorradas.clisvr;

import com.fern.util.ILogger;
import com.fern.util.Logger;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static com.fern.chorradas.clisvr.IOHandler.EOM;

/**
 * Simple client, sends a simple predefined text message to the counter party on
 * a predefined port and receives a response
 */
public class TestCli implements AutoCloseable {
    private static final ILogger LOGGER = Logger.loggerFor(TestCli.class);
    private static final int IN_BUFF_SIZE = 200;
    private static final int OUT_BUFF_SIZE = 200;
    private static final int NUM_SEND = 5;

    private final Socket sck;
    private final BufferedOutputStream out;
    private final BufferedReader in;
    private final StringBuilder message;
    private final char[] inBuff;

    public TestCli(InetAddress host, int port) throws IOException {
        sck = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(sck.getInputStream(), StandardCharsets.UTF_8));
        out = new BufferedOutputStream(sck.getOutputStream(), OUT_BUFF_SIZE);
        message = new StringBuilder();
        inBuff = new char[IN_BUFF_SIZE];
    }

    private void sendMsg(String msg) throws IOException {
        LOGGER.info("sending: '%s'", msg);
        out.write(msg.getBytes());
        if (!msg.endsWith(EOM)) {
            LOGGER.info("needs EOM: '%c'", EOM.charAt(0));
            out.write(EOM.getBytes());
        }
        out.flush();
        LOGGER.info("sent : '%s'", msg);
    }

    private String receiveMsg() throws IOException {
        message.setLength(0);
        LOGGER.info("expecting message");
        for (int n; (n = in.read(inBuff)) > 0; ) {
            message.append(inBuff, 0, n);
            LOGGER.info("read %d bytes, msg: %s", n, message);
        }
        LOGGER.info("received: %s", message);
        return message.toString();
    }

    public static void main(String[] args) throws Exception {
        try (TestCli cli = new TestCli(InetAddress.getLocalHost(), TestSvr.SERVICE_PORT)) {
            for (int i = 0; i < NUM_SEND; i++) {
                cli.sendMsg("Hi, would you like a coffee?");
            }
            for (int i = 0; i < NUM_SEND; i++) {
                cli.receiveMsg();
            }
        } finally {
            LOGGER.info("Bye");
        }
    }

    @Override
    public void close() throws IOException {
        LOGGER.info("closing");
        out.close();
        LOGGER.info("out closed");
        in.close();
        LOGGER.info("in closed");
        sck.close();
        LOGGER.info("sck closed");
    }
}