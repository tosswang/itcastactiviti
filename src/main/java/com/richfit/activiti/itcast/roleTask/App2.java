package com.richfit.activiti.itcast.roleTask;

import java.util.List;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.junit.Test;

/**
 * 不管是按组也好，角色也好，流程任务的执行总是终归要由具体人来完成，所以claim方法总是需要的
 * 
 * 重点啊，一般情况下，每个userTask指定的Lisnter啊、Assign啊、变量啊都是为了指定当前由谁来完成本节点的任务！！！
 * 注意是本节点！！！！
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
		db.name("角色任务");// 增加部署的名称
		// 从classpath资源中加载流程定义文件，一次只能加载一个文件
		Deployment dm = db.addClasspathResource("diagram/roleTask/RoleTaskProcess.bpmn")
				.addClasspathResource("diagram/roleTask/RoleTaskProcess.png").deploy();
		// Deployment对应数据表，可以获得他对应的字段dm.getId()指的是部署ID
		System.out.println(dm.getId() + "|" + dm.getName());
		/**添加用户角色组**/
		IdentityService is=processEngine.getIdentityService();
		/**创建角色，对应数据表act_id_group(角色表)中生成数据
		 * 
		 * 这里必须切记一点，我们创建的角色，必须和  <userTask id="roleTask" name="审批" activiti:candidateGroups="部门长">
		 * 中ctiviti:candidateGroups的值保持一致！！！！因为之后的任务分配就是按用于该角色的用户分配的！！！！！
		 * **/
		is.saveGroup(new GroupEntity("秘书长"));
		is.saveGroup(new GroupEntity("部门长"));
		/**创建用户，对应数据表act_id_user（用户表）中生成数据*/
		is.saveUser(new UserEntity("段永刚"));
		is.saveUser(new UserEntity("王帅宗"));
		is.saveUser(new UserEntity("李铁刚"));
		/**角色用户挂钩，对应数据表act_id_membership（角色-用户关联关系表）中生成数据***/
		is.createMembership("段永刚","秘书长");
		is.createMembership("王帅宗","部门长");
		is.createMembership("李铁刚","部门长");
		
		System.out.println("角色用户完成勾连");
	}

	/**
	 * 2.启动流程实例，启动后ru_task表中，Assign_字段对应为空，说明要么没有分配任务，要么是一个组任务
	 * 在表act_ru_identitylink可查询到人；对于对角色分配的任务，它和多人组任务还不同，多人组任务在该表中
	 * 每一个人2条记录，TYPE_字段对应的是参与者与候选者，USER_ID的值是组中每一个人名字。而角色任务不是
	 * 这样，在表act_ru_identitylink中只有一条记录（记录了正在执行任务的角色，还有其他角色但他们现在不干活所以不记录），
	 * 在TYPE_字段对应的是候选者，而且USER_ID值为NULL，只在GROUP_ID中有角色的名字。
	 */
	@Test
	public void startProcessInstance() {

		String processKey = "RoleTasks";
		RuntimeService rs = processEngine.getRuntimeService();
		ProcessInstance pi = rs.startProcessInstanceByKey(processKey);
		System.out.println(pi.getId() + "||" + pi.getProcessDefinitionId());
	}
	
	/**
	 * 角色分配的任务，其实就是另一种组任务，可以把角色理解成组，所以
	 * 该组中的人都可以查组任务，非该组中的人就不行了
	 * **/
	@Test
	public void findMyGroupTask()
	{
		String candidateUser="王帅宗";
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
		String taskId="20004";
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
		String taskId="20004";
		String userId="李铁刚";
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
		String taskId="2504";
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
		String taskId="2504";
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
		String taskId="2504";
		ts.complete(taskId);
		System.out.println("完成任务：任务ID："+taskId);
		
	}
	
	
	
}
