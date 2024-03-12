package com.fern.chorradas.clisvr;

import com.fern.util.ILogger;
import com.fern.util.Logger;
import org.eclipse.jetty.util.IO;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simple NIO based server side, expects connections on a predefined port, then
 * reads a simple text based message from each and bounces some message back to
 * the client. The client end is expected to end the connection.
 */
public class TestSvr implements Closeable {
    private static final ILogger LOGGER = Logger.loggerFor(TestSvr.class);
    public static final int SERVICE_PORT = 9999;

    private final Selector clientSelector;
    private final ServerSocketChannel ssc;
    private final ExecutorService workers;
    private final AtomicBoolean isServing;

    private TestSvr() throws Exception {
        ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.socket().bind(new InetSocketAddress(InetAddress.getLocalHost(), SERVICE_PORT));
        ssc.register(clientSelector = Selector.open(), SelectionKey.OP_ACCEPT);
        workers = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), runnable -> {
            Thread tr = new Thread(runnable);
            tr.setDaemon(true);
            return tr;
        });
        isServing = new AtomicBoolean(false);
        LOGGER.info("server bound to port: %d", SERVICE_PORT);
    }

    public void serve() throws Exception {
        if (!isServing.compareAndSet(false, true)) {
            throw new RuntimeException("already running");
        }

        LOGGER.info("server is running");
        while (isServing.get()) {
            while (0L == clientSelector.select(100L)) {
                // await for some activity
            }

            LOGGER.info("detected some activity on the wire");
            Set<SelectionKey> selectionKeys = clientSelector.selectedKeys();
            // There are four operations: OP_ACCEPT, OP_CONNECT, OP_READ, and OP_WRITE.
            // The values can be bitwise ORed | together if you are interested in
            // multiple operations. interestOps(0) clears the interest set, setting
            // none of the bits.
            for (Iterator<SelectionKey> it = selectionKeys.iterator(); it.hasNext(); ) {
                SelectionKey key = it.next();
                selectionKeys.remove(key);
                if (key.isAcceptable()) {
                    SocketChannel clientSck = ssc.accept();
                    clientSck.configureBlocking(false);
                    clientSck.register(clientSelector, SelectionKey.OP_READ).attach(new IOHandler(clientSck));
                    LOGGER.info("client connection: %s", clientSck.getRemoteAddress());
                } else if (key.isValid()) {
                    key.interestOps(0);
                    workers.execute(() -> handleClient(key));
                } else {
                    key.cancel();
                }
            }
        }
    }

    private void handleClient(SelectionKey key) {
        IOHandler handler = (IOHandler) key.attachment();
        try {
            if (key.isReadable()) {
                handler.receiveMsg(key);
            } else if (key.isWritable()) {
                handler.flushOutbound(key);
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                handler.getSocketChannel().close();
            } catch (IOException ignore) {
                /* no-op */
            } finally {
                key.cancel();
            }
        } finally {
            clientSelector.wakeup();
        }
    }

    @Override
    public void close() {
        isServing.set(false);
        workers.shutdown();
        try {
            workers.awaitTermination(200L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }

    public static void main(String[] args) throws Exception {
        try(TestSvr svr = new TestSvr()) {
            svr.serve();
            TimeUnit.SECONDS.sleep(10L);
        }
    }
}