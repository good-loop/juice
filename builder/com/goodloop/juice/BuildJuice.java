package com.goodloop.juice;

import java.util.List;

import com.winterwell.bob.BuildTask;
import com.winterwell.bob.tasks.MavenDependencyTask;
import com.winterwell.bob.wwjobs.BuildWinterwellProject;


public class BuildJuice extends BuildWinterwellProject {

	public BuildJuice() {
		super("juice");
// TODO	add {project-dir}/src/?java to the classpath	setVersion(JuiceConfig.version); //"1.0.0");
		setVersion("1.1.0"); // 15 Jul 2021
	}
	
	@Override
	public List<BuildTask> getDependencies() {
		List<BuildTask> deps = super.getDependencies();
		
		MavenDependencyTask mdt = new MavenDependencyTask();
		mdt.setIncSrc(true);
		mdt.addDependency("org.jsoup", "jsoup", "1.14.1");
		mdt.addDependency("io.github.fanyong920", "jvppeteer", "1.1.3"); // java client for puppeteer
//		mdt.addDependency("org.apache.commons:commons-compress:1.20"); // dependency of jvppeteer

//		mdt.setSkipGap(null); // debug - force a rerun
		deps.add(mdt);
		
		return deps;
	}

}
