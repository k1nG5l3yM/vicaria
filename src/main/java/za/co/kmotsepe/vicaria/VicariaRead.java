package za.co.kmotsepe.vicaria;

/* Written and copyright 2001-2003 Benjamin Kohl.
 * Distributed under the GNU General Public License; see the README file.
 * This code comes with NO WARRANTY.
 *
 * Adopted and modified by Kingsley Motsepe (Copyright 2017)
 * Distributed under the GNU General Public License; see the README file.
 * This code comes with NO WARRANTY.
 * More Information and documentation: https://github.com/k1nG5l3yM/vicaria
 */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File: VicariaRead.java reads from a VicariaClientInputStream and writes to
 * the BufferedOutputStream
 *
 * @author Benjamin Kohl
 * @author Kingsley Motsepe
 * @since %G%
 * @version %I%
 */
public class VicariaRead extends Thread {

    private final int BUFFER_SIZE = 96000;
    private final BufferedInputStream in;
    private final BufferedOutputStream out;
    private final VicariaHTTPSession connection;
    private static VicariaServer server;

    private static final Logger LOGGER = LoggerFactory.getLogger(VicariaRead.class);

    public VicariaRead(VicariaServer server, VicariaHTTPSession connection, BufferedInputStream l_in,
            BufferedOutputStream l_out) {
        in = l_in;
        out = l_out;
        this.connection = connection;
        VicariaRead.server = server;
        setPriority(Thread.MIN_PRIORITY);
        startSelf();
    }

    @Override
    public void run() {
        read();
    }

    private void read() {
        int bytes_read;
        byte[] buf = new byte[BUFFER_SIZE];
        try {
            while (true) {
                bytes_read = in.read(buf);
                if (bytes_read != -1) {
                    out.write(buf, 0, bytes_read);
                    out.flush();
                    server.addBytesRead(bytes_read);
                } else {
                    break;
                }
            }
        } catch (IOException e) {
        }

        try {
            if (connection.getStatus() != VicariaHTTPSession.SC_CONNECTING_TO_HOST) // *uaaahhh*: fixes a very strange
                                                                                    // bug
            {
                connection.getLocalSocket().close();
            }
            // why? If we are connecting to a new host (and this thread is already running!)
            // , the upstream
            // socket will be closed. So we get here and close our own downstream
            // socket..... and the browser
            // displays an empty page because jhttpp2
            // closes the connection..... so close the downstream socket only when NOT
            // connecting to a new host....
        } catch (IOException e_socket_close) {
            LOGGER.error(e_socket_close.getMessage());
        }
    }

    public void close() {
        try {
            in.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * 
     */
    private void startSelf() {
        this.start();
    }
}
