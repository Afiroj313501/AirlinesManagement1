Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Airlines Management Chat System" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "This script will help you start the chat system" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. First, compile the Java files:" -ForegroundColor Green
Write-Host "   javac -cp . src/main/java/com/example/airlinesmanagement1/*.java" -ForegroundColor White
Write-Host ""
Write-Host "2. Start the Chat Server:" -ForegroundColor Green
Write-Host "   java -cp src/main/java com.example.airlinesmanagement1.ChatServer" -ForegroundColor White
Write-Host ""
Write-Host "3. In another terminal, start a user client:" -ForegroundColor Green
Write-Host "   java -cp src/main/java com.example.airlinesmanagement1.SimpleUser" -ForegroundColor White
Write-Host ""
Write-Host "4. For admin chat, use the JavaFX application" -ForegroundColor Green
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Read-Host "Press Enter to continue" 