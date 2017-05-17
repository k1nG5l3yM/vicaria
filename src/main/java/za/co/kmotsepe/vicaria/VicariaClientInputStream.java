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
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File: VicariaBufferedFilterStream.java
 *
 * @author Benjamin Kohl
 * @author KIngsley Motsepe <kmotsepe@gmail.com>
 * @since %G%
 * @version %I%
 */
public class VicariaClientInputStream extends BufferedInputStream {

    //private boolean filter = false;
    private String buf;
    private int lread = 0;
    /**
     * The length of the header (with body, if one)
     */
    private int header_length = 0;
    /**
     * The length of the (optional) body of the actual request
     */
    private int content_len = 0;
    /**
     * This is set to true with requests with bodies, like "POST"
     */
    private boolean body = false;
    private static VicariaServer server;
    private final VicariaHTTPSession vicariaHttpSession;
    private InetAddress remote_host;
    private String remote_host_name;
    private boolean ssl = false;
    private String errordescription;
    private int statuscode;

    public String url;
    public String method;
    public int remote_port = 0;
    public int post_data_len = 0;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(VicariaAdmin.class);

    public int getHeaderLength() {
        return header_length;
    }

    public InetAddress getRemoteHost() {
        return remote_host;
    }

    public String getRemoteHostName() {
        return remote_host_name;
    }

    public VicariaClientInputStream(VicariaServer server, VicariaHTTPSession vicariaHttpSession, InputStream a) {
        super(a);
        VicariaClientInputStream.server = server;
        this.vicariaHttpSession = vicariaHttpSession;
    }

