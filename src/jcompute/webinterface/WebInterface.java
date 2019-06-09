package jcompute.webinterface;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
public class WebInterface {

	
	@RequestMapping("/")
	String root() {
		return "Hello World!";
	}
	
	@RequestMapping("/test")
	String test() {
		return "Test";
	}

}