#!/bin/bash

# Define variables for paths and remote settings
KEY_PATH="~/.ssh/id_rsa_new"
JAR_PATH="/Volumes/workspace/projects/carbarn/Scrapify/target/Scrapify-1.jar"
REMOTE_TEMP_PATH="C:/Users/Administrator"
REMOTE_PATH="D:/CI-CD/scrappers"
REMOTE_USER="Administrator"
REMOTE_HOST="44.194.94.140"
JAR_NAME="Scrapify-1.jar"
CONTEXT_PATH="/scrapify"
PORT=8020

# Step 1: Upload the new JAR file to the temporary folder
echo "Uploading new JAR file to temporary folder..."
scp -i "$KEY_PATH" "$JAR_PATH" "$REMOTE_USER@$REMOTE_HOST:$REMOTE_TEMP_PATH/$JAR_NAME"

# Step 2: SSH into the EC2 instance to check for Java processes on port, stop the existing process, and delete the existing JAR
echo "Checking for any running instance of the application on port $PORT and stopping it..."
ssh -i "$KEY_PATH" "$REMOTE_USER@$REMOTE_HOST" "powershell.exe -Command \"\$process = Get-NetTCPConnection -LocalPort $PORT -State Listen | Select-Object -ExpandProperty OwningProcess; if (\$process) { Stop-Process -Id \$process -Force; Write-Host 'Stopped process with PID: \$process'; } else { Write-Host 'No process found running on port $PORT.'; }; Remove-Item -Path '$REMOTE_PATH/$JAR_NAME' -Force; Write-Host 'Existing JAR file deleted if it existed.'\""

# Step 3: Move the new JAR file from the temporary folder to the target location
echo "Moving new JAR file from temporary location to $REMOTE_PATH..."
ssh -i "$KEY_PATH" "$REMOTE_USER@$REMOTE_HOST" "powershell.exe -Command \"Move-Item -Path '$REMOTE_TEMP_PATH/$JAR_NAME' -Destination '$REMOTE_PATH/$JAR_NAME' -Force; Write-Host 'Moved new JAR file successfully.'\""

# Step 4: Start the new application
echo "Starting the new application..."
ssh -i "$KEY_PATH" "$REMOTE_USER@$REMOTE_HOST" "powershell.exe -Command \"Start-Process java -ArgumentList '-jar $REMOTE_PATH/$JAR_NAME --server.port=$PORT --spring.profiles.active=prod --server.servlet.context-path=$CONTEXT_PATH' -NoNewWindow -Wait; Write-Host 'Application started successfully!'\""

# Final message
echo "Deployment script completed successfully!"
