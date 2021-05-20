package com.goodloop.juice;

import java.io.File;
import java.util.List;

import com.winterwell.bob.BuildTask;
import com.winterwell.bob.tasks.MavenDependencyTask;
import com.winterwell.bob.wwjobs.BuildWinterwellProject;
import com.winterwell.utils.io.FileUtils;

public class BuildJuice extends BuildWinterwellProject {

	public BuildJuice() {
		super("juice");
		setVersion("1.0.0"); // 22 Mar 2021
//		setScpToWW(true);
	}
	
	@Override
	public List<BuildTask> getDependencies() {
		List<BuildTask> deps = super.getDependencies();
		
		MavenDependencyTask mdt = new MavenDependencyTask();
		mdt.setIncSrc(true);
		mdt.addDependency("org.jsoup", "jsoup", "1.13.1");
		mdt.addDependency("io.github.fanyong920", "jvppeteer", "1.1.3"); // java client for puppeteer
//		mdt.addDependency("org.apache.commons:commons-compress:1.20"); // dependency of jvppeteer

//		mdt.setSkipGap(null); // debug - force a rerun
		deps.add(mdt);
		
		return deps;
	}

}
