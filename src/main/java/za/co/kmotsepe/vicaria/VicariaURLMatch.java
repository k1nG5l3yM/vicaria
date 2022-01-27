package za.co.kmotsepe.vicaria;

/*
 * Adopted and modified by Kingsley Motsepe (Copyright 2017)
 * Distributed under the GNU General Public License; see the README file.
 * This code comes with NO WARRANTY.
 * More Information and documentation: https://github.com/k1nG5l3yM/vicaria
 */

/**
 * Title: jHTTPp2: Java HTTP Filter Proxy Copyright: Copyright (c) 2001-2003
 * Benjamin Kohl
 *
 * @author Benjamin Kohl
 * @author Kingsley Motsepe
 * @since %G%
 * @version %I%
 */
public class VicariaURLMatch implements java.io.Serializable {

    String match;
    String desc;
    boolean cookies_enabled;
    int actionindex;

    /**
     * 
     * @param match
     * @param cookies_enabled
     * @param actionindex
     * @param description
     */
    public VicariaURLMatch(String match, boolean cookies_enabled, int actionindex, String description) {
        this.match = match;
        this.cookies_enabled = cookies_enabled;
        this.actionindex = actionindex;
        this.desc = description;
    }

    /**
     * 
     * @return
     */
    public String getMatch() {
        return match;
    }

    /**
     * 
     * @return
     */
    public boolean getCookiesEnabled() {
        return cookies_enabled;
    }

    public int getActionIndex() {
        return actionindex;
    }

    /**
     * 
     * @return
     */
    public String getDescription() {
        return desc;
    }

    /**
     * 
     * @return
     */
    @Override
    public String toString() {
        return "\"" + match + "\" " + desc;
    }
}
