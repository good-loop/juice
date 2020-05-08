package com.goodloop.juice;

import java.io.File;
import java.util.List;

import com.winterwell.bob.BuildTask;
import com.winterwell.bob.tasks.MavenDependencyTask;
import com.winterwell.bob.wwjobs.BuildWinterwellProject;
import com.winterwell.utils.io.FileUtils;

public class BuildJuice extends BuildWinterwellProject {

	public BuildJuice() {
		super(new File(FileUtils.getWinterwellDir(), "juice"), "juice");
//		setScpToWW(true);
	}
	
	@Override
	public List<BuildTask> getDependencies() {
		List<BuildTask> deps = super.getDependencies();
		
		MavenDependencyTask mdt = new MavenDependencyTask();
		mdt.addDependency("org.jsoup", "jsoup", "1.13.1");
		deps.add(mdt);
		
		return deps;
	}

}
