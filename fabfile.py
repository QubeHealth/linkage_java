import logging
from fabric import Connection, task
from invoke import run

# Update these variables with your GCP instance details
HOST = '34.93.59.36'  # Optional if using hostname
USER = 'root'
KEY_FILE_NAME = '/Users/muhammedafthad/.ssh/gcp_instance_key'

APP_NAME = 'linkage'
JAR_FILE = 'target/' + APP_NAME + '-1.0-SNAPSHOT'+'.jar'  # Change this to your actual JAR file path
CONFIG_FILE = 'config.yml'  # Path to your config.yml file
OUTPUT_LOG = '/Users/muhammedafthad/MASTER_JAVA/server_log.txt'
PORT=5016

# Remote paths
REMOTE_PROJECT_DIR = '/root/java_service/' + APP_NAME + '/'
REMOTE_JAR_PATH = REMOTE_PROJECT_DIR + APP_NAME + '.jar'
REMOTE_CONFIG_PATH = REMOTE_PROJECT_DIR + 'config.yml'
REMOTE_NOHUP_LOG_PATH = REMOTE_PROJECT_DIR + 'nohup.out'  # Path to the nohup log file

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(message)s')
logger = logging.getLogger(__name__)
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
def build(c):
    """Package the local project build.
    example: fab build
    """
    logger.info(f"{bcolors.OKGREEN}Starting to package the local project build...{bcolors.ENDC}")
    run('mvn clean package')
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
def start_server(c):
    """Start the Dropwizard application.
    example: fab start-server
    """
    with conn.cd(REMOTE_PROJECT_DIR):
        logger.info(f"{bcolors.OKBLUE}Starting the Dropwizard application...{bcolors.ENDC}")
        conn.run(f'nohup java -jar {REMOTE_JAR_PATH} server {REMOTE_CONFIG_PATH} &')
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
    start_server(c)
    logger.info(f"{bcolors.OKGREEN}Full deployment process completed{bcolors.ENDC}")

@task
def update_server(c):
    """Update the binary file and restart the server."""
    logger.info(f"{bcolors.OKBLUE}Starting binary update process...{bcolors.ENDC}")
    build(c)
    stop_server(c)
    put_binary(c)
    start_server(c)
    logger.info(f"{bcolors.OKGREEN}Binary update process completed{bcolors.ENDC}")
