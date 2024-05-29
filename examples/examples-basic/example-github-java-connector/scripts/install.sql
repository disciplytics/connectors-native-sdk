-- Copyright (c) 2024 Snowflake Inc.
SET APP_NAME = '&{APP_NAME}';
SET APP_INSTANCE_NAME = $APP_NAME || '_INSTANCE';

-- Create stage
CREATE OR REPLACE DATABASE IDENTIFIER('&STAGE_DB');
CREATE STAGE IF NOT EXISTS IDENTIFIER('&STAGE_NAME');

PUT 'file://sf_build/*' @&{STAGE_NAME}/&APP_VERSION AUTO_COMPRESS = FALSE overwrite=true;

-- App setup
DROP APPLICATION PACKAGE IF EXISTS IDENTIFIER($APP_NAME);
CREATE APPLICATION PACKAGE IF NOT EXISTS IDENTIFIER($APP_NAME);
ALTER APPLICATION PACKAGE IDENTIFIER($APP_NAME) ADD VERSION &APP_VERSION USING @&{STAGE_NAME}/&APP_VERSION;

-- Dev: install the app
DROP APPLICATION IF EXISTS IDENTIFIER($APP_INSTANCE_NAME) CASCADE;
CREATE APPLICATION IDENTIFIER($APP_INSTANCE_NAME) FROM APPLICATION PACKAGE IDENTIFIER($APP_NAME) USING VERSION &APP_VERSION;
