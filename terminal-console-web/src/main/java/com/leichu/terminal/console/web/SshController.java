package com.leichu.terminal.console.web;

import com.alibaba.fastjson.JSONObject;
import com.leichu.terminal.console.interactive.model.AuthParam;
import com.leichu.terminal.console.interactive.model.Protocol;
import com.leichu.terminal.console.interactive.model.Vendor;
import com.leichu.terminal.console.service.InteractiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/ssh")
public class SshController {

	private static final Logger logger = LoggerFactory.getLogger(SshController.class);

	@Resource
	private InteractiveService interactiveService;

	@PostMapping("/auth")
	public JSONObject auth(@RequestBody JSONObject jsonParam) {
		logger.info("Auth:{}", jsonParam.toJSONString());
		Protocol protocol = Protocol.match(jsonParam.getString("protocol"));
		Vendor vendor = Vendor.match(jsonParam.getString("vendor"));
		AuthParam param = JSONObject.parseObject(jsonParam.toJSONString(), AuthParam.class);
		return interactiveService.auth(param, protocol, vendor);
	}

}
