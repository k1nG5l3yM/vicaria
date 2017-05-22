package za.co.kmotsepe.vicaria;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import org.apache.http.client.methods.HttpGet;

/**
 * Courtesy of http://stackoverflow.com/users/22769/igor-zelaya Found at:
 * http://stackoverflow.com/questions/13255622/parsing-raw-http-request
 *
 * Class for HTTP request parsing as defined by RFC 2612:
 *
 * Request = Request-Line ; Section 5.1 (( general-header ; Section 4.5 |
 * request-header ; Section 5.3 | entity-header ) CRLF) ; Section 7.1 CRLF [
 * message-body ] ; Section 4.3
 *
 * @author izelaya
 * @author Kingsley Motsepe
 *
 */
public class HttpRequestParser {

    private String _requestLine;
    private Hashtable<String, String> _requestHeaders;
    private StringBuffer _messagetBody;

    public HttpRequestParser() {
        _requestHeaders = new Hashtable<String, String>();
        _messagetBody = new StringBuffer();
    }

    /**
     * Parse and HTTP request.
     *
     * @param request String holding http request.
     * @throws IOException If an I/O error occurs reading the input stream.
     * @throws HttpFormatException If HTTP Request is malformed
     */
    public void parseRequest(String request) throws IOException, HttpFormatException {
        BufferedReader reader = new BufferedReader(new StringReader(request));

        setRequestLine(reader.readLine()); // Request-Line ; Section 5.1

        String header = reader.readLine();
        while (header.length() > 0) {
            appendHeaderParameter(header);
            header = reader.readLine();
        }

        String bodyLine = reader.readLine();
        while (bodyLine != null) {
            appendMessageBody(bodyLine);
            bodyLine = reader.readLine();
        }

    }

    /**
     *
     * 5.1 Request-Line The Request-Line begins with a method token, followed by
     * the Request-URI and the protocol version, and ending with CRLF. The
     * elements are separated by SP characters. No CR or LF is allowed except in
     * the final CRLF sequence.
     *
     * @return String with Request-Line
     */
    public String getRequestLine() {
        return _requestLine;
    }

    private void setRequestLine(String requestLine) throws HttpFormatException {
        if (requestLine == null || requestLine.length() == 0) {
            throw new HttpFormatException("Invalid Request-Line: " + requestLine);
        }
        _requestLine = requestLine;
    }

    private void appendHeaderParameter(String header) throws HttpFormatException {
        int idx = header.indexOf(":");
        if (idx == -1) {
            throw new HttpFormatException("Invalid Header Parameter: " + header);
        }
        _requestHeaders.put(header.substring(0, idx), header.substring(idx + 1, header.length()));
    }

    /**
     * The message-body (if any) of an HTTP message is used to carry the
     * entity-body associated with the request or response. The message-body
     * differs from the entity-body only when a transfer-coding has been
     * applied, as indicated by the Transfer-Encoding header field (section
     * 14.41).
     *
     * @return String with message-body
     */
    public String getMessageBody() {
        return _messagetBody.toString();
    }

    private void appendMessageBody(String bodyLine) {
        _messagetBody.append(bodyLine).append("\r\n");
    }

    /**
     * For list of available headers refer to sections: 4.5, 5.3, 7.1 of RFC
     * 2616
     *
     * @param headerName Name of header
     * @return String with the value of the header or null if not found.
     */
    public String getHeaderParam(String headerName) {
        return _requestHeaders.get(headerName);
    }

    /**
     * additional method from @author Kingsley Motsepe
     *
     * @return
     */
    public String getURL() {
        String requestLine = getRequestLine();
        String requestURL = Arrays.asList(requestLine.split(" ")).get(1);

        return requestURL;
    }

    /**
     *
     * @return
     */
    public Map<String, String> getAllHeaders() {
        return _requestHeaders;
    }

    public HttpGet getAsApacheHttpRequest() {
        //HttpRequest apacheHttpRequest = new HttpRequest();
        HttpGet httpGet = new HttpGet(getURL());
        //Map headers = getAllHeaders();
        HashMap headers = new HashMap();//geAllHeaders();
        headers.putAll(getAllHeaders());

        Iterator it = headers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            httpGet.addHeader(pair.getKey().toString(), pair.getValue().toString());
            //System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }

        return null;
    }
}