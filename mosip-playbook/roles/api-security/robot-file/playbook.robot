*** Settings ***
Library  Collections
Library  RoboZap  https://127.0.0.1:<port>  <port>
Library  RoboFunctionalTest

*** Variables ***
${TARGET_NAME}  <target_name>  #tochange
${TARGET_URI}  <target_uri>  #tochange

#CONFIG
${RESULTS_PATH}  <report_dir>  #tochange

#ZAP
${ZAP_PATH}  <zap_dir>  #tochange
${CONTEXT}  <context>  #tochange
${REPORT_TITLE}  Mosip-Test-Report-ZAP  #tochange
${REPORT_FORMAT}  json  #tochange
${ZAP_REPORT_FILE}  MOSIP.json  #tochange
${REPORT_AUTHOR}  <author>  #tochange
${SCANPOLICY}  <scan_policy>  #tochange

#TEST-RIG
${PATH}  <testrig_dir>  #tochange
${MODULE}  <module>  #tochange
${ENVUSER}  <envuser>  #tochange
${TESTLEVEL}  <testlevel>  #tochange
${HOST}  <host>  #tochange
${PORT}  <port>  #tochange


*** Test Cases ***
Initialize ZAP
    [Tags]  zap_init
    start gui zap  ${ZAP_PATH}
    sleep  20
    zap open url  ${TARGET_URI}

ZAP Contextualize
    [Tags]  zap_context
    ${contextid}=  Run Keyword and Ignore Error  zap define context  ${CONTEXT}  ${TARGET_URI}
    Run Keyword and Ignore Error  set suite variable  ${CONTEXT_ID}  ${contextid}
    sleep  10

Functional Test Start
    [Tags]  start_functional_test
    Run Keyword and Ignore Error  start functional test  ${PATH}  ${MODULE}  ${ENVUSER}  ${TARGET_URI}  ${TESTLEVEL}  ${HOST}  ${PORT}
    sleep  10

ZAP Active Scan
    [Tags]  zap_start_ascan
    ${scanid}=  Run Keyword and Ignore Error  zap start ascan  ${CONTEXT_ID}  ${TARGET_URI}  ${SCANPOLICY}
    Run Keyword and Ignore Error  set suite variable  ${SCAN_ID}  ${scanid}
    Run Keyword and Ignore Error  zap scan status  ${scanid}
    sleep  100

ZAP Json Report
    [Tags]  zap_write_to_json_file
    Run Keyword and Ignore Error  zap write to json file  ${TARGET_URI}  ${RESULTS_PATH}  ${REPORT_TITLE}

#ZAP Die
#    [Tags]  zap_kill
#    zap shutdown
#    sleep  10

#Write ZAP Results to DB
#    parse zap json  ${RESULTS_PATH}/${ZAP_REPORT_FILE}  ${TARGET_NAME}
