package com.ohlon.controller;

import java.util.Map;

import org.json.JSONArray;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

@Controller()
@SessionAttributes("currentServer")
public class MainController extends AbstractController {

	@ModelAttribute("currentServer")
	public Server initializeServer() {
		return new Server();
	}

	@RequestMapping(value = { "", "/", "/login" })
	public ModelAndView login(@RequestParam(value = "error", required = false) String error, @RequestParam(value = "logout", required = false) String logout) {

		ModelAndView model = new ModelAndView();
		model.addObject("displayLoginForm", true);

		if (error != null) {
			model.addObject("error", "Invalid username and password!");
		}

		if (logout != null) {
			model.addObject("msg", "You've been logged out successfully.");
		}

		JSONArray listOfServers = getServerData();
		if (listOfServers == null || listOfServers.length() == 0) {
			model.addObject("error", "Your server configuration file doesn't exist or is empty.");
			model.addObject("displayLoginForm", false);
		}

		model.setViewName("login");

		return model;

	}

	@RequestMapping("/live")
	public ModelAndView live(@RequestParam(value = "id", required = false) String serverId, @ModelAttribute("currentServer") Server currentServer) {
		currentServer.setId(serverId);
		return new ModelAndView("live", generateParams(currentServer.getId()));
	}

	@RequestMapping("/batchclass")
	public ModelAndView batchclass(@RequestParam(value = "id", required = false) String serverId, @ModelAttribute("currentServer") Server currentServer) {
		currentServer.setId(serverId);
		return new ModelAndView("batchclass", generateParams(currentServer.getId()));
	}

	@RequestMapping("/batchinstance")
	public ModelAndView batchinstance(@RequestParam(value = "id", required = false) String serverId, @RequestParam(value = "identifier", required = false) String identifier, @ModelAttribute("currentServer") Server currentServer) {
		currentServer.setId(serverId);
		Map<String, Object> params = generateParams(currentServer.getId());
		params.put("identifier", identifier);
		return new ModelAndView("batchinstance", params);
	}

	@RequestMapping("/reporting")
	public ModelAndView reporting(@RequestParam(value = "id", required = false) String serverId, @ModelAttribute("currentServer") Server currentServer) {
		currentServer.setId(serverId);
		return new ModelAndView("reporting", generateParams(currentServer.getId()));
	}

	@RequestMapping("/user")
	public ModelAndView user(@RequestParam(value = "id", required = false) String serverId, @ModelAttribute("currentServer") Server currentServer) {
		currentServer.setId(serverId);
		return new ModelAndView("user", generateParams(currentServer.getId()));
	}

	@RequestMapping("/error")
	public ModelAndView error(@RequestParam(value = "id", required = false) String serverId, @ModelAttribute("currentServer") Server currentServer) {
		currentServer.setId(serverId);
		return new ModelAndView("error", generateParams(currentServer.getId()));
	}

	@RequestMapping("/serverstatus")
	public ModelAndView serverstatus(@RequestParam(value = "id", required = false) String serverId, @ModelAttribute("currentServer") Server currentServer) {
		currentServer.setId(serverId);
		return new ModelAndView("serverstatus", generateParams(currentServer.getId()));
	}

	/***
	 * LIVE GRAPHS
	 */

	@RequestMapping("/graph/batch-priorities")
	public ModelAndView batchPriorities(@ModelAttribute("currentServer") Server currentServer) {
		return new ModelAndView("/graph/live/batch-priorities", generateParams(currentServer.getId()));
	}

	@RequestMapping("/graph/batchclass")
	public ModelAndView batchclassgraph(@ModelAttribute("currentServer") Server currentServer) {
		return new ModelAndView("/graph/live/batchclass", generateParams(currentServer.getId()));
	}

	@RequestMapping("/graph/batchinstance")
	public ModelAndView batchinstance(@ModelAttribute("currentServer") Server currentServer) {
		return new ModelAndView("/graph/live/batchinstance", generateParams(currentServer.getId()));
	}

	@RequestMapping("/graph/heap-memory-usage")
	public ModelAndView heapMemoryUsage(@ModelAttribute("currentServer") Server currentServer) {
		return new ModelAndView("/graph/live/heap-memory-usage", generateParams(currentServer.getId()));
	}

	@RequestMapping("/graph/error-batch-instances")
	public ModelAndView errorBatchInstances(@ModelAttribute("currentServer") Server currentServer) {
		return new ModelAndView("/graph/live/error-batch-instances", generateParams(currentServer.getId()));
	}

	@RequestMapping("/graph/readyforreview-batch-instances")
	public ModelAndView readyForReviewBatchInstances(@ModelAttribute("currentServer") Server currentServer) {
		return new ModelAndView("/graph/live/readyforreview-batch-instances", generateParams(currentServer.getId()));
	}

	@RequestMapping("/graph/readyforvalidation-batch-instances")
	public ModelAndView readyForValidationBatchInstances(@ModelAttribute("currentServer") Server currentServer) {
		return new ModelAndView("/graph/live/readyforvalidation-batch-instances", generateParams(currentServer.getId()));
	}

	@RequestMapping("/graph/cpu-usage")
	public ModelAndView cpuUsage(@ModelAttribute("currentServer") Server currentServer) {
		return new ModelAndView("/graph/live/cpu-usage", generateParams(currentServer.getId()));
	}

	@RequestMapping("/graph/running-batch-instances")
	public ModelAndView runningBatchInstances(@ModelAttribute("currentServer") Server currentServer) {
		return new ModelAndView("/graph/live/running-batch-instances", generateParams(currentServer.getId()));
	}

