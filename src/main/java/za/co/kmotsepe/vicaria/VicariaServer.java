package za.co.kmotsepe.vicaria;

/* Written and copyright 2001-2003 Benjamin Kohl.
 * Distributed under the GNU General Public License; see the README file.
 * This code comes with NO WARRANTY.
 * More Information and documentation: HTTP://jhttp2.sourceforge.net/
 *
 * Adopted and modified by Kingsley Motsepe (Copyright 2017)
 * Distributed under the GNU General Public License; see the README file.
 * This code comes with NO WARRANTY.
 * More Information and documentation: https://github.com/k1nG5l3yM/vicaria
 */
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.net.BindException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Benjamin Kohl
 * @author Kingsley Motsepe
 * @since %G%
 * @version %I%
 */
public class VicariaServer implements Runnable {

    // TODO Most properties here need to load from properties file. Especially
    // credentials
    private static final String CRLF = "\r\n";
    private static final String VERSION = "0.5.1";
    private static final String V_SPECIAL = " 2003-05-20";
    private final String HTTP_VERSION = "HTTP/1.1";

    private final String DATA_FILE = "server.data";
    private final String SERVER_PROPERTIES_FILE = "server.properties";

    private String http_useragent = "Mozilla/4.0 (compatible; MSIE 4.0; WindowsNT 5.0)";
    private ServerSocket listen;
    private BufferedWriter logfile;
    private BufferedWriter access_logfile;
    private Properties serverproperties = null;

    private long bytesread;
    private long byteswritten;
    private int numconnections;

    private boolean enable_cookies_by_default = true;
    private WildcardDictionary dic = new WildcardDictionary();
    private ArrayList urlactions;

    // TODO This has to go. Will place in default config file :)
    public final int DEFAULT_SERVER_PORT = 8088;
    public final String WEB_CONFIG_FILE = "admin/jp2-config";

    public int port = DEFAULT_SERVER_PORT;
    public InetAddress proxy;
    public int proxy_port = 0;

    public long config_auth = 0;
    public long config_session_id = 0;

    // TODO This has to go. Will place in default config file :)
    public String config_user = "root";
    public String config_password = "geheim";

    public static boolean error;
    public static String error_msg;

    public boolean use_proxy = false;
    public static boolean block_urls = false;
    public boolean filter_http = false;
    public boolean debug = false;
    public boolean log_access = false;
    public String log_access_filename = "paccess.log";
    public boolean webconfig = true;
    public boolean www_server = true;

    private static final Logger LOGGER = LoggerFactory.getLogger(VicariaServer.class);

    void init() {

        LOGGER.info("Server startup");

        try {
            restoreSettings();
        } catch (Exception e_load) {
            LOGGER.error("Error while resoring settings: " + e_load.getMessage());
        }

        try {
            listen = new ServerSocket(port);
        } catch (BindException e_bind_socket) {
            LOGGER.error("Socket " + port + " is already in use (Another Vicaria proxy running?) "
                    + e_bind_socket.getMessage());
        } catch (IOException e_io_socket) {
            LOGGER.error("Error while creating server socket on port " + port + ". " + e_io_socket.getMessage());
        }
    }

    public VicariaServer() {
        // TODO maybe load this from a properties/text file?
        // TODO have launcher message display in 'bootstrap' function?

        bootStrap();
    }

