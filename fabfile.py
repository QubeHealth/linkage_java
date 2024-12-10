import logging
from fabric import Connection, task
from invoke import run
import json
import os
from datetime import datetime
import platform


# Configure logging
logging.basicConfig(level=logging.INFO, format='%(message)s')
logger = logging.getLogger(__name__)

class bcolors:
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'


def load_config(env):
    with open('fab_config.json') as config_file:
        config = json.load(config_file)
    return config.get(env, {})

# Environment setup
ENVIRONMENT = 'uat'  # You can change this to 'local' or 'production' as needed
config = load_config(ENVIRONMENT)

# Update these variables with your GCP instance details

HOST = config.get("host")
USER = config.get("username")
KEY_FILE_NAME = config.get('ssh_path')

APP_NAME = 'linkage'
JAR_FILE = 'target/' + APP_NAME + '-1.0-SNAPSHOT'+'.jar'  # Change this to your actual JAR file path
CONFIG_FILE = 'config.yml'  # Path to your config.yml file
OUTPUT_LOG = 'server_log.log'
PORT=5016

# Remote paths
REMOTE_PROJECT_DIR = '/root/java_service/' + APP_NAME + '/'
REMOTE_JAR_PATH = REMOTE_PROJECT_DIR + APP_NAME+ '.jar'
REMOTE_CONFIG_PATH = REMOTE_PROJECT_DIR + 'config.yml'
REMOTE_NOHUP_LOG_PATH = REMOTE_PROJECT_DIR + APP_NAME +'.out'  # Path to the nohup log file
PUSH_HISTORY_FILE = REMOTE_PROJECT_DIR + "push_history.log"
# Configure logging
def get_current_branch():
    result = run("git rev-parse --abbrev-ref HEAD", hide=True)
    return result.stdout.strip()

current_branch = get_current_branch()
logger.info(f"{bcolors.WARNING}You are on {ENVIRONMENT} server Host: {HOST}\nUsername: {USER}{bcolors.ENDC}")
logger.info(f"{bcolors.FAIL}Current Git branch: {current_branch}{bcolors.ENDC}")

conn = Connection(host=HOST, user=USER, connect_kwargs={"key_filename": KEY_FILE_NAME})

class bcolors:
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'

@task
def ensure_push_history_file(c):
    """
    Ensure the push history file exists on the remote server.
    """
    try:
        result = conn.run(f"test -f {PUSH_HISTORY_FILE} && echo 'exists' || echo 'not_exists'", hide=True)
        if result.stdout.strip() == "not_exists":
            conn.run(f"echo '=== Push History Log ===' > {PUSH_HISTORY_FILE}")
            logger.info(f"{bcolors.OKGREEN}{PUSH_HISTORY_FILE} created successfully on remote server{bcolors.ENDC}")
        else:
            logger.info(f"{bcolors.OKBLUE}{PUSH_HISTORY_FILE} already exists on remote server{bcolors.ENDC}")
    except Exception as e:
        logger.error(f"{bcolors.FAIL}Error ensuring push history file: {e}{bcolors.ENDC}")

@task
def append_push_history_record_remote(c):
    """
    Append a record to the push history file on the remote server.
    """
    try:
        # Ensure the push history file exists
        ensure_push_history_file(c)

        # Gather branch name
        result = run("git rev-parse --abbrev-ref HEAD", hide=True)
        branch_name = result.stdout.strip()

        # Gather system and user details
        os_info = platform.platform()
        pc_name = platform.node()
        os_user = os.getenv("USER") or os.getenv("USERNAME", "Unknown User")
        timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

        # Prepare user details
        user_details = f"Branch: {branch_name}, User: {os_user}, PC: {pc_name}"

        # Prepare the record
        record = f"""
        [{timestamp}] Branch: {branch_name}, OS: {os_info}, PC: {pc_name}, User: {os_user}\n"
        """

        # Append the record to the file on the remote server
        conn.run(f"echo '{record}' >> {PUSH_HISTORY_FILE}")
        logger.info(f"{bcolors.OKGREEN}Push history record added successfully to {PUSH_HISTORY_FILE}{bcolors.ENDC}")
    except Exception as e:
        logger.error(f"{bcolors.FAIL}Failed to append push history record: {e}{bcolors.ENDC}")

@task
def get_latest_push_history(c):
    """
    Retrieve the latest push history record from the remote server.
    """
    try:
        logger.info(f"{bcolors.OKBLUE}Fetching the latest push history records...{bcolors.ENDC}")
        result = conn.run(f"tail -n 20 {PUSH_HISTORY_FILE}", hide=True)  # Adjust the number of lines to fetch

        if result.stdout.strip():
            logger.info(f"\n{bcolors.BOLD}{bcolors.OKGREEN}=== Latest Push History Records ==={bcolors.ENDC}")
            logger.info(f"{bcolors.WARNING}{result.stdout.strip()}{bcolors.ENDC}")
            logger.info(f"{bcolors.OKGREEN}==================================={bcolors.ENDC}")
        else:
            logger.warning(f"\n{bcolors.BOLD}{bcolors.WARNING}No records found in {PUSH_HISTORY_FILE}{bcolors.ENDC}")
    except Exception as e:
        logger.error(f"\n{bcolors.BOLD}{bcolors.FAIL}Failed to fetch the latest push history record:{bcolors.ENDC} {e}")
