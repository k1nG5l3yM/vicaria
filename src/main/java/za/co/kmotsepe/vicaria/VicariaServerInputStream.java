package za.co.kmotsepe.vicaria;

/* Written and copyright 2001 Benjamin Kohl.
 * Distributed under the GNU General Public License; see the README file.
 * This code comes with NO WARRANTY.
 *
 * Adopted and modified by Kingsley Motsepe (Copyright 2017)
 * Distributed under the GNU General Public License; see the README file.
 * This code comes with NO WARRANTY.
 * More Information and documentation: https://github.com/k1nG5l3yM/vicaria
 */
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * @author Benjamin Kohl
 * @author Kingsley Motsepe <kmotsepe@gmail.com>
 * @since %G%
 * @version %I%
 */
public class VicariaServerInputStream extends BufferedInputStream implements VicariaInputStream {

    private final VicariaHTTPSession connection;
    
    /**
     * 
     * @param server
     * @param connection
     * @param a
     * @param filter 
     */
    public VicariaServerInputStream(VicariaServer server, 
            VicariaHTTPSession connection, InputStream a, boolean filter) {
        super(a);
        this.connection = connection;
    }

    @Override
    public int read_f(byte[] b) throws IOException {
        return read(b);
    }
}
