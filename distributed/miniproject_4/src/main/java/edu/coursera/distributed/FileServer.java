package edu.coursera.distributed;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A basic and very limited implementation of a file server that responds to GET
 * requests from HTTP clients.
 */
public final class FileServer {
    /**
     * Main entrypoint for the basic file server.
     *
     * @param socket Provided socket to accept connections on.
     * @param fs     A proxy filesystem to serve files from. See the PCDPFilesystem
     *               class for more detailed documentation of its usage.
     * @param ncores The number of cores that are available to your
     *               multi-threaded file server. Using this argument is entirely
     *               optional. You are free to use this information to change
     *               how you create your threads, or ignore it.
     * @throws IOException If an I/O error is detected on the server. This
     *                     should be a fatal error, your file server
     *                     implementation is not expected to ever throw
     *                     IOExceptions during normal operation.
     */
    public void run(final ServerSocket socket, final PCDPFilesystem fs,
                    final int ncores) throws IOException {
        /*
         * Enter a spin loop for handling client requests to the provided
         * ServerSocket object.
         */
        final ExecutorService pool = Executors.newFixedThreadPool(ncores);

        for (int i = 0; i < ncores; i++) {
            pool.submit(() -> {
                while (true) {
                    try {
                        processRequest(socket, fs);
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            });
        }


    }

    private void processRequest(ServerSocket socket, PCDPFilesystem fs) throws IOException {
        try (
                Socket user = socket.accept();
                /* Socket in */
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(user.getInputStream()));
                /* Socket out */
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(user.getOutputStream()));
        ) {
            String httpPacket = reader.readLine();
            HttpPayload payload = new HttpPayload(httpPacket);
            if (payload.isGetMethod()) {
                String path = payload.getHttpResourse();
                writeToSocket(writer, fs, path);
            }
        } finally {

        }
    }

    private void writeToSocket(BufferedWriter writer, PCDPFilesystem fs,
                               String resource) throws IOException {
        String message;
        PCDPPath path = new PCDPPath(resource);
        String data = fs.readFile(path);
        if (data != null || "".equals(data)) {
            message = "HTTP/1.0 200 OK\r\n" +
                    "Server: FileServer\r\n" +
                    "\r\n";
            writer.write(message);
            writer.write(data);
        } else {
            message = "HTTP/1.0 404 Not Found\r\n" +
                    "Server: FileServer\r\n" +
                    "\r\n";
            writer.write(message);
        }
    }

    private class HttpPayload {
        private String httpMethod;
        private String httpResourse;

        public HttpPayload(String httpPacket) {
            String[] payload = httpPacket.split(" ");
            httpMethod = payload[0];
            httpResourse = payload[1];
        }

        public String getHttpMethod() {
            return httpMethod;
        }

        public Boolean isGetMethod() {
            return "GET".equals(httpMethod);
        }

        public Boolean isPostMethod() {
            return "POST".equals(httpMethod);
        }

        public String getHttpResourse() {
            return httpResourse;
        }
    }

}
