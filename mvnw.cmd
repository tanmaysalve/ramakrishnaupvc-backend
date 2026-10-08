@echo off
setlocal
set MVN_VERSION=3.9.11
set BASE=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MVN_VERSION%
set MAVEN_HOME=%BASE%\apache-maven-%MVN_VERSION%
if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  if not exist "%BASE%" mkdir "%BASE%"
  powershell -NoProfile -Command "Invoke-WebRequest -UseBasicParsing 'https://archive.apache.org/dist/maven/maven-3/%MVN_VERSION%/binaries/apache-maven-%MVN_VERSION%-bin.zip' -OutFile '%BASE%\maven.zip'"
  powershell -NoProfile -Command "Expand-Archive -Force '%BASE%\maven.zip' '%BASE%'"
)
call "%MAVEN_HOME%\bin\mvn.cmd" %*
endlocal
