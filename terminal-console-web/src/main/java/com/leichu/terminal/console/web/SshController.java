package com.leichu.terminal.console.web;

import com.alibaba.fastjson.JSONObject;
import com.leichu.terminal.console.service.SshService;
import com.leichu.terminal.console.service.ssh.SshAuthParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/ssh")
public class SshController {

	@Resource
	private SshService sshService;

	@PostMapping("/auth")
	public JSONObject auth(@RequestBody SshAuthParam param) {
		return sshService.auth(param);
	}

}
