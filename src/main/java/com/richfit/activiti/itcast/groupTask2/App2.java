package com.richfit.activiti.itcast.groupTask2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.junit.Test;


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
		db.name("组任务");// 增加部署的名称
		// 从classpath资源中加载流程定义文件，一次只能加载一个文件
		Deployment dm = db.addClasspathResource("diagram/groupTask2/MyGroupTaskProcess.bpmn")
				.addClasspathResource("diagram/groupTask2/MyGroupTaskProcess.png").deploy();
		// Deployment对应数据表，可以获得他对应的字段dm.getId()指的是部署ID
		System.out.println(dm.getId() + "|" + dm.getName());
	}

	/**
	 * 2.启动流程实例
	 */
	@Test
	public void startProcessInstance() {

		String processKey = "groupProcessTask2";
		Map<String,Object> param=new HashMap<String,Object>();
		param.put("userIds","满仓,宫硕,李康");
		RuntimeService rs = processEngine.getRuntimeService();
		ProcessInstance pi = rs.startProcessInstanceByKey(processKey, param);
		System.out.println(pi.getId() + "||" + pi.getProcessDefinitionId());
	}
	
	@Test
	public void findMyGroupTask()
	{
		String candidateUser="宫硕";
		/**taskCandidateUser方法，查的就是act_ru_identitylink表中类型为Candidate的成员*/
		List<Task> list=processEngine.getTaskService().createTaskQuery().taskCandidateUser(candidateUser).list();
		for(Task tk:list)
		{
			System.out.println("任务名称："+tk.getName());
			System.out.println("任务办理人："+tk.getAssignee());
			System.out.println("流程实例ID："+tk.getProcessInstanceId());
		}
	}
	
	/**查询正在执行的任务办理人表**/
	@Test
	public void findPersonTask()
	{
		String taskId="17506";
		/**用如下方法查询到的是作为候选人（candidate）的所有人员**/
		List<IdentityLink> ls=processEngine.getTaskService().getIdentityLinksForTask(taskId);
		for(IdentityLink ilk:ls)
		{
			System.out.println("人选："+ilk.getUserId()+ "   类型: "+ilk.getType());
		}
	}
	
	/**将组任务分给个人**/
	@Test
	public void claim()
	{
		String taskId="17506";
		String userId="满仓";
		//将组任务分配给个人。分配的人可以是组中成员，也可以是非组中成员
		processEngine.getTaskService().claim(taskId, userId);
		/**
		 * 我感觉用processEngine.getTaskService().setAssign方法也可以的
		 * */
	}
	/**将个人任务会退给组任务*/
	@Test
	public void  unclaim()
	{
		String taskId="17506";
		processEngine.getTaskService().unclaim(taskId);
		/**
		 * 也可以用
		 * processEngine.getTaskService().setAssign(taskId,null)实现相同的办法
		 * **/
	}
	/**向组中新增成员**/
	@Test
	public void addGroup()
	{
		String taskId="17506";
		String userId="贾福星";
		processEngine.getTaskService().addCandidateUser(taskId, userId);
	}
	
	/**从组中删除成员**/
	@Test
	public void delGroup()
	{
		String taskId="2504";
		String userId="段永刚";
		processEngine.getTaskService().deleteCandidateUser(taskId, userId);
	}
	
	
	/**完成任务**/
	@Test
	public void completeMyTask() {
		// 获得正在执行的任务管理相关的service
		TaskService ts = processEngine.getTaskService();
		String taskId="17506";
		ts.complete(taskId);
		System.out.println("完成任务：任务ID："+taskId);
		
	}
	
	
	
}
