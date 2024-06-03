/** Copyright (c) 2024 Snowflake Inc. */
package com.snowflake.connectors.application.lifecycle.resume;

import com.snowflake.connectors.common.response.ConnectorResponse;

/**
 * Validator called during the {@link ResumeConnectorHandler} execution, may be used to provide
 * custom connector state validation.
 *
 * <p>Default implementation of this validator calls the {@code PUBLIC.RESUME_CONNECTOR_VALIDATE}
 * procedure.
 */
@FunctionalInterface
public interface ResumeConnectorStateValidator {

  /**
   * Validates the state of the connector.
   *
   * @return a response with the code {@code OK} if the validation was successful, otherwise a
   *     response with an error code and an error message
   */
  ConnectorResponse validate();
}
