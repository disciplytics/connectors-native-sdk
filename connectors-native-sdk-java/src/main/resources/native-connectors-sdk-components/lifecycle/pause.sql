-- Copyright (c) 2024 Snowflake Inc.

CREATE OR REPLACE PROCEDURE PUBLIC.PAUSE_CONNECTOR_VALIDATE()
    RETURNS VARIANT
    LANGUAGE SQL
    EXECUTE AS OWNER
    AS
    BEGIN
        RETURN OBJECT_CONSTRUCT('response_code', 'OK');
    END;

CREATE OR REPLACE PROCEDURE PUBLIC.PAUSE_CONNECTOR_INTERNAL()
    RETURNS VARIANT
    LANGUAGE SQL
    EXECUTE AS OWNER
    AS
    BEGIN
        RETURN OBJECT_CONSTRUCT('response_code', 'OK');
    END;

CREATE OR REPLACE PROCEDURE PUBLIC.PAUSE_CONNECTOR()
    RETURNS VARIANT
    LANGUAGE JAVA
    RUNTIME_VERSION = '11'
    PACKAGES = ('com.snowflake:snowpark:1.11.0')
    IMPORTS = ('/connectors-native-sdk.jar')
    HANDLER = 'com.snowflake.connectors.application.lifecycle.pause.PauseConnectorHandler.pauseConnector';
GRANT USAGE ON PROCEDURE PUBLIC.PAUSE_CONNECTOR() TO APPLICATION ROLE ADMIN;
