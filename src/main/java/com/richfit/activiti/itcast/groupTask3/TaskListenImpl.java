package com.richfit.activiti.itcast.groupTask3;

import java.util.ArrayList;
import java.util.Collection;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

public class TaskListenImpl implements TaskListener {

	/**通过该方法，可以指定任务的办理人**/
	@Override
	public void notify(DelegateTask delegateTask) {
		
		Collection<String> candidateUsers=new ArrayList<String>();
		candidateUsers.add("桐玉");
		candidateUsers.add("志强");
		candidateUsers.add("振东");
		delegateTask.addCandidateUsers(candidateUsers);		
	}

}
