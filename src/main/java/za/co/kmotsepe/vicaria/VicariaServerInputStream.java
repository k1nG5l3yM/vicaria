package za.co.kmotsepe.vicaria;

/* Written and copyright 2001 Benjamin Kohl.
 * Distributed under the GNU General Public License; see the README file.
 * This code comes with NO WARRANTY.
 */

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;

public class VicariaServerInputStream extends BufferedInputStream implements VicariaInputStream
{
        private VicariaHTTPSession connection;

        public VicariaServerInputStream(VicariaServer server,VicariaHTTPSession connection,InputStream a,boolean filter)
	{
          super(a);
          this.connection=connection;
	}
	public int read_f(byte[] b)throws IOException
	{
          return read(b);
        }
}

