/** Copyright (c) 2024 Snowflake Inc. */
package com.snowflake.connectors.application.lifecycle.resume;

import com.snowflake.connectors.common.response.ConnectorResponse;

/**
 * Callback called during the {@link ResumeConnectorHandler} execution, may be used to provide
 * custom resuming logic.
 *
 * <p>Default implementation of this callback calls the {@code PUBLIC.RESUME_CONNECTOR_INTERNAL}
 * procedure.
 */
@FunctionalInterface
public interface ResumeConnectorCallback {

  /**
   * Executes the callback logic.
   *
   * @return a response with the code {@code OK} if the execution was successful, a response with
   *     the code {@link
   *     com.snowflake.connectors.application.lifecycle.LifecycleService#ROLLBACK_CODE ROLLBACK} if
   *     the callback performed a rollback operation, or a response with an error code and an error
   *     message
   */
  ConnectorResponse execute();
}
