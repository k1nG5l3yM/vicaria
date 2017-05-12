package za.co.kmotsepe.vicaria;

import com.github.lalyos.jfiglet.FigletFont;

/* Written and copyright 2001-2003 Benjamin Kohl.
 * Distributed under the GNU General Public License; see the README file.
 * This code comes with NO WARRANTY.
 */

 /* Adopted and modified by Kingsley Motsepe (Copyright 2017)
 * Distributed under the GNU General Public License; see the README file.
 * This code comes with NO WARRANTY.
 */
/**
 * Title: jHTTPp2: Java HTTP Filter Proxy Description: starts thwe Swing GUI or
 * the console-mode only proxy Copyright: Copyright (c) 2017
 *
 * @author Benjamin Kohl
 * @author Kingsley Motsepe <kmotsepe@gmail.com>
 * @since %G%
 * @version %I%
 */
public class VicariaLauncher {

    static VicariaServer server;

    public static void main(String[] args) {
        //load ascii banner
        String asciiArt = FigletFont.convertOneLine("Vicaria - HTTP - Proxy");
        System.out.println(asciiArt);

        server = new VicariaServer(true);
        if (server.error) {
            System.out.println("Error: " + server.error_msg);
        } else {
            new Thread(server).start();
            System.out.println("Running on port " + server.port);
        }
    }
}
