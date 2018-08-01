package com.richfit.activiti.itcast.paitagateway;

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

	/** 1.通过流程引擎部署流程定义 */
	@Test
	public void deploymentProcessDefine() {
		// 与流程定义和部署相关的service
		RepositoryService rs = processEngine.getRepositoryService();
		// 由仓库对象产生部署对象
		DeploymentBuilder db = rs.createDeployment();
		db.name("费用报销");// 增加部署的名称
		// 从classpath资源中加载流程定义文件，一次只能加载一个文件
		Deployment dm = db.addClasspathResource("diagram/paitagateway/exclusivesequflow.bpmn")
				.addClasspathResource("diagram/paitagateway/exclusivesequflow.png").deploy();
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

		String processKey = "feelreimburse";
		RuntimeService rs = processEngine.getRuntimeService();
		ProcessInstance pi = rs.startProcessInstanceByKey(processKey);
		System.out.println(pi.getId() + "||" + pi.getProcessDefinitionId());
	}
		
	/**完成任务**/
	@Test
	public void completeMyTask() {
		// 获得正在执行的任务管理相关的service
		TaskService ts = processEngine.getTaskService();
		String taskId="35005";
		//完成任务的同时，设置流程变量(其中对应的key为<conditionExpression后的<![CDATA[${budget<5}]])，使用流程变量来指定
		//完成当前任务后，接下来该走哪一个支流（这里的连线）
		Map<String,Object> variables=new HashMap<String,Object>();
		 variables.put("money", 200);
		ts.complete(taskId, variables);//注意这里，这个流程参数的传递，就这么写，不用考虑他应该在哪一个节点上写，最后一个节点也行！当然如果是最后一个节点，流程变量就不用再设置了！！！！
		System.out.println("完成任务：任务ID："+taskId);
		
	}
	
}
