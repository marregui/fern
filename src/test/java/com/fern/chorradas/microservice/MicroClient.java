package com.fern.chorradas.microservice;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public final class MicroClient {
    private static final Gson GSON = new Gson();

    private static String prepareCommand(String[] params, int offset) {
        StringBuilder sb = new StringBuilder(MicroserviceUtils.ROOTPATH);
        for (int i = offset; i < params.length; i++) {
            String param = params[i].replaceAll(" ", "//");
            System.out.println(param);
            sb.append(param).append(MicroserviceUtils.PATHSEP);
        }
        int sblen = sb.length();
        if (sblen > 0) {
            sb.setLength(sblen - MicroserviceUtils.PATHSEP.length());
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.printf("Syntax: java -cp.. %s host port command [parameters]\n", MicroClient.class.getName());
            System.out.println("       note: the param 'this//is//cool' becomes 'this is cool' ('//' is interpreted as blank)");
            System.exit(1);
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String command = prepareCommand(args, 2);
        try (Socket sck = new Socket(host, port);
             BufferedReader br = new BufferedReader(new InputStreamReader(sck.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(sck.getOutputStream(), StandardCharsets.UTF_8))) {

            // request
            bw.write(MicroserviceUtils.createRequestHeader(command));
            bw.flush();

            // response
            ResponseMessage<?> response = GSON.fromJson(HttpChunkedReader.slurp(br), ResponseMessage.class);
            System.out.println(response);
        } catch (Exception e) {
            System.out.printf("[%s:%d] %s => %s\n", host, port, command, e.getMessage());
            e.printStackTrace();
        }
    }

    private MicroClient() {
        throw new UnsupportedOperationException("no instances are allowed, it is a utilities class");
    }
}
