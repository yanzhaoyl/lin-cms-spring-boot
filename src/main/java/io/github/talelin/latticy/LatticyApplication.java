package io.github.talelin.latticy;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author pedro@TaleLin
 */
@RestController
@MapperScan(basePackages = {"io.github.talelin.latticy.mapper"})
@SpringBootApplication(scanBasePackages = {"io.github.talelin.latticy"})
public class LatticyApplication {

    public static void main(String[] args) {
        SpringApplication.run(LatticyApplication.class, args);
    }

    @RequestMapping("/")
    public String index() {
        return "<style type=\"text/css\">*{ padding: 0; margin: 0; } div{ padding: 4px 48px;} a{color:#2E5CD5;cursor:" +
                "pointer;text-decoration: none} a:hover{text-decoration:underline; } body{ background: #fff; font-family:" +
                "\"Century Gothic\",\"Microsoft yahei\"; color: #333;font-size:18px;} h1{ font-size: 100px; font-weight: normal;" +
                "margin-bottom: 12px; } p{ line-height: 1.6em; font-size: 42px }</style><div style=\"padding: 24px 48px;\"><p>" +
                "Lin <br/><span style=\"font-size:30px\">心上无垢，林间有风。</span></p></div> ";
    }
    
    
    
 	// http访问自动转https访问
 	// https://www.jianshu.com/p/8d4aba3b972d			单项证书
 	// https://www.cnblogs.com/htuao/p/10091458.html	双向证书
 	@Bean
 	public Connector connector() {
 		Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
 		connector.setScheme("http");
 		connector.setPort(80);
 		connector.setSecure(false);
 		connector.setRedirectPort(5000);
 		return connector;
 	}

 	@Bean
 	public TomcatServletWebServerFactory tomcatServletWebServerFactory(Connector connector) {
 		TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
 			@Override
 			protected void postProcessContext(Context context) {
 				SecurityConstraint securityConstraint = new SecurityConstraint();
 				securityConstraint.setUserConstraint("CONFIDENTIAL");
 				SecurityCollection collection = new SecurityCollection();
 				collection.addPattern("/*");
 				securityConstraint.addCollection(collection);
 				context.addConstraint(securityConstraint);
 			}
 		};
 		tomcat.addAdditionalTomcatConnectors(connector);
 		return tomcat;
 	}
    
}
