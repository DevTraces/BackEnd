PROJECT_ROOT="/home/ubuntu/app/deploy"		# 여기서는 배포할 프로젝트가 위치할 디렉토리
JAR_FILE="$PROJECT_ROOT/spring-project.jar"	# 배포할 프로젝트가 빌드된 jar 파일

DEPLOY_LOG="$PROJECT_ROOT/deploy.log"

TIME_NOW=$(date +%c)

CURRENT_PID=$(pgrep -f $JAR_FILE)

if [ -z $CURRENT_PID ]; then
        echo "$TIME_NOW > 현재 실행중인 애플리케이션이 없습니다" >> $DEPLOY_LOG
else
        echo "$TIME_NOW > 실행중인 $CURRENT_PID 애플리케이션 종료 " >> $DEPLOY_LOG
        kill -15 $CURRENT_PID
fi