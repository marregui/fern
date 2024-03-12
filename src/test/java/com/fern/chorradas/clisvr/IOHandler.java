package com.fern.chorradas.clisvr;

import com.fern.util.ILogger;
import com.fern.util.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

public class IOHandler {

    private static final ILogger LOGGER = Logger.loggerFor(IOHandler.class);
    public static final String EOM = "\0";
    private static final int IN_BUFF_SIZE = 200;
    private static final int OUT_BUFF_SIZE = 200;

    private final SocketChannel sck;
    private final ByteBuffer inBuff;
    private final ByteBuffer outBuff;
    private final CharsetEncoder encoder;
    private final CharsetDecoder decoder;

    public IOHandler(SocketChannel sck) {
        this.sck = sck;
        inBuff = ByteBuffer.allocate(IN_BUFF_SIZE);
        outBuff = ByteBuffer.allocate(OUT_BUFF_SIZE);
        encoder = StandardCharsets.UTF_8.newEncoder();
        decoder = StandardCharsets.UTF_8.newDecoder();
    }

    public SocketChannel getSocketChannel() {
        return sck;
    }

    public void receiveMsg(SelectionKey key) throws IOException {
        if (-1 == sck.read(inBuff) || EOM.charAt(0) == inBuff.get(inBuff.position() - 1)) {
            inBuff.flip();
            decoder.reset();
            String msg = decoder.decode(inBuff).toString();
            inBuff.clear();
            if (null != msg && msg.endsWith(EOM)) {
                msg = msg.substring(0, msg.length() - 1);
            }
            LOGGER.info("[%s] received: %s", sck.getRemoteAddress(), msg);
            sendMsg(key, msg);
        } else {
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    public void sendMsg(SelectionKey key, String msg) {
        if (null != msg && false == msg.endsWith(EOM)) {
            msg += EOM;
        }
        outBuff.clear();
        encoder.reset();
        encoder.encode(CharBuffer.wrap(msg), outBuff, true);
        outBuff.flip();
        key.interestOps(SelectionKey.OP_WRITE);
    }

    public void flushOutbound(SelectionKey key) throws IOException {
        if (outBuff.hasRemaining()) {
            int n = sck.write(outBuff);
            LOGGER.info("socket.write() invoked: %d", n);
            key.interestOps(outBuff.remaining() > 0 ? SelectionKey.OP_WRITE : SelectionKey.OP_READ);
        }
    }
}