package com.richfit.activiti.itcast.receiveTask;

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


public class App {
	/** getDefaultProcessEngine()默认加载了activiti.cfg.xml */
	ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

	/** 1.通过流程引擎部署流程定义
	 **  2.receiveTask和UserTask最大区别是，后者可以指定执行人，而receiveTask没有 */
	@Test
	public void deploymentProcessDefine() {
		// 与流程定义和部署相关的service
		RepositoryService rs = processEngine.getRepositoryService();
		// 由仓库对象产生部署对象
		DeploymentBuilder db = rs.createDeployment();
		db.name("考勤汇总");// 增加部署的名称
		// 从classpath资源中加载流程定义文件，一次只能加载一个文件
		Deployment dm = db.addClasspathResource("diagram/receiveTask/receiveTask.bpmn")
				.addClasspathResource("diagram/receiveTask/receiveTask.png").deploy();
		// Deployment对应数据表，可以获得他对应的字段dm.getId()指的是部署ID
		System.out.println(dm.getId() + "|" + dm.getName());
	}

	/**
	 * 2.启动流程实例，刚才做了一个测试，一个流程实例结束后，
	 * 可以再次执行该方法，启动新的流程实例，新流程实例用的
	 * 流程定义还是原来的，除非重新执行上面的deploymentProcessDefine()
	 * 方法，此时会有新的版本生成
	 */
	@Test
	public void startProcessInstance() {

		String processKey ="infoSend";
		// 获得正在执行的流程实例和执行对象相关的Service，也是对应后台的数据表
		/***
		 * 注意这里
		 * */
		RuntimeService rs = processEngine.getRuntimeService();
		ProcessInstance pi = rs.startProcessInstanceByKey(processKey);
		System.out.println(pi.getId() + "||" + pi.getProcessDefinitionId());
		
		/**获取执行对象ID*/
		String exeId=processEngine.getRuntimeService().createExecutionQuery()
								.processInstanceId(pi.getId()).activityId("receivetask1")
								.singleResult().getId();
		/**使用流程变量设置前日考勤状况，用来传递业务数据**/
		 processEngine.getRuntimeService().setVariable(exeId, "汇总前日考勤数据","100出勤，100缺勤，70出差");
		 
		 /**向后执行一步，如果流程处于等待状态，使得流程继续执行**/
		 processEngine.getRuntimeService().signal(exeId);
		 
		 /**从流程变量中获取汇总当日销售额的值**/
		 
		 /***这个例子没有做完，根据教程所说，这个ReciverTask一般用在不人为干涉的业务流程！！！！比如机器自己去跑一个流程
		  * 此时不会再ru_task表中产生数据，流程的驱动前行都是靠代码来完成的
		  * **/
	}
	
	/**完成任务**/
	@Test
	public void completeMyTask() {
		// 获得正在执行的任务管理相关的service
		TaskService ts = processEngine.getTaskService();
		String taskId="72502";
		ts.complete(taskId);
		System.out.println("完成任务：任务ID："+taskId);
		
	}
	
}
