/** Copyright (c) 2024 Snowflake Inc. */
package com.snowflake.connectors;

import static com.snowflake.connectors.util.ConnectorStatus.CONFIGURING;
import static com.snowflake.connectors.util.ConnectorStatus.ConnectorConfigurationStatus.INSTALLED;
import static com.snowflake.connectors.util.ResponseAssertions.assertThat;
import static java.lang.String.format;

import com.snowflake.connectors.application.Application;
import com.snowflake.connectors.util.ConnectorStatus;
import com.snowflake.connectors.util.ConnectorStatus.ConnectorConfigurationStatus;
import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.Session;
import com.snowflake.snowpark_java.types.Variant;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.Random.class)
public class BaseNativeSdkIntegrationTest {

  protected static final String WAREHOUSE = "XS";

  protected Session session;
  protected Application application;

  @BeforeEach
  public void beforeEach() {
    executeInApp("TRUNCATE TABLE IF EXISTS STATE.APP_CONFIG");
    executeInApp("TRUNCATE TABLE IF EXISTS STATE.APP_STATE");
    executeInApp("TRUNCATE TABLE IF EXISTS STATE.INGESTION_RUN");
    executeInApp("TRUNCATE TABLE IF EXISTS STATE.INGESTION_PROCESS");
    executeInApp("TRUNCATE TABLE IF EXISTS STATE.RESOURCE_INGESTION_DEFINITION");
    setConnectorStatus(CONFIGURING, INSTALLED);
  }

  @BeforeAll
  public void beforeAll() throws IOException {
    session = SnowparkSessionProvider.createSession();
    application = Application.createNewInstance(session);

    session.sql("USE DATABASE " + application.instanceName).collect();
    session.sql("USE SCHEMA PUBLIC").collect();

    application.grantUsageOnWarehouse(WAREHOUSE);
    application.grantExecuteTaskPrivilege();
  }

  @AfterAll
  public void afterAll() {
    application.dropInstance();
  }

  protected Map<String, Variant> callProcedure(String procedureQuery) {
    Variant response = callProcedureRaw(procedureQuery);
    return response == null ? new HashMap<>() : response.asMap();
  }

  protected Variant callProcedureRaw(String procedureQuery) {
    return session.sql("CALL PUBLIC." + procedureQuery).collect()[0].getVariant(0);
  }

  protected Row[] executeInApp(String query) {
    var escapedQuery = query.replace("'", "\\'");
    return session.sql(format("CALL PUBLIC.EXECUTE_SQL('%s')", escapedQuery)).collect();
  }

  protected void setConnectorStatus(
      ConnectorStatus status, ConnectorConfigurationStatus configurationStatus) {
    setConnectorStatus(status.toString(), configurationStatus.toString());
  }

  protected void setConnectorStatus(String status, String configurationStatus) {
    String connectorStatus =
        format(
            "OBJECT_CONSTRUCT('status', '%s', 'configurationStatus', '%s')",
            status, configurationStatus);
    setConnectorStatusValue(connectorStatus);
  }

  private void setConnectorStatusValue(String statusObject) {
    var query =
        "MERGE INTO STATE.APP_STATE AS dst "
            + "USING (SELECT %1$s AS value) AS src "
            + "ON dst.key = '%2$s' "
            + "WHEN MATCHED THEN UPDATE SET dst.value = src.value "
            + "WHEN NOT MATCHED THEN INSERT VALUES ('%2$s', src.value, current_timestamp())";
    executeInApp(format(query, statusObject, "connector_status"));
  }

  protected void assertExternalStatus(
      ConnectorStatus connectorStatus, ConnectorConfigurationStatus configurationStatus) {
    assertExternalStatus(connectorStatus.name(), configurationStatus.name());
  }

  protected void assertExternalStatus(String expectedStatus, String expectedConfigurationStatus) {
    var response = callProcedure("GET_CONNECTOR_STATUS()");
    assertThat(response)
        .hasOkResponseCode()
        .hasField("status", expectedStatus)
        .hasField("configurationStatus", expectedConfigurationStatus);
  }

  protected void assertInternalStatus(
      ConnectorStatus connectorStatus, ConnectorConfigurationStatus configurationStatus) {
    assertInternalStatus(connectorStatus.name(), configurationStatus.name());
  }

  protected void assertInternalStatus(String expectedStatus, String expectedConfigurationStatus) {
    var query = "SELECT value FROM STATE.APP_STATE WHERE KEY = 'connector_status'";
    var status = executeInApp(query)[0].getVariant(0).asMap();
    assertThat(status)
        .hasField("status", expectedStatus)
        .hasField("configurationStatus", expectedConfigurationStatus);
  }

  protected void mockProcedure(String procedure, String responseCode, String message) {
    Map<String, Object> response = new HashMap<>();
    Optional.ofNullable(responseCode).ifPresent(v -> response.put("response_code", v));
    Optional.ofNullable(message).ifPresent(v -> response.put("message", v));
    mockProcedure(procedure, response);
  }

  protected void mockProcedure(String procedure, Map<String, Object> response) {
    var query = "CALL PUBLIC.MOCK_PROCEDURE('PUBLIC.%s', PARSE_JSON('%s'))";
    session.sql(format(query, procedure, new Variant(response).asJsonString())).collect();
  }

  protected void mockProcedureWithBody(String procedure, String body) {
    var query = "CALL PUBLIC.MOCK_PROCEDURE_WITH_BODY('PUBLIC.%s', '%s')";
    session.sql(format(query, procedure, body)).collect();
  }

  protected void mockProcedureToThrow(String procedure) {
    var query = "CALL PUBLIC.MOCK_PROCEDURE_TO_THROW('PUBLIC.%s')";
    session.sql(format(query, procedure)).collect();
  }

  protected void dropProcedure(String procedure) {
    var query = "CALL PUBLIC.DROP_PROCEDURE('PUBLIC.%s')";
    session.sql(format(query, procedure)).collect();
  }

  protected void setupWarehouseReference() {
    session
        .sql(
            format(
                "CALL PUBLIC.REGISTER_REFERENCE('WAREHOUSE_REFERENCE', 'ADD',"
                    + " SYSTEM$REFERENCE('WAREHOUSE', '%s', 'PERSISTENT', 'USAGE'))",
                WAREHOUSE))
        .collect();
  }
}
