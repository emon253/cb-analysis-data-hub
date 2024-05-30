pipeline {
    agent any
    environment {
        TOMCAT_USER = 'carbarn'
        TOMCAT_PASSWORD = 'carbarn@23'
        TOMCAT_HOST = 'localhost'
        TOMCAT_PORT = '8090'
        APP_NAME = 'scrapify'
        WAR_FILE = 'target/Scrapify-1.war'
    }
    stages {
        stage('Checkout') {
            steps {
                cleanWs()
                git branch: 'main', url: 'https://github.com/emon253/cb-analysis-data-hub.git'
            }
        }
        stage('Build') {
            steps {
                bat 'mvn clean package -Pprod'
            }
        }
        stage('Deploy') {
            steps {
                script {
                    def warFile = "${env.WAR_FILE}"
                    def tomcatUrl = "http://${env.TOMCAT_HOST}:${env.TOMCAT_PORT}/manager/text/deploy?path=/${env.APP_NAME}&update=true"
                    def curlCommand = "curl -u ${env.TOMCAT_USER}:${env.TOMCAT_PASSWORD} -T \"${warFile}\" \"${tomcatUrl}\""

                    bat curlCommand
                }
            }
        }
    }
}
