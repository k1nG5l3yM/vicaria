package za.co.kmotsepe.vicaria;

/* Written and copyright 2001 Benjamin Kohl.
 * Distributed under the GNU General Public License; see the README file.
 * This code comes with NO WARRANTY.
 *
 * Adopted and modified by Kingsley Motsepe (Copyright 2017)
 * Distributed under the GNU General Public License; see the README file.
 * This code comes with NO WARRANTY.
 * More Information and documentation: https://github.com/k1nG5l3yM/vicaria
 * 
 * @author Benjamin Kohl
 * @author Kingsley Motsepe <kmotsepe@gmail.com>
 * @since %G%
 * @version %I%
 */
import java.io.IOException;

public interface VicariaInputStream {

    /**
     * reads the data
     * @param b
     * @return 
     * @throws java.io.IOException
     */
    public int read_f(byte[] b) throws IOException;
}
