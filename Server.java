import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.OutputStream;
import java.io.Serializable;
/*
//需要引入外部依赖jackson
//maven只有本地依赖，没有cdn方式引入，因为后端要确保高可用，不能依赖外部网络资源
//maven是全局级别的依赖管理，npm是项目级别的依赖管理，所以jackson要放在项目外的单独文件夹
*databind是高级模块，需要依赖基础模块core和annotations，所以需要下载三个jar包
//编译和运行时都要明确指定这三个依赖路径
*/
import com.fasterxml.jackson.databind.ObjectMapper;

public class Server{
  /*
  *在java中声明一个对象数组，要先建立对象的模板
  *类是对象的模板，对象是类的实例，每个对象在new的时候分配内存空间，状态独立
  *内部非static类依赖外部类的实例，如果外部类不支持序列化，那么内部类也不能序列化
  *所以在main方法外创建内部static类，并表示可以被序列化
  */
  public static class Anime implements Serializable{
    //类和构造方法是公开的，成员变量是私有的
    //无参构造方法，有参构造方法，setter和getter一个都不能省略，怪不得lombok这么流行...
    private String img;
    private String name;
    private String desc;
    
    public Anime(){}
    public Anime(String img,String name,String desc){
      this.img=img;
      this.name=name;
      this.desc=desc;
    }
    public String getImg(){
      return img;
    }
    public void setImg(String img){
      this.img=img;
    }
    public String getName(){
      return name;
    }
    public void setName(String name){
      this.name=name;
    }
    public String getDesc(){
      return desc;
    }
    public void setDesc(String desc){
      this.desc=desc;
    }
  }
  /*
  //先用javac命令把源码编译成字节码，JVM才能看懂
  //再用java命令调用JVM运行字节码
  //JVM会从main方法开始执行
  */
  public static void main(String[] args) throws IOException{
    /*
    *1.创建服务器实例
    *服务器监听所有网络接口的8040，请求队列的最大长度使用默认值(0代表默认值)
    *因为我引入的HttpServer类内部抛出了IOException，所以我必须抛出或者处理这个异常
    */
    HttpServer server=HttpServer.create(new InetSocketAddress(8040),0);
    /*
    //2.设置服务器对于不同路径的响应逻辑
    //为不同接口分别新建处理器实例，并且重写处理器内部的handle方法
    //handle方法的参数类型也需要import
    */
    server.createContext("/game",new HttpHandler(){
      @Override
      public void handle(HttpExchange exchange)throws IOException{
        /*
        //exchange:一次请求和响应的交换过程
        //要定义三个东西:响应码，内容长度，内容
        //响应码和内容长度放在响应头里，内容放在响应体里
        */
        String res="111";
        exchange.sendResponseHeaders(200,res.getBytes().length);
        /*
        //内容需要用流来读取，对于java程序是向外的，所以用输出流
        //往哪流
        */
        OutputStream os=exchange.getResponseBody();
        //流什么
        os.write(res.getBytes());
        //所有的流都要记得用完关闭
        os.close();
      }
    });
    
    //第二个接口
    //这里请求/anime /anime222 /anime/game都能匹配到这个接口
    server.createContext("/anime",new HttpHandler(){
      @Override
      public void handle(HttpExchange exchange)throws IOException{
        /*
        //配置服务端跨域设置
        //一个域名对应的是ip+端口，所以相同ip不同端口要用到两个域名
        //前后端虽然都在本地，但是端口不同，前端访问后端属于跨域
        //在服务端响应头中配置允许领域外的前端ip+端口访问
        */
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin","http://127.0.0.1:8000");
        //如果fetch请求是put请求或者请求参数是json类型的，都属于复杂请求，会多一个预检的请求，服务端响应头还要多配置两项
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods","GET,POST,PUT,DELETE,OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers","Content-Type");
        //最后处理下预检请求，返回空数据
        if("OPTIONS".equals(exchange.getRequestMethod())){
          exchange.sendResponseHeaders(200,-1);
          //预检请求不走后续构造响应流程
          return;
        }
        
        //用模板建立对象数组
        Anime[] animes={
          new Anime("11","111","11111"),
          new Anime("22","222","22222"),
        };
        //将对象数组序列化为json字符串，引入jackson的objectMapper类
        //实例化转换工具
        ObjectMapper om=new ObjectMapper();
        //用工具转换对象数组
        String res=om.writeValueAsString(animes);
        exchange.sendResponseHeaders(200,res.getBytes().length);
        OutputStream os=exchange.getResponseBody();
        os.write(res.getBytes());
        os.close();
      }
    });
    
    //3.设置服务器是否能够处理并发请求
    server.setExecutor(null);
    
    //4.启动服务器
    server.start();
    
    //java中char用单引号，字符串用双引号，和js不一样
    System.out.println("服务器启动成功，监听8040");
  }
}