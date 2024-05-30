pipeline {
    agent any
    environment {
        APP_NAME = 'scrapify'
        JAR_FILE = 'Scrapify-1.jar'
        REMOTE_DIR = 'D:\\app_data_collection'
        WORKSPACE_DIR = ''
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
                script {
                    // Store the workspace directory path
                    env.WORKSPACE_DIR = pwd()

                    // Build the project with Maven, using the production profile
                    bat 'mvn clean package -Pprod'

                    // List the contents of the target directory
                    bat 'dir target'
                }
            }
        }
        stage('Deploy') {
            steps {
                script {
                    def jarFile = "${env.WORKSPACE_DIR}\\target\\${env.JAR_FILE}"
                    def remoteDir = "${env.REMOTE_DIR}"
                    def javaCommand = "java -jar ${remoteDir}\\${env.JAR_FILE} > ${remoteDir}\\output.log 2>&1 &"

                    // Debugging step to print paths
                    echo "JAR File Path: ${jarFile}"
                    echo "Remote Directory: ${remoteDir}"

                    // Ensure remote directory exists
                    bat """
                    if not exist "${remoteDir}" mkdir "${remoteDir}"
                    """

                    // Copy the JAR file to the remote directory
                    bat """
                    copy "${jarFile}" "${remoteDir}\\"
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
