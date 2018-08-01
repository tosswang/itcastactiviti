package com.richfit.activiti.itcast.bingxinggateway;

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
		db.name("人力相关申报审批");// 增加部署的名称
		// 从classpath资源中加载流程定义文件，一次只能加载一个文件
		Deployment dm = db.addClasspathResource("diagram/bingxinggateway/parallelsequflow.bpmn")
				.addClasspathResource("diagram/bingxinggateway/parallelsequflow.png").deploy();
		// Deployment对应数据表，可以获得他对应的字段dm.getId()指的是部署ID
		System.out.println(dm.getId() + "|" + dm.getName());
	}

	@Test
	public void startProcessInstance() {

		String processKey ="hrAffairSp";
		RuntimeService rs = processEngine.getRuntimeService();
		ProcessInstance pi = rs.startProcessInstanceByKey(processKey);
		System.out.println(pi.getId() + "||" + pi.getProcessDefinitionId());
	}
	
	/**完成任务**/
	@Test
	public void completeMyTask() {
		TaskService ts = processEngine.getTaskService();
		String taskId="72502";
		ts.complete(taskId);
		System.out.println("完成任务：任务ID："+taskId);
		
	}
	
	/**笔记：
	 * 
	 * 并行网关，有一个开头，就绝对会成对出现一个结尾，就和大括号一样，有一个左就必定有一个右！！！！！先这么理解！！！
	 * 
	 * 观察act_hi_taskinst表，可见在一条支线上的所有任务，在历史任务表的EXECUTION_ID_字段对应的value是一样的！
	 * 观察act_ru_execution表，可见只要任务没完成总有并行网关的id值，这是因为，只要存在分支，那整个任务必须在
	 * 所有分支结束后才能结束，所以并行网管的值一直存在；
	 * 
	 * 再次强调，不论是否分支，一个流程中流程实例只有一个，但执行对象可以有多个；
	 * 还有如果并行网关有汇聚和分支两个功能的话，那么必须在汇聚点等待所有分支都到达后，才会进行分支！！！切记这一点！！！！
	 * 先汇聚再分发！！！！！！！类似于先头部队等到后续部队后，才会再次进发。
	 * 并行网关不会解析条件，也就是说在连线上设置条件、变量毫无意义。
	 * 并行网关各个分支上的任务节点个数是可以不相等的，但终究会汇合到一个并行网关节点！！
	 * */
	
}
