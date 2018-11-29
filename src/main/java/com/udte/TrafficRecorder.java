
package com.udte;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptInitializer;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.intercept.common.FullResponseIntercept;
import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

public class TrafficRecorder {

  public static void main(String[] args) throws Exception {
    System.out.println("started");
    HttpProxyServerConfig config = new HttpProxyServerConfig();
    config.setHandleSsl(true);
    new HttpProxyServer().serverConfig(config).proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
      @Override
      public void init(HttpProxyInterceptPipeline pipeline) {
        pipeline.addLast(new FullResponseIntercept() {

          @Override
          public boolean match(HttpRequest httpRequest, HttpResponse httpResponse,
              HttpProxyInterceptPipeline pipeline) {
            // 在匹配到百度首页时插入js
            // return HttpUtil.checkUrl(pipeline.getHttpRequest(), "^www.baidu.com$")
            // && isHtml(httpRequest, httpResponse);
            return true;
          }

          @Override
          public void handelResponse(HttpRequest httpRequest, FullHttpResponse httpResponse,
              HttpProxyInterceptPipeline pipeline) {
            // 打印原始响应信息
            // System.out.println(httpResponse.toString());
            // System.out.println(httpResponse.content().toString(Charset.defaultCharset()));
            // 修改响应头和响应体
            // httpResponse.headers().set("handel", "edit head");
            /*
             * int index = ByteUtil.findText(httpResponse.content(), "<head>");
             * ByteUtil.insertText(httpResponse.content(), index,
             * "<script>alert(1)</script>");
             */
            // httpResponse.content().writeBytes("<script>alert('hello proxyee')</script>".getBytes());
            

            String host = httpRequest.headers().get("Host");
            String uri = httpRequest.getUri();
            if("/".equals(uri)){
              uri = "/index.html";
            }
            
            int size = httpResponse.content().readableBytes();
            byte[] res = new byte[size];
            
            if (size ==0 || res.equals(""))
              return;// 判断输入的byte是否为空
            try {
              File path = new File("cached/" + host.replace(".", "/").replace(":", "/") + "/" + uri);
              File fileParent = path.getParentFile();
              fileParent.mkdirs();
              
              FileOutputStream fileOutputStream = new FileOutputStream(path);// 打开输入流
              try{
                FileChannel localfileChannel = fileOutputStream.getChannel();
                ByteBuffer byteBuffer = httpResponse.content().nioBuffer();
                int writen = 0;
                while(writen < size){
                  writen += localfileChannel.write(byteBuffer);
                }
                localfileChannel.force(false);
              }finally{
                fileOutputStream.close();
              }
              
            } catch (Exception ex) {
              System.out.println("Exception: " + ex);
              // ex.printStackTrace();
            }
          }
        });
      }
    }).start(9999);
  }
}
