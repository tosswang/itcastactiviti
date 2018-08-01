package com.richfit.activiti.itcast;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.junit.Test;

/**
 * Hello world!
 *
 */
public class TestActiviti {
	@Test
	public void createTable() {
		ProcessEngine processEngine = ProcessEngineConfiguration
				.createProcessEngineConfigurationFromResource("activiti.cfg.xml").buildProcessEngine();
		System.out.println("流程引擎："+processEngine);
	}
}
