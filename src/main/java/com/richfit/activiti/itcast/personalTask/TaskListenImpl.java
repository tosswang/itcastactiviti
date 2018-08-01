package com.richfit.activiti.itcast.personalTask;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

public class TaskListenImpl implements TaskListener {

	/**通过该方法，可以指定任务的办理人**/
	@Override
	public void notify(DelegateTask delegateTask) {
			/**指定个人任务的办理人也可以指定组任务的办理人**/
		/**在这里可以加入数据库查询语句，获取下一个节点的任务办理人，就是在数据库中
		 * 指定一张表，可以把当前登录人的上级找到，然后指定到下面语句的方法参数中
		 * 即可
		 * **/
		System.out.println("AA");
		delegateTask.setAssignee("张无忌");
	}

}
