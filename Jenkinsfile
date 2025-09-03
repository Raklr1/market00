pipeline {
    agent any
    tools{
        jdk 'JDK-21'
        maven 'Maven'
        //nodejs 'nodejs'
    }
    stages {
        stage('清除deployment,service') {
            steps {
                withKubeConfig([credentialsId: 'k8s1']) {
                    bat 'kubectl delete deployment backend --ignore-not-found'
                    bat 'kubectl delete deployment frontend --ignore-not-found'
                    bat 'kubectl delete service backend --ignore-not-found'
                    bat 'kubectl delete service frontend --ignore-not-found'
                }
            }
        }
         stage('清除镜像') {
            steps {
                withKubeConfig([credentialsId: 'k8s1']) {
                    script {
                    try {
                        bat 'minikube image rm market-back:v1'
                    } catch (Exception e) {
                        echo 'minikube无market-back:v1'
                    }
                    try {
                        bat 'minikube image rm market-front:v1'
                    } catch (Exception e) {
                        echo 'minikube无market-front:v1'
                    }
                    try {
                        bat 'docker image rm market-back:v1'
                    } catch (Exception e) {
                        echo 'docker无market-back:v1'
                    }
                    try {
                        bat 'docker image rm market-front:v1'
                    } catch (Exception e) {
                        echo 'docker无market-front:v1'
                    }
                    }
                }
            }
        }
        stage('部署数据库') {
            steps {
                withKubeConfig([credentialsId: 'k8s1']) {

                    bat 'minikube update-context'
                    bat 'minikube image load mysql:8.0.43'
                    bat 'kubectl apply -f mysqlpv.yaml'
                    bat 'kubectl apply -f init.yaml'
                    bat 'kubectl apply -f mysql-deployment.yaml'
                    bat 'kubectl apply -f mysql-service.yaml'
                }
            }
        }
        stage('构建后端，导入minikube') {
            steps {
                withKubeConfig([credentialsId: 'k8s1']) {
                bat '''
                cd ./backend
                mvn clean package
                '''
                bat '''
                cd ./backend
                docker build -t market-back:v1 .
                '''
                bat 'minikube image load market-back:v1'
                }
            }
        }
        stage('部署后端') {
            steps {
                withKubeConfig([credentialsId: 'k8s1']) {
                    bat 'kubectl apply -f backend-deployment.yaml'
                    bat 'kubectl apply -f backend-service.yaml'
                }
            }
        }
        stage('构建前端，导入minikube') {
            steps {
                withKubeConfig([credentialsId: 'k8s1']) {
                bat '''
                cd ./frontend
                npm install
                
                '''
                bat '''
                cd ./frontend
                npm run build
                '''
                bat '''
                cd ./frontend
                docker build -t market-front:v1 .
                '''
                bat 'minikube image load market-front:v1'
                }
            }
        }
        stage('部署前端') {
            steps {
                withKubeConfig([credentialsId: 'k8s1']) {
                    bat 'kubectl apply -f frontend-deployment.yaml'
                    bat 'kubectl apply -f frontend-service.yaml'
                }
            }
        }
        
    }
    post {
        always {
            echo "Kubernetes自动部署完成"
        }
    }
    triggers {
        githubPush()
    }
}
