pipeline {
    agent any
    environment {
        APP_NAME = 'scrapify'
        JAR_FILE = 'Scrapify-1.jar'
        REMOTE_DIR = 'D:\\app_data_collection'
        JAR_FILE_PATH = "target\\Scrapify-1.jar"
    }
    stages {
        stage('Checkout') {
            steps {
                cleanWs()
                // Checkout code from the repository
                git url: 'https://github.com/emon253/cb-analysis-data-hub.git', branch: 'main'
            }
        }
        stage('Build') {
            steps {
                // Build the project with Maven, using the production profile
                bat 'mvn clean package -Pprod'

                // List the contents of the target directory
                bat 'dir target'
            }
        }
        stage('Deploy') {
            steps {
                script {
                    // Construct the command to run the JAR file
                    def javaCommand = "java -jar ${REMOTE_DIR}\\${JAR_FILE} > ${REMOTE_DIR}\\output.log 2>&1 &"

                    // Debugging step to print paths
                    echo "JAR File Path: ${JAR_FILE_PATH}"
                    echo "Remote Directory: ${REMOTE_DIR}"

                    // Ensure remote directory exists
                    bat """
                    if not exist "${REMOTE_DIR}" mkdir "${REMOTE_DIR}"
                    """

                    // Copy the JAR file to the remote directory
                    bat """
                    copy "${JAR_FILE_PATH}" "${REMOTE_DIR}\\"
                    """

                    // Kill any running instance of the application
                    bat """
                    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8082') do taskkill /f /pid %%a
                    """

                    // Start the new JAR file
                    bat """
                    start /b cmd /c "${javaCommand}"
                    """
                }
            }
        }
    }
}
