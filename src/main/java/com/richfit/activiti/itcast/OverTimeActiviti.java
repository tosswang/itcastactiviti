package com.richfit.activiti.itcast;

import java.util.List;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.junit.Test;

public class OverTimeActiviti {
	/** getDefaultProcessEngine()默认加载了activiti.cfg.xml */
	ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

	/** 1.通过流程引擎部署流程定义 */
	@Test
	public void deploymentProcessDefine() {
		// 与流程定义和部署相关的service
		RepositoryService rs = processEngine.getRepositoryService();
		// 由仓库对象产生部署对象
		DeploymentBuilder db = rs.createDeployment();
		db.name("请假定义");// 增加部署的名称
		// 从classpath资源中加载流程定义文件，一次只能加载一个文件
		Deployment dm = db.addClasspathResource("diagram/MyProcess.bpmn").addClasspathResource("diagram/MyProcess.png")
				.deploy();
		// Deployment对应数据表，可以获得他对应的字段dm.getId()指的是部署ID
		System.out.println(dm.getId() + "|" + dm.getName());
		
		/***部署流程定义，涉及到的数据表包括：
		 * 部署对象表：act_re_deployment，
		 * 流程定义表：act_re_procdef，这两张表通过act_re_procdef表中的DEPLOYMENT_ID_进行关联，
		 * act_re_procdef表中的name字段值对应bpmn
		 * 文件中的<process id="overTime" name="overTimeProcess" isExecutable="true">的name属性值，字段key对应
		 * id属性，act_re_procdef表中的version是在key字段值相同的情况下如果bpmn文件做过多次修改，那么就会产生多个版本;
		 * 资源文件表，act_ge_bytearray,与部署对象表通过DEPLOYMENT_ID_进行关联,存储了流程定义的xml文件和png文件;
		 * act_ge_property，主键生成策略表，其中有一个字段NAME_值 next.dbid，VALUE_字段值为一个数字，这个表示的是部署的下一个
		 * 流程的id即act_re_deployment表中的ID字段值是多少，是自动生成的
		 * **/
	}
	/**对部署的后记补充：除了可以用通过addClasspathResource部署服务，还给有把流程定义bpm和png打包为一个zip文件，
	 * 注意必须是zip，然后加载
	 * **/

	/**
	 * 2.启动流程实例，一般是这样，我们要生成请假表单，然后， 为了让请假信息在流程中流动，需要启动流程实例，
	 * 今天边工告诉了我，请一次假就是启动一个新的流程实例，所以在下面的任务中 打印可见n个不同的流程实例ID
	 * 在我们的项目中，启动流程实例，其实就是通过填写完毕加班申请单后，选择提交按钮，就开启了一个实例，
	 * 本例子是绑定了人，我们的项目应该是绑定了角色吧
	 * 补充：启动的流程是没有办法去删除流程定义的
	 */
	@Test
	public void startProcessInstance() {
		// 获得正在执行的流程实例和执行对象相关的Service，也是对应后台的数据表
		RuntimeService rs = processEngine.getRuntimeService();//这个service主要和act_ru_execution相关,该表最重要的数据就是流程实例ID
		// 使用流程定义的key启动流程实例，这里就是通过加班流程的key启动该流程,而且每次都是取最新版本的流程
		ProcessInstance pi = rs.startProcessInstanceByKey("overTime");
		System.out.println(pi.getId() + "||" + pi.getProcessDefinitionId());
		
		/***启动流程实例，涉及到的表包括：（activiti数据库中的含有ru的表就是正在运行的各类实体，ru代表running）
		 * 1.正在执行的执行对象表：act_ru_execution，在单个流程中（无分支），该表的ID字段（执行对象ID）与流程实例ID字段的值
		 * 是相等的
		 * 其次，ACT_ID活动ID，表示正在执行的活动的ID，对应bpmn文件中的<userTask id="depApproval" ，元素的id。
		 * 2.流程实例历史表：act_hi_procinst，其中END_TIME_为NULL,表示该实例正在运行中；该表的ID（也叫执行对象ID）与
		 * 流程实例ID字段的值是相等的在单个流程中。
		 * 3.正在执行的任务表，act_ru_task（节点为userTask类型的才会在该表中有记录），记录了流程实例ID、当前任务办理人
		 * （ASSIGNEE_）。
		 * 4.任务历史表，act_hi_taskinst（（节点为userTask类型的才会在该表中有记录），通过该表的END_TIME_是否为NULL可知
		 * 该任务是否完成了为NULL表示正在执行的。
		 * 5.所有活动节点历史表，act_hi_actinst（记录不止是userTask类型），只要一开启流程实例，马上该表中就有数据了，
		 * 之前是没有的
		 * */
	}
	/**补充说明：一个流程，流程实例只有一个，执行对象可以有多个，例如在有分支的流程中，
	 * 一个分支就是一个执行对象，2个就是2个执行对象，3个就是3个执行对象（一个执行对象
	 * 在act_ru_execution表中就只有一条记录！！！！！）
	 * **/
	
	
	/** 3.流程启动后，查询当前人的任务情况 ，也就是对应着我们项目中的待办任务查询！！！**/
	@Test
	public void findPersonalTask() {
		// 获得正在执行的任务管理相关的service
		TaskService ts = processEngine.getTaskService();
		// 创建任务查询对象
		TaskQuery tq = ts.createTaskQuery();
		// 指定各人，查询其任务,因为一个人在一个流程引擎中可以对应多个加班申请，所以返回的是列表
		List<Task> ls = tq.taskAssignee("王帅宗").list();

		for (Task task : ls) {
			System.out.println(
					"任务ID：" + task.getId() + "||任务名称：" + task.getName() + "||流程实例ID：" + task.getProcessInstanceId());
		}
		
		/**
		 * 查询任务涉及到的数据表：
		 * 1.正在执行的任务表，act_ru_task。查询当前的任务情况，就是从这张表来查询的，这张表记录了当前流程实例下，
		 * 流程已经流到了要处理任务的人员或角色信息这个节点，流程没走到你那里的话，这个表里是不会记录你的信息的
		 * 
		 * ***/
	}