@task
def download_push_history(c):
    """
    Download the push history file from the remote server to the local machine.
    """
    local_file = "push_history.log"  # Name of the file on your local machine
    try:
        logger.info(f"{bcolors.OKBLUE}Downloading {PUSH_HISTORY_FILE} from the server...{bcolors.ENDC}")
        conn.get(remote=PUSH_HISTORY_FILE, local=local_file)
        logger.info(f"{bcolors.OKGREEN}Push history file downloaded successfully as {local_file}{bcolors.ENDC}")
    except Exception as e:
        logger.error(f"{bcolors.FAIL}Failed to download the push history file: {e}{bcolors.ENDC}")


@task
def build(c):
    """Package the local project build.
    example: fab build
    """
    logger.info(f"{bcolors.OKGREEN}Starting to package the local project build...{bcolors.ENDC}")
    run('mvn clean install')
    logger.info(f"{bcolors.OKGREEN}Packaged local project into {JAR_FILE}{bcolors.ENDC}")

@task
def put_binary(c):
    """Upload the binary file and config file to the GCP instance.
    example: fab put-binary
    """
    logger.info(f"{bcolors.OKBLUE}Starting deployment to the GCP instance...{bcolors.ENDC}")
    logger.info(f"{bcolors.OKBLUE}Uploading the build {JAR_FILE} jar file to the remote server path {REMOTE_PROJECT_DIR} ...{bcolors.ENDC}")
    conn.put(JAR_FILE, REMOTE_JAR_PATH)
    logger.info(f"{bcolors.OKGREEN}Uploaded {JAR_FILE} to {REMOTE_JAR_PATH}{bcolors.ENDC}")

@task
def put_config(c):
     logger.info(f"{bcolors.OKBLUE}Uploading the config {CONFIG_FILE} file to the remote server path {REMOTE_PROJECT_DIR} ...{bcolors.ENDC}")
     conn.put(CONFIG_FILE, REMOTE_CONFIG_PATH)
     logger.info(f"{bcolors.OKGREEN}Uploaded {CONFIG_FILE} to {REMOTE_CONFIG_PATH}{bcolors.ENDC}")

@task
def put_ssh_file(c):
     logger.info(f"{bcolors.OKBLUE}Uploading the config {CONFIG_FILE} file to the remote server path {REMOTE_PROJECT_DIR} ...{bcolors.ENDC}")
     conn.put('sslPrivateKey.p1',REMOTE_PROJECT_DIR)
     logger.info(f"{bcolors.OKGREEN}Uploaded {CONFIG_FILE} to {REMOTE_CONFIG_PATH}{bcolors.ENDC}")

@task
def start_server(c):
    """Start the Dropwizard application.
    example: fab start-server
    """
    with conn.cd(REMOTE_PROJECT_DIR):
        logger.info(f"{bcolors.OKBLUE}Starting the Dropwizard application...{bcolors.ENDC}")
        conn.run(f'nohup java -jar {REMOTE_JAR_PATH} server {REMOTE_CONFIG_PATH} > {REMOTE_NOHUP_LOG_PATH} 2>&1 &')
        logger.info(f"{bcolors.OKGREEN}Application started{bcolors.ENDC}")

@task
def setup_server(c):
    """Setup the server with necessary software."""
    logger.info(f"{bcolors.OKBLUE}Starting server setup...{bcolors.ENDC}")
    
    logger.info(f"{bcolors.OKBLUE}Updating package lists...{bcolors.ENDC}")
    conn.run('sudo apt update')
    logger.info(f"{bcolors.OKGREEN}Package lists updated{bcolors.ENDC}")

    logger.info(f"{bcolors.OKBLUE}Installing Java...{bcolors.ENDC}")
    conn.run('sudo apt install -y openjdk-11-jdk')
    logger.info(f"{bcolors.OKGREEN}Java installed{bcolors.ENDC}")

@task
def stop_server(c):
    """ Stop the Dropwizard application.
    example: fab stop-server
    """
    logger.info(f"{bcolors.WARNING}Stopping the application...{bcolors.ENDC}")
    conn.run(f'fuser -k {PORT}/tcp')
    logger.info(f"{bcolors.FAIL}Application stopped{bcolors.ENDC}")

@task
def get_live_log(c):
    """
    Tail the application logs
    """
    conn.run(f"tail -f {REMOTE_NOHUP_LOG_PATH}")

@task
def get_full_log(c):
    """
    Retrieve the full application log.
    """
    conn.run(f"cat {REMOTE_NOHUP_LOG_PATH}")

@task
def download_log(c):
    """
    Download the application log file from the remote server.
    """
    logger.info(f"{bcolors.OKBLUE}Downloading logs{bcolors.ENDC}")
    conn.get(remote=REMOTE_NOHUP_LOG_PATH, local=OUTPUT_LOG)
    logger.info(f"{bcolors.OKGREEN}Downloaded Logs in {OUTPUT_LOG}{bcolors.ENDC}")

@task
def get_error_log(c):
    """
    Tail the application error logs
    """
    conn.run(f"tail -f {REMOTE_NOHUP_LOG_PATH}")

@task
def deploy_server(c):
    """Run full deployment: package, setup server, and deploy."""
    logger.info(f"{bcolors.OKBLUE}Starting full deployment process...{bcolors.ENDC}")
    build(c)
    put_binary(c)
    append_push_history_record_remote(c)
    start_server(c)
    logger.info(f"{bcolors.OKGREEN}Full deployment process completed{bcolors.ENDC}")

@task
def update_server(c):
    """Update the binary file and restart the server."""
    logger.info(f"{bcolors.OKBLUE}Starting binary update process...{bcolors.ENDC}")
    build(c)
    stop_server(c)
    put_binary(c)
    append_push_history_record_remote(c)
    start_server(c)
    logger.info(f"{bcolors.OKGREEN}Binary update process completed{bcolors.ENDC}")