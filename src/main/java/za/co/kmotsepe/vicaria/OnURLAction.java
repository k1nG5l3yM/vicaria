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
 * Title:        jHTTPp2: Java HTTP Filter Proxy
 * Description: An OpenSource HTTP Proxy
 * Copyright:    Copyright (c) 2001 Benjamin Kohl
 * @author Benjamin Kohl
 * @author Kingsley Motsepe <kmotsepe@gmail.com>
 * @since %G%
 * @version %I%
 */
public class OnURLAction implements java.io.Serializable {

    private String customerrortext, desc, httppath, newlocation;
    private boolean log, block, customtext, http_rq, anotherlocation;
    
    /**
     * 
     * @param desc 
     */
    public OnURLAction(String desc) {
        this.desc = desc;
    }
    
    /**
     * 
     * @param customerrortext 
     */
    public void denyAccess(String customerrortext) {
        this.block = true;
        this.customtext = true;
        this.customerrortext = customerrortext;
    }
    
    /**
     * 
     */
    public void denyAccess() {
        block = true;
    }
    
    /**
     * 
     */
    public void logAccess() {
        log = true;
    }
    
    /**
     * 
     * @param newlocation 
     */
    public void anotherLocation(String newlocation) {
        this.anotherlocation = true;
        this.newlocation = newlocation;
    }
    
    /**
     * 
     * @return 
     */
    public boolean onAccesssDeny() {
        return block;
    }
    
    /**
     * 
     * @return 
     */
    public boolean onAccessLog() {
        return log;
    }
    
    /**
     * 
     * @return 
     */
    public boolean onAccessDenyWithCustomText() {
        return customtext;
    }
    
    /**
     * 
     * @return 
     */
    public boolean onAccessSendHTTPRequest() {
        return http_rq;
    }
    
    /**
     * 
     * @return 
     */
    public boolean onAccessRedirect() {
        return this.anotherlocation;
    }
    
    /**
     * 
     * @return 
     */
    public String newLocation() {
        return this.newlocation;
    }
    
    /**
     * 
     * @param http_rq
     * @param httppath 
     */
    public void setHTTPAction(boolean http_rq, String httppath) {
        this.http_rq = http_rq;
        this.httppath = httppath;
    }
    
    /**
     * 
     * @return 
     */
    public String getCustomErrorText() {
        return customerrortext;
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
        return desc;
    }

}
