/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package com.hp.hpl.loom.manager.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.hp.hpl.loom.manager.adapter.ActionTarget;
import com.hp.hpl.loom.manager.adapter.AdapterManager;

@Component
public class ActionExecutor {
    @SuppressWarnings("unused")
    @Autowired
    private AdapterManager adapterManager;

    private static final Log LOG = LogFactory.getLog(ActionExecutor.class);

    private TaskExecutor taskExecutor;

    private class ActionTask implements Runnable {

        private ActionTarget actionTarget;

        public ActionTask(final ActionTarget actionTarget) {
            this.actionTarget = actionTarget;
        }

        @Override
        public void run() {
            if (LOG.isInfoEnabled()) {
                if (actionTarget.getAction() != null) {
                    LOG.info("Run action: " + actionTarget.getAction().getName() + " "
                            + actionTarget.getActionResult().getId());
                }
            }
            actionTarget.doAction();
        }
    }

    public ActionExecutor(final TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public void processAction(final ActionTarget actionTarget) {
        taskExecutor.execute(new ActionTask(actionTarget));
    }

}
