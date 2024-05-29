pipeline {
    agent any
    environment {
        APP_NAME = 'scrapify'
        JAR_FILE = 'target/Scrapify-1.jar'
        REMOTE_DIR = 'D:\app_data_collection'
    }
    stages {
        stage('Checkout') {
            steps {
                // Checkout code from the repository
                git 'https://github.com/emon253/cb-analysis-data-hub.git'
            }
        }
        stage('Build') {
            steps {
                // Build the project with Maven, using the production profile
                bat 'mvn clean package -Pprod'
            }
        }
        stage('Deploy') {
            steps {
                script {
                    def jarFile = "${env.JAR_FILE}"
                    def remoteDir = "${env.REMOTE_DIR}"
                    def javaCommand = "java -jar ${remoteDir}\\${jarFile} > ${remoteDir}\\output.log 2>&1 &"

                    // Copy the JAR file to the remote directory
                    bat """
                    if not exist ${remoteDir} mkdir ${remoteDir}
                    copy ${jarFile} ${remoteDir}
                    """

                    // Kill any running instance of the application
                    bat """
                    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080') do taskkill /f /pid %%a
                    """

                    // Start the new JAR file
                    bat """
                    start /b cmd /c ${javaCommand}
                    """
                }
            }
        }
    }
}