    /**
     * calls init(), sets up the serverport and starts for each connection new
     * Jhttpp2Connection
     */
    void serve() {
        LOGGER.info("Server running");
        try {
            while (true) {
                Socket client = listen.accept();
                // TODO rather have a session builder?
                new VicariaHTTPSession(this, client);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public void run() {
        serve();
    }

    public void setErrorMsg(String a) {
        error = true;
        error_msg = a;
    }

    /**
     * Tests what method is used with the request
     *
     * @param d
     * @return -1 if the server doesn't support the method
     */
    public int getHttpMethod(String d) {
        if (startsWith(d, "GET") || startsWith(d, "HEAD")) {
            return 0;
        }
        if (startsWith(d, "POST") || startsWith(d, "PUT")) {
            return 1;
        }
        if (startsWith(d, "CONNECT")) {
            return 2;
        }
        if (startsWith(d, "OPTIONS")) {
            return 3;
        }

        return -1;/*
                   * No match...
                   * 
                   * Following methods are not implemented:
                   * || startsWith(d,"TRACE")
                   */
    }

    public boolean startsWith(String a, String what) {
        int l = what.length();
        int l2 = a.length();
        return l2 >= l ? a.substring(0, l).equals(what) : false;
    }

    /**
     * @return the Server response-header field
     */
    public String getServerIdentification() {
        return "jHTTPp2/" + getServerVersion();
    }

    public static String getServerVersion() {
        return VERSION + V_SPECIAL;
    }

    /**
     * saves all settings with a ObjectOutputStream into a file
     *
     * @throws java.io.IOException
     * @since 0.2.10
     */
    public void saveSettings() throws IOException {
        serverproperties.setProperty("server.http-proxy", Boolean.toString(use_proxy));
        serverproperties.setProperty("server.http-proxy.hostname", proxy.getHostAddress());
        serverproperties.setProperty("server.http-proxy.port", Integer.toString(proxy_port));
        serverproperties.setProperty("server.filter.http", Boolean.toString(filter_http));
        serverproperties.setProperty("server.filter.url", Boolean.toString(block_urls));
        serverproperties.setProperty("server.filter.http.useragent", http_useragent);
        serverproperties.setProperty("server.enable-cookies-by-default", Boolean.toString(enable_cookies_by_default));
        serverproperties.setProperty("server.debug-logging", Boolean.toString(debug));
        serverproperties.setProperty("server.port", Integer.toString(port));
        serverproperties.setProperty("server.access.log", Boolean.toString(log_access));
        serverproperties.setProperty("server.access.log.filename", log_access_filename);
        serverproperties.setProperty("server.webconfig", Boolean.toString(webconfig));
        serverproperties.setProperty("server.www", Boolean.toString(www_server));
        serverproperties.setProperty("server.webconfig.username", config_user);
        serverproperties.setProperty("server.webconfig.password", config_password);
        storeServerProperties();

        try (ObjectOutputStream file = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            file.writeObject(dic);
            file.writeObject(urlactions);
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
        }
    }

    /**
     * restores all Jhttpp2 options from "settings.dat"
     *
     * @since 0.2.10
     */
    public void restoreSettings()// throws Exception
    {
        getServerProperties();
        use_proxy = Boolean.valueOf(serverproperties.getProperty("server.http-proxy", "false"));
        try {
            proxy = InetAddress.getByName(serverproperties.getProperty("server.http-proxy.hostname", "127.0.0.1"));
        } catch (UnknownHostException e) {
            LOGGER.error(e.getMessage());
        }
        proxy_port = Integer.parseInt(serverproperties.getProperty("server.http-proxy.port", "8080"));
        block_urls = Boolean.valueOf(serverproperties.getProperty("server.filter.url", "false"));
        http_useragent = serverproperties.getProperty("server.filter.http.useragent",
                "Mozilla/4.0 (compatible; MSIE 4.0; WindowsNT 5.0)");
        filter_http = Boolean.valueOf(serverproperties.getProperty("server.filter.http", "false"));
        enable_cookies_by_default = Boolean
                .valueOf(serverproperties.getProperty("server.enable-cookies-by-default", "true"));
        debug = Boolean.valueOf(serverproperties.getProperty("server.debug-logging", "false"));
        port = Integer.parseInt(serverproperties.getProperty("server.port", "8088"));
        log_access = Boolean.parseBoolean(serverproperties.getProperty("server.access.log", "false"));
        log_access_filename = serverproperties.getProperty("server.access.log.filename", "paccess.log");
        webconfig = Boolean.parseBoolean(serverproperties.getProperty("server.webconfig", "true"));
        www_server = Boolean.parseBoolean(serverproperties.getProperty("server.www", "true"));
        config_user = serverproperties.getProperty("server.webconfig.username", "root");
        config_password = serverproperties.getProperty("server.webconfig.password", "geheim");

        try {

            access_logfile = new BufferedWriter(new FileWriter(log_access_filename, true));
            // Restore the WildcardDioctionary and the URLActions with the ObjectInputStream
            // (settings.dat)...
            ObjectInputStream obj_in;
            File file = new File(DATA_FILE);
            if (!file.exists()) {
                if (!file.createNewFile() || !file.canWrite()) {
                    throw new IOException("Can't create or write to file " + file.toString());
                    // setErrorMsg("Can't create or write to file " + file.toString());
                } else {
                    saveSettings();
                }
            }

            obj_in = new ObjectInputStream(new FileInputStream(file));
            dic = (WildcardDictionary) obj_in.readObject();
            // Object[] objects = (Object[])obj_in.readObject();
            urlactions = new ArrayList(Arrays.asList((Object[]) obj_in.readObject()));
            obj_in.close();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error(e.getMessage());
            // setErrorMsg("restoreSettings(): " + e.getMessage());
        }
    }

    /**
     * @return the HTTP version used by jHTTPp2
     */
    public String getHttpVersion() {
        return HTTP_VERSION;
    }

    /**
     * the User-Agent header field
     *
     * @since 0.2.17
     * @return User-Agent String
     */
    public String getUserAgent() {
        return http_useragent;
    }

    public void setUserAgent(String ua) {
        http_useragent = ua;
    }

    // TODO this won't be needed anymore. Rather use SLF4J
    /**
     * writes into the server log file and adds a new line
     *
     * @param s
     * @since 0.2.21
     */
    public void writeLog(String s) {
        writeLog(s, true);
    }

    // TODO this won't be needed anymore. Rather use SLF4J
    /**
     * writes to the server log file
     *
     * @param s
     * @param b
     * @since 0.2.21
     */
    public void writeLog(String s, boolean b) {
        try {
            s = new Date().toString() + " " + s;
            logfile.write(s, 0, s.length());
            if (b) {
                logfile.newLine();
            }
            logfile.flush();
            if (debug) {
                LOGGER.debug(s);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void closeLog() {
        try {
            LOGGER.info("Server shutdown");
            // writeLog("Server shutdown.");
            logfile.flush();
            logfile.close();
            access_logfile.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void addBytesRead(long read) {
        bytesread += read;
    }

    /**
     * Functions for the jHTTPp2 statistics: How many connections Bytes
     * read/written
     *
     * @param written
     * @since 0.3.0
     */
    public void addBytesWritten(int written) {
        byteswritten += written;
    }

    public int getServerConnections() {
        return numconnections;
    }

    public long getBytesRead() {
        return bytesread;
    }

    public long getBytesWritten() {
        return byteswritten;
    }

    public void increaseNumConnections() {
        numconnections++;
    }

    public void decreaseNumConnections() {
        numconnections--;
    }

    public void AuthenticateUser(String u, String p) {
        if (config_user.equals(u) && config_password.equals(p)) {
            config_auth = 1;
        } else {
            config_auth = 0;
        }
    }

    public String getGMTString() {
        return new Date().toString();
    }

    public VicariaURLMatch findMatch(String url) {
        return (VicariaURLMatch) dic.get(url);
    }

    public WildcardDictionary getWildcardDictionary() {
        return dic;
    }

    public ArrayList getURLActions() {
        return urlactions;
    }

    public boolean enableCookiesByDefault() {
        return this.enable_cookies_by_default;
    }

    public void enableCookiesByDefault(boolean a) {
        enable_cookies_by_default = a;
    }

    public void resetStat() {
        bytesread = 0;
        byteswritten = 0;
    }

    /**
     * @return @since 0.4.10a
     */
    public Properties getServerProperties() {
        if (serverproperties == null) {
            serverproperties = new Properties();
            try {
                serverproperties.load(new DataInputStream(new FileInputStream(SERVER_PROPERTIES_FILE)));
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
                // writeLog("getServerProperties(): " + e.getMessage());
            }
        }
        return serverproperties;
    }

    /**
     * @since 0.4.10a
     */
    public void storeServerProperties() {
        if (serverproperties == null) {
            return;
        }
        try {
            serverproperties.store(new FileOutputStream(SERVER_PROPERTIES_FILE),
                    "Jhttpp2Server main properties. Look at the README file for further documentation.");
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            // ("storeServerProperties(): " + e.getMessage());
        }
    }

    /**
     * @param s
     * @since 0.4.10a
     */
    public void logAccess(String s) {
        try {
            access_logfile.write("[" + new Date().toString() + "] " + s + "\r\n");
            access_logfile.flush();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void shutdownServer() {
        closeLog();
        System.exit(0);
    }

    /**
     * used for calling the overridden method init()
     */
    private void bootStrap() {
        LOGGER.info("Vicaria HTTP Proxy Server Release " + getServerVersion() + "\r\n"
                + "Copyright (c) 2017 Kingsley Motsepe\r\n"
                + "This software comes with ABSOLUTELY NO WARRANTY OF ANY KIND.\r\n\n"
                + "Project Home: https://github.com/k1nG5l3yM/vicaria");
        init();
    }

}
