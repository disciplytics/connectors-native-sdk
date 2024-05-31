/** Copyright (c) 2024 Snowflake Inc. */
package com.snowflake.connectors.taskreactor.registry;

import static com.snowflake.connectors.common.assertions.NativeSdkAssertions.assertThat;

import com.snowflake.connectors.common.object.Identifier;
import com.snowflake.connectors.taskreactor.BaseTaskReactorIntegrationTest;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class InstanceRegistryRepositoryIntegrationTest extends BaseTaskReactorIntegrationTest {

  InstanceRegistryRepository repository = new DefaultInstanceRegistryRepository(session);

  @AfterEach
  void cleanup() {
    dropInstances();
  }

  @Test
  void shouldReturnEmptyListWhenNoInstanceIsConfigured() {
    // when
    List<TaskReactorInstance> instances = repository.fetchAll();

    // then
    assertThat(instances).isEmpty();
  }

  @Test
  void shouldReturnAllInstances() {
    // given
    TaskReactorInstance first =
        new TaskReactorInstance(Identifier.fromWithAutoQuoting("ziemniaczek"), true, true);
    TaskReactorInstance second =
        new TaskReactorInstance(Identifier.fromWithAutoQuoting("kartofelek"), false, false);
    instanceRecordExists(first);
    instanceRecordExists(second);

    // when
    List<TaskReactorInstance> instances = repository.fetchAll();

    // then
    assertThat(instances).containsExactlyInAnyOrder(first, second);
  }

  @Test
  void shouldSetActive() {
    // given
    Identifier instanceName = Identifier.fromWithAutoQuoting("IBelieveICanFly");
    TaskReactorInstance first = new TaskReactorInstance(instanceName, true, false);
    instanceRecordExists(first);

    // when
    repository.setActive(instanceName);

    // then
    TaskReactorInstance instance = repository.fetch(instanceName);
    assertThat(instance.isActive()).isTrue();
  }

  @Test
  void shouldSetInactive() {
    // given
    Identifier instanceName = Identifier.fromWithAutoQuoting("IBelieveICanTouchTheSky");
    TaskReactorInstance first = new TaskReactorInstance(instanceName, true, true);
    instanceRecordExists(first);

    // when
    repository.setInactive(instanceName);

    // then
    TaskReactorInstance instance = repository.fetch(instanceName);
    assertThat(instance.isActive()).isFalse();
  }

  private void instanceRecordExists(TaskReactorInstance instance) {
    session
        .sql(
            String.format(
                "INSERT INTO TASK_REACTOR_INSTANCES.INSTANCE_REGISTRY (INSTANCE_NAME,"
                    + " IS_INITIALIZED, IS_ACTIVE) VALUES ('%s', %s, %s)",
                instance.instanceName().toSqlString(),
                instance.isInitialized(),
                instance.isActive()))
        .collect();
  }

  private void dropInstances() {
    session.sql("TRUNCATE TABLE TASK_REACTOR_INSTANCES.INSTANCE_REGISTRY").collect();
  }
}
