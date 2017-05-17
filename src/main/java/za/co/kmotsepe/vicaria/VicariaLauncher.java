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

import com.github.lalyos.jfiglet.FigletFont;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application launcher class
 *
 * @author Benjamin Kohl
 * @author Kingsley Motsepe <kmotsepe@gmail.com>
 * @since %G%
 * @version %I%
 */
public class VicariaLauncher {

    static VicariaServer server;
    private static final Logger LOGGER = LoggerFactory.getLogger(VicariaLauncher.class);

    public static void main(String[] args) {
        //load ascii banner
        String asciiArt = FigletFont.convertOneLine(".:Vicaria - HTTP - Proxy:.");
        LOGGER.info("\n" + asciiArt);
        
        server = new VicariaServer();
        
        //TODO maybe 'too much admin' here? Better to have this in the server class instead
        if (VicariaServer.error) {
            LOGGER.error(VicariaServer.error_msg);
        } else {
            new Thread(server).start();
            LOGGER.info("Running on port " + server.port);
        }
    }
}
