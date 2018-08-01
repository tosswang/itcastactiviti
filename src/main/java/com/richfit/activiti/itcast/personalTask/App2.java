package com.richfit.activiti.itcast.personalTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Test;

/**
 * 流程变量的作用： 1.可以用来传递业务参数，比如我们的加班流程，加班表单里需要填写加班日期、时间、原因，这些都是加班参数
 * 并且不同流程实例，变量是不一样的； 2.可以指定连线完成任务（流程中的同意或拒绝） 3.动态指定任务办理人
 */
public class App2 {
	/** getDefaultProcessEngine()默认加载了activiti.cfg.xml */
	ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

	/** 1.通过流程引擎部署流程定义 */
	@Test
	public void deploymentProcessDefine() {
		// 与流程定义和部署相关的service
		RepositoryService rs = processEngine.getRepositoryService();
		// 由仓库对象产生部署对象
		DeploymentBuilder db = rs.createDeployment();
		db.name("审批事宜");// 增加部署的名称
		// 从classpath资源中加载流程定义文件，一次只能加载一个文件
		Deployment dm = db.addClasspathResource("diagram/personalTask2/PersonalTask.bpmn")
				.addClasspathResource("diagram/personalTask2/PersonalTask.png").deploy();
		// Deployment对应数据表，可以获得他对应的字段dm.getId()指的是部署ID
		System.out.println(dm.getId() + "|" + dm.getName());
	}

	/**
	 * 2.启动流程实例
	 */
	@Test
	public void startProcessInstance() {

		String processKey = "personalTask2";
		// 获得正在执行的流程实例和执行对象相关的Service，也是对应后台的数据表
		RuntimeService rs = processEngine.getRuntimeService();
		//这里回去寻找定义的类中返回的任务办理人
		ProcessInstance pi = rs.startProcessInstanceByKey(processKey);
		System.out.println(pi.getId() + "||" + pi.getProcessDefinitionId());
	}

	
	/**完成任务**/
	@Test
	public void completeMyTask() {
		// 获得正在执行的任务管理相关的service
		TaskService ts = processEngine.getTaskService();
		String taskId="2504";
		ts.complete(taskId);
		System.out.println("完成任务：任务ID："+taskId);
		
	}
	
	/**分配任务从一个人到另一个人，比如审批考勤修改从王东玉到吕祎文,常用操作**/
	@Test
	public void setAssigneeTask() {
		// 获得正在执行的任务管理相关的service
		TaskService ts = processEngine.getTaskService();
	    String taskId="";
	    String userId="";
	    ts.setAssignee(taskId, userId);	
		System.out.println("完成任务：任务ID："+taskId);
		
	}
	
}