    /**
     * Handler for the actual HTTP request
     *
     * @param a
     * @return 
     * @exception IOException
     */
    @Override
    public int read(byte[] a) throws IOException {
        statuscode = VicariaHTTPSession.SC_OK;
        if (ssl) {
            return super.read(a);
        }
        boolean cookies_enabled = server.enableCookiesByDefault();
        String rq = "";
        header_length = 0;
        post_data_len = 0;
        content_len = 0;
        boolean start_line = true;
        buf = getLine(); // reads the first line

        while (lread > 2) {
            if (start_line) {
                start_line = false;
                int methodID = server.getHttpMethod(buf);
                switch (methodID) {
                    case -1:
                        statuscode = VicariaHTTPSession.SC_NOT_SUPPORTED;
                        break;
                    case 2:
                        ssl = true;
                    default:
                        InetAddress host = parseRequest(buf, methodID);
                        if (statuscode != VicariaHTTPSession.SC_OK) {
                            break; // error occured, go on with the next line
                        }
                        if (!server.use_proxy && !ssl) {
                            /* creates a new request without the hostname */
                            buf = method + " " + url + " " + server.getHttpVersion() + "\r\n";
                            lread = buf.length();
                        }
                        if ((server.use_proxy && vicariaHttpSession.notConnected()) || !host.equals(remote_host)) {
                            if (server.debug) {
                                server.writeLog("read_f: STATE_CONNECT_TO_NEW_HOST");
                            }
                            statuscode = VicariaHTTPSession.SC_CONNECTING_TO_HOST;
                            remote_host = host;
                        }
                        /* -------------------------
					* url blocking (only "GET" method)
					* -------------------------*/
                        if (VicariaServer.block_urls && methodID == 0 && statuscode != VicariaHTTPSession.SC_FILE_REQUEST) {
                            if (server.debug) {
                                System.out.println("Searching match...");
                            }
                            VicariaURLMatch match;
                            match = server.findMatch(this.remote_host_name + url);
                            if (match != null) {
                                //if (server.debug) {
                                    LOGGER.info("Match found!");
                                    //System.out.println();
                                //
                                cookies_enabled = match.getCookiesEnabled();
                                if (match.getActionIndex() == -1) {
                                    break;
                                }
                                OnURLAction action;
                                action = (OnURLAction) server.getURLActions().get(match.getActionIndex());
                                if (action.onAccesssDeny()) {
                                    statuscode = VicariaHTTPSession.SC_URL_BLOCKED;
                                    if (action.onAccessDenyWithCustomText()) {
                                        errordescription = action.getCustomErrorText();
                                    }
                                } else if (action.onAccessRedirect()) {
                                    statuscode = VicariaHTTPSession.SC_MOVED_PERMANENTLY;
                                    errordescription = action.newLocation();
                                }
                            }//end if match!=null)
                        } //end if (server.block...
                } // end switch
            }// end if(startline)
            else {
                /*-----------------------------------------------
				* Content-Length parsing
				*-----------------------------------------------*/
                if (server.startsWith(buf.toUpperCase(), "CONTENT-LENGTH")) {
                    String clen = buf.substring(16);
                    if (clen.contains("\r")) {
                        clen = clen.substring(0, clen.indexOf("\r"));
                    } else if (clen.contains("\n")) {
                        clen = clen.substring(0, clen.indexOf("\n"));
                    }
                    try {
                        content_len = Integer.parseInt(clen);
                    } catch (NumberFormatException e) {
                        statuscode = VicariaHTTPSession.SC_CLIENT_ERROR;
                    }
                    if (server.debug) {
                        server.writeLog("read_f: content_len: " + content_len);
                    }
                    if (!ssl) {
                        body = true; // Note: in HTTP/1.1 any method can have a body, not only "POST"
                    }
                } else if (server.startsWith(buf, "Proxy-Connection:")) {
                    if (!server.use_proxy) {
                        buf = null;
                    } else {
                        buf = "Proxy-Connection: Keep-Alive\r\n";
                        lread = buf.length();
                    }
                } /*else if (server.startsWith(buf,"Connection:"))
              {
                 if (!server.use_proxy)
                 {
                   buf="Connection: Keep-Alive\r\n"; //use always keep-alive
                 lread=buf.length();
                 }
                 else buf=null;
               }*/ /*-----------------------------------------------
		 * cookie crunch section
			 	 *-----------------------------------------------*/ else if (server.startsWith(buf, "Cookie:")) {
                    if (!cookies_enabled) {
                        buf = null;
                    }
                } /*------------------------------------------------
               * Http-Header filtering section
               *------------------------------------------------*/ else if (server.filter_http) {
                    if (server.startsWith(buf, "Referer:")) {// removes "Referer"
                        buf = null;
                    } else if (server.startsWith(buf, "User-Agent")) // changes User-Agent
                    {
                        buf = "User-Agent: " + server.getUserAgent() + "\r\n";
                        lread = buf.length();
                    }
                }
            }
            if (buf != null) {
                rq += buf;
                if (server.debug) {
                    server.writeLog(buf);
                }
                header_length += lread;
            }
            buf = getLine();
        }
        rq += buf; //adds last line (should be an empty line) to the header String
        header_length += lread;

        if (header_length == 0) {
            if (server.debug) {
                server.writeLog("header_length=0, setting status to SC_CONNECTION_CLOSED (buggy request)");
            }
            statuscode = VicariaHTTPSession.SC_CONNECTION_CLOSED;
        }

        for (int i = 0; i < header_length; i++) {
            a[i] = (byte) rq.charAt(i);
        }

        if (body) {// read the body, if "Content-Length" given
            post_data_len = 0;
            while (post_data_len < content_len) {
                a[header_length + post_data_len] = (byte) read(); // writes data into the array
                post_data_len++;
            }
            header_length += content_len; // add the body-length to the header-length
            body = false;
        }

        return (statuscode == VicariaHTTPSession.SC_OK) ? header_length : -1; // return -1 with an error
    }

    /**
     * reads a line
     *
     * @return 
     * @exception IOException
     */
    public String getLine() throws IOException {
        int l = 0;
        String line = "";
        lread = 0;
        while (l != '\n') {
            l = read();
            if (l != -1) {
                line += (char) l;
                lread++;
            } else {
                break;
            }
        }
        return line;
    }