	/** 查询到任务后就要完成任务，相当于我们填充了加班申请单后，点击提交按钮的操作 ***/
	@Test
	public void completeMyTask() {
		// 获得正在执行的任务管理相关的service
		TaskService ts = processEngine.getTaskService();
		String taskId="10002";
		ts.complete(taskId);
		
		/**
		 * 完成任务涉及到的数据表：
		 * 1.正在执行的任务表，act_ru_task。当完成任务后，该表会删除一条记录！然后加一条需要完成的任务记录，也就是说该表
		 * 不存已完成的任务例如，假设王帅宗审批的吴璠的加班申请，那么王帅宗的任务数据会从该表删除，取而代之的是王琳的
		 * 任务数据插了进来，说明王琳要做任务了。此时查询待办任务时，参数为王帅宗时就什么也查不到，参数为王琳的话就
		 * 可以查到了。带ru的只记录正在执行的内容，切记！！！！！！！执行过得绝对不会出现。
		 * 
		 * 2.正在执行的执行对象表：act_ru_execution。一个节点完成任务后，对应的同一流程实例的记录ACT_ID_字段值会变成当前
		 * 该执行任务的下一节点的key了
		 * （该key对应bpmn中的<userTask id="depApproval" 中的id值）；当所有节点的任务都执行完毕，这里对应的流程实例记录就
		 * 彻底删除了，带ru的只记录正在执行的内容，切记！！！！！！！执行过得绝对不会出现。
		 * 
		 * 3.流程实例历史表：act_hi_procinst。此时，因为整个流程还没完所以字段END_TIME_还是为NULL，表示正在执行的流程实例
		 * 
		 * 4.任务历史表，act_hi_taskinst，此时会产生2条记录，一个是记录了该流程实例下已完成的任务节点，一个是下一个即将执行任务的节点
		 * 
		 * 5.所有活动节点历史表，act_hi_actinst，会在已有基础上增加一条记录（END_TIME_为NULL），对应的是下一个要完成任务的节点
		 * 之前的记录为已完成的节点对应的（END_TIME_有了具体值），
		 * **/
		
	}
	
	/**查询流程状态是执行还是已执行完成**/
	@Test
	public void isProcessEnd() {
		String processInstanceId="2501";
		/**到act_ru_execution正在执行的执行对象表，去查询，如果对应的流程实例id没有结果，说明该流程已经结束了*/
		ProcessInstance pi=processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
		if(pi==null)
		{
			System.out.println("流程已结束");
		}
		else
		{
			System.out.println("流程未结束");
		}
	}
	
	/**查询已办任务**/
	@Test
	public void  findHistoryTask()
	{
		String taskAssignee="吴璠";
		HistoryService hs=processEngine.getHistoryService();
		/**查询当前人的所有已办任务，查询的是act_hi_taskinst表**/
		List<HistoricTaskInstance> ls=hs.createHistoricTaskInstanceQuery().taskAssignee(taskAssignee).finished().list();
		//hs.createHistoricTaskInstanceQuery().taskAssignee(taskAssignee).taskDeleteReason("completed")
		if(ls!=null && ls.size()>0)
		{
			for(HistoricTaskInstance ht:ls)
			{
				System.out.println("任务名称："+ht.getName()+"  开始时间："+ht.getStartTime()+" 结束时间："+ht.getEndTime());
			}
		}
	}
}	
/**
 * 补充：已启动的流程，不能删除流程定义的，非要想重新弄的话，就再画一个新的流程图，然后key（也就是bpmn文件中的id
 * 值）要和已有的启动的流程保持一致，这样再启动新的流程实例时，系统就按新的流程来了（按最新版本来），老的流程就不用了，
 * 但已走过的流程，也不会受影响，老人老办法，新人新办法;
 * 
 * 补充：ProcessDefinition对象，对应的是act_re_procdef流程定义表
 *             ProcessDefinitionEntiy对象，对应的是*.bpmn文件中的内容，要是需要获取流程中连线内容，需要在该对象中寻找
 * **/
