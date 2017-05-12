package za.co.kmotsepe.vicaria;

/* Written and copyright 2001 Benjamin Kohl.
 * Distributed under the GNU General Public License; see the README file.
 * This code comes with NO WARRANTY.
 */
import java.io.IOException;

public interface VicariaInputStream {

    /**
     * reads the data
     */
    public int read_f(byte[] b) throws IOException;
}
