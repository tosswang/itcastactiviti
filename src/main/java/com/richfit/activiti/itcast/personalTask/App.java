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
		db.name("审批事宜");// 增加部署的名称
		// 从classpath资源中加载流程定义文件，一次只能加载一个文件
		Deployment dm = db.addClasspathResource("diagram/personalTask/PersonalTask.bpmn")
				.addClasspathResource("diagram/personalTask/PersonalTask.png").deploy();
		// Deployment对应数据表，可以获得他对应的字段dm.getId()指的是部署ID
		System.out.println(dm.getId() + "|" + dm.getName());
	}

	/**
	 * 2.启动流程实例
	 */
	@Test
	public void startProcessInstance() {

		String processKey = "personalTask";
		// 获得正在执行的流程实例和执行对象相关的Service，也是对应后台的数据表
		RuntimeService rs = processEngine.getRuntimeService();
		
		/**
		 * 启动流程实例的同时，设置流程变量，使用流程变量用来指定任务的办理人
		 * 对应流程定义文件中的变量,实际应用中，启动流程实例是要同时制定第一个节点
		 * 任务办理人信息的，例子中大部分都是直接根据流程key启动是因为，节点中
		 * 已经写死了执行人信息的，但像我们的各个流程中，没有什么写死的，必须动态
		 * 指定.其实这个就已经是调用了TaskListenerImpl方法动态指定了办理人
		 * */
		Map<String,Object> var=new HashMap<String,Object>();
		var.put("userId","周芷若" );
		ProcessInstance pi = rs.startProcessInstanceByKey(processKey, var);
		System.out.println(pi.getId() + "||" + pi.getProcessDefinitionId());
	}

	
	/**完成任务**/
	@Test
	public void completeMyTask() {
		// 获得正在执行的任务管理相关的service
		TaskService ts = processEngine.getTaskService();
		String taskId="42506"; 
		ts.complete(taskId);
		System.out.println("完成任务：任务ID："+taskId);		
	}
		
}
/***
 * 利用本例子做了一个试用，TaskListener，这个接口我已开始捆绑（捆绑这个词是我想到的，非常恰当！！！）到了我第一个节点上，
 * 结果发现，当我开始启动流程实例时，他就会自动调用实现类，为第一个节点指定了执行人，
 * 当我取消了他们的绑定关系，启动流程实例时，会按绑定到该节点的变量名去指定执行人（接口绑定优先于变量绑定！！！）；
 * 所以这个执行人是什么和我们绑定在该节点的内容是有关系的，是变量，就从变量中获取，是接口
 * 就从接口中获取；
 * 其他节点也是一样，比如这个例子，当我在第二个节点绑定了该接口，那么，在第一个节点执行人
 * 完成了任务后，即执行了ts.complete方法后，系统会自动跑到第二个节点，流程会自动调用TaskListener
 * 指定执行人！其他三、四、五等节点也是一样
 * **/