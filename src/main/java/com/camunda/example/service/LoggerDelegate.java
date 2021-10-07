/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camunda.example.service;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.model.bpmn.BpmnModelException;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component("logger")
@Scope("prototype")
public class LoggerDelegate implements JavaDelegate {

  @Setter
  private Expression injectedProperty;

  public void execute(DelegateExecution exec) {

    log.info("\n LoggerDelegate invoked activityName: '{}'," +
            " processInstanceId: {}, businessKey: {}, executionId: {}, modelName: {}, elementId: {} \n",
        exec.getCurrentActivityName().replaceAll("\n", " "),
        exec.getProcessInstanceId(),
        exec.getProcessBusinessKey(),
        exec.getId(),
        exec.getBpmnModelInstance().getModel().getModelName(),
        exec.getBpmnModelElementInstance().getId()
    );

    // log process data
    log.info("--- Variables ---");
    Map<String, Object> variables = exec.getVariables();
    for (Map.Entry<String, Object> entry : variables.entrySet())
      log.info("data id: {} data value: {}",entry.getKey(), entry.getValue());

    // log injected property if set
    if (injectedProperty != null)
      log.info("Injected property: {}", injectedProperty.getValue(exec));

    // log extension properties if any are defined
    ExtensionElements extensionElements = exec.getBpmnModelElementInstance().getExtensionElements();
    if (extensionElements != null) {
      try {
        CamundaProperties camProps = extensionElements
            .getElementsQuery().filterByType(CamundaProperties.class).singleResult();
        if (camProps != null) {
          for (CamundaProperty prop : camProps.getCamundaProperties())
            log.info("Camunda property {} with value {}", prop.getCamundaName(), prop.getCamundaValue());
        }
      } catch (BpmnModelException e) {}
    }
    log.info("\n");
  }

  public void executeWith(DelegateExecution exec, String param) {
    log.info("executeWith method -->");
    log.info("Parameter from executeWith method: {}", param);
    execute(exec);
  }

}