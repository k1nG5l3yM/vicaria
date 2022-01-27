package za.co.kmotsepe.vicaria;

/*
 * Adopted and modified by Kingsley Motsepe (Copyright 2017)
 * Distributed under the GNU General Public License; see the README file.
 * This code comes with NO WARRANTY.
 * More Information and documentation: https://github.com/k1nG5l3yM/vicaria
 */

/**
 * Title: jHTTPp2: Java HTTP Filter Proxy Description: static utility routines
 * Copyright: Copyright (c) 2001 Benjamin Kohl
 *
 * @author Benjamin Kohl
 * @author Kingsley Motsepe
 * @since %G%
 * @version %I%
 */
public class VicariaUtils {

    //
    // Copyright (C)1996,1998 by Jef Poskanzer <jef@acme.com>. All rights reserved.
    //
    // Redistribution and use in source and binary forms, with or without
    // modification, are permitted provided that the following conditions
    // are met:
    // 1. Redistributions of source code must retain the above copyright
    // notice, this list of conditions and the following disclaimer.
    // 2. Redistributions in binary form must reproduce the above copyright
    // notice, this list of conditions and the following disclaimer in the
    // documentation and/or other materials provided with the distribution.
    //
    // THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
    // ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    // IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    // ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
    // FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
    // DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
    // OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
    // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
    // LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
    // OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
    // SUCH DAMAGE.
    //
    // Visit the ACME Labs Java page for up-to-date versions of this and other
    // fine Java utilities: http://www.acme.com/java/
    public static String urlDecoder(String encoded) {
        StringBuilder decoded = new StringBuilder();
        int len = encoded.length();
        for (int i = 0; i < len; ++i) {
            if (encoded.charAt(i) == '%' && i + 2 < len) {
                int d1 = Character.digit(encoded.charAt(i + 1), 16);
                int d2 = Character.digit(encoded.charAt(i + 2), 16);
                if (d1 != -1 && d2 != -1) {
                    decoded.append((char) ((d1 << 4) + d2));
                }
                i += 2;
            } else if (encoded.charAt(i) == '+') {
                decoded.append(' ');
            } else {
                decoded.append(encoded.charAt(i));
            }
        }
        return decoded.toString();
    }

}
