package br.com.b3.digsta.dbs.server.request.commands;

import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.com.b3.digsta.dbs.network.CommandControl;
import br.com.b3.digsta.dbs.network.CommandUnit;

@RestController
@RequestMapping("/command")
public class CommandRequest {

	@Autowired
	private CommandControl commandControl;

	@Value("${um.dois}")
	private String x;
	
	@RequestMapping(value = "/run", method = RequestMethod.POST)
    public Response run(@RequestBody CommandParameters params) {
		
		CommandUnit comm = commandControl.run(params.getName(), params.getArguments());
		return Response.status(200).entity(comm).build();
    }
	
	@RequestMapping(value = "/status/{id}", method = RequestMethod.GET)
    public CommandUnit getStatus(@PathVariable("id") String id) {
//		System.out.println(id);
//		System.out.println(x);
//		return commandControl.getStatus(id);
		return commandControl.getInfo(id);
    }
	
	
}
