package com.winterwell.juice.web;

import com.winterwell.juice.JuiceConfig;
import com.winterwell.web.app.AMain;

public class JuiceMain extends AMain<JuiceConfig> {

	public JuiceMain() {	
		super("juice",JuiceConfig.class);
	}
	
	public static void main(String[] args) {
		JuiceMain amain = new JuiceMain();
		amain.doMain(args);
	}
}
