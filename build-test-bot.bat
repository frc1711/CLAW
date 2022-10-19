rmdir "test-bot/src/main/java/rct" /s /q
robocopy "lib/rct" "test-bot/src/main/java/rct" /E

cd test-bot
call gradlew.bat deploy
cd ..