    /**
     * Parser for the first (!) line from the HTTP request<BR>
     * Sets up the URL, method and remote hostname.
     *
     * @param a
     * @param method_index
     * @return an InetAddress for the hostname, null on errors with a
     * statuscode!=SC_OK
     */
    public InetAddress parseRequest(String a, int method_index) {
        if (server.debug) {
            server.writeLog(a);
        }
        String f;
        int pos;
        url = "";
        if (ssl) {
            f = a.substring(8);
        } else {
            method = a.substring(0, a.indexOf(" ")); //first word in the line
            pos = a.indexOf(":"); // locate first :
            if (pos == -1) { // occours with "GET / HTTP/1.1"
                url = a.substring(a.indexOf(" ") + 1, a.lastIndexOf(" "));
                if (method_index == 0) { // method_index==0 --> GET
                    if (url.contains(server.WEB_CONFIG_FILE)) {
                        statuscode = VicariaHTTPSession.SC_CONFIG_RQ;
                    } else {
                        statuscode = VicariaHTTPSession.SC_FILE_REQUEST;
                    }
                } else {
                    if (method_index == 1 && url.contains(server.WEB_CONFIG_FILE)) { // allow "POST" for admin log in
                        statuscode = VicariaHTTPSession.SC_CONFIG_RQ;
                    } else {
                        statuscode = VicariaHTTPSession.SC_INTERNAL_SERVER_ERROR;
                        errordescription = "This WWW proxy supports only the \"GET\" method while acting as webserver.";
                    }
                }
                return null;
            }
            f = a.substring(pos + 3); //removes "http://"
        }
        pos = f.indexOf(" "); // locate space, should be the space before "HTTP/1.1"
        if (pos == -1) { // buggy request
            statuscode = VicariaHTTPSession.SC_CLIENT_ERROR;
            errordescription = "Your browser sent an invalid request: \"" + a + "\"";
            return null;
        }
        f = f.substring(0, pos); //removes all after space
        // if the url contains a space... it's not our mistake...(url's must never contain a space character)
        pos = f.indexOf("/"); // locate the first slash
        if (pos != -1) {
            url = f.substring(pos); // saves path without hostname
            f = f.substring(0, pos); // reduce string to the hostname
        } else {
            url = "/"; // occurs with this request: "GET http://localhost HTTP/1.1"
        }
        pos = f.indexOf(":"); // check for the portnumber
        if (pos != -1) {
            String l_port = f.substring(pos + 1);
            l_port = l_port.contains(" ") ? l_port.substring(0, l_port.indexOf(" ")) : l_port;
            int i_port = 80;
            try {
                i_port = Integer.parseInt(l_port);
            } catch (NumberFormatException e_get_host) {
                server.writeLog("get_Host :" + e_get_host + " !!!!");
            }
            f = f.substring(0, pos);
            remote_port = i_port;
        } else {
            remote_port = 80;
        }
        remote_host_name = f;
        InetAddress address = null;
        if (server.log_access) {
            server.logAccess(vicariaHttpSession.getLocalSocket().getInetAddress().getHostAddress() + " " + method + " " + getFullURL());
        }
        try {
            address = InetAddress.getByName(f);
            if (remote_port == server.port && address.equals(InetAddress.getLocalHost())) {
                if (url.contains(server.WEB_CONFIG_FILE) && (method_index == 0 || method_index == 1)) {
                    statuscode = VicariaHTTPSession.SC_CONFIG_RQ;
                } else if (method_index > 0) {
                    statuscode = VicariaHTTPSession.SC_INTERNAL_SERVER_ERROR;
                    errordescription = "This WWW proxy supports only the \"GET\" method while acting as webserver.";
                } else {
                    statuscode = VicariaHTTPSession.SC_FILE_REQUEST;
                }
            }
        } catch (UnknownHostException e_u_host) {
            
            if (!server.use_proxy) {
                statuscode = VicariaHTTPSession.SC_HOST_NOT_FOUND;
            }
            LOGGER.error(e_u_host.getMessage());
        }
        
        return address;
    }

    /**
     * @return boolean whether the actual connection was established with the
     * CONNECT method.
     * @since 0.2.21
     */
    public boolean isTunnel() {
        return ssl;
    }

    /**
     * @return the full qualified URL of the actual request.
     * @since 0.4.0
     */
    public String getFullURL() {
        return "http" + (ssl ? "s" : "") + "://" + getRemoteHostName()
                + (remote_port != 80 ? (":" + remote_port) : "") + url;
    }

    /**
     * @return status-code for the actual request
     * @since 0.3.5
     */
    public int getStatusCode() {
        return statuscode;
    }

    /**
     * @return the (optional) error-description for this request
     */
    public String getErrorDescription() {
        return errordescription;
    }
}