	/***
	 * BATCH CLASS GRAPHS
	 */

	@RequestMapping("/graph/batchclass-repartition")
	public ModelAndView batchClassRepartition(@RequestParam(value = "bc", required = false) String bc, @RequestParam(value = "from", required = false) String from,
			@RequestParam(value = "to", required = false) String to, @ModelAttribute("currentServer") Server currentServer) {
		Map<String, Object> params = generateParams(currentServer.getId());
		params.put("bc", bc);
		params.put("from", from);
		params.put("to", to);
		return new ModelAndView("/graph/batchclass/repartition", params);
	}

	@RequestMapping("/graph/batchclass-accumulation")
	public ModelAndView batchClassAccumulation(@RequestParam(value = "bc", required = false) String bc, @RequestParam(value = "from", required = false) String from,
			@RequestParam(value = "to", required = false) String to, @ModelAttribute("currentServer") Server currentServer) {
		Map<String, Object> params = generateParams(currentServer.getId());
		params.put("bc", bc);
		params.put("from", from);
		params.put("to", to);
		return new ModelAndView("/graph/batchclass/accumulation", params);
	}

	@RequestMapping("/graph/batchclass-batchinstances")
	public ModelAndView batchClassBatchInstanceList(@RequestParam(value = "bc", required = false) String bc, @RequestParam(value = "from", required = false) String from,
			@RequestParam(value = "to", required = false) String to, @ModelAttribute("currentServer") Server currentServer) {
		Map<String, Object> params = generateParams(currentServer.getId());
		params.put("bc", bc);
		params.put("from", from);
		params.put("to", to);
		return new ModelAndView("/graph/batchclass/batchinstance", params);
	}

	/***
	 * REPORTING GRAPHS
	 */

	@RequestMapping("/graph/reporting/artifact-accumulation")
	public ModelAndView artifactAccumulation(@RequestParam(value = "bc", required = false) String bc, @RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "name", required = false) String name, @RequestParam(value = "from", required = false) String from, @RequestParam(value = "to", required = false) String to,
			@ModelAttribute("currentServer") Server currentServer) {
		Map<String, Object> params = generateParams(currentServer.getId());
		params.put("bc", bc);
		params.put("type", type);
		params.put("name", name);
		params.put("from", from);
		params.put("to", to);
		return new ModelAndView("/graph/reporting/accumulation", params);
	}

	@RequestMapping("/graph/reporting/artifact-repartition")
	public ModelAndView artifactRepartition(@RequestParam(value = "bc", required = false) String bc, @RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "name", required = false) String name, @RequestParam(value = "from", required = false) String from, @RequestParam(value = "to", required = false) String to,
			@ModelAttribute("currentServer") Server currentServer) {
		Map<String, Object> params = generateParams(currentServer.getId());
		params.put("bc", bc);
		params.put("type", type);
		params.put("name", name);
		params.put("from", from);
		params.put("to", to);
		return new ModelAndView("/graph/reporting/repartition", params);
	}

	/***
	 * USER GRAPHS
	 */

	@RequestMapping("/graph/user/review-accumulation")
	public ModelAndView userReviewAccumulation(@RequestParam(value = "bc", required = false) String bc, @RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "from", required = false) String from, @RequestParam(value = "to", required = false) String to, @RequestParam(value = "user", required = false) String user,
			@ModelAttribute("currentServer") Server currentServer) {
		Map<String, Object> params = generateParams(currentServer.getId());
		params.put("bc", bc);
		params.put("user", user);
		params.put("name", name);
		params.put("from", from);
		params.put("to", to);
		return new ModelAndView("/graph/user/review-accumulation", params);
	}

	@RequestMapping("/graph/user/review-repartition")
	public ModelAndView userReviewRepartition(@RequestParam(value = "bc", required = false) String bc, @RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "from", required = false) String from, @RequestParam(value = "to", required = false) String to, @RequestParam(value = "user", required = false) String user,
			@ModelAttribute("currentServer") Server currentServer) {
		Map<String, Object> params = generateParams(currentServer.getId());
		params.put("bc", bc);
		params.put("user", user);
		params.put("name", name);
		params.put("from", from);
		params.put("to", to);
		return new ModelAndView("/graph/user/review-repartition", params);
	}

	@RequestMapping("/graph/user/validation-accumulation")
	public ModelAndView userValidationAccumulation(@RequestParam(value = "bc", required = false) String bc, @RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "from", required = false) String from, @RequestParam(value = "to", required = false) String to, @RequestParam(value = "user", required = false) String user,
			@ModelAttribute("currentServer") Server currentServer) {
		Map<String, Object> params = generateParams(currentServer.getId());
		params.put("bc", bc);
		params.put("user", user);
		params.put("name", name);
		params.put("from", from);
		params.put("to", to);
		return new ModelAndView("/graph/user/validation-accumulation", params);
	}

	@RequestMapping("/graph/user/validation-repartition")
	public ModelAndView userValidationRepartition(@RequestParam(value = "bc", required = false) String bc, @RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "from", required = false) String from, @RequestParam(value = "to", required = false) String to, @RequestParam(value = "user", required = false) String user,
			@ModelAttribute("currentServer") Server currentServer) {
		Map<String, Object> params = generateParams(currentServer.getId());
		params.put("bc", bc);
		params.put("user", user);
		params.put("name", name);
		params.put("from", from);
		params.put("to", to);
		return new ModelAndView("/graph/user/validation-repartition", params);
	}

}
