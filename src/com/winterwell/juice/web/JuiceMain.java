package com.winterwell.juice.web;

import com.winterwell.juice.JuiceConfig;
import com.winterwell.web.app.AMain;
import com.winterwell.web.app.JettyLauncher;
import com.winterwell.web.app.MasterServlet;

public class JuiceMain extends AMain<JuiceConfig> {

	public JuiceMain() {	
		super("juice",JuiceConfig.class);
	}
	
	public static void main(String[] args) {
		JuiceMain amain = new JuiceMain();
		amain.doMain(args);
	}
	
	@Override
	protected void addJettyServlets(JettyLauncher jetty) {
		super.addJettyServlets(jetty);
		MasterServlet ms = jetty.addMasterServlet();
		ms.addServlet("juice", JuiceServlet.class);
		ms.addServlet("xray", XrayServlet.class);
	}
}
